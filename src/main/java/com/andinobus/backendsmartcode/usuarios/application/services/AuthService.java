package com.andinobus.backendsmartcode.usuarios.application.services;

import com.andinobus.backendsmartcode.usuarios.api.dto.AuthDtos;
import com.andinobus.backendsmartcode.usuarios.domain.entities.AppUser;
import com.andinobus.backendsmartcode.usuarios.domain.entities.UserToken;
import com.andinobus.backendsmartcode.usuarios.domain.repositories.UserRepository;
import com.andinobus.backendsmartcode.usuarios.domain.repositories.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Profile("dev")
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest req) {
        // Verificar si el email ya existe
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        // Crear nuevo usuario
        AppUser user = AppUser.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .nombres(req.getNombres())
                .apellidos(req.getApellidos())
                .rol("CLIENTE")
                .activo(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        // Guardar en BD
        user = userRepository.save(user);
        
        // Generar y guardar token
        String token = generateAndSaveToken(user.getId());
        
        return AuthDtos.AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .rol(user.getRol())
                .nombres(user.getNombres())
                .apellidos(user.getApellidos())
                .build();
    }
    
    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
        // Buscar usuario por email
        AppUser user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));
        
        // Verificar contraseña
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Credenciales incorrectas");
        }
        
        // Verificar que esté activo
        if (!user.getActivo()) {
            throw new RuntimeException("Usuario inactivo");
        }
        
        // Generar y guardar token
        String token = generateAndSaveToken(user.getId());
        
        return AuthDtos.AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .rol(user.getRol())
                .nombres(user.getNombres())
                .apellidos(user.getApellidos())
                .build();
    }
    
    @Transactional(readOnly = true)
    public AuthDtos.MeResponse getMe(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        return AuthDtos.MeResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .rol(user.getRol())
                .nombres(user.getNombres())
                .apellidos(user.getApellidos())
                .build();
    }
    
    @Transactional(readOnly = true)
    public AuthDtos.MeResponse getMeByEmail(String email) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        return AuthDtos.MeResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .rol(user.getRol())
                .nombres(user.getNombres())
                .apellidos(user.getApellidos())
                .build();
    }
    
    @Transactional(readOnly = true)
    public AuthDtos.MeResponse getMeByToken(String token) {
        // Buscar token en BD
        UserToken userToken = userTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));
        
        // Verificar si el token está expirado
        if (userToken.isExpired()) {
            throw new RuntimeException("Token expirado");
        }
        
        // Buscar usuario asociado al token
        AppUser user = userRepository.findById(userToken.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Verificar que el usuario esté activo
        if (!user.getActivo()) {
            throw new RuntimeException("Usuario inactivo");
        }
        
        return AuthDtos.MeResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .rol(user.getRol())
                .nombres(user.getNombres())
                .apellidos(user.getApellidos())
                .build();
    }
    
    @Transactional
    public void logout(String token) {
        userTokenRepository.deleteByToken(token);
    }
    
    @Transactional
    public void cleanExpiredTokens() {
        userTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
    
    private String generateAndSaveToken(Long userId) {
        String token = "token-" + UUID.randomUUID().toString();
        
        UserToken userToken = UserToken.builder()
                .token(token)
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
        
        userTokenRepository.save(userToken);
        
        return token;
    }
}
