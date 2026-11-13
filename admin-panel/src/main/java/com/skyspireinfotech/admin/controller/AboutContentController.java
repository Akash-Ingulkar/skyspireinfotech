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

/**
 * About content. Shown in two places on the website: the About section on the
 * home page and the "Our story" / "Why choose us" parts of about.html.
 */
@Controller
@RequestMapping("/about")
public class AboutContentController {

    private static final long MAX_IMAGE_BYTES = 8L * 1024 * 1024;

    private final SiteContentRepository siteContentRepository;
    private final MediaStorage mediaStorage;

    public AboutContentController(SiteContentRepository siteContentRepository, MediaStorage mediaStorage) {
        this.siteContentRepository = siteContentRepository;
        this.mediaStorage = mediaStorage;
    }

    @GetMapping
    public String view(Model model) {
        model.addAttribute("content", loadOrCreate());
        model.addAttribute("active", "about");
        model.addAttribute("title", "About");
        return "admin/about";
    }

    @PostMapping
    public String save(@ModelAttribute("content") SiteContent form, RedirectAttributes redirectAttributes) {
        SiteContent content = loadOrCreate();
        content.setHeading(form.getHeading());
        content.setSubheading(form.getSubheading());
        content.setBodyText(form.getBodyText());
        content.setStatProjects(form.getStatProjects());
        content.setStatClients(form.getStatClients());
        content.setStatYears(form.getStatYears());
        content.setFeature1Title(form.getFeature1Title());
        content.setFeature1Text(form.getFeature1Text());
        content.setFeature2Title(form.getFeature2Title());
        content.setFeature2Text(form.getFeature2Text());
        content.setFeature3Title(form.getFeature3Title());
        content.setFeature3Text(form.getFeature3Text());
        siteContentRepository.save(content);
        redirectAttributes.addFlashAttribute("success", "About content updated.");
        return "redirect:/about";
    }

    @PostMapping("/image")
    public String uploadImage(@RequestParam("imageFile") MultipartFile imageFile, RedirectAttributes redirectAttributes) {
        SiteContent content = loadOrCreate();
        try {
            String previous = content.getImageUrl();
            content.setImageUrl(mediaStorage.store(imageFile, MediaStorage.IMAGE_TYPES, MAX_IMAGE_BYTES,
                    previous != null && previous.startsWith("/uploads/") ? previous : null));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mediaError", e.getMessage());
            return "redirect:/about";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("mediaError", "Could not save the image. Please try again.");
            return "redirect:/about";
        }
        siteContentRepository.save(content);
        redirectAttributes.addFlashAttribute("success", "About image updated.");
        return "redirect:/about";
    }

    /** Back to the photos built into the website. */
    @PostMapping("/image/remove")
    public String removeImage(RedirectAttributes redirectAttributes) throws IOException {
        SiteContent content = loadOrCreate();
        mediaStorage.delete(content.getImageUrl());
        content.setImageUrl(null);
        siteContentRepository.save(content);
        redirectAttributes.addFlashAttribute("success", "About image removed. The website shows its default photos.");
        return "redirect:/about";
    }

    private SiteContent loadOrCreate() {
        return siteContentRepository.findBySectionKey("ABOUT").orElseGet(() -> {
            SiteContent content = new SiteContent();
            content.setSectionKey("ABOUT");
            return content;
        });
    }
}
