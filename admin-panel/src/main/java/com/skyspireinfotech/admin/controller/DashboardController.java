package com.skyspireinfotech.admin.controller;

import com.skyspireinfotech.admin.entity.ContactMessage;
import com.skyspireinfotech.admin.entity.ServiceItem;
import com.skyspireinfotech.admin.repository.ContactMessageRepository;
import com.skyspireinfotech.admin.repository.ServiceItemRepository;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final ServiceItemRepository serviceItemRepository;
    private final ContactMessageRepository contactMessageRepository;

    public DashboardController(ServiceItemRepository serviceItemRepository,
                                ContactMessageRepository contactMessageRepository) {
        this.serviceItemRepository = serviceItemRepository;
        this.contactMessageRepository = contactMessageRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<ServiceItem> services = serviceItemRepository.findAllByOrderByDisplayOrderAsc();
        List<ContactMessage> messages = contactMessageRepository.findAllByOrderBySubmittedAtDesc();

        long activeServices = services.stream().filter(ServiceItem::isActive).count();
        long newInquiries = messages.stream().filter(m -> !m.isRead()).count();

        model.addAttribute("totalServices", services.size());
        model.addAttribute("activeServices", activeServices);
        model.addAttribute("totalInquiries", messages.size());
        model.addAttribute("newInquiries", newInquiries);
        model.addAttribute("recentMessages", messages.stream().limit(5).toList());
        model.addAttribute("active", "dashboard");
        model.addAttribute("title", "Dashboard");
        return "admin/dashboard";
    }
}
