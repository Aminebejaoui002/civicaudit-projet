import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <nav class="navbar">
      <div class="navbar-content">
        <a routerLink="/marches" class="navbar-brand">
          CivicAudit
        </a>
        
        <div class="navbar-links">
          <a routerLink="/marches" routerLinkActive="active">Marchés</a>
          <a routerLink="/statistiques" routerLinkActive="active">Statistiques</a>
          
          <div class="navbar-auth">
            <ng-container *ngIf="(authService.currentUser$ | async) as user; else notConnected">
              <span class="user-info">{{ user.email }}</span>
              <button (click)="logout()" class="btn-logout">Déconnexion</button>
            </ng-container>
            <ng-template #notConnected>
              <a routerLink="/login" class="btn-login">Connexion</a>
              <a routerLink="/register" class="btn-register">Inscription</a>
            </ng-template>
          </div>
        </div>
      </div>
    </nav>
  `,
  styles: [`
    .navbar {
      background: white;
      border-bottom: 1px solid var(--border);
      padding: 1rem 0;
      position: sticky;
      top: 0;
      z-index: 100;
    }
    
    .navbar-content {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 1.5rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .navbar-brand {
      font-size: 1.25rem;
      font-weight: 700;
      color: var(--primary);
      text-decoration: none;
    }
    
    .navbar-links {
      display: flex;
      align-items: center;
      gap: 2rem;
      
      a {
        color: var(--text);
        text-decoration: none;
        font-weight: 500;
        transition: color 0.2s;
        
        &:hover {
          color: var(--primary);
        }
        
        &.active {
          color: var(--primary);
        }
      }
    }
    
    .navbar-auth {
      display: flex;
      align-items: center;
      gap: 1rem;
      margin-left: 2rem;
      padding-left: 2rem;
      border-left: 1px solid var(--border);
    }
    
    .user-info {
      font-size: 0.875rem;
      color: var(--text-muted);
    }
    
    .btn-login,
    .btn-logout {
      padding: 0.5rem 1rem;
      border-radius: 6px;
      font-weight: 500;
      transition: all 0.2s;
      font-size: 0.875rem;
    }
    
    .btn-login {
      color: var(--primary);
      background: transparent;
      text-decoration: none;
      
      &:hover {
        background: var(--bg-light);
      }
    }
    
    .btn-logout {
      background: transparent;
      border: 1px solid var(--border);
      color: var(--text);
      cursor: pointer;
      
      &:hover {
        background: var(--bg-light);
      }
    }
    
    .btn-register {
      padding: 0.5rem 1rem;
      background: var(--primary);
      color: white;
      border-radius: 6px;
      font-weight: 500;
      font-size: 0.875rem;
      text-decoration: none;
      transition: background 0.2s;
      
      &:hover {
        background: #2563eb;
      }
    }
    
    @media (max-width: 768px) {
      .navbar-content {
        flex-direction: column;
        gap: 1rem;
      }
      
      .navbar-links {
        flex-direction: column;
        gap: 0.75rem;
      }
      
      .navbar-auth {
        margin-left: 0;
        padding-left: 0;
        border-left: none;
        padding-top: 0.75rem;
        border-top: 1px solid var(--border);
      }
    }
  `]
})
export class NavbarComponent {
  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/marches']);
  }
}
