package com.Pixel_To_Art_And_Steganography.Art_Steganography.Controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

public class Controlador {
    @GetMapping
    public String get_Index(Model model) {
        return "index";
    }
    @PostMapping
    public String post_Index(Model model) {
        return "index";
    }
    @GetMapping
    public String get_Art(Model model) {
        return "Art";
    }
    @PostMapping
    public String post_Art(Model model) {
        return "Art";
    }
    @GetMapping
    public String get_Image(Model model) {
        return "Steganography";
    }
    @PostMapping
    public String post_Image(Model model) {
        return "Steganography";
    }
}
