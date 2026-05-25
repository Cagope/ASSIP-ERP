import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';
import {
  ActivoFijoListDTO
} from './activos-fijos.api';

@Injectable({
  providedIn: 'root'
})
export class ActivosFijosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: ActivoFijoListDTO[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo: 'activos_fijos.xlsx',

      hojas: [

        {
          nombreHoja: 'Activos Fijos',

          titulo: 'Listado de activos fijos',

          columnas: [
            'Placa',
            'Nombre Activo',
            'Agencia',
            'Fecha Ingreso',
            'Meses Depreciación',
            'Valor Adquisición',
            'Valor Mensual Depreciación',
            'Estado'
          ],

          filas: items.map(a => [

            a.placaActivo || '',

            a.nombreActivo || '',

            a.nombreAgencia || '',

            a.fechaIngreso || '',

            Number(a.mesesDepreciacion || 0),

            Number(a.valorAdquisicion || 0),

            Number(a.valorMensual || 0),

            a.nombreEstadoActivo || ''

          ]),

          anchos: [
            18,
            42,
            28,
            18,
            18,
            22,
            26,
            22
          ]
        }

      ]

    });

  }

}
