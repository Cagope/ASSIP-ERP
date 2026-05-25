import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  FormaAhorro
} from './formas-ahorro.api';

@Injectable({
  providedIn: 'root'
})
export class FormaAhorroExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: FormaAhorro[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        'formas_ahorro.xlsx',

      hojas: [

        {
          nombreHoja:
            'Formas de Ahorro',

          titulo:
            'FORMAS DE AHORRO',

          columnas: [
            'Código',
            'Nombre',
            'Consecutivo',
            'Tipo Captación',
            'Tiempo Liquidación',
            'Cuenta Corto Plazo',
            'Cuenta Largo Plazo',
            'Cuenta Gasto',
            'Cuenta CxP Forma',
            'Cuenta GMF',
            'Tipo Interés',
            'Autorizado',
            'Documento Forma',
            'Período Gracia',
            'Valor Mínimo',
            'Tasa Interés (%)'
          ],

          filas: items.map(f => [

            f.codigoForma || '',

            f.nombreForma || '',

            f.consecutivoForma ?? '',

            f.tipoCaptacion || '',

            f.tiempoLiquidacion || '',

            f.cuentaFormaCorto || '',

            f.cuentaFormaLargo || '',

            f.cuentaGasto || '',

            f.cuentaCxpForma || '',

            f.cuentaGmfForma || '',

            f.tipoInteresForma || '',

            f.autorizadoForma
              ? 'SI'
              : 'NO',

            f.documentoForma || '',

            f.periodoGracia ?? '',

            Number(f.valorMinimo || 0),

            Number(f.tasaInteresForma || 0)

          ]),

          anchos: [
            12,
            32,
            14,
            18,
            18,
            22,
            22,
            22,
            22,
            22,
            16,
            12,
            18,
            16,
            18,
            18
          ]
        }

      ]

    });
  }
}
