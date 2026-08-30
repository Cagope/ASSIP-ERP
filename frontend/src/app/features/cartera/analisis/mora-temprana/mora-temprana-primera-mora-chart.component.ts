import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MoraTempranaPrimeraMora } from './mora-temprana.models';

@Component({
  selector: 'app-mora-temprana-primera-mora-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mora-temprana-primera-mora-chart.component.html',
  styleUrls: ['./mora-temprana-primera-mora-chart.component.scss']
})
export class MoraTempranaPrimeraMoraChartComponent {
  @Input() datos: MoraTempranaPrimeraMora[] = [];

  readonly mobs = [1, 2, 3, 4, 5, 6];

  valor(evento: string, mob: number): MoraTempranaPrimeraMora | undefined {
    return this.datos.find(x => x.evento === evento && Number(x.mob) === mob);
  }

  get maximo(): number {
    return Math.max(1, ...this.datos.map(x => Number(x.cantidadCreditos || 0)));
  }

  alto(evento: string, mob: number): number {
    return Number(this.valor(evento, mob)?.cantidadCreditos || 0) * 100 / this.maximo;
  }
}
