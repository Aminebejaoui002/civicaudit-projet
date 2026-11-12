package com.civicaudit.civicaudit_backend.service;

import com.civicaudit.civicaudit_backend.entity.Alerte;
import com.civicaudit.civicaudit_backend.entity.MarchePublic;
import com.civicaudit.civicaudit_backend.entity.Titulaire;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du module alert-engine (CDC 3.5) sur des jeux de données synthétiques,
 * pour vérifier les cas limites indépendamment des données réelles.
 */
class AlerteServiceTest {

    private static final LocalDate AUJOURDHUI = LocalDate.of(2026, 6, 1);

    // Le calcul est une fonction pure : pas besoin des repositories
    private final AlerteService service = new AlerteService(null, null);

    private long sequence = 1;

    private MarchePublic marche(String montant, LocalDate date, String cpv, String procedure, Titulaire... titulaires) {
        MarchePublic m = new MarchePublic();
        m.setId(sequence++);
        m.setUid("M" + m.getId());
        m.setObjet("Marché de test " + m.getId());
        m.setMontant(new BigDecimal(montant));
        m.setDateNotification(date);
        m.setCpvCode(cpv);
        m.setProcedure(procedure);
        m.setTitulaires(new ArrayList<>(List.of(titulaires)));
        return m;
    }

    private static Titulaire titulaire(String siret) {
        Titulaire t = new Titulaire();
        t.setId(siret);
        t.setNom("Entreprise " + siret);
        return t;
    }

    @Nested
    @DisplayName("Règle 1 : concentration fournisseur (> 40 % sur 3 ans)")
    class Concentration {

        private final Titulaire a = titulaire("11111111111111");
        private final Titulaire b = titulaire("22222222222222");
        private final Titulaire c = titulaire("33333333333333");

        @Test
        void alerteSurLesMarchesRecentsDuFournisseurDominant() {
            List<MarchePublic> marches = List.of(
                    marche("300000", AUJOURDHUI.minusMonths(2), null, null, a),  // récent (< 1 an)
                    marche("300000", AUJOURDHUI.minusYears(2), null, null, a),   // ancien (> 1 an) mais dans les 3 ans
                    marche("400000", AUJOURDHUI.minusMonths(3), null, null, b));

            List<Alerte> alertes = service.alertesConcentration(marches, AUJOURDHUI);

            // A = 600 000 / 1 000 000 = 60 % > 40 % → alerte uniquement sur son marché de moins d'un an
            assertThat(alertes).hasSize(1);
            assertThat(alertes.get(0).getType()).isEqualTo(Alerte.Type.CONCENTRATION_ELEVEE);
            assertThat(alertes.get(0).getMarche().getTitulaires()).containsExactly(a);
            assertThat(alertes.get(0).getValeurCalculee()).isEqualByComparingTo("60.00");
        }

        @Test
        void exactement40PourcentNeDeclenchePasDAlerte() {
            List<MarchePublic> marches = List.of(
                    marche("400000", AUJOURDHUI.minusMonths(1), null, null, a),
                    marche("300000", AUJOURDHUI.minusMonths(1), null, null, b),
                    marche("300000", AUJOURDHUI.minusMonths(1), null, null, c));

            assertThat(service.alertesConcentration(marches, AUJOURDHUI)).isEmpty();
        }

        @Test
        void lesMarchesDePlusDe3AnsNeSontPasComptes() {
            List<MarchePublic> marches = List.of(
                    marche("5000000", AUJOURDHUI.minusYears(4), null, null, a), // hors fenêtre de 3 ans
                    marche("100000", AUJOURDHUI.minusMonths(1), null, null, a),
                    marche("200000", AUJOURDHUI.minusMonths(1), null, null, b),
                    marche("200000", AUJOURDHUI.minusMonths(1), null, null, c));

            // Sur 3 ans : A = 20 %, B = 40 %, C = 40 % → aucun fournisseur au-delà de 40 %
            assertThat(service.alertesConcentration(marches, AUJOURDHUI)).isEmpty();
        }

