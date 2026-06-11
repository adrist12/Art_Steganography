package com.Pixel_To_Art_And_Steganography.Art_Steganography.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Servicio para extraer texto de imágenes usando Tesseract OCR.
 * Procesa imágenes subidas por el usuario y retorna el texto reconocido.
 */
@Service
public class ArtService {

    private final ITesseract tesseract;

    public ArtService() {
        this.tesseract = new Tesseract();
        // Configurar ruta de datos de Tesseract (ajustar según instalación)
        // En Windows: "C:\\Program Files\\Tesseract OCR\\tessdata"
        // En Mac/Linux: "/usr/share/tesseract-ocr/4.00/tessdata" o "/usr/local/share/tessdata"
        // Si no se configura, Tesseract buscará en la ruta por defecto del sistema
        try {
            tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
        } catch (Exception e) {
            // Si no existe la ruta, Tesseract usará la ruta por defecto
            System.out.println("Advertencia: No se pudo configurar la ruta de tessdata. Se usará la ruta por defecto.");
        }

        // Configurar idioma por defecto (inglés)
        tesseract.setLanguage("eng");
        // Configurar modo de página (auto-detección)
        tesseract.setPageSegMode(1);
    }

    /**
     * Extrae texto de una imagen usando Tesseract OCR.
     *
     * @param imagen Archivo de imagen (PNG, JPG, BMP, etc.)
     * @return Texto extraído de la imagen
     * @throws RuntimeException si ocurre un error durante el procesamiento
     */
    public String extraerTextoDeImagen(MultipartFile imagen) {
        if (imagen == null || imagen.isEmpty()) {
            throw new IllegalArgumentException("La imagen no puede estar vacía");
        }

        File archivoTemporal = null;

        try {
            // Guardar el MultipartFile en un archivo temporal
            archivoTemporal = File.createTempFile("tesseract_", "_" + imagen.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(archivoTemporal)) {
                fos.write(imagen.getBytes());
            }

            // Ejecutar OCR
            String textoExtraido = tesseract.doOCR(archivoTemporal);

            // Limpiar el texto (eliminar espacios extra y líneas vacías)
            textoExtraido = limpiarTexto(textoExtraido);

            return textoExtraido;

        } catch (TesseractException e) {
            throw new RuntimeException("Error en el motor OCR: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar la imagen: " + e.getMessage(), e);
        } finally {
            // Eliminar archivo temporal
            if (archivoTemporal != null && archivoTemporal.exists()) {
                archivoTemporal.delete();
            }
        }
    }

    /**
     * Limpia el texto extraído eliminando espacios extra y líneas vacías.
     */
    private String limpiarTexto(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }

        // Eliminar líneas vacías y espacios extra
        StringBuilder sb = new StringBuilder();
        String[] lineas = texto.split("\n");

        for (String linea : lineas) {
            String lineaLimpia = linea.trim();
            if (!lineaLimpia.isEmpty()) {
                sb.append(lineaLimpia).append("\n");
            }
        }

        return sb.toString().trim();
    }

}
