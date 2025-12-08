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

        Integer filas = request.getFilas();
        Integer columnas = request.getColumnas();
        Integer piso = request.getPiso() != null ? request.getPiso() : 1;
        Boolean incluirFilaTrasera = Boolean.TRUE.equals(request.getIncluirFilaTrasera());

        // Si sobrescribir es true, eliminar layout existente SOLO del piso específico
        if (Boolean.TRUE.equals(request.getSobrescribir())) {
            // Para piso 1, eliminamos todos los asientos del bus para evitar conflictos de numeración
            if (piso == 1) {
                asientoLayoutRepository.deleteByBusId(busId);
                log.info("Todos los asientos eliminados para bus {} (sobrescribiendo piso 1)", busId);
            } else {
                asientoLayoutRepository.deleteByBusIdAndPiso(busId, piso);
                log.info("Layout del piso {} eliminado para bus {}", piso, busId);
            }
            entityManager.flush();
            entityManager.clear();
        } else {
            // Verificar si ya existe layout para este piso específico
            Long count = asientoLayoutRepository.countByBusIdAndPiso(busId, piso);
            if (count > 0) {
                throw new IllegalStateException(
                    String.format("El piso %d del bus ya tiene un layout configurado. Use sobrescribir=true para reemplazarlo.", piso)
                );
            }
            
            // Si es piso 1 y no hay asientos en piso 1, verificar que no haya asientos huérfanos
            // que puedan causar conflictos de numeración
            if (piso == 1) {
                Long totalAsientos = asientoLayoutRepository.countByBusId(busId);
                if (totalAsientos > 0) {
                    throw new IllegalStateException(
                        "El bus tiene asientos de otros pisos. Para generar el piso 1, primero use sobrescribir=true para limpiar todos los asientos."
                    );
                }
            }
        }
        
        // Determinar capacidad según el piso
        Integer capacidad;
        Integer numeroAsientoInicio;
        
        if (Boolean.TRUE.equals(bus.getTieneDosNiveles())) {
            if (piso == 1) {
                capacidad = bus.getCapacidadPiso1();
                numeroAsientoInicio = 1;
            } else if (piso == 2) {
                capacidad = bus.getCapacidadPiso2();
                // Para el piso 2, calcular el inicio basándose en el máximo número de asiento existente
                // en lugar de la capacidad configurada (para evitar conflictos si el piso 1 tiene más asientos)
                Integer maxNumeroExistente = asientoLayoutRepository.findMaxNumeroAsientoByBusId(busId);
                if (maxNumeroExistente != null) {
                    numeroAsientoInicio = maxNumeroExistente + 1;
                } else {
                    // Si no hay asientos, usar la capacidad del piso 1 como base
                    numeroAsientoInicio = bus.getCapacidadPiso1() + 1;
                }
                log.info("Piso 2: máximo asiento existente={}, inicio desde asiento {}", maxNumeroExistente, numeroAsientoInicio);
            } else {
                throw new IllegalArgumentException("El piso debe ser 1 o 2");
            }
        } else {
            capacidad = bus.getCapacidadAsientos();
            numeroAsientoInicio = 1;
            piso = 1; // Forzar piso 1 para buses de un solo nivel
        }
        
        log.info("Generando layout para bus {}: piso={}, filas={}, columnas={}, incluirFilaTrasera={}, capacidad={}",
                 busId, piso, filas, columnas, incluirFilaTrasera, capacidad);

        // Calcular capacidad total del layout
        // Si incluye fila trasera: (filas-1) * columnas + 5 asientos traseros
        // Si no: filas * columnas
        int capacidadLayout;
        if (incluirFilaTrasera) {
            capacidadLayout = (filas - 1) * columnas + 5;
        } else {
            capacidadLayout = filas * columnas;
        }

        // Validar que el layout sea suficiente para la capacidad
        // El layout puede generar más espacios de los necesarios, pero solo se crearán asientos hasta la capacidad exacta
        if (capacidadLayout < capacidad) {
            throw new IllegalArgumentException(
                String.format("El layout (%s = %d asientos máximos) es insuficiente para la capacidad del piso (%d asientos). " +
                    "Aumenta las filas, columnas, o considera usar fila trasera.", 
                    incluirFilaTrasera ? String.format("(%d-1)×%d + 5 traseros", filas, columnas) : String.format("%d×%d", filas, columnas),
                    capacidadLayout, capacidad)
            );
        }
        
        log.info("Layout puede generar hasta {} asientos, se crearán exactamente {} según la capacidad del piso", 
                 capacidadLayout, capacidad);

        List<AsientoLayout> asientos = new ArrayList<>();
        int numeroAsiento = numeroAsientoInicio;
        int asientosGenerados = 0;

        // Determinar cuántas filas tendrán el layout normal
        int filasNormales = incluirFilaTrasera ? filas - 1 : filas;

        // Generar asientos en grid principal (todas las filas excepto la última si hay fila trasera)
        for (int fila = 0; fila < filasNormales; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                AsientoLayout asiento = AsientoLayout.builder()
                        .bus(bus)
                        .numeroAsiento(numeroAsiento)
                        .fila(fila)
                        .columna(columna)
                        .tipoAsiento("NORMAL")
                        .piso(piso)
                        .habilitado(true) // Todos habilitados por defecto
                        .build();
                asientos.add(asiento);
                numeroAsiento++;
                asientosGenerados++;
            }
        }

        // Generar fila trasera continua con 5 asientos:
        // Col 0-1: 2 izquierda, Col 2: centro (pasillo), Col 3-4: 2 derecha
        if (incluirFilaTrasera) {
            int filaFinal = filasNormales; // La última fila
            // Distribución: [0, 1, 2, 3, 4] = [izq, izq, centro, der, der]
            for (int columna = 0; columna < 5; columna++) {
                AsientoLayout asiento = AsientoLayout.builder()
                        .bus(bus)
                        .numeroAsiento(numeroAsiento)
                        .fila(filaFinal)
                        .columna(columna)
                        .tipoAsiento("NORMAL")
                        .piso(piso)
                        .habilitado(true) // Todos habilitados por defecto
                        .build();
                asientos.add(asiento);
                numeroAsiento++;
                asientosGenerados++;
            }
        }

        asientoLayoutRepository.saveAll(asientos);
        
        int asientosExcedentes = asientos.size() - capacidad;
        
        log.info("Layout generado para bus {} (piso {}): {} asientos creados - Capacidad: {} (grid {}×{} {})", 
                 busId, piso, asientos.size(), capacidad, filas, columnas, 
                 incluirFilaTrasera ? "+ fila trasera" : "");

        String mensaje = asientosExcedentes > 0 
            ? String.format("Layout generado: %d asientos creados. ATENCIÓN: Excede la capacidad en %d asiento%s. Debes deshabilitar %d asiento%s manualmente.", 
                           asientos.size(), asientosExcedentes, asientosExcedentes > 1 ? "s" : "", 
                           asientosExcedentes, asientosExcedentes > 1 ? "s" : "")
            : "Layout generado exitosamente";

        return AsientoOperationResponse.builder()
                .success(true)
                .message(mensaje)
                .asientosCreados(asientos.size())
                .asientosActualizados(0)
                .asientosHabilitados(asientos.size()) // Todos habilitados inicialmente
                .asientosDeshabilitados(0) // Ninguno deshabilitado automáticamente
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
                .piso(asiento.getPiso())
                .habilitado(asiento.getHabilitado())
                .build();
    }

    private boolean isValidTipoAsiento(String tipo) {
        return "NORMAL".equals(tipo) || "VIP".equals(tipo) || "ACONDICIONADO".equals(tipo);
    }
}
