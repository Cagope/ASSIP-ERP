import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { EmpleadoContratosApi, EmpleadoContratoListDTO } from './empleado-contratos.api';

import { EmpleadoContratosPrintService } from './empleado-contratos-print.service';
import { EmpleadoContratosExporterService } from './empleado-contratos-exporter.service';

@Component({
  standalone: true,
  selector: 'app-empleado-contratos-list',
  templateUrl: './empleado-contratos-list.component.html',
  styleUrls: ['./empleado-contratos-list.component.scss'],
  imports: [CommonModule, RouterModule, FormsModule, HeaderActionsComponent],
})
export class EmpleadoContratosListComponent implements OnInit {

  private readonly api = inject(EmpleadoContratosApi);
  private readonly router = inject(Router);

  private readonly printService = inject(EmpleadoContratosPrintService);
  private readonly exporterService = inject(EmpleadoContratosExporterService);

  contratos: EmpleadoContratoListDTO[] = [];
  loading = false;

  q = '';

  ngOnInit(): void {
    this.cargar();
  }

  // =========================
  // CARGAR
  // =========================

  cargar(): void {

    this.loading = true;

    this.api.listar().subscribe({
      next: (data) => {
        this.contratos = data ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error listando contratos', err);
        this.contratos = [];
        this.loading = false;
      }
    });
  }

  // =========================
  // MÉTODOS PUENTE (compatibilidad con HTML actual)
  // =========================

  documentoEmpleado(idEmpleado: number): string {
    const c = this.contratos.find(x => x.idEmpleado === idEmpleado);
    return c?.documentoEmpleado ?? '';
  }

  nombreEmpleado(idEmpleado: number): string {
    const c = this.contratos.find(x => x.idEmpleado === idEmpleado);
    return c?.nombreEmpleado ?? '';
  }

  // =========================
  // FILTRO
  // =========================

  get filtrados(): EmpleadoContratoListDTO[] {

    const q = (this.q ?? '').trim().toLowerCase();
    if (!q) return this.contratos;

    return this.contratos.filter(x => {

      const doc = (x.documentoEmpleado ?? '').toLowerCase();
      const nom = (x.nombreEmpleado ?? '').toLowerCase();
      const per = (x.periodoPago ?? '').toLowerCase();

      return doc.includes(q) || nom.includes(q) || per.includes(q);
    });
  }

  // =========================
  // HEADER
  // =========================

  nuevo(): void {
    this.router.navigate(['/nomina/empleado-contratos/nuevo']);
  }

  refrescar(): void {
    this.cargar();
  }

  imprimir(): void {
    this.printService.imprimir(this.filtrados, new Map(), new Map());
  }

  exportar(): void {
    this.exporterService.exportar(this.filtrados);
  }


  // =========================
  // FILA
  // =========================

  editar(id: number): void {
    this.router.navigate([`/nomina/empleado-contratos/${id}/editar`]);
  }

  eliminar(id: number): void {

    const ok = confirm('¿Desea eliminar este contrato?');
    if (!ok) return;

    this.api.eliminar(id).subscribe({
      next: () => this.cargar(),
      error: (err) => {
        console.error('Error eliminando contrato', err);
        alert('No se pudo eliminar el contrato.');
      }
    });
  }
}
