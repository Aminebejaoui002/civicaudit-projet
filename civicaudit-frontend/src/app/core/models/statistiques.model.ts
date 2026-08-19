export interface Statistiques {
  montantTotal: number;
  nombreMarches: number;
  nombreFournisseurs: number;
  montantParAnnee: { [annee: string]: number };
  topFournisseurs: TopFournisseur[];
  repartitionProcedures: { [procedure: string]: number };
  repartitionCpv: { [cpv: string]: number };
}

export interface TopFournisseur {
  nom: string;
  siret: string;
  montant: number;
}
