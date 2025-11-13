package com.andinobus.backendsmartcode.planificacion;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class CsvPlanificacionParserTest {

    private final CsvPlanificacionParser parser = new CsvPlanificacionParser();

    @Test
    void parse_withBasicHeadersAndComma() {
        String csv = "ORIGEN,DESTINO,HORA DE SALIDA\n" +
                "Ambato,Quito,07:30\n" +
                "Latacunga,Cuenca,14:00\n";
        PlanificacionParseResult result = parser.parse(csv, "test");
        assertEquals(2, result.getItems().size());
        assertEquals("Ambato", result.getItems().get(0).origen());
        assertEquals("Quito", result.getItems().get(0).destino());
        assertEquals(LocalTime.of(7,30), result.getItems().get(0).horaSalida());
        assertNull(result.getItems().get(0).horaLlegada());
        assertNull(result.getItems().get(0).error());
    }

    @Test
    void parse_withArrivalAndSemicolon() {
        String csv = "Ciudad Origen;Ciudad Destino;Hora de Salida;Hora de Llegada\n" +
                "Ambato;Quito;730;07:00\n" +
                "Riobamba;Guayaquil;14h30;17.45\n";
        PlanificacionParseResult result = parser.parse(csv, "test");
        assertEquals(2, result.getItems().size());
        assertEquals(LocalTime.of(7,30), result.getItems().get(0).horaSalida());
        assertEquals(LocalTime.of(7,0), result.getItems().get(0).horaLlegada());
        assertEquals(LocalTime.of(14,30), result.getItems().get(1).horaSalida());
        assertEquals(LocalTime.of(17,45), result.getItems().get(1).horaLlegada());
    }

    @Test
    void parse_invalidTimesProduceErrorButStillReturnItems() {
        String csv = "ORIGEN,DESTINO,HORA DE SALIDA,HORA DE LLEGADA\n" +
                "Ambato,Quito,25:99,10:00\n";
        PlanificacionParseResult result = parser.parse(csv, "test");
        assertEquals(1, result.getItems().size());
        PlanificacionItem item = result.getItems().get(0);
        assertNull(item.horaSalida());
        assertEquals(LocalTime.of(10,0), item.horaLlegada());
        assertNotNull(item.error());
        assertTrue(item.error().contains("SALIDA"));
    }
}
