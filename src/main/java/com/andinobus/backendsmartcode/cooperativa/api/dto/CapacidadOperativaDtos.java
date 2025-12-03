package com.andinobus.backendsmartcode.cooperativa.api.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * DTOs para el cálculo y gestión de capacidad operativa de la cooperativa.
 * Permite calcular cuántas frecuencias puede manejar la cooperativa según
 * sus recursos (buses y choferes) y sus restricciones.
 */
public class CapacidadOperativaDtos {

    /**
     * Respuesta principal con la capacidad operativa calculada
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapacidadOperativaResponse {
        private Long cooperativaId;
        private LocalDate fecha;

        // === RECURSOS DISPONIBLES ===
        private int totalBuses;
        private int busesActivos;
        private int totalChoferes;
        private int choferesActivos;

        // === HORAS DISPONIBLES POR DÍA ===
        private int horasBusDisponiblesDia;      // buses × 24
        private int horasChoferDisponiblesDia;   // choferes × 8 (o 10 si excepcional)
        private int horasOperativasRealesDia;    // mínimo de los anteriores (cuello de botella)

        // === CAPACIDAD DE FRECUENCIAS ===
        private int maxFrecuenciasPorDia;        // Máximo que puede ejecutar
        private int frecuenciasProgramadas;      // Actualmente programadas
        private int frecuenciasDisponibles;      // Cuántas más puede agregar

        // === ESTADO ===
        private boolean hayDeficit;              // Más frecuencias que capacidad
        private int deficitFrecuencias;          // Cuántas frecuencias sobran
        private double porcentajeUsoCapacidad;   // % de uso actual
        private String cuelloBotella;            // BUSES | CHOFERES | EQUILIBRADO

        // === CONFIGURACIÓN ===
        private ConfiguracionOperativa configuracion;

        // === ALERTAS Y RECOMENDACIONES ===
        private List<AlertaCapacidad> alertas;
        private List<String> sugerencias;

        // === PLANIFICACIÓN ===
        private int semanasPlanificacionDefecto;
        private int semanasPlanificacionMax;
    }

    /**
     * Alerta sobre capacidad operativa
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertaCapacidad {
        private String tipo;       // ERROR | WARNING | INFO
        private String codigo;     // Código interno
        private String titulo;     // Título breve
        private String mensaje;    // Mensaje descriptivo
        private int prioridad;     // 1=alta, 2=media, 3=baja
        private String accionSugerida;
    }

    /**
     * Configuración operativa resumida
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfiguracionOperativa {
        private int horasMaxChoferDia;
        private int horasMaxExcepcionales;
        private int diasExcepcionalesSemana;
        private int descansoInterprovincialMin;
        private int descansoIntraprovincialMin;
        private double umbralInterprovincialKm;
        private int tiempoPromedioFrecuenciaMin;
    }

    /**
     * Validación antes de generar frecuencias
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidacionGeneracionResponse {
        private boolean puedeGenerar;
        private int frecuenciasSolicitadas;
        private int frecuenciasMaxPosibles;
        private int frecuenciasQueSeCrearan;
        private int frecuenciasQueNoSeCrearan;
        
        private CapacidadOperativaResponse capacidadActual;
        
        private List<AlertaCapacidad> alertas;
        private List<String> advertencias;
        private String resumenCapacidad;
    }

    /**
     * Resumen de capacidad por semana
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapacidadSemanalResponse {
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private int semana;
        
        private int totalFrecuenciasPosibles;
        private int frecuenciasProgramadas;
        private int frecuenciasDisponibles;
        
        private List<CapacidadDiariaResumen> dias;
        private List<AlertaCapacidad> alertas;
    }

    /**
     * Resumen diario de capacidad
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapacidadDiariaResumen {
        private LocalDate fecha;
        private String diaSemana;
        private int maxFrecuencias;
        private int frecuenciasProgramadas;
        private int disponibles;
        private double porcentajeUso;
        private boolean hayDeficit;
        private String estadoIcono; // ✅ ⚠️ ❌
    }

    /**
     * Request para calcular capacidad de un período
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalcularCapacidadRequest {
        private Long cooperativaId;
        private LocalDate fechaInicio;
        private int semanas;
        private Integer duracionPromedioViajeMin;
        private Boolean incluirInterprovinciales;
    }

    /**
     * Resumen ejecutivo para mostrar en el modal de generación
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenCapacidadModal {
        // Recursos
        private int buses;
        private int choferes;
        private String ratioChoferBus; // ej: "1.0" o "2.5"
        
        // Capacidad diaria
        private int frecuenciasMaxDia;
        private int frecuenciasActualesDia;
        private int frecuenciasDisponiblesDia;
        
        // Para el período seleccionado
        private int semanasSeleccionadas;
        private int frecuenciasTotalesGenerables;
        private int frecuenciasQueSeGeneraran;
        
        // Estado
        private String estadoCapacidad; // OPTIMO | LIMITADO | DEFICIT
        private String mensajeEstado;
        private String colorEstado; // green | yellow | red
        
        // Cuello de botella
        private boolean cuelloBotellaBuses;
        private boolean cuelloBotellaCuósteres;
        private String recomendacion;
    }
}
