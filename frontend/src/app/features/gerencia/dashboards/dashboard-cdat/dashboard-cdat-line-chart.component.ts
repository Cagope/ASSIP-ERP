import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

import { DashboardCdatTendencia } from './dashboard-cdat.models';

@Component({
  selector: 'app-dashboard-cdat-line-chart',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './dashboard-cdat-line-chart.component.html',
  styleUrls: ['./dashboard-cdat-line-chart.component.scss']
})
export class DashboardCdatLineChartComponent {

  @Input()
  data: DashboardCdatTendencia[] = [];

  get maxValor(): number {
    const valores = this.data.flatMap(x => [
      x.aperturas || 0,
      x.renovaciones || 0,
      x.cancelaciones || 0
    ]);

    return Math.max(...valores, 1);
  }

  get puntosAperturas(): string {
    return this.construirPuntos('aperturas');
  }

  get puntosRenovaciones(): string {
    return this.construirPuntos('renovaciones');
  }

  get puntosCancelaciones(): string {
    return this.construirPuntos('cancelaciones');
  }

  private construirPuntos(
    campo: 'aperturas' | 'renovaciones' | 'cancelaciones'
  ): string {

    if (!this.data.length) {
      return '';
    }

    const ancho = 900;
    const alto = 260;
    const margenX = 45;
    const margenY = 25;

    const espacio =
      (ancho - margenX * 2) / Math.max(this.data.length - 1, 1);

    return this.data
      .map((item, index) => {

        const valor = Number(item[campo] || 0);

        const x =
          margenX + index * espacio;

        const y =
          alto - margenY - ((valor / this.maxValor) * (alto - margenY * 2));

        return `${x},${y}`;
      })
      .join(' ');
  }

}
