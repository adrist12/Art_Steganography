package com.Pixel_To_Art_And_Steganography.Art_Steganography.repository;

// El Repository — analogía: el catálogo de la biblioteca
// Spring Data JPA GENERA la implementación automáticamente
// Solo defines la interfaz con los métodos que necesitas

import com.Pixel_To_Art_And_Steganography.Art_Steganography.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // Spring traduce esto a: SELECT * FROM users WHERE email = ?
    // La magia: el nombre del método ES la query
    Optional<User> findByEmail(String email);

    // ¿Qué generaría este método?
    boolean existsByEmail(String email);
}
