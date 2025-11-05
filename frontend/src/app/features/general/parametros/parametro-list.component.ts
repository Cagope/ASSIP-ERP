import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { ParametrosApi, Parametro } from './parametros.api';
import { ParametrosExporterService } from './parametros-exporter.service';
import { ParametrosPrintService } from './parametros-print.service';

/**
 * 📋 Listado de Parámetros
 * ------------------------------------------------------------
 * Permite visualizar, exportar e imprimir los parámetros
 * configurables del sistema por agencia.
 */
@Component({
  selector: 'app-parametro-list',
  standalone: true,
  imports: [CommonModule, HeaderActionsComponent],
  templateUrl: './parametro-list.component.html',
  styleUrls: ['./parametro-list.component.scss']
})
export class ParametroListComponent implements OnInit {
  private readonly api = inject(ParametrosApi);
  private readonly exporter = inject(ParametrosExporterService);
  private readonly printer = inject(ParametrosPrintService);
  private readonly router = inject(Router);

  parametros: Parametro[] = [];
  cargando = false;
  error = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.api.listar().subscribe({
      next: (data) => (this.parametros = data),
      error: () => (this.error = 'Error al cargar parámetros'),
      complete: () => (this.cargando = false)
    });
  }

  nuevoParametro(): void {
    this.router.navigate(['/general/parametros/nuevo']);
  }

  editarParametro(id: number): void {
    this.router.navigate(['/general/parametros', id, 'editar']);
  }

  eliminarParametro(id: number): void {
    if (!confirm('¿Eliminar este parámetro?')) return;
    this.api.eliminar(id).subscribe(() => this.cargar());
  }

  exportarExcel(): void {
    this.exporter.exportarParametros(this.parametros);
  }

  imprimirListado(): void {
    this.printer.imprimir(this.parametros);
  }
}
