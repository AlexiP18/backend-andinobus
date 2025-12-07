package com.andinobus.backendsmartcode.operacion.api.controllers;

import com.andinobus.backendsmartcode.operacion.application.services.AsignacionFrecuenciaService;
import com.andinobus.backendsmartcode.operacion.application.services.AsignacionFrecuenciaService.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsignacionFrecuenciaControllerTest {

    @Mock
    private AsignacionFrecuenciaService asignacionService;

    @InjectMocks
    private AsignacionFrecuenciaController controller;

    @Test
    void validarChofer_delegatesToService_returnsBody() {
        Long choferId = 10L;
        LocalDate fecha = LocalDate.of(2025, 1, 15);
        int duracion = 120;

        ValidacionChoferResponse resp = ValidacionChoferResponse.builder()
                .choferId(choferId)
                .choferNombre("Juan Perez")
                .fecha(fecha)
                .horasTrabajadasHoy(2)
                .minutosTrabajadasHoy(120)
                .horasPropuestas(4)
                .limiteHorasHoy(8)
                .diasJornadaExtendidaSemana(0)
                .puedeAsignarse(true)
                .mensaje("OK")
                .build();

        when(asignacionService.validarAsignacionChofer(choferId, fecha, duracion)).thenReturn(resp);

        ResponseEntity<ValidacionChoferResponse> result = controller.validarChofer(choferId, fecha, duracion);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isEqualTo(resp);
        verify(asignacionService).validarAsignacionChofer(choferId, fecha, duracion);
    }

    @Test
    void validarTerminales_delegatesToService_returnsBody() {
        Long origenId = 1L;
        Long destinoId = 2L;
        LocalDate fecha = LocalDate.of(2025, 2, 3);
        LocalTime salida = LocalTime.of(8, 0);
        LocalTime llegada = LocalTime.of(10, 0);

        ValidacionTerminalResponse resp = ValidacionTerminalResponse.builder()
                .terminalOrigenId(origenId)
                .terminalOrigenNombre("Orig")
                .terminalDestinoId(destinoId)
                .terminalDestinoNombre("Dest")
                .fecha(fecha)
                .horaSalida(salida)
                .horaLlegada(llegada)
                .origenDisponible(true)
                .destinoDisponible(true)
                .puedeAsignarse(true)
                .mensaje("OK")
                .build();

        when(asignacionService.validarAsignacionTerminal(origenId, destinoId, fecha, salida, llegada)).thenReturn(resp);

        ResponseEntity<ValidacionTerminalResponse> result = controller.validarTerminales(origenId, destinoId, fecha, salida, llegada);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isEqualTo(resp);
        verify(asignacionService).validarAsignacionTerminal(origenId, destinoId, fecha, salida, llegada);
    }

    @Test
    void asignarFrecuencia_delegatesToService_returnsBody() {
        AsignacionRequest req = AsignacionRequest.builder()
                .choferId(5L)
                .frecuenciaViajeId(7L)
                .terminalOrigenId(1L)
                .terminalDestinoId(2L)
                .fecha(LocalDate.of(2025, 3, 5))
                .horaSalida(LocalTime.of(9, 0))
                .horaLlegada(LocalTime.of(11, 0))
                .duracionMinutos(120)
                .build();

        AsignacionResponse resp = AsignacionResponse.builder()
                .exitoso(true)
                .mensaje("Asignado")
                .validacionChofer(ValidacionChoferResponse.builder().choferId(req.getChoferId()).build())
                .validacionTerminal(ValidacionTerminalResponse.builder().terminalOrigenId(req.getTerminalOrigenId()).build())
                .build();

        when(asignacionService.asignarFrecuencia(req)).thenReturn(resp);

        ResponseEntity<AsignacionResponse> result = controller.asignarFrecuencia(req);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isEqualTo(resp);
        verify(asignacionService).asignarFrecuencia(req);
    }

    @Test
    void obtenerResumenHoras_withDate_passesDate() {
        Long choferId = 9L;
        LocalDate fecha = LocalDate.of(2025, 4, 1);

        ResumenHorasChoferResponse resp = ResumenHorasChoferResponse.builder()
                .choferId(choferId)
                .choferNombre("Chofer")
                .semanaInicio(fecha)
                .semanaFin(fecha)
                .totalHorasSemana(10)
                .totalMinutosSemana(600)
                .diasConJornadaExtendida(0)
                .diasRestantesJornadaExtendida(2)
                .build();

        when(asignacionService.obtenerResumenHorasSemana(choferId, fecha)).thenReturn(resp);

        ResponseEntity<ResumenHorasChoferResponse> result = controller.obtenerResumenHoras(choferId, fecha);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isEqualTo(resp);
        verify(asignacionService).obtenerResumenHorasSemana(choferId, fecha);
    }

    @Test
    void obtenerResumenHoras_withNullDate_usesNow() {
        Long choferId = 11L;

        ResumenHorasChoferResponse resp = ResumenHorasChoferResponse.builder()
                .choferId(choferId)
                .choferNombre("Chofer")
                .totalHorasSemana(0)
                .totalMinutosSemana(0)
                .diasConJornadaExtendida(0)
                .diasRestantesJornadaExtendida(2)
                .build();

        when(asignacionService.obtenerResumenHorasSemana(any(), any())).thenReturn(resp);

        ResponseEntity<ResumenHorasChoferResponse> result = controller.obtenerResumenHoras(choferId, null);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isEqualTo(resp);

        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(asignacionService).obtenerResumenHorasSemana(org.mockito.ArgumentMatchers.eq(choferId), captor.capture());
        assertThat(captor.getValue()).isNotNull();
    }
}
