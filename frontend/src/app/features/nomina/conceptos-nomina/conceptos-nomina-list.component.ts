import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { ConceptosNominaApi, ConceptoNominaListDTO } from './conceptos-nomina.api';
import { ConceptosNominaPrintService } from './conceptos-nomina-print.service';
import { ConceptosNominaExporterService } from './conceptos-nomina-exporter.service';

@Component({
  standalone: true,
  selector: 'app-conceptos-nomina-list',
  templateUrl: './conceptos-nomina-list.component.html',
  styleUrls: ['./conceptos-nomina-list.component.scss'],
  imports: [CommonModule, RouterModule, FormsModule, HeaderActionsComponent],
})
export class ConceptosNominaListComponent implements OnInit {

  private readonly api = inject(ConceptosNominaApi);
  private readonly router = inject(Router);

  private readonly printService = inject(ConceptosNominaPrintService);
  private readonly exporterService = inject(ConceptosNominaExporterService);

  items: ConceptoNominaListDTO[] = [];
  loading = false;

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
        console.error('Error listando conceptos nómina', err);
        this.items = [];
        this.loading = false;
      }
    });
  }

  get filtrados(): ConceptoNominaListDTO[] {
    const q = (this.q ?? '').trim().toLowerCase();
    if (!q) return this.items;

    return this.items.filter(x =>
      (x.codigo ?? '').toLowerCase().includes(q) ||
      (x.nombre ?? '').toLowerCase().includes(q) ||
      (x.tipo ?? '').toLowerCase().includes(q)
    );
  }

  // =========================================================
  // ✅ HEADER
  // =========================================================

  nuevo(): void {
    this.router.navigate(['/nomina/conceptos-nomina/nuevo']);
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

  editar(codigo: string): void {
    this.router.navigate([`/nomina/conceptos-nomina/${codigo}/editar`]);
  }

  eliminar(codigo: string): void {
    const ok = confirm('¿Desea eliminar este concepto?');
    if (!ok) return;

    this.api.eliminar(codigo).subscribe({
      next: () => this.cargar(),
      error: (err) => {
        console.error('Error eliminando concepto', err);
        alert('No se pudo eliminar.');
      }
    });
  }
}
