package com.andinobus.backendsmartcode.planificacion;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/planificaciones")
public class PlanificacionController {

    private final CsvPlanificacionParser parser;

    public PlanificacionController(CsvPlanificacionParser parser) {
        this.parser = parser;
    }

    @PostMapping(value = "/preview", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public PlanificacionParseResult preview(@RequestPart("file") MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            return parser.parse(is, file.getOriginalFilename());
        }
    }

    @GetMapping("/examples")
    public List<PlanificacionParseResult> examples() throws IOException {
        String[] names = new String[]{"Horas de Trabajo RUTAS 1.csv", "Horas de Trabajo RUTAS 2.csv"};
        List<PlanificacionParseResult> results = new ArrayList<>();
        for (String name : names) {
            // 1) Try classpath (e.g., if files were moved to src/main/resources)
            ClassPathResource res = new ClassPathResource(name);
            if (res.exists()) {
                try (InputStream is = res.getInputStream()) {
                    results.add(parser.parse(is, name));
                    continue;
                }
            }
            // 2) Try filesystem at application working directory (repo root during dev)
            java.nio.file.Path path = java.nio.file.Paths.get(name);
            if (java.nio.file.Files.exists(path)) {
                try (InputStream is = java.nio.file.Files.newInputStream(path)) {
                    results.add(parser.parse(is, path.toAbsolutePath().toString()));
                }
            }
        }
        return results;
    }
}
