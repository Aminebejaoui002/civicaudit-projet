import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface RapportImport {
  fichier: string;
  lignesLues: number;
  lignesHorsPerimetre: number;
  marchesTrouves: number;
  marchesImportes: number;
  marchesMisAJour: number;
  marchesRejetes: number;
  raisonsRejet: string[];
}

export interface ResultatImport {
  rapport: RapportImport;
  alertesGenerees: number;
}

export interface ErreurImport {
  id: number;
  uidMarche: string | null;
  ligneBrute: string;
  raison: string;
  sourceFilename: string;
  dateImport: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  importer(fichier: File): Observable<ResultatImport> {
    const formData = new FormData();
    formData.append('fichier', fichier);
    return this.http.post<ResultatImport>(`${this.apiUrl}/import`, formData);
  }

  getErreurs(): Observable<ErreurImport[]> {
    return this.http.get<ErreurImport[]>(`${this.apiUrl}/import/erreurs`);
  }
}
