import {
  CommonModule
} from '@angular/common';

import {
  Component,
  Input
} from '@angular/core';

import {
  finalize
} from 'rxjs/operators';

import {
  ProcesamientoCierreApi
} from './procesamiento-cierre.api';

import {
  ResultadoProcesamientoCierre
} from './procesamiento-cierre.models';


@Component({
  selector: 'app-procesamiento-cierre',

  standalone: true,

  imports: [
    CommonModule
  ],

  templateUrl:
    './procesamiento-cierre.component.html',

  styleUrl:
    './procesamiento-cierre.component.scss'
})
export class ProcesamientoCierreComponent {

  // =========================================================
  // ENTRADAS DESDE EL COMPONENTE PADRE
  // =========================================================

  @Input()
  idCierreCartera:
    number | null = null;

  @Input()
  estadoCierre:
    string | null = null;

  @Input()
  fechaCorte:
    string | null = null;


  // =========================================================
  // ESTADO DEL PROCESO
  // =========================================================

  ejecutando = false;

  error = '';

  mensaje = '';


  // =========================================================
  // RESULTADO DEL ÚLTIMO PROCESAMIENTO
  // =========================================================

  resultado:
    ResultadoProcesamientoCierre | null = null;


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly api:
      ProcesamientoCierreApi
  ) {}


  // =========================================================
  // EJECUTAR PROCESAMIENTO COMPLETO
  //
  // Backend:
  //
  // POST
  // /cartera/cierre-mensual/{id}/procesar-calculos
  //
  // Ejecuta:
  //
  // - intereses
  // - seguros
  // - alivios
  // - cálculos previos
  // - Anexo 1
  // - Anexo 2 / PE
  // - validaciones finales
  // - cuadre consolidado
  //
  // =========================================================

  procesar(): void {

    this.limpiarMensajes();

    if (!this.idCierreCartera) {

      this.error =
        'Debe seleccionar un cierre de cartera.';

      return;
    }


    if (!this.puedeProcesar()) {

      this.error =
        'Los cálculos solamente pueden ejecutarse '
        + 'cuando la fotografía se encuentre cerrada en firme.';

      return;
    }


    if (this.ejecutando) {
      return;
    }


    const confirmar =
      window.confirm(
        'Se ejecutarán nuevamente todos los cálculos del cierre'
        + this.descripcionFechaConfirmacion()
        + '.\n\n'
        + 'El proceso recalculará los resultados regenerables, '
        + 'incluyendo Anexo 1 y Anexo 2 / PE.\n\n'
        + '¿Desea continuar?'
      );

    if (!confirmar) {
      return;
    }


    this.ejecutando = true;

    this.resultado = null;


    this.api
      .procesar(
        this.idCierreCartera
      )
      .pipe(
        finalize(() => {

          this.ejecutando = false;

        })
      )
      .subscribe({

        next: (resultado) => {

          this.resultado =
            resultado;

          this.mensaje =
            resultado.validacionesFinalesOk
              ? 'Los cálculos del cierre finalizaron correctamente.'
              : 'Los cálculos terminaron, pero existen validaciones pendientes.';

        },


        error: (err) => {

          console.error(
            'Error procesando cálculos del cierre de cartera:',
            err
          );

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible procesar los cálculos del cierre.'
            );

        }

      });
  }


  // =========================================================
  // VALIDAR SI PUEDE PROCESAR
  //
  // C = fotografía cerrada en firme
  // =========================================================

  puedeProcesar(): boolean {

    if (!this.idCierreCartera) {
      return false;
    }

    return (
      (this.estadoCierre ?? '')
        .trim()
        .toUpperCase() === 'C'
    );
  }


  // =========================================================
  // VALIDAR SI EXISTE RESULTADO
  // =========================================================

  tieneResultado(): boolean {

    return this.resultado !== null;
  }


  // =========================================================
  // RESULTADO GENERAL CORRECTO
  // =========================================================

  procesamientoCorrecto(): boolean {

    return !!this.resultado
      && this.resultado.validacionesFinalesOk
      && (
        this.resultado.estadoValidacionFinal ?? ''
      )
        .trim()
        .toUpperCase() === 'OK';
  }


  // =========================================================
  // CONTROL A1 + PE
  // =========================================================

  poblacionFinalCuadrada(): boolean {

    if (!this.resultado) {
      return false;
    }

    return (
      this.resultado.cantidadResultadosA1
      +
      this.resultado.cantidadResultadosPe
      ===
      this.resultado.cantidadResultados
    );
  }


  // =========================================================
  // CONTROL PE
  // =========================================================

  poblacionPeCuadrada(): boolean {

    if (!this.resultado) {
      return false;
    }

    return (
      this.resultado.cantidadResultadosPe
      ===
      this.resultado.cantidadPePersistidos
    );
  }


  // =========================================================
  // TEXTO DEL ESTADO
  // =========================================================

  descripcionEstado(
    estado: string | null | undefined
  ): string {

    switch (
      (estado ?? '')
        .trim()
        .toUpperCase()
    ) {

      case 'P':
        return 'En proceso';

      case 'C':
        return 'Cerrado';

      default:
        return estado || '';

    }
  }


  // =========================================================
  // FORMATO FECHA
  // =========================================================

  formatearFecha(
    fecha: string | null | undefined
  ): string {

    if (!fecha) {
      return '';
    }

    const valor =
      fecha.substring(
        0,
        10
      );

    const partes =
      valor.split('-');

    if (partes.length !== 3) {
      return fecha;
    }

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
  }


  // =========================================================
  // FORMATO ENTERO
  // =========================================================

  formatearEntero(
    valor: number | null | undefined
  ): string {

    return new Intl.NumberFormat(
      'es-CO',
      {
        maximumFractionDigits: 0
      }
    ).format(
      Number(valor ?? 0)
    );
  }


  // =========================================================
  // DESCRIPCIÓN DE FECHA PARA CONFIRMACIÓN
  // =========================================================

  private descripcionFechaConfirmacion(): string {

    if (!this.fechaCorte) {
      return '';
    }

    return ' correspondiente al '
      + this.formatearFecha(
        this.fechaCorte
      );
  }


  // =========================================================
  // LIMPIAR MENSAJES
  // =========================================================

  private limpiarMensajes(): void {

    this.error = '';

    this.mensaje = '';
  }


  // =========================================================
  // MENSAJE DE ERROR DEL BACKEND
  // =========================================================

  private obtenerMensajeError(
    err: any,
    mensajeDefecto: string
  ): string {

    if (
      typeof err?.error === 'string'
      &&
      err.error.trim()
    ) {

      return err.error;
    }


    if (
      typeof err?.error?.message === 'string'
      &&
      err.error.message.trim()
    ) {

      return err.error.message;
    }


    if (
      typeof err?.message === 'string'
      &&
      err.message.trim()
    ) {

      return err.message;
    }


    return mensajeDefecto;
  }
}
