import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { PlanCuentasApi, PlanCuenta } from './plan-cuentas.api';
import { PlanCuentasExporterService } from './plan-cuentas-exporter.service';
import { PlanCuentasPrintService } from './plan-cuentas-print.service';

/**
 * 📋 Listado del Plan de Cuentas
 * ------------------------------------------------------------
 * Permite visualizar, exportar e imprimir el catálogo contable
 * de cada agencia.
 */
@Component({
  selector: 'app-plan-cuentas-list',
  standalone: true,
  imports: [CommonModule, HeaderActionsComponent],
  templateUrl: './plan-cuentas-list.component.html',
  styleUrls: ['./plan-cuentas-list.component.scss']
})
export class PlanCuentasListComponent implements OnInit {

  private readonly api = inject(PlanCuentasApi);
  private readonly exporter = inject(PlanCuentasExporterService);
  private readonly printer = inject(PlanCuentasPrintService);
  private readonly router = inject(Router);

  cuentas: PlanCuenta[] = [];
  cargando = false;
  error = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;

    // ⚠️ Por ahora usamos agencia 2 (igual que en tus pruebas PowerShell)
    const idAgencia = 2;

    this.api.listar(idAgencia).subscribe({
      next: (data) => (this.cuentas = data),
      error: () => (this.error = 'Error al cargar el plan de cuentas'),
      complete: () => (this.cargando = false)
    });
  }


  nuevaCuenta(): void {
    this.router.navigate(['/contabilidad/plan-cuentas/nuevo']);
  }

  editarCuenta(id: number): void {
    this.router.navigate(['/contabilidad/plan-cuentas', id, 'editar']);
  }

  eliminarCuenta(c: PlanCuenta): void {
    if (!confirm('¿Eliminar esta cuenta contable?')) return;
    if (!c.id) return;  // ← ahora id correcto

    this.api.eliminar(c.id, c.idAgencia).subscribe(() => {
      this.cargar();
    });
  }

  exportarExcel(): void {
    this.exporter.exportar(this.cuentas);
  }

  imprimirListado(): void {
    this.printer.imprimir(this.cuentas);
  }
}
