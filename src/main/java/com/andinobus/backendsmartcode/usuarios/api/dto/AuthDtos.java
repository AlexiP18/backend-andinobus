package com.andinobus.backendsmartcode.usuarios.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;

public class AuthDtos {
    @Data
    public static class LoginRequest {
        @Email
        @NotBlank
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @Email
        @NotBlank
        private String email;
        @NotBlank
        private String password;
        private String nombres;
        private String apellidos;
    }

    @Data
    @Builder
    public static class AuthResponse {
        private String token;
        private Long userId;
        private String email;
        private String rol;
        private String nombres;
        private String apellidos;
    }

    @Data
    @Builder
    public static class MeResponse {
        private Long userId;
        private String email;
        private String rol;
        private String nombres;
        private String apellidos;
    }
}
