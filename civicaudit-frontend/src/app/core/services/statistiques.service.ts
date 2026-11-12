import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Statistiques } from '../models/statistiques.model';

@Injectable({ providedIn: 'root' })
export class StatistiquesService {
  private baseUrl = `${environment.apiUrl}/statistiques`;

  constructor(private http: HttpClient) {}

  getStatistiques(): Observable<Statistiques> {
    return this.http.get<Statistiques>(this.baseUrl);
  }
}
