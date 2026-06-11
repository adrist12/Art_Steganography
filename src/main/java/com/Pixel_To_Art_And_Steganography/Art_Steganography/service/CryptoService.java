package com.Pixel_To_Art_And_Steganography.Art_Steganography.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Service
public class CryptoService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;
    private static final int SALT_LENGTH = 16;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Comprime datos usando GZIP.
     *
     * @param data Datos a comprimir (bytes)
     * @return Datos comprimidos
     */
    public byte[] compress(byte[] data) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {

            gzipOutputStream.write(data);
            gzipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al comprimir datos: " + e.getMessage(), e);
        }
    }

    /**
     * Descomprime datos GZIP.
     *
     * @param compressedData Datos comprimidos
     * @return Datos descomprimidos
     */
    public byte[] decompress(byte[] compressedData) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al descomprimir datos: " + e.getMessage(), e);
        }
    }

    /**
     * Cifra texto plano usando AES-GCM.
     * Flujo: texto → comprimir → cifrar → Base64
     *
     * @param plaintext Texto a cifrar
     * @param password  Contraseña para derivar la clave
     * @return Cadena Base64 con IV + datos cifrados
     */
    public String encrypt(String plaintext, String password) {
        try {
            // 1. Comprimir el texto
            byte[] compressedData = compress(plaintext.getBytes("UTF-8"));

            // 2. Generar salt y derivar clave
            byte[] salt = new byte[SALT_LENGTH];
            SECURE_RANDOM.nextBytes(salt);
            SecretKey secretKey = deriveKey(password, salt);

            // 3. Generar IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            // 4. Cifrar
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            byte[] encryptedData = cipher.doFinal(compressedData);

            // 5. Combinar salt + iv + datos cifrados y codificar en Base64
            byte[] combined = new byte[salt.length + iv.length + encryptedData.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(iv, 0, combined, salt.length, iv.length);
            System.arraycopy(encryptedData, 0, combined, salt.length + iv.length, encryptedData.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("Error al cifrar datos: " + e.getMessage(), e);
        }
    }

    /**
     * Descifra datos usando AES-GCM.
     * Flujo: Base64 → descifrar → descomprimir → texto original
     *
     * @param encryptedBase64 Cadena Base64 con IV + datos cifrados
     * @param password        Contraseña para derivar la clave
     * @return Texto descifrado y descomprimido
     */
    public String decrypt(String encryptedBase64, String password) {
        try {
            // 1. Decodificar Base64
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);

            // 2. Extraer salt, iv y datos cifrados
            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[combined.length - SALT_LENGTH - GCM_IV_LENGTH];

            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(combined, SALT_LENGTH, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, SALT_LENGTH + GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);

            // 3. Derivar clave
            SecretKey secretKey = deriveKey(password, salt);

            // 4. Descifrar
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            byte[] compressedData = cipher.doFinal(encryptedData);

            // 5. Descomprimir
            byte[] decompressedData = decompress(compressedData);

            return new String(decompressedData, "UTF-8");

        } catch (Exception e) {
            throw new RuntimeException("Error al descifrar datos: " + e.getMessage(), e);
        }
    }

    /**
     * Cifra bytes arbitrarios usando AES-GCM.
     * Flujo: bytes → comprimir → cifrar → Base64
     *
     * @param data     Datos a cifrar
     * @param password Contraseña para derivar la clave
     * @return Cadena Base64 con IV + datos cifrados
     */
    public String encryptBytes(byte[] data, String password) {
        try {
            // 1. Comprimir
            byte[] compressedData = compress(data);

            // 2. Generar salt y derivar clave
            byte[] salt = new byte[SALT_LENGTH];
            SECURE_RANDOM.nextBytes(salt);
            SecretKey secretKey = deriveKey(password, salt);

            // 3. Generar IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            // 4. Cifrar
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            byte[] encryptedData = cipher.doFinal(compressedData);

            // 5. Combinar salt + iv + datos cifrados y codificar en Base64
            byte[] combined = new byte[salt.length + iv.length + encryptedData.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(iv, 0, combined, salt.length, iv.length);
            System.arraycopy(encryptedData, 0, combined, salt.length + iv.length, encryptedData.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("Error al cifrar bytes: " + e.getMessage(), e);
        }
    }

    /**
     * Descifra bytes usando AES-GCM.
     * Flujo: Base64 → descifrar → descomprimir → bytes originales
     *
     * @param encryptedBase64 Cadena Base64 con IV + datos cifrados
     * @param password        Contraseña para derivar la clave
     * @return Datos descifrados y descomprimidos
     */
    public byte[] decryptBytes(String encryptedBase64, String password) {
        try {
            // 1. Decodificar Base64
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);

            // 2. Extraer salt, iv y datos cifrados
            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedData = new byte[combined.length - SALT_LENGTH - GCM_IV_LENGTH];

            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(combined, SALT_LENGTH, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, SALT_LENGTH + GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);

            // 3. Derivar clave
            SecretKey secretKey = deriveKey(password, salt);

            // 4. Descifrar
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            byte[] compressedData = cipher.doFinal(encryptedData);

            // 5. Descomprimir
            return decompress(compressedData);

        } catch (Exception e) {
            throw new RuntimeException("Error al descifrar bytes: " + e.getMessage(), e);
        }
    }

    /**
     * Deriva una clave AES a partir de una contraseña y un salt usando PBKDF2.
     *
     * @param password Contraseña
     * @param salt     Salt aleatorio
     * @return Clave secreta AES
     */
    private SecretKey deriveKey(String password, byte[] salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            return new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (Exception e) {
            throw new RuntimeException("Error al derivar clave: " + e.getMessage(), e);
        }
    }
}
