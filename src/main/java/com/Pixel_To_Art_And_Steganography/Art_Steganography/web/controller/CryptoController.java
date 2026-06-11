package com.Pixel_To_Art_And_Steganography.Art_Steganography.web.controller;

import com.Pixel_To_Art_And_Steganography.Art_Steganography.service.CryptoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para el módulo de Criptografía
 * Rutas: /crypto
 *
 * Flujo: texto → comprimir → cifrar → ocultar en imagen
 * Flujo inverso: imagen → extraer → descifrar → descomprimir → texto original
 */
@Controller
@RequestMapping("/crypto")
public class CryptoController {

    private final CryptoService cryptoService;

    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    /**
     * GET /crypto - Muestra la página principal de criptografía
     */
    @GetMapping
    public String mostrarCrypto(Model model, HttpSession session) {
        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }
        return "./modules/crypto";
    }

    /**
     * POST /crypto/cifrar - Cifra un texto con AES-256-GCM y compresión GZIP
     */
    @PostMapping("/cifrar")
    public String cifrarTexto(
            @RequestParam String texto,
            @RequestParam String password,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }

        try {
            if (texto == null || texto.trim().isEmpty()) {
                model.addAttribute("error", "El texto a cifrar no puede estar vacío");
                return "./modules/crypto";
            }

            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "La contraseña no puede estar vacía");
                return "./modules/crypto";
            }

            // Flujo: texto → comprimir → cifrar
            String resultadoCifrado = cryptoService.comprimirYCifrarV2(texto, password);

            model.addAttribute("textoOriginal", texto);
            model.addAttribute("textoCifrado", resultadoCifrado);
            model.addAttribute("success", "Texto cifrado exitosamente con AES-256-GCM y compresión GZIP");

            return "./modules/crypto";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cifrar: " + e.getMessage());
            return "./modules/crypto";
        }
    }

    /**
     * POST /crypto/descifrar - Descifra un texto cifrado y lo descomprime
     */
    @PostMapping("/descifrar")
    public String descifrarTexto(
            @RequestParam String textoCifrado,
            @RequestParam String password,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }

        try {
            if (textoCifrado == null || textoCifrado.trim().isEmpty()) {
                model.addAttribute("error", "El texto cifrado no puede estar vacío");
                return "./modules/crypto";
            }

            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "La contraseña no puede estar vacía");
                return "./modules/crypto";
            }

            // Flujo inverso: descifrar → descomprimir → texto original
            String textoDescifrado = cryptoService.descifrarYDescomprimirV2(textoCifrado, password);

            model.addAttribute("textoCifrado", textoCifrado);
            model.addAttribute("textoDescifrado", textoDescifrado);
            model.addAttribute("success", "Texto descifrado exitosamente");

            return "./modules/crypto";

        } catch (Exception e) {
            model.addAttribute("error", "Error al descifrar: " + e.getMessage() +
                    ". Verifique que la contraseña sea correcta.");
            return "./modules/crypto";
        }
    }

    /**
     * POST /crypto/comprimir - Comprime un texto usando GZIP
     */
    @PostMapping("/comprimir")
    public String comprimirTexto(
            @RequestParam String texto,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }

        try {
            if (texto == null || texto.trim().isEmpty()) {
                model.addAttribute("error", "El texto a comprimir no puede estar vacío");
                return "./modules/crypto";
            }

            byte[] datosComprimidos = cryptoService.comprimir(texto.getBytes("UTF-8"));
            String resultadoBase64 = java.util.Base64.getEncoder().encodeToString(datosComprimidos);

            long tamanoOriginal = texto.getBytes("UTF-8").length;
            long tamanoComprimido = datosComprimidos.length;
            double ratio = ((double) tamanoComprimido / tamanoOriginal) * 100;

            model.addAttribute("textoOriginal", texto);
            model.addAttribute("textoComprimido", resultadoBase64);
            model.addAttribute("tamanoOriginal", tamanoOriginal);
            model.addAttribute("tamanoComprimido", tamanoComprimido);
            model.addAttribute("ratioCompresion", String.format("%.2f", ratio));
            model.addAttribute("success", "Texto comprimido exitosamente con GZIP");

            return "./modules/crypto";

        } catch (Exception e) {
            model.addAttribute("error", "Error al comprimir: " + e.getMessage());
            return "./modules/crypto";
        }
    }

    /**
     * POST /crypto/descomprimir - Descomprime un texto comprimido con GZIP
     */
    @PostMapping("/descomprimir")
    public String descomprimirTexto(
            @RequestParam String textoComprimido,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }

        try {
            if (textoComprimido == null || textoComprimido.trim().isEmpty()) {
                model.addAttribute("error", "El texto comprimido no puede estar vacío");
                return "./modules/crypto";
            }

            byte[] datosComprimidos = java.util.Base64.getDecoder().decode(textoComprimido);
            byte[] datosDescomprimidos = cryptoService.descomprimir(datosComprimidos);
            String textoDescomprimido = new String(datosDescomprimidos, "UTF-8");

            model.addAttribute("textoComprimido", textoComprimido);
            model.addAttribute("textoDescomprimido", textoDescomprimido);
            model.addAttribute("success", "Texto descomprimido exitosamente");

            return "./modules/crypto";

        } catch (Exception e) {
            model.addAttribute("error", "Error al descomprimir: " + e.getMessage() +
                    ". Verifique que el texto esté correctamente codificado en Base64.");
            return "./modules/crypto";
        }
    }
}
