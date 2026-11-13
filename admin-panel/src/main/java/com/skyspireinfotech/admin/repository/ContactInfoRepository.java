package com.skyspireinfotech.admin.repository;

import com.skyspireinfotech.admin.entity.ContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactInfoRepository extends JpaRepository<ContactInfo, Long> {
}
