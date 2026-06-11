package com.Pixel_To_Art_And_Steganography.Art_Steganography.domain;

import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;




@Entity                          // Le dice a JPA: "esto es una tabla"
@Table(name = "usuarios")
@Data
public class User {


    @Id                          // Clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private int id_usuario;


    @Column(nullable = false)
    private String nombre;


    @Column(unique = true, nullable = false, length = 100)
    private String email;        // Único, obligatorio


    @Column(nullable = false)
    private String contrasena;     // ← ¿Qué problema ves aquí?
    // Pista: nunca guardes texto plano



}
