package com.Pixel_To_Art_And_Steganography.Art_Steganography.service;

import com.Pixel_To_Art_And_Steganography.Art_Steganography.domain.User;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.domain.WebAuthnCredential;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.repository.UserRepository;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.repository.WebAuthnCredentialRepository;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;


@Service
public class WebAuthnService {

    private final RelyingParty relyingParty;
    private final UserRepository userRepository;
    private final WebAuthnCredentialRepository credentialRepository;

    // Inyección de dependencias (asumiendo que RelyingParty ya está configurado como Bean)
    public WebAuthnService(RelyingParty relyingParty,
                           UserRepository userRepository,
                           WebAuthnCredentialRepository credentialRepository) {
        this.relyingParty = relyingParty;
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
    }

    // =========================================================================
    // MÉTODO 1: INICIAR REGISTRO
    // =========================================================================
    public PublicKeyCredentialCreationOptions startRegistration(
            String email, String nombre, HttpSession session) {

        // 1. Construir UserIdentity
        // El userHandle debe ser único e inmutable. Usamos el email en bytes.
        ByteArray userHandle = new ByteArray(email.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        UserIdentity userIdentity = UserIdentity.builder()
                .name(email)          // username visible
                .displayName(nombre)  // nombre para mostrar
                .id(userHandle)       // el handle en bytes
                .build();

        // 2. Crear opciones de inicio
        StartRegistrationOptions options = StartRegistrationOptions.builder()
                .user(userIdentity)
                .build();

        // 3. Generar las opciones públicas y guardar el estado en sesión
        PublicKeyCredentialCreationOptions creationOptions = relyingParty.startRegistration(options);

        // Guardamos el objeto completo para verificarlo luego
        session.setAttribute("registrationRequest", creationOptions);

        return creationOptions;
    }

    // =========================================================================
    // MÉTODO 2: FINALIZAR REGISTRO
    // =========================================================================
    @Transactional

    public void finishRegistration(String responseJson, HttpSession session) throws Exception {

        PublicKeyCredentialCreationOptions creationOptions =
                (PublicKeyCredentialCreationOptions) session.getAttribute("registrationRequest");

        if (creationOptions == null) {
            throw new IllegalStateException("Solicitud de registro expirada.");
        }


        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential =
                PublicKeyCredential.parseRegistrationResponseJson(responseJson);

        FinishRegistrationOptions finishOptions = FinishRegistrationOptions.builder()
                .request(creationOptions)
                .response(credential)
                .build();

        // ✅ Retorna RegistrationResult, no RegisteredCredential
        RegistrationResult result = relyingParty.finishRegistration(finishOptions);

        // ✅ Email desde el userHandle de la sesión
        String email = new String(
                creationOptions.getUser().getId().getBytes(),
                java.nio.charset.StandardCharsets.UTF_8
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        WebAuthnCredential newCred = new WebAuthnCredential();
        newCred.setUsuario(user);
        newCred.setCredentialId(result.getKeyId().getId().getBytes()); // ✅
        newCred.setPublicKeyCose(result.getPublicKeyCose().getBytes());
        newCred.setSignCount(result.getSignatureCount());

        credentialRepository.save(newCred);
        session.removeAttribute("registrationRequest");
    }

    // =========================================================================
    // MÉTODO 3: INICIAR LOGIN
    // =========================================================================
    public AssertionRequest  startLogin(String email, HttpSession session) {
        // 1. Verificar si el usuario existe (opcional pero recomendado para UX)
        if (!userRepository.existsByEmail(email)) {
            // Por seguridad, a veces se prefiere no revelar si el usuario existe o no.
            // Pero para WebAuthn necesitamos saber si tiene credenciales registradas.
            throw new IllegalArgumentException("Usuario no encontrado o sin credenciales registradas.");
        }

        // 2. Crear opciones de inicio de aserción
        StartAssertionOptions options = StartAssertionOptions.builder()
                .username(email)
                .build();

        // 3. Generar el challenge y guardar el request en sesión
        AssertionRequest assertionRequest = relyingParty.startAssertion(options);
        session.setAttribute("assertionRequest", assertionRequest);

        // Retornamos las opciones que el frontend necesita
        return assertionRequest;
    }

    // =========================================================================
    // MÉTODO 4: FINALIZAR LOGIN
    // =========================================================================
    @Transactional
    public String finishLogin(String responseJson, HttpSession session) throws Exception {

        AssertionRequest assertionRequest =
                (AssertionRequest) session.getAttribute("assertionRequest");

        if (assertionRequest == null) {
            throw new IllegalStateException("Sesión de login inválida o expirada.");
        }


        PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential =
                PublicKeyCredential.parseAssertionResponseJson(responseJson);

        FinishAssertionOptions finishOptions = FinishAssertionOptions.builder()
                .request(assertionRequest)
                .response(credential)  // ✅ tipos coinciden
                .build();

        AssertionResult result = relyingParty.finishAssertion(finishOptions);

        if (!result.isSuccess()) {
            throw new SecurityException("Autenticación WebAuthn fallida.");
        }

        // Actualizar signCount
        credentialRepository.findByCredentialId(result.getCredentialId().getBytes())
                .ifPresent(cred -> {
                    cred.setSignCount(result.getSignatureCount());
                    cred.setLastUsed(java.time.LocalDateTime.now());
                    credentialRepository.save(cred);
                });

        session.removeAttribute("assertionRequest");
        return result.getUsername();
    }
}