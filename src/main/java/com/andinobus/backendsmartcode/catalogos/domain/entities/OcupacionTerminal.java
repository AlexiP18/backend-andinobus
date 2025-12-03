package com.andinobus.backendsmartcode.catalogos.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad para tracking de ocupación de frecuencias por terminal y hora.
 * Permite validar que no se exceda la capacidad máxima del terminal.
 */
@Entity
@Table(name = "ocupacion_terminal", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"terminal_id", "fecha", "hora"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcupacionTerminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "terminal_id", nullable = false)
    private Terminal terminal;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(name = "frecuencias_asignadas", nullable = false)
    @Builder.Default
    private Integer frecuenciasAsignadas = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Incrementa el contador de frecuencias asignadas
     */
    public void incrementarFrecuencias() {
        this.frecuenciasAsignadas++;
    }

    /**
     * Decrementa el contador de frecuencias asignadas
     */
    public void decrementarFrecuencias() {
        if (this.frecuenciasAsignadas > 0) {
            this.frecuenciasAsignadas--;
        }
    }

    /**
     * Verifica si se puede agregar otra frecuencia basándose en la capacidad del terminal
     */
    public boolean puedeAgregarFrecuencia(int maxFrecuenciasPorHora) {
        return this.frecuenciasAsignadas < maxFrecuenciasPorHora;
    }
}
