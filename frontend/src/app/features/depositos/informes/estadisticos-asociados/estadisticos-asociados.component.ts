import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  Chart,
  BarController,
  BarElement,
  CategoryScale,
  Legend,
  LinearScale,
  Tooltip
} from 'chart.js';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { GeneralApi } from '../../../../shared/general/general.api';

import {
  EstadisticosAsociadosApi,
  EstadisticosAsociadosDetalle,
  EstadisticosAsociadosItem,
  EstadisticosAsociadosResumen
} from './estadisticos-asociados.api';

import {
  EstadisticosAsociadosExporterService
} from './estadisticos-asociados-exporter.service';

Chart.register(
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  Tooltip,
  Legend
);

@Component({
  selector: 'app-estadisticos-asociados',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './estadisticos-asociados.component.html',
  styleUrls: ['./estadisticos-asociados.component.scss']
})
export class EstadisticosAsociadosComponent implements OnInit {

  private readonly api = inject(EstadisticosAsociadosApi);
  private readonly generalApi = inject(GeneralApi);
  private readonly exporter = inject(EstadisticosAsociadosExporterService);

  private readonly charts = new Map<string, Chart>();

  agencias: any[] = [];
  formas: any[] = [];

  filtros = {
    fechaCorte: this.fechaHoy(),
    idAgencia: 0,
    codigoForma: '0'
  };

  cargando = false;
  error = '';

  resumen: EstadisticosAsociadosResumen | null = null;
  items: EstadisticosAsociadosItem[] = [];
  detalle: EstadisticosAsociadosDetalle[] = [];

  async ngOnInit(): Promise<void> {
    await this.cargarAgencias();
    await this.cargarFormas();
  }

  async cargarAgencias(): Promise<void> {
    try {
      const agencias =
        await this.generalApi.listarAgencias().toPromise();

      this.agencias = agencias || [];
    } catch (e) {
      console.error(e);
      this.agencias = [];
    }
  }

  async cargarFormas(): Promise<void> {

    try {

      const formas =
        await this.api.listarFormasAhorro();

      this.formas = Array.from(
        new Map(
          (formas || []).map(f => [
            f.codigoForma,
            f
          ])
        ).values()
      ).sort((a: any, b: any) =>
        String(a.codigoForma)
          .localeCompare(String(b.codigoForma))
      );

    } catch (e) {

      console.error(e);

      this.formas = [];

    }

  }

  async buscar(): Promise<void> {
    this.error = '';

    if (!this.filtros.fechaCorte) {
      this.error = 'Debe seleccionar la fecha de corte.';
      return;
    }

    if (this.filtros.idAgencia === null || this.filtros.idAgencia === undefined) {
      this.error = 'Debe seleccionar una agencia.';
      return;
    }

    this.cargando = true;

    try {
      const response =
        await this.api.consultar({
          fechaCorte: this.filtros.fechaCorte,
          idAgencia: Number(this.filtros.idAgencia),
          codigoForma: this.filtros.codigoForma || '0'
        });

      this.resumen = response.resumen;
      this.items = response.items || [];
      this.detalle = response.detalle || [];

      setTimeout(() => {
        this.renderCharts();
      });

    } catch (e: any) {
      console.error(e);

      this.error =
        e?.error?.message ||
        'No fue posible consultar el informe.';

    } finally {
      this.cargando = false;
    }
  }

  limpiar(): void {
    this.filtros = {
      fechaCorte: this.fechaHoy(),
      idAgencia: 0,
      codigoForma: '0'
    };

    this.resumen = null;
    this.items = [];
    this.detalle = [];
    this.error = '';

    this.destroyCharts();
  }

  grupos(): string[] {
    return Array.from(
      new Set(
        this.items.map(i => i.grupo)
      )
    );
  }

  itemsPorGrupo(
    grupo: string
  ): EstadisticosAsociadosItem[] {
    return this.items
      .filter(i => i.grupo === grupo)
      .sort((a, b) => Number(b.cantidad || 0) - Number(a.cantidad || 0));
  }

  totalGrupo(
    grupo: string
  ): number {
    return this.itemsPorGrupo(grupo)
      .reduce(
        (total, item) => total + Number(item.cantidad || 0),
        0
      );
  }

  saldoGrupo(
    grupo: string
  ): number {
    return this.itemsPorGrupo(grupo)
      .reduce(
        (total, item) => total + Number(item.saldoTotal || 0),
        0
      );
  }

  chartId(grupo: string): string {
    return 'chart-' + grupo
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/\s+/g, '-')
      .replace(/[^a-zA-Z0-9-]/g, '')
      .toLowerCase();
  }

  private renderCharts(): void {
    this.destroyCharts();

    for (const grupo of this.grupos()) {
      this.renderChartGrupo(grupo);
    }
  }

  private renderChartGrupo(
    grupo: string
  ): void {
    const canvas =
      document.getElementById(
        this.chartId(grupo)
      ) as HTMLCanvasElement;

    if (!canvas) {
      return;
    }

    const items =
      this.itemsPorGrupo(grupo);

    const chart =
      new Chart(canvas, {
        type: 'bar',
        data: {
          labels: items.map(i => i.categoria),
          datasets: [
            {
              label: 'Cantidad',
              data: items.map(i => Number(i.cantidad || 0)),
              borderWidth: 1,
              borderRadius: 6,
              maxBarThickness: 55
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: false
            },
            tooltip: {
              enabled: true
            }
          },
          scales: {
            x: {
              grid: {
                display: false
              }
            },
            y: {
              beginAtZero: true,
              ticks: {
                precision: 0
              }
            }
          }
        }
      });

    this.charts.set(
      grupo,
      chart
    );
  }

  private destroyCharts(): void {
    this.charts.forEach(chart => chart.destroy());
    this.charts.clear();
  }

  exportar(): void {
    this.exporter.exportarExcel(
      this.resumen,
      this.items,
      this.detalle,
      this.filtros.fechaCorte
    );
  }

  imprimir(): void {
    window.print();
  }

  private fechaHoy(): string {
    return new Date()
      .toISOString()
      .substring(0, 10);
  }

}
