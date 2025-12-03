package com.andinobus.backendsmartcode.catalogos.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bus_operacion_diaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusOperacionDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "horas_operacion")
    @Builder.Default
    private Double horasOperacion = 0.0;

    @Column(name = "frecuencias_realizadas")
    @Builder.Default
    private Integer frecuenciasRealizadas = 0;

    @Column(name = "km_recorridos")
    @Builder.Default
    private Double kmRecorridos = 0.0;

    @Column(name = "estado_bus", length = 32)
    @Builder.Default
    private String estadoBus = "DISPONIBLE"; // DISPONIBLE, EN_RUTA, EN_MANTENIMIENTO, PARADA

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Agrega operación al registro del día
     */
    public void agregarOperacion(double horas, double km) {
        this.horasOperacion += horas;
        this.kmRecorridos += km;
        this.frecuenciasRealizadas++;
    }

    /**
     * Verifica si el bus puede operar más horas
     */
    public boolean puedeOperar(double horasAdicionales, int maxHorasDiarias) {
        return (this.horasOperacion + horasAdicionales) <= maxHorasDiarias;
    }

    /**
     * Calcula horas disponibles para el bus
     */
    public double horasDisponibles(int maxHorasDiarias) {
        return Math.max(0, maxHorasDiarias - this.horasOperacion);
    }
}
