package com.Pixel_To_Art_And_Steganography.Art_Steganography.repository;

import com.Pixel_To_Art_And_Steganography.Art_Steganography.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.domain.WebAuthnCredential;

public interface WebAuthnCredentialRepository extends JpaRepository<WebAuthnCredential, Long> {
    // Todas las credenciales de un usuario
    // Usada en: mostrar dispositivos registrados, login (buscar candidatos)
    List<WebAuthnCredential> findByUsuario(User usuario);
    // Buscar por el ID que manda el browser durante el login
    // Usada en: WebAuthnService.finishLogin()
    Optional<WebAuthnCredential> findByCredentialId(byte[] credentialId);

    boolean existsByUsuario(User usuario);

}
