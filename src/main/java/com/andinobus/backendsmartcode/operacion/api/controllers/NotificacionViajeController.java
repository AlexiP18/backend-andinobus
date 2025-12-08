package com.andinobus.backendsmartcode.operacion.api.controllers;

import com.andinobus.backendsmartcode.operacion.api.dto.NotificacionViajeDtos.*;
import com.andinobus.backendsmartcode.operacion.application.services.NotificacionViajeService;
import com.andinobus.backendsmartcode.operacion.domain.entities.NotificacionViaje;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de notificaciones de viajes
 */
@RestController
@RequestMapping("/api/cooperativa/{cooperativaId}/notificaciones")
@RequiredArgsConstructor
public class NotificacionViajeController {

    private final NotificacionViajeService notificacionService;

    /**
     * Obtiene todas las notificaciones de una cooperativa
     * 
     * GET /api/cooperativa/{cooperativaId}/notificaciones
     */
    @GetMapping
    public ResponseEntity<List<NotificacionResponse>> getNotificaciones(
            @PathVariable Long cooperativaId,
            @RequestParam(required = false, defaultValue = "false") Boolean soloNoLeidas
    ) {
        List<NotificacionViaje> notificaciones;
        
        if (Boolean.TRUE.equals(soloNoLeidas)) {
            notificaciones = notificacionService.getNotificacionesNoLeidas(cooperativaId);
        } else {
            notificaciones = notificacionService.getNotificacionesCooperativa(cooperativaId);
        }
        
        List<NotificacionResponse> response = notificaciones.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene las notificaciones paginadas
     * 
     * GET /api/cooperativa/{cooperativaId}/notificaciones/paginado?page=0&size=10
     */
    @GetMapping("/paginado")
    public ResponseEntity<Page<NotificacionResponse>> getNotificacionesPaginadas(
            @PathVariable Long cooperativaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<NotificacionViaje> notificaciones = notificacionService.getNotificacionesPaginadas(cooperativaId, page, size);
        Page<NotificacionResponse> response = notificaciones.map(this::toResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el conteo de notificaciones no leídas
     * 
     * GET /api/cooperativa/{cooperativaId}/notificaciones/count
     */
    @GetMapping("/count")
    public ResponseEntity<NotificacionCountResponse> getConteoNoLeidas(@PathVariable Long cooperativaId) {
        Long count = notificacionService.contarNotificacionesNoLeidas(cooperativaId);
        
        return ResponseEntity.ok(NotificacionCountResponse.builder()
                .noLeidas(count)
                .build());
    }

    /**
     * Marca una notificación como leída
     * 
     * PUT /api/cooperativa/{cooperativaId}/notificaciones/{id}/leer
     */
    @PutMapping("/{id}/leer")
    public ResponseEntity<Void> marcarComoLeida(
            @PathVariable Long cooperativaId,
            @PathVariable Long id
    ) {
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Marca todas las notificaciones como leídas
     * 
     * PUT /api/cooperativa/{cooperativaId}/notificaciones/leer-todas
     */
    @PutMapping("/leer-todas")
    public ResponseEntity<MarcadoResponse> marcarTodasComoLeidas(@PathVariable Long cooperativaId) {
        int marcadas = notificacionService.marcarTodasComoLeidas(cooperativaId);
        
        return ResponseEntity.ok(MarcadoResponse.builder()
                .notificacionesMarcadas(marcadas)
                .mensaje("Se marcaron " + marcadas + " notificaciones como leídas")
                .build());
    }

    /**
     * Convierte la entidad a DTO de respuesta
     */
    private NotificacionResponse toResponse(NotificacionViaje notificacion) {
        return NotificacionResponse.builder()
                .id(notificacion.getId())
                .viajeId(notificacion.getViaje().getId())
                .tipo(notificacion.getTipo())
                .titulo(notificacion.getTitulo())
                .mensaje(notificacion.getMensaje())
                .detalleViaje(notificacion.getDetalleViaje())
                .leida(notificacion.getLeida())
                .fechaCreacion(notificacion.getFechaCreacion())
                .fechaLectura(notificacion.getFechaLectura())
                .build();
    }
}
