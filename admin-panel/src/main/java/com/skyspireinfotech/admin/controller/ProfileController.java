package com.skyspireinfotech.admin.controller;

import com.skyspireinfotech.admin.entity.AdminUser;
import com.skyspireinfotech.admin.entity.SiteSettings;
import com.skyspireinfotech.admin.repository.AdminUserRepository;
import com.skyspireinfotech.admin.repository.SiteSettingsRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final AdminUserRepository adminUserRepository;
    private final SiteSettingsRepository siteSettingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public ProfileController(AdminUserRepository adminUserRepository,
                              SiteSettingsRepository siteSettingsRepository,
                              PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.siteSettingsRepository = siteSettingsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        model.addAttribute("active", "profile");
        model.addAttribute("title", "Profile");
        return "admin/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@RequestParam String currentPassword,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        AdminUser user = adminUserRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in admin not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "Current password is incorrect.");
            return "redirect:/profile";
        }
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("passwordError", "New password must be at least 8 characters.");
            return "redirect:/profile";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "New password and confirmation don't match.");
            return "redirect:/profile";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        adminUserRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Password updated.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/logo")
    public String uploadLogo(@RequestParam MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
        SiteSettings settings = currentSettings();
        String url = storeImage(file, settings.getLogoUrl());
        if (url == null) {
            redirectAttributes.addFlashAttribute("uploadError", "Please choose an image file to upload.");
            return "redirect:/profile";
        }
        settings.setLogoUrl(url);
        siteSettingsRepository.save(settings);
        redirectAttributes.addFlashAttribute("success", "Logo updated.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/favicon")
    public String uploadFavicon(@RequestParam MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
        SiteSettings settings = currentSettings();
        String url = storeImage(file, settings.getFaviconUrl());
        if (url == null) {
            redirectAttributes.addFlashAttribute("uploadError", "Please choose an image file to upload.");
            return "redirect:/profile";
        }
        settings.setFaviconUrl(url);
        siteSettingsRepository.save(settings);
        redirectAttributes.addFlashAttribute("success", "Favicon updated.");
        return "redirect:/profile";
    }

    private SiteSettings currentSettings() {
        return siteSettingsRepository.findAll().stream().findFirst().orElseGet(SiteSettings::new);
    }

    private String storeImage(MultipartFile file, String previousUrl) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return null;
        }

        Path dir = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/svg+xml" -> ".svg";
            case "image/x-icon", "image/vnd.microsoft.icon" -> ".ico";
            case "image/webp" -> ".webp";
            default -> "";
        };

        String filename = UUID.randomUUID() + extension;
        Path target = dir.resolve(filename);
        file.transferTo(target);

        if (previousUrl != null && previousUrl.startsWith("/uploads/")) {
            Path previous = dir.resolve(previousUrl.substring("/uploads/".length()));
            Files.deleteIfExists(previous);
        }

        return "/uploads/" + filename;
    }
}
