import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  LiquidacionContabilizacionApi,
  LiquidacionMovimientoContableDTO,
  LiquidacionContabilizacionResultadoDTO
} from './liquidacion-contabilizacion.api';

import { PeriodosNominaApi } from '../../periodos-nomina/periodos-nomina.api';

import {
  TiposComprobantesApi,
  TipoComprobante
} from '../../../contabilidad/tipos-comprobantes/tipos-comprobantes.api';

import { LiquidacionContabilizacionExporter } from './liquidacion-contabilizacion.exporter';

interface ResumenCuentaDTO {
  idAgencia: number;
  codigoCuenta: string;
  nombreCuenta: string;
  totalDebito: number;
  totalCredito: number;
}

@Component({
  standalone: true,
  selector: 'app-liquidacion-contabilizacion',
  imports: [CommonModule, FormsModule],
  templateUrl: './liquidacion-contabilizacion.component.html',
  styleUrls: ['./liquidacion-contabilizacion.component.scss']
})
export class LiquidacionContabilizacionComponent {

  private api = inject(LiquidacionContabilizacionApi);
  private periodosApi = inject(PeriodosNominaApi);
  private tiposComprobantesApi = inject(TiposComprobantesApi);
  private liquidacionContabilizacionExporter = inject(LiquidacionContabilizacionExporter);

  idPeriodoNomina: number | null = null;
  periodos: any[] = [];
  tiposComprobantes: TipoComprobante[] = [];
  consecutivoSugerido: string | null = null;

  cargando = false;
  ejecutando = false;
  reversando = false;

  movimientos: LiquidacionMovimientoContableDTO[] = [];
  resumenCuentas: ResumenCuentaDTO[] = [];
  resultado: LiquidacionContabilizacionResultadoDTO | null = null;
  comprobanteExistente: string | null = null;

  error = '';

  ngOnInit(): void {

    this.periodosApi
      .listarParaContabilizacion()
      .subscribe(data => {

        this.periodos = data ?? [];

        if (this.periodos.length > 0) {

          const ultimoPeriodo =
            [...this.periodos].sort((a, b) => b.idPeriodo - a.idPeriodo)[0];

          this.idPeriodoNomina = ultimoPeriodo?.idPeriodo ?? null;

          this.cargarFechaPeriodo();
          this.cargarTiposComprobantesPorPeriodo();
        }

      });

  }

  // 🔵 Documento contable preview
  tipoComprobante = 'NM';
  numeroComprobantePreview = '';
  fechaContabilizacion: string | null = null;

  preview(): void {

    this.error = '';
    this.resultado = null;
    this.cargando = true;

    if (!this.idPeriodoNomina) {
      this.error = 'Debe seleccionar un período de nómina.';
      this.cargando = false;
      return;
    }

    this.api.preview(this.idPeriodoNomina as number).subscribe({

      next: (data) => {

        this.movimientos = data ?? [];
        this.resumenCuentas = this.construirResumen(this.movimientos);

        this.cargando = false;
      },

      error: (err) => {

        console.error(err);
        this.error = 'Error generando preview contable.';

        this.movimientos = [];
        this.resumenCuentas = [];

        this.cargando = false;
      }
    });
  }

  ejecutar(): void {

    this.error = '';

    if (!this.idPeriodoNomina) {
      this.error = 'Debe indicar el período de nómina.';
      return;
    }

    const p = this.periodoSeleccionado;

    const descripcion = p
      ? `${p.nombreAgencia} | ${p.descripcion} | ${p.tipoPeriodo}`
      : `ID ${this.idPeriodoNomina}`;

    const ok = confirm(
      `¿Desea contabilizar la liquidación de nómina del período:\n\n${descripcion}?`
    );

    if (!ok) return;

    if (this.ejecutando) return;

    this.ejecutando = true;

    this.api.ejecutar(this.idPeriodoNomina).subscribe({

      next: (resp) => {

        this.resultado = resp;
        this.ejecutando = false;
        this.error = '';

        alert('Contabilización realizada correctamente.');

        // limpiar pantalla para no regenerar preview sobre período ya contabilizado
        this.movimientos = [];
        this.resumenCuentas = [];

        // refrescar estado del período y comprobante existente
        this.recargarPeriodos(this.idPeriodoNomina);
      },

      error: (err) => {

        console.error(err);
        this.error = 'Error ejecutando la contabilización.';
        this.ejecutando = false;
      }
    });
  }

