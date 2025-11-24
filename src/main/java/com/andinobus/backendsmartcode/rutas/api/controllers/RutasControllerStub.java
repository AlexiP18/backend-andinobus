package com.andinobus.backendsmartcode.rutas.api.controllers;

import com.andinobus.backendsmartcode.rutas.api.dto.RutasDtos;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Profile("!dev")
public class RutasControllerStub {

    @GetMapping("/rutas/buscar")
    public RutasDtos.SearchRouteResponse buscarRutas(@RequestParam(required = false) String origen,
                                                     @RequestParam(required = false) String destino,
                                                     @RequestParam(required = false) String fecha,
                                                     @RequestParam(required = false) String cooperativa,
                                                     @RequestParam(required = false) String tipoAsiento,
                                                     @RequestParam(required = false, name = "tipoViaje") String tipoViaje,
                                                     @RequestParam(defaultValue = "0") Integer page,
                                                     @RequestParam(defaultValue = "20") Integer size) {
        RutasDtos.SearchRouteItem item = RutasDtos.SearchRouteItem.builder()
                .frecuenciaId(1L)
                .cooperativaId(1L)
                .cooperativa("Cooperativa Demo")
                .origen(origen != null ? origen : "Quito")
                .destino(destino != null ? destino : "Loja")
                .horaSalida("18:00")
                .duracionEstimada("12:00")
                .tipoViaje(tipoViaje != null ? tipoViaje : "directo")
                .asientosPorTipo(Map.of("Normal", 24, "VIP", 8))
                .build();
        return RutasDtos.SearchRouteResponse.builder()
                .items(List.of(item))
                .total(1)
                .page(page)
                .size(size)
                .build();
    }

    @GetMapping("/viajes")
    public RutasDtos.ViajesResponse viajesPorFecha(@RequestParam String fecha,
                                                   @RequestParam(defaultValue = "0") Integer page,
                                                   @RequestParam(defaultValue = "20") Integer size) {
        RutasDtos.ViajeItem v = RutasDtos.ViajeItem.builder()
                .id(1001L)
                .frecuenciaId(1L)
                .fecha(fecha)
                .origen("Quito")
                .destino("Loja")
                .horaSalida("18:00")
                .estado("programado")
                .build();
        return RutasDtos.ViajesResponse.builder()
                .items(List.of(v))
                .total(1)
                .page(page)
                .size(size)
                .build();
    }

    @GetMapping("/viajes/{id}/disponibilidad")
    public RutasDtos.DisponibilidadResponse disponibilidad(@PathVariable Long id) {
        return RutasDtos.DisponibilidadResponse.builder()
                .viajeId(id)
                .totalAsientos(40)
                .disponibles(28)
                .porTipo(Map.of("Normal", 22, "VIP", 6))
                .build();
    }

    @GetMapping("/viajes/{id}/bus")
    public RutasDtos.BusFichaResponse busDeViaje(@PathVariable Long id) {
        return RutasDtos.BusFichaResponse.builder()
                .viajeId(id)
                .busId(1L)
                .cooperativa("Cooperativa Demo")
                .numeroInterno("10")
                .placa("ABC-1234")
                .chasisMarca("Volvo")
                .carroceriaMarca("Marcopolo")
                .fotoUrl(null)
                .build();
    }
}
