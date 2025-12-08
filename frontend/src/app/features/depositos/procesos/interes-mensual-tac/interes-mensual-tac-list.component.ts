import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';

// 🔹 API TAC
import { InteresMensualTacApi } from './interes-mensual-tac.api';

// 🔹 Servicios compartidos
import { GeneralApi } from '../../../../shared/general/general.api';
import { AgenciaFormaApi } from '../../../../shared/agencia-forma/agencia-forma.api';

// 🔹 Exportar e imprimir
import { InteresMensualTacExporterService } from './interes-mensual-tac-exporter.service';
import { InteresMensualTacPrintService } from './interes-mensual-tac-print.service';

@Component({
  selector: 'app-interes-mensual-tac-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './interes-mensual-tac-list.component.html',
  styleUrls: ['./interes-mensual-tac-list.component.scss']
})
export class InteresMensualTacListComponent implements OnInit {

  private readonly api = inject(InteresMensualTacApi);
  private readonly generalApi = inject(GeneralApi);
  private readonly agenciaFormaApi = inject(AgenciaFormaApi);

  private readonly exporter = inject(InteresMensualTacExporterService);
  private readonly printService = inject(InteresMensualTacPrintService);

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
      const ag = await this.generalApi.listarAgencias().toPromise();
      this.agencias = ag ?? [];
      this.formas = [];
    } catch (e: any) {
      console.error('❌ Error cargando agencias:', e);
      this.error = 'No se pudieron cargar las agencias.';
      this.agencias = [];
    } finally {
      this.cargando = false;
    }
  }

  // ==========================================================
  // 🔁 CASCADA FORMAS POR AGENCIA
  // ==========================================================
  async cargarFormas(idAgencia: string) {
    this.formas = [];
    this.filtros.formaId = '';

    const id = Number(idAgencia);
    if (!id || id === 0) return;

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
  // ⚙️ EJECUTAR LIQUIDACIÓN TAC
  // ==========================================================
  async liquidar(): Promise<void> {
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
        formaId: Number(this.filtros.formaId),   // Se usa en Service (formas 7 o 14)
        fechaProceso: this.filtros.fechaProceso,
        fechaLiquidacion: this.filtros.fechaLiquidacion
      };

      const res = await this.api.liquidar(body);
      this.resultados = Array.isArray(res) ? res : (res ?? []);

    } catch (e: any) {
      console.error('❌ Error en liquidación TAC:', e);

      const texto =
        e?.error?.message ??
        (typeof e?.error === 'string' ? e.error : null) ??
        e?.message ??
        '';

      this.error = texto || 'No se pudo ejecutar la liquidación TAC.';
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

    const agSel = this.agencias.find(a => a.idAgencia === Number(this.filtros.agenciaId));

    const codigoAgencia =
      agSel?.codigoAgencia
        ? String(agSel.codigoAgencia).padStart(2, '0')
        : '00';

    const formaSel = this.formas.find(
      f => f.idFormaAhorro === Number(this.filtros.formaId)
    );

    this.exporter.exportarExcel(this.resultados, {
      fechaProceso: this.filtros.fechaProceso,
      fechaLiquidacion: this.filtros.fechaLiquidacion,
      codigoAgencia,
      codigoForma: formaSel?.codigoForma ?? 'XX',
      nombreForma: formaSel?.nombreForma ?? ''
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

    const formaSel = this.formas.find(
      f => f.idFormaAhorro === Number(this.filtros.formaId)
    );

    const agenciaSel = this.agencias.find(
      a => a.idAgencia === Number(this.filtros.agenciaId)
    );

    const filtrosExtendidos = {
      ...this.filtros,
      codigoForma: formaSel?.codigoForma ?? '',
      nombreForma: formaSel?.nombreForma ?? '',
      codigoAgencia: agenciaSel?.codigoAgencia ?? '',
      nombreAgencia: agenciaSel?.nombreAgencia ?? ''
    };

    this.printService.imprimir(this.resultados, filtrosExtendidos);
  }

}
