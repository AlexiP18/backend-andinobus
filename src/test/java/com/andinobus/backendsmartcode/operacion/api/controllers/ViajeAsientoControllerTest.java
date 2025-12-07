package com.andinobus.backendsmartcode.operacion.api.controllers;

import com.andinobus.backendsmartcode.operacion.api.dto.ViajeAsientoDtos;
import com.andinobus.backendsmartcode.operacion.application.services.ViajeAsientoService;
import com.andinobus.backendsmartcode.operacion.domain.entities.ViajeAsiento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViajeAsientoControllerTest {

    @Mock
    private ViajeAsientoService service;

    @InjectMocks
    private ViajeAsientoController controller;

    @Test
    void obtenerAsientosViaje_mapsEntitiesToDto() {
        Long viajeId = 100L;

        ViajeAsiento a = new ViajeAsiento();
        a.setNumeroAsiento("1A");
        a.setTipoAsiento("NORMAL");
        a.setEstado("DISPONIBLE");

        when(service.obtenerAsientosViaje(viajeId)).thenReturn(List.of(a));

        ResponseEntity<List<ViajeAsientoDtos.AsientoDisponibilidadResponse>> resp = controller.obtenerAsientosViaje(viajeId);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).hasSize(1);
        ViajeAsientoDtos.AsientoDisponibilidadResponse dto = resp.getBody().get(0);
        assertThat(dto.getNumeroAsiento()).isEqualTo("1A");
        assertThat(dto.getTipoAsiento()).isEqualTo("NORMAL");
        assertThat(dto.getEstado()).isEqualTo("DISPONIBLE");

        verify(service).obtenerAsientosViaje(viajeId);
    }

    @Test
    void obtenerAsientosDisponibles_mapsEntitiesToDto() {
        Long viajeId = 200L;

        ViajeAsiento a = new ViajeAsiento();
        a.setNumeroAsiento("2B");
        a.setTipoAsiento("VIP");
        a.setEstado("DISPONIBLE");

        when(service.obtenerAsientosDisponibles(viajeId)).thenReturn(List.of(a));

        ResponseEntity<List<ViajeAsientoDtos.AsientoDisponibilidadResponse>> resp = controller.obtenerAsientosDisponibles(viajeId);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).hasSize(1);
        ViajeAsientoDtos.AsientoDisponibilidadResponse dto = resp.getBody().get(0);
        assertThat(dto.getNumeroAsiento()).isEqualTo("2B");
        assertThat(dto.getTipoAsiento()).isEqualTo("VIP");
        assertThat(dto.getEstado()).isEqualTo("DISPONIBLE");

        verify(service).obtenerAsientosDisponibles(viajeId);
    }

    @Test
    void obtenerEstadisticas_returnsMappedResponse() {
        Long viajeId = 300L;

        ViajeAsientoService.AsientosEstadisticas stats = new ViajeAsientoService.AsientosEstadisticas(10, 7, 2, 1);

        when(service.obtenerEstadisticas(viajeId)).thenReturn(stats);

        ResponseEntity<ViajeAsientoDtos.AsientosEstadisticasResponse> resp = controller.obtenerEstadisticas(viajeId);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        ViajeAsientoDtos.AsientosEstadisticasResponse body = resp.getBody();
        assertThat(body.getTotal()).isEqualTo(10);
        assertThat(body.getDisponibles()).isEqualTo(7);
        assertThat(body.getReservados()).isEqualTo(2);
        assertThat(body.getVendidos()).isEqualTo(1);

        verify(service).obtenerEstadisticas(viajeId);
    }

    @Test
    void inicializarAsientos_callsServiceAndReturnsCreatedCount() {
        Long viajeId = 400L;

        // inicializarAsientosViaje is void; after calling, controller queries stats
        when(service.obtenerEstadisticas(viajeId)).thenReturn(new ViajeAsientoService.AsientosEstadisticas(5,5,0,0));

        ResponseEntity<ViajeAsientoDtos.InicializarAsientosResponse> resp = controller.inicializarAsientos(viajeId);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        ViajeAsientoDtos.InicializarAsientosResponse body = resp.getBody();
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getAsientosCreados()).isEqualTo(5);

        verify(service).inicializarAsientosViaje(viajeId);
        verify(service).obtenerEstadisticas(viajeId);
    }
}
