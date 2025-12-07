package com.andinobus.backendsmartcode.ventas.api.controllers;

import com.andinobus.backendsmartcode.ventas.api.dto.VentasDtos;
import com.andinobus.backendsmartcode.ventas.application.services.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservasControllerTest {

    private ReservaService reservaService = mock(ReservaService.class);
    private ReservasController controller;

    @BeforeEach
    void setUp() {
        controller = new ReservasController(reservaService);
    }

    @Test
    void crearReserva_usesClienteEmailFromRequest() {
        VentasDtos.ReservaCreateRequest req = new VentasDtos.ReservaCreateRequest();
        req.setViajeId(10L);
        req.setClienteEmail("req@cliente.com");

        VentasDtos.ReservaResponse resp = VentasDtos.ReservaResponse.builder()
                .id(1L)
                .viajeId(10L)
                .estado("pendiente")
                .build();

        when(reservaService.crearReserva(req, "req@cliente.com")).thenReturn(resp);

        var response = controller.crearReserva(req);
        assertEquals(200, response.getStatusCodeValue());
        assertSame(resp, response.getBody());
        verify(reservaService).crearReserva(req, "req@cliente.com");
    }

    @Test
    void obtenerReserva_usesClienteEmailParamOrAuth() {
        VentasDtos.ReservaDetalleResponse resp = VentasDtos.ReservaDetalleResponse.builder()
                .id(2L)
                .cliente("c")
                .monto(BigDecimal.TEN)
                .build();

        when(reservaService.obtenerReserva(2L, "param@c.com")).thenReturn(resp);
        var r1 = controller.obtenerReserva(2L, "param@c.com", null);
        assertSame(resp, r1.getBody());
        verify(reservaService).obtenerReserva(2L, "param@c.com");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("auth@c.com");
        when(reservaService.obtenerReserva(3L, "auth@c.com")).thenReturn(resp);
        var r2 = controller.obtenerReserva(3L, null, auth);
        assertSame(resp, r2.getBody());
        verify(reservaService).obtenerReserva(3L, "auth@c.com");
    }

    @Test
    void listarMisReservas_usesEmailFallback() {
        VentasDtos.ReservaDetalleResponse d = VentasDtos.ReservaDetalleResponse.builder().id(5L).build();
        when(reservaService.listarReservasPorCliente("u@x.com")).thenReturn(List.of(d));

        var resp = controller.listarMisReservas("u@x.com", null);
        assertEquals(1, resp.getBody().size());
        verify(reservaService).listarReservasPorCliente("u@x.com");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("auth2@x.com");
        when(reservaService.listarReservasPorCliente("auth2@x.com")).thenReturn(List.of(d));
        var resp2 = controller.listarMisReservas(null, auth);
        assertEquals(1, resp2.getBody().size());
        verify(reservaService).listarReservasPorCliente("auth2@x.com");
    }

    @Test
    void cancelarReserva_callsServiceAndReturnsNoContent() {
        var resp = controller.cancelarReserva(7L, "c@x.com", null);
        assertEquals(204, resp.getStatusCodeValue());
        verify(reservaService).cancelarReserva(7L, "c@x.com");
    }

    @Test
    void obtenerAsientosDisponibles_returnsList() {
        VentasDtos.AsientoDisponibilidadDto a = VentasDtos.AsientoDisponibilidadDto.builder().numeroAsiento("1A").build();
        when(reservaService.obtenerAsientosDisponibles(11L)).thenReturn(List.of(a));

        var resp = controller.obtenerAsientosDisponibles(11L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(1, resp.getBody().size());
        verify(reservaService).obtenerAsientosDisponibles(11L);
    }

    @Test
    void obtenerAsientosDisponiblesPorFrecuencia_returnsResponse() {
        VentasDtos.AsientosViajeResponse r = VentasDtos.AsientosViajeResponse.builder().viajeId(22L).build();
        when(reservaService.obtenerAsientosDisponiblesPorFrecuencia(22L, "2025-12-07")).thenReturn(r);

        var resp = controller.obtenerAsientosDisponiblesPorFrecuencia(22L, "2025-12-07");
        assertEquals(200, resp.getStatusCodeValue());
        assertSame(r, resp.getBody());
        verify(reservaService).obtenerAsientosDisponiblesPorFrecuencia(22L, "2025-12-07");
    }

    @Test
    void obtenerReservasPorCooperativa_delegates() {
        VentasDtos.ReservaCooperativaDto dto = VentasDtos.ReservaCooperativaDto.builder().id(33L).build();
        when(reservaService.obtenerReservasPorCooperativa(9L, "pendiente")).thenReturn(List.of(dto));

        var resp = controller.obtenerReservasPorCooperativa(9L, "pendiente");
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(1, resp.getBody().size());
        verify(reservaService).obtenerReservasPorCooperativa(9L, "pendiente");
    }
}
