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
@Table(name = "cooperativa")
public class Cooperativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 13)
    private String ruc;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "color_primario", length = 7)
    @Builder.Default
    private String colorPrimario = "#16a34a";

    @Column(name = "color_secundario", length = 7)
    @Builder.Default
    private String colorSecundario = "#15803d";

    @Column(length = 255)
    private String facebook;

    @Column(length = 255)
    private String twitter;

    @Column(length = 255)
    private String instagram;

    @Column(length = 255)
    private String linkedin;

    @Column(length = 255)
    private String youtube;

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
        if (colorPrimario == null) colorPrimario = "#16a34a";
        if (colorSecundario == null) colorSecundario = "#15803d";
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
