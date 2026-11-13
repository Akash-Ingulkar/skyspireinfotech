package com.skyspireinfotech.admin.repository;

import com.skyspireinfotech.admin.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    java.util.List<ContactMessage> findAllByOrderBySubmittedAtDesc();
}
