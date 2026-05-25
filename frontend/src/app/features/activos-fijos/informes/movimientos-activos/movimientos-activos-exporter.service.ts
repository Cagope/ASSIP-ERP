import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

export interface MovimientosActivosExportMeta {
  agencia?: string;
  activo?: string;
  movimiento?: string;
  fechaIni?: string | null;
  fechaFin?: string | null;
  idActivoFijo?: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class MovimientosActivosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    rows: any[],
    meta?: MovimientosActivosExportMeta
  ): void {

    if (!rows || rows.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const fechaTexto =
      new Date().toLocaleString('es-CO');

    this.excelExport.exportar({

      nombreArchivo:
        this.buildFileName(meta?.agencia, meta?.activo),

      hojas: [

        {
          nombreHoja: 'Filtros',

          titulo: 'Filtros movimientos activos',

          columnas: [
            'Campo',
            'Valor'
          ],

          filas: [
            ['Agencia', meta?.agencia || 'TODAS'],
            ['Activo', meta?.activo || 'TODOS'],
            ['Movimiento', meta?.movimiento || 'TODOS'],
            ['Fecha Inicial', meta?.fechaIni || ''],
            ['Fecha Final', meta?.fechaFin || ''],
            ['Generado', fechaTexto],
            ['Total filas', rows.length]
          ],

          anchos: [
            18,
            60
          ]
        },

        {
          nombreHoja: 'Movimientos',

          titulo: 'Movimientos de activos',

          columnas: [
            'Fecha',
            'Hora',
            'ID Activo',
            'Placa',
            'Activo',
            'ID Agencia',
            'Agencia',
            'Tipo Movimiento',
            'Movimiento',
            'Tipo Comprobante',
            'Número Comprobante',
            'Débito',
            'Crédito'
          ],

          filas: rows.map(r => [
            r?.fecha || '',
            r?.hora || '',
            r?.id_activo_fijo ?? '',
            r?.placa_activo || '',
            r?.nombre_activo || '',
            r?.id_agencia ?? '',
            r?.nombre_agencia || '',
            r?.codigo_movimiento || '',
            r?.nombre_movimiento || '',
            r?.tipo_comprobante || '',
            r?.numero_comprobante || '',
            Number(r?.valor_debito || 0),
            Number(r?.valor_credito || 0)
          ]),

          anchos: [
            12,
            10,
            10,
            12,
            35,
            10,
            30,
            14,
            22,
            16,
            18,
            14,
            14
          ]
        }

      ]

    });
  }

  private buildFileName(
    agencia?: string,
    activo?: string
  ): string {

    const ag =
      this.limpiarNombreArchivo(
        agencia || 'TODAS'
      );

    const act =
      this.limpiarNombreArchivo(
        activo || 'TODOS'
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

    return `MOVIMIENTOS_ACTIVOS_${ag}_${act}_${yyyy}${mm}${dd}.xlsx`;
  }

  private limpiarNombreArchivo(
    value: string
  ): string {

    return value
      .toUpperCase()
      .replaceAll(' ', '_')
      .replaceAll('/', '-')
      .replaceAll('.', '');
  }
}
