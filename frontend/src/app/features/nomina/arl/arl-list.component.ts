import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { ArlApi, ArlListDTO } from './arl.api';
import { ArlPrintService } from './arl-print.service';
import { ArlExporterService } from './arl-exporter.service';

@Component({
  standalone: true,
  selector: 'app-arl-list',
  templateUrl: './arl-list.component.html',
  styleUrls: ['./arl-list.component.scss'],
  imports: [CommonModule, RouterModule, FormsModule, HeaderActionsComponent],
})
export class ArlListComponent implements OnInit {

  private readonly api = inject(ArlApi);
  private readonly router = inject(Router);

  private readonly printService = inject(ArlPrintService);
  private readonly exporterService = inject(ArlExporterService);

  arl: ArlListDTO[] = [];
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
        this.arl = data ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error listando ARL nómina', err);
        this.arl = [];
        this.loading = false;
      }
    });
  }

  get arlFiltradas(): ArlListDTO[] {
    const q = (this.q ?? '').trim().toLowerCase();
    if (!q) return this.arl;

    return this.arl.filter(x =>
      (x.nombreArl ?? '').toLowerCase().includes(q) ||
      (x.documento ?? '').toLowerCase().includes(q)
    );
  }

  // =========================================================
  // ✅ ACCIONES HEADER
  // =========================================================

  nuevo(): void {
    this.router.navigate(['/nomina/arl/nuevo']);
  }

  refrescar(): void {
    this.cargar();
  }

  imprimir(): void {
    this.printService.imprimir(this.arlFiltradas);
  }

  exportar(): void {
    this.exporterService.exportar(this.arlFiltradas);
  }

  // =========================================================
  // ✅ ACCIONES FILA
  // =========================================================

  editar(id: number): void {
    this.router.navigate([`/nomina/arl/${id}/editar`]);
  }

  eliminar(id: number): void {
    const ok = confirm('¿Desea eliminar esta ARL?');
    if (!ok) return;

    this.api.eliminar(id).subscribe({
      next: () => this.cargar(),
      error: (err) => {
        console.error('Error eliminando ARL', err);
        alert('No se pudo eliminar la ARL.');
      }
    });
  }
}
