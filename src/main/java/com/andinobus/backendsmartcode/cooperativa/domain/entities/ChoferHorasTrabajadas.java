package com.andinobus.backendsmartcode.cooperativa.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "chofer_horas_trabajadas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoferHorasTrabajadas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chofer_id", nullable = false)
    private UsuarioCooperativa chofer;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "horas_trabajadas")
    @Builder.Default
    private Double horasTrabajadas = 0.0;

    @Column(name = "horas_excepcionales")
    @Builder.Default
    private Boolean horasExcepcionales = false;

    @Column(name = "frecuencias_realizadas")
    @Builder.Default
    private Integer frecuenciasRealizadas = 0;

    @Column(name = "km_recorridos")
    @Builder.Default
    private Double kmRecorridos = 0.0;

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
     * Agrega horas trabajadas al registro del día
     */
    public void agregarHoras(double horas, double km) {
        this.horasTrabajadas += horas;
        this.kmRecorridos += km;
        this.frecuenciasRealizadas++;
    }

    /**
     * Verifica si puede agregar más horas (basado en límite de 10 max)
     */
    public boolean puedeAgregarHoras(double horasAdicionales, int maxNormal, int maxExcepcional) {
        double horasResultantes = this.horasTrabajadas + horasAdicionales;
        if (horasResultantes <= maxNormal) {
            return true;
        }
        // Si excede el límite normal pero está dentro del excepcional
        return horasResultantes <= maxExcepcional;
    }
}
