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

        // 🚀 SOLUCIÓN PORTABLE: Buscamos primero una carpeta 'tessdata' local en el proyecto
        File rutaLocalTessdata = new File("tessdata");

        if (rutaLocalTessdata.exists() && rutaLocalTessdata.isDirectory()) {
            // Si creas la carpeta en la raíz del proyecto, usará esta de forma segura
            tesseract.setDatapath(rutaLocalTessdata.getAbsolutePath());
            System.out.println("Tesseract OCR: Cargando datos desde el directorio local del proyecto: " + rutaLocalTessdata.getAbsolutePath());
        } else {
            // Si no existe la carpeta local, detectamos el sistema operativo de respaldo
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // Ruta estándar si instalaste Tesseract en Windows
                tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
            } else {
                // Ruta estándar para entornos Linux / despliegues
                tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
            }
            System.out.println("Tesseract OCR: Carpeta local no encontrada. Usando ruta por defecto del sistema operativo.");
        }

        // Configurar idioma por defecto (inglés)
        tesseract.setLanguage("eng");

        // Configurar modo de página: 1 significa OSD (Orientación y detección de script automática)
        // Cambiar a 3 (Fully automatic page segmentation, but no OSD) suele ser más estable si el 'eng.traineddata' es básico.
        tesseract.setPageSegMode(3);
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
            // Guardar el MultipartFile en un archivo temporal de forma segura
            archivoTemporal = File.createTempFile("tesseract_", "_" + imagen.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(archivoTemporal)) {
                fos.write(imagen.getBytes());
            }

            // Ejecutar OCR de forma sincronizada para proteger el hilo de memoria nativa de JNA
            String textoExtraido;
            synchronized (this) {
                textoExtraido = tesseract.doOCR(archivoTemporal);
            }

            // Limpiar el texto (eliminar espacios extra y líneas vacías)
            textoExtraido = limpiarTexto(textoExtraido);

            return textoExtraido;

        } catch (TesseractException e) {
            throw new RuntimeException("Error en el motor OCR: Revise si 'eng.traineddata' existe en la ruta configurada. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar la imagen: " + e.getMessage(), e);
        } finally {
            // Garantizar la eliminación del archivo temporal para no llenar el disco
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