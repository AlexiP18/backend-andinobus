package com.andinobus.backendsmartcode.catalogos.application.services;

import com.andinobus.backendsmartcode.catalogos.api.dto.AsientoLayoutDtos.*;
import com.andinobus.backendsmartcode.catalogos.domain.entities.AsientoLayout;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.AsientoLayoutRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusRepository;
import com.andinobus.backendsmartcode.common.errors.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Profile("dev")
@Service
@RequiredArgsConstructor
public class AsientoLayoutService {

    private final AsientoLayoutRepository asientoLayoutRepository;
    private final BusRepository busRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Obtiene el layout completo de asientos de un bus
     */
    @Transactional(readOnly = true)
    public BusLayoutResponse getLayout(Long busId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new NotFoundException("Bus no encontrado"));

        List<AsientoLayout> asientos = asientoLayoutRepository.findByBusIdOrderByFilaAscColumnaAsc(busId);
        
        // Si no hay layout, devolver estructura vacía
        if (asientos.isEmpty()) {
            return BusLayoutResponse.builder()
                    .busId(busId)
                    .placa(bus.getPlaca())
                    .capacidadTotal(bus.getCapacidadAsientos())
                    .capacidadHabilitada(0)
                    .maxFilas(0)
                    .maxColumnas(0)
                    .asientos(new ArrayList<>())
                    .build();
        }

        // Obtener dimensiones máximas
        Integer maxFilas = asientoLayoutRepository.findMaxFilaByBusId(busId);
        Integer maxColumnas = asientoLayoutRepository.findMaxColumnaByBusId(busId);
        
        // Contar asientos habilitados
        Long capacidadHabilitada = asientoLayoutRepository.countByBusIdAndHabilitadoTrue(busId);

        // Convertir a DTOs
        List<AsientoResponse> asientosResponse = asientos.stream()
                .map(this::toAsientoResponse)
                .collect(Collectors.toList());

