package com.andinobus.backendsmartcode.tracking.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para mostrar información de viajes activos en los dashboards
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViajeActivoDTO {
    private Long id;
    private Long viajeId;
    
    // Información del bus
    private String busPlaca;
    private Long busId;
    
    // Información de la cooperativa
    private String cooperativaNombre;
    private Long cooperativaId;
    
    // Información de la ruta
    private String rutaOrigen;
    private String rutaDestino;
    private String rutaNombre;
    
    // Información del chofer
    private String choferNombre;
    private String choferApellido;
    private Long choferId;
    
    // Información del viaje
    private String fechaSalida;
    private String horaSalida;
    private String horaLlegadaEstimada;
    private String estado; // PROGRAMADO, EN_CURSO, EN_TERMINAL, FINALIZADO
    
    // Información de capacidad
    private Integer numeroPasajeros;
    private Integer capacidadTotal;
    
    // Posición actual (si está disponible)
    private BigDecimal latitudActual;
    private BigDecimal longitudActual;
    private BigDecimal velocidadKmh;
    private LocalDateTime ultimaActualizacion;
    
    // Información adicional
    private LocalDateTime horaInicioReal;
    private LocalDateTime horaFinReal;

    // Coordenadas de terminales para mostrar la ruta en el mapa
    private BigDecimal terminalOrigenLatitud;
    private BigDecimal terminalOrigenLongitud;
    private BigDecimal terminalDestinoLatitud;
    private BigDecimal terminalDestinoLongitud;
    private String terminalOrigenNombre;
    private String terminalDestinoNombre;

    /**
     * Calcula el porcentaje de ocupación del bus
     */
    public Double getPorcentajeOcupacion() {
        if (capacidadTotal == null || capacidadTotal == 0) {
            return 0.0;
        }
        return (numeroPasajeros.doubleValue() / capacidadTotal.doubleValue()) * 100;
    }

    /**
     * Verifica si el viaje tiene posición GPS actual
     */
    public boolean tienePosicionActual() {
        return latitudActual != null && longitudActual != null;
    }

    /**
     * Verifica si tiene coordenadas de terminales para mostrar la ruta
     */
    public boolean tieneCoordenasRuta() {
        return terminalOrigenLatitud != null && terminalOrigenLongitud != null 
            && terminalDestinoLatitud != null && terminalDestinoLongitud != null;
    }

    /**
     * Obtiene el nombre completo del chofer
     */
    public String getChoferNombreCompleto() {
        if (choferNombre == null) return "Sin asignar";
        return choferNombre + (choferApellido != null ? " " + choferApellido : "");
    }
}
