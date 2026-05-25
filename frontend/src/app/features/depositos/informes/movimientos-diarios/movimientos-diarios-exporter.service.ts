import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

import {
  MovimientosDiariosItem,
  MovimientosDiariosResumen
} from './movimientos-diarios.api';

@Injectable({
  providedIn: 'root'
})
export class MovimientosDiariosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    items: MovimientosDiariosItem[],
    resumen: MovimientosDiariosResumen | null,
    fechaInicial: string,
    fechaFinal: string
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `movimientos-diarios-${fechaInicial}-${fechaFinal}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Movimientos diarios',

          titulo:
            'MOVIMIENTOS DIARIOS',

          filtros: [
            ['Fecha inicial', fechaInicial || ''],
            ['Fecha final', fechaFinal || '']
          ],

          resumen: [
            ['Total movimientos', Number(resumen?.totalMovimientos || 0)],
            ['Total débitos', Number(resumen?.totalDebitos || 0)],
            ['Total créditos', Number(resumen?.totalCreditos || 0)]
          ],

          columnas: [
            'Fecha',
            'Hora',
            'Agencia',
            'Cuenta',
            'Documento',
            'Nombre',
            'Forma',
            'Tipo movimiento',
            'Número',
            'Débito',
            'Crédito'
          ],

          filas: items.map(item => [
            item.fechaMovimiento || '',
            item.horaMovimiento || '',
            `${item.codigoAgencia || ''} - ${item.nombreAgencia || ''}`,
            item.codigoCuenta || '',
            item.documento || '',
            item.nombreCompleto || '',
            `${item.codigoForma || ''} - ${item.nombreForma || ''}`,
            `${item.codigoMovimiento || ''} - ${item.nombreMovimiento || ''}`,
            item.idExtractoCuentaAhorro || '',
            Number(item.debito || 0),
            Number(item.credito || 0)
          ]),

          anchos: [
            14,
            12,
            28,
            14,
            16,
            38,
            28,
            32,
            14,
            18,
            18
          ]
        }

      ]

    });
  }
}
