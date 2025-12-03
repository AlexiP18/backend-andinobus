package com.andinobus.backendsmartcode.catalogos.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa la configuración detallada del layout de asientos de un bus
 */
@Entity
@Table(name = "asiento_layout")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsientoLayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @Column(name = "numero_asiento", nullable = false)
    private Integer numeroAsiento;

    @Column(nullable = false)
    private Integer fila;

    @Column(nullable = false)
    private Integer columna;

    @Column(name = "tipo_asiento", nullable = false, length = 32)
    @Builder.Default
    private String tipoAsiento = "NORMAL"; // NORMAL | VIP | ACONDICIONADO

    @Column(nullable = false)
    @Builder.Default
    private Integer piso = 1; // 1 = Piso 1, 2 = Piso 2 (solo para buses con dos niveles)

    @Column(nullable = false)
    @Builder.Default
    private Boolean habilitado = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (habilitado == null) {
            habilitado = true;
        }
        if (tipoAsiento == null) {
            tipoAsiento = "NORMAL";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
