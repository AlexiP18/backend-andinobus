package com.andinobus.backendsmartcode.cooperativa.domain.enums;

/**
 * Roles específicos para usuarios dentro del entorno COOPERATIVA
 */
public enum RolCooperativa {
    /**
     * Administrador de la cooperativa
     * Tiene acceso completo a gestión de buses, frecuencias, personal, reportes y configuración
     */
    ADMIN,
    
    /**
     * Oficinista/Vendedor de boletos
     * Puede vender boletos presencialmente, ver ventas, monitorear buses y pasajeros
     */
    OFICINISTA,
    
    /**
     * Chofer/Conductor
     * Puede ver su viaje asignado, lista de pasajeros, notificar salida/llegada
     */
    CHOFER
}
