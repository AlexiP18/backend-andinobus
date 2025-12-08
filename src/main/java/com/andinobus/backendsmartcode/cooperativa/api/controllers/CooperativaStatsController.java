package com.andinobus.backendsmartcode.cooperativa.api.controllers;

import com.andinobus.backendsmartcode.cooperativa.api.dto.CooperativaStatsDtos;
import com.andinobus.backendsmartcode.cooperativa.application.services.CooperativaStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cooperativa")
@RequiredArgsConstructor
public class CooperativaStatsController {
    
    private final CooperativaStatsService statsService;

    /**
     * Obtener estadísticas generales de la cooperativa
     * GET /api/cooperativa/{cooperativaId}/stats
     */
    @GetMapping("/{cooperativaId}/stats")
    public CooperativaStatsDtos.CooperativaStats getStats(@PathVariable Long cooperativaId) {
        return statsService.getCooperativaStats(cooperativaId);
    }

    /**
     * Obtener estadísticas para el dashboard de Admin de Cooperativa
     * GET /api/cooperativa/{cooperativaId}/admin-stats
     */
    @GetMapping("/{cooperativaId}/admin-stats")
    public CooperativaStatsDtos.AdminStats getAdminStats(@PathVariable Long cooperativaId) {
        return statsService.getAdminStats(cooperativaId);
    }

    /**
     * Obtener estadísticas para el dashboard de Oficinista
     * GET /api/cooperativa/{cooperativaId}/oficinista-stats
     */
    @GetMapping("/{cooperativaId}/oficinista-stats")
    public CooperativaStatsDtos.OficinistaStats getOficinistaStats(@PathVariable Long cooperativaId) {
        return statsService.getOficinistaStats(cooperativaId);
    }

    /**
     * Obtener estadísticas para el dashboard de Chofer
     * GET /api/cooperativa/chofer/{choferId}/stats
     */
    @GetMapping("/chofer/{choferId}/stats")
    public CooperativaStatsDtos.ChoferStats getChoferStats(@PathVariable Long choferId) {
        return statsService.getChoferStats(choferId);
    }

    /**
     * Obtener lista de buses de la cooperativa
     * GET /api/cooperativa/{cooperativaId}/buses
     */
    @GetMapping("/{cooperativaId}/stats/buses")
    public CooperativaStatsDtos.BusesList getBuses(@PathVariable Long cooperativaId) {
        return statsService.getBuses(cooperativaId);
    }

    /**
     * Obtener lista de personal de la cooperativa (vista de estadísticas)
     * GET /api/cooperativa/{cooperativaId}/stats/personal
     */
    @GetMapping("/{cooperativaId}/stats/personal")
    public CooperativaStatsDtos.PersonalList getPersonal(@PathVariable Long cooperativaId) {
        return statsService.getPersonal(cooperativaId);
    }
}
