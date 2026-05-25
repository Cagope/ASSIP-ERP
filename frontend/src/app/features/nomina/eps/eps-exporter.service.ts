import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  EpsListDTO
} from './eps.api';

@Injectable({
  providedIn: 'root'
})
export class EpsExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: EpsListDTO[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        'eps.xlsx',

      hojas: [

        {
          nombreHoja:
            'EPS',

          titulo:
            'LISTADO EPS',

          columnas: [
            'ID',
            'Nombre EPS',
            'Documento',
            'Activo'
          ],

          filas: items.map(x => [
            x.idEps ?? '',
            x.nombreEps || '',
            x.documento || '',
            x.activo ? 'SI' : 'NO'
          ]),

          anchos: [
            12,
            40,
            18,
            12
          ]
        }

      ]

    });
  }
}
