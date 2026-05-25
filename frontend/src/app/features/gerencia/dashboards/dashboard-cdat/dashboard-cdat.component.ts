import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { DashboardCdatApi } from './dashboard-cdat.api';
import { DashboardCdatLineChartComponent } from './dashboard-cdat-line-chart.component';
import { DashboardCdatDonutChartComponent } from './dashboard-cdat-donut-chart.component';

import {
  DashboardCdatAgencia,
  DashboardCdatGrupo,
  DashboardCdatResumen,
  DashboardCdatTendencia,
  DashboardCdatVencimiento
} from './dashboard-cdat.models';

@Component({
  selector: 'app-dashboard-cdat',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    DashboardCdatLineChartComponent,
    DashboardCdatDonutChartComponent
  ],
  templateUrl: './dashboard-cdat.component.html',
  styleUrls: ['./dashboard-cdat.component.scss']
})
export class DashboardCdatComponent implements OnInit {

  fechaCorte =
    new Date().toISOString().substring(0, 10);

  cargando = false;
  error = '';

  resumen: DashboardCdatResumen = {
    totalCdats: 0,
    valorTotalCaptado: 0,
    promedioTasa: 0,
    promedioPlazo: 0,
    vencen30Dias: 0,
    renovacionesMes: 0,
    cancelacionesMes: 0
  };

  agencias: DashboardCdatAgencia[] = [];
  plazos: DashboardCdatGrupo[] = [];
  tasas: DashboardCdatGrupo[] = [];
  tendencia: DashboardCdatTendencia[] = [];
  vencimientos: DashboardCdatVencimiento[] = [];

  constructor(
    private api: DashboardCdatApi
  ) {
  }

  ngOnInit(): void {
    this.consultar();
  }

  consultar(): void {

    this.error = '';

    if (!this.fechaCorte) {
      this.error = 'Debe seleccionar una fecha de corte.';
      return;
    }

    this.cargando = true;

    this.api.consultar({
      fechaCorte: this.fechaCorte
    }).subscribe({
      next: response => {
        this.resumen = response.resumen;
        this.agencias = response.agencias;
        this.plazos = response.plazos;
        this.tasas = response.tasas;
        this.tendencia = response.tendencia;
        this.cargando = false;
        this.vencimientos = response.vencimientos;
      },
      error: err => {
        console.error(err);
        this.error = 'No fue posible consultar el dashboard CDAT.';
        this.cargando = false;
      }
    });
  }

  volver(): void {
    history.back();
  }

}
