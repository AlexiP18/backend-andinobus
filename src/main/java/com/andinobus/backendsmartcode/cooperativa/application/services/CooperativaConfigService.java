package com.andinobus.backendsmartcode.cooperativa.application.services;

import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.CooperativaRepository;
import com.andinobus.backendsmartcode.common.errors.NotFoundException;
import com.andinobus.backendsmartcode.cooperativa.api.dto.CooperativaConfigDtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CooperativaConfigService {

    private final CooperativaRepository cooperativaRepository;

    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    /**
     * Obtener la configuración de una cooperativa
     */
    @Transactional(readOnly = true)
    public ConfiguracionResponse getConfiguracion(Long cooperativaId) {
        Cooperativa cooperativa = cooperativaRepository.findById(cooperativaId)
                .orElseThrow(() -> new NotFoundException("Cooperativa no encontrada con id: " + cooperativaId));

        return toResponse(cooperativa);
    }

    /**
     * Actualizar la configuración de una cooperativa
     */
    @Transactional
    public ConfiguracionResponse updateConfiguracion(Long cooperativaId, UpdateConfiguracionRequest request) {
        Cooperativa cooperativa = cooperativaRepository.findById(cooperativaId)
                .orElseThrow(() -> new NotFoundException("Cooperativa no encontrada con id: " + cooperativaId));

        // Actualizar campos si están presentes
        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            cooperativa.setNombre(request.getNombre());
        }
        if (request.getDescripcion() != null) {
            cooperativa.setDescripcion(request.getDescripcion());
        }
        if (request.getLogoUrl() != null) {
            cooperativa.setLogoUrl(request.getLogoUrl());
        }
        if (request.getColorPrimario() != null && request.getColorPrimario().matches("^#[0-9A-Fa-f]{6}$")) {
            cooperativa.setColorPrimario(request.getColorPrimario());
        }
        if (request.getColorSecundario() != null && request.getColorSecundario().matches("^#[0-9A-Fa-f]{6}$")) {
            cooperativa.setColorSecundario(request.getColorSecundario());
        }
        if (request.getFacebook() != null) {
            cooperativa.setFacebook(request.getFacebook());
        }
        if (request.getTwitter() != null) {
            cooperativa.setTwitter(request.getTwitter());
        }
        if (request.getInstagram() != null) {
            cooperativa.setInstagram(request.getInstagram());
        }
        if (request.getLinkedin() != null) {
            cooperativa.setLinkedin(request.getLinkedin());
        }
        if (request.getYoutube() != null) {
            cooperativa.setYoutube(request.getYoutube());
        }

        cooperativa = cooperativaRepository.save(cooperativa);
        log.info("Configuración actualizada para cooperativa id: {}", cooperativaId);

        return toResponse(cooperativa);
    }

    /**
     * Subir logo de cooperativa (Base64)
     */
    @Transactional
    public ConfiguracionResponse uploadLogo(Long cooperativaId, UpdateLogoRequest request) {
        Cooperativa cooperativa = cooperativaRepository.findById(cooperativaId)
                .orElseThrow(() -> new NotFoundException("Cooperativa no encontrada con id: " + cooperativaId));

        try {
            // Decodificar Base64
            String base64Data = request.getLogoBase64();
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1]; // Remover el prefijo data:image/...;base64,
            }
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // Crear directorio si no existe
            Path uploadDir = Paths.get(uploadPath, "logos");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Eliminar logo anterior si existe
            if (cooperativa.getLogoUrl() != null && !cooperativa.getLogoUrl().isBlank()) {
                try {
                    String oldFileName = cooperativa.getLogoUrl().substring(cooperativa.getLogoUrl().lastIndexOf("/") + 1);
                    Path oldFilePath = uploadDir.resolve(oldFileName);
                    Files.deleteIfExists(oldFilePath);
                } catch (Exception e) {
                    log.warn("No se pudo eliminar el logo anterior: {}", e.getMessage());
                }
            }

            // Generar nombre único para el archivo
            String extension = getExtension(request.getFileName());
            String fileName = "logo_" + cooperativaId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
            Path filePath = uploadDir.resolve(fileName);

            // Guardar archivo
            Files.write(filePath, imageBytes);

            // Actualizar URL del logo
            String logoUrl = baseUrl + "/uploads/logos/" + fileName;
            cooperativa.setLogoUrl(logoUrl);
            cooperativa = cooperativaRepository.save(cooperativa);

            log.info("Logo actualizado para cooperativa id: {} -> {}", cooperativaId, logoUrl);

            return toResponse(cooperativa);

        } catch (IOException e) {
            log.error("Error al guardar logo: {}", e.getMessage());
            throw new RuntimeException("Error al guardar el logo: " + e.getMessage());
        }
    }

    /**
     * Eliminar logo de cooperativa
     */
    @Transactional
    public ConfiguracionResponse deleteLogo(Long cooperativaId) {
        Cooperativa cooperativa = cooperativaRepository.findById(cooperativaId)
                .orElseThrow(() -> new NotFoundException("Cooperativa no encontrada con id: " + cooperativaId));

        if (cooperativa.getLogoUrl() != null && !cooperativa.getLogoUrl().isBlank()) {
            try {
                String fileName = cooperativa.getLogoUrl().substring(cooperativa.getLogoUrl().lastIndexOf("/") + 1);
                Path filePath = Paths.get(uploadPath, "logos", fileName);
                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                log.warn("No se pudo eliminar el archivo de logo: {}", e.getMessage());
            }
        }

        cooperativa.setLogoUrl(null);
        cooperativa = cooperativaRepository.save(cooperativa);

        log.info("Logo eliminado para cooperativa id: {}", cooperativaId);

        return toResponse(cooperativa);
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".png"; // Por defecto
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private ConfiguracionResponse toResponse(Cooperativa cooperativa) {
        return ConfiguracionResponse.builder()
                .id(cooperativa.getId())
                .nombre(cooperativa.getNombre())
                .ruc(cooperativa.getRuc())
                .logoUrl(cooperativa.getLogoUrl())
                .descripcion(cooperativa.getDescripcion())
                .colorPrimario(cooperativa.getColorPrimario() != null ? cooperativa.getColorPrimario() : "#16a34a")
                .colorSecundario(cooperativa.getColorSecundario() != null ? cooperativa.getColorSecundario() : "#15803d")
                .facebook(cooperativa.getFacebook())
                .twitter(cooperativa.getTwitter())
                .instagram(cooperativa.getInstagram())
                .linkedin(cooperativa.getLinkedin())
                .youtube(cooperativa.getYoutube())
                .build();
    }
}
