package com.Pixel_To_Art_And_Steganography.Art_Steganography.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class StegoService {

    private static final String END_DELIMITER = "ENDSTEGO";
    private static final int BITS_PER_BYTE = 8;
    private static final int CHANNELS_PER_PIXEL = 3; // RGB

    /**
     * Oculta un mensaje de texto en una imagen usando LSB steganography.
     * Modifica los bits menos significativos de los canales RGB.
     *
     * @param imageFile Archivo de imagen (PNG recomendado)
     * @param message   Mensaje de texto a ocultar
     * @return BufferedImage con el mensaje oculto
     */
    public BufferedImage hideMessage(MultipartFile imageFile, String message) throws IOException {
        BufferedImage image = ImageIO.read(imageFile.getInputStream());
        if (image == null) {
            throw new IOException("No se pudo leer la imagen. Asegúrate de que sea un formato válido (PNG recomendado).");
        }

        String messageWithDelimiter = message + END_DELIMITER;
        byte[] messageBytes = messageWithDelimiter.getBytes("UTF-8");
        List<Integer> messageBits = bytesToBits(messageBytes);

        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;
        int totalChannels = totalPixels * CHANNELS_PER_PIXEL;

        if (messageBits.size() > totalChannels) {
            throw new IOException("El mensaje es demasiado largo para esta imagen. " +
                    "Se necesitan " + messageBits.size() + " bits, pero la imagen solo tiene " + totalChannels + " bits disponibles.");
        }

        BufferedImage stegoImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        stegoImage.createGraphics().drawImage(image, 0, 0, null);

        int bitIndex = 0;
        outerLoop:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = stegoImage.getRGB(x, y);
                Color color = new Color(rgb);

                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();

                int[] channels = {red, green, blue};
                int[] newChannels = new int[3];

                for (int c = 0; c < CHANNELS_PER_PIXEL; c++) {
                    if (bitIndex < messageBits.size()) {
                        // Limpiar el LSB y establecer el nuevo bit
                        newChannels[c] = (channels[c] & 0xFE) | messageBits.get(bitIndex);
                        bitIndex++;
                    } else {
                        newChannels[c] = channels[c];
                    }
                }

                Color newColor = new Color(newChannels[0], newChannels[1], newChannels[2]);
                stegoImage.setRGB(x, y, newColor.getRGB());

                if (bitIndex >= messageBits.size()) {
                    break outerLoop;
                }
            }
        }

        return stegoImage;
    }

    /**
     * Extrae un mensaje de texto oculto en una imagen usando LSB steganography.
     *
     * @param imageFile Archivo de imagen con mensaje oculto
     * @return Mensaje de texto extraído
     */
    public String extractMessage(MultipartFile imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile.getInputStream());
        if (image == null) {
            throw new IOException("No se pudo leer la imagen. Asegúrate de que sea un formato válido.");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        List<Integer> extractedBits = new ArrayList<>();

        // Extraer bits LSB de cada canal RGB
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                Color color = new Color(rgb);

                // Extraer LSB de cada canal
                extractedBits.add(color.getRed() & 1);
                extractedBits.add(color.getGreen() & 1);
                extractedBits.add(color.getBlue() & 1);
            }
        }

        // Convertir bits a bytes
        byte[] extractedBytes = bitsToBytes(extractedBits);
        String extractedText = new String(extractedBytes, "UTF-8");

        // Buscar el delimitador de fin
        int delimiterIndex = extractedText.indexOf(END_DELIMITER);
        if (delimiterIndex != -1) {
            return extractedText.substring(0, delimiterIndex);
        } else {
            throw new IOException("No se encontró un mensaje oculto válido en la imagen. " +
                    "Asegúrate de que la imagen contenga un mensaje cifrado con este sistema.");
        }
    }

    /**
     * Convierte un array de bytes a una lista de bits (0 o 1).
     */
    private List<Integer> bytesToBits(byte[] bytes) {
        List<Integer> bits = new ArrayList<>();
        for (byte b : bytes) {
            for (int i = BITS_PER_BYTE - 1; i >= 0; i--) {
                bits.add((b >> i) & 1);
            }
        }
        return bits;
    }

    /**
     * Convierte una lista de bits a un array de bytes.
     */
    private byte[] bitsToBytes(List<Integer> bits) {
        int numBytes = bits.size() / BITS_PER_BYTE;
        byte[] bytes = new byte[numBytes];

        for (int i = 0; i < numBytes; i++) {
            int byteValue = 0;
            for (int j = 0; j < BITS_PER_BYTE; j++) {
                int bitIndex = i * BITS_PER_BYTE + j;
                if (bitIndex < bits.size()) {
                    byteValue = (byteValue << 1) | bits.get(bitIndex);
                }
            }
            bytes[i] = (byte) byteValue;
        }

        return bytes;
    }

    /**
     * Convierte BufferedImage a byte array (PNG).
     */
    public byte[] imageToBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    /**
     * Convierte byte array a BufferedImage.
     */
    public BufferedImage bytesToImage(byte[] imageBytes) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(imageBytes));
    }
}
