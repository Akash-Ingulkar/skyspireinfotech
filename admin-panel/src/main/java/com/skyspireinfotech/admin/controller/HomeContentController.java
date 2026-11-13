package com.skyspireinfotech.admin.controller;

import com.skyspireinfotech.admin.entity.SiteContent;
import com.skyspireinfotech.admin.repository.SiteContentRepository;
import com.skyspireinfotech.admin.service.MediaStorage;
import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/home")
public class HomeContentController {

    private static final long MAX_IMAGE_BYTES = 8L * 1024 * 1024;
    private static final long MAX_VIDEO_BYTES = 50L * 1024 * 1024;

    private final SiteContentRepository siteContentRepository;
    private final MediaStorage mediaStorage;

    public HomeContentController(SiteContentRepository siteContentRepository, MediaStorage mediaStorage) {
        this.siteContentRepository = siteContentRepository;
        this.mediaStorage = mediaStorage;
    }

    @GetMapping
    public String view(Model model) {
        model.addAttribute("content", loadOrCreate());
        model.addAttribute("active", "home");
        model.addAttribute("title", "Home");
        return "admin/home";
    }

    @PostMapping
    public String save(@ModelAttribute("content") SiteContent form, RedirectAttributes redirectAttributes) {
        SiteContent content = loadOrCreate();
        content.setHeading(form.getHeading());
        content.setSubheading(form.getSubheading());
        content.setBodyText(form.getBodyText());
        siteContentRepository.save(content);
        redirectAttributes.addFlashAttribute("success", "Home page content updated.");
        return "redirect:/home";
    }

    /**
     * Hero card media. A new file is optional when the chosen type already has
     * one uploaded, so switching back and forth between image and video doesn't
     * require re-uploading.
     */
    @PostMapping("/media")
    public String saveMedia(@RequestParam("mediaType") String mediaType,
                            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                            @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                            RedirectAttributes redirectAttributes) {
        SiteContent content = loadOrCreate();
        try {
            if ("IMAGE".equals(mediaType)) {
                if (imageFile != null && !imageFile.isEmpty()) {
                    content.setImageUrl(mediaStorage.store(imageFile, MediaStorage.IMAGE_TYPES, MAX_IMAGE_BYTES, content.getImageUrl()));
                } else if (!isUpload(content.getImageUrl())) {
                    throw new IllegalArgumentException("Please choose an image to upload.");
                }
            } else if ("VIDEO".equals(mediaType)) {
                if (videoFile != null && !videoFile.isEmpty()) {
                    content.setVideoUrl(mediaStorage.store(videoFile, MediaStorage.VIDEO_TYPES, MAX_VIDEO_BYTES, content.getVideoUrl()));
                } else if (!isUpload(content.getVideoUrl())) {
                    throw new IllegalArgumentException("Please choose a video to upload.");
                }
            } else {
                throw new IllegalArgumentException("Choose Image or Video.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mediaError", e.getMessage());
            return "redirect:/home";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("mediaError", "Could not save the file. Please try again.");
            return "redirect:/home";
        }

        content.setMediaType(mediaType);
        siteContentRepository.save(content);
        redirectAttributes.addFlashAttribute("success",
                "VIDEO".equals(mediaType) ? "Hero now shows your video." : "Hero now shows your image.");
        return "redirect:/home";
    }

    /** Back to the photo built into the website; uploaded files are kept for switching back. */
    @PostMapping("/media/reset")
    public String resetMedia(RedirectAttributes redirectAttributes) {
        SiteContent content = loadOrCreate();
        content.setMediaType(null);
        siteContentRepository.save(content);
        redirectAttributes.addFlashAttribute("success", "Hero now shows the website's default photo.");
        return "redirect:/home";
    }

    private static boolean isUpload(String url) {
        return url != null && url.startsWith("/uploads/");
    }

    private SiteContent loadOrCreate() {
        return siteContentRepository.findBySectionKey("HOME").orElseGet(() -> {
            SiteContent content = new SiteContent();
            content.setSectionKey("HOME");
            return content;
        });
    }
}
