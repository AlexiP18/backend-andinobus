package com.andinobus.backendsmartcode.ventas.api.controllers;

import com.andinobus.backendsmartcode.ventas.api.dto.VentasDtos;
import com.andinobus.backendsmartcode.ventas.application.services.BoletoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BoletosControllerTest {

    private BoletoService boletoService = mock(BoletoService.class);
    private BoletosController controller;

    @BeforeEach
    void setUp() {
        controller = new BoletosController(boletoService);
    }

    @Test
    void generarBoleto_usesClienteEmailWhenProvided() {
        Long reservaId = 123L;
        String clienteEmail = "client@example.com";

        VentasDtos.BoletoResponse resp = VentasDtos.BoletoResponse.builder()
                .codigoBoleto("CB-1")
                .reservaId(reservaId)
                .estado("EMITIDO")
                .codigoQR("data:...")
                .build();

        when(boletoService.generarBoleto(reservaId, clienteEmail)).thenReturn(resp);

        var response = controller.generarBoleto(reservaId, clienteEmail, null);
        assertEquals(200, response.getStatusCodeValue());
        assertSame(resp, response.getBody());
        verify(boletoService).generarBoleto(reservaId, clienteEmail);
    }

    @Test
    void generarBoleto_usesAuthenticationNameWhenClienteEmailMissing() {
        Long reservaId = 222L;
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("authuser@example.com");

        VentasDtos.BoletoResponse resp = VentasDtos.BoletoResponse.builder()
                .codigoBoleto("CB-2")
                .reservaId(reservaId)
                .estado("EMITIDO")
                .build();

        when(boletoService.generarBoleto(reservaId, "authuser@example.com")).thenReturn(resp);

        var response = controller.generarBoleto(reservaId, null, auth);
        assertEquals(200, response.getStatusCodeValue());
        assertSame(resp, response.getBody());
        verify(boletoService).generarBoleto(reservaId, "authuser@example.com");
    }

    @Test
    void generarBoleto_allowsNullEmailWhenNeitherProvided() {
        Long reservaId = 333L;

        VentasDtos.BoletoResponse resp = VentasDtos.BoletoResponse.builder()
                .codigoBoleto("CB-3")
                .reservaId(reservaId)
                .estado("EMITIDO")
                .build();

        when(boletoService.generarBoleto(reservaId, null)).thenReturn(resp);

        var response = controller.generarBoleto(reservaId, null, null);
        assertEquals(200, response.getStatusCodeValue());
        assertSame(resp, response.getBody());
        verify(boletoService).generarBoleto(reservaId, null);
    }
}
