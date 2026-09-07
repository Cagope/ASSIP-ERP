import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { GeneralApi } from '../../../../shared/general/general.api';

import {
  GmfSemanalApi,
  GmfSemanalItem,
  GmfSemanalResumen
} from './gmf-semanal.api';

import {
  GmfSemanalExporterService
} from './gmf-semanal-exporter.service';

@Component({
  selector: 'app-gmf-semanal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './gmf-semanal.component.html',
  styleUrls: ['./gmf-semanal.component.scss']
})
export class GmfSemanalComponent implements OnInit {

  private readonly api = inject(GmfSemanalApi);
  private readonly generalApi = inject(GeneralApi);
  private readonly exporter = inject(GmfSemanalExporterService);

  agencias: any[] = [];
  formas: any[] = [];

  filtros = {
    fechaInicial: this.fechaHoy(),
    fechaFinal: this.fechaHoy(),
    idAgencia: 0,
    numeroSemana: 1,
    codigoForma: '0'
  };

  cargando = false;
  error = '';

  resumen: GmfSemanalResumen | null = null;
  items: GmfSemanalItem[] = [];

  async ngOnInit(): Promise<void> {
    await this.cargarCatalogos();
  }

  async cargarCatalogos(): Promise<void> {

    try {
      const agencias = await this.generalApi.listarAgencias().toPromise();
      this.agencias = agencias || [];
    } catch (e) {
      console.error('Error cargando agencias:', e);
      this.agencias = [];
    }

    try {
      const formas = await this.api.listarFormasAhorro();

      this.formas = Array.from(
        new Map(
          (formas || []).map(f => [
            f.codigoForma,
            f
          ])
        ).values()
      ).sort((a: any, b: any) =>
        String(a.codigoForma || '')
          .localeCompare(String(b.codigoForma || ''))
      );

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

    if (!this.filtros.numeroSemana || Number(this.filtros.numeroSemana) <= 0) {
      this.error = 'El número de semana debe ser mayor que cero.';
      return;
    }

    this.cargando = true;
    this.resumen = null;
    this.items = [];

    try {

      const response = await this.api.consultar({
        fechaInicial: this.filtros.fechaInicial,
        fechaFinal: this.filtros.fechaFinal,
        idAgencia: Number(this.filtros.idAgencia || 0),
        numeroSemana: Number(this.filtros.numeroSemana),
        codigoForma: this.filtros.codigoForma || '0'
      });

      this.resumen = response.resumen;
      this.items = response.items || [];

    } catch (e: any) {

      console.error('Error consultando informe semanal GMF:', e);

      this.error =
        e?.error?.message ||
        e?.error ||
        'No fue posible consultar el informe semanal GMF.';

    } finally {
      this.cargando = false;
    }
  }

  limpiar(): void {
    this.filtros = {
      fechaInicial: this.fechaHoy(),
      fechaFinal: this.fechaHoy(),
      idAgencia: 0,
      numeroSemana: 1,
      codigoForma: '0'
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
      this.filtros.fechaFinal,
      Number(this.filtros.numeroSemana),
      Number(this.filtros.idAgencia || 0),
      this.filtros.codigoForma || '0'
    );
  }

  private fechaHoy(): string {
    return new Date()
      .toISOString()
      .substring(0, 10);
  }
}
