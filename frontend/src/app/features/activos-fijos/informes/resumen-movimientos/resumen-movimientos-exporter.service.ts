import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

export interface ResumenMovimientosExportMeta {
  agencia?: string;
  movimiento?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ResumenMovimientosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    rows: any[],
    meta?: ResumenMovimientosExportMeta
  ): void {

    if (!rows || rows.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        this.buildFileName(meta?.agencia, meta?.movimiento),

      hojas: [

        {
          nombreHoja: 'Resumen Movimientos',

          titulo: 'Resumen de movimientos de activos',

          filtros: [
            ['Agencia', meta?.agencia || 'TODAS'],
            ['Movimiento', meta?.movimiento || 'TODOS']
          ],

          columnas: [
            'Periodo',
            'Id Agencia',
            'Agencia',
            'Código Movimiento',
            'Movimiento',
            'Cantidad Movimientos',
            'Cantidad Activos',
            'Total Débito',
            'Total Crédito',
            'Neto'
          ],

          filas: rows.map(r => [
            this.val(r?.periodo_mes),
            this.num(r?.id_agencia),
            this.val(r?.nombre_agencia),
            this.val(r?.codigo_movimiento),
            this.val(r?.nombre_movimiento),
            this.num(r?.cantidad_movimientos),
            this.num(r?.cantidad_activos),
            this.money(r?.total_debito),
            this.money(r?.total_credito),
            this.money(r?.neto)
          ]),

          anchos: [
            12,
            10,
            35,
            16,
            25,
            18,
            15,
            14,
            14,
            14
          ]
        }

      ]

    });
  }

  private val(
    value: any
  ): string {

    return String(value ?? '').trim();
  }

  private num(
    value: any
  ): number {

    const n =
      Number(value ?? 0);

    return isNaN(n)
      ? 0
      : n;
  }

  private money(
    value: any
  ): number {

    const n =
      Number(value ?? 0);

    return isNaN(n)
      ? 0
      : Math.round(n * 100) / 100;
  }

  private buildFileName(
    agencia?: string,
    movimiento?: string
  ): string {

    const ag =
      this.cleanFilePart(
        agencia || 'TODAS'
      );

    const mov =
      this.cleanFilePart(
        movimiento || 'TODOS'
      );

    const hoy =
      new Date();

    const yyyy =
      hoy.getFullYear();

    const mm =
      String(hoy.getMonth() + 1)
        .padStart(2, '0');

    const dd =
      String(hoy.getDate())
        .padStart(2, '0');

    return `resumen_movimientos_${ag}_${mov}_${yyyy}${mm}${dd}.xlsx`;
  }

  private cleanFilePart(
    value: string
  ): string {

    return value
      .toUpperCase()
      .replaceAll(' ', '_')
      .replaceAll('/', '-')
      .replaceAll('—', '-');
  }
}
