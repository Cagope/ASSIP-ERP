import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class MovimientosInusualesExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarListado(
    fecha: string,
    datos: any[]
  ): void {

    if (!datos || datos.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const columnas =
      Object.keys(datos[0] || {});

    this.excelExport.exportar({

      nombreArchivo:
        `movimientos_inusuales_${fecha}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Movimientos Inusuales',

          titulo:
            'INFORME MOVIMIENTOS INUSUALES',

          filtros: [
            ['Fecha corte', fecha || '']
          ],

          columnas,

          filas: datos.map(x =>
            columnas.map(c => x[c])
          ),

          anchos:
            columnas.map(() => 24)
        }

      ]

    });
  }
}
