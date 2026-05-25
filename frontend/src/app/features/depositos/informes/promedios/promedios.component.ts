import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { GeneralApi } from '../../../../shared/general/general.api';
import { NumericFormatDirective } from '../../../../shared/utils/numeric-format.directive';

import {
  PromediosApi,
  PromediosForma,
  PromediosItem,
  PromediosResumen
} from './promedios.api';

import {
  PromediosExporterService
} from './promedios-exporter.service';

@Component({
  selector: 'app-promedios',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    NumericFormatDirective
  ],
  templateUrl: './promedios.component.html',
  styleUrls: ['./promedios.component.scss']
})
export class PromediosComponent implements OnInit {

  private readonly api =
    inject(PromediosApi);

  private readonly generalApi =
    inject(GeneralApi);

  private readonly exporter =
    inject(PromediosExporterService);

  agencias: any[] = [];
  formasAhorro: any[] = [];

  filtros = {
    fechaCorte: this.fechaHoy(),
    idAgencia: 0,
    codigoForma: '0',
    limitePantalla: 20
  };

  cargando = false;
  error = '';

  resumen: PromediosResumen | null = null;

  formas: PromediosForma[] = [];

  mayores: PromediosItem[] = [];
  menores: PromediosItem[] = [];

  itemsExcel: PromediosItem[] = [];

  async ngOnInit(): Promise<void> {
    await this.cargarCatalogos();
  }

  async cargarCatalogos(): Promise<void> {

    try {

      const agencias =
        await this.generalApi
          .listarAgencias()
          .toPromise();

      this.agencias =
        agencias || [];

    } catch (e) {

      console.error(e);

    }

    try {

      const formas =
        await this.api.listarFormasAhorro();

      this.formasAhorro = Array.from(
        new Map(
          (formas || []).map((f: any) => [
            f.codigoForma,
            f
          ])
        ).values()
      ).sort((a: any, b: any) =>
        String(a.codigoForma)
          .localeCompare(String(b.codigoForma))
      );

    } catch (e) {

      console.error(e);

    }

  }

  async buscar(): Promise<void> {

    this.error = '';

    const limite =
      Number(
        String(this.filtros.limitePantalla || 20)
          .replace(/,/g, '')
      );

    if (!this.filtros.fechaCorte) {
      this.error = 'Debe seleccionar la fecha de corte.';
      return;
    }

    if (!this.filtros.idAgencia) {
      this.error = 'Debe seleccionar una agencia.';
      return;
    }

    if (!limite || limite <= 0) {
      this.error = 'Debe indicar un límite válido.';
      return;
    }

    if (limite > 100) {
      this.error = 'El límite no puede ser mayor a 100.';
      return;
    }

    this.cargando = true;

    try {

      const response =
        await this.api.consultar({
          fechaCorte: this.filtros.fechaCorte,
          idAgencia: Number(this.filtros.idAgencia),
          codigoForma: this.filtros.codigoForma || '0',
          limitePantalla: limite
        });

      this.resumen =
        response.resumen;

      this.formas =
        response.formas || [];

      this.mayores =
        response.mayores || [];

      this.menores =
        response.menores || [];

      this.itemsExcel =
        response.itemsExcel || [];

    } catch (e: any) {

      console.error(e);

      this.error =
        e?.error?.message ||
        'No fue posible consultar el informe.';

    } finally {

      this.cargando = false;

    }

  }

  limpiar(): void {

    this.filtros = {
      fechaCorte: this.fechaHoy(),
      idAgencia: 0,
      codigoForma: '0',
      limitePantalla: 20
    };

    this.resumen = null;

    this.formas = [];

    this.mayores = [];
    this.menores = [];

    this.itemsExcel = [];

    this.error = '';

  }

  exportar(): void {

    this.exporter.exportarExcel(
      this.resumen,
      this.formas,
      this.itemsExcel,
      this.filtros.fechaCorte
    );

  }

  imprimir(): void {
    window.print();
  }

  private fechaHoy(): string {

    return new Date()
      .toISOString()
      .substring(0, 10);

  }

}
