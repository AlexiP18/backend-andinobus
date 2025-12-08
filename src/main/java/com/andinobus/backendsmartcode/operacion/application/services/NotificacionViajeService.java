package com.andinobus.backendsmartcode.operacion.application.services;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import com.andinobus.backendsmartcode.operacion.domain.entities.NotificacionViaje;
import com.andinobus.backendsmartcode.operacion.domain.entities.Viaje;
import com.andinobus.backendsmartcode.operacion.domain.repositories.NotificacionViajeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio para gestionar las notificaciones de viajes a las cooperativas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionViajeService {

    private final NotificacionViajeRepository notificacionRepository;

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Crea una notificación cuando un viaje es iniciado
     */
    @Transactional
    public NotificacionViaje notificarViajeIniciado(Viaje viaje) {
        Cooperativa cooperativa = viaje.getBus().getCooperativa();
        if (cooperativa == null) {
            log.warn("No se pudo crear notificación: el viaje {} no tiene cooperativa asociada", viaje.getId());
            return null;
        }

        String choferNombre = viaje.getChofer() != null ? 
                viaje.getChofer().getNombreCompleto() : "No asignado";
        String horaSalida = viaje.getHoraSalidaReal() != null ? 
                viaje.getHoraSalidaReal().format(HORA_FORMATTER) : "N/A";

        String titulo = String.format("🚌 Viaje Iniciado: %s → %s", 
                viaje.getFrecuencia().getOrigen(), 
                viaje.getFrecuencia().getDestino());

        String mensaje = String.format(
                "El chofer %s ha iniciado el viaje a las %s.\n" +
                "Bus: %s | Ruta: %s → %s",
                choferNombre,
                horaSalida,
                viaje.getBus().getPlaca(),
                viaje.getFrecuencia().getOrigen(),
                viaje.getFrecuencia().getDestino()
        );

        String detalleViaje = String.format(
                "Viaje ID: %d | Fecha: %s | Bus: %s | Chofer: %s | Origen: %s | Destino: %s | Hora salida: %s",
                viaje.getId(),
                viaje.getFecha(),
                viaje.getBus().getPlaca(),
                choferNombre,
                viaje.getFrecuencia().getOrigen(),
                viaje.getFrecuencia().getDestino(),
                horaSalida
        );

        NotificacionViaje notificacion = NotificacionViaje.builder()
                .viaje(viaje)
                .cooperativa(cooperativa)
                .tipo("VIAJE_INICIADO")
                .titulo(titulo)
                .mensaje(mensaje)
                .detalleViaje(detalleViaje)
                .leida(false)
                .fechaCreacion(LocalDateTime.now())
                .build();

        notificacion = notificacionRepository.save(notificacion);
        log.info("Notificación de viaje iniciado creada: ID={}, Viaje={}, Cooperativa={}", 
                notificacion.getId(), viaje.getId(), cooperativa.getId());

        return notificacion;
    }

    /**
     * Crea una notificación cuando un viaje es finalizado
     */
    @Transactional
    public NotificacionViaje notificarViajeFinalizado(Viaje viaje, String observaciones) {
        Cooperativa cooperativa = viaje.getBus().getCooperativa();
        if (cooperativa == null) {
            log.warn("No se pudo crear notificación: el viaje {} no tiene cooperativa asociada", viaje.getId());
            return null;
        }

        String choferNombre = viaje.getChofer() != null ? 
                viaje.getChofer().getNombreCompleto() : "No asignado";
        String horaLlegada = viaje.getHoraLlegadaReal() != null ? 
                viaje.getHoraLlegadaReal().format(HORA_FORMATTER) : "N/A";

        String titulo = String.format("✅ Viaje Completado: %s → %s", 
                viaje.getFrecuencia().getOrigen(), 
                viaje.getFrecuencia().getDestino());

        String mensaje = String.format(
                "El chofer %s ha completado el viaje a las %s.\n" +
                "Bus: %s | Ruta: %s → %s%s",
                choferNombre,
                horaLlegada,
                viaje.getBus().getPlaca(),
                viaje.getFrecuencia().getOrigen(),
                viaje.getFrecuencia().getDestino(),
                observaciones != null && !observaciones.isEmpty() ? 
                        "\nObservaciones: " + observaciones : ""
        );

        String detalleViaje = String.format(
                "Viaje ID: %d | Fecha: %s | Bus: %s | Chofer: %s | Origen: %s | Destino: %s | " +
                "Hora salida real: %s | Hora llegada: %s | Observaciones: %s",
                viaje.getId(),
                viaje.getFecha(),
                viaje.getBus().getPlaca(),
                choferNombre,
                viaje.getFrecuencia().getOrigen(),
                viaje.getFrecuencia().getDestino(),
                viaje.getHoraSalidaReal() != null ? viaje.getHoraSalidaReal().format(HORA_FORMATTER) : "N/A",
                horaLlegada,
                observaciones != null ? observaciones : "Ninguna"
        );

        NotificacionViaje notificacion = NotificacionViaje.builder()
                .viaje(viaje)
                .cooperativa(cooperativa)
                .tipo("VIAJE_FINALIZADO")
                .titulo(titulo)
                .mensaje(mensaje)
                .detalleViaje(detalleViaje)
                .leida(false)
                .fechaCreacion(LocalDateTime.now())
                .build();

        notificacion = notificacionRepository.save(notificacion);
        log.info("Notificación de viaje finalizado creada: ID={}, Viaje={}, Cooperativa={}", 
                notificacion.getId(), viaje.getId(), cooperativa.getId());

        return notificacion;
    }

    /**
     * Obtiene todas las notificaciones de una cooperativa
     */
    @Transactional(readOnly = true)
    public List<NotificacionViaje> getNotificacionesCooperativa(Long cooperativaId) {
        return notificacionRepository.findByCooperativaIdOrderByFechaCreacionDesc(cooperativaId);
    }

    /**
     * Obtiene las notificaciones paginadas de una cooperativa
     */
    @Transactional(readOnly = true)
    public Page<NotificacionViaje> getNotificacionesPaginadas(Long cooperativaId, int page, int size) {
        return notificacionRepository.findByCooperativaIdPaginated(cooperativaId, PageRequest.of(page, size));
    }

    /**
     * Obtiene solo las notificaciones no leídas
     */
    @Transactional(readOnly = true)
    public List<NotificacionViaje> getNotificacionesNoLeidas(Long cooperativaId) {
        return notificacionRepository.findUnreadByCooperativaId(cooperativaId);
    }

    /**
     * Cuenta las notificaciones no leídas
     */
    @Transactional(readOnly = true)
    public Long contarNotificacionesNoLeidas(Long cooperativaId) {
        return notificacionRepository.countUnreadByCooperativaId(cooperativaId);
    }

    /**
     * Marca una notificación como leída
     */
    @Transactional
    public void marcarComoLeida(Long notificacionId) {
        notificacionRepository.findById(notificacionId).ifPresent(notificacion -> {
            notificacion.setLeida(true);
            notificacion.setFechaLectura(LocalDateTime.now());
            notificacionRepository.save(notificacion);
        });
    }

    /**
     * Marca todas las notificaciones de una cooperativa como leídas
     */
    @Transactional
    public int marcarTodasComoLeidas(Long cooperativaId) {
        return notificacionRepository.markAllAsReadByCooperativaId(cooperativaId);
    }
}
