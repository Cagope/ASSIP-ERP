import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';

import { CajasProvisionApi } from '../provisiones/cajas-provision.api';
import { CajaProvisionActiva } from '../provisiones/cajas-provision.models';

import { OperacionesPendientesApi } from './operaciones-pendientes.api';
import { CajaOperacionPendiente } from './operaciones-pendientes.models';


@Component({
  selector: 'app-operaciones-pendientes',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './operaciones-pendientes.component.html',
  styleUrl: './operaciones-pendientes.component.scss'
})
export class OperacionesPendientesComponent implements OnInit {

  // =========================================================
  // ESTADO
  // =========================================================

  cargando = false;
  confirmando = false;

  mensajeError = '';
  mensajeExito = '';

  provision: CajaProvisionActiva | null = null;

  operaciones: CajaOperacionPendiente[] = [];

  operacionSeleccionada: CajaOperacionPendiente | null = null;


  constructor(
    private readonly api: OperacionesPendientesApi,
    private readonly cajasProvisionApi: CajasProvisionApi
  ) {}


  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {
    this.cargar();
  }


  // =========================================================
  // CARGAR BANDEJA
  // =========================================================

  cargar(): void {

    if (this.cargando || this.confirmando) {
      return;
    }

    this.cargando = true;

    this.mensajeError = '';
    this.mensajeExito = '';

    this.provision = null;
    this.operaciones = [];
    this.operacionSeleccionada = null;

    this.cajasProvisionApi
      .obtenerProvisionActivaUsuario()
      .subscribe({

        next: (provision) => {

          this.provision = provision;

          this.cargarPendientes(
            provision
          );
        },

        error: (error: HttpErrorResponse) => {

          this.cargando = false;

          this.mensajeError =
            this.obtenerMensajeError(
              error,
              'No existe una caja abierta para el usuario actual.'
            );
        }
      });
  }


  // =========================================================
  // CONSULTAR PENDIENTES
  // =========================================================

  private cargarPendientes(
    provision: CajaProvisionActiva
  ): void {

    this.api.listarPendientes(
      provision.idCaja,
      provision.fechaContable
    ).subscribe({

      next: (operaciones) => {

        this.operaciones =
          operaciones ?? [];

        this.cargando = false;
      },

      error: (error: HttpErrorResponse) => {

        this.operaciones = [];
        this.cargando = false;

        this.mensajeError =
          this.obtenerMensajeError(
            error,
            'No fue posible consultar las operaciones pendientes.'
          );
      }
    });
  }


  // =========================================================
  // ATENDER
  // =========================================================

  atender(
    operacion: CajaOperacionPendiente
  ): void {

    if (this.confirmando) {
      return;
    }

    this.mensajeError = '';
    this.mensajeExito = '';

    this.operacionSeleccionada =
      operacion;
  }


  cerrarDetalle(): void {

    if (this.confirmando) {
      return;
    }

    this.operacionSeleccionada = null;
  }


  // =========================================================
  // CONFIRMAR
  // =========================================================

  confirmarOperacion(): void {

    const operacion =
      this.operacionSeleccionada;

    const provision =
      this.provision;

    if (
      !operacion
      || !provision
      || this.confirmando
    ) {
      return;
    }

    this.confirmando = true;

    this.mensajeError = '';
    this.mensajeExito = '';

    this.api.confirmar(
      operacion.idMovimientoCaja,
      provision.idCaja
    ).subscribe({

      next: () => {

        /*
         * La operación queda REGISTRADA.
         *
         * Al retirarla localmente desaparece inmediatamente
         * de la bandeja sin necesidad de recargar toda
         * la pantalla.
         */
        this.operaciones =
          this.operaciones.filter(
            item =>
              item.idMovimientoCaja !==
              operacion.idMovimientoCaja
          );

        this.operacionSeleccionada = null;

        this.confirmando = false;

        this.mensajeExito =
          'La operación fue confirmada correctamente.';
      },

      error: (error: HttpErrorResponse) => {

        this.confirmando = false;

        this.mensajeError =
          this.obtenerMensajeError(
            error,
            'No fue posible confirmar la operación.'
          );
      }
    });
  }


  // =========================================================
  // PRESENTACIÓN
  // =========================================================

  etiquetaNaturaleza(
    naturaleza: string
  ): string {

    if (naturaleza === 'INGRESO') {
      return 'Recibir';
    }

    if (naturaleza === 'EGRESO') {
      return 'Entregar';
    }

    return naturaleza;
  }


  instruccionOperacion(
    operacion: CajaOperacionPendiente
  ): string {

    if (
      operacion.naturaleza === 'INGRESO'
    ) {
      return 'Verifique físicamente el valor recibido antes de confirmar la operación.';
    }

    if (
      operacion.naturaleza === 'EGRESO'
    ) {
      return 'Verifique la identidad, el soporte y el valor antes de realizar la entrega.';
    }

    return 'Revise la información de la operación antes de continuar.';
  }


  // =========================================================
  // ERRORES
  // =========================================================

  private obtenerMensajeError(
    error: HttpErrorResponse,
    mensajePredeterminado: string
  ): string {

    if (
      typeof error.error === 'string'
      && error.error.trim()
    ) {
      return error.error;
    }

    if (
      error.error
      && typeof error.error === 'object'
      && typeof error.error.message === 'string'
      && error.error.message.trim()
    ) {
      return error.error.message;
    }

    return mensajePredeterminado;
  }
}
