package com.Pixel_To_Art_And_Steganography.Art_Steganography.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;


@Entity
@Table(name = "credenciales_webauthn")
@Data
public class WebAuthnCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación Many-to-One: Muchas credenciales pertenecen a un usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private User usuario;

    // Identificador único de la credencial (binario)
    @Column(name = "credential_id", nullable = false, columnDefinition = "VARBINARY(255)")
    private byte[] credentialId;

    // Clave pública en formato COSE (binario, puede ser grande)
    @Column(name = "public_key_cose", nullable = false, columnDefinition = "BLOB")
    private byte[] publicKeyCose;

    // Contador de firmas para protección anti-replay
    @Column(name = "sign_count", nullable = false)
    private long signCount;

    // Identificador del tipo de autenticador (binario, 16 bytes usualmente)
    @Column(name = "aaguid", columnDefinition = "BINARY(16)")
    private byte[] aaguid;

    // Nombre amigable para el usuario (ej. "iPhone de Juan")
    @Column(name = "nombre_dispositivo", length = 100)
    private String nombreDispositivo;

    // Fecha de registro
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Última vez que se usó
    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    // Callbacks para manejar fechas automáticamente
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastUsed = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        // Opcional: Actualizar lastUsed solo si se usa para login,
        // o manejarlo manualmente en el servicio de login.
        // this.lastUsed = LocalDateTime.now();
    }
}
