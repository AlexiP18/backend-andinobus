package com.andinobus.backendsmartcode.admin.domain.entities;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Terminal;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.UsuarioCooperativa;
import com.andinobus.backendsmartcode.admin.domain.enums.TipoFrecuencia;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "frecuencia_viaje")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FrecuenciaViaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cooperativa_id")
    private Cooperativa cooperativa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chofer_id")
    private UsuarioCooperativa chofer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camino_id")
    private com.andinobus.backendsmartcode.catalogos.domain.Camino camino;

    /**
     * Terminal de origen de la frecuencia
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_origen_id")
    private Terminal terminalOrigen;

    /**
     * Terminal de destino de la frecuencia
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_destino_id")
    private Terminal terminalDestino;

    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida;

    @Column(name = "hora_llegada_estimada")
    private LocalTime horaLlegadaEstimada;

    @Column(name = "dias_operacion", nullable = false, length = 100)
    @Builder.Default
    private String diasOperacion = "LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO"; // CSV de días

    @Column(name = "precio_base")
    private Double precioBase;

    @Column(name = "asientos_disponibles")
    private Integer asientosDisponibles;

    /**
     * Duración estimada del viaje en minutos
     */
    @Column(name = "duracion_estimada_minutos")
    private Integer duracionEstimadaMinutos;

    /**
     * Kilómetros de la ruta
     */
    @Column(name = "kilometros_ruta")
    private Double kilometrosRuta;

    /**
     * Costo de combustible estimado para el viaje
     */
    @Column(name = "costo_combustible_estimado")
    private Double costoCombustibleEstimado;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    /**
     * Tipo de frecuencia: INTERPROVINCIAL, INTRAPROVINCIAL
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_frecuencia", length = 32)
    @Builder.Default
    private TipoFrecuencia tipoFrecuencia = TipoFrecuencia.INTERPROVINCIAL;

    /**
     * Orden de la frecuencia en el día (para sistemas de rotación)
     */
    @Column(name = "orden_dia")
    private Integer ordenDia;

    /**
     * Si requiere que el bus esté físicamente en la terminal de origen
     */
    @Column(name = "requiere_bus_en_terminal")
    @Builder.Default
    private Boolean requiereBusEnTerminal = false;

    /**
     * Tiempo mínimo de espera antes de asignar otra frecuencia al mismo bus (minutos)
     */
    @Column(name = "tiempo_minimo_espera_minutos")
    @Builder.Default
    private Integer tiempoMinimoEsperaMinutos = 30;

    /**
     * Estado de la frecuencia: ACTIVA, PAUSADA, CANCELADA, EN_MANTENIMIENTO
     */
    @Column(length = 32)
    @Builder.Default
    private String estado = "ACTIVA";

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @OneToMany(mappedBy = "frecuenciaViaje", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<ParadaFrecuencia> paradas = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method para agregar paradas
    public void addParada(ParadaFrecuencia parada) {
        paradas.add(parada);
        parada.setFrecuenciaViaje(this);
    }

    public void removeParada(ParadaFrecuencia parada) {
        paradas.remove(parada);
        parada.setFrecuenciaViaje(null);
    }
}
