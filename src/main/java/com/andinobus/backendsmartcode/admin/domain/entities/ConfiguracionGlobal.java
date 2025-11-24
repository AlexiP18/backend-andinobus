package com.andinobus.backendsmartcode.admin.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_global")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionGlobal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Branding
    @Column(name = "nombre_aplicacion", length = 100)
    @Builder.Default
    private String nombreAplicacion = "AndinoBus";

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "logo_small_url", length = 500)
    private String logoSmallUrl;

    @Column(name = "favicon_url", length = 500)
    private String faviconUrl;

    // Colores corporativos
    @Column(name = "color_primario", length = 7)
    @Builder.Default
    private String colorPrimario = "#1E40AF"; // blue-800

    @Column(name = "color_secundario", length = 7)
    @Builder.Default
    private String colorSecundario = "#3B82F6"; // blue-600

    @Column(name = "color_acento", length = 7)
    @Builder.Default
    private String colorAcento = "#10B981"; // green-500

    // Redes Sociales
    @Column(name = "facebook_url", length = 200)
    private String facebookUrl;

    @Column(name = "twitter_url", length = 200)
    private String twitterUrl;

    @Column(name = "instagram_url", length = 200)
    private String instagramUrl;

    @Column(name = "youtube_url", length = 200)
    private String youtubeUrl;

    @Column(name = "linkedin_url", length = 200)
    private String linkedinUrl;

    // Contacto y Soporte
    @Column(name = "email_soporte", length = 100)
    private String emailSoporte;

    @Column(name = "telefono_soporte", length = 20)
    private String telefonoSoporte;

    @Column(name = "whatsapp_soporte", length = 20)
    private String whatsappSoporte;

    @Column(name = "direccion_fisica", length = 300)
    private String direccionFisica;

    @Column(name = "horario_atencion", length = 200)
    private String horarioAtencion;

    // Información adicional
    @Column(name = "sitio_web", length = 200)
    private String sitioWeb;

    @Column(name = "terminos_condiciones_url", length = 500)
    private String terminosCondicionesUrl;

    @Column(name = "politica_privacidad_url", length = 500)
    private String politicaPrivacidadUrl;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
