package com.andinobus.backendsmartcode.catalogos.domain;

import com.andinobus.backendsmartcode.admin.domain.entities.Ruta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa un camino físico específico de una ruta.
 * Una ruta puede tener múltiples caminos (ej: vía rápida, vía turística).
 */
@Entity
@Table(name = "camino")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Camino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @Column(nullable = false, length = 100)
    private String nombre; // "Vía Rápida", "Vía Panorámica", etc.

    @Column(name = "distancia_km")
    private Double distanciaKm;

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCamino tipo;

    /**
     * Polyline codificado (formato Google Maps Encoding)
     * Representa el trayecto GPS completo del camino
     */
    @Column(columnDefinition = "TEXT")
    private String polyline;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TipoCamino {
        RAPIDO,      // Vía más directa y rápida
        NORMAL,      // Vía estándar
        TURISTICO,   // Vía con paradas turísticas
        ECONOMICO    // Vía con más paradas urbanas
    }
}
