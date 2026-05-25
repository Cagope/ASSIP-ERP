import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  BloqueListDTO
} from './bloques.api';

@Injectable({
  providedIn: 'root'
})
export class BloquesExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: BloqueListDTO[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo: 'bloques.xlsx',

      hojas: [

        {
          nombreHoja: 'Bloques',

          titulo: 'Listado de bloques',

          columnas: [
            'Código',
            'Nombre Bloque',
            'Meses Depreciación Defecto'
          ],

          filas: items.map(b => [

            b.codigoBloque || '',

            b.nombreBloque || '',

            Number(b.mesesDepreciacionDefecto || 0)

          ]),

          anchos: [
            18,
            42,
            24
          ]
        }

      ]

    });

  }

}
