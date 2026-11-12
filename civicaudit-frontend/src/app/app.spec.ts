import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { App } from './app';
import { getAlerteMessage, getAlerteTypeLabel } from './core/models/alerte.model';

describe('App', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), provideHttpClient()]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render the navbar with public links only when logged out', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();
    const html = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(html).toContain('CivicAudit');
    expect(html).toContain('Statistiques');
    expect(html).not.toContain('Modération');
  });
});

describe('Wording des alertes (CDC 3.5)', () => {
  const types = ['CONCENTRATION_ELEVEE', 'MONTANT_ATYPIQUE', 'SEUIL_SUSPECT'] as const;

  it('reste neutre : jamais « fraude », « anomalie détectée » ni « suspect »', () => {
    for (const type of types) {
      const texte = (getAlerteTypeLabel(type) + ' ' +
        getAlerteMessage({ id: 1, type, valeurCalculee: 38000, dateGeneration: '2026-01-01' })).toLowerCase();
      expect(texte).not.toContain('fraude');
      expect(texte).not.toContain('anomalie détectée');
      expect(texte).not.toContain('suspect');
      expect(texte).toMatch(/examiner|vérifier/);
    }
  });
});
