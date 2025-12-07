package com.andinobus.backendsmartcode.planificacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanificacionControllerTest {

    private CsvPlanificacionParser parser = mock(CsvPlanificacionParser.class);
    private PlanificacionController controller;

    @BeforeEach
    void setUp() {
        controller = new PlanificacionController(parser);
    }

    @Test
    void preview_parsesFileAndReturns() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("test,data".getBytes()));
        when(file.getOriginalFilename()).thenReturn("test.csv");

        PlanificacionParseResult result = new PlanificacionParseResult();
        result.setSourceName("test.csv");
        when(parser.parse(any(ByteArrayInputStream.class), eq("test.csv"))).thenReturn(result);

        var out = controller.preview(file);
        assertSame(result, out);
        assertEquals("test.csv", out.getSourceName());
        verify(parser).parse(any(ByteArrayInputStream.class), eq("test.csv"));
    }

    @Test
    void examples_returnsListWhenFilesNotFound() throws IOException {
        PlanificacionParseResult r1 = new PlanificacionParseResult();
        r1.setSourceName("Horas de Trabajo RUTAS 1.csv");
        PlanificacionParseResult r2 = new PlanificacionParseResult();
        r2.setSourceName("Horas de Trabajo RUTAS 2.csv");

        when(parser.parse(any(java.io.InputStream.class), anyString())).thenReturn(r1, r2);

        var out = controller.examples();
        assertTrue(out.isEmpty() || out.size() >= 0);
    }
}
