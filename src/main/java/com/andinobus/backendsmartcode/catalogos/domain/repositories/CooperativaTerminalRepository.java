package com.andinobus.backendsmartcode.catalogos.domain.repositories;

import com.andinobus.backendsmartcode.catalogos.domain.entities.CooperativaTerminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CooperativaTerminalRepository extends JpaRepository<CooperativaTerminal, Long> {

    List<CooperativaTerminal> findByCooperativaIdAndActivoTrue(Long cooperativaId);

    List<CooperativaTerminal> findByTerminalIdAndActivoTrue(Long terminalId);

    Optional<CooperativaTerminal> findByCooperativaIdAndTerminalId(Long cooperativaId, Long terminalId);

    boolean existsByCooperativaIdAndTerminalIdAndActivoTrue(Long cooperativaId, Long terminalId);

    @Query("SELECT ct FROM CooperativaTerminal ct " +
           "JOIN FETCH ct.terminal t " +
           "WHERE ct.cooperativa.id = :cooperativaId AND ct.activo = true " +
           "ORDER BY t.provincia, t.canton")
    List<CooperativaTerminal> findByCooperativaIdWithTerminal(@Param("cooperativaId") Long cooperativaId);

    @Query("SELECT ct FROM CooperativaTerminal ct " +
           "JOIN FETCH ct.cooperativa c " +
           "WHERE ct.terminal.id = :terminalId AND ct.activo = true " +
           "ORDER BY c.nombre")
    List<CooperativaTerminal> findByTerminalIdWithCooperativa(@Param("terminalId") Long terminalId);

    @Query("SELECT DISTINCT ct.terminal.id FROM CooperativaTerminal ct " +
           "WHERE ct.cooperativa.id = :cooperativaId AND ct.activo = true")
    List<Long> findTerminalIdsByCooperativaId(@Param("cooperativaId") Long cooperativaId);
}
