package com.civicaudit.civicaudit_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AlerteDTO {
    private Long id;
    private String type;
    private BigDecimal valeurCalculee;
    private LocalDateTime dateGeneration;

    public AlerteDTO(Long id, String type, BigDecimal valeurCalculee, LocalDateTime dateGeneration) {
        this.id = id;
        this.type = type;
        this.valeurCalculee = valeurCalculee;
        this.dateGeneration = dateGeneration;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getValeurCalculee() {
        return valeurCalculee;
    }

    public LocalDateTime getDateGeneration() {
        return dateGeneration;
    }
}
