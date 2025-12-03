package com.andinobus.backendsmartcode.catalogos.domain.entities;

import com.andinobus.backendsmartcode.usuarios.domain.entities.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa la relación entre un Usuario (oficinista) y un Terminal.
 * Permite establecer en qué terminales trabaja cada oficinista.
 */
@Entity
@Table(name = "usuario_terminal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioTerminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private AppUser usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false)
    private Terminal terminal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cooperativa_id")
    private Cooperativa cooperativa;

    @Column(length = 100)
    @Builder.Default
    private String cargo = "Oficinista";

    @Column(length = 50)
    private String turno; // MAÑANA, TARDE, NOCHE, COMPLETO

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