        return BusLayoutResponse.builder()
                .busId(busId)
                .placa(bus.getPlaca())
                .capacidadTotal(bus.getCapacidadAsientos())
                .capacidadHabilitada(capacidadHabilitada.intValue())
                .maxFilas(maxFilas != null ? maxFilas + 1 : 0) // +1 porque es 0-indexed
                .maxColumnas(maxColumnas != null ? maxColumnas + 1 : 0)
                .asientos(asientosResponse)
                .build();
    }

    /**
     * Genera un layout automático en grid según la capacidad del bus
     */
    @Transactional
    public AsientoOperationResponse generateLayout(Long busId, GenerateLayoutRequest request) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new NotFoundException("Bus no encontrado"));

        // Si sobrescribir es true, eliminar layout existente
        if (Boolean.TRUE.equals(request.getSobrescribir())) {
            asientoLayoutRepository.deleteByBusId(busId);
            entityManager.flush();
            entityManager.clear();
            log.info("Layout existente eliminado para bus {}", busId);
        } else {
            // Verificar si ya existe layout
            Long count = asientoLayoutRepository.countByBusId(busId);
            if (count > 0) {
                throw new IllegalStateException("El bus ya tiene un layout configurado. Use sobrescribir=true para reemplazarlo.");
            }
        }

        Integer filas = request.getFilas();
        Integer columnas = request.getColumnas();
        Integer capacidad = bus.getCapacidadAsientos();
        Boolean incluirFilaTrasera = Boolean.TRUE.equals(request.getIncluirFilaTrasera());
        
        log.info("Generando layout para bus {}: filas={}, columnas={}, incluirFilaTrasera={}, capacidad={}", 
                 busId, filas, columnas, incluirFilaTrasera, capacidad);

        // Calcular capacidad total del layout
        // Si incluye fila trasera: (filas-1) * columnas + 5 asientos traseros
        // Si no: filas * columnas
        int capacidadLayout;
        if (incluirFilaTrasera) {
            capacidadLayout = (filas - 1) * columnas + 5;
        } else {
            capacidadLayout = filas * columnas;
        }

        // Validar que el layout sea exacto o suficiente
        if (capacidadLayout < capacidad) {
            throw new IllegalArgumentException(
                String.format("El layout (%s = %d asientos) es insuficiente para la capacidad del bus (%d asientos)", 
                    incluirFilaTrasera ? String.format("(%d-1)x%d + 5 traseros", filas, columnas) : String.format("%dx%d", filas, columnas),
                    capacidadLayout, capacidad)
            );
        }
        
        // Si incluye fila trasera, la capacidad del bus debe ser múltiplo correcto
        if (incluirFilaTrasera) {
            int asientosNormales = (filas - 1) * columnas;
            if (capacidad > asientosNormales && capacidad < asientosNormales + 5) {
                throw new IllegalArgumentException(
                    String.format("Con fila trasera activada, la capacidad del bus debe ser %d (sin traseros) o %d (con 5 traseros completos). Capacidad actual: %d", 
                        asientosNormales, asientosNormales + 5, capacidad)
                );
            }
        }

        List<AsientoLayout> asientos = new ArrayList<>();
        int numeroAsiento = 1;

        // Determinar cuántas filas tendrán el layout normal (4 columnas)
        int filasNormales = incluirFilaTrasera ? filas - 1 : filas;

        // Generar asientos en grid principal (todas las filas excepto la última si hay fila trasera)
        for (int fila = 0; fila < filasNormales; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                if (numeroAsiento <= capacidad) {
                    AsientoLayout asiento = AsientoLayout.builder()
                            .bus(bus)
                            .numeroAsiento(numeroAsiento)
                            .fila(fila)
                            .columna(columna)
                            .tipoAsiento("NORMAL")
                            .habilitado(true)
                            .build();
                    asientos.add(asiento);
                    numeroAsiento++;
                }
            }
        }

        // Generar fila trasera continua con 5 asientos:
        // Col 0-1: 2 izquierda, Col 2: centro (pasillo), Col 3-4: 2 derecha
        // Los asientos traseros se inicializan como NORMAL por defecto, pero se identifican por su posición
        if (incluirFilaTrasera && numeroAsiento <= capacidad) {
            int filaFinal = filasNormales; // La última fila
            // Distribución: [0, 1, 2, 3, 4] = [izq, izq, centro, der, der]
            for (int columna = 0; columna < 5 && numeroAsiento <= capacidad; columna++) {
                AsientoLayout asiento = AsientoLayout.builder()
                        .bus(bus)
                        .numeroAsiento(numeroAsiento)
                        .fila(filaFinal)
                        .columna(columna)
                        .tipoAsiento("NORMAL") // Se puede cambiar a VIP o ACONDICIONADO después
                        .habilitado(true)
                        .build();
                asientos.add(asiento);
                numeroAsiento++;
            }
        }

        asientoLayoutRepository.saveAll(asientos);
        log.info("Layout generado para bus {}: {} asientos (grid {}x{} + {})", 
                 busId, asientos.size(), filas, columnas, incluirFilaTrasera ? "5 traseros" : "0");

        return AsientoOperationResponse.builder()
                .success(true)
                .message("Layout generado exitosamente")
                .asientosCreados(asientos.size())
                .asientosActualizados(0)
                .build();
    }

    /**
     * Actualiza un asiento individual
     */
    @Transactional
    public AsientoResponse updateAsiento(Long busId, Long asientoId, UpdateAsientoRequest request) {
        AsientoLayout asiento = asientoLayoutRepository.findById(asientoId)
                .orElseThrow(() -> new NotFoundException("Asiento no encontrado"));

        if (!asiento.getBus().getId().equals(busId)) {
            throw new IllegalArgumentException("El asiento no pertenece al bus especificado");
        }

        // Actualizar campos
        if (request.getTipoAsiento() != null) {
            // Validar tipo de asiento
            if (!isValidTipoAsiento(request.getTipoAsiento())) {
                throw new IllegalArgumentException("Tipo de asiento inválido. Use: NORMAL, VIP o ACONDICIONADO");
            }
            asiento.setTipoAsiento(request.getTipoAsiento());
        }

        if (request.getHabilitado() != null) {
            asiento.setHabilitado(request.getHabilitado());
        }

        AsientoLayout saved = asientoLayoutRepository.save(asiento);
        log.info("Asiento {} del bus {} actualizado: tipo={}, habilitado={}", 
                asiento.getNumeroAsiento(), busId, saved.getTipoAsiento(), saved.getHabilitado());

        return toAsientoResponse(saved);
    }

    /**
     * Actualiza múltiples asientos a la vez
     */
    @Transactional
    public AsientoOperationResponse bulkUpdateAsientos(Long busId, BulkUpdateAsientosRequest request) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new NotFoundException("Bus no encontrado"));

        int actualizados = 0;

        for (UpdateAsientoItem item : request.getAsientos()) {
            AsientoLayout asiento;
            
            if (item.getId() != null) {
                // Actualizar existente
                asiento = asientoLayoutRepository.findById(item.getId())
                        .orElseThrow(() -> new NotFoundException("Asiento con ID " + item.getId() + " no encontrado"));
            } else if (item.getNumeroAsiento() != null) {
                // Buscar por número de asiento
                asiento = asientoLayoutRepository.findByBusIdAndNumeroAsiento(busId, item.getNumeroAsiento())
                        .orElseThrow(() -> new NotFoundException("Asiento número " + item.getNumeroAsiento() + " no encontrado"));
            } else {
                throw new IllegalArgumentException("Debe proporcionar id o numeroAsiento");
            }

            // Validar que pertenece al bus
            if (!asiento.getBus().getId().equals(busId)) {
                throw new IllegalArgumentException("El asiento " + asiento.getNumeroAsiento() + " no pertenece al bus " + busId);
            }

            // Actualizar campos
            if (item.getFila() != null) asiento.setFila(item.getFila());
            if (item.getColumna() != null) asiento.setColumna(item.getColumna());
            if (item.getTipoAsiento() != null) {
                if (!isValidTipoAsiento(item.getTipoAsiento())) {
                    throw new IllegalArgumentException("Tipo de asiento inválido: " + item.getTipoAsiento());
                }
                asiento.setTipoAsiento(item.getTipoAsiento());
            }
            if (item.getHabilitado() != null) asiento.setHabilitado(item.getHabilitado());

            asientoLayoutRepository.save(asiento);
            actualizados++;
        }

        log.info("Bulk update completado para bus {}: {} asientos actualizados", busId, actualizados);

        return AsientoOperationResponse.builder()
                .success(true)
                .message("Asientos actualizados exitosamente")
                .asientosCreados(0)
                .asientosActualizados(actualizados)
                .build();
    }

    /**
     * Elimina el layout completo de un bus
     */
    @Transactional
    public AsientoOperationResponse deleteLayout(Long busId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new NotFoundException("Bus no encontrado"));

        Long count = asientoLayoutRepository.countByBusId(busId);
        asientoLayoutRepository.deleteByBusId(busId);
        entityManager.flush();
        entityManager.clear();
        log.info("Layout eliminado para bus {}: {} asientos", busId, count);

        return AsientoOperationResponse.builder()
                .success(true)
                .message("Layout eliminado exitosamente")
                .asientosCreados(0)
                .asientosActualizados(count.intValue())
                .build();
    }

    // ===== Métodos auxiliares =====

    private AsientoResponse toAsientoResponse(AsientoLayout asiento) {
        return AsientoResponse.builder()
                .id(asiento.getId())
                .numeroAsiento(asiento.getNumeroAsiento())
                .fila(asiento.getFila())
                .columna(asiento.getColumna())
                .tipoAsiento(asiento.getTipoAsiento())
                .habilitado(asiento.getHabilitado())
                .build();
    }

    private boolean isValidTipoAsiento(String tipo) {
        return "NORMAL".equals(tipo) || "VIP".equals(tipo) || "ACONDICIONADO".equals(tipo);
    }
}
