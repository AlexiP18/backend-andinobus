package com.andinobus.backendsmartcode.cooperativa.api.controllers;

import com.andinobus.backendsmartcode.cooperativa.api.dto.GeneracionFrecuenciasDtos.*;
import com.andinobus.backendsmartcode.cooperativa.application.services.GeneracionFrecuenciasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cooperativa/{cooperativaId}/generacion-frecuencias")
@RequiredArgsConstructor
@Tag(name = "Generación Automática de Frecuencias", description = "APIs para generar frecuencias automáticamente")
public class GeneracionFrecuenciasController {

    private final GeneracionFrecuenciasService generacionService;

    @PostMapping("/importar-csv")
    @Operation(summary = "Importar plantilla desde CSV")
    public ResponseEntity<ImportarCsvResponse> importarCsv(
            @PathVariable Long cooperativaId,
            @RequestBody ImportarCsvRequest request) {
        return ResponseEntity.ok(generacionService.importarCsv(cooperativaId, request));
    }

    @GetMapping("/plantillas")
    @Operation(summary = "Listar plantillas de la cooperativa")
    public ResponseEntity<List<PlantillaRotacion>> getPlantillas(
            @PathVariable Long cooperativaId) {
        return ResponseEntity.ok(generacionService.getPlantillas(cooperativaId));
    }

    @GetMapping("/plantillas/{plantillaId}")
    @Operation(summary = "Obtener detalle de una plantilla")
    public ResponseEntity<PlantillaRotacion> getPlantilla(
            @PathVariable Long cooperativaId,
            @PathVariable Long plantillaId) {
        return ResponseEntity.ok(generacionService.getPlantilla(plantillaId));
    }

    @DeleteMapping("/plantillas/{plantillaId}")
    @Operation(summary = "Eliminar una plantilla")
    public ResponseEntity<Void> eliminarPlantilla(
            @PathVariable Long cooperativaId,
            @PathVariable Long plantillaId) {
        generacionService.eliminarPlantilla(plantillaId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/preview")
    @Operation(summary = "Vista previa de generación de frecuencias")
    public ResponseEntity<PreviewGeneracionResponse> previewGeneracion(
            @PathVariable Long cooperativaId,
            @RequestBody GenerarFrecuenciasRequest request) {
        return ResponseEntity.ok(generacionService.previewGeneracion(cooperativaId, request));
    }

    @PostMapping("/generar")
    @Operation(summary = "Generar frecuencias según plantilla")
    public ResponseEntity<ResultadoGeneracionResponse> generarFrecuencias(
            @PathVariable Long cooperativaId,
            @RequestBody GenerarFrecuenciasRequest request) {
        return ResponseEntity.ok(generacionService.generarFrecuencias(cooperativaId, request));
    }
}
