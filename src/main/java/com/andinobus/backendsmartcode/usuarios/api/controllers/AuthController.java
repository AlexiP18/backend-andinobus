package com.andinobus.backendsmartcode.usuarios.api.controllers;

import com.andinobus.backendsmartcode.usuarios.api.dto.AuthDtos;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class AuthController {

    @PostMapping("/auth/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        // Stub: retorna un token fijo en función del email
        String role = req.getEmail().toLowerCase().contains("admin") ? "ADMIN" : "CLIENTE";
        String token = role.equals("ADMIN") ? "demo-token-admin" : "demo-token-client";
        return AuthDtos.AuthResponse.builder()
                .token(token)
                .userId(1L)
                .email(req.getEmail())
                .rol(role)
                .nombres("Usuario")
                .apellidos("Demo")
                .build();
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        // Stub: crea usuario en memoria (no persistente) y devuelve token demo
        String token = "demo-token-client";
        return AuthDtos.AuthResponse.builder()
                .token(token)
                .userId(2L)
                .email(req.getEmail())
                .rol("CLIENTE")
                .nombres(req.getNombres())
                .apellidos(req.getApellidos())
                .build();
    }

    @GetMapping("/users/me")
    public AuthDtos.MeResponse me(HttpServletRequest request, @RequestHeader(value = "Authorization", required = false) String auth,
                                  @RequestHeader(value = "X-Demo-Token", required = false) String demoToken) {
        String token = demoToken;
        if (token == null && auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        }
        String role = "CLIENTE";
        String email = "cliente@example.com";
        Long userId = 2L;
        if ("demo-token-admin".equals(token)) {
            role = "ADMIN";
            email = "admin@example.com";
            userId = 1L;
        }
        return AuthDtos.MeResponse.builder()
                .userId(userId)
                .email(email)
                .rol(role)
                .nombres("Usuario")
                .apellidos("Demo")
                .build();
    }
}
