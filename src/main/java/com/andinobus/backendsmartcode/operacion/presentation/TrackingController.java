package com.andinobus.backendsmartcode.operacion.presentation;

import com.andinobus.backendsmartcode.operacion.application.dto.ActualizarPosicionRequest;
import com.andinobus.backendsmartcode.operacion.application.dto.PosicionViajeDTO;
import com.andinobus.backendsmartcode.operacion.application.service.TrackingService;
import com.andinobus.backendsmartcode.usuarios.api.dto.AuthDtos;
import com.andinobus.backendsmartcode.usuarios.application.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el tracking GPS de viajes en tiempo real
 * 
 * Permisos:
 * - CHOFER: Puede actualizar posición de SUS viajes asignados
 * - CLIENTE: Puede ver tracking solo de SU viaje (del boleto comprado)
 * - ADMIN COOPERATIVA: Puede ver tracking de TODOS los buses de su cooperativa
 * - SUPER ADMIN: Puede ver tracking de TODOS los buses del sistema
 */
@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Slf4j
public class TrackingController {

    private final TrackingService trackingService;
    private final AuthService authService;

    private String extractToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        String demoToken = request.getHeader("X-Demo-Token");
        String token = demoToken;
        if (token == null && auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        }
        return token;
    }

    /**
     * Actualizar posición GPS (solo CHOFER del viaje)
     * POST /api/tracking/viajes/{viajeId}/posicion
     */
    @PostMapping("/viajes/{viajeId}/posicion")
    @PreAuthorize("hasRole('CHOFER')")
    public ResponseEntity<?> actualizarPosicion(
            @PathVariable Long viajeId,
            @Valid @RequestBody ActualizarPosicionRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            // Obtener ID del chofer autenticado desde el token (Authorization o X-Demo-Token)
            String token = extractToken(httpRequest);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "error", "Token no proporcionado"
                ));
            }
            AuthDtos.MeResponse me = authService.getMeByToken(token);
            Long choferId = me.getUserId();

            // Verificar que el chofer tiene permiso para este viaje
            if (!trackingService.choferTienePermisoParaViaje(viajeId, choferId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "error", "No tienes permiso para actualizar este viaje"
                ));
            }

            PosicionViajeDTO posicion = trackingService.actualizarPosicion(viajeId, request);
            return ResponseEntity.ok(posicion);

        } catch (Exception e) {
            log.error("Error al actualizar posición del viaje {}: {}", viajeId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Obtener historial de posiciones (CLIENTE, ADMIN COOPERATIVA, SUPER ADMIN)
     * GET /api/tracking/viajes/{viajeId}/posiciones
     */
    @GetMapping("/viajes/{viajeId}/posiciones")
    @PreAuthorize("hasAnyRole('CLIENTE', 'COOPERATIVA', 'ADMIN')")
    public ResponseEntity<?> obtenerHistorial(
            @PathVariable Long viajeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            HttpServletRequest httpRequest
    ) {
        try {
            // TODO: Verificar permisos según rol
            // - CLIENTE: Solo si tiene un boleto para este viaje
            // - ADMIN COOPERATIVA: Solo si el viaje es de su cooperativa
            // - SUPER ADMIN: Todos los viajes

            List<PosicionViajeDTO> historial = trackingService.obtenerHistorial(viajeId, desde);
            return ResponseEntity.ok(historial);

        } catch (Exception e) {
            log.error("Error al obtener historial del viaje {}: {}", viajeId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Obtener posición actual (CLIENTE, ADMIN COOPERATIVA, SUPER ADMIN)
     * GET /api/tracking/viajes/{viajeId}/posicion-actual
     */
    @GetMapping("/viajes/{viajeId}/posicion-actual")
    @PreAuthorize("hasAnyRole('CLIENTE', 'COOPERATIVA', 'ADMIN')")
    public ResponseEntity<?> obtenerPosicionActual(
            @PathVariable Long viajeId,
            HttpServletRequest httpRequest
    ) {
        try {
            // TODO: Verificar permisos según rol
            // - CLIENTE: Solo si tiene un boleto para este viaje
            // - ADMIN COOPERATIVA: Solo si el viaje es de su cooperativa
            // - SUPER ADMIN: Todos los viajes

            PosicionViajeDTO posicion = trackingService.obtenerPosicionActual(viajeId);
            
            if (posicion == null) {
                return ResponseEntity.ok(Map.of(
                        "mensaje", "No hay posiciones registradas para este viaje"
                ));
            }

            return ResponseEntity.ok(posicion);

        } catch (Exception e) {
            log.error("Error al obtener posición actual del viaje {}: {}", viajeId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Iniciar viaje manualmente (solo CHOFER del viaje)
     * POST /api/tracking/viajes/{viajeId}/iniciar
     */
    @PostMapping("/viajes/{viajeId}/iniciar")
    @PreAuthorize("hasRole('CHOFER')")
    public ResponseEntity<?> iniciarViaje(
            @PathVariable Long viajeId,
            HttpServletRequest httpRequest
    ) {
        try {
            String token = extractToken(httpRequest);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "error", "Token no proporcionado"
                ));
            }
            AuthDtos.MeResponse me = authService.getMeByToken(token);
            Long choferId = me.getUserId();

            if (!trackingService.choferTienePermisoParaViaje(viajeId, choferId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "error", "No tienes permiso para iniciar este viaje"
                ));
            }

            trackingService.iniciarViaje(viajeId);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Viaje iniciado correctamente"
            ));

        } catch (Exception e) {
            log.error("Error al iniciar viaje {}: {}", viajeId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Finalizar viaje manualmente (solo CHOFER del viaje)
     * POST /api/tracking/viajes/{viajeId}/finalizar
     */
    @PostMapping("/viajes/{viajeId}/finalizar")
    @PreAuthorize("hasRole('CHOFER')")
    public ResponseEntity<?> finalizarViaje(
            @PathVariable Long viajeId,
            HttpServletRequest httpRequest
    ) {
        try {
            String token = extractToken(httpRequest);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "error", "Token no proporcionado"
                ));
            }
            AuthDtos.MeResponse me = authService.getMeByToken(token);
            Long choferId = me.getUserId();

            if (!trackingService.choferTienePermisoParaViaje(viajeId, choferId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "error", "No tienes permiso para finalizar este viaje"
                ));
            }

            trackingService.finalizarViaje(viajeId);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Viaje finalizado correctamente"
            ));

        } catch (Exception e) {
            log.error("Error al finalizar viaje {}: {}", viajeId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}
