package com.andinobus.backendsmartcode.admin.domain.entities;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Terminal;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Asignación de un bus a un día específico del ciclo de rotación.
 * 
 * Esta entidad representa una fila del CSV de "Horas de Trabajo RUTAS",
 * donde para cada día del ciclo se especifica qué bus opera qué ruta.
 * 
 * Ejemplo del CSV:
 * DIA | HORA_SALIDA | ORIGEN | DESTINO | ... | día-1 | día-2 | día-3 ...
 *  1  |    6:00     | AMBATO | QUITO   | ... |  67   |  77   |  79   ...
 * 
 * Los números (67, 77, 79) representan identificadores de buses.
 */
@Entity
@Table(name = "asignacion_rotacion", 
    uniqueConstraints = @UniqueConstraint(
        name = "uk_ciclo_dia_orden",
        columnNames = {"ciclo_id", "dia_ciclo", "bus_id", "orden"}
    ))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionRotacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ciclo_id", nullable = false)
    private CicloRotacion ciclo;

    /**
     * Día dentro del ciclo (1, 2, 3, ... hasta diasCiclo)
     */
    @Column(name = "dia_ciclo", nullable = false)
    private Integer diaCiclo;

    /**
     * Bus asignado para este día
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    /**
     * Frecuencia asociada (opcional, puede ser null si es día libre)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frecuencia_viaje_id")
    private FrecuenciaViaje frecuenciaViaje;

    /**
     * Orden de la salida dentro del día (un bus puede tener múltiples salidas)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 1;

    /**
     * Hora de salida programada
     */
    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    /**
     * Terminal de origen
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_origen_id")
    private Terminal terminalOrigen;

    /**
     * Terminal de destino
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_destino_id")
    private Terminal terminalDestino;

    /**
     * Observaciones (ej: "GUIAS", "PARADA" del CSV)
     */
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
