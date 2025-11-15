package com.andinobus.backendsmartcode.usuarios.domain.repositories;

import com.andinobus.backendsmartcode.usuarios.domain.entities.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByToken(String token);
    List<UserToken> findByUserId(Long userId);
    void deleteByToken(String token);
    void deleteByUserId(Long userId);
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
