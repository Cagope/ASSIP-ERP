import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  CargoListDTO
} from './cargos.api';

@Injectable({
  providedIn: 'root'
})
export class CargosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: CargoListDTO[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `cargos_${this.fechaArchivo()}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Cargos',

          titulo:
            'LISTADO DE CARGOS',

          columnas: [
            'ID',
            'Nombre Cargo',
            'Activo'
          ],

          filas: items.map(c => [
            c.idCargo,
            c.nombreCargo,
            c.activo ? 'SI' : 'NO'
          ]),

          anchos: [
            12,
            40,
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
