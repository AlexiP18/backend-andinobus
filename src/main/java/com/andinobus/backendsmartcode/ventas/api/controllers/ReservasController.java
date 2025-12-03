package com.andinobus.backendsmartcode.ventas.api.controllers;

import com.andinobus.backendsmartcode.ventas.api.dto.VentasDtos;
import com.andinobus.backendsmartcode.ventas.application.services.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Profile("dev")
@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservasController {

    private final ReservaService reservaService;

    @PostMapping
    public ResponseEntity<VentasDtos.ReservaResponse> crearReserva(
            @RequestBody VentasDtos.ReservaCreateRequest request) {
        
        // Usar el email del request en lugar de authentication
        VentasDtos.ReservaResponse response = reservaService.crearReserva(request, request.getClienteEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reservaId}")
    public ResponseEntity<VentasDtos.ReservaDetalleResponse> obtenerReserva(
            @PathVariable Long reservaId,
            @RequestParam(required = false) String clienteEmail,
            Authentication authentication) {
        
        String email = clienteEmail != null ? clienteEmail : (authentication != null ? authentication.getName() : null);
        VentasDtos.ReservaDetalleResponse response = reservaService.obtenerReserva(reservaId, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mis-reservas")
    public ResponseEntity<List<VentasDtos.ReservaDetalleResponse>> listarMisReservas(
            @RequestParam(required = false) String clienteEmail,
            Authentication authentication) {
        
        String email = clienteEmail != null ? clienteEmail : (authentication != null ? authentication.getName() : null);
        List<VentasDtos.ReservaDetalleResponse> reservas = reservaService.listarReservasPorCliente(email);
        return ResponseEntity.ok(reservas);
    }

    @DeleteMapping("/{reservaId}")
    public ResponseEntity<Void> cancelarReserva(
            @PathVariable Long reservaId,
            @RequestParam(required = false) String clienteEmail,
            Authentication authentication) {
        
        String email = clienteEmail != null ? clienteEmail : (authentication != null ? authentication.getName() : null);
        reservaService.cancelarReserva(reservaId, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/viaje/{viajeId}/asientos")
    public ResponseEntity<List<VentasDtos.AsientoDisponibilidadDto>> obtenerAsientosDisponibles(
            @PathVariable Long viajeId) {
        
        List<VentasDtos.AsientoDisponibilidadDto> asientos = reservaService.obtenerAsientosDisponibles(viajeId);
        return ResponseEntity.ok(asientos);
    }

    @GetMapping("/frecuencia/{frecuenciaId}/asientos")
    public ResponseEntity<VentasDtos.AsientosViajeResponse> obtenerAsientosDisponiblesPorFrecuencia(
            @PathVariable Long frecuenciaId,
            @RequestParam String fecha) {
        
        VentasDtos.AsientosViajeResponse response = reservaService.obtenerAsientosDisponiblesPorFrecuencia(frecuenciaId, fecha);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cooperativa/{cooperativaId}")
    public ResponseEntity<List<VentasDtos.ReservaCooperativaDto>> obtenerReservasPorCooperativa(
            @PathVariable Long cooperativaId,
            @RequestParam(required = false) String estado) {
        
        List<VentasDtos.ReservaCooperativaDto> reservas = reservaService.obtenerReservasPorCooperativa(cooperativaId, estado);
        return ResponseEntity.ok(reservas);
    }
}
