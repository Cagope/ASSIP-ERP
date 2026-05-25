import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  Component,
  OnInit,
  inject
} from '@angular/core';

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
import { NumericFormatDirective } from '../../../../shared/utils/numeric-format.directive';

import {
  SaldosRangosEdadApi,
  SaldosRangosEdadItem,
  SaldosRangosEdadRango,
  SaldosRangosEdadResumen
} from './saldos-rangos-edad.api';

import {
  SaldosRangosEdadExporterService
} from './saldos-rangos-edad-exporter.service';

Chart.register(
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  Tooltip,
  Legend
);

@Component({
  selector: 'app-saldos-rangos-edad',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    NumericFormatDirective
  ],
  templateUrl: './saldos-rangos-edad.component.html',
  styleUrls: ['./saldos-rangos-edad.component.scss']
})
export class SaldosRangosEdadComponent
  implements OnInit, AfterViewInit {

  private readonly api =
    inject(SaldosRangosEdadApi);

  private readonly generalApi =
    inject(GeneralApi);

  private readonly exporter =
    inject(SaldosRangosEdadExporterService);

  private chart: Chart | null = null;

  agencias: any[] = [];

  filtros = {
    fechaCorte: this.fechaHoy(),
    idAgencia: 0
  };

  rangos: SaldosRangosEdadRango[] = [
    {
      nombreRango: 'Menores edad',
      edadInicial: 0,
      edadFinal: 17
    },
    {
      nombreRango: 'Jóvenes',
      edadInicial: 18,
      edadFinal: 30
    },
    {
      nombreRango: 'Adultos',
      edadInicial: 31,
      edadFinal: 45
    },
    {
      nombreRango: 'Senior',
      edadInicial: 46,
      edadFinal: 60
    },
    {
      nombreRango: 'Mayores',
      edadInicial: 61,
      edadFinal: 120
    }
  ];

  cargando = false;
  error = '';

  resumen: SaldosRangosEdadResumen[] = [];
  itemsExcel: SaldosRangosEdadItem[] = [];

  async ngOnInit(): Promise<void> {
    await this.cargarAgencias();
  }

  ngAfterViewInit(): void {
  }

  async cargarAgencias(): Promise<void> {

    try {

      const agencias =
        await this.generalApi.listarAgencias().toPromise();

      this.agencias =
        agencias || [];

    } catch (e) {

      console.error(e);

      this.agencias = [];

    }

  }

  async buscar(): Promise<void> {

    this.error = '';

    if (!this.filtros.fechaCorte) {
      this.error = 'Debe seleccionar la fecha de corte.';
      return;
    }

    if (!this.filtros.idAgencia || Number(this.filtros.idAgencia) === 0) {
      this.error = 'Debe seleccionar una agencia.';
      return;
    }

    if (!this.validarRangos()) {
      return;
    }

    this.cargando = true;

    try {

      const response =
        await this.api.consultar({
          fechaCorte: this.filtros.fechaCorte,
          idAgencia: Number(this.filtros.idAgencia),
          rangos: this.rangos.map(r => ({
            nombreRango: r.nombreRango,
            edadInicial: Number(String(r.edadInicial || 0).replace(/,/g, '')),
            edadFinal: Number(String(r.edadFinal || 0).replace(/,/g, ''))
          }))
        });

      this.resumen =
        response.resumen || [];

      this.itemsExcel =
        response.itemsExcel || [];

      setTimeout(() => {
        this.renderChart();
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

  private renderChart(): void {

    const canvas =
      document.getElementById(
        'chartRangosEdad'
      ) as HTMLCanvasElement;

    if (!canvas) {
      return;
    }

    if (this.chart) {
      this.chart.destroy();
    }

    this.chart = new Chart(canvas, {

      type: 'bar',

      data: {

        labels:
          this.resumen.map(r => `${r.edadInicial} - ${r.edadFinal}`),

        datasets: [
          {
            label: 'Saldo total aportes',
            data: this.resumen.map(r => Number(r.saldoTotalAportes || 0)),
            borderWidth: 1,
            borderRadius: 6,
            maxBarThickness: 70
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
            enabled: true,
            callbacks: {
              label: context => {
                const value = Number(context.raw || 0);
                return `Saldo: ${value.toLocaleString('es-CO')}`;
              }
            }
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
              callback: value => Number(value).toLocaleString('es-CO')
            }
          }
        }
      }

    });

  }

  validarRangos(): boolean {

    if (!this.rangos || this.rangos.length !== 5) {
      this.error = 'Debe configurar exactamente 5 rangos.';
      return false;
    }

    for (const rango of this.rangos) {

      const inicial =
        Number(String(rango.edadInicial || 0).replace(/,/g, ''));

      const final =
        Number(String(rango.edadFinal || 0).replace(/,/g, ''));

      if (!rango.nombreRango || !rango.nombreRango.trim()) {
        this.error = 'Todos los rangos deben tener nombre.';
        return false;
      }

      if (inicial < 0) {
        this.error = 'La edad inicial no puede ser negativa.';
        return false;
      }

      if (final < inicial) {
        this.error = 'La edad final no puede ser menor que la edad inicial.';
        return false;
      }

    }

    return true;

  }

  limpiar(): void {

    this.filtros = {
      fechaCorte: this.fechaHoy(),
      idAgencia: 0
    };

    this.rangos = [
      {
        nombreRango: 'Menores edad',
        edadInicial: 0,
        edadFinal: 17
      },
      {
        nombreRango: 'Jóvenes',
        edadInicial: 18,
        edadFinal: 30
      },
      {
        nombreRango: 'Adultos',
        edadInicial: 31,
        edadFinal: 45
      },
      {
        nombreRango: 'Senior',
        edadInicial: 46,
        edadFinal: 60
      },
      {
        nombreRango: 'Mayores',
        edadInicial: 61,
        edadFinal: 120
      }
    ];

    this.resumen = [];
    this.itemsExcel = [];
    this.error = '';

    if (this.chart) {
      this.chart.destroy();
      this.chart = null;
    }

  }

  totalAsociados(): number {

    return this.resumen.reduce(
      (total, item) =>
        total + Number(item.cantidadAsociados || 0),
      0
    );

  }

  totalAportes(): number {

    return this.resumen.reduce(
      (total, item) =>
        total + Number(item.saldoTotalAportes || 0),
      0
    );

  }

  exportar(): void {

    this.exporter.exportarExcel(
      this.resumen,
      this.itemsExcel,
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
