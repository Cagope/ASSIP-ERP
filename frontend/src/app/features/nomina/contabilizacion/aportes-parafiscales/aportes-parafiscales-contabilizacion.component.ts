import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  AportesParafiscalesContabilizacionApi,
  AportesParafiscalesContabilizacionResultadoDTO
} from './aportes-parafiscales-contabilizacion.api';

import { LiquidacionMovimientoContableDTO } from '../liquidacion/liquidacion-contabilizacion.api';
import { PeriodosNominaApi } from '../../periodos-nomina/periodos-nomina.api';
import {
  TiposComprobantesApi,
  TipoComprobante
} from '../../../contabilidad/tipos-comprobantes/tipos-comprobantes.api';

import { AportesParafiscalesContabilizacionExporter } from './aportes-parafiscales-contabilizacion.exporter';

interface ResumenCuentaDTO {
  idAgencia: number;
  codigoCuenta: string;
  nombreCuenta: string;
  totalDebito: number;
  totalCredito: number;
}

@Component({
  standalone: true,
  selector: 'app-aportes-parafiscales-contabilizacion',
  imports: [CommonModule, FormsModule],
  templateUrl: './aportes-parafiscales-contabilizacion.component.html',
  styleUrls: ['./aportes-parafiscales-contabilizacion.component.scss']
})
export class AportesParafiscalesContabilizacionComponent {

  private api = inject(AportesParafiscalesContabilizacionApi);
  private periodosApi = inject(PeriodosNominaApi);
  private tiposComprobantesApi = inject(TiposComprobantesApi);
  private aportesParafiscalesExporter =
    inject(AportesParafiscalesContabilizacionExporter);

  idPeriodoNomina: number | null = null;
  periodos: any[] = [];
  tiposComprobantes: TipoComprobante[] = [];

  cargando = false;
  ejecutando = false;
  reversando = false;

  movimientos: LiquidacionMovimientoContableDTO[] = [];
  resumenCuentas: ResumenCuentaDTO[] = [];
  resultado: AportesParafiscalesContabilizacionResultadoDTO | null = null;
  comprobanteExistente: string | null = null;
  error = '';

  tipoComprobante = 'NM';
  numeroComprobantePreview = '';
  fechaContabilizacion: string | null = null;

  ngOnInit(): void {
    this.periodosApi.listarParaContabilizacion().subscribe(data => {
      this.periodos = data ?? [];

      if (this.periodos.length > 0) {
        const ultimoPeriodo =
          [...this.periodos].sort((a, b) => b.idPeriodo - a.idPeriodo)[0];

        this.idPeriodoNomina = ultimoPeriodo?.idPeriodo ?? null;

        this.cargarFechaPeriodo();
        this.cargarComprobanteExistente();
        this.cargarTiposComprobantesPorPeriodo();
      }
    });
  }

  preview(): void {
    this.error = '';
    this.resultado = null;
    this.cargando = true;

    if (!this.idPeriodoNomina) {
      this.error = 'Debe seleccionar un período para identificar el mes.';
      this.cargando = false;
      return;
    }

    this.api.preview(this.idPeriodoNomina).subscribe({
      next: (data) => {
        this.movimientos = data ?? [];
        this.resumenCuentas = this.construirResumen(this.movimientos);
        this.cargando = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error generando preview.';
        this.movimientos = [];
        this.resumenCuentas = [];
        this.cargando = false;
      }
    });
  }

  ejecutar(): void {
    this.error = '';

    if (!this.idPeriodoNomina) {
      this.error = 'Debe indicar el período.';
      return;
    }

    if (this.comprobanteExistente) {
      this.error = 'El mes ya se encuentra contabilizado.';
      return;
    }

    const p = this.periodoSeleccionado;
    const descripcion = p
      ? `${p.nombreAgencia} | ${p.descripcion} | ${p.tipoPeriodo}`
      : `ID ${this.idPeriodoNomina}`;

    const ok = confirm(
      `¿Desea contabilizar los aportes empleador y parafiscales del mes:\n\n${descripcion}?`
    );

    if (!ok || this.ejecutando) return;

    this.ejecutando = true;

    this.api.ejecutar(this.idPeriodoNomina).subscribe({
      next: (resp) => {
        this.resultado = resp;
        this.ejecutando = false;

        alert('Contabilización realizada correctamente.');

        this.movimientos = [];
        this.resumenCuentas = [];
        this.error = '';

        this.cargarComprobanteExistente();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error ejecutando contabilización.';
        this.ejecutando = false;
      }
    });
  }

  reversar(): void {
    this.error = '';

    if (!this.idPeriodoNomina) {
      this.error = 'Debe indicar el período.';
      return;
    }

    const p = this.periodoSeleccionado;
    const descripcion = p
      ? `${p.nombreAgencia} | ${p.descripcion} | ${p.tipoPeriodo}`
      : `ID ${this.idPeriodoNomina}`;

    const ok = confirm(
      `¿Desea REVERSAR la contabilización de aportes empleador y parafiscales del mes:\n\n${descripcion}?\n\n⚠ Esta acción eliminará el comprobante contable generado.`
    );

    if (!ok) return;

    this.reversando = true;

    this.api.reversar(this.idPeriodoNomina).subscribe({
      next: () => {
        this.reversando = false;

        alert('Contabilización reversada correctamente.');

        this.resultado = null;
        this.movimientos = [];
        this.resumenCuentas = [];

        this.cargarComprobanteExistente();
        this.preview();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error reversando contabilización.';
        this.reversando = false;
      }
    });
  }

