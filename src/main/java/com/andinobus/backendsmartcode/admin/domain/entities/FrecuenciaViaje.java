package com.andinobus.backendsmartcode.admin.domain.entities;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
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

    @Column(columnDefinition = "TEXT")
    private String observaciones;

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
