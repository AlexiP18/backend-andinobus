package com.andinobus.backendsmartcode.planificacion;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanificacionParseResult {
    private List<PlanificacionItem> items = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private String sourceName;

    public List<PlanificacionItem> getItems() {
        return items;
    }

    public void setItems(List<PlanificacionItem> items) {
        this.items = items;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public void addItem(PlanificacionItem item) {
        this.items.add(item);
    }
}