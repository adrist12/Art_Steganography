package com.Pixel_To_Art_And_Steganography.Art_Steganography.service;

// El Repository — analogía: el catálogo de la biblioteca
// Spring Data JPA GENERA la implementación automáticamente
// Solo defines la interfaz con los métodos que necesitas

import com.Pixel_To_Art_And_Steganography.Art_Steganography.domain.User;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.dto.RegisterForm;
import com.Pixel_To_Art_And_Steganography.Art_Steganography.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

// AuthService — analogía: el bibliotecario que verifica carnet
// Recibe credenciales, consulta repositorio, compara password

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<String> authenticate(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> BCrypt.checkpw(password, user.getContrasena()))
                .map(User::getNombre);
    }
    public void registerUser(RegisterForm form) {
        // 1. Validar manualmente si las contraseñas coinciden
        if (!form.getContrasena().equals(form.getConfirmarContrasena())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        // 2. Verificar si el email ya existe
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // 3. Hashear la contraseña
        String contrasenaHasheada = BCrypt.hashpw(form.getContrasena(), BCrypt.gensalt(12));
        // 4. Crear la entidad User (NO pasas el DTO directo a la BD)
        User user = new User();
        user.setNombre(form.getNombre());
        user.setEmail(form.getEmail());
        user.setContrasena(contrasenaHasheada);

        // 5. Guardar
        userRepository.save(user);
    }
}