        @Test
        void aucuneDonneeAucuneAlerte() {
            assertThat(service.alertesConcentration(List.of(), AUJOURDHUI)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Règle 2 : montant atypique (moyenne + 2 écarts-types par catégorie CPV)")
    class MontantAtypique {

        @Test
        void alerteSurLeMarcheHorsNorme() {
            List<MarchePublic> marches = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                marches.add(marche("10000", AUJOURDHUI, "45233140", "Procédure adaptée"));
            }
            MarchePublic horsNorme = marche("1000000", AUJOURDHUI, "45000000", "Procédure adaptée");
            marches.add(horsNorme);

            List<Alerte> alertes = service.alertesMontantAtypique(marches);

            assertThat(alertes).hasSize(1);
            assertThat(alertes.get(0).getType()).isEqualTo(Alerte.Type.MONTANT_ATYPIQUE);
            assertThat(alertes.get(0).getMarche()).isSameAs(horsNorme);
        }

        @Test
        void gardeFouAucuneAlerteSiMoinsDe5MarchesDansLaCategorie() {
            List<MarchePublic> marches = List.of(
                    marche("10000", AUJOURDHUI, "45000000", null),
                    marche("10000", AUJOURDHUI, "45000000", null),
                    marche("10000", AUJOURDHUI, "45000000", null),
                    marche("9000000", AUJOURDHUI, "45000000", null));

            assertThat(service.alertesMontantAtypique(marches)).isEmpty();
        }

        @Test
        void lesCategoriesSontCalculeesSeparement() {
            List<MarchePublic> marches = new ArrayList<>();
            // Catégorie 45 : montants élevés mais homogènes
            for (int i = 0; i < 10; i++) {
                marches.add(marche("2000000", AUJOURDHUI, "45000000", null));
            }
            // Catégorie 79 : montants faibles et homogènes
            for (int i = 0; i < 10; i++) {
                marches.add(marche("20000", AUJOURDHUI, "79000000", null));
            }
            // Un gros marché de travaux n'est pas atypique comparé aux autres travaux
            assertThat(service.alertesMontantAtypique(marches)).isEmpty();
        }

        @Test
        void marchesSansCodeCpvIgnores() {
            List<MarchePublic> marches = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                marches.add(marche("10000", AUJOURDHUI, null, null));
            }
            marches.add(marche("1000000", AUJOURDHUI, null, null));

            assertThat(service.alertesMontantAtypique(marches)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Règle 3 : procédure sans mise en concurrence entre 35 000 et 40 000 €")
    class SeuilSuspect {

        private static final String SANS_PUBLICITE = "Marché passé sans publicité ni mise en concurrence préalable";

        @Test
        void alerteDansLaZoneProcheDuSeuil() {
            List<Alerte> alertes = service.alertesSeuil(List.of(marche("38000", AUJOURDHUI, null, SANS_PUBLICITE)));

            assertThat(alertes).hasSize(1);
            assertThat(alertes.get(0).getType()).isEqualTo(Alerte.Type.SEUIL_SUSPECT);
            assertThat(alertes.get(0).getValeurCalculee()).isEqualByComparingTo("38000.00");
        }

        @Test
        void bornesIncluses() {
            assertThat(service.alertesSeuil(List.of(
                    marche("35000", AUJOURDHUI, null, SANS_PUBLICITE),
                    marche("40000", AUJOURDHUI, null, SANS_PUBLICITE)))).hasSize(2);
        }

        @Test
        void horsZoneAucuneAlerte() {
            assertThat(service.alertesSeuil(List.of(
                    marche("34999.99", AUJOURDHUI, null, SANS_PUBLICITE),
                    marche("40000.01", AUJOURDHUI, null, SANS_PUBLICITE)))).isEmpty();
        }

        @Test
        void procedureAvecMiseEnConcurrenceAucuneAlerte() {
            assertThat(service.alertesSeuil(List.of(
                    marche("38000", AUJOURDHUI, null, "Procédure adaptée"),
                    marche("38000", AUJOURDHUI, null, null)))).isEmpty();
        }

        @Test
        void reconnaitLesLibellesNegocieSans() {
            assertThat(AlerteService.estProcedureSansMiseEnConcurrence("Procédure négociée sans publicité")).isTrue();
            assertThat(AlerteService.estProcedureSansMiseEnConcurrence("Marché négocié sans mise en concurrence")).isTrue();
            assertThat(AlerteService.estProcedureSansMiseEnConcurrence("Appel d'offres ouvert")).isFalse();
        }
    }

    @Test
    @DisplayName("calculerAlertes combine les 3 règles")
    void calculerAlertesCombineLesTroisRegles() {
        Titulaire seul = titulaire("44444444444444");
        List<MarchePublic> marches = List.of(
                marche("38000", AUJOURDHUI.minusMonths(1), null,
                        "Procédure négociée sans publicité ni mise en concurrence", seul));

        List<Alerte> alertes = service.calculerAlertes(marches, AUJOURDHUI);

        // Un seul fournisseur = 100 % (règle 1) + zone de seuil (règle 3) ; pas de CPV (règle 2 non applicable)
        assertThat(alertes).extracting(Alerte::getType)
                .containsExactlyInAnyOrder(Alerte.Type.CONCENTRATION_ELEVEE, Alerte.Type.SEUIL_SUSPECT);
    }
}
