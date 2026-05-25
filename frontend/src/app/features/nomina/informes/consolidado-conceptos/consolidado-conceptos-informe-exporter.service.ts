import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class ConsolidadoConceptosInformeExporterService {

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

    this.excelExport.exportar({

      nombreArchivo:
        'consolidado_conceptos.xlsx',

      hojas: [

        {
          nombreHoja:
            'Consolidado',

          titulo:
            'CONSOLIDADO DE CONCEPTOS NÓMINA',

          columnas: [
            'Código',
            'Nombre',
            'Tipo',
            'Registros',
            'Cantidad Total',
            'Valor Total'
          ],

          filas: items.map(x => [
            x.codigoConcepto || '',
            x.nombreConcepto || '',
            x.tipoConcepto || '',
            Number(x.cantidadRegistros || 0),
            Number(x.totalCantidad || 0),
            Number(x.totalValor || 0)
          ]),

          anchos: [
            16,
            42,
            18,
            18,
            20,
            20
          ]
        }

      ]

    });
  }
}
