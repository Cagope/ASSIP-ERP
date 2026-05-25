import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { GeneralApi } from '../../../../shared/general/general.api';

import {
  ResumenTipoMovimientoApi,
  ResumenTipoMovimientoItem,
  ResumenTipoMovimientoResumen
} from './resumen-tipo-movimiento.api';

import {
  ResumenTipoMovimientoExporterService
} from './resumen-tipo-movimiento-exporter.service';

@Component({
  selector: 'app-resumen-tipo-movimiento',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './resumen-tipo-movimiento.component.html',
  styleUrls: ['./resumen-tipo-movimiento.component.scss']
})
export class ResumenTipoMovimientoComponent implements OnInit {

  private readonly api =
    inject(ResumenTipoMovimientoApi);

  private readonly generalApi =
    inject(GeneralApi);

  private readonly exporter =
    inject(ResumenTipoMovimientoExporterService);

  agencias: any[] = [];
  formas: any[] = [];

  filtros = {
    fechaInicial: this.fechaHoy(),
    fechaFinal: this.fechaHoy(),
    idAgencia: 0,
    codigoForma: '0',
    codigoMovimiento: '0'
  };

  cargando = false;
  error = '';

  resumen: ResumenTipoMovimientoResumen | null = null;
  items: ResumenTipoMovimientoItem[] = [];

  resumenDetalle: ResumenTipoMovimientoResumen | null = null;
  itemsDetalle: ResumenTipoMovimientoItem[] = [];

  codigoMovimientoSeleccionado = '';
  nombreMovimientoSeleccionado = '';

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

      console.error('Error cargando formas:', e);
      this.formas = [];

    }

  }

  async buscar(): Promise<void> {

    this.error = '';

    this.itemsDetalle = [];
    this.resumenDetalle = null;

    if (!this.filtros.fechaInicial) {
      this.error = 'Debe seleccionar la fecha inicial.';
      return;
    }

    if (!this.filtros.fechaFinal) {
      this.error = 'Debe seleccionar la fecha final.';
      return;
    }

    this.cargando = true;

    try {

      const response =
        await this.api.consultar({
          fechaInicial: this.filtros.fechaInicial,
          fechaFinal: this.filtros.fechaFinal,
          idAgencia: Number(this.filtros.idAgencia || 0),
          codigoForma: this.filtros.codigoForma || '0',
          codigoMovimiento: '0'
        });

      this.resumen =
        response.resumen;

      this.items =
        response.items || [];

    } catch (e: any) {

      console.error('Error consultando resumen:', e);

      this.error =
        e?.error?.message ||
        'No fue posible consultar el informe.';

    } finally {

      this.cargando = false;

    }

  }

  async verDetalle(
    item: ResumenTipoMovimientoItem
  ): Promise<void> {

    this.codigoMovimientoSeleccionado =
      item.codigoMovimiento;

    this.nombreMovimientoSeleccionado =
      item.nombreMovimiento;

    try {

      const response =
        await this.api.detalle({
          fechaInicial: this.filtros.fechaInicial,
          fechaFinal: this.filtros.fechaFinal,
          idAgencia: Number(this.filtros.idAgencia || 0),
          codigoForma: this.filtros.codigoForma || '0',
          codigoMovimiento: item.codigoMovimiento
        });

      this.resumenDetalle =
        response.resumen;

      this.itemsDetalle =
        response.items || [];

    } catch (e) {

      console.error('Error cargando detalle:', e);

    }

  }

  limpiar(): void {

    this.filtros = {
      fechaInicial: this.fechaHoy(),
      fechaFinal: this.fechaHoy(),
      idAgencia: 0,
      codigoForma: '0',
      codigoMovimiento: '0'
    };

    this.resumen = null;
    this.items = [];

    this.resumenDetalle = null;
    this.itemsDetalle = [];

    this.error = '';

  }

  exportar(): void {

    this.exporter.exportarExcel(
      this.items,
      this.resumen,
      this.filtros.fechaInicial,
      this.filtros.fechaFinal
    );

  }

  private fechaHoy(): string {

    return new Date()
      .toISOString()
      .substring(0, 10);

  }

}
