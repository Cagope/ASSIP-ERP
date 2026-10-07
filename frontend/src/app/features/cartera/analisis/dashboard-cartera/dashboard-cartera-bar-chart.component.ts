import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnChanges,
  SimpleChanges
} from '@angular/core';

import { CommonModule } from '@angular/common';

import {
  DashboardCarteraLinea
} from './dashboard-cartera.models';

interface DashboardCarteraBarItem {
  codigo: string;
  descripcion: string;
  saldoCartera: number;
  saldoCreditosMora: number;
  indiceMora: number;
  porcentajeSaldo: number;
  porcentajeMora: number;
}

@Component({
  selector: 'app-dashboard-cartera-bar-chart',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './dashboard-cartera-bar-chart.component.html',
  styleUrls: ['./dashboard-cartera-bar-chart.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardCarteraBarChartComponent
  implements OnChanges {

  // =========================================================
  // Entradas
  // =========================================================

  @Input() titulo =
    'Comportamiento por línea de crédito';

  @Input() subtitulo =
    'Comparación del saldo de cartera y el saldo de créditos en mora.';

  @Input() data: DashboardCarteraLinea[] = [];

  @Input() limite = 10;

  // =========================================================
  // Datos preparados
  // =========================================================

  items: DashboardCarteraBarItem[] = [];

  totalSaldoCartera = 0;

  totalSaldoCreditosMora = 0;

  indiceMoraGeneral = 0;

  // =========================================================
  // Ciclo de cambios
  // =========================================================

  ngOnChanges(
    changes: SimpleChanges
  ): void {

    if (
      changes['data']
      || changes['limite']
    ) {
      this.prepararDatos();
    }

  }

  // =========================================================
  // Preparación de información
  // =========================================================

  private prepararDatos(): void {

    const lista =
      Array.isArray(this.data)
        ? this.data
        : [];

    const normalizados = lista
      .map(item => ({
        codigo:
          item.codigoLineaCredito?.trim()
          || 'SIN CÓDIGO',

        descripcion:
          item.nombreLineaCredito?.trim()
          || 'Línea sin descripción',

        saldoCartera:
          this.normalizarNumero(
            item.saldoCartera
          ),

        saldoCreditosMora:
          this.normalizarNumero(
            item.saldoCreditosMora
          ),

        indiceMora:
          this.normalizarNumero(
            item.indiceMora
          )
      }))
      .sort(
        (a, b) =>
          b.saldoCartera
          - a.saldoCartera
      );

    const limiteSeguro =
      this.limite > 0
        ? this.limite
        : normalizados.length;

    const seleccionados =
      normalizados.slice(
        0,
        limiteSeguro
      );

    const maximoSaldo =
      Math.max(
        0,
        ...seleccionados.map(
          item => item.saldoCartera
        )
      );

    this.items = seleccionados.map(
      item => ({
        ...item,

        porcentajeSaldo:
          this.calcularPorcentajeBarra(
            item.saldoCartera,
            maximoSaldo
          ),

        porcentajeMora:
          this.calcularPorcentajeBarra(
            item.saldoCreditosMora,
            maximoSaldo
          )
      })
    );

    this.totalSaldoCartera =
      normalizados.reduce(
        (
          acumulado,
          item
        ) =>
          acumulado
          + item.saldoCartera,
        0
      );

    this.totalSaldoCreditosMora =
      normalizados.reduce(
        (
          acumulado,
          item
        ) =>
          acumulado
          + item.saldoCreditosMora,
        0
      );

    this.indiceMoraGeneral =
      this.totalSaldoCartera > 0
        ? (
            this.totalSaldoCreditosMora
            / this.totalSaldoCartera
          ) * 100
        : 0;

  }

  private calcularPorcentajeBarra(
    valor: number,
    maximo: number
  ): number {

    if (
      valor <= 0
      || maximo <= 0
    ) {
      return 0;
    }

    return Math.min(
      100,
      Math.max(
        0,
        (valor / maximo) * 100
      )
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
  // Formatos
  // =========================================================

  formatoMoneda(
    valor: number
  ): string {

    return new Intl.NumberFormat(
      'es-CO',
      {
        style: 'currency',
        currency: 'COP',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }
    ).format(valor);

  }

  formatoPorcentaje(
    valor: number
  ): string {

    return new Intl.NumberFormat(
      'es-CO',
      {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }
    ).format(valor);

  }

  trackByLinea(
    index: number,
    item: DashboardCarteraBarItem
  ): string {

    return `${item.codigo}-${index}`;

  }

}
