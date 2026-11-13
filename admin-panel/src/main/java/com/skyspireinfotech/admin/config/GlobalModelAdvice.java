package com.skyspireinfotech.admin.config;

import com.skyspireinfotech.admin.entity.SiteSettings;
import com.skyspireinfotech.admin.repository.SiteSettingsRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Makes branding settings (logo, favicon) available to every Thymeleaf
 * template — including the login page — without each controller having
 * to fetch and add it manually.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final SiteSettingsRepository siteSettingsRepository;

    public GlobalModelAdvice(SiteSettingsRepository siteSettingsRepository) {
        this.siteSettingsRepository = siteSettingsRepository;
    }

    @ModelAttribute("siteSettings")
    public SiteSettings siteSettings() {
        return siteSettingsRepository.findAll().stream().findFirst().orElseGet(SiteSettings::new);
    }
}
