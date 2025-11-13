package com.andinobus.backendsmartcode.ventas.api.controllers;

import com.andinobus.backendsmartcode.ventas.api.dto.VentasDtos;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class VentasController {

    private static final AtomicLong RESERVA_SEQ = new AtomicLong(1000);
    private static final Map<Long, VentasDtos.ReservaDetalleResponse> RESERVAS = new ConcurrentHashMap<>();
    private static final Map<String, VentasDtos.BoletoResponse> BOLETOS = new ConcurrentHashMap<>();

    @PostMapping("/reservas")
    @ResponseStatus(HttpStatus.CREATED)
    public VentasDtos.ReservaResponse crearReserva(@Valid @RequestBody VentasDtos.ReservaCreateRequest req) {
        long id = RESERVA_SEQ.incrementAndGet();
        String expira = OffsetDateTime.now().plusMinutes(15).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        VentasDtos.ReservaDetalleResponse detalle = VentasDtos.ReservaDetalleResponse.builder()
                .id(id)
                .viajeId(req.getViajeId())
                .cliente("Cliente Demo")
                .asientos(req.getAsientos())
                .estado("pendiente")
                .monto(new BigDecimal("10.00"))
                .build();
        RESERVAS.put(id, detalle);
        return VentasDtos.ReservaResponse.builder()
                .id(id)
                .viajeId(req.getViajeId())
                .asientos(req.getAsientos())
                .estado("pendiente")
                .fechaExpira(expira)
                .build();
    }

    @GetMapping("/reservas/{id}")
    public VentasDtos.ReservaDetalleResponse obtenerReserva(@PathVariable Long id) {
        VentasDtos.ReservaDetalleResponse d = RESERVAS.get(id);
        if (d == null) {
            throw new IllegalArgumentException("Reserva no encontrada (stub)");
        }
        return d;
    }

    @PostMapping(path = "/pagos/transferencia", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> pagoTransferenciaMultipart(@RequestPart(value = "comprobante", required = false) MultipartFile comprobante,
                                                          @RequestParam("reservaId") Long reservaId,
                                                          @RequestParam(value = "monto", required = false) BigDecimal monto,
                                                          @RequestParam(value = "referencia", required = false) String referencia) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("status", "RECEIVED");
        res.put("reservaId", reservaId);
        res.put("monto", monto);
        res.put("referencia", referencia);
        res.put("comprobanteNombre", comprobante != null ? comprobante.getOriginalFilename() : null);
        // marcar como pagado en stub
        VentasDtos.ReservaDetalleResponse d = RESERVAS.get(reservaId);
        if (d != null) {
            RESERVAS.put(reservaId, VentasDtos.ReservaDetalleResponse.builder()
                    .id(d.getId()).viajeId(d.getViajeId()).cliente(d.getCliente())
                    .asientos(d.getAsientos()).estado("pagado").monto(d.getMonto()).build());
        }
        return res;
    }

    @PostMapping(path = "/pagos/paypal/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> paypalWebhook(@RequestBody VentasDtos.PaypalWebhookEvent event) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("received", true);
        res.put("event_type", event.getEvent_type());
        res.put("resource_id", event.getResource_id());
        return res;
    }

    @PostMapping("/boletos/emitir")
    @ResponseStatus(HttpStatus.CREATED)
    public VentasDtos.BoletoResponse emitir(@RequestBody VentasDtos.EmitirBoletoRequest req) {
        VentasDtos.ReservaDetalleResponse d = RESERVAS.get(req.getReservaId());
        if (d == null || !"pagado".equalsIgnoreCase(d.getEstado())) {
            throw new IllegalArgumentException("La reserva no está pagada o no existe (stub)");
        }
        String codigo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        VentasDtos.BoletoResponse br = VentasDtos.BoletoResponse.builder()
                .codigo(codigo)
                .reservaId(req.getReservaId())
                .estado("emitido")
                .qr("QR-" + codigo)
                .build();
        BOLETOS.put(codigo, br);
        return br;
    }

    @GetMapping("/boletos/{codigo}")
    public VentasDtos.BoletoResponse obtenerBoleto(@PathVariable String codigo) {
        VentasDtos.BoletoResponse br = BOLETOS.get(codigo);
        if (br == null) {
            throw new IllegalArgumentException("Boleto no encontrado (stub)");
        }
        return br;
    }
}
