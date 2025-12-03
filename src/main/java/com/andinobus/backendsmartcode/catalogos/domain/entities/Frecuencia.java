package com.andinobus.backendsmartcode.catalogos.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "frecuencia")
public class Frecuencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cooperativa_id", nullable = false)
    private Cooperativa cooperativa;

    @Column(nullable = false, length = 120)
    private String origen;

    @Column(nullable = false, length = 120)
    private String destino;

    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida;

    @Column(name = "duracion_estimada_min")
    private Integer duracionEstimadaMin;

    @Column(name = "dias_operacion", length = 100)
    private String diasOperacion;

    @Column(name = "activa", nullable = false)
    @Builder.Default
    private Boolean activa = true;
}
