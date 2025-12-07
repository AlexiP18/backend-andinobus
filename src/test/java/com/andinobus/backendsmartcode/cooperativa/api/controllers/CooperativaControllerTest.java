package com.andinobus.backendsmartcode.cooperativa.api.controllers;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.cooperativa.api.dto.CooperativaDtos;
import com.andinobus.backendsmartcode.cooperativa.application.services.CooperativaService;
import com.andinobus.backendsmartcode.operacion.domain.entities.AsignacionBusFrecuencia;
import com.andinobus.backendsmartcode.operacion.domain.entities.DiaParadaBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CooperativaControllerTest {

    private CooperativaService cooperativaService = mock(CooperativaService.class);
    private CooperativaController controller;

    @BeforeEach
    void setUp() {
        controller = new CooperativaController(cooperativaService);
    }

    @Test
    void obtenerBuses_mapsToDto() {
        Cooperativa coop = new Cooperativa();
        coop.setId(1L);

        Bus bus = Bus.builder()
                .id(100L)
                .cooperativa(coop)
                .numeroInterno("INT-1")
                .placa("ABC-123")
                .chasisMarca("MARC")
                .carroceriaMarca("CAR")
                .capacidadAsientos(45)
                .estado("DISPONIBLE")
                .activo(true)
                .fotoUrl("/img.png")
                .build();

        when(cooperativaService.obtenerBusesCooperativa(1L)).thenReturn(List.of(bus));

        var resp = controller.obtenerBuses(1L);
        assertEquals(200, resp.getStatusCodeValue());
        var body = resp.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        CooperativaDtos.BusDto dto = body.get(0);
        assertEquals(100L, dto.getId());
        assertEquals("ABC-123", dto.getPlaca());
        verify(cooperativaService).obtenerBusesCooperativa(1L);
    }

    @Test
    void obtenerBusesDisponibles_mapsToDto() {
        Cooperativa coop = new Cooperativa(); coop.setId(2L);
        Bus bus = Bus.builder().id(200L).cooperativa(coop).estado("DISPONIBLE").build();
        LocalDate fecha = LocalDate.now();
        when(cooperativaService.obtenerBusesDisponibles(2L, fecha)).thenReturn(List.of(bus));

        var resp = controller.obtenerBusesDisponibles(2L, fecha);
        assertEquals(200, resp.getStatusCodeValue());
        var body = resp.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(200L, body.get(0).getId());
        verify(cooperativaService).obtenerBusesDisponibles(2L, fecha);
    }

    @Test
    void asignarBus_delegatesAndReturnsDto() {
        Cooperativa coop = new Cooperativa(); coop.setId(3L);
        Bus bus = Bus.builder().id(300L).cooperativa(coop).build();

        AsignacionBusFrecuencia asign = AsignacionBusFrecuencia.builder()
                .id(10L)
                .bus(bus)
                .fechaInicio(LocalDate.now())
                .estado("ACTIVA")
                .observaciones("obs")
                .build();

        CooperativaDtos.AsignarBusRequest req = new CooperativaDtos.AsignarBusRequest();
        req.setBusId(300L);
        req.setFrecuenciaId(55L);
        req.setFechaInicio(LocalDate.now());
        req.setFechaFin(null);
        req.setObservaciones("obs");

        when(cooperativaService.asignarBusAFrecuencia(300L, 55L, req.getFechaInicio(), req.getFechaFin(), "obs"))
                .thenReturn(asign);

        var resp = controller.asignarBus(req);
        assertEquals(200, resp.getStatusCodeValue());
        var body = resp.getBody();
        assertNotNull(body);
        assertEquals(10L, body.getId());
        assertEquals(300L, body.getBus().getId());
        verify(cooperativaService).asignarBusAFrecuencia(300L, 55L, req.getFechaInicio(), req.getFechaFin(), "obs");
    }

    @Test
    void finalizarAsignacion_callsService() {
        controller.finalizarAsignacion(77L);
        verify(cooperativaService).finalizarAsignacion(77L);
    }

    @Test
    void obtenerAsignacionesActivas_mapsToDto() {
        Cooperativa coop = new Cooperativa(); coop.setId(4L);
        Bus bus = Bus.builder().id(400L).cooperativa(coop).build();
        AsignacionBusFrecuencia a = AsignacionBusFrecuencia.builder().id(20L).bus(bus).estado("ACTIVA").build();
        when(cooperativaService.obtenerAsignacionesActivas(4L)).thenReturn(List.of(a));

        var resp = controller.obtenerAsignacionesActivas(4L);
        assertEquals(200, resp.getStatusCodeValue());
        var body = resp.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(20L, body.get(0).getId());
        verify(cooperativaService).obtenerAsignacionesActivas(4L);
    }

    @Test
    void registrarDiaParada_delegatesAndReturnsDto() {
        Cooperativa coop = new Cooperativa(); coop.setId(5L);
        Bus bus = Bus.builder().id(500L).cooperativa(coop).build();
        DiaParadaBus d = DiaParadaBus.builder().id(30L).bus(bus).fecha(LocalDate.now()).motivo("MANTENIMIENTO").observaciones("obs").build();

        CooperativaDtos.RegistrarDiaParadaRequest req = new CooperativaDtos.RegistrarDiaParadaRequest();
        req.setBusId(500L);
        req.setFecha(d.getFecha());
        req.setMotivo(d.getMotivo());
        req.setObservaciones(d.getObservaciones());

        when(cooperativaService.registrarDiaParada(500L, d.getFecha(), d.getMotivo(), d.getObservaciones())).thenReturn(d);

        var resp = controller.registrarDiaParada(req);
        assertEquals(200, resp.getStatusCodeValue());
        var body = resp.getBody();
        assertNotNull(body);
        assertEquals(30L, body.getId());
        verify(cooperativaService).registrarDiaParada(500L, d.getFecha(), d.getMotivo(), d.getObservaciones());
    }

    @Test
    void obtenerDiasParada_mapsToDto() {
        Cooperativa coop = new Cooperativa(); coop.setId(6L);
        Bus bus = Bus.builder().id(600L).cooperativa(coop).build();
        DiaParadaBus d = DiaParadaBus.builder().id(40L).bus(bus).fecha(LocalDate.now()).motivo("OTRO").build();
        LocalDate inicio = LocalDate.now().minusDays(1);
        LocalDate fin = LocalDate.now().plusDays(1);
        when(cooperativaService.obtenerDiasParada(6L, inicio, fin)).thenReturn(List.of(d));

        var resp = controller.obtenerDiasParada(6L, inicio, fin);
        assertEquals(200, resp.getStatusCodeValue());
        var body = resp.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(40L, body.get(0).getId());
        verify(cooperativaService).obtenerDiasParada(6L, inicio, fin);
    }

    @Test
    void obtenerResumenDisponibilidad_calculatesAndReturns() {
        Cooperativa coop = new Cooperativa(); coop.setId(7L);
        Bus b1 = Bus.builder().id(701L).cooperativa(coop).estado("DISPONIBLE").build();
        Bus b2 = Bus.builder().id(702L).cooperativa(coop).estado("EN_SERVICIO").build();
        Bus b3 = Bus.builder().id(703L).cooperativa(coop).estado("MANTENIMIENTO").build();
        when(cooperativaService.obtenerBusesCooperativa(7L)).thenReturn(List.of(b1,b2,b3));
        when(cooperativaService.calcularExcesoBuses(7L, LocalDate.now())).thenReturn(5);

        var resp = controller.obtenerResumenDisponibilidad(7L, LocalDate.now());
        assertEquals(200, resp.getStatusCodeValue());
        var body = resp.getBody();
        assertNotNull(body);
        assertEquals(3, body.getTotalBuses());
        assertEquals(1, body.getBusesDisponibles());
        assertEquals(1, body.getBusesEnServicio());
        assertEquals(1, body.getBusesMantenimiento());
        assertEquals(0, body.getBusesParada());
        assertEquals(5, body.getExcesoBuses());
        verify(cooperativaService).obtenerBusesCooperativa(7L);
        verify(cooperativaService).calcularExcesoBuses(7L, LocalDate.now());
    }
}
