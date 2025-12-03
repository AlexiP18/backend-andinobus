package com.andinobus.backendsmartcode.catalogos.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un Terminal Terrestre.
 * Las terminales son los puntos físicos donde los buses pueden:
 * - Iniciar un viaje (origen)
 * - Finalizar un viaje (destino)
 * - Realizar paradas intermedias para embarque/desembarque
 */
@Entity
@Table(name = "terminal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Terminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String provincia;

    @Column(nullable = false, length = 100)
    private String canton;

    /**
     * Tipología del terminal según capacidad e infraestructura:
     * T1 - Terminal básico/satélite (ej: Pujilí, Satélite La Concordia)
     * T2 - Terminal pequeño (ej: Paute, Sigsig, Chambo)
     * T3 - Terminal mediano (ej: Riobamba, Latacunga, Ibarra)
     * T4 - Terminal grande (ej: Cañar, Binacional Santa Rosa)
     * T5 - Terminal principal/hub (ej: Cuenca, Machala, Quito, Guayaquil)
     */
    @Column(nullable = false, length = 2)
    private String tipologia;

    /**
     * Número de andenes disponibles para embarque/desembarque
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer andenes = 1;

    /**
     * Frecuencias máximas por andén (por defecto 96 = 1 cada 15 min × 24 horas)
     */
    @Column(name = "frecuencias_por_anden", nullable = false)
    @Builder.Default
    private Integer frecuenciasPorAnden = 96;

    /**
     * Máximo de frecuencias diarias permitidas (andenes × frecuenciasPorAnden)
     */
    @Column(name = "max_frecuencias_diarias", nullable = false)
    @Builder.Default
    private Integer maxFrecuenciasDiarias = 96;

    /**
     * Coordenadas GPS para tracking
     */
    @Column(precision = 10)
    private Double latitud;

    @Column(precision = 10)
    private Double longitud;

    /**
     * Dirección física del terminal
     */
    @Column(length = 300)
    private String direccion;

    /**
     * Teléfono de contacto
     */
    @Column(length = 20)
    private String telefono;

    /**
     * Horario de operación
     */
    @Column(name = "horario_apertura", length = 5)
    private String horarioApertura; // "05:00"

    @Column(name = "horario_cierre", length = 5)
    private String horarioCierre; // "23:00"

    /**
     * URL de la imagen del terminal
     * Puede ser una URL externa o un base64 para imágenes pequeñas
     */
    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private String imagenUrl;

    /**
     * Estado del terminal
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (activo == null) activo = true;
        if (andenes == null) andenes = 1;
        if (frecuenciasPorAnden == null) frecuenciasPorAnden = 96;
        // Calcular max frecuencias
        calcularMaxFrecuencias();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calcularMaxFrecuencias();
    }

    /**
     * Calcula el máximo de frecuencias basado en andenes y frecuencias por andén
     */
    public void calcularMaxFrecuencias() {
        this.maxFrecuenciasDiarias = (this.andenes != null ? this.andenes : 1) * 
                                      (this.frecuenciasPorAnden != null ? this.frecuenciasPorAnden : 96);
    }

    /**
     * Obtiene la descripción de la tipología
     */
    public String getDescripcionTipologia() {
        return switch (this.tipologia) {
            case "T1" -> "Terminal básico/satélite";
            case "T2" -> "Terminal pequeño";
            case "T3" -> "Terminal mediano";
            case "T4" -> "Terminal grande";
            case "T5" -> "Terminal principal/hub";
            default -> "Sin clasificar";
        };
    }

    /**
     * Verifica si el terminal puede recibir más frecuencias
     */
    public boolean puedeRecibirFrecuencias(int frecuenciasActuales) {
        return frecuenciasActuales < this.maxFrecuenciasDiarias;
    }

    /**
     * Obtiene el porcentaje de ocupación
     */
    public double getPorcentajeOcupacion(int frecuenciasActuales) {
        if (this.maxFrecuenciasDiarias == 0) return 0;
        return (double) frecuenciasActuales / this.maxFrecuenciasDiarias * 100;
    }
}
