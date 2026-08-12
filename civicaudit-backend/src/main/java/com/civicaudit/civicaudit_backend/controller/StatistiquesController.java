package com.civicaudit.civicaudit_backend.controller;

import com.civicaudit.civicaudit_backend.dto.StatistiquesDTO;
import com.civicaudit.civicaudit_backend.service.StatistiquesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistiques")
public class StatistiquesController {

    private final StatistiquesService statistiquesService;

    public StatistiquesController(StatistiquesService statistiquesService) {
        this.statistiquesService = statistiquesService;
    }

    @GetMapping
    public StatistiquesDTO statistiques() {
        return statistiquesService.calculer();
    }
}