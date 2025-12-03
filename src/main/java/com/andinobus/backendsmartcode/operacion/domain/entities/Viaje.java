package com.andinobus.backendsmartcode.operacion.domain.entities;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Frecuencia;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.UsuarioCooperativa;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad que representa un viaje específico de un bus
 * con frecuencia, fecha y chofer asignados
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "viaje", indexes = {
    @Index(name = "idx_viaje_frecuencia_id", columnList = "frecuencia_id"),
    @Index(name = "idx_viaje_bus_id", columnList = "bus_id"),
    @Index(name = "idx_viaje_chofer_id", columnList = "chofer_id"),
    @Index(name = "idx_viaje_fecha", columnList = "fecha"),
    @Index(name = "idx_viaje_estado", columnList = "estado")
})
public class Viaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoja_ruta_id")
    private HojaRuta hojaRuta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frecuencia_id", nullable = false)
    private Frecuencia frecuencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chofer_id")
    private UsuarioCooperativa chofer;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida;

    @Column(name = "hora_salida_programada", nullable = false)
    private LocalTime horaSalidaProgramada;

    @Column(name = "hora_salida_real")
    private LocalTime horaSalidaReal;

    @Column(name = "hora_llegada_estimada")
    private LocalTime horaLlegadaEstimada;

    @Column(name = "hora_llegada_real")
    private LocalTime horaLlegadaReal;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "PROGRAMADO"; // PROGRAMADO | EN_TERMINAL | EN_RUTA | COMPLETADO | CANCELADO | EN_CURSO | FINALIZADO | CANCELADO

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    // ===================================
    // Campos de tracking GPS
    // ===================================

    @Column(name = "hora_inicio_real")
    private LocalDateTime horaInicioReal;

    @Column(name = "hora_fin_real")
    private LocalDateTime horaFinReal;

    @Column(name = "latitud_actual")
    private Double latitudActual;

    @Column(name = "longitud_actual")
    private Double longitudActual;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (estado == null) {
            estado = "PROGRAMADO";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===================================
    // Métodos de utilidad
    // ===================================

    public boolean isProgramado() {
        return "PROGRAMADO".equals(this.estado);
    }

    public boolean isEnTerminal() {
        return "EN_TERMINAL".equals(this.estado);
    }

    public boolean isEnRuta() {
        return "EN_RUTA".equals(this.estado);
    }

    public boolean isCompletado() {
        return "COMPLETADO".equals(this.estado);
    }

    public boolean isCancelado() {
        return "CANCELADO".equals(this.estado);
    }

    public boolean haIniciado() {
        return horaSalidaReal != null;
    }

    public boolean haFinalizado() {
        return horaLlegadaReal != null;
    }

    // ===================================
    // Métodos de tracking GPS
    // ===================================

    public boolean isEnCurso() {
        return "EN_CURSO".equals(this.estado) || "EN_RUTA".equals(this.estado);
    }

    public boolean isFinalizado() {
        return "FINALIZADO".equals(this.estado) || "COMPLETADO".equals(this.estado);
    }

    public void actualizarPosicion(Double latitud, Double longitud) {
        this.latitudActual = latitud;
        this.longitudActual = longitud;
        this.ultimaActualizacion = LocalDateTime.now();
    }

    public boolean tienePosicionActual() {
        return latitudActual != null && longitudActual != null;
    }
}
