package com.andinobus.backendsmartcode.usuarios.domain.repositories;

import com.andinobus.backendsmartcode.usuarios.domain.entities.ConfirmacionToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ConfirmacionTokenRepository extends JpaRepository<ConfirmacionToken, Long> {

    Optional<ConfirmacionToken> findByToken(String token);

    Optional<ConfirmacionToken> findByUserId(Long userId);

    Optional<ConfirmacionToken> findByUserEmail(String email);

    /**
     * Busca un token válido (no expirado y no confirmado) para un usuario
     */
    @Query("SELECT ct FROM ConfirmacionToken ct WHERE ct.userId = :userId AND ct.confirmedAt IS NULL AND ct.expiresAt > :now")
    Optional<ConfirmacionToken> findValidTokenByUserId(Long userId, LocalDateTime now);

    /**
     * Elimina tokens expirados
     */
    @Modifying
    @Query("DELETE FROM ConfirmacionToken ct WHERE ct.expiresAt < :now AND ct.confirmedAt IS NULL")
    void deleteExpiredTokens(LocalDateTime now);

    /**
     * Elimina todos los tokens de un usuario (para regenerar)
     */
    @Modifying
    void deleteByUserId(Long userId);

    /**
     * Verifica si existe un token válido para un email
     */
    @Query("SELECT COUNT(ct) > 0 FROM ConfirmacionToken ct WHERE ct.userEmail = :email AND ct.confirmedAt IS NULL AND ct.expiresAt > :now")
    boolean existsValidTokenByEmail(String email, LocalDateTime now);
}
