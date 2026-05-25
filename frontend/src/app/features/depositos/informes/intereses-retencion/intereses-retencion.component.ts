import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { GeneralApi } from '../../../../shared/general/general.api';

import {
  InteresesRetencionApi,
  InteresesRetencionItem,
  InteresesRetencionResumen
} from './intereses-retencion.api';

import {
  InteresesRetencionExporterService
} from './intereses-retencion-exporter.service';

@Component({
  selector: 'app-intereses-retencion',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './intereses-retencion.component.html',
  styleUrls: ['./intereses-retencion.component.scss']
})
export class InteresesRetencionComponent implements OnInit {

  private readonly api =
    inject(InteresesRetencionApi);

  private readonly generalApi =
    inject(GeneralApi);

  private readonly exporter =
    inject(InteresesRetencionExporterService);

  agencias: any[] = [];
  formas: any[] = [];

  filtros = {
    fechaInicial: this.fechaHoy(),
    fechaFinal: this.fechaHoy(),
    idAgencia: 0,
    codigoForma: '0',
    codigoCuenta: ''
  };

  cargando = false;
  error = '';

  resumen: InteresesRetencionResumen | null = null;

  items: InteresesRetencionItem[] = [];

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

    this.cargando = true;

    try {

      const response =
        await this.api.consultar({
          fechaInicial: this.filtros.fechaInicial,
          fechaFinal: this.filtros.fechaFinal,
          idAgencia: Number(this.filtros.idAgencia || 0),
          codigoForma: this.filtros.codigoForma || '0',
          codigoCuenta: this.filtros.codigoCuenta || ''
        });

      this.resumen =
        response.resumen;

      this.items =
        response.items || [];

    } catch (e: any) {

      console.error('Error consultando informe:', e);

      this.error =
        e?.error?.message ||
        'No fue posible consultar el informe.';

    } finally {

      this.cargando = false;

    }

  }

  limpiar(): void {

    this.filtros = {
      fechaInicial: this.fechaHoy(),
      fechaFinal: this.fechaHoy(),
      idAgencia: 0,
      codigoForma: '0',
      codigoCuenta: ''
    };

    this.resumen = null;
    this.items = [];
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
