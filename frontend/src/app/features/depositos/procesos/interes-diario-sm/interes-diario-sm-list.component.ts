import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { InteresDiarioSmApi } from './interes-diario-sm.api';
import { GeneralApi } from '../../../../shared/general/general.api';
import { FormasAhorroApi } from '../../formas-ahorro/formas-ahorro.api';

import { InteresDiarioSmExporterService } from './interes-diario-sm-exporter.service';
import { InteresDiarioSmPrintService } from './interes-diario-sm-print.service';

@Component({
  selector: 'app-interes-diario-sm-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './interes-diario-sm-list.component.html',
  styleUrls: ['./interes-diario-sm-list.component.scss']
})
export class InteresDiarioSmListComponent implements OnInit {

  private readonly api = inject(InteresDiarioSmApi);
  private readonly generalApi = inject(GeneralApi);
  private readonly formasApi = inject(FormasAhorroApi);

  private readonly exporter = inject(InteresDiarioSmExporterService);
  private readonly printService = inject(InteresDiarioSmPrintService);

  agencias: any[] = [];
  formas: any[] = [];

  filtros = {
    agenciaId: '0',
    formaId: '',
    fechaProceso: '',
    fechaLiquidacion: ''
  };

  cargando = false;
  error = '';
  resultados: any[] = [];

  // ==========================================================
  // 🔄 CARGA INICIAL: agencias + formas
  // ==========================================================
  async ngOnInit(): Promise<void> {
    this.error = '';
    this.cargando = true;

    try {
      const ag = await firstValueFrom(this.generalApi.listarAgencias());
      this.agencias = ag ?? [];

      const fr = await firstValueFrom(this.formasApi.listar());
      this.formas = fr ?? [];

    } catch (e: any) {
      console.error('❌ Error cargando datos iniciales:', e);

      if (e?.error?.message) {
        this.error = e.error.message;
      } else if (typeof e?.error === 'string') {
        this.error = e.error;
      } else if (e?.message) {
        this.error = e.message;
      } else {
        this.error = 'No se pudieron cargar los catálogos.';
      }

    } finally {
      this.cargando = false;
    }
  }

  // ==========================================================
  // ⚙️ EJECUTAR PROCESO
  // ==========================================================
  async ejecutar(): Promise<void> {
    this.error = '';

    if (!this.filtros.fechaProceso) {
      this.error = 'Debe seleccionar la fecha de proceso.';
      return;
    }

    if (!this.filtros.fechaLiquidacion) {
      this.error = 'Debe seleccionar la fecha de liquidación.';
      return;
    }

    if (!this.filtros.formaId) {
      this.error = 'Debe seleccionar la forma.';
      return;
    }

    this.cargando = true;
    this.resultados = [];

    try {
      const body = {
        agenciaId: Number(this.filtros.agenciaId),
        formaId: Number(this.filtros.formaId),
        fechaProceso: this.filtros.fechaProceso,
        fechaLiquidacion: this.filtros.fechaLiquidacion
      };

      const res = await this.api.ejecutar(body);
      this.resultados = Array.isArray(res) ? res : (res ?? []);

    } catch (e: any) {
      console.error('❌ Error ejecutando Interés Diario SM:', e);

      // ======================================================
      // 🟡 Detectar mensaje CONFIRMAR_DIAS en CUALQUIER campo
      // ======================================================
      const texto =
        e?.error?.message ??
        (typeof e?.error === 'string' ? e.error : null) ??
        e?.message ??
        '';

      if (typeof texto === 'string' && texto.includes('CONFIRMAR_DIAS|')) {

        const partes = texto.split('|');
        const dias = partes[1];
        const fechaUltima = partes[2];

        const ok = confirm(
          `⚠ Han pasado ${dias} días desde la última liquidación (${fechaUltima}).\n\n` +
          `¿Desea continuar con el proceso?`
        );

        if (ok) {
          return this.ejecutarConConfirmacion();
        } else {
          this.error = 'Proceso cancelado por el usuario.';
          return;
        }
      }

      // ======================================================
      // ❌ ERRORES NORMALES
      // ======================================================
      if (e?.error?.message) {
        this.error = e.error.message;
      } else if (typeof e?.error === 'string') {
        this.error = e.error;
      } else if (e?.message) {
        this.error = e.message;
      } else {
        this.error = 'No se pudo ejecutar el proceso.';
      }

    } finally {
      this.cargando = false;
    }

  }

  // ==========================================================
  // 🔁 EJECUTAR CON CONFIRMACIÓN
  // ==========================================================
  private async ejecutarConConfirmacion(): Promise<void> {

    this.error = '';
    this.cargando = true;
    this.resultados = [];

    try {
      const body = {
        agenciaId: Number(this.filtros.agenciaId),
        formaId: Number(this.filtros.formaId),
        fechaProceso: this.filtros.fechaProceso,
        fechaLiquidacion: this.filtros.fechaLiquidacion,
        confirmado: true
      };

      const res = await this.api.ejecutar(body);
      this.resultados = Array.isArray(res) ? res : (res ?? []);

    } catch (e: any) {
      console.error('❌ Error ejecutando con confirmación:', e);

      if (e?.error?.message) {
        this.error = e.error.message;
      } else if (typeof e?.error === 'string') {
        this.error = e.error;
      } else if (e?.message) {
        this.error = e.message;
      } else {
        this.error = 'No se pudo ejecutar el proceso.';
      }

    } finally {
      this.cargando = false;
    }
  }

  // ==========================================================
  // 🧹 LIMPIAR
  // ==========================================================
  limpiar(): void {
    this.filtros = {
      agenciaId: '0',
      formaId: '',
      fechaProceso: '',
      fechaLiquidacion: ''
    };
    this.resultados = [];
    this.error = '';
  }

  // ==========================================================
  // 📤 EXPORTAR A EXCEL
  // ==========================================================
  exportar(): void {
    if (!this.resultados || this.resultados.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.exporter.exportarExcel(this.resultados, {
      fechaProceso: this.filtros.fechaProceso,
      fechaLiquidacion: this.filtros.fechaLiquidacion
    });
  }

  // ==========================================================
  // 🖨️ IMPRIMIR
  // ==========================================================
  imprimir(): void {
    if (!this.resultados || this.resultados.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    this.printService.imprimir(this.resultados, this.filtros);
  }
}
