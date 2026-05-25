import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class CdatLiquidacionDiariaExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    preview: any
  ): void {

    if (!preview?.items?.length) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `liquidacion-diaria-cdat-${preview.fechaLiquidacion || 'proceso'}.xlsx`,

      hojas: [

        {
          nombreHoja: 'Liquidacion CDAT',

          titulo: 'LIQUIDACIÓN DIARIA CDAT',

          filtros: [
            ['Fecha liquidación', preview.fechaLiquidacion || ''],
            ['Total CDATS', preview.totalCdats || 0]
          ],

          columnas: [
            'CDAT',
            'Documento',
            'Nombre',
            'Capital',
            'Tasa',
            'Interés',
            'Retención',
            'Neto',
            'Trasladado',
            'Cuenta ahorro'
          ],

          filas: [
            ...preview.items.map((item: any) => [
              item.codigoCdat || '',
              item.documento || '',
              item.nombreCompleto || '',
              Number(item.valorCapital || 0),
              Number(item.tasaNominalAnual || 0),
              Number(item.valorInteres || 0),
              Number(item.valorRetencion || 0),
              Number(item.valorNeto || 0),
              Number(item.valorTrasladado || 0),
              item.codigoCuentaAhorro || ''
            ]),

            [
              '',
              '',
              'TOTALES',
              Number(preview.totalCapital || 0),
              '',
              Number(preview.totalInteres || 0),
              Number(preview.totalRetencion || 0),
              Number(preview.totalNeto || 0),
              Number(preview.totalTrasladado || 0),
              ''
            ]
          ],

          anchos: [
            14,
            16,
            42,
            16,
            10,
            16,
            16,
            16,
            16,
            18
          ]
        }

      ]

    });
  }
}
