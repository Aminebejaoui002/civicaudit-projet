import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CommentaireModeration, SignalementModeration } from '../models/moderation.model';
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
    return this.http.post<void>(`${this.apiUrl}/commentaires/${id}/approuver`, {});
  }

  rejeterCommentaire(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/commentaires/${id}/rejeter`, {});
  }

  getSignalements(): Observable<SignalementModeration[]> {
    return this.http.get<SignalementModeration[]>(`${this.apiUrl}/signalements`);
  }

  changerStatutSignalement(id: number, statut: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/signalements/${id}/statut`, null, {
      params: { statut }
    });
  }
}
