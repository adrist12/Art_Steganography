package com.Pixel_To_Art_And_Steganography.Art_Steganography.web.controller;

import com.Pixel_To_Art_And_Steganography.Art_Steganography.domain.User;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.repository.UserRepository;
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
            return "redirect:/auth/login"; // Redirige al login si la sesión caducó
        }
        return "./auth/security"; // Retorna tu archivo security.html de la carpeta templates
    }

    /**
     * Procesa la actualización o creación de la contraseña tradicional.
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
            return "redirect:/auth/login"; // Redirigir si la sesión caducó
        }

        // 2. Validar que las contraseñas coincidan en texto plano
        if (!nuevaContrasena.equals(confirmarContrasena)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas de control no coinciden.");
            return "redirect:/dashboard"; // O a la ruta de tu vista de seguridad
        }

        try {
            // 3. Buscar el usuario en la base de datos
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario de sesión no válido."));

            // 4. Actualizar la contraseña
            // 🛡️ RECOMENDACIÓN: Si usas encriptación en el login tradicional (ej. BCrypt),
            // pasa la contraseña por tu bean encriptador aquí antes de guardarla:
            // String passwordEncriptada = passwordEncoder.encode(nuevaContrasena);
            // user.setContrasena(passwordEncriptada);

            user.setContrasena(nuevaContrasena);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("success", "Llave tradicional actualizada con éxito en la base de datos.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Falla en el sistema de persistencia: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }
}