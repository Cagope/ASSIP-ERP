import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

import {
  ExtractoCuentaMovimiento,
  ExtractoCuentaResumen
} from './extracto-cuenta.api';

@Injectable({
  providedIn: 'root'
})
export class ExtractoCuentaExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    resumen: ExtractoCuentaResumen | null,
    movimientos: ExtractoCuentaMovimiento[],
    fechaInicial: string,
    fechaFinal: string
  ): void {

    if (!resumen || !movimientos || movimientos.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `extracto-cuenta-${resumen.codigoCuenta}-${fechaInicial}-${fechaFinal}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Extracto cuenta',

          titulo:
            'EXTRACTO DE MOVIMIENTOS',

          filtros: [
            ['Cuenta', resumen.codigoCuenta || ''],
            ['Documento', resumen.documento || ''],
            ['Nombre', resumen.nombreCompleto || ''],
            ['Forma', `${resumen.codigoForma || ''} - ${resumen.nombreForma || ''}`],
            ['Agencia', resumen.nombreAgencia || ''],
            ['Fecha inicial', fechaInicial],
            ['Fecha final', fechaFinal]
          ],

          resumen: [
            ['Saldo inicial', Number(resumen.saldoInicial || 0)],
            ['Total créditos', Number(resumen.totalCreditos || 0)],
            ['Total débitos', Number(resumen.totalDebitos || 0)],
            ['Saldo final', Number(resumen.saldoFinal || 0)]
          ],

          columnas: [
            'Fecha',
            'Hora',
            'Tipo movimiento',
            'Descripción movimiento',
            'Tipo comp.',
            'Número comp.',
            'Débito',
            'Crédito',
            'Saldo'
          ],

          filas: movimientos.map(m => [
            m.fechaMovimiento || '',
            m.horaMovimiento || '',
            m.tipoMovimiento || '',
            m.descripcionMovimiento || '',
            m.tipoComprobante || '',
            m.numeroComprobante || '',
            Number(m.debito || 0),
            Number(m.credito || 0),
            Number(m.saldo || 0)
          ]),

          anchos: [
            14,
            12,
            18,
            34,
            14,
            16,
            18,
            18,
            18
          ]
        }

      ]

    });
  }
}
