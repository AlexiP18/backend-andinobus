package com.andinobus.backendsmartcode.config;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador temporal para diagnosticar problemas de email.
 * ELIMINAR EN PRODUCCIÓN DESPUÉS DE RESOLVER EL PROBLEMA.
 */
@RestController
@RequestMapping("/api/diagnostic")
@RequiredArgsConstructor
@Slf4j
public class EmailDiagnosticController {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:NOT_SET}")
    private String mailUsername;

    @Value("${spring.mail.host:NOT_SET}")
    private String mailHost;

    @Value("${spring.mail.port:0}")
    private int mailPort;

    @GetMapping("/email-config")
    public ResponseEntity<Map<String, Object>> getEmailConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", mailHost);
        config.put("port", mailPort);
        config.put("username", mailUsername);
        config.put("passwordSet", mailUsername != null && !mailUsername.equals("NOT_SET"));
        return ResponseEntity.ok(config);
    }

    @PostMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestParam String to) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("=== INICIANDO PRUEBA DE EMAIL ===");
            log.info("Host: {}, Port: {}, Username: {}", mailHost, mailPort, mailUsername);
            log.info("Destinatario: {}", to);

            // Intentar enviar email simple primero
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailUsername);
            message.setTo(to);
            message.setSubject("Test Email desde AndinoBus");
            message.setText("Este es un email de prueba para verificar la configuración SMTP.");

            log.info("Enviando email...");
            mailSender.send(message);
            log.info("Email enviado exitosamente!");

            result.put("success", true);
            result.put("message", "Email enviado correctamente a " + to);
            
        } catch (Exception e) {
            log.error("ERROR al enviar email: ", e);
            result.put("success", false);
            result.put("error", e.getClass().getSimpleName());
            result.put("message", e.getMessage());
            if (e.getCause() != null) {
                result.put("cause", e.getCause().getMessage());
            }
        }
        
        return ResponseEntity.ok(result);
    }
}
