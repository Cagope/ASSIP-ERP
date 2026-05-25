import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { GeneralApi } from '../../../../shared/general/general.api';

import {
  CumpleaniosAsociadosApi,
  CumpleaniosAsociadosItem,
  CumpleaniosAsociadosResumen
} from './cumpleanios-asociados.api';

import {
  CumpleaniosAsociadosExporterService
} from './cumpleanios-asociados-exporter.service';

@Component({
  selector: 'app-cumpleanios-asociados',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './cumpleanios-asociados.component.html',
  styleUrls: ['./cumpleanios-asociados.component.scss']
})
export class CumpleaniosAsociadosComponent implements OnInit {

  private readonly api = inject(CumpleaniosAsociadosApi);
  private readonly generalApi = inject(GeneralApi);
  private readonly exporter = inject(CumpleaniosAsociadosExporterService);

  agencias: any[] = [];

  filtros = {
    fechaInicial: this.primerDiaMes(),
    fechaFinal: this.ultimoDiaMes(),
    idAgencia: 0
  };

  cargando = false;
  error = '';

  resumen: CumpleaniosAsociadosResumen | null = null;
  items: CumpleaniosAsociadosItem[] = [];

  async ngOnInit(): Promise<void> {
    await this.cargarAgencias();
  }

  async cargarAgencias(): Promise<void> {
    try {
      const agencias =
        await this.generalApi.listarAgencias().toPromise();

      this.agencias = agencias || [];
    } catch (e) {
      console.error(e);
      this.agencias = [];
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

    if (!this.filtros.idAgencia || Number(this.filtros.idAgencia) === 0) {
      this.error = 'Debe seleccionar una agencia.';
      return;
    }

    this.cargando = true;

    try {
      const response =
        await this.api.consultar({
          fechaInicial: this.filtros.fechaInicial,
          fechaFinal: this.filtros.fechaFinal,
          idAgencia: Number(this.filtros.idAgencia)
        });

      this.resumen = response.resumen;
      this.items = response.items || [];

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
      fechaInicial: this.primerDiaMes(),
      fechaFinal: this.ultimoDiaMes(),
      idAgencia: 0
    };

    this.resumen = null;
    this.items = [];
    this.error = '';
  }

  exportar(): void {
    this.exporter.exportarExcel(
      this.resumen,
      this.items,
      this.filtros.fechaInicial,
      this.filtros.fechaFinal
    );
  }

  imprimir(): void {
    window.print();
  }

  private primerDiaMes(): string {
    const hoy = new Date();

    return new Date(
      hoy.getFullYear(),
      hoy.getMonth(),
      1
    ).toISOString().substring(0, 10);
  }

  private ultimoDiaMes(): string {
    const hoy = new Date();

    return new Date(
      hoy.getFullYear(),
      hoy.getMonth() + 1,
      0
    ).toISOString().substring(0, 10);
  }

}
