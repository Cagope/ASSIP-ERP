import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { RevalorizacionApi } from './revalorizacion.api';
import { GeneralApi } from '../../../../shared/general/general.api';
import { AgenciaFormaApi } from '../../../../shared/agencia-forma/agencia-forma.api';

import { RevalorizacionExporterService } from './revalorizacion-exporter.service';
import { RevalorizacionPrintService } from './revalorizacion-print.service';

import {
  TiposComprobantesApi,
  TipoComprobante
} from '../../../contabilidad/tipos-comprobantes/tipos-comprobantes.api';

@Component({
  selector: 'app-revalorizacion-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './revalorizacion-list.component.html',
  styleUrls: ['./revalorizacion-list.component.scss']
})
export class RevalorizacionListComponent implements OnInit {

  private readonly api = inject(RevalorizacionApi);
  private readonly generalApi = inject(GeneralApi);
  private readonly agenciaFormaApi = inject(AgenciaFormaApi);
  private readonly tiposComprobantesApi = inject(TiposComprobantesApi);

  private readonly exporter = inject(RevalorizacionExporterService);
  private readonly printService = inject(RevalorizacionPrintService);

  agencias: any[] = [];
  formas: any[] = [];
  tiposComprobantes: TipoComprobante[] = [];

  formaSeleccionada: any = null;
  resumenContable: any[] = [];
  ultimaLiquidacion: any = null;

  resumen = {
    totalRevalorizacion: 0,
    totalPromedio: 0,
    totalSaldoActual: 0,
    totalCuentas: 0
  };

  filtros = {
    agenciaId: '0',
    formaId: '',

    fechaInicio: '',
    fechaFin: '',
    fechaProceso: '',
    fechaLiquidacion: '',

    tasa: '',

    tipoComprobante: '',
    numeroComprobante: ''
  };

  cargando = false;
  error = '';
  resultados: any[] = [];

