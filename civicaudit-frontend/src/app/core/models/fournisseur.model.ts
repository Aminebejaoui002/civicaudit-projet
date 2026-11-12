export interface FournisseurDetail {
  siret: string;
  nom: string;
  nombreMarches: number;
  montantTotalCumule: number; // même nom que FournisseurDTO côté backend
  pourcentageConcentration: number;
}
