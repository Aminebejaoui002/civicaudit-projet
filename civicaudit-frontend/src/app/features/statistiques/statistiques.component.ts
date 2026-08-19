import { Component, OnInit, ChangeDetectorRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { StatistiquesService } from '../../core/services/statistiques.service';
import { Statistiques } from '../../core/models/statistiques.model';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-statistiques',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './statistiques.component.html',
  styleUrl: './statistiques.component.scss'
})
export class StatistiquesComponent implements OnInit, AfterViewInit {
  stats: Statistiques | null = null;
  chargement = true;
  erreur = false;

  private charts: Chart[] = [];

  constructor(
    private statistiquesService: StatistiquesService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.chargerStatistiques();
  }

  ngAfterViewInit(): void {
    // Les graphiques seront créés après le chargement des données
  }

  chargerStatistiques(): void {
    this.chargement = true;
    this.statistiquesService.getStatistiques().subscribe({
      next: (stats) => {
        this.stats = stats;
        this.chargement = false;
        this.cdr.detectChanges();
        
        // Créer les graphiques après que les données soient chargées et le DOM mis à jour
        setTimeout(() => this.creerGraphiques(), 100);
      },
      error: () => {
        this.erreur = true;
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }

  creerGraphiques(): void {
    if (!this.stats) return;

    // Détruire les graphiques existants
    this.charts.forEach(chart => chart.destroy());
    this.charts = [];

    // Graphique montant par année
    const canvasMontant = document.getElementById('chartMontantAnnee') as HTMLCanvasElement;
    if (canvasMontant) {
      const annees = Object.keys(this.stats.montantParAnnee).sort();
      const montants = annees.map(a => this.stats!.montantParAnnee[a]);
      
      this.charts.push(new Chart(canvasMontant, {
        type: 'bar',
        data: {
          labels: annees,
          datasets: [{
            label: 'Montant total (€)',
            data: montants,
            backgroundColor: 'rgba(59, 130, 246, 0.8)',
            borderColor: 'rgb(59, 130, 246)',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false },
            tooltip: {
              callbacks: {
                label: (context) => this.formatMontant(context.parsed.y as number)
              }
            }
          },
          scales: {
            y: {
              beginAtZero: true,
              ticks: {
                callback: (value) => this.formatMontantCourt(Number(value))
              }
            }
          }
        }
      }));
    }

    // Graphique top fournisseurs
    const canvasFournisseurs = document.getElementById('chartTopFournisseurs') as HTMLCanvasElement;
    if (canvasFournisseurs && this.stats.topFournisseurs.length > 0) {
      const top10 = this.stats.topFournisseurs.slice(0, 10);
      const noms = top10.map(f => this.truncateText(f.nom, 30));
      const montants = top10.map(f => f.montant);
      
      this.charts.push(new Chart(canvasFournisseurs, {
        type: 'bar',
        data: {
          labels: noms,
          datasets: [{
            label: 'Montant cumulé (€)',
            data: montants,
            backgroundColor: 'rgba(16, 185, 129, 0.8)',
            borderColor: 'rgb(16, 185, 129)',
            borderWidth: 1
          }]
        },
        options: {
          indexAxis: 'y',
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false },
            tooltip: {
              callbacks: {
                label: (context) => this.formatMontant(context.parsed.x as number)
              }
            }
          },
          scales: {
            x: {
              beginAtZero: true,
              ticks: {
                callback: (value) => this.formatMontantCourt(Number(value))
              }
            }
          }
        }
      }));
    }

    // Graphique répartition procédures
    const canvasProcedures = document.getElementById('chartProcedures') as HTMLCanvasElement;
    if (canvasProcedures) {
      const procedures = Object.keys(this.stats.repartitionProcedures);
      const valeurs = Object.values(this.stats.repartitionProcedures);
      
      this.charts.push(new Chart(canvasProcedures, {
        type: 'bar',
        data: {
          labels: procedures.map(p => this.truncateText(p, 25)),
          datasets: [{
            label: 'Nombre de marchés',
            data: valeurs,
            backgroundColor: 'rgba(245, 158, 11, 0.8)',
            borderColor: 'rgb(245, 158, 11)',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false }
          },
          scales: {
            y: { beginAtZero: true }
          }
        }
      }));
    }

    // Graphique répartition CPV
    const canvasCpv = document.getElementById('chartCpv') as HTMLCanvasElement;
    if (canvasCpv) {
      const cpvs = Object.keys(this.stats.repartitionCpv);
      const valeurs = Object.values(this.stats.repartitionCpv);
      
      this.charts.push(new Chart(canvasCpv, {
        type: 'bar',
        data: {
          labels: cpvs.map(c => this.truncateText(c, 30)),
          datasets: [{
            label: 'Nombre de marchés',
            data: valeurs,
            backgroundColor: 'rgba(139, 92, 246, 0.8)',
            borderColor: 'rgb(139, 92, 246)',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false }
          },
          scales: {
            y: { beginAtZero: true }
          }
        }
      }));
    }
  }

  formatMontant(montant: number): string {
    return new Intl.NumberFormat('fr-FR', { 
      style: 'currency', 
      currency: 'EUR', 
      maximumFractionDigits: 0 
    }).format(montant);
  }

  formatMontantCourt(montant: number): string {
    if (montant >= 1000000) {
      return (montant / 1000000).toFixed(1) + 'M€';
    }
    if (montant >= 1000) {
      return (montant / 1000).toFixed(0) + 'k€';
    }
    return montant + '€';
  }

  truncateText(text: string, maxLength: number): string {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength - 3) + '...';
  }

  ngOnDestroy(): void {
    // Nettoyer les graphiques
    this.charts.forEach(chart => chart.destroy());
  }
}
