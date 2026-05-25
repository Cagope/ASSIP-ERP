import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  CesantiasDTO
} from './cesantias.api';

@Injectable({
  providedIn: 'root'
})
export class CesantiasExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: CesantiasDTO[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `cesantias_${this.fechaArchivo()}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Cesantias',

          titulo:
            'LISTADO CESANTÍAS',

          columnas: [
            'ID',
            'Nombre Cesantías',
            'Documento',
            'Activo'
          ],

          filas: items.map(x => [
            x.idCesantias || '',
            x.nombreCesantias || '',
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
