import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { GeneralApi } from '../../../../shared/general/general.api';

import {
  EntradasSalidasApi,
  EntradasSalidasItem,
  EntradasSalidasResumen
} from './entradas-salidas.api';

import {
  EntradasSalidasExporterService
} from './entradas-salidas-exporter.service';

@Component({
  selector: 'app-entradas-salidas',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './entradas-salidas.component.html',
  styleUrls: ['./entradas-salidas.component.scss']
})
export class EntradasSalidasComponent implements OnInit {

  private readonly api =
    inject(EntradasSalidasApi);

  private readonly generalApi =
    inject(GeneralApi);

  private readonly exporter =
    inject(EntradasSalidasExporterService);

  agencias: any[] = [];
  formas: any[] = [];

  filtros = {
    fechaInicial: this.primerDiaMes(),
    fechaFinal: this.fechaHoy(),
    idAgencia: 0,
    codigoForma: '0'
  };

  cargando = false;
  error = '';

  resumen: EntradasSalidasResumen | null = null;
  items: EntradasSalidasItem[] = [];

  fechaSeleccionada = '';
  cargandoFormas = false;
  itemsFormas: EntradasSalidasItem[] = [];

  formaSeleccionada = '';
  itemsDetalle: EntradasSalidasItem[] = [];
  resumenDetalle: EntradasSalidasResumen | null = null;
  cargandoDetalle = false;

  async ngOnInit(): Promise<void> {

    await this.cargarCatalogos();

  }

  async cargarCatalogos(): Promise<void> {

    try {

      const agencias =
        await this.generalApi.listarAgencias().toPromise();

      this.agencias =
        agencias || [];

    } catch (e) {

      console.error('Error cargando agencias:', e);
      this.agencias = [];

    }

    try {

      const formas =
        await this.api.listarFormasAhorro();

      this.formas =
        formas || [];

    } catch (e) {

      console.error('Error cargando formas de ahorro:', e);
      this.formas = [];

    }
  }

  async buscar(): Promise<void> {

    this.error = '';

    if (!this.filtros.fechaInicial) {
      this.error = 'Debe seleccionar la fecha inicial.';
      return;
    }

    if (!this.filtros.fechaFinal) {
      this.error = 'Debe seleccionar la fecha final.';
      return;
    }

    if (this.filtros.fechaInicial > this.filtros.fechaFinal) {
      this.error = 'La fecha inicial no puede ser mayor que la fecha final.';
      return;
    }

    this.cargando = true;

    try {

      const response =
        await this.api.consultar({
          fechaInicial: this.filtros.fechaInicial,
          fechaFinal: this.filtros.fechaFinal,
          idAgencia: Number(this.filtros.idAgencia || 0),
          codigoForma: this.filtros.codigoForma || '0'
        });

      this.resumen =
        response.resumen;

      this.items =
        response.items || [];

      this.fechaSeleccionada = '';
      this.itemsFormas = [];
      this.formaSeleccionada = '';
      this.itemsDetalle = [];
      this.resumenDetalle = null;

    } catch (e: any) {

      console.error('Error consultando entradas y salidas:', e);

      this.error =
        e?.error?.message ||
        'No fue posible consultar el informe.';

    } finally {

      this.cargando = false;

    }
  }

  limpiar(): void {

    this.filtros = {
      fechaInicial: this.primerDiaMes(),
      fechaFinal: this.fechaHoy(),
      idAgencia: 0,
      codigoForma: '0'
    };

    this.resumen = null;
    this.items = [];
    this.fechaSeleccionada = '';
    this.itemsFormas = [];
    this.formaSeleccionada = '';
    this.itemsDetalle = [];
    this.resumenDetalle = null;
    this.error = '';

  }

  async verResumenForma(
    item: EntradasSalidasItem
  ): Promise<void> {

    this.error = '';
    this.cargandoFormas = true;

    this.fechaSeleccionada =
      item.fechaMovimiento;

    try {

      const response =
        await this.api.resumenPorForma({
          fechaInicial: item.fechaMovimiento,
          fechaFinal: item.fechaMovimiento,
          idAgencia: Number(this.filtros.idAgencia || 0),
          codigoForma: this.filtros.codigoForma || '0'
        });

      this.itemsFormas =
        response.items || [];

    } catch (e: any) {

      console.error('Error consultando resumen por forma:', e);

      this.error =
        e?.error?.message ||
        'No fue posible consultar el resumen por forma.';

    } finally {

      this.cargandoFormas = false;

    }

  }

  async verDetalleForma(
    item: EntradasSalidasItem
  ): Promise<void> {

    this.error = '';
    this.cargandoDetalle = true;

    this.formaSeleccionada =
      `${item.codigoForma} - ${item.nombreForma}`;

    try {

      const response =
        await this.api.detalleMovimientos({
          fechaInicial: this.fechaSeleccionada,
          fechaFinal: this.fechaSeleccionada,
          idAgencia: Number(this.filtros.idAgencia || 0),
          codigoForma: item.codigoForma || '0'
        });

      this.itemsDetalle =
        response.items || [];
      this.resumenDetalle =
        response.resumen;

    } catch (e: any) {

      console.error('Error consultando detalle:', e);

      this.error =
        e?.error?.message ||
        'No fue posible consultar el detalle.';

    } finally {

      this.cargandoDetalle = false;

    }

  }

  exportar(): void {

    this.exporter.exportarExcel(
      this.items,
      this.resumen,
      this.filtros.fechaInicial,
      this.filtros.fechaFinal,
      this.itemsFormas,
      this.itemsDetalle,
      this.fechaSeleccionada,
      this.formaSeleccionada,
      this.resumenDetalle
    );

  }

  private fechaHoy(): string {

    return new Date()
      .toISOString()
      .substring(0, 10);

  }

  private primerDiaMes(): string {

    const hoy =
      new Date();

    const primerDia =
      new Date(
        hoy.getFullYear(),
        hoy.getMonth(),
        1
      );

    return primerDia
      .toISOString()
      .substring(0, 10);

  }

}
