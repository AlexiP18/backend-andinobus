package com.andinobus.backendsmartcode.operacion.domain.entities;

import com.andinobus.backendsmartcode.ventas.domain.entities.Reserva;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "viaje_asiento")
public class ViajeAsiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    @Column(name = "numero_asiento", nullable = false, length = 10)
    private String numeroAsiento;

    @Column(name = "tipo_asiento", nullable = false, length = 32)
    private String tipoAsiento;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String estado = "DISPONIBLE"; // DISPONIBLE | RESERVADO | VENDIDO | BLOQUEADO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (estado == null) estado = "DISPONIBLE";
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
