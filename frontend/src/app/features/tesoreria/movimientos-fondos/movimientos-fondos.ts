
import {
  Component,
  DestroyRef,
  OnInit,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { MovimientosFondosApi } from './movimientos-fondos.api';

import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';


import {
  CajaConProvision,
  CajaMovimiento,
  ChequeDisponible,
  CuentaBancariaMovimiento,
  EfectivoCaja,
  ModalidadTrasladoCaja,
  MovimientoFondoPreview,
  MovimientoFondoRequest,
  TipoMovimientoFondo
} from './movimientos-fondos.models';

import {
  AgenciaSesion,
  SessionService
} from '../../../core/auth/session.service';


@Component({
  selector: 'app-movimientos-fondos',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NumericFormatDirective
  ],
  templateUrl: './movimientos-fondos.html',
  styleUrl: './movimientos-fondos.scss'
})
export class MovimientosFondosComponent implements OnInit {

  private readonly api = inject(MovimientosFondosApi);
  private readonly destroyRef = inject(DestroyRef);

  private readonly session = inject(SessionService);

  // =========================================================
  // ESTADO
  // =========================================================

  cargando = false;
  consultando = false;
  mensajeError = '';
  mensajeInformativo = '';


  preview: MovimientoFondoPreview | null = null;
  aplicando = false;
  movimientoAplicado = false;
  numeroComprobanteGenerado = '';
  private solicitudPrevisualizada = '';

  // =========================================================
  // CATÁLOGOS
  // =========================================================

  readonly tiposMovimiento: {
    valor: TipoMovimientoFondo;
    etiqueta: string;
    disponible: boolean;
  }[] = [
    {
      valor: 'CONSIGNACION',
      etiqueta: 'Consignación bancaria',
      disponible: true
    },
    {
      valor: 'RETIRO',
      etiqueta: 'Retiro bancario',
      disponible: false
    },
    {
      valor: 'TRASLADO_CAJAS',
      etiqueta: 'Movimiento entre cajas',
      disponible: true
    },
    {
      valor: 'TRASLADO_BANCARIO',
      etiqueta: 'Traslado bancario',
      disponible: false
    }
  ];

  readonly modalidadesTraslado: {
    valor: ModalidadTrasladoCaja;
    etiqueta: string;
  }[] = [
    {
      valor: 'PROVISION',
      etiqueta: 'Provisión a caja operativa'
    },
    {
      valor: 'REEMBOLSO',
      etiqueta: 'Reembolso a caja control'
    }
  ];

  cuentasBancarias: CuentaBancariaMovimiento[] = [];
  chequesDisponibles: ChequeDisponible[] = [];
  cajasMovimiento: CajaMovimiento[] = [];
  cajasConProvision: CajaConProvision[] = [];
  agencias: AgenciaSesion[] = [];

  // =========================================================
  // FORMULARIO
  // =========================================================

  idAgencia: number | null = null;

  tipoMovimiento: TipoMovimientoFondo = 'TRASLADO_CAJAS';
  modalidadTrasladoCaja: ModalidadTrasladoCaja = 'PROVISION';

  fechaMovimiento = this.fechaLocal();

  numeroSoporte = '';
  observacion = '';

  valor: number | null = null;

  idCuentaBancariaDestino: number | null = null;

  idCajaOrigen: number | null = null;
  idCajaDestino: number | null = null;

  idsChequesSeleccionados: number[] = [];

  efectivos: EfectivoCaja[] = [];

  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    this.agencias = this.session.getAgencias();

    const agenciaActiva = this.session.getAgenciaActiva();

