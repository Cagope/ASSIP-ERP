import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { ActivosFijosInformesApi } from '../activos-fijos-informes.api';
import { SessionService } from '../../../../core/auth/session.service';

import { GeneralApi } from '../../../../shared/general/general.api';
import { ActivosFijosApi } from '../../activos/activos-fijos.api';

import { KardexActivoPrintService } from './kardex-activo-print.service';
import { KardexActivoExporterService } from './kardex-activo-exporter.service';

@Component({
  standalone: true,
  selector: 'app-kardex-activo-list',
  templateUrl: './kardex-activo-list.component.html',
  styleUrls: ['./kardex-activo-list.component.scss'],
  imports: [CommonModule, FormsModule, HeaderActionsComponent]
})
export class KardexActivoListComponent implements OnInit {

  private readonly api = inject(ActivosFijosInformesApi);
  private readonly session = inject(SessionService);

  private readonly generalApi = inject(GeneralApi);
  private readonly activosApi = inject(ActivosFijosApi);

  private readonly printService = inject(KardexActivoPrintService);
  private readonly exporter = inject(KardexActivoExporterService);

  // ============================================================
  // ✅ DATA
  // ============================================================
  rows: any[] = [];

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

  fechaIni: string | null = null;
  fechaFin: string | null = null;

  ngOnInit(): void {

    this.cargarAgencias();

    // ✅ Agencia activa por defecto (si existe)
    try {
      const ag = (this.session as any).getAgenciaActiva?.();
      if (ag?.id) this.idAgencia = Number(ag.id);
    } catch {}

    // ✅ cargar activos por agencia (si ya hay agencia)
    this.cargarActivosPorAgencia();
  }

  // ============================================================
  // ✅ COMBOS
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

  cargarActivosPorAgencia(): void {

    if (!this.idAgencia) {
      this.activos = [];
      this.idActivoFijo = null;
      return;
    }

    // ✅ Reusamos /activos-fijos/activos (listar) y filtramos en front
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

  onAgenciaChange(): void {
    this.idActivoFijo = null;
    this.cargarActivosPorAgencia();
  }

  // ============================================================
  // ✅ CONSULTAR
  // ============================================================
  consultar(): void {

    if (!this.idAgencia) {
      this.errorMsg = 'Debe seleccionar una agencia.';
      return;
    }

    if (!this.idActivoFijo) {
      this.errorMsg = 'Debe seleccionar un activo.';
      return;
    }

    if (!this.fechaIni || !this.fechaFin) {
      this.errorMsg = 'Debe seleccionar fecha inicial y fecha final.';
      return;
    }

    this.loading = true;
    this.errorMsg = '';
    this.rows = [];

    this.api.movimientosActivos({
      idAgencia: this.idAgencia,
      idActivoFijo: this.idActivoFijo,
      fechaIni: this.fechaIni,
      fechaFin: this.fechaFin,
      codigoMovimiento: null // ✅ kardex = TODOS los movimientos
    }).subscribe({
      next: (data: any[]) => {
        this.rows = data || [];
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'No fue posible consultar el Kardex del activo.';
        this.loading = false;
      }
    });
  }

  // ============================================================
  // ✅ LIMPIAR (sin borrar agencia)
  // ============================================================
  limpiar(): void {
    this.idActivoFijo = null;
    this.fechaIni = null;
    this.fechaFin = null;

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

    const ag = this.agencias.find(a => Number(a.idAgencia) === Number(this.idAgencia));
    const nombreAgencia = ag?.nombreAgencia || 'TODAS';

    const act = this.activos.find(a => Number(a.idActivoFijo) === Number(this.idActivoFijo));
    const nombreActivo = act ? `${act.placaActivo} — ${act.nombreActivo}` : 'ACTIVO';

    this.printService.imprimir(this.rows, {
      agencia: nombreAgencia,
      activo: nombreActivo,
      fechaIni: this.fechaIni,
      fechaFin: this.fechaFin
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

    const act = this.activos.find(a => Number(a.idActivoFijo) === Number(this.idActivoFijo));
    const nombreActivo = act ? `${act.placaActivo} — ${act.nombreActivo}` : 'ACTIVO';

    this.exporter.exportar(this.rows, {
      agencia: nombreAgencia,
      activo: nombreActivo,
      fechaIni: this.fechaIni,
      fechaFin: this.fechaFin,
      idActivoFijo: this.idActivoFijo
    });
  }
}
