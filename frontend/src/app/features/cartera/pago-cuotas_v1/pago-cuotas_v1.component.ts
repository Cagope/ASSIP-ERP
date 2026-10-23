import { CommonModule } from '@angular/common';
import { Component, OnDestroy, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';

import {
  ConsultaCreditosAsociado,
  ConsultaCreditosBuscadorComponent
} from '../consulta-creditos/buscador/consulta-creditos-buscador.component';
import { TablaAmortizacionComponent } from '../../../shared/financiero/tabla-amortizacion/tabla-amortizacion.component';
import { PagoCuotasV1Api } from './pago-cuotas_v1.api';
import {
  PagoCuotasCreditoDetalle,
  PagoCuotasCreditoResumen,
  PagoCuotasLiquidacion,
  PagoCuotasPagoRealizado,
  PagoCuotasSimulacion
} from './pago-cuotas_v1.models';

/**
 * Pago de Cuotas V1: consulta, liquidación y simulación exclusivamente.
 * El registro financiero del pago aún no está habilitado.
 */
@Component({
  selector: 'app-pago-cuotas-v1',
  standalone: true,
  imports: [CommonModule, FormsModule, ConsultaCreditosBuscadorComponent, TablaAmortizacionComponent],
  templateUrl: './pago-cuotas_v1.component.html',
  styleUrls: ['./pago-cuotas_v1.component.scss']
})
export class PagoCuotasV1Component implements OnDestroy {
  @ViewChild('tablaAmortizacion') tablaAmortizacion?: TablaAmortizacionComponent;
  private readonly destruir$ = new Subject<void>();

  asociadoSeleccionado: ConsultaCreditosAsociado | null = null;
  creditos: PagoCuotasCreditoResumen[] = [];
  creditoSeleccionado: PagoCuotasCreditoDetalle | null = null;
  idCarteraCreditoSeleccionado: number | null = null;

  fechaPago = this.fechaLocalActual();
  periodoInteresMeses = 1;
  cuotasACancelar: number | null = null;
  valorPago: number | null = null;

  liquidacion: PagoCuotasLiquidacion | null = null;
  simulacion: PagoCuotasSimulacion | null = null;

  pagosRealizados: PagoCuotasPagoRealizado[] = [];
  mostrarPagosRealizados = false;
  mostrarTablaAmortizacion = false;

  cargandoCreditos = false;
  cargandoDetalle = false;
  cargandoLiquidacion = false;
  cargandoSimulacion = false;
  cargandoPagos = false;
  error: string | null = null;
  aviso: string | null = null;

  constructor(private readonly api: PagoCuotasV1Api) {}

  get ocupado(): boolean {
    return this.cargandoCreditos || this.cargandoDetalle ||
      this.cargandoLiquidacion || this.cargandoSimulacion || this.cargandoPagos;
  }

  get puedeLiquidar(): boolean {
    return !!this.creditoSeleccionado && !!this.fechaPago &&
      Number.isInteger(this.periodoInteresMeses) && this.periodoInteresMeses > 0 &&
      (this.cuotasACancelar == null ||
        (Number.isInteger(this.cuotasACancelar) && this.cuotasACancelar > 0)) &&
      !this.ocupado;
  }

  get puedeSimular(): boolean {
    return !!this.liquidacion && this.valorPago != null &&
      Number.isFinite(this.valorPago) && this.valorPago >= 0 && !this.ocupado;
  }

  seleccionarAsociado(asociado: ConsultaCreditosAsociado): void {
    const id = Number(asociado.idDatosPersonal);
    if (!Number.isInteger(id) || id <= 0 || this.ocupado) return;

    this.asociadoSeleccionado = asociado;
    this.creditos = [];
    this.limpiarCredito();
    this.error = null;
    this.cargandoCreditos = true;

    this.api.listarCreditosPorAsociado(id)
      .pipe(takeUntil(this.destruir$))
      .subscribe({
        next: creditos => {
          this.creditos = Array.isArray(creditos)
            ? creditos.filter(credito => Number(credito.saldoActual) > 0)
            : [];
          this.cargandoCreditos = false;
        },
        error: error => {
          this.cargandoCreditos = false;
          this.error = this.mensajeError(error, 'No fue posible consultar los créditos.');
        }
      });
  }

  limpiarBusqueda(): void {
    if (this.ocupado) return;
    this.asociadoSeleccionado = null;
    this.creditos = [];
    this.limpiarCredito();
    this.error = null;
    this.aviso = null;
  }

  seleccionarCredito(credito: PagoCuotasCreditoResumen): void {
    if (this.ocupado) return;
    const id = Number(credito.idCarteraCredito);
    if (!Number.isInteger(id) || id <= 0) return;

    this.limpiarCredito();
    this.idCarteraCreditoSeleccionado = id;
    this.cargandoDetalle = true;
    this.error = null;

    this.api.consultarCredito(id)
      .pipe(takeUntil(this.destruir$))
      .subscribe({
        next: detalle => {
          this.creditoSeleccionado = detalle;
          this.cargandoDetalle = false;
        },
        error: error => {
          this.cargandoDetalle = false;
          this.idCarteraCreditoSeleccionado = null;
          this.error = this.mensajeError(error, 'No fue posible consultar el crédito.');
        }
      });
  }

  cambiarCredito(): void {
    if (this.ocupado) return;

    this.limpiarCredito();
    this.error = null;
    this.aviso = null;
  }

  /** La fecha nueva requiere volver a determinar automáticamente las cuotas. */
  cambiarFechaPago(): void {
    this.cuotasACancelar = null;
    this.invalidarCalculo();
  }

  /** Invalida resultados cuando cambian los parámetros de liquidación. */
  invalidarCalculo(): void {
    this.liquidacion = null;
    this.simulacion = null;
    this.valorPago = null;
    this.error = null;
  }

  /** Recalcula con la cantidad elegida al confirmar el cambio del campo. */
  recalcularCuotas(): void {
    this.invalidarCalculo();
    if (this.puedeLiquidar) {
      this.liquidar();
    }
  }

  /** Ejecutar cuando el funcionario cambie el valor recibido. */
  invalidarSimulacion(): void {
    this.simulacion = null;
    this.error = null;
  }

  liquidar(): void {
    if (!this.puedeLiquidar || !this.idCarteraCreditoSeleccionado) return;
    this.invalidarCalculo();
    this.cargandoLiquidacion = true;

    this.api.liquidar({
      idCarteraCredito: this.idCarteraCreditoSeleccionado,
      fechaPago: this.fechaPago,
      periodoInteresMeses: this.periodoInteresMeses,
      cuotasACancelar: this.cuotasACancelar
    }).pipe(takeUntil(this.destruir$)).subscribe({
      next: liquidacion => {
        this.liquidacion = liquidacion;
        this.cuotasACancelar = liquidacion.cuotasConsideradas;
        this.valorPago = liquidacion.valorPagoSugerido;
        this.cargandoLiquidacion = false;
      },
      error: error => {
        this.cargandoLiquidacion = false;
        this.error = this.mensajeError(error, 'No fue posible liquidar las cuotas.');
      }
    });
  }

  simularPago(): void {
    if (!this.puedeSimular || !this.idCarteraCreditoSeleccionado || this.valorPago == null) return;
    this.simulacion = null;
    this.error = null;
    this.cargandoSimulacion = true;

    this.api.simularPago({
      idCarteraCredito: this.idCarteraCreditoSeleccionado,
      fechaPago: this.fechaPago,
      periodoInteresMeses: this.periodoInteresMeses,
      cuotasACancelar: this.cuotasACancelar,
      valorPago: this.valorPago
    }).pipe(takeUntil(this.destruir$)).subscribe({
      next: simulacion => {
        this.simulacion = simulacion;
        this.cargandoSimulacion = false;
      },
      error: error => {
        this.cargandoSimulacion = false;
        this.error = this.mensajeError(error, 'No fue posible simular el pago.');
      }
    });
  }

  verPagosRealizados(): void {
    if (!this.idCarteraCreditoSeleccionado || this.ocupado) return;
    if (this.mostrarPagosRealizados) {
      this.mostrarPagosRealizados = false;
      return;
    }
    this.mostrarTablaAmortizacion = false;
    this.error = null;
    this.cargandoPagos = true;

    this.api.listarPagosRealizados(this.idCarteraCreditoSeleccionado)
      .pipe(takeUntil(this.destruir$))
      .subscribe({
        next: pagos => {
          this.pagosRealizados = Array.isArray(pagos) ? pagos : [];
          this.mostrarPagosRealizados = true;
          this.cargandoPagos = false;
        },
        error: error => {
          this.cargandoPagos = false;
          this.error = this.mensajeError(error, 'No fue posible consultar los pagos realizados.');
        }
      });
  }

  verTablaAmortizacion(): void {
    if (!this.creditoSeleccionado || !this.idCarteraCreditoSeleccionado || this.ocupado) return;

    this.mostrarPagosRealizados = false;
    this.mostrarTablaAmortizacion = false;
    this.aviso = null;

    this.tablaAmortizacion?.abrirCredito(this.idCarteraCreditoSeleccionado);
  }

  cerrarConsultas(): void {
    this.mostrarPagosRealizados = false;
    this.mostrarTablaAmortizacion = false;
    this.aviso = null;
  }

  trackByCredito(_index: number, credito: PagoCuotasCreditoResumen): number {
    return credito.idCarteraCredito;
  }

  trackByPago(_index: number, pago: PagoCuotasPagoRealizado): string {
    return `${pago.fechaPago}|${pago.tipoComprobante}|${pago.numeroComprobante}`;
  }

  totalOtrosConceptos(pago: PagoCuotasPagoRealizado): number {
    return (pago.aportes ?? 0)
         + (pago.papeleria ?? 0)
         + (pago.fondoGarantia ?? 0)
         + (pago.otrosValores ?? 0);
  }

  ngOnDestroy(): void {
    this.destruir$.next();
    this.destruir$.complete();
  }

  private limpiarCredito(): void {
    this.idCarteraCreditoSeleccionado = null;
    this.creditoSeleccionado = null;
    this.cuotasACancelar = null;
    this.liquidacion = null;
    this.simulacion = null;
    this.valorPago = null;
    this.pagosRealizados = [];
    this.cerrarConsultas();
  }

  private fechaLocalActual(): string {
    const hoy = new Date();
    const ano = hoy.getFullYear();
    const mes = String(hoy.getMonth() + 1).padStart(2, '0');
    const dia = String(hoy.getDate()).padStart(2, '0');
    return `${ano}-${mes}-${dia}`;
  }

  private mensajeError(error: unknown, predeterminado: string): string {
    if (!error || typeof error !== 'object') return predeterminado;
    const respuesta = error as {
      message?: string;
      error?: string | { mensaje?: string; message?: string };
    };
    if (typeof respuesta.error === 'string') return respuesta.error || predeterminado;
    return respuesta.error?.mensaje ?? respuesta.error?.message ?? respuesta.message ?? predeterminado;
  }
}

