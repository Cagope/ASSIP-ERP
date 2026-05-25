import { Component, Input, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ReportingService } from '../../../../shared/reporting/reporting.service';

import {
  ReportQueryRequest,
  ReportResult
} from '../../../../shared/reporting/reporting.api';

import { ConsultaCdatsExtractoPrintService }
from './consulta-cdats-extracto-print.service';

import { ConsultaCdatsExtractoExporterService }
from './consulta-cdats-extracto-exporter.service';

@Component({
  selector: 'app-consulta-cdats-detalle',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './consulta-cdats-detalle.component.html',
  styleUrls: ['./consulta-cdats-detalle.component.scss']
})
export class ConsultaCdatsDetalleComponent implements OnInit {

  private readonly reporting = inject(ReportingService);
  private readonly printService = inject(ConsultaCdatsExtractoPrintService);
  private readonly exporterService = inject(ConsultaCdatsExtractoExporterService);

  @Input() cdat: any;

  fechaInicial = '';
  fechaFinal = '';

  cargandoExtracto = false;
  errorExtracto = '';

  movimientos: any[] = [];
  resumenPorMovimiento: any[] = [];

  ngOnInit(): void {
    this.cargarFechasActuales();
  }

  nombreCompleto(): string {

    return this.cdat?.nombre_completo_apellidos
      || this.cdat?.nombre_completo_nombres
      || this.cdat?.nombres
      || '';
  }

  nombreCotitular(): string {

    return this.cdat?.nombre_cotitular_apellidos
      || this.cdat?.nombre_cotitular_nombres
      || '';
  }

  tieneCotitular(): boolean {

    return !!this.cdat?.id_datos_personal_cotitular;
  }

  esActivo(): boolean {

    return this.cdat?.estado_cdat === 'A';
  }

  siNo(valor: any): string {

    if (valor === true || valor === 'S') {
      return 'Sí';
    }

    return 'No';
  }

  async consultarExtracto(): Promise<void> {

    if (!this.cdat?.id_cuenta_cdat) {
      this.errorExtracto = 'No existe CDAT seleccionado.';
      return;
    }

    if (!this.fechaInicial || !this.fechaFinal) {
      this.errorExtracto = 'Debe seleccionar fechas.';
      return;
    }

    if (this.fechaInicial > this.fechaFinal) {
      this.errorExtracto =
        'La fecha inicial no puede ser mayor que la fecha final.';
      return;
    }

    this.cargandoExtracto = true;

    this.errorExtracto = '';
    this.movimientos = [];
    this.resumenPorMovimiento = [];

    try {

      const req: ReportQueryRequest = {
        schema: 'cdat',
        view: 'vw_cdat_extractos_total',
        filters: {
          codigo_cdat: this.cdat.codigo_cdat,
          fecha_movimiento_desde: this.fechaInicial,
          fecha_movimiento_hasta: this.fechaFinal
        }
      };

      const res: ReportResult =
        await this.reporting.ejecutarReporte(req);

      this.movimientos = (res.data ?? []).filter(
        (m: any) => String(m.codigo_cdat) === String(this.cdat.codigo_cdat)
      );

      this.calcularResumenPorMovimiento();

      if (this.movimientos.length === 0) {
        this.errorExtracto =
          'No se encontraron movimientos.';
      }

    } catch (e) {

      console.error('❌ Error extracto CDAT:', e);

      this.errorExtracto =
        'Error al consultar extracto.';

    } finally {

      this.cargandoExtracto = false;

    }
  }

  cargarFechasActuales(): void {

    const hoy = new Date();

    const inicioMes =
      new Date(
        hoy.getFullYear(),
        hoy.getMonth(),
        1
      );

    this.fechaInicial =
      this.toISODate(inicioMes);

    this.fechaFinal =
      this.toISODate(hoy);
  }

  private toISODate(d: Date): string {

    const yyyy = d.getFullYear();

    const mm =
      String(d.getMonth() + 1)
        .padStart(2, '0');

    const dd =
      String(d.getDate())
        .padStart(2, '0');

    return `${yyyy}-${mm}-${dd}`;
  }

  imprimirExtracto(): void {

    if (this.movimientos.length === 0) {
      return;
    }

    this.printService.imprimir(
      this.cdat,
      this.fechaInicial,
      this.fechaFinal,
      this.movimientos,
      this.resumenPorMovimiento
    );
  }

  calcularResumenPorMovimiento(): void {

    const mapa = new Map<string, any>();

    for (const m of this.movimientos) {

      const codigo =
        m.tipo_movimiento || '';

      const descripcion =
        m.descripcion_movimiento || 'Sin descripción';

      const key =
        `${codigo}|${descripcion}`;

      if (!mapa.has(key)) {

        mapa.set(key, {
          tipoMovimiento: codigo,
          descripcionMovimiento: descripcion,
          valor: 0
        });

      }

      const item = mapa.get(key);

      const debito =
        Number(m.valor_debito) || 0;

      const credito =
        Number(m.valor_credito) || 0;

      item.valor +=
        credito > 0
          ? credito
          : debito;
    }

    this.resumenPorMovimiento =
      Array.from(mapa.values());
  }

  exportarExtracto(): void {

    if (this.movimientos.length === 0) {
      return;
    }

    this.exporterService.exportar(
      this.cdat,
      this.fechaInicial,
      this.fechaFinal,
      this.movimientos,
      this.resumenPorMovimiento
    );
  }


}
