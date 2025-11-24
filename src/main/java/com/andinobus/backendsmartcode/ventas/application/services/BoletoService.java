package com.andinobus.backendsmartcode.ventas.application.services;

import com.andinobus.backendsmartcode.ventas.api.dto.VentasDtos;
import com.andinobus.backendsmartcode.ventas.domain.entities.Reserva;
import com.andinobus.backendsmartcode.ventas.domain.repositories.ReservaRepository;
import com.andinobus.backendsmartcode.operacion.domain.entities.ViajeAsiento;
import com.andinobus.backendsmartcode.operacion.domain.repositories.ViajeAsientoRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.andinobus.backendsmartcode.common.errors.NotFoundException;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Profile("dev")
@Service
@RequiredArgsConstructor
public class BoletoService {

    private final ReservaRepository reservaRepository;
    private final ViajeAsientoRepository viajeAsientoRepository;

    @Transactional(readOnly = true)
    public VentasDtos.BoletoResponse generarBoleto(Long reservaId, String clienteEmail) {
        // 1. Buscar reserva (por ID y email si está presente, solo por ID si no)
        Reserva reserva;
        if (clienteEmail != null && !clienteEmail.isEmpty()) {
            reserva = reservaRepository.findByIdAndClienteEmail(reservaId, clienteEmail)
                    .orElseThrow(() -> new NotFoundException("Reserva no encontrada"));
        } else {
            reserva = reservaRepository.findById(reservaId)
                    .orElseThrow(() -> new NotFoundException("Reserva no encontrada"));
        }

        // 2. Validar que esté pagada
        if (!"PAGADO".equals(reserva.getEstado())) {
            throw new RuntimeException("La reserva debe estar pagada para generar el boleto");
        }

        // 3. Generar código del boleto
        String codigoBoleto = generarCodigoBoleto(reserva);

        // 4. Obtener información de asientos
        List<ViajeAsiento> asientos = viajeAsientoRepository.findByReservaId(reservaId);
        String asientosStr = asientos.stream()
                .map(ViajeAsiento::getNumeroAsiento)
                .collect(Collectors.joining(", "));

        // 5. Generar contenido del QR
        String qrContent = String.format(
                "BOLETO: %s\nRESERVA: %d\nVIAJE: %d\nASIENTOS: %s\nCLIENTE: %s",
                codigoBoleto,
                reserva.getId(),
                reserva.getViaje().getId(),
                asientosStr,
                reserva.getClienteEmail()
        );

        // 6. Generar código QR
        String qrDataUrl = generarCodigoQR(qrContent);

        log.info("Boleto generado: {} para reserva {}", codigoBoleto, reservaId);

        return VentasDtos.BoletoResponse.builder()
                .codigoBoleto(codigoBoleto)
                .reservaId(reservaId)
                .estado("EMITIDO")
                .codigoQR(qrDataUrl)
                .build();
    }

    private String generarCodigoBoleto(Reserva reserva) {
        // Formato: AB-YYYYMMDD-XXXXX
        String fecha = reserva.getViaje().getFecha().toString().replace("-", "");
        String random = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return String.format("AB-%s-%s", fecha, random);
    }

    private String generarCodigoQR(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrBytes = outputStream.toByteArray();

            String base64QR = Base64.getEncoder().encodeToString(qrBytes);
            return "data:image/png;base64," + base64QR;

        } catch (Exception e) {
            log.error("Error generando código QR", e);
            // Retornar placeholder en caso de error
            return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        }
    }
}
