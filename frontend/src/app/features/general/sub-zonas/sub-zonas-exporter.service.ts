import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  SubZona
} from './sub-zonas.api';

@Injectable({
  providedIn: 'root'
})
export class SubZonasExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarSubZonas(
    items: SubZona[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        'subzonas.xlsx',

      hojas: [

        {
          nombreHoja:
            'SubZonas',

          titulo:
            'LISTADO DE SUBZONAS',

          columnas: [
            'Código',
            'Nombre',
            'Zona',
            'Comentario'
          ],

          filas: items.map(i => [
            i.codigoSubZona || '',
            i.nombreSubZona || '',
            i.zona?.nombreZona || '',
            i.comentarioSubZona || ''
          ]),

          anchos: [
            16,
            36,
            28,
            42
          ]
        }

      ]

    });
  }
}
