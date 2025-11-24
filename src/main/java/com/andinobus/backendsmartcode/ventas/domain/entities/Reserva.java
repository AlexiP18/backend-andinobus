package com.andinobus.backendsmartcode.ventas.domain.entities;

import com.andinobus.backendsmartcode.operacion.domain.entities.Viaje;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    @Column(name = "cliente_email", length = 180)
    private String clienteEmail;

    @Column(nullable = false)
    private Integer asientos;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String estado = "PENDIENTE"; // PENDIENTE | PAGADO | CANCELADO | EXPIRADO

    @Column(precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "PENDIENTE";
        }
    }
}
