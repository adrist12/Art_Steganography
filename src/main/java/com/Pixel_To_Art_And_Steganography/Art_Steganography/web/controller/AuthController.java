package com.Pixel_To_Art_And_Steganography.Art_Steganography.web.controller;

import com.Pixel_To_Art_And_Steganography.Art_Steganography.domain.User;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.dto.RegisterForm;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    //--------RUTAS GET--------------
    @GetMapping("/login")
    public String mostrarLogin() {

        return "./auth/login";
    }
    @GetMapping("/register")
    public String mostrarRegistro(Model model) {
        model.addAttribute("registroForm", new RegisterForm()); // Importante para el form binding
        return "./auth/registro";
    }

    //--------RUTAS POST--------------
    @PostMapping("/register")
    public String procesarRegistro(@Valid @ModelAttribute RegisterForm form, Model model) {
        try {
            authService.registerUser(form); // Pasas el DTO al servicio
            return "redirect:/auth/login?success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "./auth/registro"; // Vuelve a mostrar el formulario con el error
        }
    }

    @PostMapping("/login")
    public String post_Login(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String remember,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        return authService.authenticate(email, password)
                .map(userName -> {
                    session.setAttribute("userSession", email);
                    session.setAttribute("userName", userName);
                    if ("on".equals(remember)) {
                        session.setMaxInactiveInterval(30 * 24 * 60 * 60);
                    }
                    return "redirect:/dashboard";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error",
                            "Credenciales incorrectas");
                    return "redirect:/";
                });
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
