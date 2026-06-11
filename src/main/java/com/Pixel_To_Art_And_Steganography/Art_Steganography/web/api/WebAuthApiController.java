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

    // Inyectamos UserRepository para poder obtener el nombre del usuario al finalizar el login
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
            PublicKeyCredentialCreationOptions options = webAuthnService.startRegistration(email, nombre, session);
            // toCredentialsCreateJson() convierte el objeto a la estructura JSON que espera el navegador
            return ResponseEntity.ok(options.toCredentialsCreateJson());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error iniciando registro: " + e.getMessage());
        }
    }

    /**
     * 2. Finaliza el registro.
     * Recibe la respuesta del navegador y guarda la credencial en BD.
     */
    @PostMapping("/register/finish")
    public ResponseEntity<?> registerFinish(
            @RequestBody String responseJson,
            HttpSession session) {
        try {
            webAuthnService.finishRegistration(responseJson, session);
            return ResponseEntity.ok(Map.of("success", true, "message", "Credencial registrada correctamente"));
        } catch (Exception e) {
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
            // CORRECCIÓN 1: El servicio retorna AssertionRequest completo, no solo las opciones
            AssertionRequest assertionRequest = webAuthnService.startLogin(email, session);


            // toCredentialsGetJson() extrae las opciones y las serializa a JSON
            return ResponseEntity.ok(assertionRequest.toCredentialsGetJson());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error iniciando login: " + e.getMessage());
        }
    }

    /**
     * 4. Finaliza el login.
     * Verifica la firma y crea la sesión de usuario (igual que el login normal).
     */
    @PostMapping("/login/finish")
    public ResponseEntity<?> loginFinish(
            @RequestBody String responseJson,
            HttpSession session) {
        try {
            // Verifica la firma y retorna el email del usuario
            String username = webAuthnService.finishLogin(responseJson, session);

            // CORRECCIÓN 2: Crear la sesión de usuario manualmente
            // Esto es CRÍTICO para que la aplicación sepa que el usuario está logueado

            // 1. Guardar el identificador (email) en la sesión
            session.setAttribute("userSession", username);

            // 2. Buscar al usuario en BD para obtener datos adicionales (como el nombre para mostrar)
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado tras login exitoso"));

            // 3. Guardar el nombre para la vista (dashboard)
            session.setAttribute("userName", user.getNombre());

            // Opcional: Guardar el ID si lo usas en otras partes
            // session.setAttribute("userId", user.getId());

            return ResponseEntity.ok(Map.of("success", true, "redirectUrl", "/inicio"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error en login: " + e.getMessage());
        }

    }
}
