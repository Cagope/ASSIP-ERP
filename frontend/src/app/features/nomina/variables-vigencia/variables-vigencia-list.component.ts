import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import {
  VariablesVigenciaApi,
  VariablesVigenciaListDTO
} from './variables-vigencia.api';

import { VariablesVigenciaPrintService } from './variables-vigencia-print.service';
import { VariablesVigenciaExporterService } from './variables-vigencia-exporter.service';

@Component({
  standalone: true,
  selector: 'app-variables-vigencia-list',
  templateUrl: './variables-vigencia-list.component.html',
  styleUrls: ['./variables-vigencia-list.component.scss'],
  imports: [CommonModule, RouterModule, FormsModule, HeaderActionsComponent],
})
export class VariablesVigenciaListComponent implements OnInit {

  private readonly api = inject(VariablesVigenciaApi);
  private readonly router = inject(Router);

  private readonly printService = inject(VariablesVigenciaPrintService);
  private readonly exporterService = inject(VariablesVigenciaExporterService);

  items: VariablesVigenciaListDTO[] = [];
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
        this.items = data ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error listando Variables Vigencia', err);
        this.items = [];
        this.loading = false;
      }
    });
  }

  get filtrados(): VariablesVigenciaListDTO[] {
    const q = (this.q ?? '').trim().toLowerCase();
    if (!q) return this.items;

    return this.items.filter(x =>
      (x.fechaInicial ?? '').toLowerCase().includes(q) ||
      (x.fechaFinal ?? '').toLowerCase().includes(q) ||
      String(x.smmlv ?? '').includes(q) ||
      String(x.auxTransporte ?? '').includes(q)
    );
  }

  // =========================================================
  // ✅ HEADER
  // =========================================================

  nuevo(): void {
    this.router.navigate(['/nomina/variables-vigencia/nuevo']);
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
  // ✅ FILA
  // =========================================================

  editar(id: number): void {
    this.router.navigate([`/nomina/variables-vigencia/${id}/editar`]);
  }

  eliminar(id: number): void {
    const ok = confirm('¿Desea eliminar esta vigencia?');
    if (!ok) return;

    this.api.eliminar(id).subscribe({
      next: () => this.cargar(),
      error: (err) => {
        console.error('Error eliminando Variables Vigencia', err);
        alert('No se pudo eliminar.');
      }
    });
  }
}
