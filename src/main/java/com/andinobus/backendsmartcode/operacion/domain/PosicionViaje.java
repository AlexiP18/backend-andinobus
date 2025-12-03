package com.andinobus.backendsmartcode.operacion.domain;

import com.andinobus.backendsmartcode.operacion.domain.entities.Viaje;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Almacena las posiciones GPS del bus durante un viaje.
 * Utilizado para tracking en tiempo real y análisis histórico.
 */
@Entity
@Table(
    name = "posicion_viaje",
    indexes = {
        @Index(name = "idx_viaje_timestamp", columnList = "viaje_id, timestamp DESC")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PosicionViaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    /**
     * Coordenadas GPS actuales del bus
     */
    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    /**
     * Velocidad del bus en km/h
     */
    @Column(name = "velocidad_kmh")
    private Double velocidadKmh;

    /**
     * Precisión de la señal GPS en metros
     */
    @Column(name = "precision")
    private Double precision;

    /**
     * Timestamp del dispositivo GPS
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Proveedor de localización (GPS, NETWORK, FUSED)
     */
    @Column(length = 20)
    private String provider;

    /**
     * Timestamp de inserción en BD
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
