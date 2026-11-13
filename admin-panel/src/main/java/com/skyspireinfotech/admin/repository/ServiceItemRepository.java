package com.skyspireinfotech.admin.repository;

import com.skyspireinfotech.admin.entity.ServiceItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {
    List<ServiceItem> findAllByOrderByDisplayOrderAsc();
}
