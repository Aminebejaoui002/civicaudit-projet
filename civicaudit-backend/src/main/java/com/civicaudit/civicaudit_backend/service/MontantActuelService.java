package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.entity.Avenant;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.repository.AvenantRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MontantActuelService {

    private final AvenantRepository avenantRepository;

    public MontantActuelService(AvenantRepository avenantRepository) {
        this.avenantRepository = avenantRepository;
    }

    // Calcule, pour tous les marchés, le montant "actuel" (dernier avenant si existant, sinon montant d'origine).
    // Une seule requête pour tous les avenants, regroupement en mémoire (volume gérable : ~1000 avenants).
    public Map<Long, BigDecimal> calculerMontantsActuels(List<MarchePublic> marches) {
        List<Avenant> tousLesAvenants = avenantRepository.findAll();

        Map<Long, Avenant> dernierAvenantParMarche = new HashMap<>();
        for (Avenant a : tousLesAvenants) {
            Long marcheId = a.getMarchePublic().getId();
            Avenant actuel = dernierAvenantParMarche.get(marcheId);
            if (actuel == null || a.getDateAvenant().isAfter(actuel.getDateAvenant())) {
                dernierAvenantParMarche.put(marcheId, a);
            }
        }

        Map<Long, BigDecimal> resultat = new HashMap<>();
        for (MarchePublic m : marches) {
            Avenant dernier = dernierAvenantParMarche.get(m.getId());
            resultat.put(m.getId(), dernier != null ? dernier.getMontantApres() : m.getMontant());
        }
        return resultat;
    }
}
