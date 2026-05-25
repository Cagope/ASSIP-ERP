import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  CajaCompensacionListDTO
} from './caja-compensacion.api';

@Injectable({
  providedIn: 'root'
})
export class CajaCompensacionExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: CajaCompensacionListDTO[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `caja_compensacion_${this.fechaArchivo()}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'CajaCompensacion',

          titulo:
            'LISTADO CAJAS DE COMPENSACIÓN',

          columnas: [
            'ID',
            'Nombre Caja',
            'Documento',
            'Activo'
          ],

          filas: items.map(x => [
            x.idCaja,
            x.nombreCaja,
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
