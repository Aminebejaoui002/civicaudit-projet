import { Routes } from '@angular/router';
import { MarcheListeComponent } from './features/marches/marche-liste.component';
import { MarcheDetailComponent } from './features/marches/marche-detail.component';
import { FournisseurDetailComponent } from './features/fournisseurs/fournisseur-detail.component';
import { StatistiquesComponent } from './features/statistiques/statistiques.component';
import { LoginComponent } from './features/auth/login.component';
import { RegisterComponent } from './features/auth/register.component';

export const routes: Routes = [
  { path: '', redirectTo: 'marches', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'marches', component: MarcheListeComponent },
  { path: 'marches/:id', component: MarcheDetailComponent },
  { path: 'fournisseurs/:siret', component: FournisseurDetailComponent },
  { path: 'statistiques', component: StatistiquesComponent },
];