import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { ActivosFijosInformesApi } from '../activos-fijos-informes.api';
import { SessionService } from '../../../../core/auth/session.service';

import { GeneralApi } from '../../../../shared/general/general.api';
import { ActivosFijosApi } from '../../activos/activos-fijos.api';
import { MovimientosActivosPrintService } from './movimientos-activos-print.service';
import { MovimientosActivosExporterService } from './movimientos-activos-exporter.service';

@Component({
  standalone: true,
  selector: 'app-movimientos-activos-list',
  templateUrl: './movimientos-activos-list.component.html',
  styleUrls: ['./movimientos-activos-list.component.scss'],
  imports: [CommonModule, FormsModule, HeaderActionsComponent]
})
export class MovimientosActivosListComponent implements OnInit {

  private readonly api = inject(ActivosFijosInformesApi);
  private readonly session = inject(SessionService);

  private readonly generalApi = inject(GeneralApi);
  private readonly activosApi = inject(ActivosFijosApi);
  private readonly printService = inject(MovimientosActivosPrintService);
  private readonly exporter = inject(MovimientosActivosExporterService);

  // ============================================================
  // ✅ DATA
  // ============================================================
  rows: any[] = [];
  movimientos: any[] = [];

  agencias: any[] = [];
  activos: any[] = [];

  // ============================================================
  // ✅ UI
  // ============================================================
  loading = false;
  errorMsg = '';

  // ============================================================
  // ✅ FILTROS (OBLIGATORIOS)
  // ============================================================
  idAgencia: number | null = null;
  idActivoFijo: number | null = null;

  // ============================================================
  // ✅ FILTROS (OPCIONALES)
  // ============================================================
  codigoMovimiento: string | null = null;
  fechaIni: string | null = null;
  fechaFin: string | null = null;

  ngOnInit(): void {

    // ✅ cargar combos
    this.cargarAgencias();
    this.cargarCatalogoMovimientos();

    // ✅ agencia activa por defecto si existe
    try {
      const ag = (this.session as any).getAgenciaActiva?.();
      if (ag?.id) {
        this.idAgencia = Number(ag.id);
      }
    } catch {}

    // ✅ cargar activos (depende agencia)
    this.cargarActivosPorAgencia();
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
  // ✅ CARGAR ACTIVOS (según agencia)
  // ============================================================
  cargarActivosPorAgencia(): void {

    // 🔒 si no hay agencia seleccionada => limpiar lista
    if (!this.idAgencia) {
      this.activos = [];
      this.idActivoFijo = null;
      return;
    }

    // ✅ traer activos y filtrar por agencia en front
    this.activosApi.listar().subscribe({
      next: (data: any[]) => {
        const all = data || [];
        this.activos = all
          .filter(a => Number(a?.idAgencia ?? a?.id_agencia ?? 0) === Number(this.idAgencia))
          .map(a => ({
            idActivoFijo: a?.idActivoFijo ?? a?.id_activo_fijo,
            placaActivo: a?.placaActivo ?? a?.placa_activo,
            nombreActivo: a?.nombreActivo ?? a?.nombre_activo
          }))
          .filter(a => a.idActivoFijo != null);
      },
      error: (err) => {
        console.error(err);
        this.activos = [];
      }
    });
  }

  // ============================================================
  // ✅ CATÁLOGO MOVIMIENTOS
  // ============================================================
  private cargarCatalogoMovimientos(): void {
    this.api.activosMovimientos().subscribe({
      next: (data: any[]) => this.movimientos = data || [],
      error: (err) => {
        console.error(err);
        this.movimientos = [];
      }
    });
  }

  // ============================================================
  // ✅ EVENTO: CAMBIO AGENCIA
  // ============================================================
  onAgenciaChange(): void {
    this.idActivoFijo = null;
    this.cargarActivosPorAgencia();
  }

  // ============================================================
  // ✅ CONSULTAR
  // ============================================================
  consultar(): void {

    // ✅ OBLIGATORIO
    if (!this.idAgencia) {
      this.errorMsg = 'Debe seleccionar una agencia.';
      return;
    }

    // ✅ OBLIGATORIO
    if (!this.idActivoFijo) {
      this.errorMsg = 'Debe seleccionar un activo.';
      return;
    }

    this.loading = true;
    this.errorMsg = '';
    this.rows = [];

    this.api.movimientosActivos({
      idAgencia: this.idAgencia,
      codigoMovimiento: this.codigoMovimiento,
      fechaIni: this.fechaIni,
      fechaFin: this.fechaFin,
      idActivoFijo: this.idActivoFijo
    }).subscribe({
      next: (data: any[]) => {
        this.rows = data || [];
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'No fue posible consultar los movimientos de activos.';
        this.loading = false;
      }
    });
  }

  // ============================================================
  // ✅ LIMPIAR (SIN BORRAR AGENCIA)
  // ============================================================
  limpiar(): void {
    this.codigoMovimiento = null;
    this.fechaIni = null;
    this.fechaFin = null;

    // 🔒 no limpiamos agencia
    this.idActivoFijo = null;

    this.rows = [];
    this.errorMsg = '';
  }

  get total(): number {
    return this.rows.length;
  }

  // ============================================================
  // ✅ IMPRIMIR
  // ============================================================
  imprimir(): void {

    if (!this.rows || this.rows.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    // ✅ tomar agencia desde SELECT (no desde sesión)
    const ag = this.agencias.find(a => Number(a.idAgencia) === Number(this.idAgencia));
    const nombreAgencia = ag?.nombreAgencia || 'TODAS';

    const mov = this.movimientos.find(x => String(x.codigo_movimiento) === String(this.codigoMovimiento));
    const nombreMovimiento = mov
      ? `${mov.codigo_movimiento} - ${mov.nombre}`
      : 'TODOS';

    this.printService.imprimir(this.rows, {
      nombreAgencia,
      nombreMovimiento,
      fechaIni: this.fechaIni,
      fechaFin: this.fechaFin,
      idActivoFijo: this.idActivoFijo
    });
  }

  // ============================================================
  // ✅ EXPORTAR EXCEL
  // ============================================================
  exportar(): void {

    if (!this.rows || this.rows.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const ag = this.agencias.find(a => Number(a.idAgencia) === Number(this.idAgencia));
    const nombreAgencia = ag?.nombreAgencia || 'TODAS';

    const mov = this.movimientos.find(m => String(m.codigo_movimiento) === String(this.codigoMovimiento));
    const nombreMovimiento = mov
      ? `${mov.codigo_movimiento} - ${mov.nombre}`
      : 'TODOS';

    this.exporter.exportar(this.rows, {
      agencia: nombreAgencia,
      movimiento: nombreMovimiento,
      fechaIni: this.fechaIni,
      fechaFin: this.fechaFin,
      idActivoFijo: this.idActivoFijo
    });
  }
}
