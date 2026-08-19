import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MarcheListe, MarcheDetail, Avenant, PageResult } from '../models/marche.model';

export interface MarcheFiltres {
  page?: number;
  taille?: number;
  tri?: string;
  dateDebut?: string;
  dateFin?: string;
  montantMin?: number;
  montantMax?: number;
  categorieCpv?: string;
  texte?: string;
  procedure?: string;
}

@Injectable({ providedIn: 'root' })
export class MarcheService {
  private baseUrl = 'http://localhost:8080/api/marches';

  constructor(private http: HttpClient) {}

  lister(filtres: MarcheFiltres): Observable<PageResult<MarcheListe>> {
    let params = new HttpParams();
    Object.entries(filtres).forEach(([cle, valeur]) => {
      if (valeur !== undefined && valeur !== null && valeur !== '') {
        params = params.set(cle, valeur.toString());
      }
    });
    return this.http.get<PageResult<MarcheListe>>(this.baseUrl, { params });
  }

  getDetail(id: number): Observable<MarcheDetail> {
    return this.http.get<MarcheDetail>(`${this.baseUrl}/${id}`);
  }

  getAvenants(id: number): Observable<Avenant[]> {
    return this.http.get<Avenant[]>(`${this.baseUrl}/${id}/avenants`);
  }
}