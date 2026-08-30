import {
  CommonModule
} from '@angular/common';

import {
  Component,
  Input,
  OnChanges,
  SimpleChanges
} from '@angular/core';

import {
  finalize
} from 'rxjs/operators';

import {
  ValidacionCierreApi
} from './validacion-cierre.api';

import {
  ResultadoValidacionCierre
} from './validacion-cierre.models';


@Component({
  selector: 'app-validacion-cierre',

  standalone: true,

  imports: [
    CommonModule
  ],

  templateUrl:
    './validacion-cierre.component.html',

  styleUrl:
    './validacion-cierre.component.scss'
})
export class ValidacionCierreComponent
  implements OnChanges {

  // =========================================================
  // ENTRADA
  // =========================================================

  @Input()
  idCierreCartera:
    number | null = null;


  // =========================================================
  // ESTADO
  // =========================================================

  cargando = false;

  error = '';

  resultado:
    ResultadoValidacionCierre | null = null;


  constructor(
    private readonly api:
      ValidacionCierreApi
  ) {}


  // =========================================================
  // CAMBIO DE CIERRE
  //
  // Al seleccionar otro cierre:
  //
  // - limpia resultado anterior
  // - consulta automáticamente sus validaciones
  //
  // =========================================================

  ngOnChanges(
    changes: SimpleChanges
  ): void {

    if (
      changes['idCierreCartera']
    ) {

      this.resultado = null;

      this.error = '';

      if (this.idCierreCartera) {

        this.consultar();

      }
    }
  }


  // =========================================================
  // CONSULTAR VALIDACIÓN
  // =========================================================

  consultar(): void {

    if (
      !this.idCierreCartera
      ||
      this.cargando
    ) {
      return;
    }


    this.error = '';

    this.cargando = true;


    this.api
      .consultar(
        this.idCierreCartera
      )
      .pipe(
        finalize(() => {

          this.cargando = false;

        })
      )
      .subscribe({

        next: (resultado) => {

          this.resultado =
            resultado;

        },


        error: (err) => {

          console.error(
            'Error consultando validaciones del cierre:',
            err
          );

          this.resultado = null;

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible consultar las validaciones del cierre.'
            );

        }

      });
  }


  // =========================================================
  // ESTADO DE UN CONTROL NUMÉRICO
  // =========================================================

  controlOk(
    cantidad:
      number | null | undefined
  ): boolean {

    return Number(
      cantidad ?? 0
    ) === 0;
  }


  // =========================================================
  // CONTROL DE POBLACIÓN
  // =========================================================

  poblacionCuadrada(): boolean {

    if (!this.resultado) {
      return false;
    }

    return (
      this.resultado.cantidadCreditosCabecera
      ===
      this.resultado.cantidadCreditosFotografia

      &&

      this.resultado.cantidadCreditosFotografia
      ===
      this.resultado.cantidadResultados
    );
  }


  // =========================================================
  // DISTRIBUCIÓN A1 + PE
  // =========================================================

  distribucionCuadrada(): boolean {

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
  // PE PERSISTIDO
  // =========================================================

  pePersistidoCuadrado(): boolean {

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
  // FORMATO ENTERO
  // =========================================================

  formatearEntero(
    valor:
      number | null | undefined
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
  // MENSAJE DE ERROR
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
