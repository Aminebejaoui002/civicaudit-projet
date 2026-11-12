import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MarcheListe, MarcheDetail, Avenant, PageResult, ValeursFiltres } from '../models/marche.model';
import { environment } from '../../../environments/environment';

export interface MarcheFiltres {
  page?: number;
  taille?: number;
  tri?: string;
  dateDebut?: string;
  dateFin?: string;
  montantMin?: number | null;
  montantMax?: number | null;
  categorieCpv?: string;
  texte?: string;
  procedure?: string;
}

@Injectable({ providedIn: 'root' })
export class MarcheService {
  private baseUrl = `${environment.apiUrl}/marches`;

  constructor(private http: HttpClient) {}

  lister(filtres: MarcheFiltres): Observable<PageResult<MarcheListe>> {
    return this.http.get<PageResult<MarcheListe>>(this.baseUrl, { params: this.versParams(filtres) });
  }

  getDetail(id: number): Observable<MarcheDetail> {
    return this.http.get<MarcheDetail>(`${this.baseUrl}/${id}`);
  }

  getAvenants(id: number): Observable<Avenant[]> {
    return this.http.get<Avenant[]>(`${this.baseUrl}/${id}/avenants`);
  }

  /** Catégories CPV et procédures présentes dans les données (listes déroulantes de filtres). */
  getValeursFiltres(): Observable<ValeursFiltres> {
    return this.http.get<ValeursFiltres>(`${this.baseUrl}/filtres`);
  }

  /** Export CSV des lignes correspondant exactement aux filtres actifs (CDC 3.7). */
  exporterCSV(filtres: MarcheFiltres): Observable<Blob> {
    const { page, taille, ...sansPagination } = filtres;
    return this.http.get(`${this.baseUrl}/export`, {
      params: this.versParams(sansPagination),
      responseType: 'blob'
    });
  }

  private versParams(filtres: MarcheFiltres): HttpParams {
    let params = new HttpParams();
    Object.entries(filtres).forEach(([cle, valeur]) => {
      if (valeur !== undefined && valeur !== null && valeur !== '') {
        params = params.set(cle, String(valeur).trim());
      }
    });
    return params;
  }
}
