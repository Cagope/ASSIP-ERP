import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { GeneralApi } from '../../../../shared/general/general.api';

import {
  MovimientosDiariosApi,
  MovimientosDiariosItem,
  MovimientosDiariosResumen
} from './movimientos-diarios.api';

import {
  MovimientosDiariosExporterService
} from './movimientos-diarios-exporter.service';

@Component({
  selector: 'app-movimientos-diarios',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './movimientos-diarios.component.html',
  styleUrls: ['./movimientos-diarios.component.scss']
})
export class MovimientosDiariosComponent implements OnInit {

  private readonly api =
    inject(MovimientosDiariosApi);

  private readonly generalApi =
    inject(GeneralApi);

  private readonly exporter =
    inject(MovimientosDiariosExporterService);

  agencias: any[] = [];
  formas: any[] = [];
  tiposMovimiento: any[] = [];

  filtros = {
    fechaInicial: this.fechaHoy(),
    fechaFinal: this.fechaHoy(),
    idAgencia: 0,
    codigoForma: '0',
    codigoMovimiento: '0'
  };

  cargando = false;
  error = '';

  resumen: MovimientosDiariosResumen | null = null;

  items: MovimientosDiariosItem[] = [];

  // ======================================================
  // AGRUPADO
  // ======================================================

  gruposAgencia: any[] = [];

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

    try {

      const tipos =
        await this.api.listarTiposMovimiento();

      this.tiposMovimiento =
        tipos || [];

    } catch (e) {

      console.error('Error cargando tipos de movimiento:', e);
      this.tiposMovimiento = [];

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
          codigoForma: this.filtros.codigoForma || '0',
          codigoMovimiento: this.filtros.codigoMovimiento || '0'
        });

      this.resumen =
        response.resumen;

      this.items =
        response.items || [];

      this.agruparDatos();

    } catch (e: any) {

      console.error('Error consultando movimientos diarios:', e);

      this.error =
        e?.error?.message ||
        'No fue posible consultar el informe.';

    } finally {

      this.cargando = false;

    }

  }

  private agruparDatos(): void {

    const agenciasMap =
      new Map<string, any>();

    for (const item of this.items) {

      const keyAgencia =
        `${item.codigoAgencia}-${item.nombreAgencia}`;

      if (!agenciasMap.has(keyAgencia)) {

        agenciasMap.set(keyAgencia, {
          codigoAgencia: item.codigoAgencia,
          nombreAgencia: item.nombreAgencia,
          fechasMap: new Map<string, any>()
        });

      }

      const grupoAgencia =
        agenciasMap.get(keyAgencia);

      const fecha =
        item.fechaMovimiento;

      if (!grupoAgencia.fechasMap.has(fecha)) {

        grupoAgencia.fechasMap.set(fecha, {
          fecha,
          formasMap: new Map<string, any>()
        });

      }

      const grupoFecha =
        grupoAgencia.fechasMap.get(fecha);

      const keyForma =
        `${item.codigoForma}-${item.nombreForma}`;

      if (!grupoFecha.formasMap.has(keyForma)) {

        grupoFecha.formasMap.set(keyForma, {
          codigoForma: item.codigoForma,
          nombreForma: item.nombreForma,
          items: []
        });

      }

      grupoFecha.formasMap
        .get(keyForma)
        .items
        .push(item);

    }

    this.gruposAgencia =
      Array.from(agenciasMap.values())
        .map(a => ({
          ...a,
          fechas: Array.from(a.fechasMap.values())
            .map((f: any) => ({
              ...f,
              formas: Array.from(f.formasMap.values())
            }))
        }));

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
    this.gruposAgencia = [];
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
