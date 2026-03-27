import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';

import {
  NovedadesNominaApi,
  NovedadNominaListDTO
} from './novedades-nomina.api';

@Component({
  standalone: true,
  selector: 'app-novedades-nomina-list',
  templateUrl: './novedades-nomina-list.component.html',
  styleUrls: ['./novedades-nomina-list.component.scss'],
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    HeaderActionsComponent
  ],
})
export class NovedadesNominaListComponent implements OnInit {

  private readonly api = inject(NovedadesNominaApi);
  private readonly router = inject(Router);

  items: NovedadNominaListDTO[] = [];
  loading = false;

  // filtros
  q = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {

    this.loading = true;

    this.api.listar().subscribe({
      next: d => {
        this.items = d ?? [];
        this.loading = false;
      },
      error: err => {
        console.error(err);
        this.items = [];
        this.loading = false;
      }
    });
  }

  get filtrados(): NovedadNominaListDTO[] {

    const q = this.q.trim().toLowerCase();

    if (!q) return this.items;

    return this.items.filter(x => {

      const texto = `
        ${x.codigoConcepto}
        ${x.observacion}
        ${x.estado}
      `.toLowerCase();

      return texto.includes(q);
    });
  }

  // =========================
  // Header actions
  // =========================

  nuevo(): void {
    this.router.navigate(['/nomina/novedades/nuevo']);
  }

  editar(id: number): void {
    this.router.navigate(['/nomina/novedades', id]);
  }

  eliminar(id: number): void {

    if (!confirm('¿Eliminar novedad?')) return;

    this.api.eliminar(id).subscribe({
      next: () => this.cargar(),
      error: () => alert('No se pudo eliminar.')
    });
  }
}
