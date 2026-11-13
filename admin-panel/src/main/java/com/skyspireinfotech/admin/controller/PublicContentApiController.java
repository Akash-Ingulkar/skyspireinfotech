package com.skyspireinfotech.admin.controller;

import com.skyspireinfotech.admin.entity.SiteContent;
import com.skyspireinfotech.admin.repository.SiteContentRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only JSON feed of admin-managed content for the public website to
 * fetch client-side. Unauthenticated on purpose — this is the same content
 * that's already publicly visible on the live site, just served as data.
 */
@RestController
@CrossOrigin(origins = {
        "http://localhost:5500",
        "https://skyspireinfotech.com",
        "https://www.skyspireinfotech.com"
})
public class PublicContentApiController {

    private final SiteContentRepository siteContentRepository;

    public PublicContentApiController(SiteContentRepository siteContentRepository) {
        this.siteContentRepository = siteContentRepository;
    }

    @GetMapping("/api/about")
    public ResponseEntity<SiteContent> about() {
        return siteContentRepository.findBySectionKey("ABOUT")
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Hero card media for the home page. URLs are returned site-absolute
     * (e.g. "/admin/uploads/x.mp4") so the static website can use them as-is.
     * mediaType is null when the website should keep its built-in photo.
     */
    @GetMapping("/api/home")
    public Map<String, Object> home(HttpServletRequest request) {
        SiteContent content = siteContentRepository.findBySectionKey("HOME").orElse(null);
        Map<String, Object> body = new LinkedHashMap<>();
        String type = content == null ? null : content.getMediaType();
        String url = null;
        if ("IMAGE".equals(type)) {
            url = content.getImageUrl();
        } else if ("VIDEO".equals(type)) {
            url = content.getVideoUrl();
        }
        if (url == null || url.isBlank()) {
            type = null;
        } else if (url.startsWith("/")) {
            url = request.getContextPath() + url;
        }
        body.put("mediaType", type);
        body.put("mediaUrl", type == null ? null : url);
        return body;
    }
}
