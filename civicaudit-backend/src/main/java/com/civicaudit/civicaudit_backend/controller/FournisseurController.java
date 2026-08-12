package com.civicaudit.civicaudit_backend.controller;

import com.civicaudit.civicaudit_backend.dto.FournisseurDTO;
import com.civicaudit.civicaudit_backend.service.FournisseurService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fournisseurs")
public class FournisseurController {

    private final FournisseurService fournisseurService;

    public FournisseurController(FournisseurService fournisseurService) {
        this.fournisseurService = fournisseurService;
    }

    @GetMapping("/{siret}")
    public FournisseurDTO detail(@PathVariable String siret) {
        return fournisseurService.getFournisseur(siret);
    }

    @GetMapping("/{siret}/marches")
    public Object marches(@PathVariable String siret) {
        return fournisseurService.getFournisseur(siret).getMarches();
    }
}