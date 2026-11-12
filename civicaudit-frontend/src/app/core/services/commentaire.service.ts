import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Commentaire, CommentaireRequest } from '../models/commentaire.model';

@Injectable({ providedIn: 'root' })
export class CommentaireService {
  private baseUrl = `${environment.apiUrl}/marches`;

  constructor(private http: HttpClient) {}

  getCommentaires(marcheId: number): Observable<Commentaire[]> {
    return this.http.get<Commentaire[]>(`${this.baseUrl}/${marcheId}/commentaires`);
  }

  creerCommentaire(marcheId: number, request: CommentaireRequest): Observable<Commentaire> {
    return this.http.post<Commentaire>(`${this.baseUrl}/${marcheId}/commentaires`, request);
  }

  supprimerCommentaire(marcheId: number, commentaireId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${marcheId}/commentaires/${commentaireId}`);
  }
}
