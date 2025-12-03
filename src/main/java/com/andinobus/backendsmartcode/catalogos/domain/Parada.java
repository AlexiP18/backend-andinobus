package com.andinobus.backendsmartcode.catalogos.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa una parada específica en un camino.
 * Incluye coordenadas GPS para geolocalización.
 */
@Entity(name = "ParadaCamino")
@Table(name = "parada_camino")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Parada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camino_id", nullable = false)
    private Camino camino;

    @Column(nullable = false, length = 100)
    private String nombre; // "Terminal Quitumbe", "Peaje Machachi", etc.

    @Column(length = 255)
    private String direccion;

    /**
     * Coordenadas GPS de la parada
     */
    @Column(nullable = false)
    private Double latitud; // Rango: -90 a +90

    @Column(nullable = false)
    private Double longitud; // Rango: -180 a +180

    /**
     * Orden secuencial en el camino (1, 2, 3...)
     */
    @Column(nullable = false)
    private Integer orden;

    /**
     * Tiempo estimado desde el origen en minutos
     */
    @Column(name = "tiempo_estimado_minutos")
    private Integer tiempoEstimadoMinutos;

    /**
     * Define si se puede abordar el bus en esta parada
     */
    @Column(name = "permite_abordaje", nullable = false)
    private Boolean permiteAbordaje = true;

    /**
     * Define si se puede descender del bus en esta parada
     */
    @Column(name = "permite_descenso", nullable = false)
    private Boolean permiteDescenso = true;

    /**
     * Precio acumulado desde el origen hasta esta parada
     */
    @Column(name = "precio_desde_origen", precision = 10, scale = 2)
    private BigDecimal precioDesdeOrigen;

    @Column(nullable = false)
    private Boolean activa = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
