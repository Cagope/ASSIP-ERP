import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  TipoComprobante
} from './tipos-comprobantes.api';

@Injectable({
  providedIn: 'root'
})
export class TiposComprobantesExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: TipoComprobante[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        'tipos_comprobantes.xlsx',

      hojas: [

        {
          nombreHoja:
            'Tipos de Comprobantes',

          titulo:
            'TIPOS DE COMPROBANTES',

          columnas: [
            'Agencia',
            'Tipo',
            'Nombre',
            'Consecutivo',
            'Activo'
          ],

          filas: items.map(t => [

            t.idAgencia ?? '',

            t.tipoComprobante || '',

            t.nombreTipoComprobante || '',

            t.cscComprobante ?? '',

            t.comprobanteActivo
              ? 'SI'
              : 'NO'

          ]),

          anchos: [
            12,
            14,
            42,
            16,
            12
          ]
        }

      ]

    });
  }
}
