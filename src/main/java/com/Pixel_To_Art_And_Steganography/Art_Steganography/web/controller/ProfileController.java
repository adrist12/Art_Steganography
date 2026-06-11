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

    // Inyección por constructor estándar para el repositorio
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

        // 1. Obtener el email del usuario desde la sesión HTTP activa
        String userEmail = (String) session.getAttribute("userSession");

        if (userEmail == null) {
            return "redirect:/auth/login";
        }

        // 2. Validar que las contraseñas coincidan antes del procesamiento criptográfico
        if (!nuevaContrasena.equals(confirmarContrasena)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas de control no coinciden.");
            return "redirect:/profile/security";
        }

        try {
            // 3. Buscar el usuario en la base de datos
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario de sesión no válido."));

            // 4. Transformación Criptográfica con jBCrypt Nativo
            // BCrypt.gensalt() genera una sal aleatoria segura de forma automática.
            // BCrypt.hashpw() fusiona la contraseña en texto plano con la sal y computa el hash.
            String passwordEncriptada = BCrypt.hashpw(nuevaContrasena, BCrypt.gensalt());

            // Asignamos el hash seguro de 60 caracteres a la entidad
            user.setContrasena(passwordEncriptada);

            // Persistencia en base de datos
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("success", "Llave tradicional actualizada con éxito usando jBCrypt nativo.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Falla en el sistema de persistencia: " + e.getMessage());
        }

        return "redirect:/profile/security";
    }
}