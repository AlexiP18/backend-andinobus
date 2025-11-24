package com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories;

import com.andinobus.backendsmartcode.cooperativa.domain.entities.UsuarioCooperativa;
import com.andinobus.backendsmartcode.cooperativa.domain.enums.RolCooperativa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de usuarios del entorno COOPERATIVA
 */
@Repository
public interface UsuarioCooperativaRepository extends JpaRepository<UsuarioCooperativa, Long> {

    /**
     * Buscar usuario por email (para login)
     */
    Optional<UsuarioCooperativa> findByEmail(String email);

    /**
     * Buscar usuario por email y que esté activo
     */
    Optional<UsuarioCooperativa> findByEmailAndActivoTrue(String email);

    /**
     * Buscar usuario por cédula
     */
    Optional<UsuarioCooperativa> findByCedula(String cedula);

    /**
     * Verificar si existe un email
     */
    boolean existsByEmail(String email);

    /**
     * Verificar si existe una cédula
     */
    boolean existsByCedula(String cedula);

    /**
     * Obtener todos los usuarios de una cooperativa
     */
    List<UsuarioCooperativa> findByCooperativaId(Long cooperativaId);

    /**
     * Obtener todos los usuarios activos de una cooperativa
     */
    List<UsuarioCooperativa> findByCooperativaIdAndActivoTrue(Long cooperativaId);

    /**
     * Obtener usuarios por rol en una cooperativa
     */
    List<UsuarioCooperativa> findByCooperativaIdAndRolCooperativa(Long cooperativaId, RolCooperativa rolCooperativa);

    /**
     * Obtener usuarios activos por rol en una cooperativa
     */
    List<UsuarioCooperativa> findByCooperativaIdAndRolCooperativaAndActivoTrue(
        Long cooperativaId, 
        RolCooperativa rolCooperativa
    );

    /**
     * Obtener todos los choferes de una cooperativa
     */
    @Query("SELECT u FROM UsuarioCooperativa u WHERE u.cooperativa.id = :cooperativaId AND u.rolCooperativa = 'CHOFER' AND u.activo = true")
    List<UsuarioCooperativa> findChoferesActivosByCooperativa(@Param("cooperativaId") Long cooperativaId);

    /**
     * Obtener todos los oficinistas de una cooperativa
     */
    @Query("SELECT u FROM UsuarioCooperativa u WHERE u.cooperativa.id = :cooperativaId AND u.rolCooperativa = 'OFICINISTA' AND u.activo = true")
    List<UsuarioCooperativa> findOficinistasActivosByCooperativa(@Param("cooperativaId") Long cooperativaId);

    /**
     * Obtener admins de una cooperativa
     */
    @Query("SELECT u FROM UsuarioCooperativa u WHERE u.cooperativa.id = :cooperativaId AND u.rolCooperativa = 'ADMIN' AND u.activo = true")
    List<UsuarioCooperativa> findAdminsActivosByCooperativa(@Param("cooperativaId") Long cooperativaId);

    /**
     * Buscar oficinista por código de empleado en una cooperativa
     */
    Optional<UsuarioCooperativa> findByCooperativaIdAndCodigoEmpleado(Long cooperativaId, String codigoEmpleado);

    /**
     * Buscar chofer por número de licencia
     */
    Optional<UsuarioCooperativa> findByLicenciaConducir(String licenciaConducir);

    /**
     * Contar usuarios por rol en una cooperativa
     */
    @Query("SELECT COUNT(u) FROM UsuarioCooperativa u WHERE u.cooperativa.id = :cooperativaId AND u.rolCooperativa = :rol AND u.activo = true")
    long countByCooperativaAndRol(@Param("cooperativaId") Long cooperativaId, @Param("rol") RolCooperativa rol);
    
    /**
     * Contar usuarios activos de una cooperativa
     */
    int countByCooperativaIdAndActivoTrue(Long cooperativaId);

    /**
     * Contar todos los usuarios (activos e inactivos) por rol en una cooperativa
     * Usado para generar códigos de empleado únicos
     */
    long countByCooperativaIdAndRolCooperativa(Long cooperativaId, RolCooperativa rolCooperativa);
}
