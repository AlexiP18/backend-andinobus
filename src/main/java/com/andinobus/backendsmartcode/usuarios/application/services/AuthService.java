package com.andinobus.backendsmartcode.usuarios.application.services;

import com.andinobus.backendsmartcode.cooperativa.domain.entities.UsuarioCooperativa;
import com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.UsuarioCooperativaRepository;
import com.andinobus.backendsmartcode.email.EmailService;
import com.andinobus.backendsmartcode.usuarios.api.dto.AuthDtos;
import com.andinobus.backendsmartcode.usuarios.domain.entities.AppUser;
import com.andinobus.backendsmartcode.usuarios.domain.entities.ConfirmacionToken;
import com.andinobus.backendsmartcode.usuarios.domain.entities.UserToken;
import com.andinobus.backendsmartcode.usuarios.domain.repositories.ConfirmacionTokenRepository;
import com.andinobus.backendsmartcode.usuarios.domain.repositories.UserRepository;
import com.andinobus.backendsmartcode.usuarios.domain.repositories.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Profile("dev")
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final UsuarioCooperativaRepository usuarioCooperativaRepository;
    private final ConfirmacionTokenRepository confirmacionTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // Credenciales del administrador del sistema (hardcoded)
    @Value("${admin.email:admin@andinobus.com}")
    private String adminEmail;

    @Value("${admin.password:Admin123!}")
    private String adminPassword;
    
    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest req) {
        // Verificar si el email ya existe
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        // Crear nuevo usuario (email NO confirmado)
        AppUser user = AppUser.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .nombres(req.getNombres())
                .apellidos(req.getApellidos())
                .rol("CLIENTE")
                .activo(true)
                .emailConfirmado(false) // Pendiente de confirmación
                .createdAt(LocalDateTime.now())
                .build();
        
        // Guardar en BD
        user = userRepository.save(user);
        
        // Generar token de confirmación
        String confirmToken = UUID.randomUUID().toString();
        ConfirmacionToken tokenEntity = ConfirmacionToken.builder()
                .token(confirmToken)
                .userId(user.getId())
                .userEmail(user.getEmail())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        confirmacionTokenRepository.save(tokenEntity);
        
        // Enviar email de confirmación
        try {
            emailService.sendConfirmacionCuentaEmail(user.getEmail(), user.getNombres(), confirmToken);
            log.info("Email de confirmación enviado a: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar email de confirmación a {}: {}", user.getEmail(), e.getMessage());
            // No fallar el registro si el email no se envía
        }
        
        // Retornar respuesta (sin token de sesión, debe confirmar primero)
        return AuthDtos.AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .rol(user.getRol())
                .nombres(user.getNombres())
                .apellidos(user.getApellidos())
                .message("Registro exitoso. Por favor revisa tu correo para confirmar tu cuenta.")
                .requiresConfirmation(true)
                .build();
    }
    
    /**
     * Login para usuarios CLIENTE (tabla: app_user)
     */
    @Transactional
    public AuthDtos.AuthResponse loginCliente(AuthDtos.LoginRequest req) {
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
        
        // Verificar que el email esté confirmado
        if (!Boolean.TRUE.equals(user.getEmailConfirmado())) {
            throw new RuntimeException("Debes confirmar tu email antes de iniciar sesión. Revisa tu bandeja de entrada.");
        }
        
        // Generar y guardar token
        String token = generateAndSaveToken(user.getId(), "CLIENTE");
        
        return AuthDtos.AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .rol("CLIENTE")
                .nombres(user.getNombres())
                .apellidos(user.getApellidos())
                .build();
    }

    /**
     * Login para usuarios COOPERATIVA (tabla: usuario_cooperativa)
     * Incluye: ADMIN, OFICINISTA, CHOFER
     */
    @Transactional
    public AuthDtos.AuthResponse loginCooperativa(AuthDtos.LoginRequest req) {
        // Buscar usuario en tabla usuario_cooperativa
        UsuarioCooperativa user = usuarioCooperativaRepository.findByEmail(req.getEmail())
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
        String token = generateAndSaveToken(user.getId(), "COOPERATIVA");
        
        return AuthDtos.AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .rol("COOPERATIVA")
                .rolCooperativa(user.getRolCooperativa().name())
                .cooperativaId(user.getCooperativa().getId())
                .cooperativaNombre(user.getCooperativa().getNombre())
                .nombres(user.getNombres())
                .apellidos(user.getApellidos())
                .cedula(user.getCedula())
                .telefono(user.getTelefono())
                .fotoUrl(user.getFotoUrl())
                .build();
    }

    /**
     * Login para ADMINISTRADOR del sistema (hardcoded, sin tabla)
     */
    @Transactional
    public AuthDtos.AuthResponse loginAdmin(AuthDtos.LoginRequest req) {
        // Verificar email
        if (!adminEmail.equals(req.getEmail())) {
            throw new RuntimeException("Credenciales incorrectas");
        }
        
        // Verificar contraseña
        if (!adminPassword.equals(req.getPassword())) {
            throw new RuntimeException("Credenciales incorrectas");
        }
        
        // Generar y guardar token (userId = -1 para admin)
        String token = generateAndSaveToken(-1L, "ADMIN");
        
        return AuthDtos.AuthResponse.builder()
                .token(token)
                .userId(-1L)
                .email(adminEmail)
                .rol("ADMIN")
                .nombres("Super")
                .apellidos("Administrador")
                .build();
    }

    /**
     * Método legacy para compatibilidad (llama a loginCliente)
     */
    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
        return loginCliente(req);
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
        
        Long userId = userToken.getUserId();
        String userType = userToken.getUserType();
        
        // Super Admin (userId = -1 o userType = ADMIN)
        if (userId == -1 || "ADMIN".equals(userType)) {
            return AuthDtos.MeResponse.builder()
                    .userId(-1L)
                    .email(adminEmail)
                    .rol("ADMIN")
                    .nombres("Administrador")
                    .apellidos("del Sistema")
                    .build();
        }
        
        // Usuario COOPERATIVA
        if ("COOPERATIVA".equals(userType)) {
            UsuarioCooperativa usuarioCoop = usuarioCooperativaRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario cooperativa no encontrado"));
            
            // Verificar que el usuario esté activo
            if (!usuarioCoop.getActivo()) {
                throw new RuntimeException("Usuario inactivo");
            }
            
            return AuthDtos.MeResponse.builder()
                    .userId(usuarioCoop.getId())
                    .email(usuarioCoop.getEmail())
                    .rol("COOPERATIVA")
                    .rolCooperativa(usuarioCoop.getRolCooperativa().name())
                    .nombres(usuarioCoop.getNombres())
                    .apellidos(usuarioCoop.getApellidos())
                    .cedula(usuarioCoop.getCedula())
                    .telefono(usuarioCoop.getTelefono())
                    .fotoUrl(usuarioCoop.getFotoUrl())
                    .cooperativaId(usuarioCoop.getCooperativa() != null ? usuarioCoop.getCooperativa().getId() : null)
                    .cooperativaNombre(usuarioCoop.getCooperativa() != null ? usuarioCoop.getCooperativa().getNombre() : null)
                    .build();
        }
        
        // Usuario CLIENTE (default)
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario cliente no encontrado"));
        
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
    
    /**
     * Método temporal para resetear las contraseñas de los usuarios cooperativa
     */
    @Transactional
    public String resetCooperativaPasswords() {
        String newPassword = "admin123";
        String newHash = passwordEncoder.encode(newPassword);
        
        // Actualizar todos los usuarios cooperativa
        var usuarios = usuarioCooperativaRepository.findAll();
        int count = 0;
        
        for (var usuario : usuarios) {
            usuario.setPasswordHash(newHash);
            usuarioCooperativaRepository.save(usuario);
            count++;
        }
        
        return String.format("Se actualizaron %d usuarios. Nueva contraseña: %s, Hash: %s", 
                             count, newPassword, newHash);
    }
    
    private String generateAndSaveToken(Long userId, String userType) {
        String token = "token-" + UUID.randomUUID().toString();
        
        UserToken userToken = UserToken.builder()
                .token(token)
                .userId(userId)
                .userType(userType)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
        
        userTokenRepository.save(userToken);
        
        return token;
    }

    // Sobrecarga para mantener compatibilidad
    private String generateAndSaveToken(Long userId) {
        return generateAndSaveToken(userId, "CLIENTE");
    }
    
    /**
     * Confirmar cuenta de cliente mediante token
     */
    @Transactional
    public AuthDtos.ConfirmacionResponse confirmarCuenta(String token) {
        // Buscar el token
        ConfirmacionToken confirmToken = confirmacionTokenRepository.findByToken(token)
                .orElse(null);
        
        if (confirmToken == null) {
            return AuthDtos.ConfirmacionResponse.builder()
                    .success(false)
                    .message("Token de confirmación inválido o no encontrado")
                    .build();
        }
        
        // Verificar si ya fue confirmado
        if (confirmToken.isConfirmed()) {
            return AuthDtos.ConfirmacionResponse.builder()
                    .success(false)
                    .message("Esta cuenta ya fue confirmada anteriormente")
                    .build();
        }
        
        // Verificar si expiró
        if (confirmToken.isExpired()) {
            return AuthDtos.ConfirmacionResponse.builder()
                    .success(false)
                    .message("El token de confirmación ha expirado. Por favor solicita uno nuevo.")
                    .build();
        }
        
        // Buscar el usuario
        AppUser user = userRepository.findById(confirmToken.getUserId())
                .orElse(null);
        
        if (user == null) {
            return AuthDtos.ConfirmacionResponse.builder()
                    .success(false)
                    .message("Usuario asociado al token no encontrado")
                    .build();
        }
        
        // Confirmar el email
        user.setEmailConfirmado(true);
        userRepository.save(user);
        
        // Marcar token como usado
        confirmToken.setConfirmedAt(LocalDateTime.now());
        confirmacionTokenRepository.save(confirmToken);
        
        // Enviar email de confirmación exitosa
        try {
            emailService.sendCuentaConfirmadaEmail(user.getEmail(), user.getNombres());
        } catch (Exception e) {
            log.warn("No se pudo enviar email de confirmación exitosa: {}", e.getMessage());
        }
        
        log.info("Cuenta confirmada exitosamente para: {}", user.getEmail());
        
        return AuthDtos.ConfirmacionResponse.builder()
                .success(true)
                .message("¡Tu cuenta ha sido confirmada exitosamente! Ya puedes iniciar sesión.")
                .email(user.getEmail())
                .build();
    }
    
    /**
     * Reenviar email de confirmación
     */
    @Transactional
    public AuthDtos.ConfirmacionResponse reenviarConfirmacion(String email) {
        // Buscar usuario
        AppUser user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            return AuthDtos.ConfirmacionResponse.builder()
                    .success(false)
                    .message("No existe una cuenta con ese email")
                    .build();
        }
        
        // Verificar si ya está confirmado
        if (Boolean.TRUE.equals(user.getEmailConfirmado())) {
            return AuthDtos.ConfirmacionResponse.builder()
                    .success(false)
                    .message("Esta cuenta ya está confirmada. Puedes iniciar sesión.")
                    .build();
        }
        
        // Eliminar tokens anteriores
        confirmacionTokenRepository.deleteByUserId(user.getId());
        
        // Generar nuevo token
        String newToken = UUID.randomUUID().toString();
        ConfirmacionToken tokenEntity = ConfirmacionToken.builder()
                .token(newToken)
                .userId(user.getId())
                .userEmail(user.getEmail())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        confirmacionTokenRepository.save(tokenEntity);
        
        // Enviar email
        try {
            emailService.sendConfirmacionCuentaEmail(user.getEmail(), user.getNombres(), newToken);
            log.info("Email de confirmación reenviado a: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error al reenviar email: {}", e.getMessage());
            return AuthDtos.ConfirmacionResponse.builder()
                    .success(false)
                    .message("Error al enviar el email. Intenta nuevamente más tarde.")
                    .build();
        }
        
        return AuthDtos.ConfirmacionResponse.builder()
                .success(true)
                .message("Se ha enviado un nuevo enlace de confirmación a tu correo.")
                .build();
    }
}
