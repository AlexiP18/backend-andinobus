package com.andinobus.backendsmartcode.planificacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlanificacionParseResultTest {

    private PlanificacionParseResult result;

    @BeforeEach
    void setUp() {
        result = new PlanificacionParseResult();
    }

    @Test
    void initialState_isEmpty() {
        assertTrue(result.getItems().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
        assertNull(result.getSourceName());
    }

    @Test
    void setSourceName_storesValue() {
        result.setSourceName("test.csv");
        assertEquals("test.csv", result.getSourceName());
    }

    @Test
    void addItem_addsToList() {
        PlanificacionItem item = new PlanificacionItem("A", "B", LocalTime.of(10, 0), LocalTime.of(12, 0), 1, "10:00", "12:00", null);
        result.addItem(item);
        assertEquals(1, result.getItems().size());
        assertSame(item, result.getItems().get(0));
    }

    @Test
    void addWarning_addsToList() {
        result.addWarning("Warning 1");
        result.addWarning("Warning 2");
        assertEquals(2, result.getWarnings().size());
        assertEquals("Warning 1", result.getWarnings().get(0));
        assertEquals("Warning 2", result.getWarnings().get(1));
    }

    @Test
    void setItems_replacesItemList() {
        PlanificacionItem item1 = new PlanificacionItem("A", "B", LocalTime.of(10, 0), LocalTime.of(12, 0), 1, "10:00", "12:00", null);
        PlanificacionItem item2 = new PlanificacionItem("C", "D", LocalTime.of(14, 0), LocalTime.of(16, 0), 2, "14:00", "16:00", null);
        result.setItems(List.of(item1, item2));
        assertEquals(2, result.getItems().size());
    }

    @Test
    void setWarnings_replacesWarningList() {
        result.setWarnings(List.of("W1", "W2", "W3"));
        assertEquals(3, result.getWarnings().size());
        assertEquals("W1", result.getWarnings().get(0));
    }

    @Test
    void multipleAdditions_preservesOrder() {
        PlanificacionItem item1 = new PlanificacionItem("A", "B", LocalTime.of(10, 0), LocalTime.of(12, 0), 1, "10:00", "12:00", null);
        PlanificacionItem item2 = new PlanificacionItem("C", "D", LocalTime.of(14, 0), LocalTime.of(16, 0), 2, "14:00", "16:00", null);
        result.addItem(item1);
        result.addWarning("warn1");
        result.addItem(item2);
        result.addWarning("warn2");
        
        assertEquals(2, result.getItems().size());
        assertEquals(2, result.getWarnings().size());
        assertEquals("A", result.getItems().get(0).origen());
        assertEquals("C", result.getItems().get(1).origen());
    }
}
