import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

import {
  ResumenTipoMovimientoItem,
  ResumenTipoMovimientoResumen
} from './resumen-tipo-movimiento.api';

@Injectable({
  providedIn: 'root'
})
export class ResumenTipoMovimientoExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    items: ResumenTipoMovimientoItem[],
    resumen: ResumenTipoMovimientoResumen | null,
    fechaInicial: string,
    fechaFinal: string
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `resumen-tipo-movimiento-${fechaInicial}-${fechaFinal}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Resumen movimientos',

          titulo:
            'RESUMEN POR TIPO MOVIMIENTO',

          filtros: [
            ['Fecha inicial', fechaInicial || ''],
            ['Fecha final', fechaFinal || '']
          ],

          resumen: [
            ['Total tipos', Number(resumen?.totalTipos || 0)],
            ['Total movimientos', Number(resumen?.totalMovimientos || 0)],
            ['Total débitos', Number(resumen?.totalDebitos || 0)],
            ['Total créditos', Number(resumen?.totalCreditos || 0)],
            ['Total neto', Number(resumen?.totalNeto || 0)]
          ],

          columnas: [
            'Código',
            'Tipo movimiento',
            'Movimientos',
            'Débitos',
            'Créditos',
            'Neto'
          ],

          filas: items.map(item => [
            item.codigoMovimiento || '',
            item.nombreMovimiento || '',
            Number(item.cantidadMovimientos || 0),
            Number(item.totalDebitos || 0),
            Number(item.totalCreditos || 0),
            Number(item.neto || 0)
          ]),

          anchos: [
            14,
            38,
            18,
            18,
            18,
            18
          ]
        }

      ]

    });
  }
}
