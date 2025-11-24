package com.andinobus.backendsmartcode.cooperativa.domain.entities;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import com.andinobus.backendsmartcode.cooperativa.domain.enums.RolCooperativa;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa a todos los usuarios del entorno COOPERATIVA
 * Incluye: ADMIN, OFICINISTA y CHOFER
 */
@Entity
@Table(name = "usuario_cooperativa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioCooperativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 120)
    private String nombres;

    @Column(nullable = false, length = 120)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 20)
    private String cedula;

    @Column(length = 20)
    private String telefono;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cooperativa_id", nullable = false)
    private Cooperativa cooperativa;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_cooperativa", nullable = false, length = 20)
    private RolCooperativa rolCooperativa;

    // ===================================
    // Campos específicos para OFICINISTA
    // ===================================

    @Column(name = "codigo_empleado", length = 20)
    private String codigoEmpleado;

    @Column(length = 50)
    private String terminal;

    // ===================================
    // Campos específicos para CHOFER
    // ===================================

    @Column(name = "licencia_conducir", length = 50)
    private String licenciaConducir;

    @Column(name = "fecha_vencimiento_licencia")
    private LocalDate fechaVencimientoLicencia;

    @Column(name = "tipo_licencia", length = 10)
    private String tipoLicencia; // 'C', 'D', 'E'

    @Column(name = "experiencia_anios")
    @Builder.Default
    private Integer experienciaAnios = 0;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal calificacion = BigDecimal.ZERO;

    // ===================================
    // Foto de perfil
    // ===================================

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(name = "foto_filename", length = 255)
    private String fotoFilename;

    // ===================================
    // Control de estado
    // ===================================

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (fechaRegistro == null) {
            fechaRegistro = now;
        }
        if (activo == null) {
            activo = true;
        }
        if (experienciaAnios == null) {
            experienciaAnios = 0;
        }
        if (calificacion == null) {
            calificacion = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===================================
    // Métodos de utilidad
    // ===================================

    public boolean isAdmin() {
        return RolCooperativa.ADMIN.equals(this.rolCooperativa);
    }

    public boolean isOficinista() {
        return RolCooperativa.OFICINISTA.equals(this.rolCooperativa);
    }

    public boolean isChofer() {
        return RolCooperativa.CHOFER.equals(this.rolCooperativa);
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    /**
     * Valida si el chofer tiene la licencia vigente
     */
    public boolean tieneLicenciaVigente() {
        if (!isChofer() || fechaVencimientoLicencia == null) {
            return false;
        }
        return LocalDate.now().isBefore(fechaVencimientoLicencia);
    }
}
