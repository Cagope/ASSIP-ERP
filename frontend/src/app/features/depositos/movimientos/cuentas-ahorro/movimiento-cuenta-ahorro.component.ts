import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { NumericFormatDirective } from '../../../../shared/utils/numeric-format.directive';

import {
  CuentaMovimientoDTO,
  MovimientoCuentaAhorroApi,
  MovimientoCuentaPreviewDTO,
  MovimientoCuentaRequestDTO,
  MovimientoCuentaResponseDTO,
  TipoMovimientoDTO
} from './movimiento-cuenta-ahorro.api';

import {
  TiposComprobantesApi,
  TipoComprobante,
} from '../../../contabilidad/tipos-comprobantes/tipos-comprobantes.api';

import {
  ExtractoCuentaSharedComponent
} from '../../../../shared/cuentas-ahorro/extracto-cuenta/extracto-cuenta-shared.component';

@Component({
  selector: 'app-movimiento-cuenta-ahorro',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NumericFormatDirective,
    ExtractoCuentaSharedComponent
  ],
  templateUrl: './movimiento-cuenta-ahorro.component.html',
  styleUrls: ['./movimiento-cuenta-ahorro.component.scss']
})
export class MovimientoCuentaAhorroComponent implements OnInit {

  private readonly api = inject(MovimientoCuentaAhorroApi);
  private readonly tiposComprobantesApi = inject(TiposComprobantesApi);

  cargando = false;

  error = '';
  mensaje = '';

  mostrarExtracto = false;

  idAgencia = 2;

  filtros = this.crearFiltros();

  cuentas: CuentaMovimientoDTO[] = [];

  cuentaSeleccionada: CuentaMovimientoDTO | null = null;

  tiposMovimiento: TipoMovimientoDTO[] = [];

  tiposComprobantes: TipoComprobante[] = [];

  movimiento = this.crearMovimiento();

  resultado: MovimientoCuentaResponseDTO | null = null;

  preview: MovimientoCuentaPreviewDTO | null = null;

  ngOnInit(): void {
    this.cargarTiposMovimiento();
    this.cargarTiposComprobantes();
  }

  buscar(): void {

    this.limpiarMensajes();
    this.preview = null;
    this.resultado = null;
    this.cuentaSeleccionada = null;

    if (!this.tieneFiltrosBusqueda()) {
      this.error = 'Debe ingresar al menos un criterio de búsqueda.';
      return;
    }

    this.cargando = true;

    this.api.buscarCuentas({
      idAgencia: this.idAgencia,
      documento: this.clean(this.filtros.documento),
      nombres: this.clean(this.filtros.nombres),
      primerApellido: this.clean(this.filtros.primerApellido),
      segundoApellido: this.clean(this.filtros.segundoApellido)
    }).subscribe({

      next: data => {

        this.cuentas = data || [];

        if (this.cuentas.length === 0) {
          this.mensaje =
            'No se encontraron cuentas para los criterios ingresados.';
        }

        this.cargando = false;
      },

      error: err => {
        this.error = this.obtenerMensajeError(err);
        this.cargando = false;
      }

    });

  }

  seleccionarCuenta(cuenta: CuentaMovimientoDTO): void {

    this.cuentaSeleccionada = cuenta;

    this.preview = null;
    this.resultado = null;

    this.limpiarMensajes();

    if (!cuenta.estadoOperativo) {
      this.error =
        cuenta.mensajeOperativo
        || 'La cuenta no está disponible para transaccionar.';
    }

  }

  consultarPreview(): void {

    this.preview = null;

    if (!this.validarMovimiento(false)) {
      return;
    }

    this.api.preview(
      this.construirRequest()
    ).subscribe({

      next: data => {
        this.preview = data;
        this.error = '';
      },

      error: err => {
        this.preview = null;
        this.error = this.obtenerMensajeError(err);
      }

    });

  }

