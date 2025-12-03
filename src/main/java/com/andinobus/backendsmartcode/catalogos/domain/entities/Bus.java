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

    @Column(name = "tiene_dos_niveles")
    @Builder.Default
    private Boolean tieneDosNiveles = false; // Indica si el bus tiene dos pisos

    @Column(name = "capacidad_piso_1")
    private Integer capacidadPiso1; // Capacidad de asientos en el piso 1

    @Column(name = "capacidad_piso_2")
    private Integer capacidadPiso2; // Capacidad de asientos en el piso 2

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String estado = "DISPONIBLE"; // DISPONIBLE | EN_SERVICIO | MANTENIMIENTO | PARADA

    /**
     * Terminal base donde normalmente inicia operaciones el bus
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_base_id")
    private Terminal terminalBase;

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
        if (tieneDosNiveles == null) tieneDosNiveles = false;
        if (capacidadAsientos == null) capacidadAsientos = 40;
        
        // Si tiene dos niveles, validar que las capacidades de cada piso estén definidas
        if (tieneDosNiveles && (capacidadPiso1 == null || capacidadPiso2 == null)) {
            capacidadPiso1 = capacidadAsientos / 2;
            capacidadPiso2 = capacidadAsientos - capacidadPiso1;
        }
        
        // Si no tiene dos niveles, toda la capacidad es del piso 1
        if (!tieneDosNiveles) {
            capacidadPiso1 = capacidadAsientos;
            capacidadPiso2 = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
