export interface MarcheListe {
  id: number;
  objet: string;
  montant: number;
  titulairePrincipal: string;
  dateNotification: string;
  procedure: string;
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
  sourceFilename: string;
  aDesAvenants: boolean;
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