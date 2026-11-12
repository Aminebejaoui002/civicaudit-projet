// Les noms de champs correspondent exactement à StatistiquesDTO / TopFournisseurDTO côté backend
export interface Statistiques {
  montantTotalDepenses: number;
  nombreTotalMarches: number;
  nombreFournisseursUniques: number;
  montantParAnnee: { [annee: string]: number };
  topFournisseurs: TopFournisseur[];
  repartitionParProcedure: { [procedure: string]: number };
  repartitionParCategorieCpv: { [categorie: string]: number };
}

export interface TopFournisseur {
  nom: string;
  siret: string;
  montantCumule: number;
}
