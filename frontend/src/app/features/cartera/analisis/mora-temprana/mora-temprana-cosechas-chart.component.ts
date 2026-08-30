import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MoraTempranaCosecha } from './mora-temprana.models';

@Component({
  selector: 'app-mora-temprana-cosechas-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mora-temprana-cosechas-chart.component.html',
  styleUrls: ['./mora-temprana-cosechas-chart.component.scss']
})
export class MoraTempranaCosechasChartComponent {
  @Input() datos: MoraTempranaCosecha[] = [];

  get maximo(): number {
    return Math.max(1, ...this.datos.flatMap(x => [
      Number(x.porcentajeMora30Mob3 || 0),
      Number(x.porcentajeMora30Mob6 || 0),
      Number(x.porcentajeMora60Mob6 || 0)
    ]));
  }

  ancho(valor: number): number {
    return Math.max(0, Math.min(100, Number(valor || 0) * 100 / this.maximo));
  }

  mes(fecha: string): string {
    if (!fecha) return '';
    const [y, m] = fecha.substring(0, 7).split('-');
    return `${m}/${y}`;
  }

  pct(v: number): string {
    return `${Number(v || 0).toLocaleString('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}%`;
  }
}
