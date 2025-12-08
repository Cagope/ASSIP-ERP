import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { InteresDiarioSmApi } from './interes-diario-sm.api';

// 🔹 Servicios compartidos
import { GeneralApi } from '../../../../shared/general/general.api';
import { AgenciaFormaApi } from '../../../../shared/agencia-forma/agencia-forma.api';

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

  // 🔹 Igual que en Revalorización:
  //    - GeneralApi → para AGENCIAS (trae codigoAgencia)
  //    - AgenciaFormaApi → para FORMAS por agencia
  private readonly generalApi = inject(GeneralApi);
  private readonly agenciaFormaApi = inject(AgenciaFormaApi);

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
  // 🔄 INIT
  // ==========================================================
  async ngOnInit(): Promise<void> {
    this.error = '';
    this.cargando = true;

    try {
      // 👇 IGUAL QUE EN REVALORIZACIÓN
      const ag = await this.generalApi.listarAgencias().toPromise();
      this.agencias = ag ?? [];
      this.formas = []; // vacío al inicio

    } catch (e: any) {
      console.error('❌ Error cargando agencias:', e);
      this.error = 'No se pudieron cargar las agencias.';
      this.agencias = [];
    } finally {
      this.cargando = false;
    }
  }

  // ==========================================================
  // 🔁 CASCADA: Cuando cambia la agencia
  // ==========================================================
  async cargarFormas(idAgencia: string) {
    this.formas = [];
    this.filtros.formaId = '';

    const id = Number(idAgencia);

    if (!id || id === 0) {
      return; // si es "Todas", no hay formas
    }

    try {
      const fr = await firstValueFrom(
        this.agenciaFormaApi.listarFormasPorAgencia(id)
      );
      this.formas = fr ?? [];

    } catch (e) {
      console.error('❌ Error cargando formas:', e);
      this.formas = [];
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
      console.error('❌ Error ejecutando proceso:', e);

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
          `⚠ Han pasado ${dias} días desde la última liquidación (${fechaUltima}).\n\n¿Desea continuar?`
        );

        if (ok) return this.ejecutarConConfirmacion();
        this.error = 'Proceso cancelado por el usuario.';
        return;
      }

      // error normal
      this.error = texto || 'No se pudo ejecutar el proceso.';
    } finally {
      this.cargando = false;
    }
  }

  // ==========================================================
  // 🔁 EJECUTAR CON CONFIRMAR_DIAS
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
      this.error = e?.error?.message || e.message || 'No se pudo ejecutar.';
    } finally {
      this.cargando = false;
    }
  }

  // ==========================================================
  // 🧹 LIMPIAR FORMULARIO
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
    this.formas = [];
  }

  // ==========================================================
  // 📤 EXPORTAR
  // ==========================================================
  exportar(): void {
    if (!this.resultados.length) {
      alert('No hay datos para exportar.');
      return;
    }

    const agenciaId = Number(this.filtros.agenciaId);

    // Buscar la agencia seleccionada
    const ag = this.agencias.find(a => a.idAgencia === agenciaId);

    // Igual que en Revalorización: usamos codigoAgencia
    const codigoAgencia =
      agenciaId === 0
        ? '00'
        : (ag?.codigoAgencia ?? '00');

    this.exporter.exportarExcel(this.resultados, {
      fechaProceso: this.filtros.fechaProceso,
      fechaLiquidacion: this.filtros.fechaLiquidacion,
      codigoAgencia: String(codigoAgencia).padStart(2, '0')
    });
  }

  // ==========================================================
  // 🖨 IMPRIMIR
  // ==========================================================
  imprimir(): void {
    if (!this.resultados.length) {
      alert('No hay información para imprimir.');
      return;
    }
    this.printService.imprimir(this.resultados, this.filtros);
  }
}
