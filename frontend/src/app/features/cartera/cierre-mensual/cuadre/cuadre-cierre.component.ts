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
  CuadreCierreApi
} from './cuadre-cierre.api';

import {
  CuadreCierre
} from './cuadre-cierre.models';


@Component({
  selector: 'app-cuadre-cierre',

  standalone: true,

  imports: [
    CommonModule
  ],

  templateUrl:
    './cuadre-cierre.component.html',

  styleUrl:
    './cuadre-cierre.component.scss'
})
export class CuadreCierreComponent
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
    CuadreCierre | null = null;


  constructor(
    private readonly api:
      CuadreCierreApi
  ) {}


  // =========================================================
  // CAMBIO DE CIERRE
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
  // CONSULTAR CUADRE
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
            'Error consultando cuadre del cierre:',
            err
          );

          this.resultado = null;

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible consultar el cuadre consolidado del cierre.'
            );

        }

      });
  }


  // =========================================================
  // VALIDAR DIFERENCIA CERO
  // =========================================================

  diferenciaOk(
    valor:
      number | null | undefined
  ): boolean {

    return Number(
      valor ?? 0
    ) === 0;
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
  // FORMATO MONEDA
  // =========================================================

  formatearMoneda(
    valor:
      number | null | undefined
  ): string {

    return new Intl.NumberFormat(
      'es-CO',
      {
        minimumFractionDigits: 0,
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
