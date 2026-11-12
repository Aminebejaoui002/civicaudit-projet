import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Ajoute le JWT aux requêtes et ferme la session locale si le serveur répond 401
 * (token expiré au bout de 24 h, ou compte supprimé).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  const requete = token
    ? req.clone({ headers: req.headers.set('Authorization', `Bearer ${token}`) })
    : req;

  return next(requete).pipe(
    catchError((err: HttpErrorResponse) => {
      if (token && err.status === 401) {
        authService.logout();
      }
      return throwError(() => err);
    })
  );
};