  aplicar(): void {

    this.limpiarMensajes();

    if (this.cargando) {
      return;
    }

    if (!this.validarMovimiento(true)) {
      return;
    }

    const confirma =
      confirm('¿Desea aplicar el movimiento?');

    if (!confirma) {
      return;
    }

    this.cargando = true;

    this.api.aplicar(
      this.construirRequest()
    ).subscribe({

      next: data => {

        this.resultado = data;

        this.mensaje =
          data.mensaje
          || 'Movimiento aplicado correctamente.';

        this.resetMovimiento();

        this.cargando = false;
      },

      error: err => {
        this.error = this.obtenerMensajeError(err);
        this.cargando = false;
      }

    });

  }

  limpiar(): void {

    this.filtros = this.crearFiltros();

    this.cuentas = [];

    this.cuentaSeleccionada = null;

    this.preview = null;

    this.resultado = null;

    this.movimiento = this.crearMovimiento();

    this.limpiarMensajes();

    this.mostrarExtracto = false;
  }

  cargarTiposMovimiento(): void {

    this.api.listarTiposMovimiento().subscribe({

      next: data => {
        this.tiposMovimiento = data || [];
      },

      error: err => {
        this.error = this.obtenerMensajeError(err);
      }

    });

  }

  cargarTiposComprobantes(): void {

    this.tiposComprobantesApi
      .listarPorAgencia(this.idAgencia)
      .subscribe({

        next: data => {
          this.tiposComprobantes = data || [];
        },

        error: err => {
          this.error = this.obtenerMensajeError(err);
        }

      });

  }

  verMovimientos(): void {

    if (!this.cuentaSeleccionada) {
      return;
    }

    this.mostrarExtracto =
      !this.mostrarExtracto;
  }

  valorDisponibleCuenta(): number {

    if (!this.cuentaSeleccionada) {
      return 0;
    }

    return (
      Number(this.cuentaSeleccionada.saldoActualCuenta || 0)
      - Number(this.cuentaSeleccionada.valorEnCanje || 0)
    );
  }

  tieneApoderado(): boolean {

    return !!(
      this.cuentaSeleccionada?.nombrePoder
      && this.cuentaSeleccionada?.documentoPoder
    );
  }

  tieneCuentaConjunta(): boolean {
    return !!this.cuentaSeleccionada?.cuentaConjuntaReal;
  }

  documentoSoporteTexto(): string {

    if (!this.cuentaSeleccionada?.tipoDocumentoSoporte) {
      return 'Sin documento activo';
    }

    return (
      this.cuentaSeleccionada.tipoDocumentoSoporte
      + ' / '
      + (this.cuentaSeleccionada.numeroInicialLibreta || '')
      + ' - '
      + (this.cuentaSeleccionada.numeroFinalLibreta || '')
    );
  }

  textoApoderado(): string {

    if (!this.tieneApoderado()) {
      return 'NO REGISTRA';
    }

    return (
      this.cuentaSeleccionada!.documentoPoder
      + ' - '
      + this.cuentaSeleccionada!.nombrePoder
    );
  }

  textoCuentaConjunta(): string {

    if (!this.tieneCuentaConjunta()) {
      return 'NO';
    }

    return this.cuentaSeleccionada?.conjuntos || 'SI';
  }

  esCredito(): boolean {

    return this.obtenerTipoMovimiento()?.accionMovimiento === 'S';
  }

  esDebito(): boolean {

    return this.obtenerTipoMovimiento()?.accionMovimiento === 'R';
  }

  valorCreditoPreview(): number {

    return this.esCredito()
      ? Number(this.movimiento.valorMovimiento || 0)
      : 0;
  }

  valorDebitoPreview(): number {

    return this.esDebito()
      ? Number(this.movimiento.valorMovimiento || 0)
      : 0;
  }

  nuevoSaldoPreview(): number {

    if (!this.cuentaSeleccionada) {
      return 0;
    }

    return (
      Number(this.cuentaSeleccionada.saldoActualCuenta || 0)
      + this.valorCreditoPreview()
      - this.valorDebitoPreview()
    );
  }

