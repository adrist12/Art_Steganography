package com.Pixel_To_Art_And_Steganography.Art_Steganography.web.controller;

import com.Pixel_To_Art_And_Steganography.Art_Steganography.service.CryptoService;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.service.StegoService;
import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

@Controller
@RequestMapping("/stego")
public class CryptoStegoController {

    private static final String UPLOAD_DIR = "uploads/stego/";
    private static final String END_DELIMITER = "ENDSTEGO";

    private final CryptoService cryptoService;
    private final StegoService stegoService;

    public CryptoStegoController(CryptoService cryptoService, StegoService stegoService) {
        this.cryptoService = cryptoService;
        this.stegoService = stegoService;
    }

    @GetMapping
    public String mostrarStego(Model model, HttpSession session) {
        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }
        return "./modules/steganography";
    }

    /**
     * Oculta un mensaje en una imagen.
     * Flujo: texto → comprimir → cifrar → ocultar en imagen (LSB) → descargar
     */
    @PostMapping("/hide")
    public String hideMessage(
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("message") String message,
            @RequestParam("password") String password,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }

        try {
            if (imageFile.isEmpty()) {
                model.addAttribute("error", "Debes seleccionar una imagen");
                return "./modules/steganography";
            }

            if (message == null || message.trim().isEmpty()) {
                model.addAttribute("error", "El mensaje no puede estar vacío");
                return "./modules/steganography";
            }

            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "La contraseña no puede estar vacía");
                return "./modules/steganography";
            }

            // 1. Comprimir el mensaje
            byte[] compressed = cryptoService.comprimir(message.getBytes("UTF-8"));

            // 2. Generar clave desde password y cifrar los datos comprimidos
            SecretKey clave = cryptoService.generarClaveDesdePassword(password);
            byte[] encryptedBytes = cryptoService.cifrar(compressed, clave);
            String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);

            // 3. Ocultar en la imagen usando LSB
            BufferedImage stegoImage = stegoService.hideMessage(imageFile, encryptedBase64);

            // 4. Guardar imagen temporalmente para descarga
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "stego_" + timestamp + ".png";
            Path uploadPath = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(filename);
            ImageIO.write(stegoImage, "PNG", filePath.toFile());

            model.addAttribute("success", "Mensaje ocultado exitosamente en la imagen");
            model.addAttribute("downloadFilename", filename);
            model.addAttribute("hideMessage", message);
            model.addAttribute("hidePassword", password);
            return "./modules/steganography";

        } catch (Exception e) {
            model.addAttribute("error", "Error al ocultar mensaje: " + e.getMessage());
            return "./modules/steganography";
        }
    }

    /**
     * Extrae un mensaje de una imagen.
     * Flujo: imagen → extraer bits (LSB) → descifrar → descomprimir → texto original
     */
    @PostMapping("/extract")
    public String extractMessage(
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("password") String password,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("userSession") == null) {
            return "redirect:/";
        }

        try {
            if (imageFile.isEmpty()) {
                model.addAttribute("error", "Debes seleccionar una imagen");
                return "./modules/steganography";
            }

            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "La contraseña no puede estar vacía");
                return "./modules/steganography";
            }

            // 1. Extraer datos ocultos de la imagen (LSB)
            String encryptedBase64 = stegoService.extractMessage(imageFile);

            // 2. Generar clave desde password y descifrar los datos
            SecretKey clave = cryptoService.generarClaveDesdePassword(password);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
            byte[] decryptedCompressed = cryptoService.descifrar(encryptedBytes, clave);

            // 3. Descomprimir los datos
            byte[] decompressed = cryptoService.descomprimir(decryptedCompressed);

            // 4. Convertir a texto
            String originalMessage = new String(decompressed, "UTF-8");

            model.addAttribute("success", "Mensaje extraído exitosamente");
            model.addAttribute("extractedMessage", originalMessage);
            model.addAttribute("extractPassword", password);
            return "./modules/steganography";

        } catch (Exception e) {
            model.addAttribute("error", "Error al extraer mensaje: " + e.getMessage() + ". Verifique la contraseña o que la imagen contenga un mensaje válido.");
            return "./modules/steganography";
        }
    }

    /**
     * Descarga la imagen con el mensaje oculto.
     */
    @GetMapping("/download")
    public void downloadImage(@RequestParam("filename") String filename, HttpServletResponse response) throws IOException {
        Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
        if (!Files.exists(filePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Archivo no encontrado");
            return;
        }

        byte[] imageBytes = Files.readAllBytes(filePath);
        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setContentLength(imageBytes.length);
        response.getOutputStream().write(imageBytes);
        response.getOutputStream().flush();

        // Eliminar archivo temporal después de la descarga
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            // Log pero no fallar la descarga
        }
    }
}
