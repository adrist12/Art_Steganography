package com.Pixel_To_Art_And_Steganography.Art_Steganography.web.controller;

import com.Pixel_To_Art_And_Steganography.Art_Steganography.service.ArtService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/modules/ascii")
public class AsciiController {

    private final ArtService artService;

    public AsciiController(ArtService artService) {
        this.artService = artService;
    }

    @GetMapping
    public String get_Art(Model model, HttpSession session) {
        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }
        return "./modules/Art";
    }

    @PostMapping
    public String post_Art(
            @RequestParam("image") MultipartFile imagen,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }

        try {
            if (imagen == null || imagen.isEmpty()) {
                model.addAttribute("error", "Debes seleccionar una imagen");
                return "./modules/Art";
            }

            // Procesar la imagen con Tesseract OCR
            String resultado = artService.extraerTextoDeImagen(imagen);

            if (resultado == null || resultado.trim().isEmpty()) {
                model.addAttribute("error", "No se pudo extraer texto de la imagen. Intenta con una imagen más clara.");
                return "./modules/Art";
            }

            model.addAttribute("resultado", resultado);
            model.addAttribute("success", "Texto extraído exitosamente");

            return "./modules/Art";

        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar la imagen: " + e.getMessage());
            return "./modules/Art";
        }
    }
}
