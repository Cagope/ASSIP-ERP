import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  Parametro
} from './parametros.api';

@Injectable({
  providedIn: 'root'
})
export class ParametrosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarParametros(
    items: Parametro[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        'parametros.xlsx',

      hojas: [

        {
          nombreHoja:
            'Parámetros',

          titulo:
            'LISTADO DE PARÁMETROS',

          columnas: [
            'Agencia',
            'Código',
            'Nombre',
            'Valor',
            'Tipo de Valor'
          ],

          filas: items.map(i => [
            i.idAgencia ?? '',
            i.codigoParametro || '',
            i.nombreParametro || '',
            i.valorParametro || '',
            i.tipoValor
              ? 'Porcentaje'
              : 'Valor'
          ]),

          anchos: [
            12,
            16,
            42,
            22,
            18
          ]
        }

      ]

    });
  }
}
