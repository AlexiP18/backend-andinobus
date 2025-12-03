package com.andinobus.backendsmartcode.admin.domain.enums;

/**
 * Tipos de frecuencia según la distancia/alcance del viaje
 */
public enum TipoFrecuencia {
    /**
     * Viajes dentro de la misma provincia
     * Ejemplos: Ambato-Riobamba, Quito-Latacunga
     * Características: Menor duración, descansos cortos (45 min)
     */
    INTRAPROVINCIAL,

    /**
     * Viajes entre diferentes provincias
     * Ejemplos: Quito-Guayaquil, Quito-Loja, Cuenca-Machala
     * Características: Mayor duración, descansos largos (120 min)
     */
    INTERPROVINCIAL,

    /**
     * Viajes internacionales (si aplica en el futuro)
     */
    INTERNACIONAL
}
