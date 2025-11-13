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
@Table(name = "parada")
public class Parada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frecuencia_id", nullable = false)
    private Frecuencia frecuencia;

    @Column(nullable = false, length = 120)
    private String ciudad;

    @Column(nullable = false)
    private Integer orden;

    @Column(name = "hora_estimada")
    private LocalTime horaEstimada;
}