  reversar(): void {

    this.error = '';

    if (!this.idPeriodoNomina) {
      this.error = 'Debe indicar el período de nómina.';
      return;
    }

    const p = this.periodoSeleccionado;

    const descripcion = p
      ? `${p.nombreAgencia} | ${p.descripcion} | ${p.tipoPeriodo}`
      : `ID ${this.idPeriodoNomina}`;

    const ok = confirm(
      `¿Desea REVERSAR la contabilización de nómina del período:\n\n${descripcion}?\n\n⚠ Esta acción eliminará el comprobante contable generado.`
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

        // refrescar datos
        this.cargarComprobanteExistente();
        this.preview();

      },

      error: (err) => {

        console.error(err);

        this.error =
          err?.error?.message ||
          'Error reversando la contabilización.';

        this.reversando = false;
      }

    });

  }

  // ======================================================
  // TOTALES CONTABLES
  // ======================================================

  totalDebito(): number {
    return this.movimientos.reduce(
      (acc, x) => acc + (Number(x.debito) || 0),
      0
    );
  }

  totalCredito(): number {
    return this.movimientos.reduce(
      (acc, x) => acc + (Number(x.credito) || 0),
      0
    );
  }

  cuadre(): number {
    return this.totalDebito() - this.totalCredito();
  }

  actualizarConsecutivo(): void {

    if (!this.tipoComprobante) {
      this.numeroComprobantePreview = '';
      return;
    }

    const tc = this.tiposComprobantes.find(
      t => t.tipoComprobante === this.tipoComprobante
    );

    if (!tc) return;

    const siguiente = (tc.cscComprobante ?? 0) + 1;

    const consecutivo = siguiente
      .toString()
      .padStart(7, '0');

    this.numeroComprobantePreview = consecutivo;

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
      this.error = 'Debe seleccionar un período de nómina.';
      return;
    }

    const periodo = this.periodos.find(
      p => String(p.idPeriodo) === String(this.idPeriodoNomina)
    );

    if (!periodo) {
      this.error = 'No se pudo obtener la información del período.';
      return;
    }

    this.liquidacionContabilizacionExporter.exportarComprobante(
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

  // ======================================================
  // RESUMEN POR CUENTA
  // ======================================================

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

      if (a.idAgencia !== b.idAgencia)
        return a.idAgencia - b.idAgencia;

      return a.codigoCuenta.localeCompare(b.codigoCuenta);
    });
  }

  cargarComprobanteExistente(): void {

    if (!this.idPeriodoNomina) return;

    this.api.obtenerComprobante(this.idPeriodoNomina)
      .subscribe({

        next: (numero) => {

          this.comprobanteExistente = numero ?? null;

        },

        error: (err) => {
          console.error(err);
          this.comprobanteExistente = null;
        }

      });

  }

  private recargarPeriodos(idPeriodoMantener: number | null): void {
    this.periodosApi.listarParaContabilizacion().subscribe({
      next: (data) => {
        this.periodos = data ?? [];

        if (idPeriodoMantener != null) {
          const existe = this.periodos.find(
            p => String(p.idPeriodo) === String(idPeriodoMantener)
          );

          this.idPeriodoNomina = existe ? idPeriodoMantener : null;
        }

        this.comprobanteExistente = null;
        this.cargarFechaPeriodo();
      },
      error: (err) => {
        console.error(err);
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

    // ======================================================
    // 🔥 ESTADO DEL PERÍODO
    // ======================================================

    get periodoSeleccionado(): any | null {
      return this.periodos.find(
        p => String(p.idPeriodo) === String(this.idPeriodoNomina)
      ) ?? null;
    }

    get estadoPeriodoSeleccionado(): string {
      return (this.periodoSeleccionado?.estado ?? '').toUpperCase().trim();
    }

    get esPeriodoCerrado(): boolean {
      return this.estadoPeriodoSeleccionado === 'CERRADO';
    }

    get esPeriodoContabilizado(): boolean {
      return this.estadoPeriodoSeleccionado === 'CONTABILIZADO';
    }

}
