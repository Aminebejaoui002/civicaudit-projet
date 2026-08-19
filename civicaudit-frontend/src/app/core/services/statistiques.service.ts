import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Statistiques } from '../models/statistiques.model';

@Injectable({ providedIn: 'root' })
export class StatistiquesService {
  private baseUrl = 'http://localhost:8080/api/statistiques';

  constructor(private http: HttpClient) {}

  getStatistiques(): Observable<Statistiques> {
    return this.http.get<Statistiques>(this.baseUrl);
  }
}
