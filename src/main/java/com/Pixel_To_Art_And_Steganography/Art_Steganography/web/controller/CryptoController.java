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
import java.util.Base64;

@Controller
@RequestMapping("/crypto")
public class CryptoController {

    private final CryptoService cryptoService;

    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    /**
     * Muestra la página principal del módulo de criptografía.
     */
    @GetMapping
    public String mostrarCrypto(Model model, HttpSession session) {
        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }
        return "./modules/crypto";
    }

    /**
     * Cifra un texto usando AES-GCM con compresión previa.
     * Flujo: texto → comprimir → cifrar → Base64
     */
    @PostMapping("/encrypt")
    public String encryptText(
            @RequestParam String plaintext,
            @RequestParam String password,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }

        try {
            if (plaintext == null || plaintext.trim().isEmpty()) {
                model.addAttribute("error", "El texto a cifrar no puede estar vacío");
                return "./modules/crypto";
            }

            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "La contraseña no puede estar vacía");
                return "./modules/crypto";
            }

            String encrypted = cryptoService.encrypt(plaintext, password);
            model.addAttribute("encryptedResult", encrypted);
            model.addAttribute("originalText", plaintext);
            model.addAttribute("success", "Texto cifrado exitosamente");
            return "./modules/crypto";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cifrar: " + e.getMessage());
            return "./modules/crypto";
        }
    }

    /**
     * Descifra un texto cifrado con AES-GCM.
     * Flujo: Base64 → descifrar → descomprimir → texto original
     */
    @PostMapping("/decrypt")
    public String decryptText(
            @RequestParam String encryptedText,
            @RequestParam String password,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }

        try {
            if (encryptedText == null || encryptedText.trim().isEmpty()) {
                model.addAttribute("error", "El texto cifrado no puede estar vacío");
                return "./modules/crypto";
            }

            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "La contraseña no puede estar vacía");
                return "./modules/crypto";
            }

            String decrypted = cryptoService.decrypt(encryptedText, password);
            model.addAttribute("decryptedResult", decrypted);
            model.addAttribute("encryptedInput", encryptedText);
            model.addAttribute("success", "Texto descifrado exitosamente");
            return "./modules/crypto";

        } catch (Exception e) {
            model.addAttribute("error", "Error al descifrar: " + e.getMessage() + ". Verifique la contraseña.");
            return "./modules/crypto";
        }
    }

    /**
     * Comprime datos de texto usando GZIP.
     */
    @PostMapping("/compress")
    public String compressText(
            @RequestParam String textToCompress,
            Model model,
            HttpSession session) {

        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }

        try {
            if (textToCompress == null || textToCompress.trim().isEmpty()) {
                model.addAttribute("error", "El texto a comprimir no puede estar vacío");
                return "./modules/crypto";
            }

            byte[] compressed = cryptoService.compress(textToCompress.getBytes("UTF-8"));
            String compressedBase64 = Base64.getEncoder().encodeToString(compressed);
            model.addAttribute("compressedResult", compressedBase64);
            model.addAttribute("originalText", textToCompress);
            model.addAttribute("success", "Datos comprimidos exitosamente");
            return "./modules/crypto";

        } catch (Exception e) {
            model.addAttribute("error", "Error al comprimir: " + e.getMessage());
            return "./modules/crypto";
        }
    }

    /**
     * Descomprime datos GZIP.
     */
    @PostMapping("/decompress")
    public String decompressText(
            @RequestParam String compressedText,
            Model model,
            HttpSession session) {

        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }

        try {
            if (compressedText == null || compressedText.trim().isEmpty()) {
                model.addAttribute("error", "El texto comprimido no puede estar vacío");
                return "./modules/crypto";
            }

            byte[] compressed = Base64.getDecoder().decode(compressedText);
            byte[] decompressed = cryptoService.decompress(compressed);
            String decompressedText = new String(decompressed, "UTF-8");
            model.addAttribute("decompressedResult", decompressedText);
            model.addAttribute("compressedInput", compressedText);
            model.addAttribute("success", "Datos descomprimidos exitosamente");
            return "./modules/crypto";

        } catch (Exception e) {
            model.addAttribute("error", "Error al descomprimir: " + e.getMessage());
            return "./modules/crypto";
        }
    }
}
