package com.skyspireinfotech.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // "/" here is skyspireinfotech.com/admin (context path); unauthenticated visitors
    // get bounced to the login page by Spring Security before this runs.
    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/dashboard";
    }
}
