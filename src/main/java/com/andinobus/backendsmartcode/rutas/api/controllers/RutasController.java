package com.andinobus.backendsmartcode.rutas.api.controllers;

import com.andinobus.backendsmartcode.rutas.api.dto.RutasDtos;
import com.andinobus.backendsmartcode.rutas.application.services.RutasService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RutasController {

    private final RutasService rutasService;

    @GetMapping("/rutas/buscar")
    public RutasDtos.SearchRouteResponse buscarRutas(@RequestParam(required = false) String origen,
                                                     @RequestParam(required = false) String destino,
                                                     @RequestParam(required = false) String fecha,
                                                     @RequestParam(required = false) String cooperativa,
                                                     @RequestParam(required = false) String tipoAsiento,
                                                     @RequestParam(required = false, name = "tipoViaje") String tipoViaje,
                                                     @RequestParam(defaultValue = "0") Integer page,
                                                     @RequestParam(defaultValue = "20") Integer size) {
        return rutasService.buscarRutas(origen, destino, fecha, cooperativa, tipoAsiento, tipoViaje, page, size);
    }

    @GetMapping("/viajes")
    public RutasDtos.ViajesResponse viajesPorFecha(@RequestParam String fecha,
                                                   @RequestParam(defaultValue = "0") Integer page,
                                                   @RequestParam(defaultValue = "20") Integer size) {
        return rutasService.viajesPorFecha(fecha, page, size);
    }

    @GetMapping("/viajes/{id}/disponibilidad")
    public RutasDtos.DisponibilidadResponse disponibilidad(@PathVariable Long id) {
        return rutasService.disponibilidad(id);
    }

    @GetMapping("/viajes/{id}/bus")
    public RutasDtos.BusFichaResponse busDeViaje(@PathVariable Long id) {
        return rutasService.busDeViaje(id);
    }
}
