package com.andinobus.backendsmartcode.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Profile("dev")
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from-name:AndinoBus}")
    private String fromName;

    @Value("${app.mail.from-address:noreply.andinobus@gmail.com}")
    private String fromAddress;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Envía un email HTML usando un template Thymeleaf
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Agregar variables comunes a todos los templates
            Context context = new Context();
            context.setVariables(variables);
            context.setVariable("frontendUrl", frontendUrl);

            // Procesar el template
            String htmlContent = templateEngine.process(templateName, context);

            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email enviado exitosamente a: {}", to);

        } catch (MessagingException e) {
            log.error("Error al enviar email a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error al enviar email: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al enviar email a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error inesperado al enviar email: " + e.getMessage());
        }
    }

    /**
     * Envía email de confirmación de cuenta para CLIENTES
     */
    public void sendConfirmacionCuentaEmail(String to, String nombres, String token) {
        String confirmUrl = frontendUrl + "/confirmar-cuenta?token=" + token;
        
        Map<String, Object> variables = Map.of(
            "nombres", nombres,
            "confirmUrl", confirmUrl,
            "token", token
        );

        sendHtmlEmail(to, "Confirma tu cuenta en AndinoBus", "email/confirmacion-cuenta", variables);
    }

    /**
     * Envía email de bienvenida para CHOFER/OFICINISTA
     */
    public void sendBienvenidaCooperativaEmail(
            String to, 
            String nombres, 
            String apellidos,
            String rol,
            String cooperativaNombre,
            String cooperativaLogo,
            String email,
            String password) {
        
        Map<String, Object> variables = Map.of(
            "nombres", nombres,
            "apellidos", apellidos,
            "rol", rol,
            "cooperativaNombre", cooperativaNombre,
            "cooperativaLogo", cooperativaLogo != null ? cooperativaLogo : "",
            "email", email,
            "password", password,
            "loginUrl", frontendUrl + "/login"
        );

        String subject = "Bienvenido a " + cooperativaNombre + " - AndinoBus";
        sendHtmlEmail(to, subject, "email/bienvenida-cooperativa", variables);
    }

    /**
     * Envía email para ADMIN de Cooperativa (creado por SuperAdmin)
     * Notifica que su contraseña es su cédula y debe cambiarla
     */
    public void sendAdminCooperativaEmail(
            String to,
            String nombres,
            String apellidos,
            String cooperativaNombre,
            String cooperativaLogo,
            String email,
            String cedula) {
        
        Map<String, Object> variables = Map.of(
            "nombres", nombres,
            "apellidos", apellidos,
            "cooperativaNombre", cooperativaNombre,
            "cooperativaLogo", cooperativaLogo != null ? cooperativaLogo : "",
            "email", email,
            "cedula", cedula,
            "loginUrl", frontendUrl + "/login"
        );

        String subject = "Eres Administrador de " + cooperativaNombre + " - AndinoBus";
        sendHtmlEmail(to, subject, "email/admin-cooperativa", variables);
    }

    /**
     * Envía email de cuenta confirmada exitosamente
     */
    public void sendCuentaConfirmadaEmail(String to, String nombres) {
        Map<String, Object> variables = Map.of(
            "nombres", nombres,
            "loginUrl", frontendUrl + "/login"
        );

        sendHtmlEmail(to, "¡Tu cuenta ha sido confirmada!", "email/cuenta-confirmada", variables);
    }
}
