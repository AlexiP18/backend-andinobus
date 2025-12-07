package com.andinobus.backendsmartcode.ventas.api.controllers;

import com.andinobus.backendsmartcode.ventas.api.dto.VentasDtos;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VentasControllerTest {

    private final VentasController controller = new VentasController();

    @Test
    void crearReserva_and_obtenerReserva_success() {
        VentasDtos.ReservaCreateRequest req = new VentasDtos.ReservaCreateRequest();
        req.setViajeId(55L);
        req.setAsientos(List.of("1A","1B"));

        VentasDtos.ReservaResponse resp = controller.crearReserva(req);
        assertNotNull(resp.getId());
        assertEquals(55L, resp.getViajeId());
        assertNotNull(resp.getFechaExpira());

        VentasDtos.ReservaDetalleResponse detalle = controller.obtenerReserva(resp.getId());
        assertEquals("pendiente", detalle.getEstado());
        assertEquals(55L, detalle.getViajeId());
    }

    @Test
    void obtenerReserva_notFound_throws() {
        long badId = 999999L;
        assertThrows(IllegalArgumentException.class, () -> controller.obtenerReserva(badId));
    }

    @Test
    void pagoTransferenciaMultipart_marksPagoAndReturnsInfo() {
        VentasDtos.ReservaCreateRequest req = new VentasDtos.ReservaCreateRequest();
        req.setViajeId(66L);
        VentasDtos.ReservaResponse r = controller.crearReserva(req);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("comp.pdf");

        Map<String, Object> res = controller.pagoTransferenciaMultipart(file, r.getId(), new BigDecimal("10.00"), "REF123");
        assertEquals("RECEIVED", res.get("status"));
        assertEquals(r.getId(), res.get("reservaId"));
        assertEquals("REF123", res.get("referencia"));
        assertEquals("comp.pdf", res.get("comprobanteNombre"));

        VentasDtos.ReservaDetalleResponse detalle = controller.obtenerReserva(r.getId());
        assertEquals("pagado", detalle.getEstado());
    }

    @Test
    void paypalWebhook_returnsEventInfo() {
        VentasDtos.PaypalWebhookEvent e = new VentasDtos.PaypalWebhookEvent();
        e.setEvent_type("PAYMENT.SALE.COMPLETED");
        e.setResource_id("res-1");

        Map<String, Object> res = controller.paypalWebhook(e);
        assertEquals(true, res.get("received"));
        assertEquals("PAYMENT.SALE.COMPLETED", res.get("event_type"));
        assertEquals("res-1", res.get("resource_id"));
    }

    @Test
    void emitir_and_obtenerBoleto_success() {
        VentasDtos.ReservaCreateRequest req = new VentasDtos.ReservaCreateRequest();
        VentasDtos.ReservaResponse rr = controller.crearReserva(req);

        // mark as paid
        controller.pagoTransferenciaMultipart(null, rr.getId(), null, null);

        VentasDtos.EmitirBoletoRequest emitirReq = new VentasDtos.EmitirBoletoRequest();
        emitirReq.setReservaId(rr.getId());

        VentasDtos.BoletoResponse br = controller.emitir(emitirReq);
        assertEquals(rr.getId(), br.getReservaId());
        assertNotNull(br.getCodigoBoleto());

        VentasDtos.BoletoResponse fetched = controller.obtenerBoleto(br.getCodigoBoleto());
        assertEquals(br.getCodigoBoleto(), fetched.getCodigoBoleto());
    }

    @Test
    void emitir_throwsWhenNotPaid() {
        VentasDtos.ReservaCreateRequest req = new VentasDtos.ReservaCreateRequest();
        VentasDtos.ReservaResponse rr = controller.crearReserva(req);

        VentasDtos.EmitirBoletoRequest emitirReq = new VentasDtos.EmitirBoletoRequest();
        emitirReq.setReservaId(rr.getId());

        assertThrows(IllegalArgumentException.class, () -> controller.emitir(emitirReq));
    }

    @Test
    void obtenerBoleto_notFound_throws() {
        assertThrows(IllegalArgumentException.class, () -> controller.obtenerBoleto("NOEXIST"));
    }
}
