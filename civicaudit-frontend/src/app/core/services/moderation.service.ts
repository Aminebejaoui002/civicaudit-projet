import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CommentaireModeration, SignalementModeration } from '../models/moderation.model';
import { SignalementStatut } from '../models/signalement.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ModerationService {
  private apiUrl = `${environment.apiUrl}/moderation`;

  constructor(private http: HttpClient) {}

  getCommentairesEnAttente(): Observable<CommentaireModeration[]> {
    return this.http.get<CommentaireModeration[]>(`${this.apiUrl}/commentaires`);
  }

  approuverCommentaire(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/commentaires/${id}`, { statut: 'APPROUVE' });
  }

  rejeterCommentaire(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/commentaires/${id}`, { statut: 'REJETE' });
  }

  getSignalements(): Observable<SignalementModeration[]> {
    return this.http.get<SignalementModeration[]>(`${this.apiUrl}/signalements`);
  }

  /** PATCH /api/signalements/{id}/statut (CDC 5.4, rôle MODERATEUR). */
  changerStatutSignalement(id: number, statut: SignalementStatut): Observable<void> {
    return this.http.patch<void>(`${environment.apiUrl}/signalements/${id}/statut`, { statut });
  }
}
