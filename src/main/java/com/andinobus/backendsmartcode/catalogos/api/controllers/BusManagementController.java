package com.andinobus.backendsmartcode.catalogos.api.controllers;

import com.andinobus.backendsmartcode.catalogos.api.dto.BusDtos;
import com.andinobus.backendsmartcode.catalogos.application.services.BusService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Profile("dev")
@RestController
@RequestMapping("/api/cooperativa/{cooperativaId}/buses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BusManagementController {

    private final BusService busService;

    /**
     * Crear un nuevo bus
     */
    @PostMapping
    public ResponseEntity<BusDtos.BusResponse> createBus(
            @PathVariable Long cooperativaId,
            @RequestBody BusDtos.CreateBusRequest request) {
        request.setCooperativaId(cooperativaId);
        BusDtos.BusResponse response = busService.createBus(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualizar un bus existente
     */
    @PutMapping("/{busId}")
    public ResponseEntity<BusDtos.BusResponse> updateBus(
            @PathVariable Long cooperativaId,
            @PathVariable Long busId,
            @RequestBody BusDtos.UpdateBusRequest request) {
        BusDtos.BusResponse response = busService.updateBus(busId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar (desactivar) un bus
     */
    @DeleteMapping("/{busId}")
    public ResponseEntity<Void> deleteBus(
            @PathVariable Long cooperativaId,
            @PathVariable Long busId) {
        busService.deleteBus(busId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener un bus por ID
     */
    @GetMapping("/{busId}")
    public ResponseEntity<BusDtos.BusResponse> getBus(
            @PathVariable Long cooperativaId,
            @PathVariable Long busId) {
        BusDtos.BusResponse response = busService.getBusById(busId);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todos los buses de la cooperativa
     */
    @GetMapping
    public ResponseEntity<List<BusDtos.BusResponse>> listBuses(
            @PathVariable Long cooperativaId) {
        List<BusDtos.BusResponse> buses = busService.getBusesByCooperativa(cooperativaId);
        return ResponseEntity.ok(buses);
    }

    /**
     * Subir foto del bus
     */
    @PostMapping("/{busId}/foto")
    public ResponseEntity<BusDtos.BusResponse> uploadFoto(
            @PathVariable Long cooperativaId,
            @PathVariable Long busId,
            @RequestParam("foto") org.springframework.web.multipart.MultipartFile foto) {
        BusDtos.BusResponse response = busService.uploadFoto(busId, foto);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar foto del bus
     */
    @DeleteMapping("/{busId}/foto")
    public ResponseEntity<Void> deleteFoto(
            @PathVariable Long cooperativaId,
            @PathVariable Long busId) {
        busService.deleteFoto(busId);
        return ResponseEntity.noContent().build();
    }
}
