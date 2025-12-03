package com.andinobus.backendsmartcode.cooperativa.application.services;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusRepository;
import com.andinobus.backendsmartcode.cooperativa.api.dto.CapacidadOperativaDtos.*;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.FrecuenciaConfigCooperativa;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.UsuarioCooperativa;
import com.andinobus.backendsmartcode.cooperativa.domain.enums.RolCooperativa;
import com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.FrecuenciaConfigCooperativaRepository;
import com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.UsuarioCooperativaRepository;
import com.andinobus.backendsmartcode.admin.domain.repositories.FrecuenciaViajeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Servicio para calcular la capacidad operativa de una cooperativa.
 * 
 * Reglas de negocio:
 * - Un bus puede operar 24h/día con descansos entre viajes
 * - Un chofer puede trabajar máximo 8h/día (10h excepcionales, 2 días/semana)
 * - El cuello de botella suele ser los choferes
 * - Descansos: interprovincial (~2h), intraprovincial (~45min)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CapacidadOperativaService {

    private final BusRepository busRepository;
    private final UsuarioCooperativaRepository usuarioCooperativaRepository;
    private final FrecuenciaConfigCooperativaRepository configRepository;
    private final FrecuenciaViajeRepository frecuenciaViajeRepository;

    /**
     * Calcula la capacidad operativa actual de la cooperativa
     */
    @Transactional(readOnly = true)
    public CapacidadOperativaResponse calcularCapacidad(Long cooperativaId, LocalDate fecha) {
        if (fecha == null) fecha = LocalDate.now();

        // Obtener configuración
        FrecuenciaConfigCooperativa config = configRepository.findByCooperativaId(cooperativaId)
                .orElse(crearConfiguracionDefecto());

        // Contar recursos
        List<Bus> todosLosBuses = busRepository.findByCooperativaId(cooperativaId);
        List<Bus> busesActivos = busRepository.findByCooperativaIdAndActivoTrue(cooperativaId);
        
        List<UsuarioCooperativa> choferes = usuarioCooperativaRepository
                .findByCooperativaIdAndRolCooperativaAndActivoTrue(cooperativaId, RolCooperativa.CHOFER);

        int totalBuses = todosLosBuses.size();
        int numBusesActivos = (int) busesActivos.stream()
                .filter(b -> "DISPONIBLE".equals(b.getEstado()) || "EN_SERVICIO".equals(b.getEstado()))
                .count();
        int totalChoferes = choferes.size();

        // Calcular horas disponibles por día
        int horasBusDisponiblesDia = numBusesActivos * 24;
        int horasChoferDisponiblesDia = totalChoferes * config.getMaxHorasDiariasChofer();
        
        // El cuello de botella es el mínimo
        int horasOperativasRealesDia = Math.min(horasBusDisponiblesDia, horasChoferDisponiblesDia);

        // Determinar cuello de botella
        String cuelloBotella;
        if (horasChoferDisponiblesDia < horasBusDisponiblesDia) {
            cuelloBotella = "CHOFERES";
        } else if (horasBusDisponiblesDia < horasChoferDisponiblesDia) {
            cuelloBotella = "BUSES";
        } else {
            cuelloBotella = "EQUILIBRADO";
        }

        // Calcular tiempo promedio por frecuencia (viaje + descanso)
        int tiempoPromedioFrecuenciaMin = calcularTiempoPromedioFrecuencia(config);
        
        // Máximo de frecuencias que puede ejecutar por día
        int maxFrecuenciasPorDia = (horasOperativasRealesDia * 60) / tiempoPromedioFrecuenciaMin;

        // Frecuencias actualmente programadas (activas)
        int frecuenciasProgramadas = contarFrecuenciasActivas(cooperativaId);

        // Calcular disponibilidad
        int frecuenciasDisponibles = Math.max(0, maxFrecuenciasPorDia - frecuenciasProgramadas);
        boolean hayDeficit = frecuenciasProgramadas > maxFrecuenciasPorDia;
        int deficitFrecuencias = hayDeficit ? frecuenciasProgramadas - maxFrecuenciasPorDia : 0;

        // Porcentaje de uso
        double porcentajeUso = maxFrecuenciasPorDia > 0 
                ? (double) frecuenciasProgramadas / maxFrecuenciasPorDia * 100 
                : 0;

        // Generar alertas
        List<AlertaCapacidad> alertas = generarAlertas(
                numBusesActivos, totalChoferes, frecuenciasProgramadas, 
                maxFrecuenciasPorDia, porcentajeUso, cuelloBotella
        );

        // Generar sugerencias
        List<String> sugerencias = generarSugerencias(
                numBusesActivos, totalChoferes, hayDeficit, cuelloBotella, config
        );

        // Construir configuración resumida
        ConfiguracionOperativa configResumen = ConfiguracionOperativa.builder()
                .horasMaxChoferDia(config.getMaxHorasDiariasChofer())
                .horasMaxExcepcionales(config.getMaxHorasExcepcionales())
                .diasExcepcionalesSemana(config.getMaxDiasExcepcionalesSemana())
                .descansoInterprovincialMin(config.getDescansoInterprovincialMinutos())
                .descansoIntraprovincialMin(config.getDescansoIntraprovincialMinutos())
                .umbralInterprovincialKm(config.getUmbralInterprovincialKm())
                .tiempoPromedioFrecuenciaMin(tiempoPromedioFrecuenciaMin)
                .build();

        return CapacidadOperativaResponse.builder()
                .cooperativaId(cooperativaId)
                .fecha(fecha)
                .totalBuses(totalBuses)
                .busesActivos(numBusesActivos)
                .totalChoferes(totalChoferes)
                .choferesActivos(totalChoferes)
                .horasBusDisponiblesDia(horasBusDisponiblesDia)
                .horasChoferDisponiblesDia(horasChoferDisponiblesDia)
                .horasOperativasRealesDia(horasOperativasRealesDia)
                .maxFrecuenciasPorDia(maxFrecuenciasPorDia)
                .frecuenciasProgramadas(frecuenciasProgramadas)
                .frecuenciasDisponibles(frecuenciasDisponibles)
                .hayDeficit(hayDeficit)
                .deficitFrecuencias(deficitFrecuencias)
                .porcentajeUsoCapacidad(Math.round(porcentajeUso * 10) / 10.0)
                .cuelloBotella(cuelloBotella)
                .configuracion(configResumen)
                .alertas(alertas)
                .sugerencias(sugerencias)
                .semanasPlanificacionDefecto(config.getSemanasPlanificacionDefecto())
                .semanasPlanificacionMax(config.getSemanasPlanificacionMax())
                .build();
    }

    /**
     * Valida si se puede generar un número específico de frecuencias
     */
    @Transactional(readOnly = true)
    public ValidacionGeneracionResponse validarGeneracion(
            Long cooperativaId, 
            int frecuenciasSolicitadas,
            int semanas
    ) {
        CapacidadOperativaResponse capacidad = calcularCapacidad(cooperativaId, LocalDate.now());
        
        int frecuenciasMaxPosibles = capacidad.getMaxFrecuenciasPorDia() * 7 * semanas;
        int frecuenciasActuales = capacidad.getFrecuenciasProgramadas() * 7 * semanas;
        int espacioDisponible = Math.max(0, frecuenciasMaxPosibles - frecuenciasActuales);
        
        int frecuenciasQueSeCrearan = Math.min(frecuenciasSolicitadas, espacioDisponible);
        int frecuenciasQueNoSeCrearan = frecuenciasSolicitadas - frecuenciasQueSeCrearan;
        
        boolean puedeGenerar = frecuenciasQueSeCrearan > 0;
        
        List<AlertaCapacidad> alertas = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();
        
        if (frecuenciasQueNoSeCrearan > 0) {
            alertas.add(AlertaCapacidad.builder()
                    .tipo("WARNING")
                    .codigo("CAPACIDAD_LIMITADA")
                    .titulo("Capacidad limitada")
                    .mensaje(String.format(
                            "Solo se pueden crear %d de %d frecuencias solicitadas. " +
                            "El cuello de botella son los %s.",
                            frecuenciasQueSeCrearan, frecuenciasSolicitadas, 
                            capacidad.getCuelloBotella().toLowerCase()))
                    .prioridad(1)
                    .accionSugerida(capacidad.getCuelloBotella().equals("CHOFERES") 
                            ? "Considere contratar más choferes" 
                            : "Considere agregar más buses")
                    .build());
        }
        
        if (capacidad.isHayDeficit()) {
            alertas.add(AlertaCapacidad.builder()
                    .tipo("ERROR")
                    .codigo("DEFICIT_EXISTENTE")
                    .titulo("Déficit de capacidad")
                    .mensaje(String.format(
                            "Ya existe un déficit de %d frecuencias. " +
                            "Las frecuencias actuales superan la capacidad operativa.",
                            capacidad.getDeficitFrecuencias()))
                    .prioridad(1)
                    .accionSugerida("Reduzca frecuencias activas o aumente recursos")
                    .build());
        }
        
        if (semanas > capacidad.getSemanasPlanificacionMax()) {
            advertencias.add(String.format(
                    "El período máximo de planificación es %d semanas", 
                    capacidad.getSemanasPlanificacionMax()));
        }
        
        String resumen = String.format(
                "Con %d buses y %d choferes puede ejecutar máximo %d frecuencias/día. " +
                "Actualmente tiene %d programadas, dejando espacio para %d más.",
                capacidad.getBusesActivos(), capacidad.getTotalChoferes(),
                capacidad.getMaxFrecuenciasPorDia(), capacidad.getFrecuenciasProgramadas(),
                capacidad.getFrecuenciasDisponibles());

        return ValidacionGeneracionResponse.builder()
                .puedeGenerar(puedeGenerar)
                .frecuenciasSolicitadas(frecuenciasSolicitadas)
                .frecuenciasMaxPosibles(frecuenciasMaxPosibles)
                .frecuenciasQueSeCrearan(frecuenciasQueSeCrearan)
                .frecuenciasQueNoSeCrearan(frecuenciasQueNoSeCrearan)
                .capacidadActual(capacidad)
                .alertas(alertas)
                .advertencias(advertencias)
                .resumenCapacidad(resumen)
                .build();
    }

    /**
     * Obtiene el resumen para mostrar en el modal de generación
     */
    @Transactional(readOnly = true)
    public ResumenCapacidadModal obtenerResumenParaModal(Long cooperativaId, int semanas) {
        CapacidadOperativaResponse capacidad = calcularCapacidad(cooperativaId, LocalDate.now());
        
        double ratioChoferBus = capacidad.getBusesActivos() > 0 
                ? (double) capacidad.getTotalChoferes() / capacidad.getBusesActivos() 
                : 0;
        
        int frecuenciasTotalesGenerables = capacidad.getFrecuenciasDisponibles() * 7 * semanas;
        
        String estadoCapacidad;
        String mensajeEstado;
        String colorEstado;
        
        if (capacidad.isHayDeficit()) {
            estadoCapacidad = "DEFICIT";
            mensajeEstado = "⚠️ Capacidad sobrepasada. Reduzca frecuencias o aumente recursos.";
            colorEstado = "red";
        } else if (capacidad.getPorcentajeUsoCapacidad() >= 80) {
            estadoCapacidad = "LIMITADO";
            mensajeEstado = "⚡ Capacidad casi al límite. Puede agregar pocas frecuencias más.";
            colorEstado = "yellow";
        } else {
            estadoCapacidad = "OPTIMO";
            mensajeEstado = "✅ Capacidad disponible para agregar frecuencias.";
            colorEstado = "green";
        }
        
        String recomendacion;
        if (capacidad.getCuelloBotella().equals("CHOFERES")) {
            int choferesIdeales = (int) Math.ceil(capacidad.getBusesActivos() * 3.0);
            recomendacion = String.format(
                    "Para operar los %d buses a capacidad completa (3 turnos), necesita %d choferes. " +
                    "Actualmente tiene %d.",
                    capacidad.getBusesActivos(), choferesIdeales, capacidad.getTotalChoferes());
        } else if (capacidad.getCuelloBotella().equals("BUSES")) {
            recomendacion = String.format(
                    "Tiene suficientes choferes para operar más buses. " +
                    "Considere agregar buses a la flota.");
        } else {
            recomendacion = "Recursos equilibrados. Puede agregar buses y choferes proporcionalmente.";
        }

        return ResumenCapacidadModal.builder()
                .buses(capacidad.getBusesActivos())
                .choferes(capacidad.getTotalChoferes())
                .ratioChoferBus(String.format("%.1f", ratioChoferBus))
                .frecuenciasMaxDia(capacidad.getMaxFrecuenciasPorDia())
                .frecuenciasActualesDia(capacidad.getFrecuenciasProgramadas())
                .frecuenciasDisponiblesDia(capacidad.getFrecuenciasDisponibles())
                .semanasSeleccionadas(semanas)
                .frecuenciasTotalesGenerables(frecuenciasTotalesGenerables)
                .frecuenciasQueSeGeneraran(0) // Se calcula después según selección
                .estadoCapacidad(estadoCapacidad)
                .mensajeEstado(mensajeEstado)
                .colorEstado(colorEstado)
                .cuelloBotellaBuses(capacidad.getCuelloBotella().equals("BUSES"))
                .cuelloBotellaCuósteres(capacidad.getCuelloBotella().equals("CHOFERES"))
                .recomendacion(recomendacion)
                .build();
    }

    /**
     * Calcula capacidad por semanas
     */
    @Transactional(readOnly = true)
    public CapacidadSemanalResponse calcularCapacidadSemanal(Long cooperativaId, LocalDate fechaInicio, int semana) {
        List<CapacidadDiariaResumen> dias = new ArrayList<>();
        LocalDate fechaFin = fechaInicio.plusDays(7);
        
        int totalFrecuenciasPosibles = 0;
        int frecuenciasProgramadas = 0;
        
        for (LocalDate fecha = fechaInicio; fecha.isBefore(fechaFin); fecha = fecha.plusDays(1)) {
            CapacidadOperativaResponse cap = calcularCapacidad(cooperativaId, fecha);
            
            String diaSemana = fecha.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            
            String icono;
            if (cap.isHayDeficit()) {
                icono = "❌";
            } else if (cap.getPorcentajeUsoCapacidad() >= 80) {
                icono = "⚠️";
            } else {
                icono = "✅";
            }
            
            dias.add(CapacidadDiariaResumen.builder()
                    .fecha(fecha)
                    .diaSemana(diaSemana)
                    .maxFrecuencias(cap.getMaxFrecuenciasPorDia())
                    .frecuenciasProgramadas(cap.getFrecuenciasProgramadas())
                    .disponibles(cap.getFrecuenciasDisponibles())
                    .porcentajeUso(cap.getPorcentajeUsoCapacidad())
                    .hayDeficit(cap.isHayDeficit())
                    .estadoIcono(icono)
                    .build());
            
            totalFrecuenciasPosibles += cap.getMaxFrecuenciasPorDia();
            frecuenciasProgramadas += cap.getFrecuenciasProgramadas();
        }
        
        List<AlertaCapacidad> alertas = new ArrayList<>();
        long diasConDeficit = dias.stream().filter(CapacidadDiariaResumen::isHayDeficit).count();
        if (diasConDeficit > 0) {
            alertas.add(AlertaCapacidad.builder()
                    .tipo("WARNING")
                    .codigo("DIAS_CON_DEFICIT")
                    .titulo("Días con déficit")
                    .mensaje(String.format("Hay %d días con déficit de capacidad esta semana", diasConDeficit))
                    .prioridad(1)
                    .build());
        }

        return CapacidadSemanalResponse.builder()
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin.minusDays(1))
                .semana(semana)
                .totalFrecuenciasPosibles(totalFrecuenciasPosibles)
                .frecuenciasProgramadas(frecuenciasProgramadas)
                .frecuenciasDisponibles(totalFrecuenciasPosibles - frecuenciasProgramadas)
                .dias(dias)
                .alertas(alertas)
                .build();
    }

    // === MÉTODOS AUXILIARES ===

    private int calcularTiempoPromedioFrecuencia(FrecuenciaConfigCooperativa config) {
        // Promedio entre viaje interprovincial e intraprovincial
        // Asumimos ~2h viaje promedio + descanso promedio
        int duracionPromedio = 120; // 2 horas promedio de viaje
        int descansoPromedio = (config.getDescansoInterprovincialMinutos() + 
                config.getDescansoIntraprovincialMinutos()) / 2;
        return duracionPromedio + descansoPromedio;
    }

    private int contarFrecuenciasActivas(Long cooperativaId) {
        try {
            return frecuenciaViajeRepository.findByBusCooperativaIdAndActivoTrue(cooperativaId).size();
        } catch (Exception e) {
            log.warn("Error contando frecuencias activas: {}", e.getMessage());
            return 0;
        }
    }

    private List<AlertaCapacidad> generarAlertas(
            int buses, int choferes, int frecuenciasProgramadas,
            int maxFrecuencias, double porcentajeUso, String cuelloBotella
    ) {
        List<AlertaCapacidad> alertas = new ArrayList<>();

        if (choferes == 0) {
            alertas.add(AlertaCapacidad.builder()
                    .tipo("ERROR")
                    .codigo("SIN_CHOFERES")
                    .titulo("Sin choferes")
                    .mensaje("No hay choferes registrados. Debe registrar al menos un chofer.")
                    .prioridad(1)
                    .accionSugerida("Ir a Personal > Agregar Chofer")
                    .build());
        }

        if (buses == 0) {
            alertas.add(AlertaCapacidad.builder()
                    .tipo("ERROR")
                    .codigo("SIN_BUSES")
                    .titulo("Sin buses")
                    .mensaje("No hay buses activos. Debe registrar y activar al menos un bus.")
                    .prioridad(1)
                    .accionSugerida("Ir a Buses > Agregar Bus")
                    .build());
        }

        if (frecuenciasProgramadas > maxFrecuencias) {
            alertas.add(AlertaCapacidad.builder()
                    .tipo("ERROR")
                    .codigo("DEFICIT_CAPACIDAD")
                    .titulo("Déficit de capacidad")
                    .mensaje(String.format(
                            "Tiene %d frecuencias pero solo puede ejecutar %d. " +
                            "El cuello de botella son los %s.",
                            frecuenciasProgramadas, maxFrecuencias, cuelloBotella.toLowerCase()))
                    .prioridad(1)
                    .accionSugerida(cuelloBotella.equals("CHOFERES") 
                            ? "Contrate más choferes o reduzca frecuencias" 
                            : "Agregue más buses o reduzca frecuencias")
                    .build());
        } else if (porcentajeUso >= 90) {
            alertas.add(AlertaCapacidad.builder()
                    .tipo("WARNING")
                    .codigo("CAPACIDAD_CRITICA")
                    .titulo("Capacidad crítica")
                    .mensaje(String.format("Capacidad al %.1f%%. Casi no puede agregar más frecuencias.", porcentajeUso))
                    .prioridad(2)
                    .accionSugerida("Considere aumentar recursos antes de agregar más frecuencias")
                    .build());
        } else if (porcentajeUso >= 80) {
            alertas.add(AlertaCapacidad.builder()
                    .tipo("WARNING")
                    .codigo("CAPACIDAD_ALTA")
                    .titulo("Uso alto de capacidad")
                    .mensaje(String.format("Capacidad al %.1f%%. Queda poco margen.", porcentajeUso))
                    .prioridad(3)
                    .build());
        }

        if (choferes > 0 && buses > 0 && choferes < buses) {
            alertas.add(AlertaCapacidad.builder()
                    .tipo("INFO")
                    .codigo("RATIO_BAJO")
                    .titulo("Pocos choferes por bus")
                    .mensaje(String.format(
                            "Tiene %d choferes para %d buses (ratio %.1f). " +
                            "Idealmente debería tener 2-3 choferes por bus para turnos completos.",
                            choferes, buses, (double) choferes / buses))
                    .prioridad(3)
                    .accionSugerida("Considere contratar más choferes")
                    .build());
        }

        return alertas;
    }

    private List<String> generarSugerencias(
            int buses, int choferes, boolean hayDeficit, 
            String cuelloBotella, FrecuenciaConfigCooperativa config
    ) {
        List<String> sugerencias = new ArrayList<>();

        if (hayDeficit) {
            if (cuelloBotella.equals("CHOFERES")) {
                int choferesNecesarios = (int) Math.ceil((buses * 24.0) / config.getMaxHorasDiariasChofer());
                int choferesAdicionales = choferesNecesarios - choferes;
                if (choferesAdicionales > 0) {
                    sugerencias.add(String.format(
                            "Contrate %d choferes adicionales para operar los buses a capacidad completa",
                            choferesAdicionales));
                }
            } else {
                sugerencias.add("Considere adquirir más buses para cubrir la demanda");
            }
            sugerencias.add("Reduzca el número de frecuencias activas para ajustarse a la capacidad");
        }

        if (!hayDeficit && choferes > 0 && buses > 0) {
            double ratioChoferBus = (double) choferes / buses;
            if (ratioChoferBus < 2) {
                sugerencias.add(String.format(
                        "Para aprovechar mejor los buses, considere tener al menos %d choferes (2 por bus)",
                        buses * 2));
            }
            if (ratioChoferBus >= 3) {
                sugerencias.add("Tiene buen ratio de choferes. Podría agregar más buses si la demanda lo requiere.");
            }
        }

        return sugerencias;
    }

    private FrecuenciaConfigCooperativa crearConfiguracionDefecto() {
        return FrecuenciaConfigCooperativa.builder()
                .maxHorasDiariasChofer(8)
                .maxHorasExcepcionales(10)
                .maxDiasExcepcionalesSemana(2)
                .descansoInterprovincialMinutos(120)
                .descansoIntraprovincialMinutos(45)
                .umbralInterprovincialKm(100.0)
                .semanasPlanificacionDefecto(1)
                .semanasPlanificacionMax(4)
                .tiempoDescansoEntreViajesMinutos(30)
                .build();
    }
}
