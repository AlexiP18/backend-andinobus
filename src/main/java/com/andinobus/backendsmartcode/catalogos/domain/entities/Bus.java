package com.andinobus.backendsmartcode.catalogos.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bus")
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cooperativa_id", nullable = false)
    private com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa cooperativa;

    @Column(name = "numero_interno", length = 50)
    private String numeroInterno;

    @Column(nullable = false, length = 20, unique = true)
    private String placa;

    @Column(name = "chasis_marca", length = 100)
    private String chasisMarca;

    @Column(name = "carroceria_marca", length = 100)
    private String carroceriaMarca;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(name = "foto_filename", length = 255)
    private String fotoFilename;

    @Column(name = "capacidad_asientos")
    @Builder.Default
    private Integer capacidadAsientos = 40; // Capacidad total de asientos del bus

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String estado = "DISPONIBLE"; // DISPONIBLE | EN_SERVICIO | MANTENIMIENTO | PARADA

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (activo == null) activo = true;
        if (estado == null) estado = "DISPONIBLE";
        if (capacidadAsientos == null) capacidadAsientos = 40;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
