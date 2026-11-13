package com.skyspireinfotech.admin.controller;

import com.skyspireinfotech.admin.entity.ContactInfo;
import com.skyspireinfotech.admin.repository.ContactInfoRepository;
import com.skyspireinfotech.admin.repository.ContactMessageRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/contact")
public class ContactController {

    private final ContactInfoRepository contactInfoRepository;
    private final ContactMessageRepository contactMessageRepository;

    public ContactController(ContactInfoRepository contactInfoRepository,
                              ContactMessageRepository contactMessageRepository) {
        this.contactInfoRepository = contactInfoRepository;
        this.contactMessageRepository = contactMessageRepository;
    }

    @GetMapping
    public String view(Model model) {
        model.addAttribute("info", loadOrCreate());
        model.addAttribute("messages", contactMessageRepository.findAllByOrderBySubmittedAtDesc());
        model.addAttribute("active", "contact");
        model.addAttribute("title", "Contact");
        return "admin/contact";
    }

    @PostMapping
    public String save(@ModelAttribute("info") ContactInfo form, RedirectAttributes redirectAttributes) {
        ContactInfo info = loadOrCreate();
        info.setEmail(form.getEmail());
        info.setPhone(form.getPhone());
        info.setAddress(form.getAddress());
        contactInfoRepository.save(info);
        redirectAttributes.addFlashAttribute("success", "Contact details updated.");
        return "redirect:/contact";
    }

    @PostMapping("/messages/{id}/delete")
    public String deleteMessage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        contactMessageRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Message deleted.");
        return "redirect:/contact";
    }

    private ContactInfo loadOrCreate() {
        return contactInfoRepository.findAll().stream().findFirst().orElseGet(ContactInfo::new);
    }
}
