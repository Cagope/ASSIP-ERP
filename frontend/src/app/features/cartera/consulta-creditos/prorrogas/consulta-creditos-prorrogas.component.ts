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
  ConsultaCreditoProrroga
} from '../consulta-creditos.models';


@Component({
  selector: 'app-consulta-creditos-prorrogas',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl:
    './consulta-creditos-prorrogas.component.html',
  styleUrls: [
    './consulta-creditos-prorrogas.component.scss'
  ]
})
export class ConsultaCreditosProrrogasComponent {

  // =========================================================
  // ENTRADAS
  // =========================================================

  @Input({
    required: true
  })
  credito!:
    ConsultaCreditoDetalle;

  @Input()
  prorrogas:
    ConsultaCreditoProrroga[] = [];


  // =========================================================
  // SALIDAS
  // =========================================================

  @Output()
  cerrar =
    new EventEmitter<void>();


  // =========================================================
  // INDICADORES GENERALES
  // =========================================================

  get cantidadProrrogas(): number {

    return (
      this.prorrogas?.length
      ?? 0
    );

  }


  get tieneProrrogas(): boolean {

    return this.cantidadProrrogas > 0;

  }


  get cantidadAplicadas(): number {

    return this.prorrogas.filter(
      prorroga =>
        this.codigoEstado(
          prorroga
        ) === 'A'
    ).length;

  }


  get cantidadPendientes(): number {

    return this.prorrogas.filter(
      prorroga =>
        this.codigoEstado(
          prorroga
        ) === 'P'
    ).length;

  }


  get cantidadAnuladas(): number {

    return this.prorrogas.filter(
      prorroga =>
        this.codigoEstado(
          prorroga
        ) === 'N'
    ).length;

  }


  get totalMesesProrrogados(): number {

    return this.prorrogas
      .filter(
        prorroga =>
          this.codigoEstado(
            prorroga
          ) !== 'N'
      )
      .reduce(
        (
          total,
          prorroga
        ) =>
          total
          +
          Number(
            prorroga.mesesProrroga
            ?? 0
          ),
        0
      );

  }


  get totalLiquidado(): number {

    return this.prorrogas
      .filter(
        prorroga =>
          this.codigoEstado(
            prorroga
          ) !== 'N'
      )
      .reduce(
        (
          total,
          prorroga
        ) =>
          total
          +
          Number(
            prorroga.valorTotalLiquidado
            ?? 0
          ),
        0
      );

  }


  get totalPagado(): number {

    return this.prorrogas
      .filter(
        prorroga =>
          this.codigoEstado(
            prorroga
          ) !== 'N'
      )
      .reduce(
        (
          total,
          prorroga
        ) =>
          total
          +
          Number(
            prorroga.valorTotalPagado
            ?? 0
          ),
        0
      );

  }


  // =========================================================
  // ÚLTIMA PRÓRROGA
  // =========================================================

  get ultimaProrroga():
    ConsultaCreditoProrroga | null {

    if (
      !this.prorrogas
      || this.prorrogas.length === 0
    ) {
      return null;
    }

    return this.prorrogas.reduce(
      (
        ultima,
        actual
      ) => {

        if (!ultima) {
          return actual;
        }

        const numeroUltima =
          Number(
            ultima.numeroProrroga
            ?? 0
          );

        const numeroActual =
          Number(
            actual.numeroProrroga
            ?? 0
          );

        if (
          numeroActual
          > numeroUltima
        ) {
          return actual;
        }

        if (
          numeroActual
          < numeroUltima
        ) {
          return ultima;
        }

        const fechaUltima =
          this.fechaComparable(
            ultima.fechaProrroga
          );

        const fechaActual =
          this.fechaComparable(
            actual.fechaProrroga
          );

        if (
          fechaActual
          > fechaUltima
        ) {
          return actual;
        }

        return ultima;

      },
      null as ConsultaCreditoProrroga | null
    );

  }


  // =========================================================
  // ACCIONES
  // =========================================================

  cerrarDetalle(): void {

    this.cerrar.emit();

  }


  // =========================================================
  // ESTADO
  // =========================================================

  descripcionEstado(
    prorroga:
      ConsultaCreditoProrroga
  ): string {

    switch (
      this.codigoEstado(
        prorroga
      )
    ) {

      case 'A':
        return 'Aplicada';

      case 'P':
        return 'Pendiente';

      case 'N':
        return 'Anulada';

      default:
        return (
          prorroga.estadoProrroga
          ?? ''
        );

    }

  }


  esAplicada(
    prorroga:
      ConsultaCreditoProrroga
  ): boolean {

    return (
      this.codigoEstado(
        prorroga
      ) === 'A'
    );

  }


  esPendiente(
    prorroga:
      ConsultaCreditoProrroga
  ): boolean {

    return (
      this.codigoEstado(
        prorroga
      ) === 'P'
    );

  }


  esAnulada(
    prorroga:
      ConsultaCreditoProrroga
  ): boolean {

    return (
      this.codigoEstado(
        prorroga
      ) === 'N'
    );

  }


  // =========================================================
  // COMPROBANTE
  // =========================================================

  comprobante(
    prorroga:
      ConsultaCreditoProrroga
  ): string {

    const tipo =
      (
        prorroga.tipoComprobante
        ?? ''
      ).trim();

    const numero =
      (
        prorroga.numeroComprobante
        ?? ''
      ).trim();

    if (
      tipo
      && numero
    ) {
      return `${tipo}-${numero}`;
    }

    return (
      tipo
      || numero
      || ''
    );

  }


  // =========================================================
  // VARIACIONES
  // =========================================================

  incrementoPlazo(
    prorroga:
      ConsultaCreditoProrroga
  ): number | null {

    if (
      prorroga.plazoAnterior == null
      || prorroga.plazoNuevo == null
    ) {
      return null;
    }

    return (
      Number(
        prorroga.plazoNuevo
      )
      -
      Number(
        prorroga.plazoAnterior
      )
    );

  }


  // =========================================================
  // TRACK BY
  // =========================================================

  trackByProrroga(
    index: number,
    prorroga:
      ConsultaCreditoProrroga
  ): number | string {

    return (
      prorroga.idCreditoProrroga
      ?? prorroga.numeroProrroga
      ?? index
    );

  }


  // =========================================================
  // UTILIDADES
  // =========================================================

  private codigoEstado(
    prorroga:
      ConsultaCreditoProrroga
  ): string {

    return (
      prorroga.estadoProrroga
      ?? ''
    )
      .trim()
      .toUpperCase();

  }


  private fechaComparable(
    fecha:
      string | null | undefined
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
