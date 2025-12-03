package com.andinobus.backendsmartcode.catalogos.api.controllers;

import com.andinobus.backendsmartcode.catalogos.api.dto.BusChoferDtos.*;
import com.andinobus.backendsmartcode.catalogos.application.services.BusChoferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cooperativa/{cooperativaId}/buses/{busId}/choferes")
@RequiredArgsConstructor
@Tag(name = "Bus - Choferes", description = "Gestión de asignación de choferes a buses")
public class BusChoferController {

    private final BusChoferService busChoferService;

    @GetMapping
    @Operation(summary = "Obtener choferes asignados a un bus")
    public ResponseEntity<List<BusChoferResponse>> getChoferesDelBus(
            @PathVariable Long cooperativaId,
            @PathVariable Long busId) {
        return ResponseEntity.ok(busChoferService.getChoferesDelBus(cooperativaId, busId));
    }

    @GetMapping("/disponibles")
    @Operation(summary = "Obtener choferes disponibles para asignar al bus")
    public ResponseEntity<List<ChoferDisponible>> getChoferesDisponibles(
            @PathVariable Long cooperativaId,
            @PathVariable Long busId) {
        return ResponseEntity.ok(busChoferService.getChoferesDisponibles(cooperativaId, busId));
    }

    @PostMapping
    @Operation(summary = "Asignar un chofer a un bus")
    public ResponseEntity<BusChoferResponse> asignarChofer(
            @PathVariable Long cooperativaId,
            @PathVariable Long busId,
            @RequestBody AsignarChoferRequest request) {
        return ResponseEntity.ok(busChoferService.asignarChofer(cooperativaId, busId, request));
    }

    @PutMapping("/sincronizar")
    @Operation(summary = "Sincronizar todos los choferes de un bus")
    public ResponseEntity<List<BusChoferResponse>> sincronizarChoferes(
            @PathVariable Long cooperativaId,
            @PathVariable Long busId,
            @RequestBody SincronizarChoferesRequest request) {
        return ResponseEntity.ok(busChoferService.sincronizarChoferes(cooperativaId, busId, request));
    }

    @DeleteMapping("/{choferId}")
    @Operation(summary = "Remover un chofer de un bus")
    public ResponseEntity<Void> removerChofer(
            @PathVariable Long cooperativaId,
            @PathVariable Long busId,
            @PathVariable Long choferId) {
        busChoferService.removerChofer(cooperativaId, busId, choferId);
        return ResponseEntity.noContent().build();
    }
}
