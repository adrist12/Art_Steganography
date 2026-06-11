package com.Pixel_To_Art_And_Steganography.Art_Steganography.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

public class DashboardController {
    @GetMapping("/dashboard")
    public String get_Dashboard(Model model, HttpSession session) {
        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }
        model.addAttribute("userName", session.getAttribute("userName"));
        return "dashboard";
    }
}
