import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';

import { CajasProvisionApi } from './cajas-provision.api';

import {
  CajaApertura,
  CajaAperturaRequest,
  CajaDisponible,
  CajaUsuario
} from './cajas-provision.models';


@Component({
  selector: 'app-cajas-provision',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NumericFormatDirective
  ],
  templateUrl: './cajas-provision.component.html',
  styleUrl: './cajas-provision.component.scss'
})
export class CajasProvisionComponent implements OnInit {

  // =========================================================
  // ESTADO GENERAL
  // =========================================================

  cargando = false;
  vinculando = false;
  cambiando = false;
  abriendo = false;

  modoCambio = false;

  mensajeError = '';
  mensajeExito = '';


  // =========================================================
  // VINCULACIÓN USUARIO ↔ CAJA
  // =========================================================

  cajaActiva: CajaUsuario | null = null;

  cajasDisponibles: CajaDisponible[] = [];

  idCajaSeleccionada: number | null = null;


  // =========================================================
  // APERTURA / PROVISIÓN
  // =========================================================

  apertura: CajaApertura | null = null;

  efectivoInicio = 0;
  chequesInicio = 0;
  valorInicialVentanilla = 0;

  get esCajaOperativa(): boolean {
    return this.apertura?.tipoCaja?.toUpperCase() === 'OPERATIVA';
  }

  get esCajaControl(): boolean {
    return this.apertura?.tipoCaja?.toUpperCase() === 'CONTROL';
  }

  constructor(
    private readonly api: CajasProvisionApi
  ) {}


  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {
    this.cargarEstado();
  }


  // =========================================================
  // CARGAR ESTADO GENERAL
  // =========================================================

  private cargarEstado(): void {

    this.cargando = true;

    this.limpiarMensajes();

    this.cajaActiva = null;
    this.apertura = null;

    this.cajasDisponibles = [];
    this.idCajaSeleccionada = null;

    this.modoCambio = false;


    this.api.obtenerVinculacionActiva().subscribe({

      next: (vinculacion) => {

        this.cajaActiva = vinculacion;

        /*
         * El usuario todavía no tiene caja seleccionada
         * para la jornada.
         */
        if (!vinculacion) {
          this.cargarCajasDisponibles();
          return;
        }

        /*
         * Ya existe vinculación.
         * Continuamos automáticamente con la preparación
         * financiera de la caja.
         */
        this.cargarApertura();
      },

      error: (error: HttpErrorResponse) => {

        this.cargando = false;

        this.mensajeError = this.obtenerMensajeError(
          error,
          'No fue posible consultar el estado actual de caja.'
        );
      }
    });
  }


  // =========================================================
  // CAJAS DISPONIBLES
  // =========================================================

  private cargarCajasDisponibles(): void {

    this.api.listarDisponibles().subscribe({

      next: (cajas) => {

        this.cajasDisponibles = cajas ?? [];

        this.idCajaSeleccionada = null;

        /*
         * Si únicamente existe una caja disponible,
         * la dejamos seleccionada.
         *
         * El usuario todavía debe confirmar.
         */
        if (this.cajasDisponibles.length === 1) {
          this.idCajaSeleccionada =
            this.cajasDisponibles[0].idCaja;
        }

        this.cargando = false;
      },

      error: (error: HttpErrorResponse) => {

        this.cajasDisponibles = [];
        this.idCajaSeleccionada = null;

        this.cargando = false;

        this.mensajeError = this.obtenerMensajeError(
          error,
          'No fue posible consultar las cajas disponibles.'
        );
      }
    });
  }


  // =========================================================
  // SELECCIONAR / VINCULAR CAJA
  // =========================================================

  vincularCaja(): void {

    if (this.idCajaSeleccionada === null) {

      this.mensajeError =
        'Seleccione una caja para continuar.';

      return;
    }

    if (this.vinculando) {
      return;
    }


    this.vinculando = true;

    this.limpiarMensajes();


    this.api.vincularCaja(
      this.idCajaSeleccionada
    ).subscribe({

      next: (vinculacion) => {

        this.cajaActiva = vinculacion;

        this.cajasDisponibles = [];
        this.idCajaSeleccionada = null;

        this.modoCambio = false;
        this.vinculando = false;

        /*
         * Seleccionar la caja no crea todavía la provisión.
         * Inmediatamente preparamos la apertura para que
         * el usuario continúe dentro de la misma pantalla.
         */
        this.cargando = true;

        this.cargarApertura();
      },

      error: (error: HttpErrorResponse) => {

        this.vinculando = false;

        this.mensajeError = this.obtenerMensajeError(
          error,
          'No fue posible seleccionar la caja.'
        );
      }
    });
  }


