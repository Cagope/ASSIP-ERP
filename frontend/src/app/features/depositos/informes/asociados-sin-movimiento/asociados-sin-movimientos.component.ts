import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { GeneralApi } from '../../../../shared/general/general.api';
import { NumericFormatDirective } from '../../../../shared/utils/numeric-format.directive';

import {
  AsociadosSinMovimientosApi,
  AsociadosSinMovimientosItem,
  AsociadosSinMovimientosResumen
} from './asociados-sin-movimientos.api';

import {
  AsociadosSinMovimientosExporterService
} from './asociados-sin-movimientos-exporter.service';

@Component({
  selector: 'app-asociados-sin-movimientos',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    NumericFormatDirective
  ],
  templateUrl: './asociados-sin-movimientos.component.html',
  styleUrls: ['./asociados-sin-movimientos.component.scss']
})
export class AsociadosSinMovimientosComponent {

  private readonly api =
    inject(AsociadosSinMovimientosApi);

  private readonly generalApi =
    inject(GeneralApi);

  private readonly exporter =
    inject(AsociadosSinMovimientosExporterService);

  agencias: any[] = [];
  formas: any[] = [];

  filtros = {
    fechaCorte: this.fechaHoy(),
    idAgencia: 0,
    codigoForma: '0',
    diasMinimos: 90,
    tipoSaldo: 0,
    saldoMinimo: 0
  };

  cargando = false;
  error = '';

  resumen: AsociadosSinMovimientosResumen | null = null;
  items: AsociadosSinMovimientosItem[] = [];

  constructor() {
    this.cargarCatalogos();
  }

  async cargarCatalogos(): Promise<void> {

    try {
      this.agencias =
        await this.generalApi.listarAgencias().toPromise() || [];
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

  async consultar(): Promise<void> {

    this.error = '';

    if (!this.filtros.fechaCorte) {
      this.error = 'Debe seleccionar la fecha de corte.';
      return;
    }

    if (this.filtros.diasMinimos < 0) {
      this.error = 'Los días mínimos no pueden ser negativos.';
      return;
    }

    this.cargando = true;

    try {

      const response =
        await this.api.consultar({
          fechaCorte: this.filtros.fechaCorte,
          idAgencia: Number(this.filtros.idAgencia || 0),
          codigoForma: this.filtros.codigoForma || '0',
          diasMinimos: Number(this.filtros.diasMinimos || 0),
          tipoSaldo: Number(this.filtros.tipoSaldo || 0),
          saldoMinimo: Number(this.filtros.saldoMinimo || 0)
        });

      this.resumen =
        response.resumen;

      this.items =
        response.items || [];

    } catch (e: any) {

      console.error('Error consultando asociados sin movimientos:', e);

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
      diasMinimos: 90,
      tipoSaldo: 0,
      saldoMinimo: 0
    };

    this.resumen = null;
    this.items = [];
    this.error = '';
  }

  exportar(): void {

    this.exporter.exportarExcel(
      this.items,
      this.resumen,
      this.filtros.fechaCorte
    );
  }

  claseDias(
    dias: number
  ): string {

    if (dias >= 180) {
      return 'riesgo-alto';
    }

    if (dias >= 90) {
      return 'riesgo-medio';
    }

    return 'riesgo-bajo';
  }

  private fechaHoy(): string {

    return new Date()
      .toISOString()
      .substring(0, 10);
  }

}
