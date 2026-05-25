import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { DashboardDepositosExporterService }
  from './dashboard-depositos-exporter.service';

import {
  DashboardDepositosApi,
  DashboardDepositosForma,
  DashboardDepositosGrupo,
  DashboardDepositosResumen,
  DashboardDepositosTendencia
} from './dashboard-depositos.api';

@Component({
  selector: 'app-dashboard-depositos',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './dashboard-depositos.component.html',
  styleUrls: ['./dashboard-depositos.component.scss']
})
export class DashboardDepositosComponent {

  fechaCorte = new Date()
    .toISOString()
    .split('T')[0];

  cargando = false;

  error = '';

  fechaCorteAnterior = '';

  resumen: DashboardDepositosResumen | null = null;

  formas: DashboardDepositosForma[] = [];

  agencias: DashboardDepositosGrupo[] = [];

  tendencia: DashboardDepositosTendencia[] = [];

  constructor(
    private api: DashboardDepositosApi,
    private exporter: DashboardDepositosExporterService
  ) {
    this.consultar();
  }

  async consultar(): Promise<void> {

    this.cargando = true;

    this.error = '';

    try {

      const response =
        await this.api.consultar(
          this.fechaCorte
        );

      this.resumen =
        response.resumen;

      this.formas =
        response.formas || [];

      this.agencias =
        response.agencias || [];

      this.tendencia =
        (response.tendencia || [])
          .reverse();

      this.fechaCorteAnterior =
        response.fechaCorteAnterior;

    } catch (err: any) {

      console.error(err);

      this.error =
        err?.error?.message ||
        'No fue posible cargar el dashboard de depósitos.';

    } finally {

      this.cargando = false;

    }
  }

  get maxSaldoTendencia(): number {

    if (!this.tendencia.length) {
      return 1;
    }

    return Math.max(
      ...this.tendencia.map(t =>
        Math.max(
          Number(t.saldoAportes || 0),
          Number(t.saldoDepositos || 0)
        )
      )
    );
  }

  obtenerAlturaAportes(
    valor: number
  ): number {

    return (
      (Number(valor || 0) / this.maxSaldoTendencia) * 220
    );
  }

  obtenerAlturaDepositos(
    valor: number
  ): number {

    return (
      (Number(valor || 0) / this.maxSaldoTendencia) * 220
    );
  }

  formatearValor(
    valor: number
  ): string {

    return new Intl.NumberFormat(
      'es-CO',
      {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }
    ).format(
      Number(valor || 0)
    );
  }

  private obtenerUltimoDiaMesActual(): string {

    const hoy = new Date();

    const ultimoDia = new Date(
      hoy.getFullYear(),
      hoy.getMonth() + 1,
      0
    );

    return ultimoDia
      .toISOString()
      .substring(0, 10);
  }

  exportarExcel(): void {

    this.exporter.exportar(
      this.fechaCorte,
      this.fechaCorteAnterior,
      this.resumen,
      this.formas,
      this.agencias,
      this.tendencia
    );

  }

}
