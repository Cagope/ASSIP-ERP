import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DashboardCdatApi } from './dashboard-cdat.api';

import {
  DashboardCdatAgencia,
  DashboardCdatGrupo,
  DashboardCdatResumen,
  DashboardCdatTendencia,
  DashboardCdatVencimiento,
  DashboardCdatVencimientoDetalle
} from './dashboard-cdat.models';

@Component({
  selector: 'app-dashboard-cdat',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './dashboard-cdat.component.html',
  styleUrls: ['./dashboard-cdat.component.scss']
})
export class DashboardCdatComponent implements OnInit {

  cargando = false;
  cargandoDetalle = false;

  error = '';

  fechaActual = new Date();

  resumen: DashboardCdatResumen = {
    totalCdats: 0,
    valorTotalCaptado: 0,
    promedioTasa: 0,
    promedioPlazo: 0,
    vencen30Dias: 0,
    totalAsociados: 0
  };

  agencias: DashboardCdatAgencia[] = [];
  plazos: DashboardCdatGrupo[] = [];
  tasas: DashboardCdatGrupo[] = [];
  vencimientos: DashboardCdatVencimiento[] = [];
  tendencia: DashboardCdatTendencia[] = [];

  rangoSeleccionado = '';
  detalleVencimientos: DashboardCdatVencimientoDetalle[] = [];

  constructor(
    private api: DashboardCdatApi
  ) {
  }

  ngOnInit(): void {
    this.consultar();
  }

  consultar(): void {

    this.error = '';
    this.cargando = true;

    this.api.consultar().subscribe({
      next: response => {

        this.resumen = response.resumen;

        this.agencias =
          response.agencias ?? [];

        this.plazos =
          response.plazos ?? [];

        this.tasas =
          response.tasas ?? [];

        this.vencimientos =
          response.vencimientos ?? [];

        this.tendencia =
          response.tendencia ?? [];

        this.cargando = false;
      },

      error: err => {

        console.error(err);

        this.error =
          'No fue posible consultar el dashboard CDAT.';

        this.cargando = false;
      }
    });
  }

  // =========================================================
  // GRÁFICA COMPARATIVA 12 MESES
  // =========================================================

  alturaValor(item: DashboardCdatTendencia): number {

    const maximo = Math.max(
      ...this.tendencia.map(x => x.valorCaptado),
      0
    );

    if (maximo <= 0) {
      return 0;
    }

    return (item.valorCaptado / maximo) * 100;
  }

  alturaCantidad(item: DashboardCdatTendencia): number {

    const maximo = Math.max(
      ...this.tendencia.map(x => x.cantidadCdats),
      0
    );

    if (maximo <= 0) {
      return 0;
    }

    return (item.cantidadCdats / maximo) * 100;
  }

  periodoGrafica(periodo: string): string {

    if (!periodo) {
      return '';
    }

    const partes = periodo.split('-');

    if (partes.length !== 2) {
      return periodo;
    }

    const meses = [
      'Ene',
      'Feb',
      'Mar',
      'Abr',
      'May',
      'Jun',
      'Jul',
      'Ago',
      'Sep',
      'Oct',
      'Nov',
      'Dic'
    ];

    const anio = Number(partes[0]);
    const mes = Number(partes[1]);

    if (
      !Number.isFinite(anio) ||
      !Number.isFinite(mes) ||
      mes < 1 ||
      mes > 12
    ) {
      return periodo;
    }

    return `${meses[mes - 1]} ${String(anio).slice(-2)}`;
  }

  // =========================================================
  // DETALLE DE VENCIMIENTOS
  // =========================================================

  verDetalleVencimiento(
    vencimiento: DashboardCdatVencimiento
  ): void {

    if (!vencimiento?.rango) {
      return;
    }

    this.rangoSeleccionado =
      vencimiento.rango;

    this.detalleVencimientos = [];

    this.cargandoDetalle = true;

    this.api.consultarDetalleVencimientos(
      vencimiento.rango
    ).subscribe({

      next: detalle => {

        this.detalleVencimientos =
          detalle ?? [];

        this.cargandoDetalle = false;
      },

      error: err => {

        console.error(err);

        this.error =
          'No fue posible consultar el detalle de vencimientos.';

        this.cargandoDetalle = false;
      }
    });
  }

  cerrarDetalleVencimiento(): void {

    this.rangoSeleccionado = '';

    this.detalleVencimientos = [];
  }

  // =========================================================
  // NAVEGACIÓN
  // =========================================================

  volver(): void {
    history.back();
  }
}
