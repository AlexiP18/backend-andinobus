package com.andinobus.backendsmartcode.catalogos.domain.entities;

import com.andinobus.backendsmartcode.cooperativa.domain.entities.UsuarioCooperativa;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bus_chofer")
public class BusChofer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chofer_id", nullable = false)
    private UsuarioCooperativa chofer;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String tipo = "ALTERNO"; // PRINCIPAL, ALTERNO

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 1; // 1, 2, 3

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
        if (tipo == null) tipo = "ALTERNO";
        if (orden == null) orden = 1;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
