import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  AfpListDTO
} from './afp.api';

@Injectable({
  providedIn: 'root'
})
export class AfpExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: AfpListDTO[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `afp_${this.fechaArchivo()}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'AFP',

          titulo:
            'LISTADO AFP',

          columnas: [
            'ID',
            'Nombre AFP',
            'Documento',
            'Activo'
          ],

          filas: items.map(x => [
            x.idAfp,
            x.nombreAfp,
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
