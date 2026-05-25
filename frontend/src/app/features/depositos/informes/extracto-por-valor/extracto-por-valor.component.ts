import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { GeneralApi } from '../../../../shared/general/general.api';

import {
  ExtractoPorValorApi,
  ExtractoPorValorItem,
  ExtractoPorValorResumen
} from './extracto-por-valor.api';

import {
  ExtractoPorValorExporterService
} from './extracto-por-valor-exporter.service';

import { NumericFormatDirective } from '../../../../shared/utils/numeric-format.directive';

@Component({
  selector: 'app-extracto-por-valor',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    NumericFormatDirective
  ],
  templateUrl: './extracto-por-valor.component.html',
  styleUrls: ['./extracto-por-valor.component.scss']
})
export class ExtractoPorValorComponent implements OnInit {

  private readonly api =
    inject(ExtractoPorValorApi);

  private readonly generalApi =
    inject(GeneralApi);

  private readonly exporter =
    inject(ExtractoPorValorExporterService);

  agencias: any[] = [];
  formas: any[] = [];

  filtros = {
    fechaInicial: this.fechaHoy(),
    fechaFinal: this.fechaHoy(),
    idAgencia: 0,
    codigoForma: '0',
    valor: 0,
    tipoBusqueda: 'A'
  };

  cargando = false;
  error = '';

  resumen: ExtractoPorValorResumen | null = null;

  items: ExtractoPorValorItem[] = [];

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

    if (!this.filtros.fechaInicial) {
      this.error = 'Debe seleccionar la fecha inicial.';
      return;
    }

    if (!this.filtros.fechaFinal) {
      this.error = 'Debe seleccionar la fecha final.';
      return;
    }

    if (!this.filtros.valor || this.filtros.valor <= 0) {
      this.error = 'Debe indicar el valor.';
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
          valor: Number(this.filtros.valor || 0),
          tipoBusqueda: this.filtros.tipoBusqueda || 'A'
        });

      this.resumen =
        response.resumen;

      this.items =
        response.items || [];

    } catch (e: any) {

      console.error('Error consultando extracto:', e);

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
      valor: 0,
      tipoBusqueda: 'A'
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
      this.filtros.valor
    );

  }

  private fechaHoy(): string {

    return new Date()
      .toISOString()
      .substring(0, 10);

  }

}