  async ngOnInit(): Promise<void> {
    this.error = '';
    this.cargando = true;

    try {
      const ag = await this.generalApi.listarAgencias().toPromise();
      this.agencias = ag ?? [];
      this.formas = [];
    } catch (e) {
      console.error('Error cargando agencias:', e);
      this.error = 'No se pudieron cargar las agencias.';
      this.agencias = [];
    } finally {
      this.cargando = false;
    }
  }

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
      console.error('Error cargando formas:', e);
      this.formas = [];
    }
  }

  seleccionarForma(): void {
    this.formaSeleccionada = this.formas.find(
      f => f.idFormaAhorro === Number(this.filtros.formaId)
    ) ?? null;

    this.cargarUltimaLiquidacion();
  }

  async cargarUltimaLiquidacion(): Promise<void> {
    this.ultimaLiquidacion = null;

    const idAgencia = Number(this.filtros.agenciaId);
    const idForma = Number(this.filtros.formaId);

    if (!idAgencia || !idForma) {
      return;
    }

    try {
      this.ultimaLiquidacion =
        await this.api.obtenerUltimaLiquidacion(idAgencia, idForma);
    } catch (e) {
      console.error('Error cargando última liquidación:', e);
      this.ultimaLiquidacion = null;
    }
  }

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
      console.error('Error obteniendo comprobante:', e);
      this.error = 'No se pudo obtener el consecutivo del comprobante.';
    }
  }

  async liquidar(): Promise<void> {
    this.error = '';

    if (!this.filtros.formaId) {
      this.error = 'Debe seleccionar la forma.';
      return;
    }

    if (!this.filtros.fechaInicio || !this.filtros.fechaFin) {
      this.error = 'Debe seleccionar la fecha inicio y fecha fin.';
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

    if (!this.filtros.tasa || Number(this.filtros.tasa) <= 0) {
      this.error = 'Debe indicar una tasa válida.';
      return;
    }

    this.cargando = true;
    this.resultados = [];

    try {
      const body = this.crearBodyBase();

      const res = await this.api.liquidar(body);

      this.resultados = Array.isArray(res)
        ? res
        : (res ?? []);

      this.calcularResumen();
      this.construirResumenContable();
    } catch (e: any) {
      console.error('Error ejecutando revalorización:', e);

      const texto =
        e?.error?.message ??
        (typeof e?.error === 'string' ? e.error : null) ??
        e?.message ??
        '';

      this.error = texto || 'No se pudo ejecutar el proceso.';
    } finally {
      this.cargando = false;
    }
  }

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
      '¿Desea aplicar la revalorización de aportes?\n\n' +
      'Este proceso actualizará saldos y generará extractos.'
    )) {
      return;
    }

    this.cargando = true;

    try {
      const body = {
        ...this.crearBodyBase(),
        tipoComprobante: this.filtros.tipoComprobante
      };

      await this.api.aplicar(body);

      alert('Proceso aplicado correctamente.');

      this.limpiar();
    } catch (e: any) {
      console.error('Error aplicando proceso:', e);

      this.error =
        e?.error?.message ||
        'No se pudo aplicar el proceso.';
    } finally {
      this.cargando = false;
    }
  }

  private crearBodyBase(): any {
    return {
      agenciaId: Number(this.filtros.agenciaId),
      formaId: Number(this.filtros.formaId),

      fechaInicio: this.filtros.fechaInicio,
      fechaFin: this.filtros.fechaFin,

      fechaProceso: this.filtros.fechaProceso,
      fechaLiquidacion: this.filtros.fechaLiquidacion,

      tasaRevalorizacion: Number(this.filtros.tasa)
    };
  }

  calcularResumen(): void {
    const totalRevalorizacion = this.resultados
      .reduce((acc, r) => acc + (r.valorRevalorizacion || 0), 0);

    const totalPromedio = this.resultados
      .reduce((acc, r) => acc + (r.valorPromedio || 0), 0);

    const totalSaldoActual = this.resultados
      .reduce((acc, r) => acc + (r.saldoActual || 0), 0);

    this.resumen = {
      totalRevalorizacion,
      totalPromedio,
      totalSaldoActual,
      totalCuentas: this.resultados.length
    };
  }

  construirResumenContable(): void {
    if (!this.formaSeleccionada) {
      this.resumenContable = [];
      return;
    }

    const totalRevalorizacion = this.resultados
      .reduce((acc, r) => acc + (r.valorRevalorizacion || 0), 0);

    this.resumenContable = [];

    this.resumenContable.push({
      cuenta: this.formaSeleccionada.codigoCuentaGasto || '',
      nombre: this.formaSeleccionada.nombreCuentaGasto || 'CUENTA GASTO',
      debito: totalRevalorizacion,
      credito: 0
    });

    this.resumenContable.push({
      cuenta: this.formaSeleccionada.codigoCuentaFormaCorto || '',
      nombre: this.formaSeleccionada.nombreCuentaFormaCorto || 'CUENTA APORTES',
      debito: 0,
      credito: totalRevalorizacion
    });
  }

  limpiar(): void {
    this.filtros = {
      agenciaId: '0',
      formaId: '',

      fechaInicio: '',
      fechaFin: '',
      fechaProceso: '',
      fechaLiquidacion: '',

      tasa: '',

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
      totalRevalorizacion: 0,
      totalPromedio: 0,
      totalSaldoActual: 0,
      totalCuentas: 0
    };
  }

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
      `REVALORIZACION_APORTES_` +
      `AG${String(codigoAgencia).padStart(2, '0')}_` +
      `${nombreForma.replace(/\s+/g, '_').toUpperCase()}_` +
      `${this.filtros.fechaLiquidacion}.xlsx`;

    this.exporter.exportarExcel(this.resultados, {
      fileName,
      fechaInicio: this.filtros.fechaInicio,
      fechaFin: this.filtros.fechaFin,
      fechaProceso: this.filtros.fechaProceso,
      fechaLiquidacion: this.filtros.fechaLiquidacion,
      tasa: this.filtros.tasa,
      codigoAgencia: String(codigoAgencia).padStart(2, '0'),
      nombreAgencia,
      codigoForma,
      nombreForma
    });
  }

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
