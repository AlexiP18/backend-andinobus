package com.andinobus.backendsmartcode.planificacion;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlanificacionItem(
        String origen,
        String destino,
        LocalTime horaSalida,
        LocalTime horaLlegada,
        Integer rowNumber,
        String rawHoraSalida,
        String rawHoraLlegada,
        String error
) {
}