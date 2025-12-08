import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';

// 🔹 API mensual
import { InteresMensualSmApi } from './interes-mensual-sm.api';

// 🔹 Servicios compartidos
import { GeneralApi } from '../../../../shared/general/general.api';
import { AgenciaFormaApi } from '../../../../shared/agencia-forma/agencia-forma.api';

// 🔹 Exportar e imprimir
import { InteresMensualSmExporterService } from './interes-mensual-sm-exporter.service';
import { InteresMensualSmPrintService } from './interes-mensual-sm-print.service';

@Component({
  selector: 'app-interes-mensual-sm-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './interes-mensual-sm-list.component.html',
  styleUrls: ['./interes-mensual-sm-list.component.scss']
})
export class InteresMensualSmListComponent implements OnInit {

  private readonly api = inject(InteresMensualSmApi);
  private readonly generalApi = inject(GeneralApi);
  private readonly agenciaFormaApi = inject(AgenciaFormaApi);

  private readonly exporter = inject(InteresMensualSmExporterService);
  private readonly printService = inject(InteresMensualSmPrintService);

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
  // 🔁 CASCADA: Cuando cambia la agencia
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
  // ⚙️ EJECUTAR LIQUIDACIÓN
  // ==========================================================
  async liquidar(): Promise<void> {
    this.error = '';

    // Validaciones
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

      const res = await this.api.liquidar(body);
      this.resultados = Array.isArray(res) ? res : (res ?? []);

    } catch (e: any) {
      console.error('❌ Error en liquidación mensual:', e);

      const texto =
        e?.error?.message ??
        (typeof e?.error === 'string' ? e.error : null) ??
        e?.message ??
        '';

      this.error = texto || 'No se pudo ejecutar la liquidación.';

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
    const ag = this.agencias.find(a => a.idAgencia === agenciaId);

    const codigoAgencia =
      agenciaId === 0
        ? '00'
        : (ag?.codigoAgencia ?? '00');

    // 🔍 Buscar la forma seleccionada
    const formaSel = this.formas.find(
      f => f.idFormaAhorro === Number(this.filtros.formaId)
    );

    const codigoForma = formaSel?.codigoForma ?? 'XX';
    const nombreForma = formaSel?.nombreForma ?? '';

    this.exporter.exportarExcel(this.resultados, {
      fechaProceso: this.filtros.fechaProceso,
      fechaLiquidacion: this.filtros.fechaLiquidacion,
      codigoAgencia: String(codigoAgencia).padStart(2, '0'),
      codigoForma: codigoForma,
      nombreForma: nombreForma
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

    // 🔍 Buscar la FORMA seleccionada
    const formaSel = this.formas.find(
      f => f.idFormaAhorro === Number(this.filtros.formaId)
    );

    // 🔍 Buscar la AGENCIA seleccionada
    const agenciaSel = this.agencias.find(
      a => a.idAgencia === Number(this.filtros.agenciaId)
    );

    // 📦 Construir filtros extendidos para el print
    const filtrosExtendidos = {
      ...this.filtros,

      // ➤ Forma
      codigoForma: formaSel?.codigoForma ?? '',
      nombreForma: formaSel?.nombreForma ?? '',

      // ➤ Agencia  (AQUI ESTABA EL FALTANTE)
      codigoAgencia: agenciaSel?.codigoAgencia ?? '',
      nombreAgencia: agenciaSel?.nombreAgencia ?? ''
    };

    this.printService.imprimir(this.resultados, filtrosExtendidos);
  }



}
