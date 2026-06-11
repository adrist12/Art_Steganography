package com.Pixel_To_Art_And_Steganography.Art_Steganography.web.controller;

import com.Pixel_To_Art_And_Steganography.Art_Steganography.domain.User;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Despacha la vista del centro de seguridad de credenciales.
     */
    @GetMapping("/security")
    public String viewSecurityCenter(HttpSession session) {
        if (session.getAttribute("userSession") == null) {
            return "redirect:/auth/login";
        }
        return "./auth/security";
    }

    /**
     * Procesa la actualización o creación de la contraseña de forma cifrada con jBCrypt.
     */
    @PostMapping("/update-password")
    public String updatePassword(
            @RequestParam String nuevaContrasena,
            @RequestParam String confirmarContrasena,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String userEmail = (String) session.getAttribute("userSession");

        if (userEmail == null) {
            return "redirect:/auth/login";
        }

        // Si fallan las contraseñas, SÍ lo dejamos en la página de seguridad con el error
        if (!nuevaContrasena.equals(confirmarContrasena)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas de control no coinciden.");
            return "redirect:/profile/security";
        }

        try {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario de sesión no válido."));

            // Hashing adaptativo con jBCrypt
            String passwordEncriptada = BCrypt.hashpw(nuevaContrasena, BCrypt.gensalt());
            user.setContrasena(passwordEncriptada);

            // Persistencia en base de datos
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("success", "Llave tradicional actualizada con éxito.");

            // 🚀 RUTA DE ÉXITO: Rompe el bucle y redirige al Dashboard principal
            return "redirect:/dashboard";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Falla en el sistema de persistencia: " + e.getMessage());
            // Si hay un error de base de datos, lo mantenemos en la vista de seguridad para ver el mensaje
            return "redirect:/profile/security";
        }
    }
}