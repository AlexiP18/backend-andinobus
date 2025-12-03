package com.andinobus.backendsmartcode.admin.domain.entities;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Terminal;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ruta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String origen;

    @Column(nullable = false, length = 100)
    private String destino;

    /**
     * Terminal de origen de la ruta
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_origen_id")
    private Terminal terminalOrigen;

    /**
     * Terminal de destino de la ruta
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_destino_id")
    private Terminal terminalDestino;

    @Column(name = "distancia_km")
    private Double distanciaKm;

    @Column(name = "duracion_estimada_minutos")
    private Integer duracionEstimadaMinutos;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // Información de aprobación ANT
    @Column(name = "aprobada_ant", nullable = false)
    @Builder.Default
    private Boolean aprobadaAnt = false;

    @Column(name = "numero_resolucion_ant", length = 100)
    private String numeroResolucionAnt;

    @Column(name = "fecha_aprobacion_ant")
    private LocalDate fechaAprobacionAnt;

    @Column(name = "vigencia_hasta")
    private LocalDate vigenciaHasta;

    @Column(name = "observaciones_ant", columnDefinition = "TEXT")
    private String observacionesAnt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /**
     * Tipo de ruta: INTERPROVINCIAL o INTRAPROVINCIAL
     */
    @Column(name = "tipo_ruta", length = 20)
    @Builder.Default
    private String tipoRuta = "INTERPROVINCIAL";

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
