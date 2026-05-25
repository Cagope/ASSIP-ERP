import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

import {
  ExtractoPorValorItem,
  ExtractoPorValorResumen
} from './extracto-por-valor.api';

@Injectable({
  providedIn: 'root'
})
export class ExtractoPorValorExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    items: ExtractoPorValorItem[],
    resumen: ExtractoPorValorResumen | null,
    fechaInicial: string,
    fechaFinal: string,
    valor: number
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `extracto-por-valor-${fechaInicial}-${fechaFinal}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Extracto valor',

          titulo:
            'EXTRACTO POR VALOR',

          filtros: [
            ['Fecha inicial', fechaInicial || ''],
            ['Fecha final', fechaFinal || ''],
            ['Valor buscado', Number(valor || 0)]
          ],

          resumen: [
            ['Total movimientos', Number(resumen?.totalMovimientos || 0)],
            ['Total débitos', Number(resumen?.totalDebitos || 0)],
            ['Total créditos', Number(resumen?.totalCreditos || 0)],
            ['Total neto', Number(resumen?.totalNeto || 0)]
          ],

          columnas: [
            'Fecha',
            'Hora',
            'Agencia',
            'Forma',
            'Cuenta',
            'Documento',
            'Nombre',
            'Movimiento',
            'Débito',
            'Crédito'
          ],

          filas: items.map(item => [
            item.fechaMovimiento || '',
            item.horaMovimiento || '',
            `${item.codigoAgencia || ''} - ${item.nombreAgencia || ''}`,
            `${item.codigoForma || ''} - ${item.nombreForma || ''}`,
            item.codigoCuenta || '',
            item.documento || '',
            item.nombreCompleto || '',
            `${item.codigoMovimiento || ''} - ${item.nombreMovimiento || ''}`,
            Number(item.debito || 0),
            Number(item.credito || 0)
          ]),

          anchos: [
            14,
            12,
            24,
            24,
            14,
            18,
            40,
            30,
            18,
            18
          ]
        }

      ]

    });
  }
}
