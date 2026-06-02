import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  ConvenioRecaudo
} from './convenios-recaudo.api';

@Injectable({
  providedIn: 'root'
})
export class ConveniosRecaudoExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarConvenios(
    items: ConvenioRecaudo[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        'convenios_recaudo.xlsx',

      hojas: [

        {
          nombreHoja:
            'Convenios',

          titulo:
            'LISTADO DE CONVENIOS DE RECAUDO',

          columnas: [
            'Agencia',
            'Código',
            'Convenio',
            'Documento',
            'Titular',
            'Cuenta',
            'Forma',
            'Saldo',
            'Estado'
          ],

          filas: items.map(i => [

            `${i.codigoAgencia || ''} - ${i.nombreAgencia || ''}`,

            i.codigoConvenio || '',

            i.nombreConvenio || '',

            i.documento || '',

            i.nombreTitular || '',

            i.codigoCuenta || '',

            `${i.codigoForma || ''} - ${i.nombreForma || ''}`,

            Number(i.saldoActual || 0),

            i.estado === 'A'
              ? 'Activo'
              : 'Inactivo'

          ]),

          anchos: [
            30,
            18,
            40,
            18,
            40,
            18,
            28,
            18,
            12
          ]
        }

      ]

    });
  }
}
