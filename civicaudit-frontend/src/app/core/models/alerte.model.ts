export interface Alerte {
  id: number;
  type: 'CONCENTRATION_ELEVEE' | 'MONTANT_ATYPIQUE' | 'SEUIL_SUSPECT';
  valeurCalculee: number;
  dateGeneration: string;
}

export function getAlerteMessage(alerte: Alerte): string {
  switch (alerte.type) {
    case 'CONCENTRATION_ELEVEE':
      return `Concentration élevée : ${alerte.valeurCalculee.toFixed(2)}% du montant total`;
    case 'MONTANT_ATYPIQUE':
      return `Montant atypique pour cette catégorie : ${alerte.valeurCalculee.toLocaleString('fr-FR')} €`;
    case 'SEUIL_SUSPECT':
      return `Procédure sans publicité proche du seuil : ${alerte.valeurCalculee.toLocaleString('fr-FR')} €`;
    default:
      return 'Alerte';
  }
}

export function getAlerteTypeLabel(type: string): string {
  switch (type) {
    case 'CONCENTRATION_ELEVEE':
      return 'Concentration fournisseur';
    case 'MONTANT_ATYPIQUE':
      return 'Montant atypique';
    case 'SEUIL_SUSPECT':
      return 'Seuil suspect';
    default:
      return type;
  }
}
