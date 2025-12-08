package com.andinobus.backendsmartcode.rutas.api.controllers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RutasControllerTest {

    private RutasController controller = new RutasController();

    @Test
    void buscarRutas_delegatesAndReturns() {
        var out = controller.buscarRutas("A","B","2025-12-07","C1","Normal","directo",0,20);
        assertNotNull(out);
        assertEquals(1, out.getItems().size());
    }

    @Test
    void viajesPorFecha_delegatesAndReturns() {
        var out = controller.viajesPorFecha("2025-12-07", 0, 20);
        assertNotNull(out);
        assertEquals(1, out.getItems().size());
    }

    @Test
    void disponibilidad_delegatesAndReturns() {
        var out = controller.disponibilidad(5L);
        assertNotNull(out);
        assertEquals(5L, out.getViajeId());
    }

    @Test
    void busDeViaje_delegatesAndReturns() {
        var out = controller.busDeViaje(7L);
        assertNotNull(out);
        assertEquals(7L, out.getViajeId());
    }
}
