import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { SessionService } from '../../../core/auth/session.service';

import { AfpApi, AfpListDTO } from './afp.api';
import { AfpPrintService } from './afp-print.service';
import { AfpExporterService } from './afp-exporter.service';

@Component({
  standalone: true,
  selector: 'app-afp-list',
  templateUrl: './afp-list.component.html',
  styleUrls: ['./afp-list.component.scss'],
  imports: [CommonModule, RouterModule, FormsModule, HeaderActionsComponent],
})
export class AfpListComponent implements OnInit {

  private readonly api = inject(AfpApi);
  private readonly router = inject(Router);
  private readonly session = inject(SessionService);

  private readonly printService = inject(AfpPrintService);
  private readonly exporterService = inject(AfpExporterService);

  afp: AfpListDTO[] = [];
  loading = false;

  q = '';

  get puedeEditar(): boolean {
    return this.session.tienePermiso('NOMINA_PARAMETROS_EDIT');
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading = true;

    this.api.listar().subscribe({
      next: (data) => {
        this.afp = data ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error listando AFP nómina', err);
        this.afp = [];
        this.loading = false;
      }
    });
  }

  get afpFiltradas(): AfpListDTO[] {
    const q = (this.q ?? '').trim().toLowerCase();

    if (!q) {
      return this.afp;
    }

    return this.afp.filter(x =>
      (x.nombreAfp ?? '').toLowerCase().includes(q) ||
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

    this.router.navigate(['/nomina/afp/nuevo']);
  }

  refrescar(): void {
    this.cargar();
  }

  imprimir(): void {
    this.printService.imprimir(this.afpFiltradas);
  }

  exportar(): void {
    this.exporterService.exportar(this.afpFiltradas);
  }

  // =========================================================
  // ACCIONES FILA
  // =========================================================

  editar(id: number): void {
    if (!this.puedeEditar) {
      return;
    }

    this.router.navigate([`/nomina/afp/${id}/editar`]);
  }

  eliminar(id: number): void {
    if (!this.puedeEditar) {
      return;
    }

    const ok = confirm('¿Desea eliminar esta AFP?');

    if (!ok) {
      return;
    }

    this.api.eliminar(id).subscribe({
      next: () => this.cargar(),
      error: (err) => {
        console.error('Error eliminando AFP', err);
        alert('No se pudo eliminar la AFP.');
      }
    });
  }
}
