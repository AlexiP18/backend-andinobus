package com.andinobus.backendsmartcode.rutas.api.controllers;

import com.andinobus.backendsmartcode.rutas.api.dto.RutasDtos;
import com.andinobus.backendsmartcode.rutas.application.services.RutasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RutasControllerTest {

    private RutasService rutasService = mock(RutasService.class);
    private RutasController controller;

    @BeforeEach
    void setUp() {
        controller = new RutasController(rutasService);
    }

    @Test
    void buscarRutas_delegatesAndReturns() {
        RutasDtos.SearchRouteItem item = RutasDtos.SearchRouteItem.builder()
                .frecuenciaId(1L).cooperativaId(2L).cooperativa("C1").origen("A").destino("B").build();
        RutasDtos.SearchRouteResponse resp = RutasDtos.SearchRouteResponse.builder()
                .items(List.of(item)).total(1).page(0).size(20).build();

        when(rutasService.buscarRutas("A","B","2025-12-07","C1","Normal","directo",0,20)).thenReturn(resp);

        var out = controller.buscarRutas("A","B","2025-12-07","C1","Normal","directo",0,20);
        assertSame(resp, out);
        verify(rutasService).buscarRutas("A","B","2025-12-07","C1","Normal","directo",0,20);
    }

    @Test
    void viajesPorFecha_delegatesAndReturns() {
        RutasDtos.ViajeItem v = RutasDtos.ViajeItem.builder().id(10L).fecha("2025-12-07").origen("X").destino("Y").build();
        RutasDtos.ViajesResponse resp = RutasDtos.ViajesResponse.builder().items(List.of(v)).total(1).page(0).size(20).build();

        when(rutasService.viajesPorFecha("2025-12-07", 0, 20)).thenReturn(resp);

        var out = controller.viajesPorFecha("2025-12-07", 0, 20);
        assertSame(resp, out);
        verify(rutasService).viajesPorFecha("2025-12-07", 0, 20);
    }

    @Test
    void disponibilidad_delegatesAndReturns() {
        RutasDtos.DisponibilidadResponse d = RutasDtos.DisponibilidadResponse.builder().viajeId(5L).totalAsientos(40).disponibles(20).porTipo(Map.of("NORMAL",20)).build();
        when(rutasService.disponibilidad(5L)).thenReturn(d);

        var out = controller.disponibilidad(5L);
        assertSame(d, out);
        verify(rutasService).disponibilidad(5L);
    }

    @Test
    void busDeViaje_delegatesAndReturns() {
        RutasDtos.BusFichaResponse b = RutasDtos.BusFichaResponse.builder().viajeId(7L).busId(70L).cooperativa("Coop").placa("ABC-1").build();
        when(rutasService.busDeViaje(7L)).thenReturn(b);

        var out = controller.busDeViaje(7L);
        assertSame(b, out);
        verify(rutasService).busDeViaje(7L);
    }
}
