import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { GeneralApi } from '../../../../shared/general/general.api';
import { NumericFormatDirective } from '../../../../shared/utils/numeric-format.directive';

import {
  AntiguedadAsociadosApi,
  AntiguedadAsociadosItem,
  AntiguedadAsociadosResumen
} from './antiguedad-asociados.api';

import {
  AntiguedadAsociadosExporterService
} from './antiguedad-asociados-exporter.service';

@Component({
  selector: 'app-antiguedad-asociados',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    NumericFormatDirective
  ],
  templateUrl: './antiguedad-asociados.component.html',
  styleUrls: ['./antiguedad-asociados.component.scss']
})
export class AntiguedadAsociadosComponent implements OnInit {

  private readonly api =
    inject(AntiguedadAsociadosApi);

  private readonly generalApi =
    inject(GeneralApi);

  private readonly exporter =
    inject(AntiguedadAsociadosExporterService);

  agencias: any[] = [];

  filtros = {
    fechaCorte: this.fechaHoy(),
    idAgencia: 0,
    limitePantalla: 20
  };

  cargando = false;
  error = '';

  resumen: AntiguedadAsociadosResumen[] = [];

  mayoresAntiguedad: AntiguedadAsociadosItem[] = [];

  itemsExcel: AntiguedadAsociadosItem[] = [];

  async ngOnInit(): Promise<void> {
    await this.cargarAgencias();
  }

  async cargarAgencias(): Promise<void> {

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
          limitePantalla: limite
        });

      this.resumen =
        response.resumen || [];

      this.mayoresAntiguedad =
        response.mayoresAntiguedad || [];

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
      limitePantalla: 20
    };

    this.resumen = [];

    this.mayoresAntiguedad = [];

    this.itemsExcel = [];

    this.error = '';

  }

  exportar(): void {

    this.exporter.exportarExcel(
      this.resumen,
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
