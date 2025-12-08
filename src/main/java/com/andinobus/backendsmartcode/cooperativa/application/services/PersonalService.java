package com.andinobus.backendsmartcode.cooperativa.application.services;

import com.andinobus.backendsmartcode.catalogos.domain.entities.BusChofer;
import com.andinobus.backendsmartcode.catalogos.domain.entities.Cooperativa;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.BusChoferRepository;
import com.andinobus.backendsmartcode.catalogos.infrastructure.repositories.CooperativaRepository;
import com.andinobus.backendsmartcode.cooperativa.api.dto.PersonalDtos;
import com.andinobus.backendsmartcode.cooperativa.domain.enums.RolCooperativa;
import com.andinobus.backendsmartcode.cooperativa.domain.entities.UsuarioCooperativa;
import com.andinobus.backendsmartcode.cooperativa.infrastructure.repositories.UsuarioCooperativaRepository;
import com.andinobus.backendsmartcode.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonalService {

    private final UsuarioCooperativaRepository usuarioCooperativaRepository;
    private final CooperativaRepository cooperativaRepository;
    private final BusChoferRepository busChoferRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public PersonalDtos.PersonalResponse createPersonal(PersonalDtos.CreatePersonalRequest request) {
        // Verificar que la cooperativa existe
        Cooperativa cooperativa = cooperativaRepository.findById(request.getCooperativaId())
                .orElseThrow(() -> new RuntimeException("Cooperativa no encontrada"));

        // Verificar que el email no esté duplicado
        if (usuarioCooperativaRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }

        // Validar y convertir rol
        RolCooperativa rol;
        try {
            rol = RolCooperativa.valueOf(request.getRolCooperativa().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rol inválido: " + request.getRolCooperativa());
        }

        // Generar código de empleado (requerido para OFICINISTA y CHOFER)
        String codigoEmpleado = null;
        if (rol == RolCooperativa.OFICINISTA || rol == RolCooperativa.CHOFER) {
            codigoEmpleado = generateCodigoEmpleado(cooperativa.getId(), rol);
        }

        UsuarioCooperativa.UsuarioCooperativaBuilder usuarioBuilder = UsuarioCooperativa.builder()
                .cooperativa(cooperativa)
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .cedula(request.getCedula())
                .telefono(request.getTelefono())
                .rolCooperativa(rol)
                .codigoEmpleado(codigoEmpleado)
                .activo(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());

        // Campos adicionales para CHOFER
        if (rol == RolCooperativa.CHOFER) {
            if (request.getNumeroLicencia() != null) {
                usuarioBuilder.licenciaConducir(request.getNumeroLicencia());
            }
            if (request.getTipoLicencia() != null) {
                usuarioBuilder.tipoLicencia(request.getTipoLicencia());
            }
            if (request.getFechaVencimientoLicencia() != null) {
                usuarioBuilder.fechaVencimientoLicencia(
                    java.time.LocalDate.parse(request.getFechaVencimientoLicencia())
                );
            }
        }

        UsuarioCooperativa usuario = usuarioBuilder.build();

        UsuarioCooperativa savedUsuario = usuarioCooperativaRepository.save(usuario);
        
        // Enviar email de bienvenida según el rol
        try {
            if (rol == RolCooperativa.CHOFER || rol == RolCooperativa.OFICINISTA) {
                // Para Chofer y Oficinista: email de bienvenida con credenciales
                emailService.sendBienvenidaCooperativaEmail(
                    savedUsuario.getEmail(),
                    savedUsuario.getNombres(),
                    savedUsuario.getApellidos(),
                    rol.name(),
                    cooperativa.getNombre(),
                    cooperativa.getLogoUrl(),
                    savedUsuario.getEmail(),
                    request.getPassword() // Contraseña en texto plano
                );
                log.info("Email de bienvenida enviado a {} ({})", savedUsuario.getEmail(), rol.name());
            } else if (rol == RolCooperativa.ADMIN) {
                // Para Admin de Cooperativa: email notificando que contraseña es cédula
                emailService.sendAdminCooperativaEmail(
                    savedUsuario.getEmail(),
                    savedUsuario.getNombres(),
                    savedUsuario.getApellidos(),
                    cooperativa.getNombre(),
                    cooperativa.getLogoUrl(),
                    savedUsuario.getEmail(),
                    savedUsuario.getCedula()
                );
                log.info("Email de admin enviado a {} ({})", savedUsuario.getEmail(), rol.name());
            }
        } catch (Exception e) {
            log.error("Error al enviar email a {}: {}", savedUsuario.getEmail(), e.getMessage());
            // No fallar la creación si el email no se envía
        }
        
        return mapToResponse(savedUsuario);
    }

    @Transactional
    public PersonalDtos.PersonalResponse updatePersonal(Long personalId, PersonalDtos.UpdatePersonalRequest request) {
        UsuarioCooperativa usuario = usuarioCooperativaRepository.findById(personalId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar email duplicado si se está cambiando
        if (request.getEmail() != null && !request.getEmail().equals(usuario.getEmail())) {
            if (usuarioCooperativaRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Ya existe un usuario con ese email");
            }
            usuario.setEmail(request.getEmail());
        }

        if (request.getNombres() != null) {
            usuario.setNombres(request.getNombres());
        }
        if (request.getApellidos() != null) {
            usuario.setApellidos(request.getApellidos());
        }
        if (request.getCedula() != null) {
            usuario.setCedula(request.getCedula());
        }
        if (request.getTelefono() != null) {
            usuario.setTelefono(request.getTelefono());
        }
        if (request.getRolCooperativa() != null) {
            try {
                RolCooperativa nuevoRol = RolCooperativa.valueOf(request.getRolCooperativa().toUpperCase());
                RolCooperativa rolAnterior = usuario.getRolCooperativa();
                
                // Si cambia el rol, actualizar código de empleado si es necesario
                if (nuevoRol != rolAnterior) {
                    if (nuevoRol == RolCooperativa.OFICINISTA || nuevoRol == RolCooperativa.CHOFER) {
                        // Generar nuevo código para el nuevo rol
                        usuario.setCodigoEmpleado(generateCodigoEmpleado(usuario.getCooperativa().getId(), nuevoRol));
                    } else if (nuevoRol == RolCooperativa.ADMIN) {
                        // ADMIN no necesita código de empleado
                        usuario.setCodigoEmpleado(null);
                    }
                }
                
                usuario.setRolCooperativa(nuevoRol);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Rol inválido: " + request.getRolCooperativa());
            }
        }
        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }
        
        // Campos adicionales para CHOFER
        if (request.getNumeroLicencia() != null) {
            usuario.setLicenciaConducir(request.getNumeroLicencia());
        }
        if (request.getTipoLicencia() != null) {
            usuario.setTipoLicencia(request.getTipoLicencia());
        }
        if (request.getFechaVencimientoLicencia() != null) {
            usuario.setFechaVencimientoLicencia(
                java.time.LocalDate.parse(request.getFechaVencimientoLicencia())
            );
        }

        usuario.setUpdatedAt(LocalDateTime.now());
        UsuarioCooperativa savedUsuario = usuarioCooperativaRepository.save(usuario);
        return mapToResponse(savedUsuario);
    }

    @Transactional
    public void deletePersonal(Long personalId) {
        UsuarioCooperativa usuario = usuarioCooperativaRepository.findById(personalId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Soft delete: marcar como inactivo
        usuario.setActivo(false);
        usuario.setUpdatedAt(LocalDateTime.now());
        usuarioCooperativaRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public PersonalDtos.PersonalResponse getPersonalById(Long personalId) {
        UsuarioCooperativa usuario = usuarioCooperativaRepository.findById(personalId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return mapToResponse(usuario);
    }

    @Transactional(readOnly = true)
    public List<PersonalDtos.PersonalResponse> getPersonalByCooperativa(Long cooperativaId) {
        List<UsuarioCooperativa> usuarios = usuarioCooperativaRepository.findByCooperativaId(cooperativaId);
        return usuarios.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PersonalDtos.PersonalResponse uploadFoto(Long personalId, org.springframework.web.multipart.MultipartFile foto) {
        UsuarioCooperativa usuario = usuarioCooperativaRepository.findById(personalId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        try {
            // Validar que sea una imagen
            String contentType = foto.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("El archivo debe ser una imagen");
            }

            // Validar tamaño (máximo 5MB)
            if (foto.getSize() > 5 * 1024 * 1024) {
                throw new RuntimeException("La imagen no debe superar los 5MB");
            }

            // Generar nombre único para el archivo
            String originalFilename = foto.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = "personal_" + personalId + "_" + System.currentTimeMillis() + extension;

            // Guardar archivo en el sistema de archivos (usar path absoluto)
            String baseDir = System.getProperty("user.dir");
            String uploadPath = "uploads/personal/fotos";
            java.nio.file.Path uploadDir = java.nio.file.Paths.get(baseDir, uploadPath);
            
            if (!java.nio.file.Files.exists(uploadDir)) {
                java.nio.file.Files.createDirectories(uploadDir);
            }

            java.nio.file.Path filePath = uploadDir.resolve(filename);
            foto.transferTo(filePath.toFile());

            // Eliminar foto anterior si existe
            if (usuario.getFotoFilename() != null) {
                try {
                    java.nio.file.Files.deleteIfExists(
                        java.nio.file.Paths.get(baseDir, uploadPath, usuario.getFotoFilename())
                    );
                } catch (Exception e) {
                    // Log error but continue
                    System.err.println("Error eliminando foto anterior: " + e.getMessage());
                }
            }

            // Actualizar usuario con la URL de la foto
            usuario.setFotoUrl("/uploads/personal/fotos/" + filename);
            usuario.setFotoFilename(filename);
            usuario.setUpdatedAt(LocalDateTime.now());

            UsuarioCooperativa savedUsuario = usuarioCooperativaRepository.save(usuario);
            return mapToResponse(savedUsuario);

        } catch (Exception e) {
            throw new RuntimeException("Error al subir la foto: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteFoto(Long personalId) {
        UsuarioCooperativa usuario = usuarioCooperativaRepository.findById(personalId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getFotoFilename() != null) {
            try {
                String uploadDir = "uploads/personal/fotos/";
                java.nio.file.Files.deleteIfExists(
                    java.nio.file.Paths.get(uploadDir + usuario.getFotoFilename())
                );
            } catch (Exception e) {
                System.err.println("Error eliminando foto: " + e.getMessage());
            }

            usuario.setFotoUrl(null);
            usuario.setFotoFilename(null);
            usuario.setUpdatedAt(LocalDateTime.now());
            usuarioCooperativaRepository.save(usuario);
        }
    }

    private PersonalDtos.PersonalResponse mapToResponse(UsuarioCooperativa usuario) {
        // Buscar bus asignado si es CHOFER
        Long busAsignadoId = null;
        String busAsignadoPlaca = null;
        String busAsignadoNumeroInterno = null;
        
        if (usuario.getRolCooperativa() == RolCooperativa.CHOFER) {
            List<BusChofer> asignaciones = busChoferRepository.findByChoferIdAndActivoTrue(usuario.getId());
            if (!asignaciones.isEmpty()) {
                BusChofer asignacion = asignaciones.get(0); // Tomar la primera asignación activa
                busAsignadoId = asignacion.getBus().getId();
                busAsignadoPlaca = asignacion.getBus().getPlaca();
                busAsignadoNumeroInterno = asignacion.getBus().getNumeroInterno();
            }
        }
        
        return PersonalDtos.PersonalResponse.builder()
                .id(usuario.getId())
                .cooperativaId(usuario.getCooperativa().getId())
                .cooperativaNombre(usuario.getCooperativa().getNombre())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .email(usuario.getEmail())
                .cedula(usuario.getCedula())
                .telefono(usuario.getTelefono())
                .rolCooperativa(usuario.getRolCooperativa().name())
                .activo(usuario.getActivo())
                .fotoUrl(usuario.getFotoUrl())
                .numeroLicencia(usuario.getLicenciaConducir())
                .tipoLicencia(usuario.getTipoLicencia())
                .fechaVencimientoLicencia(
                    usuario.getFechaVencimientoLicencia() != null
                        ? usuario.getFechaVencimientoLicencia().toString()
                        : null
                )
                .busAsignadoId(busAsignadoId)
                .busAsignadoPlaca(busAsignadoPlaca)
                .busAsignadoNumeroInterno(busAsignadoNumeroInterno)
                .build();
    }

    /**
     * Genera un código de empleado único para la cooperativa
     * Formato: COOP{cooperativaId}-{ROL}-{contador}
     * Ejemplo: COOP1-OFIC-001, COOP1-CHOF-001
     */
    private String generateCodigoEmpleado(Long cooperativaId, RolCooperativa rol) {
        String prefix = String.format("COOP%d-%s-", cooperativaId, getRolPrefix(rol));
        
        // Contar usuarios existentes con el mismo rol en la cooperativa
        long count = usuarioCooperativaRepository.countByCooperativaIdAndRolCooperativa(cooperativaId, rol);
        
        // Generar código con padding de 3 dígitos
        return String.format("%s%03d", prefix, count + 1);
    }

    private String getRolPrefix(RolCooperativa rol) {
        switch (rol) {
            case ADMIN:
                return "ADMIN";
            case OFICINISTA:
                return "OFIC";
            case CHOFER:
                return "CHOF";
            default:
                return "EMPL";
        }
    }
}
