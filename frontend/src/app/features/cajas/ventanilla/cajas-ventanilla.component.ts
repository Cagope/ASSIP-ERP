import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { CajasVentanillaApi } from './cajas-ventanilla.api';

import {
  CajaVentanillaEstado,
  CajaVentanillaMovimiento,
  CajaVentanillaTipoMovimiento
} from './cajas-ventanilla.models';

import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';

@Component({
  selector: 'app-cajas-ventanilla',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NumericFormatDirective
  ],
  templateUrl: './cajas-ventanilla.component.html',
  styleUrl: './cajas-ventanilla.component.scss'
})
export class CajasVentanillaComponent implements OnInit {

  // =========================================================
  // ESTADO
  // =========================================================

  estado: CajaVentanillaEstado | null = null;

  cargando = false;
  procesando = false;

  mensajeError = '';
  mensajeExito = '';


  // =========================================================
  // MOVIMIENTO
  // =========================================================

  tipoMovimiento: CajaVentanillaTipoMovimiento =
    'CAJA_A_VENTANILLA';

  valor: number | null = null;
  observacion = '';


  constructor(
    private readonly api: CajasVentanillaApi
  ) {}


  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {
    this.cargarEstadoActual();
  }


  // =========================================================
  // ESTADO ACTUAL
  // =========================================================

  cargarEstadoActual(): void {

    if (this.cargando || this.procesando) {
      return;
    }

    this.cargando = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    this.api.obtenerEstadoActual().subscribe({

      next: (estado) => {
        this.estado = estado;
        this.cargando = false;
        this.limpiarCaptura();
      },

      error: (error: HttpErrorResponse) => {
        this.cargando = false;
        this.estado = null;

        this.mensajeError = this.obtenerMensajeError(
          error,
          'No existe una caja con provisión activa para la jornada actual.'
        );
      }

    });
  }


  // =========================================================
  // SELECCIONAR TIPO
  // =========================================================

  seleccionarTipo(
    tipo: CajaVentanillaTipoMovimiento
  ): void {

    if (this.procesando) {
      return;
    }

    this.tipoMovimiento = tipo;
    this.valor = null;
    this.observacion = this.observacionSugerida(tipo);

    this.mensajeError = '';
    this.mensajeExito = '';
  }


  // =========================================================
  // TRASLADAR
  // =========================================================

  trasladar(): void {

    this.mensajeError = '';
    this.mensajeExito = '';

    if (!this.estado) {
      this.mensajeError =
        'No existe una caja disponible para realizar el traslado.';
      return;
    }

    const valor = this.valorNumerico;

    if (valor <= 0) {
      this.mensajeError =
        'Debe indicar un valor mayor que cero.';
      return;
    }

    /*
     * Validaciones de interfaz.
     *
     * El backend vuelve a calcular y validar los saldos
     * bajo bloqueo de la provisión.
     */
    if (valor > this.saldoOrigen) {
      this.mensajeError =
        this.tipoMovimiento === 'CAJA_A_VENTANILLA'
          ? 'El valor supera el efectivo disponible en la caja interna.'
          : 'El valor supera el efectivo disponible en ventanilla.';
      return;
    }

    if (
      this.tipoMovimiento === 'CAJA_A_VENTANILLA'
      && this.estado.limiteMaximoVentanilla > 0
      && this.saldoDestinoResultante
        > this.estado.limiteMaximoVentanilla
    ) {
      this.mensajeError =
        'El traslado supera el límite máximo permitido en ventanilla.';
      return;
    }

    const movimiento: CajaVentanillaMovimiento = {
      tipoMovimiento: this.tipoMovimiento,
      valor,
      observacion: this.normalizarObservacion()
    };

    this.procesando = true;

    this.api.trasladar(movimiento).subscribe({

      next: (estadoActualizado) => {

        this.estado = estadoActualizado;
        this.procesando = false;

        this.valor = null;
        this.observacion = '';

        this.mensajeExito =
          this.tipoMovimiento === 'CAJA_A_VENTANILLA'
            ? 'Traslado de Caja a Ventanilla realizado correctamente.'
            : 'Traslado de Ventanilla a Caja realizado correctamente.';
      },

      error: (error: HttpErrorResponse) => {

        this.procesando = false;

        /*
         * No alteramos localmente los saldos cuando falla.
         * El backend sigue siendo la fuente de verdad.
         */
        this.mensajeError = this.obtenerMensajeError(
          error,
          'No fue posible realizar el traslado de efectivo.'
        );
      }

    });
  }


