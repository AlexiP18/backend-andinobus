package com.andinobus.backendsmartcode.rutas.api.controllers;

import com.andinobus.backendsmartcode.rutas.api.dto.RutasDtos;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RutasControllerStubTest {

    private final RutasControllerStub controller = new RutasControllerStub();

    @Test
    void buscarRutas_withParams_returnsItem() {
        var resp = controller.buscarRutas("A","B","2025-12-07","Coop","Normal","directo",0,20);
        assertNotNull(resp);
        assertEquals(1, resp.getItems().size());
        var item = resp.getItems().get(0);
        assertEquals("A", item.getOrigen());
        assertEquals("B", item.getDestino());
        assertEquals("Cooperativa Demo", item.getCooperativa());
        assertEquals("directo", item.getTipoViaje());
        assertEquals(1, resp.getTotal());
    }

    @Test
    void buscarRutas_defaultsWhenNulls() {
        var resp = controller.buscarRutas(null,null,null,null,null,null,0,20);
        var item = resp.getItems().get(0);
        assertEquals("Quito", item.getOrigen());
        assertEquals("Loja", item.getDestino());
        assertEquals("directo", item.getTipoViaje());
    }

    @Test
    void viajesPorFecha_returnsItem() {
        var resp = controller.viajesPorFecha("2025-12-07",0,20);
        assertNotNull(resp);
        assertEquals(1, resp.getItems().size());
        assertEquals("Quito", resp.getItems().get(0).getOrigen());
    }

    @Test
    void disponibilidad_returnsCounts() {
        var resp = controller.disponibilidad(5L);
        assertEquals(5L, resp.getViajeId());
        assertEquals(40, resp.getTotalAsientos());
        assertEquals(28, resp.getDisponibles());
        assertEquals(2, resp.getPorTipo().size());
        assertEquals(22, resp.getPorTipo().get("Normal"));
    }

    @Test
    void busDeViaje_returnsBusInfo() {
        var resp = controller.busDeViaje(7L);
        assertEquals(7L, resp.getViajeId());
        assertEquals(1L, resp.getBusId());
        assertEquals("ABC-1234", resp.getPlaca());
    }
}
