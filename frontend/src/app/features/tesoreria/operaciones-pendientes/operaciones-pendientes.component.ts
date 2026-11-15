import {
  Component,
  DestroyRef,
  OnInit,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { forkJoin, finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { SessionService } from '../../../core/auth/session.service';

import {
  OperacionesPendientesApi,
  OperacionPagoRequest,
  OperacionComprobantePreview,
  OperacionPagoResponse
} from './operaciones-pendientes.api';
import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';

import {
  OperacionPendiente,
  OperacionEjecucion,
  LineaDistribucionPago,
  MedioPagoOperacion,
  ResumenDistribucionPago,
  CuentaBancariaParaPago
} from './operaciones-pendientes.models';

@Component({
  selector: 'app-operaciones-pendientes',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NumericFormatDirective
  ],
  templateUrl: './operaciones-pendientes.component.html',
  styleUrl: './operaciones-pendientes.component.scss'
})
export class OperacionesPendientesComponent implements OnInit {

  private readonly api = inject(OperacionesPendientesApi);
  private readonly session = inject(SessionService);
  private readonly destroyRef = inject(DestroyRef);

  // Permiso independiente para ejecutar operaciones financieras.
  get puedeEjecutarOperaciones(): boolean {
    return this.session.tienePermiso('TESORERIA_OPERACIONES_PENDIENTES_EXECUTE');
  }

  // =====================================================
  // ESTADO GENERAL
  // =====================================================

  idAgencia: number | null = null;

  cargando = false;
  cargandoDetalle = false;

  mensajeError = '';
  mensajeInformativo = '';

  operaciones: OperacionPendiente[] = [];

  operacionSeleccionada: OperacionPendiente | null = null;

  ejecuciones: OperacionEjecucion[] = [];

  saldoDisponible = 0;

  cargandoVistaPrevia = false;
  confirmandoPago = false;
  vistaPrevia: OperacionComprobantePreview | null = null;
  resultadoPago: OperacionPagoResponse | null = null;
  private versionVistaPrevia = 0;

  // =====================================================
  // CUENTAS BANCARIAS PARA PAGOS
  // =====================================================

  cuentasParaPago: CuentaBancariaParaPago[] = [];

  cargandoCuentas = false;
  errorCuentas = '';

  // =====================================================
  // DISTRIBUCIÓN DE PAGOS
  // =====================================================

  lineasPago: LineaDistribucionPago[] = [];

  private siguienteIdTemporal = 1;
  private versionDetalle = 0;
  private readonly observacionesSugeridas = new Map<number, string>();

  readonly mediosPago: {
    valor: MedioPagoOperacion;
    etiqueta: string;
  }[] = [
    {
      valor: 'TRANSFERENCIA',
      etiqueta: 'Transferencia bancaria'
    },
    {
      valor: 'CHEQUE',
      etiqueta: 'Cheque'
    }
  ];

  // Devolución bancaria: el origen se fija por la operación original.
  get esDevolucionCartera(): boolean {
    return this.operacionSeleccionada?.modulo_origen === 'CARTERA'
      && ['REVERSA_PAGO_CUOTAS', 'REVERSA_PRORROGA'].includes(
        this.operacionSeleccionada?.proceso_origen ?? ''
      );
  }

  private cuentaOriginalDevolucion(): number | null {
    if (!this.esDevolucionCartera) return null;
    const concepto = this.operacionSeleccionada?.concepto ?? '';
    const coincidencia = /cuenta bancaria original\s+(\d+)/i.exec(concepto);
    const cuenta = coincidencia ? Number(coincidencia[1]) : NaN;
    return Number.isSafeInteger(cuenta) && cuenta > 0 ? cuenta : null;
  }

  /** Bancos e importes originales publicados por Cartera para la reversión de prórroga. */
  private bancosOriginalesProrroga(): Map<number, number> | null {
    if (this.operacionSeleccionada?.proceso_origen !== 'REVERSA_PRORROGA') {
      return null;
    }

    const concepto = this.operacionSeleccionada.concepto ?? '';
    const coincidencia = /Bancos e importes originales:\s*\{([^}]*)\}/i.exec(concepto);
    if (!coincidencia) return null;

    const importes = new Map<number, number>();
    for (const elemento of coincidencia[1].split(',')) {
      const partes = /^\s*(\d+)\s*=\s*(\d+(?:\.\d{1,2})?)\s*$/.exec(elemento);
      if (!partes) return null;
      const cuenta = Number(partes[1]);
      const valor = Number(partes[2]);
      if (!Number.isSafeInteger(cuenta) || cuenta <= 0 ||
          !Number.isFinite(valor) || valor <= 0 || importes.has(cuenta)) {
        return null;
      }
      importes.set(cuenta, Math.round(valor * 100));
    }
    return importes.size > 0 ? importes : null;
  }

  /** Prepara únicamente las cuentas originales; las referencias son editables. */
  private prepararDevolucion(): void {
    if (!this.esDevolucionCartera || this.cargandoDetalle || this.cargandoCuentas) return;
    if (this.saldoDisponible <= 0) return;

    let distribucion: Map<number, number>;
    if (this.operacionSeleccionada?.proceso_origen === 'REVERSA_PRORROGA') {
      const bancos = this.bancosOriginalesProrroga();
      if (!bancos) {
        this.mensajeError = 'La devolución no identifica los bancos e importes originales.';
        return;
      }
      distribucion = bancos;
    } else {
      const cuentaOriginal = this.cuentaOriginalDevolucion();
      if (!cuentaOriginal) {
        this.mensajeError = 'La devolución no identifica la cuenta bancaria original.';
        return;
      }
      distribucion = new Map([[cuentaOriginal, Math.round(this.saldoDisponible * 100)]]);
    }

    const totalCentavos = [...distribucion.values()].reduce((total, valor) => total + valor, 0);
    if (totalCentavos !== Math.round(this.saldoDisponible * 100)) {
      this.mensajeError = 'Los importes originales no coinciden con el saldo de devolución.';
      return;
    }
    if ([...distribucion.keys()].some(id =>
      !this.cuentasParaPago.some(c => Number(c.id_cuenta_bancaria) === id))) {
      this.mensajeError = 'Una cuenta bancaria original no está habilitada para egresos en esta agencia.';
      return;
    }

    this.lineasPago = [...distribucion.entries()].map(([idCuenta, centavos]) => ({
      idTemporal: this.siguienteIdTemporal++,
      medioPago: 'TRANSFERENCIA' as MedioPagoOperacion,
      idBeneficiario: this.operacionSeleccionada?.id_datos_personal ?? null,
      idCuentaBancaria: idCuenta,
      numeroDocumento: '',
      valor: centavos / 100,
      observaciones: ''
    }));
    this.invalidarVistaPrevia();
    this.lineasPago.forEach(linea => this.sugerirObservacion(linea));
    this.mensajeError = '';
  }

  // =====================================================
  // INICIALIZACIÓN
  // =====================================================

  ngOnInit(): void {

    const agenciaActiva = this.session.getAgenciaActiva();

    this.idAgencia = agenciaActiva?.idAgencia ?? null;

    if (!this.idAgencia) {
      this.mensajeError =
        'No se encontró una agencia activa para el usuario.';
      return;
    }

    this.consultarOperaciones();

  }

  // =====================================================
  // CONSULTAR OBLIGACIONES
  // =====================================================

  consultarOperaciones(): void {

    if (!this.idAgencia || this.cargando) {
      return;
    }

    const agenciaConsultada = this.idAgencia;

    this.cargando = true;
    this.mensajeError = '';
    this.mensajeInformativo = '';

    this.api.listarPendientes(agenciaConsultada)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({
        next: operaciones => {

          if (this.idAgencia !== agenciaConsultada) {
            return;
          }

          this.operaciones = operaciones ?? [];

          if (this.operacionSeleccionada) {

            const actualizada = this.operaciones.find(
              operacion =>
                operacion.id_operacion_pendiente ===
                this.operacionSeleccionada?.id_operacion_pendiente
            );

            if (actualizada) {
              this.operacionSeleccionada = actualizada;
            } else {
              this.limpiarSeleccion();
            }
          }
        },
        error: error => {
          console.error(
            'Error consultando operaciones pendientes',
            error
          );

          this.mensajeError =
            'No fue posible consultar las obligaciones pendientes.';
        }
      });
  }

  // =====================================================
  // CONSULTAR CUENTAS BANCARIAS HABILITADAS PARA PAGAR
  // =====================================================

  consultarCuentasParaPago(): void {

    if (!this.idAgencia || this.cargandoCuentas) {
      return;
    }

    const agenciaConsultada = this.idAgencia;

    this.cargandoCuentas = true;
    this.errorCuentas = '';

    this.api.listarCuentasParaPago(agenciaConsultada)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.cargandoCuentas = false;
        })
      )
      .subscribe({
        next: cuentas => {

          if (this.idAgencia !== agenciaConsultada) {
            return;
          }

          this.cuentasParaPago = cuentas ?? [];

        },
        error: error => {

          console.error(
            'Error consultando cuentas bancarias para pagos',
            error
          );

          this.cuentasParaPago = [];

          this.errorCuentas =
            'No fue posible consultar las cuentas bancarias habilitadas para pagos.';
        }
      });
  }

  // =====================================================
  // SELECCIONAR OBLIGACIÓN
  // =====================================================

  /** Consulta cuentas según naturaleza y evita ofrecer cuentas de egreso en recaudos. */
  public consultarCuentasParaOperacion(): void {
    if (!this.idAgencia || !this.operacionSeleccionada) return;
    const idAgencia = this.idAgencia;
    this.cargandoCuentas = true;
    this.errorCuentas = '';
    this.cuentasParaPago = [];
    const consulta = this.operacionSeleccionada.naturaleza === 'ENTRADA'
      ? this.api.listarCuentasParaRecaudo(idAgencia)
      : this.api.listarCuentasParaPago(idAgencia);
    consulta.pipe(takeUntilDestroyed(this.destroyRef), finalize(() => {
      this.cargandoCuentas = false;
    })).subscribe({
      next: cuentas => {
        if (this.idAgencia !== idAgencia || !this.operacionSeleccionada) return;
        this.cuentasParaPago = cuentas ?? [];
        this.cargandoCuentas = false;
        this.prepararDevolucion();
      },
      error: error => {
        this.errorCuentas = error?.error?.message ?? 'No fue posible consultar las cuentas bancarias.';
      }
    });
  }

  seleccionarOperacion(
    operacion: OperacionPendiente
  ): void {

    this.invalidarVistaPrevia();
    this.versionDetalle++;
    const versionActual = this.versionDetalle;

    this.operacionSeleccionada = operacion;
    this.resultadoPago = null;
    // Carga cuentas aptas para ingresos, incluso si no están habilitadas para pagos.
    this.consultarCuentasParaOperacion();

    this.ejecuciones = [];
    this.lineasPago = [];
    this.observacionesSugeridas.clear();
    this.saldoDisponible = 0;

    this.cargandoDetalle = true;
    this.mensajeError = '';
    this.mensajeInformativo = '';

    const idOperacion = operacion.id_operacion_pendiente;

    forkJoin({
      ejecuciones: this.api.listarEjecuciones(idOperacion),
      saldo: this.api.consultarSaldo(idOperacion)
    })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          if (versionActual === this.versionDetalle) {
            this.cargandoDetalle = false;
          }
        })
      )
      .subscribe({
        next: respuesta => {

          if (versionActual !== this.versionDetalle) {
            return;
          }

          this.ejecuciones = respuesta.ejecuciones ?? [];

          this.saldoDisponible =
            respuesta.saldo.saldoDisponible ?? 0;
          this.cargandoDetalle = false;
          this.prepararDevolucion();
        },
        error: error => {

          if (versionActual !== this.versionDetalle) {
            return;
          }

          console.error(
            'Error consultando detalle de obligación',
            error
          );

          this.mensajeError =
            'No fue posible consultar el saldo y las ejecuciones.';
        }
      });
  }

  // =====================================================
  // LIMPIAR SELECCIÓN
  // =====================================================

  limpiarSeleccion(): void {

    this.invalidarVistaPrevia();
    this.versionDetalle++;

    this.operacionSeleccionada = null;
    this.ejecuciones = [];
    this.lineasPago = [];
    this.observacionesSugeridas.clear();

    this.saldoDisponible = 0;
    this.cargandoDetalle = false;

    this.mensajeError = '';
    this.mensajeInformativo = '';
  }

  /** Cierra el resumen de contabilización a petición del funcionario. */
  cerrarResultadoPago(): void {
    this.resultadoPago = null;
  }

  // =====================================================
  // AGREGAR LÍNEA DE PAGO
  // =====================================================

  agregarLineaPago(): void {

    if (
      this.esDevolucionCartera ||
      !this.operacionSeleccionada ||
      this.cargandoDetalle ||
      !['SALIDA', 'ENTRADA'].includes(this.operacionSeleccionada.naturaleza)
    ) {
      return;
    }

    const linea: LineaDistribucionPago = {
      idTemporal: this.siguienteIdTemporal++,

      medioPago: 'TRANSFERENCIA',

      idBeneficiario:
        this.operacionSeleccionada.id_datos_personal,

      idCuentaBancaria: null,

      numeroDocumento: '',

      valor: Math.max(0, this.saldoSinDistribuir),

      observaciones: ''
    };

    this.invalidarVistaPrevia();
    this.lineasPago.push(linea);
    this.sugerirObservacion(linea);

    this.mensajeError = '';
    this.mensajeInformativo = '';
  }

  // =====================================================
  // ELIMINAR LÍNEA
  // =====================================================

  eliminarLineaPago(
    idTemporal: number
  ): void {

    if (this.esDevolucionCartera) return;

    this.invalidarVistaPrevia();
    this.observacionesSugeridas.delete(idTemporal);
    this.lineasPago = this.lineasPago.filter(
      linea => linea.idTemporal !== idTemporal
    );
  }

  // =====================================================
  // CAMBIAR MEDIO DE PAGO
  // =====================================================

  cambiarMedioPago(
    linea: LineaDistribucionPago,
    medioPago: MedioPagoOperacion
  ): void {

    if (this.esDevolucionCartera) return;
    this.invalidarVistaPrevia();
    linea.medioPago = medioPago;

    linea.idCuentaBancaria = null;
    linea.numeroDocumento = '';
  }


  // =====================================================
  // SUGERIR OBSERVACIÓN SEGÚN EL PAGO
  // =====================================================


  // =====================================================
  // SUGERIR OBSERVACIÓN SEGÚN EL PAGO
  // =====================================================

  sugerirObservacion(
    linea: LineaDistribucionPago
  ): void {

    const operacion = this.operacionSeleccionada;

    if (!operacion) {
      return;
    }

    const observacionAnterior =
      this.observacionesSugeridas.get(linea.idTemporal);

    const observacionActual =
      linea.observaciones ?? '';

    // No sobrescribir observaciones personalizadas.
    if (
      observacionAnterior !== undefined &&
      observacionActual !== observacionAnterior
    ) {
      return;
    }

    const cuenta = this.cuentasParaPago.find(
      c => c.id_cuenta_bancaria === Number(linea.idCuentaBancaria)
    );

    const banco = cuenta?.nombre_banco?.trim() || '';

    const referencia = (linea.numeroDocumento || '').trim();

    const origen = operacion.modulo_origen || 'ERP';

    let observacion = this.esDevolucionCartera
      ? 'Devolución de recaudo bancario CARTERA'
      : operacion.naturaleza === 'ENTRADA'
      ? `Recaudo de cuenta por cobrar ${origen}`
      : `Pago de obligación ${origen}`;

    if (linea.medioPago === 'CHEQUE') {

      observacion += ' mediante cheque';

      if (referencia) {
        observacion += ` N.º ${referencia}`;
      }

    } else {

      observacion += ' mediante transferencia bancaria';

      if (referencia) {
        observacion += ` - Ref. ${referencia}`;
      }
    }

    if (banco) {
      observacion += ` - ${banco}`;
    }

    linea.observaciones = observacion;

    this.observacionesSugeridas.set(
      linea.idTemporal,
      observacion
    );
  }

  // =====================================================
  // VALORES Y TOTALES
  // =====================================================

  get totalPendiente(): number {

    return this.operaciones.reduce(
      (total, operacion) =>
        total + Number(operacion.saldo_pendiente || 0),
      0
    );
  }

  get totalEjecutado(): number {

    return this.operaciones.reduce(
      (total, operacion) =>
        total + Number(operacion.valor_ejecutado || 0),
      0
    );
  }

  get totalDistribuido(): number {

    return this.lineasPago.reduce(
      (total, linea) =>
        total + Number(linea.valor || 0),
      0
    );
  }

  get saldoSinDistribuir(): number {

    return this.saldoDisponible - this.totalDistribuido;
  }

  get diferenciaDistribucion(): number {
    return this.totalDistribuido - this.saldoDisponible;
  }

  get resumenDistribucion(): ResumenDistribucionPago {

    const operacion = this.operacionSeleccionada;

    const total = this.totalDistribuido;

    const valoresValidos = this.lineasPago.every(
      linea =>
        Number.isFinite(Number(linea.valor)) &&
        Number(linea.valor) > 0
    );

    const distribucionValida =
      !!operacion &&
      operacion.naturaleza === 'SALIDA' &&
      !this.cargandoDetalle &&
      this.lineasPago.length > 0 &&
      valoresValidos &&
      total > 0 &&
      total <= this.saldoDisponible;

    return {
      valorObligacion: Number(
        operacion?.valor_original ?? 0
      ),

      valorEjecutado: Number(
        operacion?.valor_ejecutado ?? 0
      ),

      saldoDisponible: this.saldoDisponible,

      totalDistribuido: total,

      saldoSinDistribuir:
        this.saldoDisponible - total,

      cantidadLineas: this.lineasPago.length,

      distribucionValida
    };
  }

  // =====================================================
  // VALIDACIÓN LOCAL DE LA DISTRIBUCIÓN
  // =====================================================

  validarDistribucion(): boolean {

    this.mensajeError = '';
    this.mensajeInformativo = '';

    if (!this.operacionSeleccionada) {

      this.mensajeError =
        'Seleccione una obligación pendiente.';

      return false;
    }

    if (this.cargandoDetalle) {

      this.mensajeError =
        'Espere a que termine la consulta del saldo.';

      return false;
    }

    if (!this.lineasPago.length) {

      this.mensajeError =
        'Agregue al menos una línea de pago.';

      return false;
    }

    if (this.esDevolucionCartera) {
      const esProrroga = this.operacionSeleccionada?.proceso_origen === 'REVERSA_PRORROGA';
      const originales = esProrroga
        ? this.bancosOriginalesProrroga()
        : (() => {
            const cuenta = this.cuentaOriginalDevolucion();
            return cuenta ? new Map([[cuenta, Math.round(this.saldoDisponible * 100)]]) : null;
          })();

      if (!originales || this.lineasPago.length !== originales.size ||
          Math.round(this.totalDistribuido * 100) !== Math.round(this.saldoDisponible * 100)) {
        this.mensajeError = 'La devolución debe cubrir exactamente los bancos e importes originales.';
        return false;
      }
      const cuentasVistas = new Set<number>();
      for (const linea of this.lineasPago) {
        const cuenta = Number(linea.idCuentaBancaria);
        if (cuentasVistas.has(cuenta) || !originales.has(cuenta) ||
            linea.medioPago !== 'TRANSFERENCIA' ||
            Math.round(Number(linea.valor) * 100) !== originales.get(cuenta)) {
          this.mensajeError = 'Cada transferencia debe corresponder al banco y valor originalmente recaudados.';
          return false;
        }
        cuentasVistas.add(cuenta);
        if (!linea.numeroDocumento.trim()) {
          this.mensajeError = 'Registre la referencia bancaria de cada devolución.';
          return false;
        }
      }
    }

    for (const linea of this.lineasPago) {

      if (
        !Number.isFinite(Number(linea.valor)) ||
        Number(linea.valor) <= 0
      ) {

        this.mensajeError =
          'Todas las líneas deben tener un valor mayor que cero.';

        return false;
      }

      if (!linea.idCuentaBancaria) {

        this.mensajeError =
          'Seleccione la cuenta bancaria de cada línea.';

        return false;
      }

      const cuentaValida = this.cuentasParaPago.some(
        cuenta =>
          cuenta.id_cuenta_bancaria ===
          Number(linea.idCuentaBancaria)
      );

      if (!cuentaValida) {

        this.mensajeError =
          'La cuenta bancaria seleccionada no está habilitada para esta operación.';

        return false;
      }

      if (
        linea.medioPago === 'CHEQUE' &&
        !linea.numeroDocumento.trim()
      ) {

        this.mensajeError =
          'Indique el número de cada cheque.';

        return false;
      }
    }

    if (this.totalDistribuido > this.saldoDisponible) {

      this.mensajeError =
        'La distribución supera el saldo disponible de la obligación.';

      return false;
    }


    return true;
  }

  // =====================================================
  // VISTA PREVIA Y CONFIRMACIÓN
  // =====================================================

  private invalidarVistaPrevia(): void {
    this.versionVistaPrevia++;
    this.vistaPrevia = null;
  }

  /** Llamar desde (ngModelChange) de cada campo editable. */
  distribucionModificada(): void {
    this.invalidarVistaPrevia();
  }

  private construirSolicitudPago(): OperacionPagoRequest {
    if (!this.operacionSeleccionada) {
      throw new Error('Seleccione una obligación.');
    }
    return {
      idOperacion: this.operacionSeleccionada.id_operacion_pendiente,
      lineas: this.lineasPago.map(linea => ({
        idBeneficiario: Number(linea.idBeneficiario),
        idCuentaBancaria: Number(linea.idCuentaBancaria),
        medioPago: linea.medioPago,
        numeroDocumento: linea.numeroDocumento.trim() || null,
        valor: Number(linea.valor),
        observaciones: linea.observaciones.trim() || null
      }))
    };
  }

  generarComprobante(): void {
    if (!this.puedeEjecutarOperaciones) return;
    if (this.cargandoVistaPrevia || this.confirmandoPago) return;
    if (!this.validarDistribucion()) return;
    this.invalidarVistaPrevia();
    const version = this.versionVistaPrevia;
    const solicitud = this.construirSolicitudPago();
    this.cargandoVistaPrevia = true;
    (this.operacionSeleccionada?.naturaleza === 'ENTRADA'
        ? this.api.generarVistaPreviaRecaudo(solicitud)
        : this.api.generarVistaPrevia(solicitud))
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => { this.cargandoVistaPrevia = false; })
      )
      .subscribe({
        next: preview => {
          if (version !== this.versionVistaPrevia) return;
          if (!preview.cuadrado) {
            this.mensajeError = 'El comprobante no está cuadrado.';
            return;
          }
          this.vistaPrevia = preview;
          this.mensajeInformativo =
            'Revise los movimientos contables antes de confirmar la operación.';
        },
        error: error => {
          if (version !== this.versionVistaPrevia) return;
          console.error('Error generando vista previa', error);
          this.mensajeError = error?.error?.message ??
            'No fue posible generar la vista previa contable.';
        }
      });
  }

  confirmarPago(): void {
    if (!this.puedeEjecutarOperaciones) return;
    if (this.confirmandoPago || this.cargandoVistaPrevia || !this.vistaPrevia) return;
    if (!this.validarDistribucion()) return;
    const solicitud = this.construirSolicitudPago();
    if (solicitud.idOperacion !== this.vistaPrevia.idOperacion ||
        this.totalDistribuido !== this.vistaPrevia.totalPago) {
      this.invalidarVistaPrevia();
      this.mensajeError = 'La distribución cambió. Genere nuevamente el comprobante.';
      return;
    }
    this.confirmandoPago = true;
    (this.operacionSeleccionada?.naturaleza === 'ENTRADA'
        ? this.api.confirmarRecaudo(solicitud)
        : this.api.confirmarPago(solicitud))
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => { this.confirmandoPago = false; })
      )
      .subscribe({
        next: resultado => {
          this.invalidarVistaPrevia();
          this.lineasPago = [];
          if (this.operacionSeleccionada && resultado.saldoPendiente > 0) {
            this.seleccionarOperacion(this.operacionSeleccionada);
          } else {
            this.limpiarSeleccion();
          }
          this.consultarOperaciones();
          this.resultadoPago = resultado;
          // El resumen permanece visible aunque la operación completada
          // desaparezca de la bandeja durante la actualización.
          this.mensajeInformativo = '';
        },
        error: error => {
          console.error('Error confirmando pago', error);
          this.mensajeError = error?.error?.message ??
            'No se pudo confirmar el pago. Consulte su estado antes de reintentar.';
          this.invalidarVistaPrevia();
        }
      });
  }

  // =====================================================
  // IDENTIFICACIÓN DE FILAS
  // =====================================================

  trackOperacion(
    index: number,
    operacion: OperacionPendiente
  ): number {

    return operacion.id_operacion_pendiente;
  }

  trackLinea(
    index: number,
    linea: LineaDistribucionPago
  ): number {

    return linea.idTemporal;
  }

  trackEjecucion(
    index: number,
    ejecucion: OperacionEjecucion
  ): number {

    return ejecucion.id_operacion_ejecucion;
  }

}
