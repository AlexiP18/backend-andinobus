package com.andinobus.backendsmartcode.operacion.domain.entities;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad para gestionar notificaciones de viajes a las cooperativas
 * Se genera cuando un chofer inicia o finaliza un viaje
 */
@Entity
@Table(name = "notificacion_viaje", indexes = {
    @Index(name = "idx_notificacion_cooperativa_id", columnList = "cooperativa_id"),
    @Index(name = "idx_notificacion_viaje_id", columnList = "viaje_id"),
    @Index(name = "idx_notificacion_leida", columnList = "leida"),
    @Index(name = "idx_notificacion_fecha", columnList = "fecha_creacion")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionViaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cooperativa_id", nullable = false)
    private Cooperativa cooperativa;

    /**
     * Tipo de notificación: VIAJE_INICIADO, VIAJE_FINALIZADO, VIAJE_CANCELADO, ALERTA_RETRASO
     */
    @Column(nullable = false, length = 30)
    private String tipo;

    /**
     * Título de la notificación
     */
    @Column(nullable = false, length = 150)
    private String titulo;

    /**
     * Mensaje detallado de la notificación
     */
    @Column(columnDefinition = "TEXT")
    private String mensaje;

    /**
     * Información adicional del viaje (origen, destino, chofer, etc.)
     */
    @Column(name = "detalle_viaje", columnDefinition = "TEXT")
    private String detalleViaje;

    /**
     * Indica si la notificación ha sido leída
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean leida = false;

    /**
     * Fecha y hora de creación de la notificación
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora en que se leyó la notificación
     */
    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
