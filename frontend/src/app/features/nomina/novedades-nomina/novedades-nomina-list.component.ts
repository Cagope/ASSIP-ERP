import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';

import {
  NovedadesNominaApi,
  NovedadNominaListDTO
} from './novedades-nomina.api';

import { NovedadesNominaExporterService } from './novedades-nomina-exporter.service';
import { NovedadesNominaPrintService } from './novedades-nomina-print.service';
import { SessionService } from '../../../core/auth/session.service';

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
  private readonly exporterService = inject(NovedadesNominaExporterService);
  private readonly printService = inject(NovedadesNominaPrintService);
  public readonly session = inject(SessionService);

  items: NovedadNominaListDTO[] = [];
  loading = false;

  agencias: any[] = [];
  idAgencia: number | null = null;

  // filtros
  q = '';

  ngOnInit(): void {
    this.cargarAgencias();
  }

  private cargarAgencias(): void {
    this.agencias = this.session.getAgencias?.() ?? [];

    if (this.agencias.length === 1) {
      this.idAgencia = this.agencias[0].idAgencia;
      this.cargar();
      return;
    }

    if (this.agencias.length > 1) {
      this.idAgencia = this.agencias[0].idAgencia;
      this.cargar();
      return;
    }

    this.items = [];
  }

  cargar(): void {

    if (!this.idAgencia) {
      this.items = [];
      return;
    }

    this.loading = true;

    this.api.listar(this.idAgencia).subscribe({
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

  onAgenciaChange(): void {
    this.cargar();
  }

  get filtrados(): NovedadNominaListDTO[] {

    const q = this.q.trim().toLowerCase();

    if (!q) return this.items;

    return this.items.filter(x => {

      const texto = `
        ${x.codigoConcepto}
        ${x.observacion}
        ${x.estado}
        ${x.documentoEmpleado ?? ''}
        ${x.nombreEmpleado ?? ''}
      `.toLowerCase();

      return texto.includes(q);
    });
  }

  nuevo(): void {
    this.router.navigate(['/nomina/novedades/nuevo']);
  }

  editar(id: number): void {
    this.router.navigate(['/nomina/novedades', id]);
  }

  eliminar(item: NovedadNominaListDTO): void {

    const mensaje =
      `¿Eliminar novedad?\n\n` +
      `${item.nombreEmpleado ?? ''}\n` +
      `Concepto: ${item.codigoConcepto}`;

    const ok = confirm(mensaje);
    if (!ok) return;

    this.api.eliminar(item.idNovedad).subscribe({
      next: () => this.cargar(),
      error: () => alert('No se pudo eliminar.')
    });
  }

  exportar(): void {
    this.exporterService.exportar(this.filtrados);
  }

  imprimir(): void {
    this.printService.imprimir(this.filtrados);
  }
}
