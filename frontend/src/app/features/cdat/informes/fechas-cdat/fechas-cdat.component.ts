import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { FechasCdatApi } from './fechas-cdat.api';
import { FechasCdatPrintService } from './fechas-cdat-print.service';
import { FechasCdatExporterService } from './fechas-cdat-exporter.service';

import {
  FechasCdatItem,
  FechasCdatRequest,
  FechasCdatResumen,
  FechasCdatTipoInforme
} from './fechas-cdat.models';

@Component({
  selector: 'app-fechas-cdat',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './fechas-cdat.component.html',
  styleUrls: ['./fechas-cdat.component.scss']
})
export class FechasCdatComponent {

  fechaInicial =
    new Date().toISOString().substring(0, 10);

  fechaFinal =
    new Date().toISOString().substring(0, 10);

  tipoInforme: FechasCdatTipoInforme = 'NUEVOS';

  cargando = false;
  error = '';

  resumen: FechasCdatResumen = {
    cantidad: 0,
    valorTotal: 0,
    promedioTasa: 0,
    promedioPlazo: 0
  };

  resultados: FechasCdatItem[] = [];

  constructor(
    private api: FechasCdatApi,
    private printService: FechasCdatPrintService,
    private exporterService: FechasCdatExporterService
  ) {
  }

  consultar(): void {

    this.error = '';

    if (!this.fechaInicial || !this.fechaFinal) {
      this.error = 'Debe seleccionar fecha inicial y fecha final.';
      return;
    }

    if (this.fechaInicial > this.fechaFinal) {
      this.error = 'La fecha inicial no puede ser mayor que la fecha final.';
      return;
    }

    const request: FechasCdatRequest = {
      tipoInforme: this.tipoInforme,
      fechaInicial: this.fechaInicial,
      fechaFinal: this.fechaFinal
    };

    this.cargando = true;
    this.resultados = [];
    this.limpiarResumen();

    this.api.consultar(request).subscribe({
      next: response => {
        this.resumen = response.resumen;
        this.resultados = response.resultados;
        this.cargando = false;
      },
      error: err => {
        console.error(err);
        this.error = 'No fue posible consultar el informe por fechas CDAT.';
        this.cargando = false;
      }
    });
  }

  imprimir(): void {

    this.error = '';

    if (!this.resultados.length) {
      this.error = 'No hay información para imprimir.';
      return;
    }

    this.printService.imprimir(
      this.tipoInforme,
      this.fechaInicial,
      this.fechaFinal,
      this.resumen,
      this.resultados
    );
  }

  exportarExcel(): void {

    this.error = '';

    if (!this.resultados.length) {
      this.error = 'No hay información para exportar.';
      return;
    }

    this.exporterService.exportar(
      this.tipoInforme,
      this.fechaInicial,
      this.fechaFinal,
      this.resumen,
      this.resultados
    );
  }

  volver(): void {
    history.back();
  }

  private limpiarResumen(): void {
    this.resumen = {
      cantidad: 0,
      valorTotal: 0,
      promedioTasa: 0,
      promedioPlazo: 0
    };
  }

}
