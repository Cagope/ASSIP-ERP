import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { ActivosFijosInformesApi } from '../activos-fijos-informes.api';
import { SessionService } from '../../../../core/auth/session.service';

import { GeneralApi } from '../../../../shared/general/general.api';
import { ActivosFijosApi } from '../../activos/activos-fijos.api';

// ✅ Servicios (los vamos a crear en el siguiente paso)
import { DepreciacionInformePrintService } from './depreciacion-informe-print.service';
import { DepreciacionInformeExporterService } from './depreciacion-informe-exporter.service';

@Component({
  standalone: true,
  selector: 'app-depreciacion-informe-list',
  templateUrl: './depreciacion-informe-list.component.html',
  styleUrls: ['./depreciacion-informe-list.component.scss'],
  imports: [CommonModule, FormsModule, HeaderActionsComponent]
})
export class DepreciacionInformeListComponent implements OnInit {

  private readonly api = inject(ActivosFijosInformesApi);
  private readonly session = inject(SessionService);

  private readonly generalApi = inject(GeneralApi);
  private readonly activosApi = inject(ActivosFijosApi);

  // ✅ imprimir / exportar
  private readonly printService = inject(DepreciacionInformePrintService);
  private readonly exporter = inject(DepreciacionInformeExporterService);

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
  // ✅ FILTROS
  // ============================================================
  idAgencia: number | null = null;

  // 🔽 Activo opcional
  idActivoFijo: number | null = null;

  fechaIni: string | null = null;
  fechaFin: string | null = null;

  // ✅ movimiento fijo: 71 = Depreciaciones
  private readonly CODIGO_DEPRECIACION = '71';

  ngOnInit(): void {

    this.cargarAgencias();

    // ✅ agencia activa por defecto si existe
    try {
      const ag = (this.session as any).getAgenciaActiva?.();
      if (ag?.id) {
        this.idAgencia = Number(ag.id);
      }
    } catch {}

    // ✅ cargar activos según agencia
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
  // ✅ CARGAR ACTIVOS POR AGENCIA
  // ============================================================
  cargarActivosPorAgencia(): void {

    if (!this.idAgencia) {
      this.activos = [];
      this.idActivoFijo = null;
      return;
    }

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
  // ✅ EVENTO CAMBIO AGENCIA
  // ============================================================
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

    if (!this.fechaIni || !this.fechaFin) {
      this.errorMsg = 'Debe seleccionar fecha inicial y fecha final.';
      return;
    }

    this.loading = true;
    this.errorMsg = '';
    this.rows = [];

    this.api.movimientosActivos({
      idAgencia: this.idAgencia,
      idActivoFijo: this.idActivoFijo, // ✅ opcional
      fechaIni: this.fechaIni,
      fechaFin: this.fechaFin,
      codigoMovimiento: this.CODIGO_DEPRECIACION
    }).subscribe({
      next: (data: any[]) => {
        this.rows = data || [];
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'No fue posible cargar el informe de depreciación.';
        this.loading = false;
      }
    });
  }

  // ============================================================
  // ✅ LIMPIAR (SIN BORRAR AGENCIA)
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

    this.printService.imprimir(this.rows, {
      agencia: nombreAgencia,
      fechaIni: this.fechaIni,
      fechaFin: this.fechaFin
    });
  }

  // ============================================================
  // ✅ EXPORTAR
  // ============================================================
  exportar(): void {
    if (!this.rows || this.rows.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const ag = this.agencias.find(a => Number(a.idAgencia) === Number(this.idAgencia));
    const nombreAgencia = ag?.nombreAgencia || 'TODAS';

    this.exporter.exportar(this.rows, {
      agencia: nombreAgencia,
      fechaIni: this.fechaIni,
      fechaFin: this.fechaFin
    });
  }
}
