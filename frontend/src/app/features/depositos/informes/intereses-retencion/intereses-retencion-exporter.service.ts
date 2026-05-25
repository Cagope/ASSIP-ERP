import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

import {
  InteresesRetencionItem,
  InteresesRetencionResumen
} from './intereses-retencion.api';

@Injectable({
  providedIn: 'root'
})
export class InteresesRetencionExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    items: InteresesRetencionItem[],
    resumen: InteresesRetencionResumen | null,
    fechaInicial: string,
    fechaFinal: string
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `intereses-retencion-${fechaInicial}-${fechaFinal}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Intereses retención',

          titulo:
            'INTERESES Y RETENCIÓN',

          filtros: [
            ['Fecha inicial', fechaInicial || ''],
            ['Fecha final', fechaFinal || '']
          ],

          resumen: [
            ['Total cuentas', Number(resumen?.totalCuentas || 0)],
            ['Total intereses', Number(resumen?.totalIntereses || 0)],
            ['Total retención', Number(resumen?.totalRetencion || 0)],
            ['Total neto', Number(resumen?.totalNeto || 0)]
          ],

          columnas: [
            'Agencia',
            'Forma',
            'Cuenta',
            'Documento',
            'Nombre',
            'Intereses',
            'Retención',
            'Neto'
          ],

          filas: items.map(item => [
            `${item.codigoAgencia || ''} - ${item.nombreAgencia || ''}`,
            `${item.codigoForma || ''} - ${item.nombreForma || ''}`,
            item.codigoCuenta || '',
            item.documento || '',
            item.nombreCompleto || '',
            Number(item.intereses || 0),
            Number(item.retencion || 0),
            Number(item.neto || 0)
          ]),

          anchos: [
            24,
            24,
            16,
            18,
            42,
            18,
            18,
            18
          ]
        }

      ]

    });
  }
}
