package com.Pixel_To_Art_And_Steganography.Art_Steganography.web.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
public class Controlador {
    
    // Usuarios de prueba
    private static final String[][] VALID_USERS = {
        {"admin@example.com", "admin123", "Administrador"},
        {"usuario@example.com", "usuario123", "Usuario"},
        {"test@example.com", "test123", "Test"}
    };
    
    @GetMapping("/")
    public String get_Index(Model model, HttpSession session) {
        return "index";
    }
    
    @PostMapping("/login")
    public String post_Login(
            @RequestParam String user,
            @RequestParam String pass,
            @RequestParam(required = false) String remember,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Validar credenciales
        String[] foundUser = null;
        for (String[] validUser : VALID_USERS) {
            if (validUser[0].equals(user) && validUser[1].equals(pass)) {
                foundUser = validUser;
                break;
            }
        }
        
        if (foundUser != null) {
            // Login exitoso - guardar en sesión
            session.setAttribute("userSession", foundUser[0]);
            session.setAttribute("userName", foundUser[2]);
            
            if ("on".equals(remember)) {
                session.setMaxInactiveInterval(30 * 24 * 60 * 60); // 30 días
            }
            
            return "redirect:/dashboard";
        } else {
            // Login fallido
            redirectAttributes.addFlashAttribute("error", "Usuario o contraseña incorrectos. Intenta con: admin@example.com / admin123");
            return "redirect:/";
        }
    }
    
    @GetMapping("/dashboard")
    public String get_Dashboard(Model model, HttpSession session) {
        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }
        model.addAttribute("userName", session.getAttribute("userName"));
        return "dashboard";
    }
    
    @GetMapping("/Art")
    public String get_Art(Model model, HttpSession session) {
        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }
        return "Art";
    }
    
    @PostMapping("/Art")
    public String post_Art(Model model, HttpSession session) {
        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }
        return "Art";
    }
    
    @GetMapping("/Steganography")
    public String get_Image(Model model, HttpSession session) {
        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }
        return "Steganography";
    }
    
    @PostMapping("/Steganography")
    public String post_Image(Model model, HttpSession session) {
        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }
        return "Steganography";
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
