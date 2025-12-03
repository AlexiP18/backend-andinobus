package com.andinobus.backendsmartcode.admin.domain.entities;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Terminal;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad para manejar la rotación y disponibilidad de buses.
 * 
 * Esta entidad registra cuándo un bus estará disponible en una terminal específica,
 * permitiendo planificar frecuencias de manera inteligente considerando:
 * - La llegada del bus a una terminal
 * - El tiempo de descanso requerido
 * - La siguiente frecuencia asignable
 */
@Entity
@Table(name = "disponibilidad_bus", indexes = {
    @Index(name = "idx_disponibilidad_bus_fecha", columnList = "bus_id, fecha"),
    @Index(name = "idx_disponibilidad_terminal_fecha", columnList = "terminal_id, fecha, hora_disponible")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadBus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cooperativa_id", nullable = false)
    private Cooperativa cooperativa;

    /**
     * Terminal donde el bus estará disponible
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "terminal_id", nullable = false)
    private Terminal terminal;

    /**
     * Fecha de disponibilidad
     */
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    /**
     * Hora de llegada del bus a la terminal
     */
    @Column(name = "hora_llegada")
    private LocalTime horaLlegada;

    /**
     * Hora en que el bus estará disponible para siguiente frecuencia
     * (hora_llegada + tiempo_descanso)
     */
    @Column(name = "hora_disponible", nullable = false)
    private LocalTime horaDisponible;

    /**
     * Frecuencia que originó esta disponibilidad (el viaje que trajo el bus aquí)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frecuencia_origen_id")
    private FrecuenciaViaje frecuenciaOrigen;

    /**
     * Frecuencia asignada para la siguiente salida (null si aún no asignada)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frecuencia_siguiente_id")
    private FrecuenciaViaje frecuenciaSiguiente;

    /**
     * Estado de la disponibilidad:
     * - PENDIENTE: El bus aún no ha llegado
     * - DISPONIBLE: El bus está listo para asignar
     * - ASIGNADO: El bus ya tiene siguiente frecuencia asignada
     * - EN_DESCANSO: El bus está en período de descanso
     * - EN_MANTENIMIENTO: El bus está en mantenimiento
     */
    @Column(name = "estado", length = 32, nullable = false)
    @Builder.Default
    private String estado = "PENDIENTE";

    /**
     * Tiempo de descanso aplicado en minutos
     */
    @Column(name = "tiempo_descanso_minutos")
    @Builder.Default
    private Integer tiempoDescansoMinutos = 45;

    /**
     * Indica si el bus está en rotación (día libre programado)
     */
    @Column(name = "en_rotacion")
    @Builder.Default
    private Boolean enRotacion = false;

    /**
     * Número de día en el ciclo de rotación (para matching con CSV)
     */
    @Column(name = "dia_rotacion")
    private Integer diaRotacion;

    /**
     * Observaciones adicionales
     */
    @Column(columnDefinition = "TEXT")
    private String observaciones;

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
