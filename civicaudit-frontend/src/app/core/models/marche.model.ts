export interface MarcheListe {
  id: number;
  objet: string;
  montant: number; // montant actuel (dernier avenant en date, sinon montant d'origine)
  titulairePrincipal: string;
  dateNotification: string;
  procedure: string;
  cpvCode: string | null;
  categorieCpvLibelle: string;
  sourceUrl: string | null;
}

export interface Titulaire {
  siret: string;
  nom: string;
}

export interface MarcheDetail {
  id: number;
  objet: string;
  montant: number;
  acheteurNom: string;
  titulaires: Titulaire[];
  dateNotification: string;
  dureeMois: number | null;
  procedure: string;
  cpvCode: string;
  categorieCpvLibelle: string;
  sourceFilename: string;
  sourceUrl: string;
  aDesAvenants: boolean;
  alertes: Alerte[];
  nombreCommentaires: number;
  nombreSignalements: number;
}

export interface Alerte {
  id: number;
  type: 'CONCENTRATION_ELEVEE' | 'MONTANT_ATYPIQUE' | 'SEUIL_SUSPECT';
  valeurCalculee: number;
  dateGeneration: string;
}

export interface Avenant {
  id: number;
  montantApres: number;
  dureeApresMois: number | null;
  dateAvenant: string;
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface CategorieCpv {
  code: string;
  libelle: string;
}

export interface ValeursFiltres {
  categoriesCpv: CategorieCpv[];
  procedures: string[];
}
