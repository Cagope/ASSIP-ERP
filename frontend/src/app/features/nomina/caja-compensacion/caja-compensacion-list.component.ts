import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { CajaCompensacionApi, CajaCompensacionListDTO } from './caja-compensacion.api';
import { CajaCompensacionPrintService } from './caja-compensacion-print.service';
import { CajaCompensacionExporterService } from './caja-compensacion-exporter.service';

@Component({
  standalone: true,
  selector: 'app-caja-compensacion-list',
  templateUrl: './caja-compensacion-list.component.html',
  styleUrls: ['./caja-compensacion-list.component.scss'],
  imports: [CommonModule, RouterModule, FormsModule, HeaderActionsComponent],
})
export class CajaCompensacionListComponent implements OnInit {

  private readonly api = inject(CajaCompensacionApi);
  private readonly router = inject(Router);

  private readonly printService = inject(CajaCompensacionPrintService);
  private readonly exporterService = inject(CajaCompensacionExporterService);

  cajas: CajaCompensacionListDTO[] = [];
  loading = false;

  q = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading = true;

    this.api.listar().subscribe({
      next: (data) => {
        this.cajas = data ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error listando Cajas de Compensación', err);
        this.cajas = [];
        this.loading = false;
      }
    });
  }

  get cajasFiltradas(): CajaCompensacionListDTO[] {
    const q = (this.q ?? '').trim().toLowerCase();
    if (!q) return this.cajas;

    return this.cajas.filter(x =>
      (x.nombreCaja ?? '').toLowerCase().includes(q) ||
      (x.documento ?? '').toLowerCase().includes(q)
    );
  }

  // =========================================================
  // ✅ HEADER
  // =========================================================
  nuevo(): void {
    this.router.navigate(['/nomina/caja-compensacion/nuevo']);
  }

  refrescar(): void {
    this.cargar();
  }

  imprimir(): void {
    this.printService.imprimir(this.cajasFiltradas);
  }

  exportar(): void {
    this.exporterService.exportar(this.cajasFiltradas);
  }

  // =========================================================
  // ✅ FILA
  // =========================================================
  editar(id: number): void {
    this.router.navigate([`/nomina/caja-compensacion/${id}/editar`]);
  }

  eliminar(id: number): void {
    const ok = confirm('¿Desea eliminar esta Caja de Compensación?');
    if (!ok) return;

    this.api.eliminar(id).subscribe({
      next: () => this.cargar(),
      error: (err) => {
        console.error('Error eliminando Caja', err);
        alert('No se pudo eliminar.');
      }
    });
  }
}
