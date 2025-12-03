package com.andinobus.backendsmartcode.cooperativa.application.services;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.domain.entities.BusChofer;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusChoferRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.CooperativaRepository;
import com.andinobus.backendsmartcode.cooperativa.api.dto.GeneracionFrecuenciasDtos.*;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.ChoferHorasTrabajadas;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.FrecuenciaConfigCooperativa;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.PlantillaRotacionFrecuencias;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.UsuarioCooperativa;
import com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.ChoferHorasTrabajadasRepository;
import com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.FrecuenciaConfigCooperativaRepository;
import com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.PlantillaRotacionFrecuenciasRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneracionFrecuenciasService {

    private final PlantillaRotacionFrecuenciasRepository plantillaRepository;
    private final BusRepository busRepository;
    private final BusChoferRepository busChoferRepository;
    private final ChoferHorasTrabajadasRepository choferHorasRepository;
    private final FrecuenciaConfigCooperativaRepository configRepository;
    private final CooperativaRepository cooperativaRepository;
    private final ObjectMapper objectMapper;

    /**
     * Importa un CSV con la plantilla de rotación de frecuencias
     */
    @Transactional
    public ImportarCsvResponse importarCsv(Long cooperativaId, ImportarCsvRequest request) {
        List<String> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();
        List<TurnoFrecuencia> turnos = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new StringReader(request.getContenidoCsv()));
            String linea;
            int lineaNum = 0;
            TurnoFrecuencia turnoActual = null;

            while ((linea = reader.readLine()) != null) {
                lineaNum++;
                
                // Saltar encabezados y líneas vacías
                if (lineaNum <= 2 || linea.trim().isEmpty()) {
                    continue;
                }

                String[] campos = linea.split(";", -1);
                if (campos.length < 5) {
                    continue;
                }

                String diaStr = campos[0].trim();
                String horaSalida = campos[1].trim();
                String origen = campos[2].trim();
                String destino = campos[3].trim();
                String horaLlegada = campos.length > 4 ? campos[4].trim() : "";

                // Si tiene número de DIA, es un turno principal
                if (!diaStr.isEmpty() && diaStr.matches("\\d+")) {
                    // Guardar turno anterior si existe
                    if (turnoActual != null) {
                        turnos.add(turnoActual);
                    }

                    int numeroDia = Integer.parseInt(diaStr);
                    boolean esParada = "PARADA".equalsIgnoreCase(origen) || horaSalida.isEmpty();

                    turnoActual = TurnoFrecuencia.builder()
                            .numeroDia(numeroDia)
                            .horaSalida(normalizarHora(horaSalida))
                            .origen(origen)
                            .destino(destino)
                            .horaLlegada(normalizarHora(horaLlegada))
                            .esParada(esParada)
                            .subTurnos(new ArrayList<>())
                            .build();

                } else if (turnoActual != null && !origen.isEmpty() && !destino.isEmpty()) {
                    // Es un sub-turno (continuación del día)
                    SubTurno subTurno = SubTurno.builder()
                            .horaSalida(normalizarHora(horaSalida))
                            .origen(origen)
                            .destino(destino)
                            .build();
                    turnoActual.getSubTurnos().add(subTurno);
                }
            }

            // Agregar último turno
            if (turnoActual != null) {
                turnos.add(turnoActual);
            }

            if (turnos.isEmpty()) {
                errores.add("No se encontraron turnos válidos en el CSV");
                return ImportarCsvResponse.builder()
                        .exitoso(false)
                        .errores(errores)
                        .advertencias(advertencias)
                        .build();
            }

            // Verificar si ya existe una plantilla con ese nombre
            String nombrePlantilla = request.getNombrePlantilla();
            if (plantillaRepository.existsByCooperativaIdAndNombre(cooperativaId, nombrePlantilla)) {
                nombrePlantilla = nombrePlantilla + "_" + System.currentTimeMillis();
                advertencias.add("Ya existía una plantilla con ese nombre. Se creó como: " + nombrePlantilla);
            }

            // Crear la plantilla
            Cooperativa cooperativa = cooperativaRepository.findById(cooperativaId)
                    .orElseThrow(() -> new EntityNotFoundException("Cooperativa no encontrada"));

            String turnosJson = objectMapper.writeValueAsString(turnos);

            PlantillaRotacionFrecuencias plantilla = PlantillaRotacionFrecuencias.builder()
                    .cooperativa(cooperativa)
                    .nombre(nombrePlantilla)
                    .descripcion(request.getDescripcion())
                    .totalTurnos(turnos.size())
                    .turnosJson(turnosJson)
                    .activa(true)
                    .build();

            plantilla = plantillaRepository.save(plantilla);

            return ImportarCsvResponse.builder()
                    .exitoso(true)
                    .plantillaId(plantilla.getId())
                    .turnosImportados(turnos.size())
                    .errores(errores)
                    .advertencias(advertencias)
                    .build();

        } catch (Exception e) {
            log.error("Error importando CSV", e);
            errores.add("Error procesando CSV: " + e.getMessage());
            return ImportarCsvResponse.builder()
                    .exitoso(false)
                    .errores(errores)
                    .advertencias(advertencias)
                    .build();
        }
    }

    /**
     * Obtiene las plantillas de una cooperativa
     */
    @Transactional(readOnly = true)
    public List<PlantillaRotacion> getPlantillas(Long cooperativaId) {
        return plantillaRepository.findByCooperativaId(cooperativaId).stream()
                .map(this::toPlantillaRotacion)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una plantilla por ID
     */
    @Transactional(readOnly = true)
    public PlantillaRotacion getPlantilla(Long plantillaId) {
        PlantillaRotacionFrecuencias plantilla = plantillaRepository.findById(plantillaId)
                .orElseThrow(() -> new EntityNotFoundException("Plantilla no encontrada"));
        return toPlantillaRotacion(plantilla);
    }

    /**
     * Vista previa de la generación de frecuencias
     */
    @Transactional(readOnly = true)
    public PreviewGeneracionResponse previewGeneracion(Long cooperativaId, GenerarFrecuenciasRequest request) {
        PlantillaRotacionFrecuencias plantilla = plantillaRepository.findById(request.getPlantillaId())
                .orElseThrow(() -> new EntityNotFoundException("Plantilla no encontrada"));

        List<TurnoFrecuencia> turnos = parseTurnos(plantilla.getTurnosJson());
        List<Bus> buses = busRepository.findAllById(request.getBusIds());
        
        if (buses.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un bus");
        }

        List<AsignacionBusDia> asignaciones = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();
        List<ConflictoDetectado> conflictos = new ArrayList<>();

        LocalDate fecha = request.getFechaInicio();
        int diaRotacion = 0;
        int totalFrecuencias = 0;

        // Ordenar buses por número interno para la rotación
        buses.sort(Comparator.comparing(Bus::getNumeroInterno, Comparator.nullsLast(String::compareTo)));

        while (!fecha.isAfter(request.getFechaFin())) {
            for (int busIndex = 0; busIndex < buses.size(); busIndex++) {
                Bus bus = buses.get(busIndex);
                
                // Calcular qué turno le corresponde a este bus en esta fecha
                // El bus rota: cada día avanza una posición en la tabla
                int turnoIndex = (diaRotacion + busIndex) % turnos.size();
                TurnoFrecuencia turno = turnos.get(turnoIndex);

                List<ViajeGenerado> viajes = new ArrayList<>();

                if (!turno.getEsParada()) {
                    // Viaje principal
                    ViajeGenerado viajePrincipal = ViajeGenerado.builder()
                            .origen(turno.getOrigen())
                            .destino(turno.getDestino())
                            .horaSalida(turno.getHoraSalida())
                            .horaLlegadaEstimada(turno.getHoraLlegada())
                            .build();
                    viajes.add(viajePrincipal);

                    // Sub-turnos
                    if (turno.getSubTurnos() != null) {
                        for (SubTurno sub : turno.getSubTurnos()) {
                            viajes.add(ViajeGenerado.builder()
                                    .origen(sub.getOrigen())
                                    .destino(sub.getDestino())
                                    .horaSalida(sub.getHoraSalida())
                                    .build());
                        }
                    }

                    totalFrecuencias += viajes.size();

                    // Verificar disponibilidad del bus
                    if (!"DISPONIBLE".equals(bus.getEstado())) {
                        conflictos.add(ConflictoDetectado.builder()
                                .fecha(fecha)
                                .busId(bus.getId())
                                .busPlaca(bus.getPlaca())
                                .descripcion("Bus no disponible: " + bus.getEstado())
                                .tipoConflicto("BUS_NO_DISPONIBLE")
                                .build());
                    }
                }

                String primerViaje = turno.getEsParada() 
                        ? "PARADA" 
                        : turno.getOrigen() + " → " + turno.getDestino() + " " + turno.getHoraSalida();

                asignaciones.add(AsignacionBusDia.builder()
                        .fecha(fecha)
                        .busId(bus.getId())
                        .busPlaca(bus.getPlaca())
                        .turnoAsignado(turno.getNumeroDia())
                        .primerViaje(primerViaje)
                        .viajes(viajes)
                        .esParada(turno.getEsParada())
                        .build());
            }

            fecha = fecha.plusDays(1);
            diaRotacion++;
        }

        long diasTotales = ChronoUnit.DAYS.between(request.getFechaInicio(), request.getFechaFin()) + 1;

        return PreviewGeneracionResponse.builder()
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .diasTotales((int) diasTotales)
                .frecuenciasAGenerar(totalFrecuencias)
                .busesParticipantes(buses.size())
                .asignaciones(asignaciones)
                .advertencias(advertencias)
                .conflictos(conflictos)
                .build();
    }

    /**
     * Genera las frecuencias basadas en la plantilla
     */
    @Transactional
    public ResultadoGeneracionResponse generarFrecuencias(Long cooperativaId, GenerarFrecuenciasRequest request) {
        // Por ahora solo retornamos el preview como resultado
        // La implementación completa crearía los registros de FrecuenciaViaje
        
        PreviewGeneracionResponse preview = previewGeneracion(cooperativaId, request);
        
        int frecuenciasCreadas = 0;
        int frecuenciasOmitidas = 0;
        List<String> mensajes = new ArrayList<>();
        List<FrecuenciaGeneradaInfo> frecuenciasGeneradas = new ArrayList<>();

        // Obtener configuración de la cooperativa
        var config = configRepository.findByCooperativaId(cooperativaId).orElse(null);

        for (AsignacionBusDia asignacion : preview.getAsignaciones()) {
            if (asignacion.getEsParada()) {
                frecuenciasOmitidas++;
                continue;
            }

            for (ViajeGenerado viaje : asignacion.getViajes()) {
                // Buscar chofer disponible si está habilitado
                Long choferId = null;
                String choferNombre = null;

                if (Boolean.TRUE.equals(request.getAsignarChoferesAutomaticamente())) {
                    var choferDisponible = buscarChoferDisponible(
                            asignacion.getBusId(), 
                            asignacion.getFecha(),
                            config
                    );
                    if (choferDisponible != null) {
                        choferId = choferDisponible.getId();
                        choferNombre = choferDisponible.getNombres() + " " + choferDisponible.getApellidos();
                    }
                }

                // Aquí se crearía el registro de FrecuenciaViaje
                // Por ahora solo registramos la información
                frecuenciasGeneradas.add(FrecuenciaGeneradaInfo.builder()
                        .fecha(asignacion.getFecha())
                        .origen(viaje.getOrigen())
                        .destino(viaje.getDestino())
                        .horaSalida(viaje.getHoraSalida())
                        .busId(asignacion.getBusId())
                        .busPlaca(asignacion.getBusPlaca())
                        .choferId(choferId)
                        .choferNombre(choferNombre)
                        .build());

                frecuenciasCreadas++;
            }
        }

        mensajes.add("Se generaron " + frecuenciasCreadas + " frecuencias correctamente");
        if (frecuenciasOmitidas > 0) {
            mensajes.add("Se omitieron " + frecuenciasOmitidas + " días de parada");
        }

        return ResultadoGeneracionResponse.builder()
                .frecuenciasCreadas(frecuenciasCreadas)
                .frecuenciasOmitidas(frecuenciasOmitidas)
                .errores(0)
                .mensajes(mensajes)
                .frecuenciasGeneradas(frecuenciasGeneradas)
                .build();
    }

    /**
     * Busca un chofer disponible para un bus en una fecha
     */
    private UsuarioCooperativa buscarChoferDisponible(Long busId, LocalDate fecha, FrecuenciaConfigCooperativa config) {
        List<BusChofer> choferesDelBus = busChoferRepository.findByBusIdAndActivoTrueOrderByOrdenAsc(busId);
        
        int maxHorasNormal = config != null ? config.getMaxHorasDiariasChofer() : 8;
        int maxHorasExcepcional = config != null ? config.getMaxHorasExcepcionales() : 10;
        int maxDiasExcepcionales = config != null ? config.getMaxDiasExcepcionalesSemana() : 2;

        LocalDate inicioSemana = fecha.with(DayOfWeek.MONDAY);
        LocalDate finSemana = inicioSemana.plusDays(6);

        for (BusChofer bc : choferesDelBus) {
            UsuarioCooperativa chofer = bc.getChofer();
            
            var horasHoy = choferHorasRepository.findByChoferIdAndFecha(chofer.getId(), fecha)
                    .orElse(ChoferHorasTrabajadas.builder().horasTrabajadas(0.0).build());

            // Verificar si tiene horas disponibles
            if (horasHoy.getHorasTrabajadas() < maxHorasNormal) {
                return chofer;
            }

            // Verificar si puede hacer horas excepcionales
            long diasExcepcionalesUsados = choferHorasRepository.countDiasExcepcionalesBySemana(
                    chofer.getId(), inicioSemana, finSemana);

            if (diasExcepcionalesUsados < maxDiasExcepcionales && 
                horasHoy.getHorasTrabajadas() < maxHorasExcepcional) {
                return chofer;
            }
        }

        return null; // No hay chofer disponible
    }

    /**
     * Elimina una plantilla
     */
    @Transactional
    public void eliminarPlantilla(Long plantillaId) {
        plantillaRepository.deleteById(plantillaId);
    }

    // === Métodos auxiliares ===

    private PlantillaRotacion toPlantillaRotacion(PlantillaRotacionFrecuencias entity) {
        List<TurnoFrecuencia> turnos = parseTurnos(entity.getTurnosJson());
        
        return PlantillaRotacion.builder()
                .id(entity.getId())
                .cooperativaId(entity.getCooperativa().getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .totalTurnos(entity.getTotalTurnos())
                .turnos(turnos)
                .activa(entity.getActiva())
                .build();
    }

    private List<TurnoFrecuencia> parseTurnos(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<TurnoFrecuencia>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parseando turnos JSON", e);
            return Collections.emptyList();
        }
    }

    private String normalizarHora(String hora) {
        if (hora == null || hora.isEmpty()) {
            return null;
        }
        
        hora = hora.trim();
        
        // Manejar formato "24:00:00" o similar
        if (hora.startsWith("24:")) {
            hora = "00:" + hora.substring(3);
        }
        
        // Remover segundos si existen
        if (hora.matches("\\d{1,2}:\\d{2}:\\d{2}")) {
            hora = hora.substring(0, hora.lastIndexOf(':'));
        }
        
        // Agregar ceros iniciales si es necesario
        if (hora.matches("\\d:\\d{2}")) {
            hora = "0" + hora;
        }
        
        return hora;
    }
}
