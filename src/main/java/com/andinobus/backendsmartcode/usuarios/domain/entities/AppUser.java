package com.andinobus.backendsmartcode.usuarios.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 180)
    private String email;
    
    @Column(name = "password_hash", length = 255)
    private String passwordHash;
    
    @Column(length = 120)
    private String nombres;
    
    @Column(length = 120)
    private String apellidos;
    
    @Column(nullable = false, length = 32)
    @Builder.Default
    private String rol = "CLIENTE";
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
    
    @Column(name = "email_confirmado", nullable = false)
    @Builder.Default
    private Boolean emailConfirmado = false;
    
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
        if (rol == null) {
            rol = "CLIENTE";
        }
        if (emailConfirmado == null) {
            emailConfirmado = false;
        }
    }
}
