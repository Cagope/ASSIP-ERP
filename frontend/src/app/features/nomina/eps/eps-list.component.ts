import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { EpsApi, EpsListDTO } from './eps.api';
import { EpsPrintService } from './eps-print.service';
import { EpsExporterService } from './eps-exporter.service';

@Component({
  standalone: true,
  selector: 'app-eps-list',
  templateUrl: './eps-list.component.html',
  styleUrls: ['./eps-list.component.scss'],
  imports: [CommonModule, RouterModule, FormsModule, HeaderActionsComponent],
})
export class EpsListComponent implements OnInit {

  private readonly api = inject(EpsApi);
  private readonly router = inject(Router);

  private readonly printService = inject(EpsPrintService);
  private readonly exporterService = inject(EpsExporterService);

  eps: EpsListDTO[] = [];
  loading = false;

  // ✅ filtro simple
  q = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading = true;

    this.api.listar().subscribe({
      next: (data) => {
        this.eps = data ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error listando EPS nómina', err);
        this.eps = [];
        this.loading = false;
      }
    });
  }

  get epsFiltradas(): EpsListDTO[] {
    const q = (this.q ?? '').trim().toLowerCase();
    if (!q) return this.eps;

    return this.eps.filter(x =>
      (x.nombreEps ?? '').toLowerCase().includes(q) ||
      (x.documento ?? '').toLowerCase().includes(q)
    );
  }

  // =========================================================
  // ✅ ACCIONES HEADER
  // =========================================================

  nuevo(): void {
    this.router.navigate(['/nomina/eps/nuevo']);
  }

  refrescar(): void {
    this.cargar();
  }

  imprimir(): void {
    this.printService.imprimir(this.epsFiltradas);
  }

  exportar(): void {
    this.exporterService.exportar(this.epsFiltradas);
  }

  // =========================================================
  // ✅ ACCIONES FILA
  // =========================================================

  editar(id: number): void {
    this.router.navigate([`/nomina/eps/${id}/editar`]);
  }

  eliminar(id: number): void {
    const ok = confirm('¿Desea eliminar esta EPS?');
    if (!ok) return;

    this.api.eliminar(id).subscribe({
      next: () => this.cargar(),
      error: (err) => {
        console.error('Error eliminando EPS', err);
        alert('No se pudo eliminar la EPS.');
      }
    });
  }
}