  formatearNumeroComprobante(): void {

    const valor = String(
      this.movimiento.numeroComprobante || ''
    )
      .replace(/\D/g, '')
      .trim();

    this.movimiento.numeroComprobante =
      valor
        ? valor.padStart(10, '0')
        : '';
  }

  private validarMovimiento(
    validarComprobante: boolean
  ): boolean {

    if (!this.cuentaSeleccionada) {
      this.error = 'Debe seleccionar una cuenta.';
      return false;
    }

    if (!this.movimiento.tipoMovimiento) {
      this.error = 'Debe seleccionar el tipo de movimiento.';
      return false;
    }

    const valor =
      Number(this.movimiento.valorMovimiento || 0);

    if (valor <= 0) {
      this.error =
        'El valor del movimiento debe ser mayor a cero.';
      return false;
    }

    if (
      this.esDebito()
      && this.nuevoSaldoPreview() < 0
    ) {
      this.error =
        'El movimiento deja la cuenta con saldo negativo.';
      return false;
    }

    if (!validarComprobante) {
      return true;
    }

    if (!this.movimiento.tipoComprobante) {
      this.error =
        'Debe seleccionar el tipo de comprobante.';
      return false;
    }

    if (!this.movimiento.numeroComprobante) {
      this.error =
        'Debe ingresar el número de comprobante.';
      return false;
    }

    if (
      !this.movimiento.detalle
      || this.movimiento.detalle.trim().length < 5
    ) {
      this.error =
        'Debe ingresar la justificación del movimiento.';
      return false;
    }

    return true;
  }

  private construirRequest(): MovimientoCuentaRequestDTO {

    return {
      idCuentaAhorro:
        this.cuentaSeleccionada!.idCuentaAhorro,

      idAgencia:
        this.idAgencia,

      fechaMovimiento:
        this.movimiento.fechaMovimiento,

      tipoMovimiento:
        this.clean(this.movimiento.tipoMovimiento) || '',

      valorMovimiento:
        Number(this.movimiento.valorMovimiento || 0),

      tipoComprobante:
        this.clean(this.movimiento.tipoComprobante),

      numeroComprobante:
        this.clean(this.movimiento.numeroComprobante),

      detalle:
        this.clean(this.movimiento.detalle)
    };
  }

  private obtenerTipoMovimiento():
    TipoMovimientoDTO | undefined {

    return this.tiposMovimiento.find(
      x => x.codigoMovimiento === this.movimiento.tipoMovimiento
    );
  }

  private obtenerMensajeError(err: any): string {

    return err?.error?.message
      || err?.error?.error
      || err?.message
      || 'Ocurrió un error procesando la solicitud.';
  }

  private tieneFiltrosBusqueda(): boolean {

    return !!(
      this.clean(this.filtros.documento)
      || this.clean(this.filtros.nombres)
      || this.clean(this.filtros.primerApellido)
      || this.clean(this.filtros.segundoApellido)
    );
  }

  private resetMovimiento(): void {

    this.movimiento =
      this.crearMovimiento();

    this.preview = null;

    this.resultado = null;

    this.cuentaSeleccionada = null;

    this.cuentas = [];

    this.filtros = this.crearFiltros();
  }

  private crearMovimiento() {

    return {
      fechaMovimiento:
        this.hoy(),

      tipoMovimiento: '',

      valorMovimiento:
        null as number | null,

      tipoComprobante: 'ND',

      numeroComprobante: '',

      detalle: ''
    };
  }

  private crearFiltros() {

    return {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: ''
    };
  }

  private limpiarMensajes(): void {
    this.error = '';
    this.mensaje = '';
  }

  private hoy(): string {
    return new Date()
      .toISOString()
      .substring(0, 10);
  }

  private clean(
    value?: string | null
  ): string | undefined {

    const limpio =
      value?.trim();

    return limpio
      ? limpio
      : undefined;
  }

}