  totalDebito(): number {
    return this.movimientos.reduce((acc, x) => acc + (Number(x.debito) || 0), 0);
  }

  totalCredito(): number {
    return this.movimientos.reduce((acc, x) => acc + (Number(x.credito) || 0), 0);
  }

  cuadre(): number {
    return this.totalDebito() - this.totalCredito();
  }

  actualizarConsecutivo(): void {
    if (!this.tipoComprobante) {
      this.numeroComprobantePreview = '';
      return;
    }

    const tc = this.tiposComprobantes.find(t => t.tipoComprobante === this.tipoComprobante);
    if (!tc) return;

    const siguiente = (tc.cscComprobante ?? 0) + 1;
    this.numeroComprobantePreview = siguiente.toString().padStart(7, '0');
  }

  cargarFechaPeriodo(): void {
    const periodo = this.periodos.find(
      p => String(p.idPeriodo) === String(this.idPeriodoNomina)
    );

    if (!periodo) {
      this.fechaContabilizacion = null;
      this.tiposComprobantes = [];
      this.numeroComprobantePreview = '';
      return;
    }

    this.fechaContabilizacion = periodo.fechaFin ?? null;
    this.movimientos = [];
    this.resumenCuentas = [];
    this.resultado = null;
    this.error = '';
    this.comprobanteExistente = null;

    this.cargarTiposComprobantesPorPeriodo();

    if (this.idPeriodoNomina) {
      this.api.obtenerComprobante(this.idPeriodoNomina)
        .subscribe(numero => {
          this.comprobanteExistente = numero ?? null;
          if (numero) {
            this.numeroComprobantePreview = numero;
          }
        });
    }
  }

  exportarExcel(): void {
    if (!this.idPeriodoNomina) {
      this.error = 'Debe seleccionar un período para identificar el mes.';
      return;
    }

    const periodo = this.periodos.find(
      p => String(p.idPeriodo) === String(this.idPeriodoNomina)
    );

    if (!periodo) {
      this.error = 'No se pudo obtener la información del período.';
      return;
    }

    this.aportesParafiscalesExporter.exportarComprobante(
      this.movimientos,
      {
        periodo: this.idPeriodoNomina as number,
        anio: periodo.anio,
        mes: periodo.mes,
        numeroPeriodo: periodo.numeroPeriodo,
        codigoAgencia: periodo.codigoAgencia ?? periodo.idAgencia,
        fechaContabilizacion: this.fechaContabilizacion,
        tipoComprobante: this.tipoComprobante,
        numeroComprobante: this.numeroComprobantePreview
      }
    );
  }

  private construirResumen(items: LiquidacionMovimientoContableDTO[]): ResumenCuentaDTO[] {
    const map = new Map<string, ResumenCuentaDTO>();

    for (const item of items) {
      const key = `${item.idAgencia}-${item.codigoCuenta}`;

      if (!map.has(key)) {
        map.set(key, {
          idAgencia: item.idAgencia,
          codigoCuenta: item.codigoCuenta,
          nombreCuenta: item.nombreCuenta,
          totalDebito: 0,
          totalCredito: 0
        });
      }

      const actual = map.get(key)!;
      actual.totalDebito += Number(item.debito) || 0;
      actual.totalCredito += Number(item.credito) || 0;
    }

    return Array.from(map.values()).sort((a, b) => {
      if (a.idAgencia !== b.idAgencia) return a.idAgencia - b.idAgencia;
      return a.codigoCuenta.localeCompare(b.codigoCuenta);
    });
  }

  cargarComprobanteExistente(): void {
    if (!this.idPeriodoNomina) return;

    this.api.obtenerComprobante(this.idPeriodoNomina)
      .subscribe({
        next: (numero) => {
          this.comprobanteExistente = numero ?? null;
          if (numero) {
            this.numeroComprobantePreview = numero;
          }
        },
        error: () => {
          this.comprobanteExistente = null;
        }
      });
  }

  private cargarTiposComprobantesPorPeriodo(): void {
    const periodo = this.periodos.find(
      p => String(p.idPeriodo) === String(this.idPeriodoNomina)
    );

    if (!periodo?.idAgencia) {
      this.tiposComprobantes = [];
      this.numeroComprobantePreview = '';
      return;
    }

    this.tiposComprobantesApi
      .listarPorAgencia(periodo.idAgencia)
      .subscribe(tc => {
        this.tiposComprobantes = tc ?? [];
        this.actualizarConsecutivo();
      });
  }

  get periodoSeleccionado(): any | null {
    return this.periodos.find(
      p => String(p.idPeriodo) === String(this.idPeriodoNomina)
    ) ?? null;
  }

  get estadoPeriodoSeleccionado(): string {
    return (this.periodoSeleccionado?.estado ?? '').toUpperCase().trim();
  }

  get esPeriodoContabilizado(): boolean {
    return this.estadoPeriodoSeleccionado === 'CONTABILIZADO';
  }
}
