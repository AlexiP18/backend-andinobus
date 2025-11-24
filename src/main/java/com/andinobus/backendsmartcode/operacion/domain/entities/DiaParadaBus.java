package com.andinobus.backendsmartcode.operacion.domain.entities;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa un día de parada programado para un bus
 * Útil para gestionar mantenimiento, descansos programados, etc.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dia_parada_bus")
public class DiaParadaBus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 32)
    private String motivo; // MANTENIMIENTO | EXCESO_CAPACIDAD | OTRO

    @Column(length = 500)
    private String observaciones;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
