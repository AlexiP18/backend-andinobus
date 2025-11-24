package com.andinobus.backendsmartcode.usuarios.api.controllers;

import com.andinobus.backendsmartcode.usuarios.api.dto.AuthDtos;
import com.andinobus.backendsmartcode.usuarios.application.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Profile("dev")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;

    /**
     * Login para usuarios CLIENTE (tabla: app_user)
     * POST /api/auth/login-cliente
     */
    @PostMapping("/auth/login-cliente")
    public AuthDtos.AuthResponse loginCliente(@Valid @RequestBody AuthDtos.LoginRequest req) {
        return authService.loginCliente(req);
    }

    /**
     * Login para usuarios COOPERATIVA (tabla: usuario_cooperativa)
     * Incluye: ADMIN, OFICINISTA, CHOFER
     * POST /api/auth/login-cooperativa
     */
    @PostMapping("/auth/login-cooperativa")
    public AuthDtos.AuthResponse loginCooperativa(@Valid @RequestBody AuthDtos.LoginRequest req) {
        return authService.loginCooperativa(req);
    }

    /**
     * Login para ADMINISTRADOR del sistema (hardcoded)
     * POST /api/auth/login-admin
     */
    @PostMapping("/auth/login-admin")
    public AuthDtos.AuthResponse loginAdmin(@Valid @RequestBody AuthDtos.LoginRequest req) {
        return authService.loginAdmin(req);
    }

    /**
     * Login genérico (mantener para compatibilidad)
     * Redirige a loginCliente
     * POST /api/auth/login
     */
    @PostMapping("/auth/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        return authService.loginCliente(req);
    }

    /**
     * Registro de nuevo usuario CLIENTE
     * POST /api/auth/register
     */
    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        return authService.register(req);
    }

    @GetMapping("/users/me")
    public AuthDtos.MeResponse me(@RequestHeader(value = "Authorization", required = false) String auth,
                                  @RequestHeader(value = "X-Demo-Token", required = false) String demoToken) {
        String token = demoToken;
        if (token == null && auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        }
        
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token no proporcionado");
        }
        
        // Validar token y retornar datos del usuario
        return authService.getMeByToken(token);
    }
    
    @PostMapping("/auth/logout")
    public void logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        String token = null;
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        }
        
        if (token != null && !token.isEmpty()) {
            authService.logout(token);
        }
    }
    
    /**
     * Endpoint temporal para resetear contraseñas de usuarios cooperativa
     * GET /api/auth/reset-cooperativa-passwords
     */
    @GetMapping("/auth/reset-cooperativa-passwords")
    public String resetCooperativaPasswords() {
        return authService.resetCooperativaPasswords();
    }
}
