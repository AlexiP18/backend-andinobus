package com.andinobus.backendsmartcode.catalogos.application.services;

import com.andinobus.backendsmartcode.catalogos.api.dto.BusCreateRequest;
import com.andinobus.backendsmartcode.catalogos.api.dto.BusResponse;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Bus;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.CooperativaRepository;
import com.andinobus.backendsmartcode.common.errors.NotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("dev")
public class BusService {

    private final BusRepository busRepository;
    private final CooperativaRepository cooperativaRepository;

    public BusService(BusRepository busRepository, CooperativaRepository cooperativaRepository) {
        this.busRepository = busRepository;
        this.cooperativaRepository = cooperativaRepository;
    }

    @Transactional
    public BusResponse create(Long cooperativaId, BusCreateRequest req) {
        Cooperativa coop = cooperativaRepository.findById(cooperativaId)
                .orElseThrow(() -> new NotFoundException("Cooperativa no encontrada"));
        Bus bus = Bus.builder()
                .cooperativa(coop)
                .numeroInterno(req.getNumeroInterno())
                .placa(req.getPlaca())
                .chasisMarca(req.getChasisMarca())
                .carroceriaMarca(req.getCarroceriaMarca())
                .fotoUrl(req.getFotoUrl())
                .activo(req.getActivo() == null ? true : req.getActivo())
                .build();
        bus = busRepository.save(bus);
        return toResponse(bus);
    }

    @Transactional(readOnly = true)
    public Page<BusResponse> listByCooperativa(Long cooperativaId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Bus> data = busRepository.findByCooperativa_IdAndActivoTrue(cooperativaId, pageable);
        return data.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public BusResponse get(Long id) {
        Bus bus = busRepository.findById(id).orElseThrow(() -> new NotFoundException("Bus no encontrado"));
        return toResponse(bus);
    }

    @Transactional
    public com.andinobus.backendsmartcode.catalogos.api.dto.BusDtos.BusResponse createBus(
            com.andinobus.backendsmartcode.catalogos.api.dto.BusDtos.CreateBusRequest request) {
        Cooperativa cooperativa = cooperativaRepository.findById(request.getCooperativaId())
                .orElseThrow(() -> new NotFoundException("Cooperativa no encontrada"));

        if (busRepository.findByPlaca(request.getPlaca()).isPresent()) {
            throw new RuntimeException("Ya existe un bus con esa placa");
        }

        Bus bus = Bus.builder()
                .cooperativa(cooperativa)
                .numeroInterno(request.getNumeroInterno())
                .placa(request.getPlaca())
                .chasisMarca(request.getChasisMarca())
                .carroceriaMarca(request.getCarroceriaMarca())
                .fotoUrl(request.getFotoUrl())
                .capacidadAsientos(request.getCapacidadAsientos() != null ? request.getCapacidadAsientos() : 40)
                .estado(request.getEstado() != null ? request.getEstado() : "DISPONIBLE")
                .activo(true)
                .build();

        Bus savedBus = busRepository.save(bus);
        return mapToBusResponse(savedBus);
    }

    @Transactional
    public com.andinobus.backendsmartcode.catalogos.api.dto.BusDtos.BusResponse updateBus(
            Long busId, com.andinobus.backendsmartcode.catalogos.api.dto.BusDtos.UpdateBusRequest request) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new NotFoundException("Bus no encontrado"));

        if (request.getPlaca() != null && !request.getPlaca().equals(bus.getPlaca())) {
            if (busRepository.findByPlaca(request.getPlaca()).isPresent()) {
                throw new RuntimeException("Ya existe un bus con esa placa");
            }
            bus.setPlaca(request.getPlaca());
        }

        if (request.getNumeroInterno() != null) bus.setNumeroInterno(request.getNumeroInterno());
        if (request.getChasisMarca() != null) bus.setChasisMarca(request.getChasisMarca());
        if (request.getCarroceriaMarca() != null) bus.setCarroceriaMarca(request.getCarroceriaMarca());
        if (request.getFotoUrl() != null) bus.setFotoUrl(request.getFotoUrl());
        if (request.getCapacidadAsientos() != null) bus.setCapacidadAsientos(request.getCapacidadAsientos());
        if (request.getEstado() != null) bus.setEstado(request.getEstado());
        if (request.getActivo() != null) bus.setActivo(request.getActivo());

