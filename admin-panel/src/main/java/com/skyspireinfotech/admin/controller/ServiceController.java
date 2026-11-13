package com.skyspireinfotech.admin.controller;

import com.skyspireinfotech.admin.entity.ServiceItem;
import com.skyspireinfotech.admin.repository.ServiceItemRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/services")
public class ServiceController {

    private final ServiceItemRepository serviceItemRepository;

    public ServiceController(ServiceItemRepository serviceItemRepository) {
        this.serviceItemRepository = serviceItemRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("services", serviceItemRepository.findAllByOrderByDisplayOrderAsc());
        model.addAttribute("active", "services");
        model.addAttribute("title", "Services");
        return "admin/services";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("service", new ServiceItem());
        model.addAttribute("active", "services");
        model.addAttribute("title", "Add Service");
        return "admin/service-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ServiceItem service = serviceItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));
        model.addAttribute("service", service);
        model.addAttribute("active", "services");
        model.addAttribute("title", "Edit Service");
        return "admin/service-form";
    }

    @PostMapping
    public String create(@ModelAttribute ServiceItem service, RedirectAttributes redirectAttributes) {
        service.setId(null);
        serviceItemRepository.save(service);
        redirectAttributes.addFlashAttribute("success", "Service added.");
        return "redirect:/services";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute ServiceItem form,
                          RedirectAttributes redirectAttributes) {
        ServiceItem service = serviceItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));
        service.setTitle(form.getTitle());
        service.setDescription(form.getDescription());
        service.setIconName(form.getIconName());
        service.setDisplayOrder(form.getDisplayOrder());
        service.setActive(form.isActive());
        serviceItemRepository.save(service);
        redirectAttributes.addFlashAttribute("success", "Service updated.");
        return "redirect:/services";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        serviceItemRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Service removed.");
        return "redirect:/services";
    }
}
