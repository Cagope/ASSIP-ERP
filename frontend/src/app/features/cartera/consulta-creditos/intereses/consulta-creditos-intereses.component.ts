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
  ConsultaCreditoInteres
} from '../consulta-creditos.models';


@Component({
  selector: 'app-consulta-creditos-intereses',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl:
    './consulta-creditos-intereses.component.html',
  styleUrls: [
    './consulta-creditos-intereses.component.scss'
  ]
})
export class ConsultaCreditosInteresesComponent {

  // =========================================================
  // ENTRADAS
  // =========================================================

  @Input({
    required: true
  })
  credito!:
    ConsultaCreditoDetalle;

  @Input()
  intereses:
    ConsultaCreditoInteres[] = [];


  // =========================================================
  // SALIDAS
  // =========================================================

  @Output()
  cerrar =
    new EventEmitter<void>();


  // =========================================================
  // INDICADORES GENERALES
  // =========================================================

  get cantidadMovimientos(): number {

    return (
      this.intereses?.length
      ?? 0
    );

  }

  get tieneMovimientos(): boolean {

    return this.cantidadMovimientos > 0;

  }

  get cantidadDebitos(): number {

    return this.intereses
      ?.filter(
        movimiento =>
          Number(
            movimiento.valorDebito
            ?? 0
          ) > 0
      )
      .length
      ?? 0;

  }

  get cantidadCreditos(): number {

    return this.intereses
      ?.filter(
        movimiento =>
          Number(
            movimiento.valorCredito
            ?? 0
          ) > 0
      )
      .length
      ?? 0;

  }

  get totalDebitos(): number {

    return this.intereses
      ?.reduce(
        (
          total,
          movimiento
        ) =>
          total
          + Number(
            movimiento.valorDebito
            ?? 0
          ),
        0
      )
      ?? 0;

  }

  get totalCreditos(): number {

    return this.intereses
      ?.reduce(
        (
          total,
          movimiento
        ) =>
          total
          + Number(
            movimiento.valorCredito
            ?? 0
          ),
        0
      )
      ?? 0;

  }

  get saldoPendiente(): number {

    const ultimo =
      this.ultimoMovimiento;

    if (
      ultimo?.saldoInteresesAcumulado
      !== null
      && ultimo?.saldoInteresesAcumulado
      !== undefined
    ) {
      return Number(
        ultimo.saldoInteresesAcumulado
      );
    }

    return (
      this.totalDebitos
      -
      this.totalCreditos
    );

  }


  // =========================================================
  // ÚLTIMO MOVIMIENTO
  // =========================================================

  get ultimoMovimiento():
    ConsultaCreditoInteres | null {

    if (
      !this.intereses
      || this.intereses.length === 0
    ) {
      return null;
    }

    return this.intereses
      .reduce(
        (
          ultimo,
          actual
        ) => {

          if (!ultimo) {
            return actual;
          }

          const fechaUltimo =
            this.fechaComparable(
              ultimo.fechaMovimiento
            );

          const fechaActual =
            this.fechaComparable(
              actual.fechaMovimiento
            );

          if (
            fechaActual > fechaUltimo
          ) {
            return actual;
          }

          if (
            fechaActual === fechaUltimo
            && Number(
              actual.idInteresCausado
              ?? 0
            )
            >
            Number(
              ultimo.idInteresCausado
              ?? 0
            )
          ) {
            return actual;
          }

          return ultimo;

        },
        null as ConsultaCreditoInteres | null
      );

  }


  get fechaUltimoMovimiento():
    string | null {

    return (
      this.ultimoMovimiento
        ?.fechaMovimiento
      ?? null
    );

  }


  // =========================================================
  // ACCIONES
  // =========================================================

  cerrarDetalle(): void {

    this.cerrar.emit();

  }


  // =========================================================
  // NATURALEZA DEL MOVIMIENTO
  // =========================================================

  esDebito(
    movimiento:
      ConsultaCreditoInteres
  ): boolean {

    return (
      Number(
        movimiento.valorDebito
        ?? 0
      ) > 0
    );

  }

  esCredito(
    movimiento:
      ConsultaCreditoInteres
  ): boolean {

    return (
      Number(
        movimiento.valorCredito
        ?? 0
      ) > 0
    );

  }

  esMovimientoSinValor(
    movimiento:
      ConsultaCreditoInteres
  ): boolean {

    return (
      Number(
        movimiento.valorDebito
        ?? 0
      ) === 0
      &&
      Number(
        movimiento.valorCredito
        ?? 0
      ) === 0
    );

  }


  descripcionNaturaleza(
    movimiento:
      ConsultaCreditoInteres
  ): string {

    if (
      movimiento.naturalezaMovimiento
    ) {
      return movimiento.naturalezaMovimiento;
    }

    if (
      this.esDebito(
        movimiento
      )
    ) {
      return 'DEBITO';
    }

    if (
      this.esCredito(
        movimiento
      )
    ) {
      return 'CREDITO';
    }

    return 'SIN MOVIMIENTO';

  }


  // =========================================================
  // DESCRIPCIONES
  // =========================================================

  descripcionComprobante(
    movimiento:
      ConsultaCreditoInteres
  ): string {

    if (
      movimiento.comprobanteCompleto
    ) {
      return movimiento.comprobanteCompleto;
    }

    if (
      movimiento.tipoComprobante
      && movimiento.numeroComprobante
    ) {
      return (
        movimiento.tipoComprobante
        + '-'
        + movimiento.numeroComprobante
      );
    }

    return (
      movimiento.numeroComprobante
      ?? movimiento.tipoComprobante
      ?? ''
    );

  }


  descripcionObservacion(
    movimiento:
      ConsultaCreditoInteres
  ): string {

    return (
      movimiento.observacionInteres
      ?? movimiento.observacion
      ?? ''
    );

  }


  descripcionPeriodo(
    movimiento:
      ConsultaCreditoInteres
  ): string {

    const inicial =
      movimiento.fechaInicialPeriodo
      ?? movimiento.periodoInicial;

    const final =
      movimiento.fechaFinalPeriodo
      ?? movimiento.periodoFinal;

    if (
      inicial
      && final
    ) {
      return (
        inicial
        + ' - '
        + final
      );
    }

    if (inicial) {
      return inicial;
    }

    if (final) {
      return final;
    }

    return '';

  }


  // =========================================================
  // VALIDACIONES VISUALES
  // =========================================================

  periodoCoincide(
    movimiento:
      ConsultaCreditoInteres
  ): boolean {

    return (
      movimiento.diasCausadosCoincidenPeriodo
      !== false
    );

  }

  requiereRevision(
    movimiento:
      ConsultaCreditoInteres
  ): boolean {

    return (
      movimiento.requiereRevision
      === true
    );

  }


  // =========================================================
  // ORDEN / TRACK BY
  // =========================================================

  trackByMovimiento(
    indice: number,
    movimiento:
      ConsultaCreditoInteres
  ): number {

    return (
      movimiento.idInteresCausado
      ?? indice
    );

  }


  // =========================================================
  // UTILIDADES
  // =========================================================

  private fechaComparable(
    fecha:
      string | null | undefined
  ): number {

    if (!fecha) {
      return 0;
    }

    const valor =
      Date.parse(
        `${fecha}T00:00:00`
      );

    return Number.isNaN(valor)
      ? 0
      : valor;

  }

}
