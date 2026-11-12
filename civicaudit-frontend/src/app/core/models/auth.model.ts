export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  consentementRgpd: boolean; // consentement RGPD explicite (case non pré-cochée)
}

export interface AuthResponse {
  token: string;
  email: string;
  role: string;
}

export interface User {
  email: string;
  role: 'USER' | 'MODERATEUR' | 'ADMIN';
}
