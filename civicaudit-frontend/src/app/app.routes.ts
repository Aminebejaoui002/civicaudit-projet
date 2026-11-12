import { Routes } from '@angular/router';
import { MarcheListeComponent } from './features/marches/marche-liste.component';
import { MarcheDetailComponent } from './features/marches/marche-detail.component';
import { FournisseurDetailComponent } from './features/fournisseurs/fournisseur-detail.component';
import { StatistiquesComponent } from './features/statistiques/statistiques.component';
import { LoginComponent } from './features/auth/login.component';
import { RegisterComponent } from './features/auth/register.component';
import { ModerationComponent } from './features/moderation/moderation.component';
import { MentionsLegalesComponent } from './features/legal/mentions-legales.component';
import { PolitiqueConfidentialiteComponent } from './features/legal/politique-confidentialite.component';
import { CgusComponent } from './features/legal/cgus.component';
import { MonCompteComponent } from './features/compte/mon-compte.component';
import { AdminImportComponent } from './features/admin/admin-import.component';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard, moderateurGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'marches', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'marches', component: MarcheListeComponent },
  { path: 'marches/:id', component: MarcheDetailComponent },
  { path: 'fournisseurs/:siret', component: FournisseurDetailComponent },
  { path: 'statistiques', component: StatistiquesComponent },
  { path: 'moderation', component: ModerationComponent, canActivate: [moderateurGuard] },
  { path: 'admin/import', component: AdminImportComponent, canActivate: [adminGuard] },
  { path: 'mon-compte', component: MonCompteComponent, canActivate: [authGuard] },
  { path: 'mentions-legales', component: MentionsLegalesComponent },
  { path: 'politique-confidentialite', component: PolitiqueConfidentialiteComponent },
  { path: 'cgus', component: CgusComponent },
  { path: '**', redirectTo: 'marches' },
];