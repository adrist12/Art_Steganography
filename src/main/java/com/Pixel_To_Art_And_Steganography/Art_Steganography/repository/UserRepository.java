package com.Pixel_To_Art_And_Steganography.Art_Steganography.repository;



import com.Pixel_To_Art_And_Steganography.Art_Steganography.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // Spring traduce esto a: SELECT * FROM users WHERE email = ?

    Optional<User> findByEmail(String email);


    boolean existsByEmail(String email);
}
