export interface Alerte {
  id: number;
  type: 'CONCENTRATION_ELEVEE' | 'MONTANT_ATYPIQUE' | 'SEUIL_SUSPECT';
  valeurCalculee: number;
  dateGeneration: string;
}

/*
 * Wording des alertes (CDC 3.5) : ce sont des signaux statistiques « à examiner », jamais des accusations.
 * Ton neutre et pédagogique, pas de « anomalie détectée » ni de « fraude », badge orange (jamais rouge).
 */

export function getAlerteTypeLabel(type: string): string {
  switch (type) {
    case 'CONCENTRATION_ELEVEE':
      return 'Fournisseur très présent — à examiner';
    case 'MONTANT_ATYPIQUE':
      return 'Montant atypique pour cette catégorie — à vérifier';
    case 'SEUIL_SUSPECT':
      return 'Montant proche du seuil de mise en concurrence — à examiner';
    default:
      return 'Point à examiner';
  }
}

export function getAlerteMessage(alerte: Alerte): string {
  const montant = (v: number) =>
    new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR', maximumFractionDigits: 0 }).format(v);
  switch (alerte.type) {
    case 'CONCENTRATION_ELEVEE':
      return `Le titulaire de ce marché représente ${alerte.valeurCalculee.toLocaleString('fr-FR')} % du montant total `
        + `des marchés de la collectivité sur les 3 dernières années (seuil d'attention : 40 %). `
        + `Cela peut s'expliquer par une spécialisation rare ou un marché pluriannuel important. `
        + `N'hésitez pas à commenter si vous avez des informations.`;
    case 'MONTANT_ATYPIQUE':
      return `Ce marché (${montant(alerte.valeurCalculee)}) représente un montant supérieur à ce qui est habituellement `
        + `observé pour cette catégorie sur cette collectivité. Cela peut avoir une explication légitime `
        + `(complexité du projet, urgence...) — n'hésitez pas à commenter si vous avez des informations.`;
    case 'SEUIL_SUSPECT':
      return `Ce marché (${montant(alerte.valeurCalculee)}) a été passé sans publicité ni mise en concurrence, pour un `
        + `montant proche du seuil réglementaire de 40 000 € HT au-delà duquel une mise en concurrence est requise. `
        + `Cette procédure est autorisée sous ce seuil ; ce signal invite simplement à vérifier le contexte.`;
    default:
      return 'Ce marché présente une caractéristique statistique qui peut mériter un examen.';
  }
}

export const ALERTE_AVERTISSEMENT =
  'Ces indicateurs sont calculés automatiquement à partir des données publiques. '
  + 'Ce sont des signaux à examiner, pas des constats d\'irrégularité.';
