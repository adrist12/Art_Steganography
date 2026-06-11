package com.Pixel_To_Art_And_Steganography.Art_Steganography.web.controller;


import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
@Controller
public class AsciiController {

    @GetMapping("/modules/ascii")
    public String get_Art(Model model, HttpSession session) {
        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }
        return "Art";
    }
    @PostMapping("/modules/ascii")
    public String post_Art(Model model, HttpSession session) {
        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }
        return "Art";
    }
}

