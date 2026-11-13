package com.skyspireinfotech.admin.controller;

import com.skyspireinfotech.admin.entity.ContactInfo;
import com.skyspireinfotech.admin.entity.ContactMessage;
import com.skyspireinfotech.admin.entity.ServiceItem;
import com.skyspireinfotech.admin.entity.SiteContent;
import com.skyspireinfotech.admin.repository.ContactInfoRepository;
import com.skyspireinfotech.admin.repository.ContactMessageRepository;
import com.skyspireinfotech.admin.repository.ServiceItemRepository;
import com.skyspireinfotech.admin.repository.SiteContentRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public, unauthenticated endpoints the static website (skyspireinfotech.com)
 * calls from the browser:
 *  - GET  /api/site    everything a page needs that's managed in the admin panel
 *  - POST /api/contact contact-form submissions, listed under Contact -> Messages
 */
@RestController
@CrossOrigin(origins = {
        "http://localhost:5500",
        "https://skyspireinfotech.com",
        "https://www.skyspireinfotech.com"
})
public class PublicSiteApiController {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int MAX_PER_WINDOW = 5;
    private static final long WINDOW_MS = 10 * 60 * 1000L;

    private final SiteContentRepository siteContentRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final ContactMessageRepository contactMessageRepository;

    /** Recent submission times per client IP, for a simple in-memory rate limit. */
    private final Map<String, Deque<Long>> recentSubmissions = new ConcurrentHashMap<>();

    public PublicSiteApiController(SiteContentRepository siteContentRepository,
                                   ServiceItemRepository serviceItemRepository,
                                   ContactInfoRepository contactInfoRepository,
                                   ContactMessageRepository contactMessageRepository) {
        this.siteContentRepository = siteContentRepository;
        this.serviceItemRepository = serviceItemRepository;
        this.contactInfoRepository = contactInfoRepository;
        this.contactMessageRepository = contactMessageRepository;
    }

    @GetMapping("/api/site")
    public Map<String, Object> site(HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();

        ContactInfo info = contactInfoRepository.findAll().stream().findFirst().orElse(null);
        if (info != null) {
            Map<String, Object> contact = new LinkedHashMap<>();
            contact.put("email", info.getEmail());
            contact.put("phone", info.getPhone());
            contact.put("address", info.getAddress());
            body.put("contact", contact);
        }

        List<Map<String, Object>> services = serviceItemRepository.findAllByOrderByDisplayOrderAsc().stream()
                .filter(ServiceItem::isActive)
                .map(s -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("title", s.getTitle());
                    item.put("description", s.getDescription());
                    return item;
                })
                .toList();
        body.put("services", services);

        SiteContent home = siteContentRepository.findBySectionKey("HOME").orElse(null);
        if (home != null) {
            Map<String, Object> hero = new LinkedHashMap<>();
            hero.put("subheading", home.getSubheading());
            hero.put("heading", home.getHeading());
            hero.put("bodyText", home.getBodyText());
            String type = home.getMediaType();
            String url = "VIDEO".equals(type) ? home.getVideoUrl() : "IMAGE".equals(type) ? home.getImageUrl() : null;
            if (url == null || url.isBlank()) {
                type = null;
                url = null;
            } else if (url.startsWith("/")) {
                url = request.getContextPath() + url;
            }
            hero.put("mediaType", type);
            hero.put("mediaUrl", url);
            body.put("home", hero);
        }

        SiteContent about = siteContentRepository.findBySectionKey("ABOUT").orElse(null);
        if (about != null) {
            Map<String, Object> section = new LinkedHashMap<>();
            section.put("subheading", about.getSubheading());
            section.put("heading", about.getHeading());
            section.put("bodyText", about.getBodyText());
            String image = about.getImageUrl();
            if (image != null && image.startsWith("/uploads/")) {
                image = request.getContextPath() + image;
            }
            section.put("imageUrl", image == null || image.isBlank() ? null : image);
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("projects", about.getStatProjects());
            stats.put("clients", about.getStatClients());
            stats.put("years", about.getStatYears());
            section.put("stats", stats);
            section.put("features", List.of(
                    feature(about.getFeature1Title(), about.getFeature1Text()),
                    feature(about.getFeature2Title(), about.getFeature2Text()),
                    feature(about.getFeature3Title(), about.getFeature3Text())));
            body.put("about", section);
        }
        return body;
    }

    @PostMapping("/api/contact")
    public ResponseEntity<Map<String, String>> contact(@RequestBody Map<String, String> form, HttpServletRequest request) {
        // Honeypot: real visitors never see this field. Pretend success so bots don't retry.
        if (!isBlank(form.get("website"))) {
            return ResponseEntity.ok(Map.of("message", "Thank you!"));
        }

        String name = trim(form.get("name"));
        String email = trim(form.get("email"));
        String phone = trim(form.get("phone"));
        String message = trim(form.get("message"));

        if (isBlank(name) || name.length() > 120) {
            return badRequest("Please enter your name.");
        }
        if (isBlank(email) || email.length() > 200 || !EMAIL.matcher(email).matches()) {
            return badRequest("Please enter a valid email address.");
        }
        if (phone != null && phone.length() > 40) {
            return badRequest("Please enter a shorter phone number.");
        }
        if (isBlank(message) || message.length() > 5000) {
            return badRequest("Please enter a message (up to 5000 characters).");
        }
        if (!allow(request.getRemoteAddr())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many messages from your connection. Please try again in a few minutes."));
        }

        ContactMessage saved = new ContactMessage();
        saved.setName(name);
        saved.setEmail(email);
        saved.setPhone(isBlank(phone) ? null : phone);
        saved.setMessage(message);
        saved.setSubmittedAt(LocalDateTime.now());
        saved.setRead(false);
        contactMessageRepository.save(saved);

        return ResponseEntity.ok(Map.of("message", "Thank you! Your message has been sent."));
    }

    private boolean allow(String ip) {
        long now = System.currentTimeMillis();
        if (recentSubmissions.size() > 10_000) {
            recentSubmissions.clear();
        }
        Deque<Long> times = recentSubmissions.computeIfAbsent(ip == null ? "?" : ip, k -> new ArrayDeque<>());
        synchronized (times) {
            while (!times.isEmpty() && now - times.peekFirst() > WINDOW_MS) {
                times.pollFirst();
            }
            if (times.size() >= MAX_PER_WINDOW) {
                return false;
            }
            times.addLast(now);
            return true;
        }
    }

    private static Map<String, Object> feature(String title, String text) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", title);
        item.put("text", text);
        return item;
    }

    private static ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
