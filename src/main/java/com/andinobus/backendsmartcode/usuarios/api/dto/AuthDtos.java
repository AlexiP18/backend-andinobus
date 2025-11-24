package com.andinobus.backendsmartcode.usuarios.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        private String rol; // CLIENTE | COOPERATIVA | ADMIN
        private String nombres;
        private String apellidos;
        private String fotoUrl;
        
        // Campos adicionales para usuarios COOPERATIVA
        private String rolCooperativa; // ADMIN | OFICINISTA | CHOFER (solo si rol=COOPERATIVA)
        private Long cooperativaId; // ID de la cooperativa (solo si rol=COOPERATIVA)
        private String cooperativaNombre; // Nombre de la cooperativa (solo si rol=COOPERATIVA)
        private String cedula;
        private String telefono;

        // Aliases para compatibilidad con posibles expectativas del frontend
        @JsonProperty("role")
        public String getRole() { return rol; }
        @JsonProperty("id")
        public Long getId() { return userId; }
        @JsonProperty("firstName")
        public String getFirstName() { return nombres; }
        @JsonProperty("lastName")
        public String getLastName() { return apellidos; }
        @JsonProperty("name")
        public String getName() {
            if (nombres == null && apellidos == null) return null;
            if (nombres == null) return apellidos;
            if (apellidos == null) return nombres;
            return (nombres + " " + apellidos).trim();
        }
        @JsonProperty("username")
        public String getUsername() { return email; }
    }

    @Data
    @Builder
    public static class MeResponse {
        private Long userId;
        private String email;
        private String rol;
        private String nombres;
        private String apellidos;
        private String cedula;
        private String telefono;
        private String fotoUrl;
        
        // Campos específicos de cooperativa
        private String rolCooperativa;
        private Long cooperativaId;
        private String cooperativaNombre;

        // Aliases para compatibilidad con posibles expectativas del frontend
        @JsonProperty("role")
        public String getRole() { return rol; }
        @JsonProperty("id")
        public Long getId() { return userId; }
        @JsonProperty("firstName")
        public String getFirstName() { return nombres; }
        @JsonProperty("lastName")
        public String getLastName() { return apellidos; }
        @JsonProperty("name")
        public String getName() {
            if (nombres == null && apellidos == null) return null;
            if (nombres == null) return apellidos;
            if (apellidos == null) return nombres;
            return (nombres + " " + apellidos).trim();
        }
        @JsonProperty("username")
        public String getUsername() { return email; }
    }
}
