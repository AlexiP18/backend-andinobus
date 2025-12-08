package com.andinobus.backendsmartcode.ventas.application.services;

import com.andinobus.backendsmartcode.ventas.api.dto.VentasDtos;
import com.andinobus.backendsmartcode.ventas.domain.entities.Reserva;
import com.andinobus.backendsmartcode.ventas.domain.repositories.ReservaRepository;
import com.andinobus.backendsmartcode.operacion.domain.entities.ViajeAsiento;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeAsientoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.andinobus.backendsmartcode.common.errors.NotFoundException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoService {

    private final ReservaRepository reservaRepository;
    private final ViajeAsientoRepository viajeAsientoRepository;

    @Transactional
    public VentasDtos.PagoResponse confirmarPago(VentasDtos.PagoConfirmacionRequest request, String clienteEmail) {
        // 1. Buscar reserva (por ID y email si está presente, solo por ID si no)
        Reserva reserva;
        if (clienteEmail != null && !clienteEmail.isEmpty()) {
            reserva = reservaRepository.findByIdAndClienteEmail(request.getReservaId(), clienteEmail)
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        } else {
            reserva = reservaRepository.findById(request.getReservaId())
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        }

        // 2. Validar estado
        if (!"PENDIENTE".equals(reserva.getEstado())) {
            return VentasDtos.PagoResponse.builder()
                    .reservaId(reserva.getId())
                    .estado("RECHAZADO")
                    .mensaje("La reserva no está pendiente de pago")
                    .build();
        }

        // 3. Actualizar estado de reserva
        reserva.setEstado("PAGADO");
        reservaRepository.save(reserva);

        // 4. Actualizar asientos a VENDIDO
        List<ViajeAsiento> asientos = viajeAsientoRepository.findByReservaId(reserva.getId());
        asientos.forEach(asiento -> {
            asiento.setEstado("VENDIDO");
            viajeAsientoRepository.save(asiento);
        });

        log.info("Pago confirmado para reserva {} con método {}", reserva.getId(), request.getMetodoPago());

        return VentasDtos.PagoResponse.builder()
                .reservaId(reserva.getId())
                .estado("PAGADO")
                .mensaje("Pago confirmado exitosamente")
                .build();
    }
}
