package com.andinobus.backendsmartcode.ventas.api.controllers;

import com.andinobus.backendsmartcode.ventas.api.dto.VentasDtos;
import com.andinobus.backendsmartcode.ventas.application.services.PagoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PagosControllerTest {

    private PagoService pagoService = mock(PagoService.class);
    private PagosController controller;

    @BeforeEach
    void setUp() {
        controller = new PagosController(pagoService);
    }

    @Test
    void confirmarPago_usesClienteEmailWhenProvided() {
        VentasDtos.PagoConfirmacionRequest req = VentasDtos.PagoConfirmacionRequest.builder()
            .reservaId(1L)
            .metodoPago("EFECTIVO")
            .referencia(null)
            .build();

        VentasDtos.PagoResponse resp = VentasDtos.PagoResponse.builder()
                .reservaId(1L)
                .estado("PAGADO")
                .mensaje("ok")
                .build();

        when(pagoService.confirmarPago(req, "cliente@x.com")).thenReturn(resp);

        var response = controller.confirmarPago(req, "cliente@x.com", null);
        assertEquals(200, response.getStatusCodeValue());
        assertSame(resp, response.getBody());
        verify(pagoService).confirmarPago(req, "cliente@x.com");
    }

    @Test
    void confirmarPago_usesAuthenticationNameWhenClienteEmailMissing() {
        VentasDtos.PagoConfirmacionRequest req = VentasDtos.PagoConfirmacionRequest.builder()
            .reservaId(2L)
            .metodoPago("TARJETA")
            .referencia(null)
            .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("auth@user.com");

        VentasDtos.PagoResponse resp = VentasDtos.PagoResponse.builder()
                .reservaId(2L)
                .estado("PAGADO")
                .mensaje("ok")
                .build();

        when(pagoService.confirmarPago(req, "auth@user.com")).thenReturn(resp);

        var response = controller.confirmarPago(req, null, auth);
        assertEquals(200, response.getStatusCodeValue());
        assertSame(resp, response.getBody());
        verify(pagoService).confirmarPago(req, "auth@user.com");
    }

    @Test
    void confirmarPago_allowsNullEmailWhenNeitherProvided() {
        VentasDtos.PagoConfirmacionRequest req = VentasDtos.PagoConfirmacionRequest.builder()
            .reservaId(3L)
            .metodoPago("PAYPAL")
            .referencia(null)
            .build();

        VentasDtos.PagoResponse resp = VentasDtos.PagoResponse.builder()
                .reservaId(3L)
                .estado("PAGADO")
                .mensaje("ok")
                .build();

        when(pagoService.confirmarPago(req, null)).thenReturn(resp);

        var response = controller.confirmarPago(req, null, null);
        assertEquals(200, response.getStatusCodeValue());
        assertSame(resp, response.getBody());
        verify(pagoService).confirmarPago(req, null);
    }
}
