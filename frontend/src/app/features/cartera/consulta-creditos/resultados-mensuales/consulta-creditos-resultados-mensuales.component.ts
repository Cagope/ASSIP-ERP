import {
  CommonModule
} from '@angular/common';

import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import {
  ConsultaCreditoDetalle,
  ConsultaCreditoResultadoMensual
} from '../consulta-creditos.models';


@Component({
  selector: 'app-consulta-creditos-resultados-mensuales',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl:
    './consulta-creditos-resultados-mensuales.component.html',
  styleUrls: [
    './consulta-creditos-resultados-mensuales.component.scss'
  ]
})
export class ConsultaCreditosResultadosMensualesComponent {

  // =========================================================
  // ENTRADAS
  // =========================================================

  @Input({
    required: true
  })
  credito!:
    ConsultaCreditoDetalle;

  @Input()
  resultadosMensuales:
    ConsultaCreditoResultadoMensual[] = [];


  // =========================================================
  // SALIDAS
  // =========================================================

  @Output()
  cerrar =
    new EventEmitter<void>();


  // =========================================================
  // RESUMEN GENERAL
  // =========================================================

  get cantidadResultados(): number {

    return (
      this.resultadosMensuales?.length
      ?? 0
    );

  }


  get tieneResultados(): boolean {

    return this.cantidadResultados > 0;

  }


  get ultimoResultado():
    ConsultaCreditoResultadoMensual | null {

    if (!this.tieneResultados) {
      return null;
    }

    return this.resultadosMensuales.reduce(
      (
        ultimo,
        actual
      ) => {

        if (!ultimo) {
          return actual;
        }

        return (
          this.fechaComparable(
            actual.fechaCorte
          )
          >
          this.fechaComparable(
            ultimo.fechaCorte
          )
        )
          ? actual
          : ultimo;

      },
      null as ConsultaCreditoResultadoMensual | null
    );

  }


  get primerResultado():
    ConsultaCreditoResultadoMensual | null {

    if (!this.tieneResultados) {
      return null;
    }

    return this.resultadosMensuales.reduce(
      (
        primero,
        actual
      ) => {

        if (!primero) {
          return actual;
        }

        return (
          this.fechaComparable(
            actual.fechaCorte
          )
          <
          this.fechaComparable(
            primero.fechaCorte
          )
        )
          ? actual
          : primero;

      },
      null as ConsultaCreditoResultadoMensual | null
    );

  }


  get saldoUltimoCorte(): number {

    return Number(
      this.ultimoResultado
        ?.saldoCreditoFechaCorte
      ?? 0
    );

  }


  get deterioroUltimoCorte(): number {

    return Number(
      this.ultimoResultado
        ?.deterioroTotal
      ?? 0
    );

  }


  get perdidaEsperadaUltimoCorte(): number {

    return Number(
      this.ultimoResultado
        ?.perdidaEsperada
      ?? 0
    );

  }


  get diasMoraUltimoCorte(): number {

    return Number(
      this.ultimoResultado
        ?.diasMora
      ?? 0
    );

  }


  // =========================================================
  // ACCIONES
  // =========================================================

  cerrarDetalle(): void {

    this.cerrar.emit();

  }


  // =========================================================
  // MÉTODO DE CÁLCULO
  // =========================================================

  descripcionMetodo(
    resultado:
      ConsultaCreditoResultadoMensual
  ): string {

    const codigo =
      (
        resultado.codigoMetodoCalculo
        ?? ''
      )
        .trim()
        .toUpperCase();

    switch (codigo) {

      case 'A1':
        return 'Anexo 1';

      case 'PE':
        return 'Pérdida Esperada';

      default:
        return codigo;

    }

  }


  // =========================================================
  // RIESGO
  // =========================================================

  claseEdad(
    edad:
      string
      | null
      | undefined
  ): string {

    switch (
      (
        edad
        ?? ''
      )
        .trim()
        .toUpperCase()
    ) {

      case 'A':
        return 'edad edad--a';

      case 'B':
        return 'edad edad--b';

      case 'C':
        return 'edad edad--c';

      case 'D':
        return 'edad edad--d';

      case 'E':
        return 'edad edad--e';

      default:
        return 'edad';

    }

  }


  // =========================================================
  // VARIACIONES
  // =========================================================

  variacionSaldo(
    indice: number
  ): number | null {

    const actual =
      this.resultadosMensuales[
        indice
      ];

    const anterior =
      this.resultadosMensuales[
        indice + 1
      ];

    if (
      !actual
      || !anterior
    ) {
      return null;
    }

    return (
      Number(
        actual.saldoCreditoFechaCorte
        ?? 0
      )
      -
      Number(
        anterior.saldoCreditoFechaCorte
        ?? 0
      )
    );

  }


  variacionDeterioro(
    indice: number
  ): number | null {

    const actual =
      this.resultadosMensuales[
        indice
      ];

    const anterior =
      this.resultadosMensuales[
        indice + 1
      ];

    if (
      !actual
      || !anterior
    ) {
      return null;
    }

    return (
      Number(
        actual.deterioroTotal
        ?? 0
      )
      -
      Number(
        anterior.deterioroTotal
        ?? 0
      )
    );

  }


  // =========================================================
  // TRACK BY
  // =========================================================

  trackByResultado(
    index: number,
    resultado:
      ConsultaCreditoResultadoMensual
  ): number | string {

    return (
      resultado.idCierreCarteraResultado
      ?? resultado.idCierreCarteraCredito
      ?? resultado.fechaCorte
      ?? index
    );

  }


  // =========================================================
  // UTILIDADES
  // =========================================================

  private fechaComparable(
    fecha:
      string
      | null
      | undefined
  ): number {

    if (!fecha) {
      return 0;
    }

    const valor =
      new Date(
        `${fecha}T00:00:00`
      )
        .getTime();

    return Number.isFinite(
      valor
    )
      ? valor
      : 0;

  }

}
