package com.andinobus.backendsmartcode.admin.application.services;

import com.andinobus.backendsmartcode.admin.api.dto.ConfiguracionGlobalDtos.*;
import com.andinobus.backendsmartcode.admin.domain.entities.ConfiguracionGlobal;
import com.andinobus.backendsmartcode.admin.domain.repositories.ConfiguracionGlobalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfiguracionGlobalService {

    private final ConfiguracionGlobalRepository configuracionRepository;

    @Transactional(readOnly = true)
    public ConfiguracionGlobalResponse getConfiguracion() {
        ConfiguracionGlobal config = configuracionRepository.findCurrent()
                .orElseGet(() -> {
                    // Crear configuración por defecto si no existe
                    ConfiguracionGlobal defaultConfig = ConfiguracionGlobal.builder()
                            .nombreAplicacion("AndinoBus")
                            .colorPrimario("#1E40AF")
                            .colorSecundario("#3B82F6")
                            .colorAcento("#10B981")
                            .build();
                    return configuracionRepository.save(defaultConfig);
                });
        return toResponse(config);
    }

    @Transactional
    public ConfiguracionGlobalResponse updateConfiguracion(UpdateConfiguracionRequest request, String updatedBy) {
        ConfiguracionGlobal config = configuracionRepository.findCurrent()
                .orElseGet(() -> ConfiguracionGlobal.builder().build());

        if (request.getNombreAplicacion() != null) config.setNombreAplicacion(request.getNombreAplicacion());
        if (request.getLogoUrl() != null) config.setLogoUrl(request.getLogoUrl());
        if (request.getLogoSmallUrl() != null) config.setLogoSmallUrl(request.getLogoSmallUrl());
        if (request.getFaviconUrl() != null) config.setFaviconUrl(request.getFaviconUrl());
        if (request.getColorPrimario() != null) config.setColorPrimario(request.getColorPrimario());
        if (request.getColorSecundario() != null) config.setColorSecundario(request.getColorSecundario());
        if (request.getColorAcento() != null) config.setColorAcento(request.getColorAcento());
        if (request.getFacebookUrl() != null) config.setFacebookUrl(request.getFacebookUrl());
        if (request.getTwitterUrl() != null) config.setTwitterUrl(request.getTwitterUrl());
        if (request.getInstagramUrl() != null) config.setInstagramUrl(request.getInstagramUrl());
        if (request.getYoutubeUrl() != null) config.setYoutubeUrl(request.getYoutubeUrl());
        if (request.getLinkedinUrl() != null) config.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getEmailSoporte() != null) config.setEmailSoporte(request.getEmailSoporte());
        if (request.getTelefonoSoporte() != null) config.setTelefonoSoporte(request.getTelefonoSoporte());
        if (request.getWhatsappSoporte() != null) config.setWhatsappSoporte(request.getWhatsappSoporte());
        if (request.getDireccionFisica() != null) config.setDireccionFisica(request.getDireccionFisica());
        if (request.getHorarioAtencion() != null) config.setHorarioAtencion(request.getHorarioAtencion());
        if (request.getSitioWeb() != null) config.setSitioWeb(request.getSitioWeb());
        if (request.getTerminosCondicionesUrl() != null) config.setTerminosCondicionesUrl(request.getTerminosCondicionesUrl());
        if (request.getPoliticaPrivacidadUrl() != null) config.setPoliticaPrivacidadUrl(request.getPoliticaPrivacidadUrl());
        if (request.getDescripcion() != null) config.setDescripcion(request.getDescripcion());

        config.setUpdatedBy(updatedBy);

        ConfiguracionGlobal savedConfig = configuracionRepository.save(config);
        log.info("Configuración global actualizada por: {}", updatedBy);
        return toResponse(savedConfig);
    }

    private ConfiguracionGlobalResponse toResponse(ConfiguracionGlobal config) {
        return ConfiguracionGlobalResponse.builder()
                .id(config.getId())
                .nombreAplicacion(config.getNombreAplicacion())
                .logoUrl(config.getLogoUrl())
                .logoSmallUrl(config.getLogoSmallUrl())
                .faviconUrl(config.getFaviconUrl())
                .colorPrimario(config.getColorPrimario())
                .colorSecundario(config.getColorSecundario())
                .colorAcento(config.getColorAcento())
                .facebookUrl(config.getFacebookUrl())
                .twitterUrl(config.getTwitterUrl())
                .instagramUrl(config.getInstagramUrl())
                .youtubeUrl(config.getYoutubeUrl())
                .linkedinUrl(config.getLinkedinUrl())
                .emailSoporte(config.getEmailSoporte())
                .telefonoSoporte(config.getTelefonoSoporte())
                .whatsappSoporte(config.getWhatsappSoporte())
                .direccionFisica(config.getDireccionFisica())
                .horarioAtencion(config.getHorarioAtencion())
                .sitioWeb(config.getSitioWeb())
                .terminosCondicionesUrl(config.getTerminosCondicionesUrl())
                .politicaPrivacidadUrl(config.getPoliticaPrivacidadUrl())
                .descripcion(config.getDescripcion())
                .build();
    }
}
