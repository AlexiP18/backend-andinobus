package com.andinobus.backendsmartcode.operacion.domain.repositories;

import com.andinobus.backendsmartcode.operacion.domain.entities.NotificacionViaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionViajeRepository extends JpaRepository<NotificacionViaje, Long> {

    /**
     * Obtiene todas las notificaciones de una cooperativa ordenadas por fecha (más recientes primero)
     */
    @Query("SELECT n FROM NotificacionViaje n WHERE n.cooperativa.id = :cooperativaId ORDER BY n.fechaCreacion DESC")
    List<NotificacionViaje> findByCooperativaIdOrderByFechaCreacionDesc(@Param("cooperativaId") Long cooperativaId);

    /**
     * Obtiene las notificaciones de una cooperativa paginadas
     */
    @Query("SELECT n FROM NotificacionViaje n WHERE n.cooperativa.id = :cooperativaId ORDER BY n.fechaCreacion DESC")
    Page<NotificacionViaje> findByCooperativaIdPaginated(@Param("cooperativaId") Long cooperativaId, Pageable pageable);

    /**
     * Obtiene solo las notificaciones no leídas de una cooperativa
     */
    @Query("SELECT n FROM NotificacionViaje n WHERE n.cooperativa.id = :cooperativaId AND n.leida = false ORDER BY n.fechaCreacion DESC")
    List<NotificacionViaje> findUnreadByCooperativaId(@Param("cooperativaId") Long cooperativaId);

    /**
     * Cuenta las notificaciones no leídas de una cooperativa
     */
    @Query("SELECT COUNT(n) FROM NotificacionViaje n WHERE n.cooperativa.id = :cooperativaId AND n.leida = false")
    Long countUnreadByCooperativaId(@Param("cooperativaId") Long cooperativaId);

    /**
     * Marca todas las notificaciones de una cooperativa como leídas
     */
    @Modifying
    @Query("UPDATE NotificacionViaje n SET n.leida = true, n.fechaLectura = CURRENT_TIMESTAMP WHERE n.cooperativa.id = :cooperativaId AND n.leida = false")
    int markAllAsReadByCooperativaId(@Param("cooperativaId") Long cooperativaId);

    /**
     * Obtiene notificaciones por viaje
     */
    List<NotificacionViaje> findByViajeId(Long viajeId);

    /**
     * Obtiene notificaciones por tipo
     */
    @Query("SELECT n FROM NotificacionViaje n WHERE n.cooperativa.id = :cooperativaId AND n.tipo = :tipo ORDER BY n.fechaCreacion DESC")
    List<NotificacionViaje> findByCooperativaIdAndTipo(@Param("cooperativaId") Long cooperativaId, @Param("tipo") String tipo);
}