  // =========================================================
  // SALDOS
  // =========================================================

  get saldoOrigen(): number {

    if (!this.estado) {
      return 0;
    }

    return this.tipoMovimiento === 'CAJA_A_VENTANILLA'
      ? this.estado.saldoCajaInterna
      : this.estado.saldoVentanilla;
  }


  get saldoDestino(): number {

    if (!this.estado) {
      return 0;
    }

    return this.tipoMovimiento === 'CAJA_A_VENTANILLA'
      ? this.estado.saldoVentanilla
      : this.estado.saldoCajaInterna;
  }


  get saldoOrigenResultante(): number {
    return this.saldoOrigen - this.valorNumerico;
  }


  get saldoDestinoResultante(): number {
    return this.saldoDestino + this.valorNumerico;
  }


  get valorNumerico(): number {

    if (
      this.valor == null
      || !Number.isFinite(Number(this.valor))
    ) {
      return 0;
    }

    return Math.max(
      0,
      Number(this.valor)
    );
  }


  // =========================================================
  // ESTADO DEL FORMULARIO
  // =========================================================

  get puedeTrasladar(): boolean {

    if (
      !this.estado
      || this.cargando
      || this.procesando
      || this.valorNumerico <= 0
    ) {
      return false;
    }

    if (
      this.valorNumerico
      > this.saldoOrigen
    ) {
      return false;
    }

    if (
      this.tipoMovimiento === 'CAJA_A_VENTANILLA'
      && this.estado.limiteMaximoVentanilla > 0
      && this.saldoDestinoResultante
        > this.estado.limiteMaximoVentanilla
    ) {
      return false;
    }

    return true;
  }


  get tituloMovimiento(): string {

    return this.tipoMovimiento === 'CAJA_A_VENTANILLA'
      ? 'Caja → Ventanilla'
      : 'Ventanilla → Caja';
  }


  get nombreOrigen(): string {

    return this.tipoMovimiento === 'CAJA_A_VENTANILLA'
      ? 'Caja interna'
      : 'Ventanilla';
  }


  get nombreDestino(): string {

    return this.tipoMovimiento === 'CAJA_A_VENTANILLA'
      ? 'Ventanilla'
      : 'Caja interna';
  }


  // =========================================================
  // LIMPIAR
  // =========================================================

  limpiarCaptura(): void {

    this.tipoMovimiento = 'CAJA_A_VENTANILLA';

    this.valor = null;

    this.observacion =
      this.observacionSugerida(
        this.tipoMovimiento
      );
  }


  // =========================================================
  // UTILIDADES
  // =========================================================

  private normalizarObservacion(): string | null {

    const observacion =
      this.observacion?.trim() ?? '';

    return observacion.length > 0
      ? observacion
      : null;
  }


  private obtenerMensajeError(
    error: HttpErrorResponse,
    mensajePredeterminado: string
  ): string {

    if (typeof error.error === 'string') {

      const mensaje =
        error.error.trim();

      if (mensaje) {
        return mensaje;
      }
    }

    if (
      error.error
      && typeof error.error === 'object'
    ) {

      const mensaje =
        error.error.message
        ?? error.error.mensaje
        ?? error.error.error;

      if (
        typeof mensaje === 'string'
        && mensaje.trim()
      ) {
        return mensaje.trim();
      }
    }

    return mensajePredeterminado;
  }

  private observacionSugerida(
    tipo: CajaVentanillaTipoMovimiento
  ): string {

    return tipo === 'CAJA_A_VENTANILLA'
      ? 'Traslado de efectivo de caja a ventanilla'
      : 'Retorno de efectivo de ventanilla a caja';
  }

}
