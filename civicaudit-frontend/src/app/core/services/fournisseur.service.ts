import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FournisseurDetail } from '../models/fournisseur.model';
import { MarcheListe, PageResult } from '../models/marche.model';

@Injectable({ providedIn: 'root' })
export class FournisseurService {
  private baseUrl = 'http://localhost:8080/api/fournisseurs';

  constructor(private http: HttpClient) {}

  getDetail(siret: string): Observable<FournisseurDetail> {
    return this.http.get<FournisseurDetail>(`${this.baseUrl}/${siret}`);
  }

  getMarches(siret: string, page: number = 0, taille: number = 20): Observable<PageResult<MarcheListe>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('taille', taille.toString());
    return this.http.get<PageResult<MarcheListe>>(`${this.baseUrl}/${siret}/marches`, { params });
  }
}
