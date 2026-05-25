import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  CdatCausacionMensualPreviewDTO
} from './causacion-mensual-cdat.api';

@Injectable({
  providedIn: 'root'
})
export class CausacionMensualCdatExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    preview: CdatCausacionMensualPreviewDTO
  ): void {

    if (!preview?.items?.length) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `causacion-mensual-cdat-${preview.fechaCorte}.xlsx`,

      hojas: [

        {
          nombreHoja: 'Causación',

          titulo: 'CAUSACIÓN MENSUAL CDAT',

          filtros: [
            ['Agencia', preview.idAgencia],
            ['Fecha corte', preview.fechaCorte],
            ['Fecha contable', preview.fechaContable]
          ],

          resumen: [
            ['Total CDAT', preview.totalCdats],
            ['Total capital', Number(preview.totalCapital || 0)],
            ['Total interés', Number(preview.totalInteres || 0)],
            ['Total retención', Number(preview.totalRetencion || 0)],
            ['Total neto', Number(preview.totalNeto || 0)]
          ],

          columnas: [
            'CDAT',
            'Documento',
            'Nombre',
            'Saldo base',
            'Tasa',
            'Días',
            'Interés',
            'Retención',
            'Neto'
          ],

          filas: [
            ...preview.items.map(item => [
              item.codigoCdat || '',
              item.documento || '',
              item.nombreCompleto || '',
              Number(item.saldoBase || 0),
              Number(item.tasaNominalAnual || 0),
              item.diasCausados || 0,
              Number(item.valorInteres || 0),
              Number(item.valorRetencion || 0),
              Number(item.valorNeto || 0)
            ]),

            [
              '',
              '',
              'TOTALES',
              Number(preview.totalCapital || 0),
              '',
              '',
              Number(preview.totalInteres || 0),
              Number(preview.totalRetencion || 0),
              Number(preview.totalNeto || 0)
            ]
          ],

          anchos: [
            14,
            18,
            42,
            18,
            12,
            10,
            16,
            16,
            16
          ]
        }

      ]

    });
  }
}
