import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  LocalizacionListDTO
} from './localizaciones.api';

import {
  GeneralApi
} from '../../../shared/general/general.api';

@Injectable({
  providedIn: 'root'
})
export class LocalizacionesExporterService {

  private agenciasMap =
    new Map<number, string>();

  constructor(
    private generalApi: GeneralApi,
    private excelExport: ExcelExportService
  ) {

    this.generalApi
      .listarAgencias()
      .subscribe(data => {

        (data || []).forEach((a: any) => {
          this.agenciasMap.set(
            a.idAgencia,
            a.nombreAgencia
          );
        });

      });

  }

  exportar(
    items: LocalizacionListDTO[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo: 'localizaciones.xlsx',

      hojas: [

        {
          nombreHoja: 'Localizaciones',

          titulo: 'Listado de localizaciones',

          columnas: [
            'Localización',
            'Teléfono',
            'ID Agencia',
            'Nombre Agencia'
          ],

          filas: items.map(l => [

            l.nombre || '',

            l.telefono || '',

            l.idAgencia ?? '',

            this.agenciasMap.get(l.idAgencia) || ''

          ]),

          anchos: [
            34,
            18,
            14,
            34
          ]
        }

      ]

    });
  }
}