  // =========================================================
  // PREPARAR APERTURA
  // =========================================================

  private cargarApertura(): void {

    this.api.prepararApertura().subscribe({

      next: (apertura) => {

        this.apertura = apertura;

        /*
         * El backend ya aplicó:
         *
         * 1. valores de la provisión actual, si existe;
         * 2. último cierre anterior;
         * 3. cero para una caja nueva.
         */
        this.efectivoInicio =
          Number(apertura.efectivoInicio ?? 0);

        this.chequesInicio =
          Number(apertura.chequesInicio ?? 0);

        /*
         * Para una caja OPERATIVA proponemos el fondo configurado.
         * El usuario podrá modificar este valor antes de abrir.
         */
        this.valorInicialVentanilla = this.esCajaOperativa
          ? Number(apertura.fondoSugeridoVentanilla ?? 0)
          : 0;

        this.cargando = false;
      },

      error: (error: HttpErrorResponse) => {

        this.apertura = null;

        this.cargando = false;

        this.mensajeError = this.obtenerMensajeError(
          error,
          'No fue posible preparar la apertura de la caja.'
        );
      }
    });
  }


  // =========================================================
  // ABRIR / PROVISIONAR CAJA
  // =========================================================

  abrirCaja(): void {

    if (!this.cajaActiva || !this.apertura) {

      this.mensajeError =
        'No existe una caja preparada para iniciar.';

      return;
    }


    /*
     * Si ya existe provisión para la jornada no intentamos
     * crear otra.
     */
    if (this.apertura.tieneProvision) {
      return;
    }

    if (
      this.efectivoInicio === null ||
      !Number.isFinite(Number(this.efectivoInicio)) ||
      Number(this.efectivoInicio) < 0
    ) {
      this.mensajeError =
        'El efectivo inicial debe ser un valor válido y no negativo.';

      return;
    }

    if (
      this.chequesInicio === null ||
      !Number.isFinite(Number(this.chequesInicio)) ||
      Number(this.chequesInicio) < 0
    ) {
      this.mensajeError =
        'El valor inicial de cheques debe ser un valor válido y no negativo.';

      return;
    }

    if (this.esCajaOperativa) {

      if (
        this.valorInicialVentanilla === null ||
        !Number.isFinite(Number(this.valorInicialVentanilla)) ||
        Number(this.valorInicialVentanilla) < 0
      ) {
        this.mensajeError =
          'El valor inicial de ventanilla no puede ser negativo.';

        return;
      }

      if (
        Number(this.valorInicialVentanilla) >
        Number(this.efectivoInicio)
      ) {
        this.mensajeError =
          'El valor inicial de ventanilla no puede superar el efectivo disponible de la caja.';

        return;
      }
    }

    if (this.abriendo) {
      return;
    }

    this.abriendo = true;

    this.limpiarMensajes();


    const request: CajaAperturaRequest = {

      idCaja: this.cajaActiva.idCaja,

      efectivoInicio:
        Number(this.efectivoInicio),

      chequesInicio:
        Number(this.chequesInicio),

      valorInicialVentanilla: this.esCajaOperativa
        ? Number(this.valorInicialVentanilla)
        : 0
    };

    this.api.abrirCaja(request).subscribe({

      next: () => {

        this.abriendo = false;

        this.mensajeExito =
          `La caja ${this.cajaActiva?.codigoCaja ?? ''} quedó abierta para operar.`;

        /*
         * Volvemos a consultar PostgreSQL.
         *
         * No fabricamos localmente el estado ABIERTA ni el
         * id de provisión.
         */
        this.cargando = true;

        this.cargarApertura();
      },

      error: (error: HttpErrorResponse) => {

        this.abriendo = false;

        this.mensajeError = this.obtenerMensajeError(
          error,
          'No fue posible iniciar la caja.'
        );
      }
    });
  }


  // =========================================================
  // INICIAR CORRECCIÓN DE CAJA
  // =========================================================

