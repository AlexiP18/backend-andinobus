package com.andinobus.backendsmartcode.admin.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "parada_frecuencia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParadaFrecuencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "frecuencia_viaje_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private FrecuenciaViaje frecuenciaViaje;

    @Column(nullable = false)
    private Integer orden; // Orden de la parada en el trayecto (1, 2, 3...)

    @Column(nullable = false, length = 200)
    private String nombreParada;

    @Column(length = 500)
    private String direccion;

    @Column(name = "tiempo_llegada")
    private LocalTime tiempoLlegada; // Hora estimada de llegada a esta parada

    @Column(name = "tiempo_espera_minutos")
    @Builder.Default
    private Integer tiempoEsperaMinutos = 5; // Tiempo de espera en esta parada

    @Column(name = "precio_desde_origen")
    private Double precioDesdeOrigen; // Precio del boleto desde origen hasta esta parada

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "permite_abordaje", nullable = false)
    @Builder.Default
    private Boolean permiteAbordaje = true; // Si los pasajeros pueden subir en esta parada

    @Column(name = "permite_descenso", nullable = false)
    @Builder.Default
    private Boolean permiteDescenso = true; // Si los pasajeros pueden bajar en esta parada

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
