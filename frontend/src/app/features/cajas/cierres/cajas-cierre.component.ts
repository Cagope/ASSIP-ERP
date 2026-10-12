import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CajasCierreApi } from './cajas-cierre.api';
import {
  CajaCierreConfirmarRequest,
  CajaCierreDisponible,
  CajaCierreEfectivoDetalle,
  CajaCierreResumen
} from './cajas-cierre.models';


interface DenominacionEfectivo {
  denominacion: number;
  cantidad: number;
}


@Component({
  selector: 'app-cajas-cierre',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './cajas-cierre.component.html',
  styleUrl: './cajas-cierre.component.scss'
})
export class CajasCierreComponent {

  cargando = false;
  confirmando = false;

  mensajeError = '';
  mensajeExito = '';

  fechaContable = '';

  resumen: CajaCierreResumen | null = null;

  cajasDisponibles: CajaCierreDisponible[] = [];
  idProvisionSeleccionada: number | null = null;
  mostrarSeleccionCaja = false;

  observacion = '';

  billetes: DenominacionEfectivo[] = [];

  valorVarios = 0;
  valorMonedas = 0;


  constructor(
    private readonly api: CajasCierreApi
  ) {}


  // =========================================================
  // CONSULTAR CIERRE POR FECHA
  // =========================================================