  iniciarCambioCaja(): void {

    if (!this.cajaActiva) {
      return;
    }


    /*
     * La corrección solamente pertenece a la etapa previa
     * a la apertura financiera.
     */
    if (this.apertura?.tieneProvision) {

      this.mensajeError =
        'La caja ya fue abierta para la jornada y no puede cambiarse desde este proceso.';

      return;
    }


    if (
      this.cargando ||
      this.vinculando ||
      this.cambiando ||
      this.abriendo
    ) {
      return;
    }


    this.limpiarMensajes();

    this.cargando = true;
    this.modoCambio = true;

    this.cajasDisponibles = [];
    this.idCajaSeleccionada = null;


    this.api.listarDisponiblesCambio().subscribe({

      next: (cajas) => {

        this.cajasDisponibles = cajas ?? [];

        if (this.cajasDisponibles.length === 1) {
          this.idCajaSeleccionada =
            this.cajasDisponibles[0].idCaja;
        }

        this.cargando = false;
      },

      error: (error: HttpErrorResponse) => {

        this.cajasDisponibles = [];
        this.idCajaSeleccionada = null;

        this.modoCambio = false;
        this.cargando = false;

        this.mensajeError = this.obtenerMensajeError(
          error,
          'No fue posible consultar las cajas disponibles para el cambio.'
        );
      }
    });
  }


  // =========================================================
  // CANCELAR CORRECCIÓN
  // =========================================================

  cancelarCambioCaja(): void {

    if (this.cambiando) {
      return;
    }

    this.modoCambio = false;

    this.cajasDisponibles = [];
    this.idCajaSeleccionada = null;

    this.limpiarMensajes();
  }


  // =========================================================
  // CAMBIAR CAJA
  // =========================================================

  cambiarCaja(): void {

    if (!this.cajaActiva) {
      return;
    }


    if (this.apertura?.tieneProvision) {

      this.mensajeError =
        'La caja ya fue abierta para la jornada.';

      return;
    }


    if (this.idCajaSeleccionada === null) {

      this.mensajeError =
        'Seleccione la nueva caja.';

      return;
    }


    if (
      this.idCajaSeleccionada ===
      this.cajaActiva.idCaja
    ) {

      this.mensajeError =
        'Seleccione una caja diferente a la actualmente vinculada.';

      return;
    }


    if (this.cambiando) {
      return;
    }


    this.cambiando = true;

    this.limpiarMensajes();


    this.api.cambiarCaja(
      this.idCajaSeleccionada
    ).subscribe({

      next: (vinculacion) => {

        this.cajaActiva = vinculacion;

        this.cajasDisponibles = [];
        this.idCajaSeleccionada = null;

        this.modoCambio = false;
        this.cambiando = false;

        /*
         * La caja cambió.
         * Descartamos cualquier preparación anterior y
         * obtenemos la correspondiente a la nueva caja.
         */
        this.apertura = null;

        this.cargando = true;

        this.mensajeExito =
          `La caja fue cambiada correctamente a ${vinculacion.codigoCaja}.`;

        this.cargarApertura();
      },

      error: (error: HttpErrorResponse) => {

        this.cambiando = false;

        this.mensajeError = this.obtenerMensajeError(
          error,
          'No fue posible cambiar la caja.'
        );
      }
    });
  }


  // =========================================================
  // PRESENTACIÓN DE CAJA
  // =========================================================

  descripcionCaja(
    caja: CajaDisponible
  ): string {

    if (caja.descripcionCaja) {

      return `${caja.codigoCaja} - ${caja.descripcionCaja}`;
    }

    return caja.codigoCaja;
  }


  // =========================================================
  // MENSAJES
  // =========================================================

  private limpiarMensajes(): void {
    this.mensajeError = '';
    this.mensajeExito = '';
  }


  private obtenerMensajeError(
    error: HttpErrorResponse,
    mensajePredeterminado: string
  ): string {

    if (
      typeof error.error === 'string' &&
      error.error.trim()
    ) {
      return error.error;
    }


    if (
      error.error &&
      typeof error.error === 'object' &&
      typeof error.error.message === 'string' &&
      error.error.message.trim()
    ) {
      return error.error.message;
    }


    return mensajePredeterminado;
  }
}
