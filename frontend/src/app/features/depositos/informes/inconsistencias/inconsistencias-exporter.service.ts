import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class InconsistenciasExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: any[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const columnas =
      Object.keys(items[0] || {});

    this.excelExport.exportar({

      nombreArchivo:
        'inconsistencias_saldos.xlsx',

      hojas: [

        {
          nombreHoja:
            'Inconsistencias',

          titulo:
            'INCONSISTENCIAS DE SALDOS',

          columnas,

          filas:
            items.map(item =>
              columnas.map(c => item[c])
            ),

          anchos:
            columnas.map(() => 24)
        }

      ]

    });
  }
}
