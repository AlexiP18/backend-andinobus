package com.andinobus.backendsmartcode.catalogos.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Representa una parada intermedia en una frecuencia
 * Ejemplo: En la ruta Quito-Loja, las paradas pueden ser: Latacunga, Riobamba, Cuenca
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "parada_intermedia")
public class ParadaIntermedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "frecuencia_id", nullable = false)
    private Frecuencia frecuencia;

    @Column(nullable = false, length = 120)
    private String ciudad;

    @Column(name = "orden_parada", nullable = false)
    private Integer ordenParada; // 1, 2, 3... para mantener el orden de las paradas

    @Column(name = "minutos_desde_origen", nullable = false)
    private Integer minutosDesdeOrigen; // Tiempo estimado desde el origen

    @Column(name = "precio_adicional", precision = 10, scale = 2)
    private BigDecimal precioAdicional; // Precio adicional si se sube/baja en esta parada

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (activo == null) activo = true;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
