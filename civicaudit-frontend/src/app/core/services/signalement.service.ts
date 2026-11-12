import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Signalement, SignalementRequest } from '../models/signalement.model';

@Injectable({ providedIn: 'root' })
export class SignalementService {
  private baseUrl = 'http://localhost:8080/api/marches';

  constructor(private http: HttpClient) {}

  getSignalements(marcheId: number): Observable<Signalement[]> {
    return this.http.get<Signalement[]>(`${this.baseUrl}/${marcheId}/signalements`);
  }

  creerSignalement(marcheId: number, request: SignalementRequest): Observable<Signalement> {
    return this.http.post<Signalement>(`${this.baseUrl}/${marcheId}/signalements`, request);
  }

  voterSignalement(marcheId: number, signalementId: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${marcheId}/signalements/${signalementId}/vote`, {});
  }
}
