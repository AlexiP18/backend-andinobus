package com.andinobus.backendsmartcode.cooperativa.domain.entities;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "frecuencia_config_cooperativa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FrecuenciaConfigCooperativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cooperativa_id", nullable = false, unique = true)
    private Cooperativa cooperativa;

    // Configuración de precios
    @Column(name = "precio_base_por_km")
    @Builder.Default
    private Double precioBasePorKm = 0.02; // Precio base por kilómetro

    @Column(name = "factor_diesel_por_km")
    @Builder.Default
    private Double factorDieselPorKm = 0.12; // Litros de diesel por km

    @Column(name = "precio_diesel")
    @Builder.Default
    private Double precioDiesel = 1.80; // Precio actual del diesel

    @Column(name = "margen_ganancia_porcentaje")
    @Builder.Default
    private Double margenGananciaPorcentaje = 30.0; // Porcentaje de ganancia sobre costo

    // Configuración de choferes
    @Column(name = "max_horas_diarias_chofer")
    @Builder.Default
    private Integer maxHorasDiariasChofer = 8;

    @Column(name = "max_horas_excepcionales")
    @Builder.Default
    private Integer maxHorasExcepcionales = 10;

    @Column(name = "max_dias_excepcionales_semana")
    @Builder.Default
    private Integer maxDiasExcepcionalesSemana = 2;

    @Column(name = "tiempo_descanso_entre_viajes_minutos")
    @Builder.Default
    private Integer tiempoDescansoEntreViajesMinutos = 30;

    // Descansos según tipo de viaje
    @Column(name = "descanso_interprovincial_minutos")
    @Builder.Default
    private Integer descansoInterprovincialMinutos = 120; // 2 horas para viajes largos

    @Column(name = "descanso_intraprovincial_minutos")
    @Builder.Default
    private Integer descansoIntraprovincialMinutos = 45; // 45 min para viajes cortos

    // Umbral de distancia para considerar interprovincial (en km)
    @Column(name = "umbral_interprovincial_km")
    @Builder.Default
    private Double umbralInterprovincialKm = 100.0;

    // Configuración de planificación
    @Column(name = "semanas_planificacion_defecto")
    @Builder.Default
    private Integer semanasPlanificacionDefecto = 1;

    @Column(name = "semanas_planificacion_max")
    @Builder.Default
    private Integer semanasPlanificacionMax = 4;

    // Configuración de buses
    @Column(name = "tiempo_minimo_parada_bus_minutos")
    @Builder.Default
    private Integer tiempoMinimoParadaBusMinutos = 15;

    @Column(name = "horas_operacion_max_bus")
    @Builder.Default
    private Integer horasOperacionMaxBus = 24;

    // Configuración de generación automática
    @Column(name = "intervalo_minimo_frecuencias_minutos")
    @Builder.Default
    private Integer intervaloMinimoFrecuenciasMinutos = 30;

    @Column(name = "hora_inicio_operacion")
    @Builder.Default
    private LocalTime horaInicioOperacion = LocalTime.of(5, 0);

    @Column(name = "hora_fin_operacion")
    @Builder.Default
    private LocalTime horaFinOperacion = LocalTime.of(23, 0);

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calcula el precio sugerido basado en kilómetros y configuración
     */
    public double calcularPrecioSugerido(double kilometros) {
        double costoCombustible = kilometros * factorDieselPorKm * precioDiesel;
        double costoBase = kilometros * precioBasePorKm;
        double costoTotal = costoCombustible + costoBase;
        return costoTotal * (1 + margenGananciaPorcentaje / 100);
    }

    /**
     * Verifica si un chofer puede trabajar más horas en un día
     */
    public boolean choferPuedeTrabajar(double horasYaTrabajadas, boolean esExcepcional) {
        int maxHoras = esExcepcional ? maxHorasExcepcionales : maxHorasDiariasChofer;
        return horasYaTrabajadas < maxHoras;
    }

    /**
     * Calcula horas restantes disponibles para un chofer
     */
    public double horasRestantesChofer(double horasYaTrabajadas, boolean esExcepcional) {
        int maxHoras = esExcepcional ? maxHorasExcepcionales : maxHorasDiariasChofer;
        return Math.max(0, maxHoras - horasYaTrabajadas);
    }

    /**
     * Obtiene el tiempo de descanso según la distancia del viaje
     */
    public int obtenerDescansoSegunDistancia(double distanciaKm) {
        if (distanciaKm >= umbralInterprovincialKm) {
            return descansoInterprovincialMinutos;
        }
        return descansoIntraprovincialMinutos;
    }

    /**
     * Determina si un viaje es interprovincial según la distancia
     */
    public boolean esViajeInterprovincial(double distanciaKm) {
        return distanciaKm >= umbralInterprovincialKm;
    }
}
