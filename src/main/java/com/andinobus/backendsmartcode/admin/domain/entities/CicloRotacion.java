package com.andinobus.backendsmartcode.admin.domain.entities;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Define un ciclo de rotación de buses para una cooperativa.
 * 
 * Un ciclo de rotación establece cómo los buses rotan a través de diferentes
 * rutas y frecuencias en un período determinado (ej: 23 días).
 * 
 * Basado en los CSV de "Horas de Trabajo RUTAS" donde se ve que los buses
 * (identificados por números como 67, 77, 79, etc.) rotan a través de
 * diferentes días y rutas.
 */
@Entity
@Table(name = "ciclo_rotacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CicloRotacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cooperativa_id", nullable = false)
    private Cooperativa cooperativa;

    /**
     * Nombre identificativo del ciclo
     * Ej: "Rotación Principal", "Rutas Interprovinciales"
     */
    @Column(nullable = false, length = 100)
    private String nombre;

    /**
     * Descripción detallada del ciclo
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Número de días que dura el ciclo completo antes de repetirse
     * Según los CSV, parece ser un ciclo de aproximadamente 23 días
     */
    @Column(name = "dias_ciclo", nullable = false)
    @Builder.Default
    private Integer diasCiclo = 23;

    /**
     * Si el ciclo está activo
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /**
     * Asignaciones de este ciclo
     */
    @OneToMany(mappedBy = "ciclo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("diaCiclo ASC, orden ASC")
    @Builder.Default
    private List<AsignacionRotacion> asignaciones = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Obtiene el día del ciclo para una fecha dada
     */
    public int getDiaCicloParaFecha(java.time.LocalDate fechaInicio, java.time.LocalDate fechaConsulta) {
        long diasDesdeInicio = java.time.temporal.ChronoUnit.DAYS.between(fechaInicio, fechaConsulta);
        return (int) ((diasDesdeInicio % diasCiclo) + 1);
    }
}
