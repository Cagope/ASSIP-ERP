import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MoraTempranaSegmento } from './mora-temprana.models';

@Component({
  selector: 'app-mora-temprana-segmentos-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mora-temprana-segmentos-chart.component.html',
  styleUrls: ['./mora-temprana-segmentos-chart.component.scss']
})
export class MoraTempranaSegmentosChartComponent {
  @Input() datos: MoraTempranaSegmento[] = [];
  @Input() limite = 12;

  get visibles(): MoraTempranaSegmento[] {
    return [...this.datos]
      .sort((a, b) => Number(b.porcentajeMora30Mob6 || 0) - Number(a.porcentajeMora30Mob6 || 0))
      .slice(0, this.limite);
  }

  get maximo(): number {
    return Math.max(1, ...this.visibles.map(x => Number(x.porcentajeMora30Mob6 || 0)));
  }

  ancho(v: number): number {
    return Math.max(0, Math.min(100, Number(v || 0) * 100 / this.maximo));
  }

  pct(v: number): string {
    return `${Number(v || 0).toLocaleString('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}%`;
  }
}