  preparar(): void {

    if (this.cargando || this.confirmando) {
      return;
    }

    if (!this.fechaContable) {
      this.mensajeError =
        'Debe seleccionar la fecha contable.';
      return;
    }

    this.cargando = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    this.resumen = null;
    this.cajasDisponibles = [];
    this.idProvisionSeleccionada = null;
    this.mostrarSeleccionCaja = false;

    this.billetes = [];
    this.valorVarios = 0;
    this.valorMonedas = 0;
    this.observacion = '';

    this.api
      .listarDenominaciones()
      .subscribe({

        next: (denominaciones) => {

          if (
            !denominaciones
            || denominaciones.length === 0
          ) {

            this.cargando = false;

            this.mensajeError =
              'No existen denominaciones de billetes activas '
              + 'configuradas para el cierre de caja.';

            return;
          }

          this.billetes =
            denominaciones.map(
              denominacion => ({
                denominacion:
                  this.numeroSeguro(
                    denominacion
                  ),
                cantidad: 0
              })
            );

          this.prepararProvision();
        },

        error: (error: HttpErrorResponse) => {

          this.cargando = false;

          this.mensajeError =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar las denominaciones '
              + 'de efectivo.'
            );
        }
      });
  }

  private prepararProvision(): void {

    this.api
      .preparar(this.fechaContable)
      .subscribe({

        next: (resumen) => {

          /*
           * Si el backend no devuelve una provisión,
           * el usuario posee alcance para seleccionar
           * la caja que desea cerrar.
           */
          if (!resumen) {
            this.cargarCajasDisponibles();
            return;
          }

          /*
           * Usuario de Caja:
           * el backend ya resolvió exclusivamente
           * la caja trabajada por ese usuario.
           */
          this.resumen = resumen;
          this.cargando = false;
        },

        error: (error: HttpErrorResponse) => {

          this.cargando = false;

          this.mensajeError =
            this.obtenerMensajeError(
              error,
              'No fue posible preparar el cierre de la caja.'
            );
        }
      });
  }

  private cargarCajasDisponibles(): void {

    this.api
      .listarDisponibles(
        this.fechaContable
      )
      .subscribe({

        next: (cajas) => {

          this.cargando = false;

          this.cajasDisponibles =
            cajas ?? [];

          this.idProvisionSeleccionada = null;

          if (this.cajasDisponibles.length === 0) {

            this.mostrarSeleccionCaja = false;

            this.mensajeError =
              'No existen cajas abiertas disponibles '
              + 'para la fecha contable seleccionada.';

            return;
          }

          /*
           * Un usuario con alcance ampliado siempre
           * selecciona explícitamente la caja.
           *
           * Incluso cuando exista una sola disponible.
           */
          this.mostrarSeleccionCaja = true;
          this.mensajeError = '';
        },

        error: (error: HttpErrorResponse) => {

          this.cargando = false;
          this.mostrarSeleccionCaja = false;

          this.mensajeError =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar las cajas disponibles.'
            );
        }
      });
  }

  seleccionarCaja(): void {

    if (
      this.cargando
      || this.confirmando
      || this.idProvisionSeleccionada == null
    ) {
      return;
    }

    this.cargando = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    this.api
      .obtenerResumen(
        this.idProvisionSeleccionada
      )
      .subscribe({

        next: (resumen) => {

          this.resumen = resumen;

          this.mostrarSeleccionCaja = false;
          this.cajasDisponibles = [];

          this.cargando = false;

          this.limpiarArqueo();
        },

        error: (error: HttpErrorResponse) => {

          this.cargando = false;

          this.mensajeError =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar la caja seleccionada.'
            );
        }
      });
  }


  // =========================================================
  // RECALCULAR PROVISIÓN SELECCIONADA
  // =========================================================

  recalcular(): void {

    if (
      this.cargando
      || this.confirmando
      || !this.resumen
    ) {
      return;
    }

    this.cargando = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    this.api
      .obtenerResumen(
        this.resumen.idProvision
      )
      .subscribe({

        next: (resumen) => {
          this.resumen = resumen;
          this.cargando = false;
        },

        error: (error: HttpErrorResponse) => {
          this.cargando = false;

          this.mensajeError =
            this.obtenerMensajeError(
              error,
              'No fue posible recalcular el cierre.'
            );
        }
      });
  }


  // =========================================================
  // ARQUEO DE EFECTIVO
  // =========================================================

  valorBillete(
    item: DenominacionEfectivo
  ): number {

    return (
      this.numeroSeguro(item.denominacion)
      * this.enteroSeguro(item.cantidad)
    );
  }


  get totalBilletes(): number {

    return this.billetes.reduce(
      (total, item) =>
        total + this.valorBillete(item),
      0
    );
  }


  get totalEfectivoContado(): number {

    return (
      this.totalBilletes
      + this.numeroSeguro(this.valorVarios)
      + this.numeroSeguro(this.valorMonedas)
    );
  }


  get diferenciaEfectivo(): number {

    if (!this.resumen) {
      return 0;
    }

    return (
      this.totalEfectivoContado
      - this.numeroSeguro(
        this.resumen.efectivoFinCalculado
      )
    );
  }


  get arqueoCuadrado(): boolean {

    if (!this.resumen) {
      return false;
    }

    return this.diferenciaEfectivo === 0;
  }


  // =========================================================
  // CONFIRMAR CIERRE
  // =========================================================

  confirmarCierre(): void {

    if (
      this.confirmando
      || this.cargando
      || !this.resumen
    ) {
      return;
    }

    if (
      this.resumen.estado !== 'ABIERTA'
    ) {
      this.mensajeError =
        'La provisión ya se encuentra cerrada.';
      return;
    }

    if (
      this.resumen.movimientosPendientes > 0
    ) {
      this.mensajeError =
        'No es posible cerrar la caja mientras existan movimientos pendientes.';
      return;
    }

    if (!this.validarArqueo()) {
      return;
    }

    const confirmado =
      window.confirm(
        '¿Confirma el cierre definitivo de la caja '
        + this.resumen.codigoCaja
        + ' para la fecha '
        + this.resumen.fechaContable
        + '?'
      );

    if (!confirmado) {
      return;
    }

    const request: CajaCierreConfirmarRequest = {
      idProvision: this.resumen.idProvision,

      observacion:
        this.observacion.trim() || null,

      efectivoDetalle:
        this.construirDetalleEfectivo()
    };

    this.confirmando = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    this.api
      .confirmar(request)
      .subscribe({

        next: (resumen) => {
          this.resumen = resumen;
          this.confirmando = false;

          this.mensajeExito =
            'La caja fue cerrada correctamente.';
        },

        error: (error: HttpErrorResponse) => {
          this.confirmando = false;

          this.mensajeError =
            this.obtenerMensajeError(
              error,
              'No fue posible cerrar la caja.'
            );
        }
      });
  }


  // =========================================================
  // CONSTRUIR DETALLE DE EFECTIVO
  // =========================================================

  private construirDetalleEfectivo():
    CajaCierreEfectivoDetalle[] {

    const detalleBilletes:
      CajaCierreEfectivoDetalle[] =
      this.billetes.map(item => ({

        tipoDetalle: 'BILLETE',

        denominacion:
          item.denominacion,

        cantidad:
          this.enteroSeguro(
            item.cantidad
          ),

        valorTotal:
          this.valorBillete(item)
      }));

    return [
      ...detalleBilletes,

      {
        tipoDetalle: 'VARIOS',
        denominacion: 0,
        cantidad: 1,
        valorTotal:
          this.numeroSeguro(
            this.valorVarios
          )
      },

      {
        tipoDetalle: 'MONEDAS',
        denominacion: 0,
        cantidad: 1,
        valorTotal:
          this.numeroSeguro(
            this.valorMonedas
          )
      }
    ];
  }


  // =========================================================
  // VALIDAR ARQUEO
  // =========================================================

  private validarArqueo(): boolean {

    for (const item of this.billetes) {

      if (
        !Number.isInteger(
          Number(item.cantidad)
        )
        || Number(item.cantidad) < 0
      ) {
        this.mensajeError =
          'Las cantidades de billetes deben ser números enteros no negativos.';
        return false;
      }
    }

    if (
      this.numeroSeguro(
        this.valorVarios
      ) < 0
    ) {
      this.mensajeError =
        'El valor de billetes varios no puede ser negativo.';
      return false;
    }

    if (
      this.numeroSeguro(
        this.valorMonedas
      ) < 0
    ) {
      this.mensajeError =
        'El valor de monedas no puede ser negativo.';
      return false;
    }

    if (!this.arqueoCuadrado) {

      this.mensajeError =
        'El efectivo contado no coincide con el efectivo calculado. '
        + 'Diferencia: '
        + this.diferenciaEfectivo;

      return false;
    }

    return true;
  }


  // =========================================================
  // LIMPIAR ARQUEO
  // =========================================================

  private limpiarArqueo(): void {

    this.billetes =
      this.billetes.map(item => ({
        denominacion:
          item.denominacion,
        cantidad: 0
      }));

    this.valorVarios = 0;
    this.valorMonedas = 0;
    this.observacion = '';
  }


  // =========================================================
  // UTILIDADES
  // =========================================================

  private numeroSeguro(
    valor: number | null | undefined
  ): number {

    const numero = Number(valor);

    return Number.isFinite(numero)
      ? numero
      : 0;
  }


  private enteroSeguro(
    valor: number | null | undefined
  ): number {

    const numero =
      this.numeroSeguro(valor);

    return Number.isInteger(numero)
      && numero >= 0
      ? numero
      : 0;
  }


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
