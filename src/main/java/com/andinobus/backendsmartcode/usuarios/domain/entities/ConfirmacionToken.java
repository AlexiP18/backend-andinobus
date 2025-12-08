package com.andinobus.backendsmartcode.usuarios.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Token para confirmación de cuenta de CLIENTES
 * Expira en 24 horas
 */
@Entity
@Table(name = "confirmacion_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmacionToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_email", nullable = false, length = 180)
    private String userEmail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            // Expira en 24 horas por defecto
            expiresAt = LocalDateTime.now().plusHours(24);
        }
    }

    /**
     * Verifica si el token ha expirado
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Verifica si el token ya fue confirmado
     */
    public boolean isConfirmed() {
        return confirmedAt != null;
    }

    /**
     * Verifica si el token es válido (no expirado y no confirmado)
     */
    public boolean isValid() {
        return !isExpired() && !isConfirmed();
    }
}
