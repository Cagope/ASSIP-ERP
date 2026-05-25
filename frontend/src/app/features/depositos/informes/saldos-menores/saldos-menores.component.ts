import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { GeneralApi } from '../../../../shared/general/general.api';
import { NumericFormatDirective } from '../../../../shared/utils/numeric-format.directive';

import {
  SaldosMenoresApi,
  SaldosMenoresItem,
  SaldosMenoresResumen
} from './saldos-menores.api';

import {
  SaldosMenoresExporterService
} from './saldos-menores-exporter.service';

@Component({
  selector: 'app-saldos-menores',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    NumericFormatDirective
  ],
  templateUrl: './saldos-menores.component.html',
  styleUrls: ['./saldos-menores.component.scss']
})
export class SaldosMenoresComponent implements OnInit {

  private readonly api = inject(SaldosMenoresApi);
  private readonly generalApi = inject(GeneralApi);
  private readonly exporter = inject(SaldosMenoresExporterService);

  agencias: any[] = [];
  formas: any[] = [];

  filtros = {
    fechaCorte: this.fechaHoy(),
    idAgencia: 0,
    codigoForma: '0',
    valorMaximo: 100000
  };

  cargando = false;
  error = '';

  resumen: SaldosMenoresResumen | null = null;
  items: SaldosMenoresItem[] = [];

  async ngOnInit(): Promise<void> {
    await this.cargarCatalogos();
  }

  async cargarCatalogos(): Promise<void> {
    try {
      const agencias =
        await this.generalApi.listarAgencias().toPromise();

      this.agencias = agencias || [];
    } catch (e) {
      console.error(e);
      this.agencias = [];
    }

    try {
      const formas =
        await this.api.listarFormasAhorro();

      this.formas = Array.from(
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
      this.formas = [];
    }
  }

  async buscar(): Promise<void> {
    this.error = '';

    const valorMaximo =
      Number(
        String(this.filtros.valorMaximo || 0)
          .replace(/,/g, '')
      );

    if (!this.filtros.fechaCorte) {
      this.error = 'Debe seleccionar la fecha de corte.';
      return;
    }

    if (!this.filtros.idAgencia || Number(this.filtros.idAgencia) === 0) {
      this.error = 'Debe seleccionar una agencia.';
      return;
    }

    if (!valorMaximo || valorMaximo <= 0) {
      this.error = 'Debe indicar un valor máximo.';
      return;
    }

    this.cargando = true;

    try {
      const response =
        await this.api.consultar({
          fechaCorte: this.filtros.fechaCorte,
          idAgencia: Number(this.filtros.idAgencia),
          codigoForma: this.filtros.codigoForma || '0',
          valorMaximo
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
      fechaCorte: this.fechaHoy(),
      idAgencia: 0,
      codigoForma: '0',
      valorMaximo: 100000
    };

    this.resumen = null;
    this.items = [];
    this.error = '';
  }

  formasConDatos(): {
    codigoForma: string;
    nombreForma: string;
    items: SaldosMenoresItem[];
  }[] {

    const mapa = new Map<string, {
      codigoForma: string;
      nombreForma: string;
      items: SaldosMenoresItem[];
    }>();

    for (const item of this.items) {
      const key = item.codigoForma || 'SIN_FORMA';

      if (!mapa.has(key)) {
        mapa.set(key, {
          codigoForma: item.codigoForma,
          nombreForma: item.nombreForma,
          items: []
        });
      }

      mapa.get(key)?.items.push(item);
    }

    return Array.from(mapa.values());
  }

  totalForma(items: SaldosMenoresItem[]): number {
    return items.reduce(
      (total, item) => total + Number(item.saldoCorte || 0),
      0
    );
  }

  exportar(): void {
    this.exporter.exportarExcel(
      this.resumen,
      this.items,
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
