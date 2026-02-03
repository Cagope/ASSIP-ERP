import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { ActivosFijosInformesApi } from '../activos-fijos-informes.api';
import { SessionService } from '../../../../core/auth/session.service';
import { GeneralApi } from '../../../../shared/general/general.api';
import { ResumenMovimientosPrintService } from './resumen-movimientos-print.service';
import { ResumenMovimientosExporterService } from './resumen-movimientos-exporter.service';

@Component({
  standalone: true,
  selector: 'app-resumen-movimientos-list',
  templateUrl: './resumen-movimientos-list.component.html',
  styleUrls: ['./resumen-movimientos-list.component.scss'],
  imports: [CommonModule, FormsModule, HeaderActionsComponent]
})
export class ResumenMovimientosListComponent implements OnInit {

  private readonly api = inject(ActivosFijosInformesApi);
  private readonly session = inject(SessionService);
  private readonly generalApi = inject(GeneralApi);
  private readonly printService = inject(ResumenMovimientosPrintService);
  private readonly exporter = inject(ResumenMovimientosExporterService);

  // ============================================================
  // ✅ DATA
  // ============================================================
  rows: any[] = [];

  // ✅ combos
  agencias: any[] = [];
  movimientos: any[] = [];

  // ============================================================
  // ✅ UI
  // ============================================================
  loading = false;
  errorMsg = '';

  // ============================================================
  // ✅ FILTROS (agencia obligatoria)
  // ============================================================
  idAgencia: number | null = null;
  codigoMovimiento: string | null = null;

  // ============================================================
  // ✅ INIT
  // ============================================================
  ngOnInit(): void {

    // ✅ cargar combos
    this.cargarAgencias();
    this.cargarCatalogoMovimientos();

    // ✅ agencia activa por defecto (si existe)
    try {
      const ag = (this.session as any).getAgenciaActiva?.();
      if (ag?.id) this.idAgencia = Number(ag.id);
    } catch {}
  }

  // ============================================================
  // ✅ CARGAR AGENCIAS
  // ============================================================
  private cargarAgencias(): void {
    this.generalApi.listarAgencias().subscribe({
      next: (data: any[]) => {
        this.agencias = data || [];
      },
      error: (err) => {
        console.error(err);
        this.agencias = [];
      }
    });
  }

  // ============================================================
  // ✅ CARGAR CATÁLOGO MOVIMIENTOS
  // ============================================================
  private cargarCatalogoMovimientos(): void {
    this.api.activosMovimientos().subscribe({
      next: (data: any[]) => {
        this.movimientos = data || [];
      },
      error: (err) => {
        console.error(err);
        this.movimientos = [];
      }
    });
  }

  // ============================================================
  // ✅ CONSULTAR
  // ============================================================
  consultar(): void {

    // 🔒 agencia obligatoria
    if (!this.idAgencia) {
      this.errorMsg = 'Debe seleccionar una agencia.';
      return;
    }

    this.loading = true;
    this.errorMsg = '';
    this.rows = [];

    this.api.resumenMovimientos({
      idAgencia: this.idAgencia,
      codigoMovimiento: this.codigoMovimiento
    }).subscribe({
      next: (data: any[]) => {
        this.rows = data || [];
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'No fue posible cargar el resumen de movimientos.';
        this.loading = false;
      }
    });
  }

  // ============================================================
  // ✅ LIMPIAR (NO borra agencia)
  // ============================================================
  limpiar(): void {
    this.codigoMovimiento = null;
    this.rows = [];
    this.errorMsg = '';
  }

  // ============================================================
  // ✅ HELPERS
  // ============================================================
  get total(): number {
    return this.rows.length;
  }

  imprimir(): void {

    if (!this.rows || this.rows.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    const mov = this.movimientos.find(m => String(m.codigo_movimiento) === String(this.codigoMovimiento));
    const nombreMovimiento = mov
      ? `${mov.codigo_movimiento} - ${mov.nombre}`
      : 'TODOS';

    // ✅ si ya tienes combo agencias, aquí sacas el nombre
    const nombreAgencia = 'SELECCIONADA'; // luego lo ponemos real

    this.printService.imprimir(this.rows, {
      nombreAgencia,
      nombreMovimiento
    });
  }

  exportar(): void {

    if (!this.rows || this.rows.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const mov = this.movimientos.find(m => String(m.codigo_movimiento) === String(this.codigoMovimiento));
    const nombreMovimiento = mov
      ? `${mov.codigo_movimiento} - ${mov.nombre}`
      : 'TODOS';

    const agenciaNombre = 'SELECCIONADA'; // luego lo sacamos del combo real

    this.exporter.exportar(this.rows, {
      agencia: agenciaNombre,
      movimiento: nombreMovimiento
    });
  }

}
