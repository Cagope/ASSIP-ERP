import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { GeneralApi } from '../../../../shared/general/general.api';
import { NumericFormatDirective } from '../../../../shared/utils/numeric-format.directive';

import {
  MovimientosPorMesesApi,
  MovimientosPorMesesAsociado,
  MovimientosPorMesesResumen,
  TipoInformeMovimientosMeses
} from './movimientos-por-meses.api';

import {
  MovimientosPorMesesExporterService
} from './movimientos-por-meses-exporter.service';

@Component({
  selector: 'app-movimientos-por-meses',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    NumericFormatDirective
  ],
  templateUrl: './movimientos-por-meses.component.html',
  styleUrls: ['./movimientos-por-meses.component.scss']
})
export class MovimientosPorMesesComponent implements OnInit {

  private readonly api = inject(MovimientosPorMesesApi);
  private readonly generalApi = inject(GeneralApi);
  private readonly exporter = inject(MovimientosPorMesesExporterService);

  agencias: any[] = [];
  formas: any[] = [];

  filtros = {
    fechaCorte: this.fechaHoy(),
    idAgencia: 0,
    codigoForma: '0',
    meses: 3,
    tipoInforme: 'RESUMEN' as TipoInformeMovimientosMeses
  };

  cargando = false;
  error = '';
  mensaje = '';

  meses: string[] = [];

  resumen: MovimientosPorMesesResumen[] = [];
  detalleAsociado: MovimientosPorMesesAsociado[] = [];

  totalMovimientos = 0;
  totalEntradas = 0;
  totalSalidas = 0;

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

      console.error(
        'Error cargando formas de ahorro:',
        e
      );

      this.formas = [];

    }
  }

  async buscar(): Promise<void> {
    this.error = '';
    this.mensaje = '';

    const meses = Number(
      String(this.filtros.meses || 0).replace(/,/g, '')
    );

    if (!this.filtros.fechaCorte) {
      this.error = 'Debe seleccionar la fecha de corte.';
      return;
    }

    if (!this.filtros.idAgencia || Number(this.filtros.idAgencia) === 0) {
      this.error = 'Debe seleccionar una agencia.';
      return;
    }

    if (!this.filtros.codigoForma || this.filtros.codigoForma === '0') {
      this.error = 'Debe seleccionar una forma de ahorro.';
      return;
    }

    if (!meses || meses <= 0) {
      this.error = 'Debe indicar el número de meses hacia atrás.';
      return;
    }

    if (meses > 24) {
      this.error = 'El número de meses no puede ser mayor a 24.';
      return;
    }

    this.cargando = true;

    try {
      const response = await this.api.consultar({
        fechaCorte: this.filtros.fechaCorte,
        idAgencia: Number(this.filtros.idAgencia),
        codigoForma: this.filtros.codigoForma,
        meses,
        tipoInforme: this.filtros.tipoInforme
      });

      this.meses = response.meses || [];
      this.resumen = response.resumen || [];
      this.detalleAsociado = response.detalleAsociado || [];

      this.totalMovimientos = response.totalMovimientos || 0;
      this.totalEntradas = response.totalEntradas || 0;
      this.totalSalidas = response.totalSalidas || 0;

      if (this.filtros.tipoInforme === 'ASOCIADO') {
        this.exportar();

        this.mensaje =
          'El detalle por asociado se generó en Excel. En pantalla solo se muestra el resumen para evitar bloqueo.';
      }

    } catch (e: any) {
      console.error('Error consultando movimientos por meses:', e);

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
      meses: 3,
      tipoInforme: 'RESUMEN'
    };

    this.meses = [];
    this.resumen = [];
    this.detalleAsociado = [];

    this.totalMovimientos = 0;
    this.totalEntradas = 0;
    this.totalSalidas = 0;

    this.error = '';
    this.mensaje = '';
  }

  obtenerResumenMes(mes: string): MovimientosPorMesesResumen {
    const item = this.resumen.find(r => r.mes === mes);

    return item || {
      mes,
      cantidadMovimientos: 0,
      entradas: 0,
      salidas: 0
    };
  }

  cantidadAsociados(): number {
    return this.obtenerAsociadosUnicos().length;
  }

  obtenerAsociadosUnicos(): MovimientosPorMesesAsociado[] {
    const mapa = new Map<string, MovimientosPorMesesAsociado>();

    for (const item of this.detalleAsociado) {
      const key = `${item.documento}|${item.codigoCuenta}|${item.codigoForma}`;

      if (!mapa.has(key)) {
        mapa.set(key, item);
      }
    }

    return Array.from(mapa.values());
  }

  exportar(): void {
    this.exporter.exportarExcel(
      this.meses,
      this.resumen,
      this.detalleAsociado,
      this.filtros.tipoInforme,
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
