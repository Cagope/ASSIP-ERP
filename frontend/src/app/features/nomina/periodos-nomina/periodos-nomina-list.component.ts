import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { PeriodosNominaApi, PeriodoNominaListDTO } from './periodos-nomina.api';
import { SessionService } from '../../../core/auth/session.service';

@Component({
  standalone: true,
  selector: 'app-periodos-nomina-list',
  templateUrl: './periodos-nomina-list.component.html',
  styleUrls: ['./periodos-nomina-list.component.scss'],
  imports: [CommonModule, RouterModule, FormsModule, HeaderActionsComponent],
})
export class PeriodosNominaListComponent implements OnInit {

  private readonly api = inject(PeriodosNominaApi);
  private readonly router = inject(Router);
  readonly session = inject(SessionService);

  items: PeriodoNominaListDTO[] = [];
  loading = false;

  // filtros
  anio: number | null = new Date().getFullYear();
  estado: string = '';
  q = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading = true;

    this.api.listar({
      agencia: null, // listado global
      anio: this.anio
    }).subscribe({
      next: (data) => {
        this.items = data ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error listando periodos', err);
        this.items = [];
        this.loading = false;
      }
    });
  }

  get filtrados(): PeriodoNominaListDTO[] {
    const q = (this.q ?? '').trim().toLowerCase();
    const estado = (this.estado ?? '').trim().toUpperCase();

    return (this.items ?? []).filter(x => {
      const okEstado = !estado || (x.estado ?? '').toUpperCase() === estado;
      const texto = `${x.anio}-${x.mes} ${x.tipoPeriodo ?? ''} ${x.estado ?? ''}`.toLowerCase();
      const okQ = !q || texto.includes(q);
      return okEstado && okQ;
    });
  }

  // =========================================================
  // 🔓 SOLO REVERSIÓN (CERRADO → ABIERTO)
  // =========================================================
  puedeReversar(p: PeriodoNominaListDTO): boolean {
    return (p.estado ?? '').toUpperCase() === 'CERRADO';
  }

  // =========================================================
  // Header → Generador (NO ejecuta procesos)
  // =========================================================
  generar(): void {
    this.router.navigate(['/nomina/periodos/generar']);
  }

  // =========================================================
  // 🔁 Reversar ejecución del período
  // =========================================================
  reversar(idPeriodo: number): void {

    const ok = confirm(
      `¿Reversar la ejecución del período #${idPeriodo}?\n\n` +
      `• Se eliminarán las liquidaciones\n` +
      `• Se eliminarán las novedades calculadas\n` +
      `• El período volverá a estado ABIERTO`
    );

    if (!ok) return;

    this.api.abrir(idPeriodo).subscribe({
      next: () => this.cargar(),
      error: (err) => {
        console.error('Error al reversar período', err);
        alert('No se pudo reversar el período.');
      }
    });
  }
}
