import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  ConceptoCuentaContableListDTO
} from './concepto-cuentas-contables.api';

@Injectable({
  providedIn: 'root'
})
export class ConceptoCuentasContablesExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: ConceptoCuentaContableListDTO[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `concepto_cuentas_contables_${this.fechaArchivo()}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'ConceptosContables',

          titulo:
            'CONCEPTO CUENTAS CONTABLES',

          columnas: [
            'Concepto',
            'Agencia',
            'Cuenta Débito',
            'Cuenta Crédito',
            'Activo'
          ],

          filas: items.map(x => [
            x.codigoConcepto || '',
            x.nombreAgencia || '',
            x.cuentaDebito || '',
            x.cuentaCredito || '',
            x.activo ? 'SI' : 'NO'
          ]),

          anchos: [
            18,
            30,
            35,
            35,
            10
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