        Bus savedBus = busRepository.save(bus);
        return mapToBusResponse(savedBus);
    }

    @Transactional
    public void deleteBus(Long busId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new NotFoundException("Bus no encontrado"));
        bus.setActivo(false);
        busRepository.save(bus);
    }

    @Transactional(readOnly = true)
    public com.andinobus.backendsmartcode.catalogos.api.dto.BusDtos.BusResponse getBusById(Long busId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new NotFoundException("Bus no encontrado"));
        return mapToBusResponse(bus);
    }

    @Transactional(readOnly = true)
    public java.util.List<com.andinobus.backendsmartcode.catalogos.api.dto.BusDtos.BusResponse> getBusesByCooperativa(Long cooperativaId) {
        java.util.List<Bus> buses = busRepository.findByCooperativaId(cooperativaId);
        return buses.stream()
                .map(this::mapToBusResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    private com.andinobus.backendsmartcode.catalogos.api.dto.BusDtos.BusResponse mapToBusResponse(Bus bus) {
        return com.andinobus.backendsmartcode.catalogos.api.dto.BusDtos.BusResponse.builder()
                .id(bus.getId())
                .cooperativaId(bus.getCooperativa().getId())
                .cooperativaNombre(bus.getCooperativa().getNombre())
                .numeroInterno(bus.getNumeroInterno())
                .placa(bus.getPlaca())
                .chasisMarca(bus.getChasisMarca())
                .carroceriaMarca(bus.getCarroceriaMarca())
                .fotoUrl(bus.getFotoUrl())
                .capacidadAsientos(bus.getCapacidadAsientos())
                .estado(bus.getEstado())
                .activo(bus.getActivo())
                .build();
    }

    private BusResponse toResponse(Bus b) {
        return BusResponse.builder()
                .id(b.getId())
                .cooperativaId(b.getCooperativa() != null ? b.getCooperativa().getId() : null)
                .numeroInterno(b.getNumeroInterno())
                .placa(b.getPlaca())
                .chasisMarca(b.getChasisMarca())
                .carroceriaMarca(b.getCarroceriaMarca())
                .fotoUrl(b.getFotoUrl())
                .activo(b.getActivo())
                .build();
    }

    /**
     * Sube una foto para un bus
     */
    @Transactional
    public com.andinobus.backendsmartcode.catalogos.api.dto.BusDtos.BusResponse uploadFoto(
            Long busId, org.springframework.web.multipart.MultipartFile foto) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new NotFoundException("Bus no encontrado"));

        // Validar que sea una imagen
        String contentType = foto.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("El archivo debe ser una imagen");
        }

        // Validar tamaño (máximo 5MB)
        if (foto.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("La imagen no debe superar los 5MB");
        }

        try {
            // Obtener directorio base del proyecto
            String baseDir = System.getProperty("user.dir");
            String uploadPath = "uploads/buses/fotos";
            
            // Eliminar foto anterior si existe
            if (bus.getFotoFilename() != null) {
                java.nio.file.Path oldPath = java.nio.file.Paths.get(baseDir, uploadPath, bus.getFotoFilename());
                java.nio.file.Files.deleteIfExists(oldPath);
            }

            // Generar nombre único: bus_{id}_{timestamp}.{extension}
            String originalFilename = foto.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = "bus_" + busId + "_" + System.currentTimeMillis() + extension;

            // Crear directorio si no existe (path absoluto)
            java.nio.file.Path uploadDir = java.nio.file.Paths.get(baseDir, uploadPath);
            if (!java.nio.file.Files.exists(uploadDir)) {
                java.nio.file.Files.createDirectories(uploadDir);
            }

            // Guardar archivo (path absoluto)
            java.nio.file.Path filePath = uploadDir.resolve(filename);
            foto.transferTo(filePath.toFile());

            // Actualizar base de datos
            bus.setFotoUrl("/uploads/buses/fotos/" + filename);
            bus.setFotoFilename(filename);
            Bus savedBus = busRepository.save(bus);

            return mapToBusResponse(savedBus);

        } catch (java.io.IOException e) {
            throw new RuntimeException("Error al guardar la foto: " + e.getMessage());
        }
    }

    /**
     * Elimina la foto de un bus
     */
    @Transactional
    public void deleteFoto(Long busId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new NotFoundException("Bus no encontrado"));

        if (bus.getFotoFilename() != null) {
            try {
                String baseDir = System.getProperty("user.dir");
                java.nio.file.Path filePath = java.nio.file.Paths.get(baseDir, "uploads/buses/fotos", bus.getFotoFilename());
                java.nio.file.Files.deleteIfExists(filePath);
            } catch (java.io.IOException e) {
                // Log error pero continuar
                System.err.println("Error al eliminar archivo físico: " + e.getMessage());
            }

            bus.setFotoUrl(null);
            bus.setFotoFilename(null);
            busRepository.save(bus);
        }
    }
}
