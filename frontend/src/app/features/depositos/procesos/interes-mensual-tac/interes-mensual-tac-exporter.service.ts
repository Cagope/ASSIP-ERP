import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class InteresMensualTacExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    items: any[],
    filtros: any
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const fecha =
      `${filtros.fechaProceso}_liq_${filtros.fechaLiquidacion}`;

    const codigoAgencia =
      filtros.codigoAgencia ?? '00';

    const codigoForma =
      filtros.codigoForma ?? 'XX';

    this.excelExport.exportar({

      nombreArchivo:
        `interes_mensual_tac_${fecha}_${codigoAgencia}_${codigoForma}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Interes Mensual TAC',

          titulo:
            'INTERÉS MENSUAL TAC',

          filtros: [
            ['Fecha proceso', filtros.fechaProceso || ''],
            ['Fecha liquidación', filtros.fechaLiquidacion || ''],
            ['Agencia', codigoAgencia],
            ['Forma', codigoForma]
          ],

          columnas: [
            'Documento',
            'Nombre Completo',
            'Promedio Mensual',
            'Interés Bruto',
            'Retención',
            'Interés Neto',
            'Tasa (%)',
            'Saldo Actual',
            'Retención Aplicada'
          ],

          filas: items.map((x: any) => [
            x.documento || '',
            x.nombreCompleto || '',
            Number(x.promedioMensual || 0),
            Number(x.interesBruto || 0),
            Number(x.retencion || 0),
            Number(x.interesNeto || 0),
            Number(x.tasa || 0),
            Number(x.saldoActual || 0),
            x.aplicaRetencion ? 'Sí' : 'No'
          ]),

          anchos: [
            18,
            42,
            18,
            18,
            18,
            18,
            12,
            18,
            18
          ]
        }

      ]

    });
  }
}
