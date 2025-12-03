package com.andinobus.backendsmartcode.catalogos.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa la relación entre una Cooperativa y un Terminal.
 * Permite establecer en qué terminales opera cada cooperativa.
 */
@Entity
@Table(name = "cooperativa_terminal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CooperativaTerminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cooperativa_id", nullable = false)
    private Cooperativa cooperativa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false)
    private Terminal terminal;

    @Column(name = "es_sede_principal")
    @Builder.Default
    private Boolean esSedePrincipal = false;

    @Column(name = "numero_andenes_asignados")
    @Builder.Default
    private Integer numeroAndenesAsignados = 0;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

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
