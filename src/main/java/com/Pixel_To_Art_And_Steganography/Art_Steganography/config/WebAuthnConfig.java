package com.Pixel_To_Art_And_Steganography.Art_Steganography.config;
// config/WebAuthnConfig.java
// Analogía: es la "identidad oficial" de tu servidor en el protocolo
// Como el certificado SSL — define quién eres ante el autenticador

import com.Pixel_To_Art_And_Steganography.Art_Steganography.repository.WebAuthnCredentialAdapter;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.repository.WebAuthnCredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
@Configuration
public class WebAuthnConfig {

    @Bean
    public RelyingParty relyingParty(WebAuthnCredentialAdapter adapter) {

        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id("localhost")
                .name("Art Steganography")
                .build();

        return RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(adapter)
                .origins(Set.of("http://localhost:8080"))
                .build();
    }
}
