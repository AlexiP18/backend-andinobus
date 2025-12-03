package com.andinobus.backendsmartcode.cooperativa.domain.entities;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Plantilla de rotación de frecuencias para una cooperativa.
 * Define el patrón de turnos que los buses seguirán en rotación.
 */
@Entity
@Table(name = "plantilla_rotacion_frecuencias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantillaRotacionFrecuencias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cooperativa_id", nullable = false)
    private Cooperativa cooperativa;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    /**
     * Total de turnos/días en el ciclo de rotación (ej: 36 días)
     */
    @Column(name = "total_turnos", nullable = false)
    private Integer totalTurnos;

    /**
     * JSON con la definición de todos los turnos
     * Estructura: [{ numeroDia, horaSalida, origen, destino, horaLlegada, esParada, subTurnos }]
     */
    @Column(name = "turnos_json", columnDefinition = "TEXT")
    private String turnosJson;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
