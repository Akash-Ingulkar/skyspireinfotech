package com.skyspireinfotech.admin.repository;

import com.skyspireinfotech.admin.entity.SiteContent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteContentRepository extends JpaRepository<SiteContent, Long> {
    Optional<SiteContent> findBySectionKey(String sectionKey);
}
