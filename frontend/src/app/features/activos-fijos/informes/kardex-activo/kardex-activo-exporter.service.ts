import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

export interface KardexActivoExportMeta {
  agencia?: string;
  activo?: string;
  fechaIni?: string | null;
  fechaFin?: string | null;
  idActivoFijo?: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class KardexActivoExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    rows: any[],
    meta?: KardexActivoExportMeta
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

          titulo: 'Filtros kardex activo',

          columnas: [
            'Campo',
            'Valor'
          ],

          filas: [
            ['Agencia', meta?.agencia || 'TODAS'],
            ['Activo', meta?.activo || ''],
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
          nombreHoja: 'Kardex',

          titulo: 'Kardex de activo fijo',

          columnas: [
            'Fecha',
            'Hora',
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
            14,
            28,
            18,
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
        activo || 'ACTIVO'
      ).slice(0, 40);

    const hoy = new Date();

    const yyyy =
      hoy.getFullYear();

    const mm =
      String(hoy.getMonth() + 1)
        .padStart(2, '0');

    const dd =
      String(hoy.getDate())
        .padStart(2, '0');

    return `KARDEX_ACTIVO_${ag}_${act}_${yyyy}${mm}${dd}.xlsx`;
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
