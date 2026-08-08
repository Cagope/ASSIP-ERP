import {
  ChangeDetectionStrategy,
  Component,
  Input
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  DashboardCarteraDistribucion
} from './dashboard-cartera.models';

interface DashboardCarteraDonutSegmento
  extends DashboardCarteraDistribucion {

  porcentajeCalculado: number;
  inicio: number;
  color: string;
}

@Component({
  selector: 'app-dashboard-cartera-donut-chart',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './dashboard-cartera-donut-chart.component.html',
  styleUrls: ['./dashboard-cartera-donut-chart.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardCarteraDonutChartComponent {

  // =========================================================
  // Entradas
  // =========================================================

  @Input()
  titulo = '';

  @Input()
  subtitulo = '';

  @Input()
  data: DashboardCarteraDistribucion[] = [];

  // =========================================================
  // Colores
  // =========================================================

  readonly colores = [
    '#047857',
    '#2563eb',
    '#f59e0b',
    '#ea580c',
    '#dc2626',
    '#7c3aed'
  ];

  // =========================================================
  // Totales
  // =========================================================

  get totalSaldoCartera(): number {

    return this.listaSegura.reduce(
      (
        acumulado,
        item
      ) =>
        acumulado
        + this.normalizarNumero(
          item.saldoCartera
        ),
      0
    );

  }

  get totalCreditos(): number {

    return this.listaSegura.reduce(
      (
        acumulado,
        item
      ) =>
        acumulado
        + this.normalizarNumero(
          item.cantidadCreditos
        ),
      0
    );

  }

  // =========================================================
  // Segmentos
  // =========================================================

  get segmentos(): DashboardCarteraDonutSegmento[] {

    let acumulado = 0;

    return this.listaSegura.map(
      (
        item,
        index
      ) => {

        const saldoCartera =
          this.normalizarNumero(
            item.saldoCartera
          );

        const porcentajeCalculado =
          this.totalSaldoCartera > 0
            ? (
                saldoCartera
                / this.totalSaldoCartera
              ) * 100
            : 0;

        const inicio =
          acumulado;

        acumulado +=
          porcentajeCalculado;

        return {
          ...item,

          saldoCartera,

          cantidadCreditos:
            this.normalizarNumero(
              item.cantidadCreditos
            ),

          porcentajeParticipacion:
            this.normalizarNumero(
              item.porcentajeParticipacion
            ),

          porcentajeCalculado,

          inicio,

          color:
            this.colores[
              index % this.colores.length
            ]
        };

      }
    );

  }

  // =========================================================
  // Fondo del donut
  // =========================================================

  get fondoDonut(): string {

    if (
      !this.segmentos.length
      || this.totalSaldoCartera <= 0
    ) {
      return '#e2e8f0';
    }

    return `conic-gradient(${
      this.segmentos
        .map(
          segmento =>
            `${segmento.color} `
            + `${segmento.inicio}% `
            + `${
              segmento.inicio
              + segmento.porcentajeCalculado
            }%`
        )
        .join(',')
    })`;

  }

  // =========================================================
  // Estado
  // =========================================================

  get tieneInformacion(): boolean {

    return (
      this.segmentos.length > 0
      && this.totalSaldoCartera > 0
    );

  }

  private get listaSegura(): DashboardCarteraDistribucion[] {

    return Array.isArray(this.data)
      ? this.data
      : [];

  }

  // =========================================================
  // Formatos
  // =========================================================

  formatoMoneda(
    valor: number | null | undefined
  ): string {

    return new Intl.NumberFormat(
      'es-CO',
      {
        style: 'currency',
        currency: 'COP',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }
    ).format(
      this.normalizarNumero(valor)
    );

  }

  formatoMonedaCompacta(
    valor: number | null | undefined
  ): string {

    const numero =
      this.normalizarNumero(valor);

    if (Math.abs(numero) >= 1_000_000_000) {

      return '$ '
        + new Intl.NumberFormat(
          'es-CO',
          {
            minimumFractionDigits: 1,
            maximumFractionDigits: 1
          }
        ).format(numero / 1_000_000_000)
        + ' mil MM';

    }

    if (Math.abs(numero) >= 1_000_000) {

      return '$ '
        + new Intl.NumberFormat(
          'es-CO',
          {
            minimumFractionDigits: 0,
            maximumFractionDigits: 1
          }
        ).format(numero / 1_000_000)
        + ' MM';

    }

    return this.formatoMoneda(numero);
  }


  formatoPorcentaje(
    valor: number | null | undefined
  ): string {

    return new Intl.NumberFormat(
      'es-CO',
      {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }
    ).format(
      this.normalizarNumero(valor)
    );

  }

  private normalizarNumero(
    valor: number | null | undefined
  ): number {

    const numero =
      Number(valor ?? 0);

    return Number.isFinite(numero)
      ? numero
      : 0;

  }

  // =========================================================
  // Seguimiento de filas
  // =========================================================

  trackBySegmento(
    index: number,
    item: DashboardCarteraDonutSegmento
  ): string {

    return `${item.codigo}-${index}`;

  }

}
