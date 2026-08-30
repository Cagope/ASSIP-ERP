import {
  ChangeDetectionStrategy,
  Component,
  Input
} from '@angular/core';

import { CommonModule } from '@angular/common';

import {
  RiesgoDeterioroEdad
} from './riesgo-deterioro.models';

interface EdadChartItem {
  edad: string;
  cantidadCreditos: number;
  saldoCartera: number;
  deterioroTotal: number;
  porcentajeSaldo: number;
  porcentajeBarraSaldo: number;
  porcentajeBarraDeterioro: number;
}

@Component({
  selector: 'app-riesgo-deterioro-edades-chart',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './riesgo-deterioro-edades-chart.component.html',
  styleUrls: ['./riesgo-deterioro-edades-chart.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RiesgoDeterioroEdadesChartComponent {

  private _items: RiesgoDeterioroEdad[] = [];

  itemsGrafico: EdadChartItem[] = [];

  @Input()
  set items(value: RiesgoDeterioroEdad[] | null | undefined) {
    this._items = value ?? [];
    this.prepararGrafico();
  }

  get items(): RiesgoDeterioroEdad[] {
    return this._items;
  }

  private prepararGrafico(): void {

    const maxSaldo = Math.max(
      0,
      ...this._items.map(
        item => Number(item.saldoCartera ?? 0)
      )
    );

    const maxDeterioro = Math.max(
      0,
      ...this._items.map(
        item => Number(item.deterioroTotal ?? 0)
      )
    );

    this.itemsGrafico = this._items.map(item => {

      const saldo =
        Number(item.saldoCartera ?? 0);

      const deterioro =
        Number(item.deterioroTotal ?? 0);

      return {
        edad: item.edad ?? '',
        cantidadCreditos:
          Number(item.cantidadCreditos ?? 0),
        saldoCartera: saldo,
        deterioroTotal: deterioro,
        porcentajeSaldo:
          Number(item.porcentajeSaldo ?? 0),

        porcentajeBarraSaldo:
          this.calcularPorcentajeBarra(
            saldo,
            maxSaldo
          ),

        porcentajeBarraDeterioro:
          this.calcularPorcentajeBarra(
            deterioro,
            maxDeterioro
          )
      };
    });

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

  money(
    value: number | null | undefined
  ): string {

    return new Intl.NumberFormat(
      'es-CO',
      {
        style: 'currency',
        currency: 'COP',
        maximumFractionDigits: 0
      }
    ).format(
      Number(value ?? 0)
    );
  }

  pct(
    value: number | null | undefined
  ): string {

    return `${Number(
      value ?? 0
    ).toLocaleString(
      'es-CO',
      {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }
    )}%`;
  }

}
