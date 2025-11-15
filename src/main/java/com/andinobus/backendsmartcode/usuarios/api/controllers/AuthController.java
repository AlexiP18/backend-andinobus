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
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;

    @PostMapping("/auth/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        return authService.login(req);
    }

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
}
