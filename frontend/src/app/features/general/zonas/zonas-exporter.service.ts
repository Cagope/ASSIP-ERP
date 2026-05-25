import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  Zona
} from './zonas.api';

@Injectable({
  providedIn: 'root'
})
export class ZonasExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    data: Zona[]
  ): void {

    if (!data || data.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        'zonas.xlsx',

      hojas: [

        {
          nombreHoja:
            'Zonas',

          titulo:
            'LISTADO DE ZONAS',

          columnas: [
            'Código',
            'Nombre',
            'Comentario'
          ],

          filas: data.map(z => [
            z.codigoZona || '',
            z.nombreZona || '',
            z.comentarioZona || ''
          ]),

          anchos: [
            16,
            36,
            42
          ]
        }

      ]

    });

  }

}
