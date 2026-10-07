import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { SessionService } from '../../../core/auth/session.service';

import { CesantiasApi, CesantiasDTO } from './cesantias.api';
import { CesantiasPrintService } from './cesantias-print.service';
import { CesantiasExporterService } from './cesantias-exporter.service';

@Component({
  standalone: true,
  selector: 'app-cesantias-list',
  templateUrl: './cesantias-list.component.html',
  styleUrls: ['./cesantias-list.component.scss'],
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    HeaderActionsComponent
  ],
})
export class CesantiasListComponent implements OnInit {

  private readonly api = inject(CesantiasApi);
  private readonly router = inject(Router);
  private readonly session = inject(SessionService);

  private readonly printService = inject(CesantiasPrintService);
  private readonly exporterService = inject(CesantiasExporterService);

  cesantias: CesantiasDTO[] = [];
  loading = false;

  // filtro simple
  q = '';

  // =========================================================
  // PERMISOS
  // =========================================================

  get puedeEditar(): boolean {
    return this.session.tienePermiso('NOMINA_PARAMETROS_EDIT');
  }

  ngOnInit(): void {
    this.cargar();
  }

  // =========================================================
  // CARGA
  // =========================================================

  cargar(): void {
    this.loading = true;

    this.api.listar().subscribe({
      next: (data) => {
        this.cesantias = data ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error listando Cesantías nómina', err);
        this.cesantias = [];
        this.loading = false;
      }
    });
  }

  // =========================================================
  // FILTRO
  // =========================================================

  get cesantiasFiltradas(): CesantiasDTO[] {
    const q = (this.q ?? '').trim().toLowerCase();

    if (!q) {
      return this.cesantias;
    }

    return this.cesantias.filter(x =>
      (x.nombreCesantias ?? '').toLowerCase().includes(q) ||
      (x.documento ?? '').toLowerCase().includes(q)
    );
  }

  // =========================================================
  // ACCIONES HEADER
  // =========================================================

  nuevo(): void {
    if (!this.puedeEditar) {
      return;
    }

    this.router.navigate(['/nomina/cesantias/nuevo']);
  }

  refrescar(): void {
    this.cargar();
  }

  imprimir(): void {
    this.printService.imprimir(this.cesantiasFiltradas);
  }

  exportar(): void {
    this.exporterService.exportar(this.cesantiasFiltradas);
  }

  // =========================================================
  // ACCIONES FILA
  // =========================================================

  editar(id: number): void {
    if (!this.puedeEditar) {
      return;
    }

    this.router.navigate([
      `/nomina/cesantias/${id}/editar`
    ]);
  }

  eliminar(id: number): void {
    if (!this.puedeEditar) {
      return;
    }

    const ok = confirm('¿Desea eliminar esta Cesantía?');

    if (!ok) {
      return;
    }

    this.api.eliminar(id).subscribe({
      next: () => this.cargar(),
      error: (err) => {
        console.error('Error eliminando Cesantía', err);
        alert('No se pudo eliminar la Cesantía.');
      }
    });
  }
}
