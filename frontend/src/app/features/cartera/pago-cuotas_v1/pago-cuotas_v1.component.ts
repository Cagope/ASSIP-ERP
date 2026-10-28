import { CommonModule } from '@angular/common';
import { Component, OnDestroy, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';
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
  PagoCuotasSimulacion, PagoCuotasCajaRecaudo, PagoCuotasOperacionPreparada, PagoCuotasMedioRecaudo, PagoCuotasCuentaAhorro, PagoCuotasComprobantePrevio
} from './pago-cuotas_v1.models';

/**
 * Pago de Cuotas V1: consulta, simulación y envío de operaciones pendientes a Caja.
 * Cartera aplica financieramente el pago; Caja confirma únicamente el recaudo físico.
 */
@Component({
  selector: 'app-pago-cuotas-v1',
  standalone: true,
  imports: [CommonModule, FormsModule, NumericFormatDirective, ConsultaCreditosBuscadorComponent, TablaAmortizacionComponent],
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
  pagoTotal = false;
  valorPago: number | null = null;

  liquidacion: PagoCuotasLiquidacion | null = null;
  simulacion: PagoCuotasSimulacion | null = null;

  cajasRecaudo: PagoCuotasCajaRecaudo[] = [];
  valorEfectivo = 0;
  cuentasAhorroDisponibles: PagoCuotasCuentaAhorro[] = [];
  abonosAhorros: {idCuentaAhorro: number | null; valor: number}[] = [];
  valorCxp = 0;
  cargandoAhorros = false;

  idCajaRecaudo: number | null = null;
  operacionPreparada: PagoCuotasOperacionPreparada | null = null;
  comprobantePrevio: PagoCuotasComprobantePrevio | null = null;
  private solicitudRevisada: string | null = null;
  cargandoCajas = false;
  preparandoOperacion = false;
  enviandoCaja = false;
  ejecutandoAhorros = false;
  idPagoPendiente: number | null = null;

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
      this.cargandoLiquidacion || this.cargandoSimulacion || this.cargandoPagos ||
      this.cargandoCajas || this.cargandoAhorros || this.preparandoOperacion || this.enviandoCaja || this.ejecutandoAhorros;
  }

  get puedeLiquidar(): boolean {
    return !!this.creditoSeleccionado && !!this.fechaPago &&
      Number.isInteger(this.periodoInteresMeses) && this.periodoInteresMeses > 0 &&
      (this.pagoTotal || this.cuotasACancelar == null ||
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

    this.idPagoPendiente = null;
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
    this.idPagoPendiente = null;
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
          this.consultarCajasRecaudo(detalle.idAgencia);
          this.cargarCuentasAhorro();
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
    this.liquidar();
  }

  /** Invalida resultados cuando cambian los parámetros de liquidación. */
  invalidarCalculo(): void {
    this.liquidacion = null;
    this.simulacion = null;
    this.operacionPreparada = null;
    this.comprobantePrevio = null;
    this.solicitudRevisada = null;
    this.valorEfectivo = 0;
    this.valorCxp = 0;
    this.abonosAhorros = [];
    this.valorPago = null;
    this.error = null;
  }

  activarPagoTotal(): void {
    if (this.ocupado || !this.creditoSeleccionado || this.pagoTotal) return;
    this.pagoTotal = true;
    this.cuotasACancelar = null;
    this.invalidarCalculo();
    this.liquidar();
  }

  volverACuotas(): void {
    if (this.ocupado || !this.pagoTotal) return;
    this.pagoTotal = false;
    this.cuotasACancelar = null;
    this.invalidarCalculo();
    this.liquidar();
  }

  /** Recalcula con la cantidad elegida al confirmar el cambio del campo. */
  recalcularCuotas(): void {
    this.invalidarCalculo();
    if (this.puedeLiquidar) {
      this.liquidar();
    }
  }

  /** Presentación colombiana; el valor enviado a la API sigue siendo numérico. */
  get valorPagoFormateado(): string {
    return this.valorPago == null
      ? ''
      : new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 }).format(this.valorPago);
  }

  cambiarValorPago(texto: string): void {
    const digitos = String(texto ?? '').replace(/\D/g, '');
    const numero = digitos ? Number(digitos) : null;
    this.valorPago = numero != null && Number.isSafeInteger(numero) ? numero : null;
    this.invalidarSimulacion();
  }

  /** Ejecutar cuando el funcionario cambie el valor recibido. */
  invalidarSimulacion(): void {
    this.simulacion = null;
    this.operacionPreparada = null;
    this.comprobantePrevio = null;
    this.solicitudRevisada = null;
    this.error = null;
  }

  liquidar(): void {
    if (!this.puedeLiquidar || !this.idCarteraCreditoSeleccionado) return;
    this.invalidarCalculo();
    this.cargandoLiquidacion = true;

    const parametros = {
      idCarteraCredito: this.idCarteraCreditoSeleccionado,
      fechaPago: this.fechaPago,
      periodoInteresMeses: this.periodoInteresMeses,
      cuotasACancelar: this.cuotasACancelar
    };
    const consulta = this.pagoTotal ? this.api.liquidarPagoTotal(parametros) : this.api.liquidar(parametros);
    consulta.pipe(takeUntil(this.destruir$)).subscribe({
      next: liquidacion => {
        this.liquidacion = liquidacion;
        if (!this.pagoTotal) this.cuotasACancelar = liquidacion.cuotasConsideradas;
        this.valorPago = liquidacion.valorPagoSugerido;
        this.cargandoLiquidacion = false;
        this.simularPago();
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
    this.operacionPreparada = null;
    this.error = null;
    this.cargandoSimulacion = true;

    const parametros = {
      idCarteraCredito: this.idCarteraCreditoSeleccionado,
      fechaPago: this.fechaPago,
      periodoInteresMeses: this.periodoInteresMeses,
      cuotasACancelar: this.cuotasACancelar,
      valorPago: this.valorPago
    };
    const consulta = this.pagoTotal ? this.api.simularPagoTotal(parametros) : this.api.simularPago(parametros);
    consulta.pipe(takeUntil(this.destruir$)).subscribe({
      next: simulacion => {
        this.simulacion = simulacion;
        this.valorEfectivo = Number(simulacion.valorPago);
        this.abonosAhorros = [];
        this.valorCxp = 0;
        this.sugerirCuentaPorPagar();
        this.cargandoSimulacion = false;
      },
      error: error => {
        this.cargandoSimulacion = false;
        this.error = this.mensajeError(error, 'No fue posible simular el pago.');
      }
    });
  }

  private consultarCajasRecaudo(idAgencia: number): void {
    this.cajasRecaudo = [];
    this.idCajaRecaudo = null;
    this.cargandoCajas = true;
    this.api.listarCajasRecaudo(idAgencia).pipe(takeUntil(this.destruir$)).subscribe({
      next: cajas => {
        this.cajasRecaudo = Array.isArray(cajas) ? cajas : [];

        this.cargandoCajas = false;
      },
      error: error => {
        this.cargandoCajas = false;
        this.error = this.mensajeError(error, 'No fue posible consultar las cajas de recaudo.');
      }
    });
  }

  /** Al elegir Caja, sugiere el saldo pendiente y exige un importe positivo. */
  cambiarCajaRecaudo(): void {
    if (this.idCajaRecaudo == null) {
      this.valorEfectivo = 0;
    } else if (this.valorEfectivo <= 0) {
      this.valorEfectivo = Math.max(0, Number(this.simulacion?.valorPago ?? 0) - this.totalAhorros);
    }
    this.sugerirCuentaPorPagar();
    this.cambiarDistribucion();
  }

  get totalAhorros(): number {
    return this.abonosAhorros.reduce((s, x) => s + Number(x.valor || 0), 0);
  }

  get totalDistribuido(): number {
    return Number(this.valorEfectivo || 0) + this.totalAhorros + Number(this.valorCxp || 0);
  }

  get saldoPorDistribuir(): number {
    return (this.simulacion?.valorPago ?? 0) - this.totalDistribuido;
  }

  /** Valida distribución y cuentas antes de solicitar la vista previa. */
  get distribucionValida(): boolean {
    if (!this.simulacion || this.simulacion.valorPago <= 0) return false;
    if (!Number.isSafeInteger(this.valorEfectivo) || this.valorEfectivo < 0 ||
        !Number.isSafeInteger(this.valorCxp) || this.valorCxp < 0 ||
        this.totalDistribuido !== this.simulacion.valorPago) return false;
    if ((this.idCajaRecaudo != null && this.valorEfectivo <= 0) ||
        (this.valorEfectivo > 0 && this.idCajaRecaudo == null)) return false;
    if (this.valorCxp > 0 && (!Number.isSafeInteger(this.idCarteraCreditoSeleccionado) ||
        (this.idCarteraCreditoSeleccionado ?? 0) <= 0)) return false;
    const ids = new Set<number>();
    for (const abono of this.abonosAhorros) {
      if (abono.idCuentaAhorro == null || ids.has(abono.idCuentaAhorro) ||
          !Number.isSafeInteger(abono.valor) || abono.valor <= 0) return false;
      const cuenta = this.cuentasAhorroDisponibles.find(c => c.idCuentaAhorro === abono.idCuentaAhorro);
      if (!cuenta || abono.valor > Number(cuenta.saldoActual)) return false;
      ids.add(abono.idCuentaAhorro);
    }
    return this.totalDistribuido > 0;
  }

  get mediosCapturados(): PagoCuotasMedioRecaudo[] {
    if (!this.distribucionValida) return [];
    const medios: PagoCuotasMedioRecaudo[] = [];
    if (this.valorEfectivo > 0) medios.push({
      tipoMedio: 'CAJA', valor: this.valorEfectivo, idCaja: this.idCajaRecaudo,
      idCuentaAhorro: null, idCuentaPorPagar: null
    });
    for (const a of this.abonosAhorros) medios.push({
      tipoMedio: 'AHORROS', valor: a.valor, idCaja: null,
      idCuentaAhorro: a.idCuentaAhorro, idCuentaPorPagar: null
    });
    if (this.valorCxp > 0) medios.push({
      tipoMedio: 'CXP', valor: this.valorCxp, idCaja: null,
      idCuentaAhorro: null, idCuentaPorPagar: this.idCarteraCreditoSeleccionado
    });
    return medios;
  }

  /** Asigna a CxC el faltante; el ID real del crédito identifica el origen del recaudo. */
  private sugerirCuentaPorPagar(): void {
    const faltante = Number(this.simulacion?.valorPago ?? 0) - Number(this.valorEfectivo || 0) - this.totalAhorros;
    this.valorCxp = Math.max(0, faltante);
  }

  /** Invalida el comprobante revisado cuando cambia cualquier medio de pago. */
  cambiarDistribucion(): void {
    this.operacionPreparada = null;
    this.comprobantePrevio = null;
    this.solicitudRevisada = null;
    this.aviso = null;
  }

  /** Reajusta el ahorro sugerido al cambiar Caja y conserva la CxC como saldo residual. */
  cambiarEfectivo(): void {
    const total = Number(this.simulacion?.valorPago ?? 0);
    const efectivo = Number(this.valorEfectivo || 0);
    // La última cuenta seleccionada absorbe el cambio de Caja; las anteriores
    // conservan los importes capturados por el usuario.
    const indice = (() => {
      for (let i = this.abonosAhorros.length - 1; i >= 0; i--) {
        if (this.abonosAhorros[i].idCuentaAhorro != null) return i;
      }
      return -1;
    })();
    if (indice >= 0) {
      const abono = this.abonosAhorros[indice];
      const cuenta = this.cuentasAhorroDisponibles.find(c => c.idCuentaAhorro === abono.idCuentaAhorro);
      const otrosAhorros = this.abonosAhorros.reduce(
        (suma, actual, i) => suma + (i === indice ? 0 : Number(actual.valor || 0)), 0);
      const pendiente = Math.max(0, total - efectivo - otrosAhorros);
      abono.valor = Math.min(pendiente, Math.max(0, Number(cuenta?.saldoActual ?? 0)));
    }
    this.sugerirCuentaPorPagar();
    this.cambiarDistribucion();
  }

  cambiarCuentaAhorro(index: number): void {
    const abono = this.abonosAhorros[index];
    if (abono) {
      const cuenta = this.cuentasAhorroDisponibles.find(c => c.idCuentaAhorro === abono.idCuentaAhorro);
      const asignadoOtras = this.abonosAhorros.reduce((s, a, i) => s + (i === index ? 0 : Number(a.valor || 0)), 0);
      // El efectivo inicial es una propuesta: al elegir AHORROS, cede el importe sugerido.
      const efectivoRecuperado = Number(this.valorEfectivo || 0) + Number(abono.valor || 0);
      const disponiblePago = Math.max(0, Number(this.simulacion?.valorPago ?? 0) - asignadoOtras);
      const sugerido = cuenta ? Math.min(disponiblePago, Math.max(0, Number(cuenta.saldoActual || 0))) : 0;
      abono.valor = sugerido;
      this.valorEfectivo = Math.max(0, efectivoRecuperado - sugerido);
    }
    this.sugerirCuentaPorPagar();
    this.cambiarDistribucion();
  }

  cambiarValorAhorro(): void {
    this.sugerirCuentaPorPagar();
    this.cambiarDistribucion();
  }

  /** Cuentas no utilizadas por otra fila del mismo pago. */
  cuentasDisponiblesParaFila(index: number): PagoCuotasCuentaAhorro[] {
    const seleccionadas = new Set(this.abonosAhorros
      .filter((_, i) => i !== index)
      .map(x => x.idCuentaAhorro)
      .filter((id): id is number => id != null));
    return this.cuentasAhorroDisponibles.filter(c => !seleccionadas.has(c.idCuentaAhorro));
  }

  get puedeAgregarCuentaAhorro(): boolean {
    return !!this.simulacion && !this.ocupado &&
      this.abonosAhorros.length < this.cuentasAhorroDisponibles.length;
  }

  agregarCuentaAhorro(): void {
    if (!this.puedeAgregarCuentaAhorro) return;
    this.abonosAhorros.push({idCuentaAhorro:null, valor:0});
    this.sugerirCuentaPorPagar();
    this.cambiarDistribucion();
  }

  quitarCuentaAhorro(index: number): void {
    this.abonosAhorros.splice(index, 1);
    this.sugerirCuentaPorPagar();
    this.cambiarDistribucion();
  }

  get excedeTotalRecaudo(): boolean {
    return Number(this.valorEfectivo || 0) + this.totalAhorros > Number(this.simulacion?.valorPago ?? 0);
  }

  /** Consulta las cuentas permitidas por Cartera, sin usar vistas genéricas. */
  private cargarCuentasAhorro(): void {
    this.cuentasAhorroDisponibles = [];
    const idCredito = this.idCarteraCreditoSeleccionado;
    if (!idCredito) return;
    this.cargandoAhorros = true;
    this.api.listarCuentasAhorroRecaudo(idCredito)
      .pipe(takeUntil(this.destruir$)).subscribe({
        next: cuentas => {
          if (this.idCarteraCreditoSeleccionado !== idCredito) return;
          this.cuentasAhorroDisponibles = (Array.isArray(cuentas) ? cuentas : [])
            .filter(c => Number(c.saldoActual) > 0);
          this.cargandoAhorros = false;
        },
        error: error => {
          this.cargandoAhorros = false;
          this.error = this.mensajeError(error, 'No fue posible consultar las cuentas de ahorro.');
        }
      });
  }

  get puedePrepararOperacion(): boolean {
    return !!this.simulacion && !!this.creditoSeleccionado &&
      this.idPagoPendiente == null &&
      this.simulacion.valorPago > 0 && this.valorPago === this.simulacion.valorPago &&
      this.distribucionValida && !this.ocupado;
  }

  /** Construye la solicitud desde los valores capturados actualmente. */
  private solicitudActual() {
    if (!this.creditoSeleccionado || !this.simulacion) return null;
    return {
      idCarteraCredito: this.creditoSeleccionado.idCarteraCredito,
      fechaPago: this.fechaPago,
      periodoInteresMeses: this.periodoInteresMeses,
      cuotasACancelar: this.pagoTotal ? null : this.cuotasACancelar,
      pagoTotal: this.pagoTotal,
      valorPago: this.simulacion.valorPago,
      idAgenciaReceptora: this.creditoSeleccionado.idAgencia,
      medios: this.mediosCapturados
    };
  }

  /** Consulta el comprobante previo para la modalidad seleccionada. */
  prepararYEnviarCaja(): void {
    if (!this.puedePrepararOperacion) return;
    const solicitud = this.solicitudActual();
    if (!solicitud) return;
    this.error = null;
    this.aviso = null;
    this.comprobantePrevio = null;
    this.solicitudRevisada = null;
    this.preparandoOperacion = true;
    this.api.comprobantePrevio(solicitud).pipe(takeUntil(this.destruir$)).subscribe({
      next: comprobante => {
        this.preparandoOperacion = false;
        if (JSON.stringify(this.solicitudActual()) !== JSON.stringify(solicitud)) {
          this.error = 'Los valores del pago cambiaron. Consulte nuevamente el comprobante.';
          return;
        }
        this.comprobantePrevio = comprobante;
        this.solicitudRevisada = JSON.stringify(solicitud);
      },
      error: error => {
        this.preparandoOperacion = false;
        this.error = this.mensajeError(error, 'No fue posible construir el comprobante contable.');
      }
    });
  }

  get puedeEnviarPagoRevisado(): boolean {
    return this.comprobantePrevio != null && this.solicitudRevisada != null &&
      this.solicitudRevisada === JSON.stringify(this.solicitudActual()) &&
      this.puedePrepararOperacion &&
      Number(this.comprobantePrevio.totalDebitos) === Number(this.comprobantePrevio.totalCreditos) &&
      Number(this.comprobantePrevio.totalDebitos) === Number(this.simulacion?.valorPago);
  }

  /** Ejecuta el medio seleccionado únicamente tras revisar el comprobante. */
  enviarPagoRevisado(): void {
    if (!this.puedeEnviarPagoRevisado) return;
    const solicitud = this.solicitudActual();
    if (!solicitud) return;
    this.enviandoCaja = this.valorEfectivo > 0;
    this.ejecutandoAhorros = this.valorEfectivo === 0;
    this.error = null;
    const incluyeCaja = this.valorEfectivo > 0;
    const ejecucion = incluyeCaja
      ? this.api.enviarPagoCaja(solicitud)
      : this.api.ejecutarPagoAhorros(solicitud);
    ejecucion.pipe(takeUntil(this.destruir$)).subscribe({
      next: idPago => {
        this.enviandoCaja = false;
        this.ejecutandoAhorros = false;
        const asociado = this.asociadoSeleccionado;
        this.limpiarCredito();
        this.idPagoPendiente = null;
        this.comprobantePrevio = null;
        this.solicitudRevisada = null;
        if (asociado) this.seleccionarAsociado(asociado);
        this.aviso = incluyeCaja
          ? `Pago N.º ${idPago} aplicado en Cartera y enviado a Caja para confirmación física.`
          : `Pago N.º ${idPago} aplicado en Cartera. Verifique el comprobante y los extractos.`;
      },
      error: error => {
        this.enviandoCaja = false;
        this.ejecutandoAhorros = false;
        this.error = this.mensajeError(error, 'No fue posible ejecutar el pago.');
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
    this.pagoTotal = false;
    this.liquidacion = null;
    this.simulacion = null;
    this.operacionPreparada = null;
    this.comprobantePrevio = null;
    this.solicitudRevisada = null;
    this.valorEfectivo = 0;
    this.valorCxp = 0;
    this.abonosAhorros = [];
    this.cuentasAhorroDisponibles = [];
    this.valorPago = null;
    this.cajasRecaudo = [];
    this.idCajaRecaudo = null;
    this.operacionPreparada = null;
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
