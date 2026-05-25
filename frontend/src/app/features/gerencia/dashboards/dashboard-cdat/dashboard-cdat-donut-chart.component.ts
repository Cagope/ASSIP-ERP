import {
  Component,
  Input
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  DashboardCdatGrupo
} from './dashboard-cdat.models';

@Component({
  selector: 'app-dashboard-cdat-donut-chart',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './dashboard-cdat-donut-chart.component.html',
  styleUrls: ['./dashboard-cdat-donut-chart.component.scss']
})
export class DashboardCdatDonutChartComponent {

  @Input()
  titulo = '';

  @Input()
  subtitulo = '';

  @Input()
  data: DashboardCdatGrupo[] = [];

  colores = [
    '#047857',
    '#2563eb',
    '#ea580c',
    '#9333ea',
    '#dc2626',
    '#0891b2'
  ];

  get total(): number {

    return this.data.reduce(
      (sum, item) => sum + Number(item.valorTotal || 0),
      0
    );
  }

  get segmentos(): any[] {

    let acumulado = 0;

    return this.data.map((item, index) => {

      const valor =
        Number(item.valorTotal || 0);

      const porcentaje =
        this.total > 0
          ? (valor / this.total) * 100
          : 0;

      const inicio = acumulado;
      acumulado += porcentaje;

      return {
        ...item,
        porcentaje,
        inicio,
        color: this.colores[index % this.colores.length]
      };
    });
  }

  get fondoDonut(): string {

    if (!this.segmentos.length) {
      return '#e2e8f0';
    }

    return 'conic-gradient(' +
      this.segmentos
        .map(s =>
          `${s.color} ${s.inicio}% ${s.inicio + s.porcentaje}%`
        )
        .join(',') +
      ')';
  }

}
