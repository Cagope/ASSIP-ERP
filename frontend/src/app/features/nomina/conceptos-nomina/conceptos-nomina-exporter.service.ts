import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  ConceptoNominaListDTO
} from './conceptos-nomina.api';

@Injectable({
  providedIn: 'root'
})
export class ConceptosNominaExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: ConceptoNominaListDTO[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `conceptos_nomina_${this.fechaArchivo()}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'ConceptosNomina',

          titulo:
            'CONCEPTOS NÓMINA',

          columnas: [
            'Código',
            'Nombre',
            'Tipo',
            'Es fijo',
            'Activo'
          ],

          filas: items.map(x => [
            x.codigoConcepto || '',
            x.nombreConcepto || '',
            x.tipoConcepto || '',
            x.esFijo ? 'SI' : 'NO',
            x.activo ? 'SI' : 'NO'
          ]),

          anchos: [
            16,
            40,
            18,
            12,
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
