import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import {
  ConceptoCuentasContablesApi,
  ConceptoCuentaContableListDTO
} from './concepto-cuentas-contables.api';

import { ConceptoCuentasContablesPrintService } from './concepto-cuentas-contables-print.service';
import { ConceptoCuentasContablesExporterService } from './concepto-cuentas-contables-exporter.service';
import { SessionService } from '../../../core/auth/session.service';

@Component({
  standalone: true,
  selector: 'app-concepto-cuentas-contables-list',
  templateUrl: './concepto-cuentas-contables-list.component.html',
  styleUrls: ['./concepto-cuentas-contables-list.component.scss'],
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    HeaderActionsComponent
  ],
})
export class ConceptoCuentasContablesListComponent implements OnInit {

  private readonly api = inject(ConceptoCuentasContablesApi);
  private readonly router = inject(Router);

  private readonly printService = inject(ConceptoCuentasContablesPrintService);
  private readonly exporterService = inject(ConceptoCuentasContablesExporterService);
  private readonly session = inject(SessionService);

  items: ConceptoCuentaContableListDTO[] = [];
  loading = false;

  // filtro texto
  q = '';

  idAgencia: number | null = null;

  // =========================================================
  // PERMISOS
  // =========================================================

  get puedeEditar(): boolean {
    return this.session.tienePermiso('NOMINA_PARAMETROS_EDIT');
  }

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {
    this.cargar();
  }

  // =========================================================
  // CARGAR - LISTADO GLOBAL
  // =========================================================

  cargar(): void {
    this.loading = true;

    this.api.listar().subscribe({
      next: (data) => {
        this.items = data ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error listando concepto cuentas contables', err);
        this.items = [];
        this.loading = false;
      }
    });
  }

  // =========================================================
  // FILTRO
  // =========================================================

  get filtrados(): ConceptoCuentaContableListDTO[] {
    const q = this.q.trim().toLowerCase();

    if (!q) {
      return this.items;
    }

    return this.items.filter(x =>
      x.codigoConcepto?.toLowerCase().includes(q) ||
      x.nombreAgencia?.toLowerCase().includes(q) ||
      String(x.idCuentaDebito).includes(q) ||
      String(x.idCuentaCredito).includes(q)
    );
  }

  // =========================================================
  // HEADER
  // =========================================================

  nuevo(): void {
    if (!this.puedeEditar) {
      return;
    }

    this.router.navigate([
      '/nomina/concepto-cuentas-contables/nuevo'
    ]);
  }

  refrescar(): void {
    this.cargar();
  }

  imprimir(): void {
    this.printService.imprimir(this.filtrados);
  }

  exportar(): void {
    this.exporterService.exportar(this.filtrados);
  }

  // =========================================================
  // FILA
  // =========================================================

  editar(idMapeo: number): void {
    if (!this.puedeEditar) {
      return;
    }

    this.router.navigate([
      `/nomina/concepto-cuentas-contables/${idMapeo}/editar`
    ]);
  }

  eliminar(idMapeo: number): void {
    if (!this.puedeEditar) {
      return;
    }

    const ok = confirm('¿Desea eliminar este mapeo contable?');

    if (!ok) {
      return;
    }

    this.api.eliminar(idMapeo).subscribe({
      next: () => this.cargar(),
      error: (err) => {
        console.error('Error eliminando mapeo contable', err);
        alert('No se pudo eliminar.');
      }
    });
  }
}
