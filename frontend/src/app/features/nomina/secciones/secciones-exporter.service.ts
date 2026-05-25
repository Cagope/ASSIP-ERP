import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  SeccionNominaDTO
} from './secciones.api';

@Injectable({
  providedIn: 'root'
})
export class SeccionesNominaExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: SeccionNominaDTO[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        'secciones_nomina.xlsx',

      hojas: [

        {
          nombreHoja:
            'Secciones',

          titulo:
            'SECCIONES NÓMINA',

          columnas: [
            'Código',
            'Nombre Sección',
            'Activo'
          ],

          filas: items.map(x => [
            x.codigo || '',
            x.nombreSeccion || '',
            x.activo ? 'SI' : 'NO'
          ]),

          anchos: [
            18,
            42,
            12
          ]
        }

      ]

    });
  }
}
