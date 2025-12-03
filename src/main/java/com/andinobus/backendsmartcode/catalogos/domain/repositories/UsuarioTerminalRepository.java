package com.andinobus.backendsmartcode.catalogos.domain.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.entities.UsuarioTerminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioTerminalRepository extends JpaRepository<UsuarioTerminal, Long> {

    List<UsuarioTerminal> findByUsuarioIdAndActivoTrue(Long usuarioId);

    List<UsuarioTerminal> findByTerminalIdAndActivoTrue(Long terminalId);

    List<UsuarioTerminal> findByCooperativaIdAndActivoTrue(Long cooperativaId);

    Optional<UsuarioTerminal> findByUsuarioIdAndTerminalId(Long usuarioId, Long terminalId);

    boolean existsByUsuarioIdAndTerminalIdAndActivoTrue(Long usuarioId, Long terminalId);

    @Query("SELECT ut FROM UsuarioTerminal ut " +
           "JOIN FETCH ut.terminal t " +
           "LEFT JOIN FETCH ut.cooperativa c " +
           "WHERE ut.usuario.id = :usuarioId AND ut.activo = true " +
           "ORDER BY t.provincia, t.canton")
    List<UsuarioTerminal> findByUsuarioIdWithTerminal(@Param("usuarioId") Long usuarioId);

    @Query("SELECT ut FROM UsuarioTerminal ut " +
           "JOIN FETCH ut.usuario u " +
           "LEFT JOIN FETCH ut.cooperativa c " +
           "WHERE ut.terminal.id = :terminalId AND ut.activo = true " +
           "ORDER BY u.apellidos, u.nombres")
    List<UsuarioTerminal> findByTerminalIdWithUsuario(@Param("terminalId") Long terminalId);

    @Query("SELECT ut FROM UsuarioTerminal ut " +
           "JOIN FETCH ut.usuario u " +
           "JOIN FETCH ut.terminal t " +
           "WHERE ut.cooperativa.id = :cooperativaId AND ut.activo = true " +
           "ORDER BY u.apellidos, u.nombres")
    List<UsuarioTerminal> findByCooperativaIdWithUsuarioAndTerminal(@Param("cooperativaId") Long cooperativaId);

    @Query("SELECT DISTINCT ut.terminal.id FROM UsuarioTerminal ut " +
           "WHERE ut.usuario.id = :usuarioId AND ut.activo = true")
    List<Long> findTerminalIdsByUsuarioId(@Param("usuarioId") Long usuarioId);
}
