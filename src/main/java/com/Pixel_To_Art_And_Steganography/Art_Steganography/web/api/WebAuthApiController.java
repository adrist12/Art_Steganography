package com.Pixel_To_Art_And_Steganography.Art_Steganography.web.api;

import com.Pixel_To_Art_And_Steganography.Art_Steganography.domain.User;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.repository.UserRepository;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.service.WebAuthnService;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/webauthn")
public class WebAuthApiController {
    private final WebAuthnService webAuthnService;
    private final UserRepository userRepository;

    public WebAuthApiController(WebAuthnService webAuthnService, UserRepository userRepository) {
        this.webAuthnService = webAuthnService;
        this.userRepository = userRepository;
    }

    /**
     * 1. Inicia el registro.
     * Retorna JSON para que el navegador cree la credencial.
     */
    @GetMapping("/register/options")
    public ResponseEntity<?> registerOptions(
            @RequestParam String email,
            @RequestParam String nombre,
            HttpSession session) {
        try {
            // Guardamos los datos de registro temporalmente en la sesión web
            session.setAttribute("temp_register_email", email);
            session.setAttribute("temp_register_nombre", nombre);

            PublicKeyCredentialCreationOptions options = webAuthnService.startRegistration(email, nombre, session);
            return ResponseEntity.ok(options.toCredentialsCreateJson());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error iniciando registro: " + e.getMessage());
        }
    }

    /**
     * 2. Finaliza el registro.
     * Crea al usuario con una contraseña por defecto válida (no nula) y guarda la credencial.
     */
    @PostMapping("/register/finish")
    public ResponseEntity<?> registerFinish(
            @RequestBody String responseJson,
            HttpSession session) {
        try {
            String email = (String) session.getAttribute("temp_register_email");
            String nombre = (String) session.getAttribute("temp_register_nombre");

            if (email != null && !userRepository.existsByEmail(email)) {
                User nuevoUsuario = new User();
                nuevoUsuario.setEmail(email);
                nuevoUsuario.setNombre(nombre);

                // 🛡️ SOLUCIÓN AL ERROR DE MYSQL:
                // Le pasamos un string vacío o un valor temporal para saltar la restricción NOT NULL.
                // Asegúrate de que el método en tu entidad se llame exactamente setContrasena o setPassword.
                nuevoUsuario.setContrasena("");

                userRepository.save(nuevoUsuario);
            }

            // Con el usuario ya persistido con éxito en la BD, Yubico puede enlazar la llave
            webAuthnService.finishRegistration(responseJson, session);

            // Limpiamos la sesión
            session.removeAttribute("temp_register_email");
            session.removeAttribute("temp_register_nombre");

            return ResponseEntity.ok(Map.of("success", true, "message", "Credencial registrada correctamente"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error registrando credencial: " + e.getMessage());
        }
    }
    /**
     * 3. Inicia el login.
     * Retorna el challenge (JSON) para que el navegador firme con la llave.
     */
    @GetMapping("/login/options")
    public ResponseEntity<?> loginOptions(
            @RequestParam String email,
            HttpSession session) {
        try {
            AssertionRequest assertionRequest = webAuthnService.startLogin(email, session);
            return ResponseEntity.ok(assertionRequest.toCredentialsGetJson());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error iniciando login: " + e.getMessage());
        }
    }

    /**
     * 4. Finaliza el login.
     * Verifica la firma y crea la sesión de usuario.
     */
    @PostMapping("/login/finish")
    public ResponseEntity<?> loginFinish(
            @RequestBody String responseJson,
            HttpSession session) {
        try {
            String username = webAuthnService.finishLogin(responseJson, session);

            session.setAttribute("userSession", username);

            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado tras login exitoso"));

            session.setAttribute("userName", user.getNombre());

            return ResponseEntity.ok(Map.of("success", true, "redirectUrl", "/dashboard"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error en login: " + e.getMessage());
        }
    }
}