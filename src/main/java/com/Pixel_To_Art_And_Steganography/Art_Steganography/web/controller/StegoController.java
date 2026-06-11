package com.Pixel_To_Art_And_Steganography.Art_Steganography.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
@Controller
public class StegoController {
    @GetMapping("/modules/stego")
    public String get_Image(Model model, HttpSession session) {
        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }
        return "./modules/Steganography";
    }

    @PostMapping("/modules/stego")
    public String post_Image(Model model, HttpSession session) {
        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }
        return "./modules/Steganography";
    }

}
