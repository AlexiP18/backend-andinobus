package com.andinobus.backendsmartcode.operacion.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "calificaciones_viaje")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalificacionViaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    @Column(name = "cliente_email", nullable = false)
    private String clienteEmail;

    @Column(name = "puntuacion", nullable = false)
    private Integer puntuacion; // 1 a 5 estrellas

    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "fecha_calificacion", nullable = false)
    private LocalDateTime fechaCalificacion;

    @Builder.Default
    @Column(name = "activa", nullable = false)
    private Boolean activa = true;

    @PrePersist
    protected void onCreate() {
        fechaCalificacion = LocalDateTime.now();
        if (activa == null) {
            activa = true;
        }
    }
}
