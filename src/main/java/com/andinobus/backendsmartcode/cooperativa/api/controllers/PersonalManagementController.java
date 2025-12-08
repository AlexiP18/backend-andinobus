package com.andinobus.backendsmartcode.cooperativa.api.controllers;

import com.andinobus.backendsmartcode.cooperativa.api.dto.PersonalDtos;
import com.andinobus.backendsmartcode.cooperativa.application.services.PersonalService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cooperativa/{cooperativaId}/personal")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PersonalManagementController {

    private final PersonalService personalService;

    /**
     * Crear un nuevo usuario de personal
     */
    @PostMapping
    public ResponseEntity<PersonalDtos.PersonalResponse> createPersonal(
            @PathVariable Long cooperativaId,
            @RequestBody PersonalDtos.CreatePersonalRequest request) {
        request.setCooperativaId(cooperativaId);
        PersonalDtos.PersonalResponse response = personalService.createPersonal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualizar un usuario existente
     */
    @PutMapping("/{personalId}")
    public ResponseEntity<PersonalDtos.PersonalResponse> updatePersonal(
            @PathVariable Long cooperativaId,
            @PathVariable Long personalId,
            @RequestBody PersonalDtos.UpdatePersonalRequest request) {
        PersonalDtos.PersonalResponse response = personalService.updatePersonal(personalId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar (desactivar) un usuario
     */
    @DeleteMapping("/{personalId}")
    public ResponseEntity<Void> deletePersonal(
            @PathVariable Long cooperativaId,
            @PathVariable Long personalId) {
        personalService.deletePersonal(personalId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener un usuario por ID
     */
    @GetMapping("/{personalId}")
    public ResponseEntity<PersonalDtos.PersonalResponse> getPersonal(
            @PathVariable Long cooperativaId,
            @PathVariable Long personalId) {
        PersonalDtos.PersonalResponse response = personalService.getPersonalById(personalId);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todo el personal de la cooperativa
     */
    @GetMapping
    public ResponseEntity<List<PersonalDtos.PersonalResponse>> listPersonal(
            @PathVariable Long cooperativaId) {
        List<PersonalDtos.PersonalResponse> personal = personalService.getPersonalByCooperativa(cooperativaId);
        return ResponseEntity.ok(personal);
    }

    /**
     * Subir foto de perfil para un usuario de personal
     */
    @PostMapping("/{personalId}/foto")
    public ResponseEntity<PersonalDtos.PersonalResponse> uploadFoto(
            @PathVariable Long cooperativaId,
            @PathVariable Long personalId,
            @RequestParam("foto") org.springframework.web.multipart.MultipartFile foto) {
        try {
            PersonalDtos.PersonalResponse response = personalService.uploadFoto(personalId, foto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminar foto de perfil de un usuario
     */
    @DeleteMapping("/{personalId}/foto")
    public ResponseEntity<Void> deleteFoto(
            @PathVariable Long cooperativaId,
            @PathVariable Long personalId) {
        personalService.deleteFoto(personalId);
        return ResponseEntity.noContent().build();
    }
}

