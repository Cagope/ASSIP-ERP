import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  ArlListDTO
} from './arl.api';

@Injectable({
  providedIn: 'root'
})
export class ArlExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: ArlListDTO[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `arl_${this.fechaArchivo()}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'ARL',

          titulo:
            'LISTADO ARL',

          columnas: [
            'ID',
            'Nombre ARL',
            'Documento',
            'Activo'
          ],

          filas: items.map(x => [
            x.idArl,
            x.nombreArl,
            x.documento || '',
            x.activo ? 'SI' : 'NO'
          ]),

          anchos: [
            12,
            40,
            20,
            12
          ]
        }

      ]

    });

  }

  private fechaArchivo(): string {

    const fecha =
      new Date();

    return `${fecha.getFullYear()}${String(fecha.getMonth() + 1).padStart(2, '0')}${String(fecha.getDate()).padStart(2, '0')}`;
  }

}
