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

import {
  TiposComprobantesApi,
  TipoComprobante
} from '../../../contabilidad/tipos-comprobantes/tipos-comprobantes.api';

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
  private readonly tiposComprobantesApi = inject(TiposComprobantesApi);

  private readonly exporter = inject(InteresMensualSmExporterService);
  private readonly printService = inject(InteresMensualSmPrintService);

  agencias: any[] = [];
  formas: any[] = [];
  tiposComprobantes: TipoComprobante[] = [];

  formaSeleccionada: any = null;

  resumen = {
    totalInteresBruto: 0,
    totalRetencion: 0,
    totalInteresNeto: 0,
    totalCuentas: 0
  };

  resumenContable: any[] = [];
  ultimaLiquidacion: any = null;

  filtros = {
    agenciaId: '0',
    formaId: '',

    fechaProceso: '',
    fechaLiquidacion: '',

    tipoComprobante: '',
    numeroComprobante: ''
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
  // 🔁 CASCADA: AGENCIA
  // ==========================================================
  async cargarFormas(idAgencia: string) {

    this.formas = [];
    this.filtros.formaId = '';
    this.formaSeleccionada = null;
    this.tiposComprobantes = [];
    this.filtros.tipoComprobante = '';
    this.filtros.numeroComprobante = '';
    this.resultados = [];
    this.resumenContable = [];

    const id = Number(idAgencia);

    if (!id || id === 0) {
      return;
    }

    try {

      const fr = await firstValueFrom(
        this.agenciaFormaApi.listarFormasPorAgencia(id)
      );

      this.formas = fr ?? [];

      const tc = await firstValueFrom(
        this.tiposComprobantesApi.listarPorAgencia(id)
      );

      this.tiposComprobantes = tc ?? [];

    } catch (e) {

      console.error('❌ Error cargando formas:', e);

      this.formas = [];
    }
  }

  // ==========================================================
  // 🔍 CAMBIO FORMA
  // ==========================================================
  // ==========================================================
  // 🔍 CAMBIO FORMA
  // ==========================================================
  seleccionarForma() {

    this.formaSeleccionada = this.formas.find(
      f => f.idFormaAhorro === Number(this.filtros.formaId)
    ) ?? null;

    console.log(
      'FORMA SELECCIONADA:',
      this.formaSeleccionada
    );

    this.cargarUltimaLiquidacion();

  }

  // ==========================================================
  // 📌 ÚLTIMA LIQUIDACIÓN
  // ==========================================================
  async cargarUltimaLiquidacion(): Promise<void> {

    this.ultimaLiquidacion = null;

    const idAgencia =
      Number(this.filtros.agenciaId);

    const idForma =
      Number(this.filtros.formaId);

    if (!idAgencia || !idForma) {
      return;
    }

    try {

      this.ultimaLiquidacion =
        await this.api.obtenerUltimaLiquidacion(
          idAgencia,
          idForma
        );

    } catch (e) {

      console.error(
        '❌ Error cargando última liquidación:',
        e
      );

      this.ultimaLiquidacion = null;
    }
  }

  // ==========================================================
  // 🔢 CARGAR PRÓXIMO COMPROBANTE
  // ==========================================================
  async cargarProximoComprobante(): Promise<void> {

    this.error = '';

    if (!this.filtros.tipoComprobante) {
      this.filtros.numeroComprobante = '';
      return;
    }

    const idAgencia = Number(this.filtros.agenciaId);

    if (!idAgencia || idAgencia === 0) {
      this.error = 'Debe seleccionar la agencia.';
      return;
    }

    try {

      const res = await this.api.obtenerProximoComprobante(
        idAgencia,
        this.filtros.tipoComprobante
      );

      this.filtros.numeroComprobante =
        res?.numeroComprobante ?? '';

    } catch (e: any) {

      console.error(
        '❌ Error obteniendo comprobante:',
        e
      );

      this.error =
        'No se pudo obtener el consecutivo del comprobante.';
    }
  }

  // ==========================================================
  // ⚙️ LIQUIDAR
  // ==========================================================
  async liquidar(): Promise<void> {

    this.error = '';

    if (!this.filtros.formaId) {
      this.error = 'Debe seleccionar la forma.';
      return;
    }

    if (!this.filtros.fechaProceso) {
      this.error = 'Debe seleccionar la fecha de proceso.';
      return;
    }

    if (!this.filtros.fechaLiquidacion) {
      this.error = 'Debe seleccionar la fecha de liquidación.';
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

      this.resultados = Array.isArray(res)
        ? res
        : (res ?? []);

      this.calcularResumen();
      this.construirResumenContable();

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
  // ✅ APLICAR
  // ==========================================================
  async aplicar(): Promise<void> {

    this.error = '';

    if (!this.resultados.length) {
      this.error = 'Debe ejecutar primero la liquidación.';
      return;
    }

    if (!this.filtros.tipoComprobante) {
      this.error = 'Debe seleccionar el tipo de comprobante.';
      return;
    }

    if (!confirm(
      '¿Desea aplicar los intereses?\n\n' +
      'Este proceso actualizará saldos y generará extractos.'
    )) {
      return;
    }

    this.cargando = true;

    try {

      const body = {
        agenciaId: Number(this.filtros.agenciaId),
        formaId: Number(this.filtros.formaId),

        fechaProceso: this.filtros.fechaProceso,
        fechaLiquidacion: this.filtros.fechaLiquidacion,

        tipoComprobante: this.filtros.tipoComprobante
      };

      await this.api.aplicar(body);

      alert('Proceso aplicado correctamente.');

      this.limpiar();

    } catch (e: any) {

      console.error('❌ Error aplicando proceso:', e);

      this.error =
        e?.error?.message ||
        'No se pudo aplicar el proceso.';

    } finally {

      this.cargando = false;
    }
  }

  // ==========================================================
  // 📊 RESUMEN
  // ==========================================================
  calcularResumen() {

    const totalInteresBruto = this.resultados
      .reduce((acc, r) => acc + (r.interesBruto || 0), 0);

    const totalRetencion = this.resultados
      .reduce((acc, r) => acc + (r.retencion || 0), 0);

    const totalInteresNeto = this.resultados
      .reduce((acc, r) => acc + (r.interesNeto || 0), 0);

    this.resumen = {
      totalInteresBruto,
      totalRetencion,
      totalInteresNeto,
      totalCuentas: this.resultados.length
    };
  }

  // ==========================================================
  // 📘 RESUMEN CONTABLE
  // ==========================================================
  // ==========================================================
  // 📘 RESUMEN CONTABLE
  // ==========================================================
  construirResumenContable(): void {

    if (!this.formaSeleccionada) {

      this.resumenContable = [];
      return;
    }

    const totalBruto = this.resultados
      .reduce((acc, r) => acc + (r.interesBruto || 0), 0);

    const totalRetencion = this.resultados
      .reduce((acc, r) => acc + (r.retencion || 0), 0);

    const totalNeto = this.resultados
      .reduce((acc, r) => acc + (r.interesNeto || 0), 0);

    this.resumenContable = [];

    // ======================================================
    // DÉBITO — GASTO INTERESES
    // ======================================================
    this.resumenContable.push({

      cuenta:
        this.formaSeleccionada.codigoCuentaGasto || '',

      nombre:
        this.formaSeleccionada.nombreCuentaGasto
        || 'CUENTA GASTO',

      debito: totalBruto,
      credito: 0
    });

    // ======================================================
    // CRÉDITO — PASIVO AHORROS
    // ======================================================
    this.resumenContable.push({

      cuenta:
        this.formaSeleccionada.codigoCuentaFormaCorto || '',

      nombre:
        this.formaSeleccionada.nombreCuentaFormaCorto
        || 'CUENTA AHORRO',

      debito: 0,
      credito: totalNeto
    });

    // ======================================================
    // CRÉDITO — RETENCIÓN
    // ======================================================
    if (totalRetencion > 0) {

      this.resumenContable.push({

        cuenta:
          this.formaSeleccionada.codigoCuentaRetencionFuente || '',

        nombre:
          this.formaSeleccionada.nombreCuentaRetencionFuente
          || 'RETENCIÓN FUENTE',

        debito: 0,
        credito: totalRetencion
      });
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
      fechaLiquidacion: '',

      tipoComprobante: '',
      numeroComprobante: ''
    };

    this.resultados = [];
    this.error = '';
    this.formas = [];
    this.tiposComprobantes = [];
    this.resumenContable = [];
    this.ultimaLiquidacion = null;

    this.formaSeleccionada = null;

    this.resumen = {
      totalInteresBruto: 0,
      totalRetencion: 0,
      totalInteresNeto: 0,
      totalCuentas: 0
    };

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

    const ag = this.agencias.find(
      a => a.idAgencia === agenciaId
    );

    const codigoAgencia =
      agenciaId === 0
        ? '00'
        : (ag?.codigoAgencia ?? '00');

    const nombreAgencia =
      ag?.nombreAgencia ?? 'TODAS';

    const formaSel = this.formaSeleccionada;

    const codigoForma =
      formaSel?.codigoForma ?? 'XX';

    const nombreForma =
      formaSel?.nombreForma ?? 'FORMA';

    const fileName =
      `INT_MENSUAL_SM_` +
      `AG${String(codigoAgencia).padStart(2, '0')}_` +
      `${nombreForma
        .replace(/\s+/g, '_')
        .toUpperCase()}_` +
      `${this.filtros.fechaLiquidacion}.xlsx`;

    this.exporter.exportarExcel(this.resultados, {

      fileName,

      fechaProceso:
        this.filtros.fechaProceso,

      fechaLiquidacion:
        this.filtros.fechaLiquidacion,

      codigoAgencia:
        String(codigoAgencia).padStart(2, '0'),

      nombreAgencia,

      codigoForma,
      nombreForma
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

    const formaSel = this.formaSeleccionada;

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

    this.printService.imprimir(
      this.resultados,
      filtrosExtendidos
    );
  }

  cambiarFechaContable(): void {
    this.filtros.fechaLiquidacion = this.filtros.fechaProceso;
  }

  totalDebitoContable(): number {
    return this.resumenContable
      .reduce((acc, r) => acc + (r.debito || 0), 0);
  }

  totalCreditoContable(): number {
    return this.resumenContable
      .reduce((acc, r) => acc + (r.credito || 0), 0);
  }

  comprobanteCuadrado(): boolean {
    return this.totalDebitoContable() === this.totalCreditoContable();
  }

}
