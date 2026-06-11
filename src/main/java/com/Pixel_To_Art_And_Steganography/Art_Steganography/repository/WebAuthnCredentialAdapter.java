package com.Pixel_To_Art_And_Steganography.Art_Steganography.repository;

import com.Pixel_To_Art_And_Steganography.Art_Steganography.domain.User;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.repository.UserRepository;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.repository.WebAuthnCredentialRepository;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.domain.WebAuthnCredential; // Asegúrate de importar tu entidad
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WebAuthnCredentialAdapter implements CredentialRepository {

    private final WebAuthnCredentialRepository credentialRepo;
    private final UserRepository userRepo;

    public WebAuthnCredentialAdapter(WebAuthnCredentialRepository credentialRepo,
                                     UserRepository userRepo) {
        this.credentialRepo = credentialRepo;
        this.userRepo = userRepo;
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        // 1. Busca el usuario por email (username)
        Optional<User> userOpt = userRepo.findByEmail(username);
        if (userOpt.isEmpty()) {
            return Set.of(); // Retorna set vacío si no existe el usuario
        }

        User user = userOpt.get();

        // 2. Busca todas sus credenciales WebAuthn
        // Asumo que tienes un método findAllByUsuario en tu repositorio
        List<WebAuthnCredential> credenciales = credentialRepo.findByUsuario(user);

        // 3. Convierte cada credentialId a PublicKeyCredentialDescriptor
        return credenciales.stream()
                .map(cred -> PublicKeyCredentialDescriptor.builder()
                        .id(new ByteArray(cred.getCredentialId()))
                        .type(PublicKeyCredentialType.PUBLIC_KEY)
                        .build())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        // Convierte el email (username) a ByteArray
        // En WebAuthn, el userHandle suele ser el ID del usuario o el email en bytes
        return Optional.of(new ByteArray(username.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        // Operación inversa: convierte ByteArray a String (email)
        String username = new String(userHandle.getBytes(), StandardCharsets.UTF_8);

        // Verificamos opcionalmente que el usuario exista antes de retornar
        if (userRepo.existsByEmail(username)) {
            return Optional.of(username);
        }
        return Optional.empty();
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        // 1. Busca la credencial por el ID binario en tu BD
        // Asumo que tienes un método findByCredentialId en tu repositorio
        Optional<WebAuthnCredential> credOpt = credentialRepo.findByCredentialId(credentialId.getBytes());

        if (credOpt.isEmpty()) {
            return Optional.empty();
        }

        WebAuthnCredential cred = credOpt.get();

        // 2. Verifica que el userHandle coincida (seguridad adicional)
        String emailFromHandle = new String(userHandle.getBytes(), StandardCharsets.UTF_8);
        if (!cred.getUsuario().getEmail().equals(emailFromHandle)) {
            return Optional.empty();
        }

        // 3. Construye el RegisteredCredential
        RegisteredCredential registeredCred = RegisteredCredential.builder()
                .credentialId(new ByteArray(cred.getCredentialId()))
                .userHandle(userHandle)
                .publicKeyCose(new ByteArray(cred.getPublicKeyCose()))
                .signatureCount(cred.getSignCount())
                .build();
        return Optional.of(registeredCred);
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        // Busca por credentialId (debería ser único, pero la interfaz pide un Set)
        Optional<WebAuthnCredential> credOpt = credentialRepo.findByCredentialId(credentialId.getBytes());

        if (credOpt.isEmpty()) {
            return Set.of();
        }

        WebAuthnCredential cred = credOpt.get();

        // Reconstruimos el userHandle desde el email del usuario asociado
        ByteArray userHandle = new ByteArray(cred.getUsuario().getEmail().getBytes(StandardCharsets.UTF_8));

        RegisteredCredential registeredCred = RegisteredCredential.builder()
                .credentialId(new ByteArray(cred.getCredentialId()))
                .userHandle(userHandle)
                .publicKeyCose(new ByteArray(cred.getPublicKeyCose()))
                .signatureCount(cred.getSignCount())
                .build();

        return Set.of(registeredCred);
    }
}   