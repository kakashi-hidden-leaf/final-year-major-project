package com.campus360.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            return redirectToDashboard(authentication);
        }
        return "login";
    }

    @GetMapping("/")
    public String rootRedirect(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        return redirectToDashboard(authentication);
    }

    private String redirectToDashboard(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_OFFICE_STAFF")) {
                return "redirect:/staff/dashboard";
            } else if (role.equals("ROLE_TEACHER")) {
                return "redirect:/teacher/dashboard";
            } else if (role.equals("ROLE_STUDENT")) {
                return "redirect:/student/dashboard";
            }
        }
        return "redirect:/login";
    }
}
