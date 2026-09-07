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
  DashboardDepositosTendencia,
  DashboardDepositosTipoCaptacion
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

  tiposCaptacion: DashboardDepositosTipoCaptacion[] = [];

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

      this.tiposCaptacion =
        response.tiposCaptacion || [];

      this.agencias =
        response.agencias || [];

      this.tendencia =
        (response.tendencia || [])
          .reverse();

      this.fechaCorteAnterior =
        response.fechaCorteAnterior || '';

    } catch (err: any) {

      console.error(err);

      this.error =
        err?.error?.message ||
        'No fue posible cargar el dashboard de depósitos.';

    } finally {

      this.cargando = false;

    }
  }

  // =========================================================
  // GRÁFICA POR FORMA DE AHORRO
  // =========================================================

  get maxSaldoFormas(): number {

    if (!this.formas.length) {
      return 1;
    }

    const maximo = Math.max(
      ...this.formas.map(f =>
        Number(f.saldoActual || 0)
      )
    );

    return maximo > 0
      ? maximo
      : 1;
  }

  obtenerAlturaForma(
    valor: number
  ): number {

    const saldo =
      Math.max(
        Number(valor || 0),
        0
      );

    return (
      saldo /
      this.maxSaldoFormas
    ) * 220;
  }

  // =========================================================
  // GRÁFICA POR TIPO DE CAPTACIÓN
  // =========================================================

  get maxSaldoTiposCaptacion(): number {

    if (!this.tiposCaptacion.length) {
      return 1;
    }

    const maximo = Math.max(
      ...this.tiposCaptacion.map(t =>
        Number(t.saldoActual || 0)
      )
    );

    return maximo > 0
      ? maximo
      : 1;
  }

  obtenerAlturaTipoCaptacion(
    valor: number
  ): number {

    const saldo =
      Math.max(
        Number(valor || 0),
        0
      );

    return (
      saldo /
      this.maxSaldoTiposCaptacion
    ) * 220;
  }

  // =========================================================
  // TENDENCIA HISTÓRICA
  // =========================================================

  get maxSaldoTendencia(): number {

    if (!this.tendencia.length) {
      return 1;
    }

    const maximo = Math.max(
      ...this.tendencia.map(t =>
        Math.max(
          Number(t.saldoAportes || 0),
          Number(t.saldoDepositos || 0)
        )
      )
    );

    return maximo > 0
      ? maximo
      : 1;
  }

  obtenerAlturaAportes(
    valor: number
  ): number {

    return (
      (
        Math.max(
          Number(valor || 0),
          0
        ) /
        this.maxSaldoTendencia
      ) * 220
    );
  }

  obtenerAlturaDepositos(
    valor: number
  ): number {

    return (
      (
        Math.max(
          Number(valor || 0),
          0
        ) /
        this.maxSaldoTendencia
      ) * 220
    );
  }

  // =========================================================
  // FORMATO
  // =========================================================

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

  // =========================================================
  // EXPORTACIÓN
  // =========================================================

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
