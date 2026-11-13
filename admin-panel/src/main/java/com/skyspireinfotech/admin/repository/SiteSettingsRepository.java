package com.skyspireinfotech.admin.repository;

import com.skyspireinfotech.admin.entity.SiteSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteSettingsRepository extends JpaRepository<SiteSettings, Long> {
}