    if (
      agenciaActiva?.idAgencia != null &&
      this.agencias.some(
        agencia => agencia.idAgencia === agenciaActiva.idAgencia
      )
    ) {
      this.idAgencia = agenciaActiva.idAgencia;
      this.cargarCatalogos();
    }
  }

  // =========================================================
  // PROPIEDADES DE PRESENTACIÓN
  // =========================================================

  get esConsignacion(): boolean {
    return this.tipoMovimiento === 'CONSIGNACION';
  }

  get esTrasladoCajas(): boolean {
    return this.tipoMovimiento === 'TRASLADO_CAJAS';
  }

  // La consulta /cajas devuelve exclusivamente provisiones ABIERTAS
  // para la agencia y fecha seleccionadas.
  provisionesConsultadas = false;
  consultandoProvisiones = false;

  cajaHabilitada(idCaja: number): boolean {
    return this.provisionesConsultadas && this.cajasConProvision.some(
      caja => caja.idCaja === idCaja
    );
  }

  get cajasSinProvisionAbierta(): CajaMovimiento[] {
    return this.cajasMovimiento.filter(caja => !this.cajaHabilitada(caja.idCaja));
  }

  get cajasControl(): CajaMovimiento[] {
    return this.cajasMovimiento.filter(
      caja => caja.tipoCaja?.toUpperCase() === 'CONTROL' && this.cajaHabilitada(caja.idCaja)
    );
  }

  get cajasOperativas(): CajaMovimiento[] {
    return this.cajasMovimiento.filter(
      caja => caja.tipoCaja?.toUpperCase() === 'OPERATIVA' && this.cajaHabilitada(caja.idCaja)
    );
  }

  get cajasOrigen(): CajaMovimiento[] {
    if (this.modalidadTrasladoCaja === 'PROVISION') {
      return this.cajasControl;
    }

    return this.cajasOperativas;
  }

  get cajasDestino(): CajaMovimiento[] {
    if (this.modalidadTrasladoCaja === 'PROVISION') {
      return this.cajasOperativas;
    }

    return this.cajasControl;
  }

  get valorChequesSeleccionados(): number {
    return this.chequesDisponibles
      .filter(cheque =>
        this.idsChequesSeleccionados.includes(
          cheque.idChequeRecibido
        )
      )
      .reduce(
        (total, cheque) => total + Number(cheque.valorCheque || 0),
        0
      );
  }

  get valorEfectivoConsignacion(): number {
    return this.efectivos.reduce(
      (total, item) => total + Number(item.valor || 0),
      0
    );
  }

  get valorTotalConsignacion(): number {
    return this.valorChequesSeleccionados +
      this.valorEfectivoConsignacion;
  }

  // =========================================================
  // CAMBIOS DEL FORMULARIO
  // =========================================================

  cambiarTipoMovimiento(): void {

    this.limpiarResultado();

    this.idCuentaBancariaDestino = null;
    this.idCajaOrigen = null;
    this.idCajaDestino = null;

    this.idsChequesSeleccionados = [];
    this.efectivos = [];
    this.valor = null;

    if (this.tipoMovimiento === 'TRASLADO_CAJAS') {
      this.modalidadTrasladoCaja = 'PROVISION';
    }

    if (this.idAgencia && !this.provisionesConsultadas) {
      this.cargarCajasConProvision();
    }
  }

  cambiarModalidadTraslado(): void {

    this.idCajaOrigen = null;
    this.idCajaDestino = null;

    this.limpiarResultado();
  }

  cambiarDatos(): void {
    this.limpiarResultado();
  }

  cambiarAgencia(): void {

    this.limpiarResultado();

    this.cuentasBancarias = [];
    this.chequesDisponibles = [];
    this.cajasMovimiento = [];
    this.cajasConProvision = [];
    this.provisionesConsultadas = false;

    this.idCuentaBancariaDestino = null;
    this.idCajaOrigen = null;
    this.idCajaDestino = null;

    this.idsChequesSeleccionados = [];
    this.efectivos = [];

    if (!this.idAgencia || this.idAgencia <= 0) {
      return;
    }

    this.cargarCatalogos();
  }

  cambiarFecha(): void {

    this.limpiarResultado();

    this.cajasConProvision = [];
    this.provisionesConsultadas = false;
    this.efectivos = [];

    this.idCajaOrigen = null;
    this.idCajaDestino = null;
    if (this.idAgencia) {
      this.cargarCajasConProvision();
    }
  }

  // =========================================================
  // CONSULTAS
  // =========================================================

  cargarCatalogos(): void {

    if (!this.idAgencia || this.idAgencia <= 0) {
      this.mensajeError = 'Seleccione una agencia válida.';
      return;
    }

    const idAgencia = this.idAgencia;

    this.consultando = true;
    this.mensajeError = '';

    this.api.listarCajasMovimiento(idAgencia)
      .pipe(
        finalize(() => this.consultando = false),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: cajas => {
          if (this.idAgencia !== idAgencia) {
            return;
          }

          this.cajasMovimiento = cajas;
        },
        error: error => this.mostrarError(error)
      });

    this.api.listarCuentasBancarias(idAgencia)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: cuentas => {
          if (this.idAgencia === idAgencia) {
            this.cuentasBancarias = cuentas;
          }
        },
        error: error => this.mostrarError(error)
      });

    this.api.listarCheques(idAgencia)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: cheques => {
          if (this.idAgencia === idAgencia) {
            this.chequesDisponibles = cheques;
          }
        },
        error: error => this.mostrarError(error)
      });

    this.cargarCajasConProvision();
  }

  cargarCajasConProvision(): void {

    if (!this.idAgencia || !this.fechaMovimiento) {
      return;
    }

    const idAgencia = this.idAgencia;
    const fecha = this.fechaMovimiento;
    this.provisionesConsultadas = false;
    this.consultandoProvisiones = true;
    this.cajasConProvision = [];
    this.idCajaOrigen = null;
    this.idCajaDestino = null;
    this.efectivos = [];
    this.limpiarResultado();

    this.api.listarCajasConProvision(idAgencia, fecha)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: cajas => {
          if (
            this.idAgencia === idAgencia &&
            this.fechaMovimiento === fecha
          ) {
            this.cajasConProvision = cajas;
            this.provisionesConsultadas = true;
            this.consultandoProvisiones = false;
          }
        },
        error: error => {
          if (this.idAgencia === idAgencia && this.fechaMovimiento === fecha) {
            this.consultandoProvisiones = false;
            this.provisionesConsultadas = false;
            this.mostrarError(error);
          }
        }
      });
  }

  // =========================================================
  // CHEQUES
  // =========================================================

  chequeSeleccionado(idCheque: number): boolean {
    return this.idsChequesSeleccionados.includes(idCheque);
  }

  cambiarCheque(idCheque: number, seleccionado: boolean): void {

    if (seleccionado) {
      if (!this.idsChequesSeleccionados.includes(idCheque)) {
        this.idsChequesSeleccionados = [
          ...this.idsChequesSeleccionados,
          idCheque
        ];
      }
    } else {
      this.idsChequesSeleccionados =
        this.idsChequesSeleccionados.filter(id => id !== idCheque);
    }

    this.limpiarResultado();
  }

  // =========================================================
  // EFECTIVO DE CONSIGNACIÓN
  // =========================================================

  obtenerValorEfectivo(idCaja: number): number | null {
    return this.efectivos.find(
      item => item.idCaja === idCaja
    )?.valor ?? null;
  }

  cambiarValorEfectivo(
    idCaja: number,
    valor: number | null
  ): void {

    this.limpiarResultado();

    this.efectivos = this.efectivos.filter(
      item => item.idCaja !== idCaja
    );

    const valorIngresado = Number(valor ?? 0);

    if (!Number.isFinite(valorIngresado) || valorIngresado <= 0) {
      return;
    }

    this.efectivos.push({
      idCaja,
      valor: valorIngresado
    });

    const caja = this.cajasConProvision.find(
      item => item.idCaja === idCaja
    );

    if (!caja) {
      this.mensajeError =
        'La caja seleccionada no tiene una provisión abierta.';
      return;
    }

    const disponible = Number(caja.efectivoDisponible ?? 0);

    if (valorIngresado > disponible) {
      this.mensajeError =
        `La caja ${caja.codigoCaja} tiene ` +
        `$${disponible.toLocaleString('es-CO')} disponibles. ` +
        `No puede consignar $${valorIngresado.toLocaleString('es-CO')}.`;
    }
  }

  // =========================================================
  // VISTA PREVIA CONTABLE
  // =========================================================

  consultarPreview(): void {

    this.limpiarResultado();

    const errorValidacion = this.validarFormulario();

    if (errorValidacion) {
      this.mensajeError = errorValidacion;
      return;
    }

    const request = this.construirRequest();

    this.cargando = true;

    this.api.previewContable(request)
      .pipe(
        finalize(() => this.cargando = false),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: resultado => {
          this.preview = resultado;
          this.solicitudPrevisualizada = JSON.stringify(request);
          this.mensajeInformativo =
            'Vista previa generada. No se ha registrado ningún movimiento.';
        },
        error: error => this.mostrarError(error)
      });
  }

  // =========================================================
  // REGISTRO DEFINITIVO
  // =========================================================

  aplicarMovimiento(): void {
    if (this.cargando || this.aplicando || this.movimientoAplicado ||
        !this.preview?.cuadrado) {
      return;
    }

    const error = this.validarFormulario();
    if (error) {
      this.mensajeError = error;
      return;
    }

    const request = this.construirRequest();
    if (JSON.stringify(request) !== this.solicitudPrevisualizada) {
      this.limpiarResultado();
      this.mensajeError = 'Los datos cambiaron. Genere nuevamente la vista previa.';
      return;
    }

    const confirmado = window.confirm(
      `¿Confirma el registro definitivo de ${request.tipoMovimiento} ` +
      `por $${this.preview.valorTotal.toLocaleString('es-CO')}?\n` +
      'Esta acción genera movimientos de caja y un comprobante contable.'
    );
    if (!confirmado) return;

    this.aplicando = true;
    this.mensajeError = '';
    this.api.aplicarMovimiento(request)
      .pipe(
        finalize(() => this.aplicando = false),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: resultado => {
          this.movimientoAplicado = true;
          this.numeroComprobanteGenerado = resultado.numeroComprobante;
          this.preview = null;
          this.mensajeInformativo =
            `Movimiento registrado correctamente. Comprobante ` +
            `${resultado.tipoComprobante} ${resultado.numeroComprobante}.`;
          this.cargarCatalogos();
        },
        error: error => this.mostrarError(error)
      });
  }

  // =========================================================
  // VALIDACIONES
  // =========================================================

  private validarFormulario(): string | null {

    if (!this.idAgencia || this.idAgencia <= 0) {
      return 'Seleccione una agencia.';
    }

    if (!this.fechaMovimiento) {
      return 'Seleccione la fecha del movimiento.';
    }

    if (!this.numeroSoporte.trim()) {
      return 'Digite el número de soporte.';
    }

    if (this.numeroSoporte.trim().length > 20) {
      return 'El número de soporte no puede superar 20 caracteres.';
    }

    if (!this.provisionesConsultadas || this.consultandoProvisiones) {
      return 'Debe verificarse la provisión abierta de las cajas para la fecha seleccionada.';
    }

    if (this.esTrasladoCajas) {

      if (!this.idCajaOrigen || !this.idCajaDestino) {
        return 'Seleccione las cajas de origen y destino.';
      }

      if (this.idCajaOrigen === this.idCajaDestino) {
        return 'La caja origen y destino deben ser diferentes.';
      }

      if (!this.valor || this.valor <= 0) {
        return 'Digite un valor mayor a cero.';
      }

      const origenValido = this.cajasOrigen.some(
        caja => caja.idCaja === this.idCajaOrigen
      );

      const destinoValido = this.cajasDestino.some(
        caja => caja.idCaja === this.idCajaDestino
      );

      if (!origenValido || !destinoValido) {
        return 'Las cajas seleccionadas no corresponden a la modalidad o no tienen provisión ABIERTA para la fecha.';
      }

      return null;
    }

    if (this.esConsignacion) {

      if (!this.idCuentaBancariaDestino) {
        return 'Seleccione la cuenta bancaria destino.';
      }

      if (this.efectivos.some(item => !this.cajaHabilitada(item.idCaja))) {
        return 'Una caja con efectivo no tiene provisión ABIERTA para la fecha.';
      }

      for (const efectivo of this.efectivos) {

        const caja = this.cajasConProvision.find(
          item => item.idCaja === efectivo.idCaja
        );

        if (!caja) {
          return 'No fue posible determinar el efectivo disponible de la caja.';
        }

        const disponible = Number(caja.efectivoDisponible ?? 0);
        const solicitado = Number(efectivo.valor ?? 0);

        if (!Number.isFinite(solicitado) || solicitado < 0) {
          return `El valor ingresado para la caja ${caja.codigoCaja} no es válido.`;
        }

        if (solicitado > disponible) {
          return `La caja ${caja.codigoCaja} tiene ` +
            `$${disponible.toLocaleString('es-CO')} disponibles. ` +
            `No puede consignar $${solicitado.toLocaleString('es-CO')}.`;
        }
      }

      if (this.valorTotalConsignacion <= 0) {
        return 'Seleccione cheques o registre efectivo para consignar.';
      }

      return null;
    }

    return 'Esta operación todavía no está habilitada.';
  }

  // =========================================================
  // CONSTRUIR SOLICITUD
  // =========================================================

  private construirRequest(): MovimientoFondoRequest {

    const request: MovimientoFondoRequest = {
      tipoMovimiento: this.tipoMovimiento,
      idAgencia: this.idAgencia!,
      fechaMovimiento: this.fechaMovimiento,
      numeroSoporte: this.numeroSoporte.trim(),
      observacion: this.observacion.trim() || null
    };

    if (this.esTrasladoCajas) {

      request.modalidadTrasladoCaja =
        this.modalidadTrasladoCaja;

      request.idCajaOrigen = this.idCajaOrigen;
      request.idCajaDestino = this.idCajaDestino;
      request.valor = Number(this.valor);

    } else if (this.esConsignacion) {

      request.idCuentaBancariaDestino =
        this.idCuentaBancariaDestino;

      request.idsCheques = [
        ...this.idsChequesSeleccionados
      ];

      request.efectivos = this.efectivos.map(
        item => ({ ...item })
      );
    }

    return request;
  }

  // =========================================================
  // UTILIDADES
  // =========================================================

  limpiarFormulario(): void {

    this.numeroSoporte = '';
    this.observacion = '';
    this.valor = null;

    this.idCuentaBancariaDestino = null;
    this.idCajaOrigen = null;
    this.idCajaDestino = null;

    this.idsChequesSeleccionados = [];
    this.efectivos = [];

    this.limpiarResultado();
  }

  private limpiarResultado(): void {
    this.preview = null;
    this.movimientoAplicado = false;
    this.numeroComprobanteGenerado = '';
    this.solicitudPrevisualizada = '';
    this.mensajeError = '';
    this.mensajeInformativo = '';
  }

  private mostrarError(error: unknown): void {

    const respuesta = error as {
      error?: {
        message?: string;
        mensaje?: string;
        error?: string;
      };
      message?: string;
    };

    this.mensajeError =
      respuesta?.error?.message ||
      respuesta?.error?.mensaje ||
      respuesta?.error?.error ||
      respuesta?.message ||
      'No fue posible completar la consulta.';
  }

  private fechaLocal(): string {

    const fecha = new Date();

    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');

    return `${anio}-${mes}-${dia}`;
  }

}
