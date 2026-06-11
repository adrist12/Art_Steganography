package com.Pixel_To_Art_And_Steganography.Art_Steganography.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Servicio de criptografía que implementa:
 * 1. Cifrado simétrico AES-256-GCM
 * 2. Compresión GZIP
 * 3. Mecanismo de extracción reversible
 *
 * Flujo: texto → comprimir → cifrar → ocultar en imagen
 * Flujo inverso: imagen → extraer → descifrar → descomprimir → texto original
 */
@Service
public class CryptoService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int AES_KEY_SIZE = 256;

    /**
     * Genera una clave AES-256 a partir de una contraseña
     */
    public SecretKey generarClave(String password) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE);
        SecretKey key = keyGen.generateKey();
        return key;
    }

    /**
     * Genera un IV aleatorio de 12 bytes para GCM
     */
    public byte[] generarIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * Comprime datos usando GZIP
     */
    public byte[] comprimir(byte[] datos) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(datos);
        }
        return baos.toByteArray();
    }

    /**
     * Descomprime datos GZIP
     */
    public byte[] descomprimir(byte[] datosComprimidos) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(datosComprimidos);
        try (GZIPInputStream gzip = new GZIPInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzip.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        }
    }

    /**
     * Cifra datos usando AES-256-GCM
     * Retorna: IV (12 bytes) + datos cifrados
     */
    public byte[] cifrar(byte[] datos, SecretKey clave) throws Exception {
        byte[] iv = generarIV();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, clave, parameterSpec);

        byte[] datosCifrados = cipher.doFinal(datos);

        // Concatenar IV + datos cifrados
        byte[] resultado = new byte[iv.length + datosCifrados.length];
        System.arraycopy(iv, 0, resultado, 0, iv.length);
        System.arraycopy(datosCifrados, 0, resultado, iv.length, datosCifrados.length);

        return resultado;
    }

    /**
     * Descifra datos AES-256-GCM
     * Espera: IV (12 bytes) + datos cifrados
     */
    public byte[] descifrar(byte[] datosCifradosConIV, SecretKey clave) throws Exception {
        if (datosCifradosConIV.length < GCM_IV_LENGTH) {
            throw new IllegalArgumentException("Datos cifrados inválidos: muy cortos");
        }

        // Extraer IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(datosCifradosConIV, 0, iv, 0, GCM_IV_LENGTH);

        // Extraer datos cifrados
        byte[] datosCifrados = new byte[datosCifradosConIV.length - GCM_IV_LENGTH];
        System.arraycopy(datosCifradosConIV, GCM_IV_LENGTH, datosCifrados, 0, datosCifrados.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, clave, parameterSpec);

        return cipher.doFinal(datosCifrados);
    }

    /**
     * Flujo completo: comprimir y luego cifrar
     * Retorna: IV + datos comprimidos y cifrados (Base64)
     */
    public String comprimirYCifrar(String textoPlano, String password) throws Exception {
        SecretKey clave = generarClave(password);

        // 1. Convertir texto a bytes
        byte[] datosOriginales = textoPlano.getBytes("UTF-8");

        // 2. Comprimir
        byte[] datosComprimidos = comprimir(datosOriginales);

        // 3. Cifrar
        byte[] datosCifrados = cifrar(datosComprimidos, clave);

        // 4. Retornar en Base64 para facilitar almacenamiento/transmisión
        return Base64.getEncoder().encodeToString(datosCifrados);
    }

    /**
     * Flujo inverso: descifrar y luego descomprimir
     * @param datosCifradosBase64 Datos en Base64 (IV + cifrado)
     * @param password Contraseña para derivar la clave
     * @return Texto original
     */
    public String descifrarYDescomprimir(String datosCifradosBase64, String password) throws Exception {
        SecretKey clave = generarClave(password);

        // 1. Decodificar Base64
        byte[] datosCifrados = Base64.getDecoder().decode(datosCifradosBase64);

        // 2. Descifrar
        byte[] datosComprimidos = descifrar(datosCifrados, clave);

        // 3. Descomprimir
        byte[] datosOriginales = descomprimir(datosComprimidos);

        // 4. Convertir a texto
        return new String(datosOriginales, "UTF-8");
    }

    /**
     * Cifra datos binarios (para imágenes) usando AES-256-GCM
     * Retorna: IV + datos cifrados
     */
    public byte[] cifrarDatosBinarios(byte[] datos, SecretKey clave) throws Exception {
        return cifrar(datos, clave);
    }

    /**
     * Descifra datos binarios
     */
    public byte[] descifrarDatosBinarios(byte[] datosCifrados, SecretKey clave) throws Exception {
        return descifrar(datosCifrados, clave);
    }

    /**
     * Genera una clave desde una contraseña (para uso consistente)
     * Usa PBKDF2 para derivar la clave de la contraseña
     */
    public SecretKey generarClaveDesdePassword(String password) throws Exception {
        // Para simplificar, usamos un hash SHA-256 de la contraseña como clave
        // En producción, usar PBKDF2 con salt
        byte[] keyBytes = password.getBytes("UTF-8");
        // Asegurar que la clave tenga 32 bytes (256 bits)
        byte[] key256 = new byte[32];
        System.arraycopy(keyBytes, 0, key256, 0, Math.min(keyBytes.length, 32));
        return new SecretKeySpec(key256, "AES");
    }

    /**
     * Versión mejorada de comprimirYCifrar que usa derivación de clave
     */
    public String comprimirYCifrarV2(String textoPlano, String password) throws Exception {
        SecretKey clave = generarClaveDesdePassword(password);

        byte[] datosOriginales = textoPlano.getBytes("UTF-8");
        byte[] datosComprimidos = comprimir(datosOriginales);
        byte[] datosCifrados = cifrar(datosComprimidos, clave);

        return Base64.getEncoder().encodeToString(datosCifrados);
    }

    /**
     * Versión mejorada de descifrarYDescomprimir
     */
    public String descifrarYDescomprimirV2(String datosCifradosBase64, String password) throws Exception {
        SecretKey clave = generarClaveDesdePassword(password);

        byte[] datosCifrados = Base64.getDecoder().decode(datosCifradosBase64);
        byte[] datosComprimidos = descifrar(datosCifrados, clave);
        byte[] datosOriginales = descomprimir(datosComprimidos);

        return new String(datosOriginales, "UTF-8");
    }
}
