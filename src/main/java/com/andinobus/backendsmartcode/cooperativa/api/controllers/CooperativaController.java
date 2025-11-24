package com.andinobus.backendsmartcode.cooperativa.api.controllers;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.cooperativa.api.dto.CooperativaDtos.*;
import com.andinobus.backendsmartcode.cooperativa.application.services.CooperativaService;
import com.andinobus.backendsmartcode.operacion.domain.entities.AsignacionBusFrecuencia;
import com.andinobus.backendsmartcode.operacion.domain.entities.DiaParadaBus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController("cooperativaOpsController")
@RequestMapping("/api/cooperativa")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Profile("dev")
public class CooperativaController {

    private final CooperativaService cooperativaService;

    /**
     * Obtener todos los buses de la cooperativa
     */
    @GetMapping("/buses")
    public ResponseEntity<List<BusDto>> obtenerBuses(
            @RequestParam Long cooperativaId) {
        
        List<Bus> buses = cooperativaService.obtenerBusesCooperativa(cooperativaId);
        List<BusDto> busesDto = buses.stream()
                .map(this::convertirBusADto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(busesDto);
    }

    /**
     * Obtener buses disponibles para una fecha
     */
    @GetMapping("/buses/disponibles")
    public ResponseEntity<List<BusDto>> obtenerBusesDisponibles(
            @RequestParam Long cooperativaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        
        List<Bus> buses = cooperativaService.obtenerBusesDisponibles(cooperativaId, fecha);
        List<BusDto> busesDto = buses.stream()
                .map(this::convertirBusADto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(busesDto);
    }

    /**
     * Asignar un bus a una frecuencia
     */
    @PostMapping("/asignaciones")
    public ResponseEntity<AsignacionBusFrecuenciaDto> asignarBus(
            @RequestBody AsignarBusRequest request) {
        
        AsignacionBusFrecuencia asignacion = cooperativaService.asignarBusAFrecuencia(
                request.getBusId(),
                request.getFrecuenciaId(),
                request.getFechaInicio(),
                request.getFechaFin(),
                request.getObservaciones()
        );
        
        return ResponseEntity.ok(convertirAsignacionADto(asignacion));
    }

    /**
     * Finalizar una asignación
     */
    @PatchMapping("/asignaciones/{asignacionId}/finalizar")
    public ResponseEntity<Void> finalizarAsignacion(@PathVariable Long asignacionId) {
        cooperativaService.finalizarAsignacion(asignacionId);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtener asignaciones activas
     */
    @GetMapping("/asignaciones")
    public ResponseEntity<List<AsignacionBusFrecuenciaDto>> obtenerAsignacionesActivas(
            @RequestParam Long cooperativaId) {
        
        List<AsignacionBusFrecuencia> asignaciones = 
                cooperativaService.obtenerAsignacionesActivas(cooperativaId);
        
        List<AsignacionBusFrecuenciaDto> asignacionesDto = asignaciones.stream()
                .map(this::convertirAsignacionADto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(asignacionesDto);
    }

    /**
     * Registrar un día de parada
     */
    @PostMapping("/dias-parada")
    public ResponseEntity<DiaParadaBusDto> registrarDiaParada(
            @RequestBody RegistrarDiaParadaRequest request) {
        
        DiaParadaBus diaParada = cooperativaService.registrarDiaParada(
                request.getBusId(),
                request.getFecha(),
                request.getMotivo(),
                request.getObservaciones()
        );
        
        return ResponseEntity.ok(convertirDiaParadaADto(diaParada));
    }

    /**
     * Obtener días de parada en un rango de fechas
     */
    @GetMapping("/dias-parada")
    public ResponseEntity<List<DiaParadaBusDto>> obtenerDiasParada(
            @RequestParam Long cooperativaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        List<DiaParadaBus> diasParada = 
                cooperativaService.obtenerDiasParada(cooperativaId, fechaInicio, fechaFin);
        
        List<DiaParadaBusDto> diasParadaDto = diasParada.stream()
                .map(this::convertirDiaParadaADto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(diasParadaDto);
    }

    /**
     * Obtener resumen de disponibilidad
     */
    @GetMapping("/resumen-disponibilidad")
    public ResponseEntity<ResumenDisponibilidadDto> obtenerResumenDisponibilidad(
            @RequestParam Long cooperativaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        
        List<Bus> todosBuses = cooperativaService.obtenerBusesCooperativa(cooperativaId);
        int exceso = cooperativaService.calcularExcesoBuses(cooperativaId, fecha);
        
        int disponibles = (int) todosBuses.stream().filter(b -> "DISPONIBLE".equals(b.getEstado())).count();
        int enServicio = (int) todosBuses.stream().filter(b -> "EN_SERVICIO".equals(b.getEstado())).count();
        int mantenimiento = (int) todosBuses.stream().filter(b -> "MANTENIMIENTO".equals(b.getEstado())).count();
        int parada = (int) todosBuses.stream().filter(b -> "PARADA".equals(b.getEstado())).count();
        
        ResumenDisponibilidadDto resumen = ResumenDisponibilidadDto.builder()
                .totalBuses(todosBuses.size())
                .busesDisponibles(disponibles)
                .busesEnServicio(enServicio)
                .busesMantenimiento(mantenimiento)
                .busesParada(parada)
                .excesoBuses(exceso)
                .build();
        
        return ResponseEntity.ok(resumen);
    }

    // Métodos de conversión a DTOs

    private BusDto convertirBusADto(Bus bus) {
        return BusDto.builder()
                .id(bus.getId())
                .numeroInterno(bus.getNumeroInterno())
                .placa(bus.getPlaca())
                .chasisMarca(bus.getChasisMarca())
                .carroceriaMarca(bus.getCarroceriaMarca())
                .capacidadAsientos(bus.getCapacidadAsientos())
                .estado(bus.getEstado())
                .activo(bus.getActivo())
                .fotoUrl(bus.getFotoUrl())
                .build();
    }

    private AsignacionBusFrecuenciaDto convertirAsignacionADto(AsignacionBusFrecuencia asignacion) {
        return AsignacionBusFrecuenciaDto.builder()
                .id(asignacion.getId())
                .bus(convertirBusADto(asignacion.getBus()))
                .fechaInicio(asignacion.getFechaInicio())
                .fechaFin(asignacion.getFechaFin())
                .estado(asignacion.getEstado())
                .observaciones(asignacion.getObservaciones())
                .build();
    }

    private DiaParadaBusDto convertirDiaParadaADto(DiaParadaBus diaParada) {
        return DiaParadaBusDto.builder()
                .id(diaParada.getId())
                .bus(convertirBusADto(diaParada.getBus()))
                .fecha(diaParada.getFecha())
                .motivo(diaParada.getMotivo())
                .observaciones(diaParada.getObservaciones())
                .build();
    }
}
