import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

import {
  SaldosMenoresItem,
  SaldosMenoresResumen
} from './saldos-menores.api';

@Injectable({
  providedIn: 'root'
})
export class SaldosMenoresExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    resumen: SaldosMenoresResumen | null,
    items: SaldosMenoresItem[],
    fechaCorte: string
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `saldos-menores-${fechaCorte}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Saldos menores',

          titulo:
            'Saldos menores',

          filtros: [
            ['Fecha corte', fechaCorte]
          ],

          resumen: [
            ['Total cuentas', Number(resumen?.totalCuentas || 0)],
            ['Total saldos', Number(resumen?.totalSaldos || 0)],
            ['Saldo promedio', Number(resumen?.saldoPromedio || 0)]
          ],

          columnas: [
            'Agencia',
            'Forma',
            'Cuenta',
            'Documento',
            'Nombre',
            'Fecha apertura',
            'Estado',
            'Saldo corte'
          ],

          filas: items.map(item => [
            item.nombreAgencia || '',
            `${item.codigoForma || ''} - ${item.nombreForma || ''}`,
            item.codigoCuenta || '',
            item.documento || '',
            item.nombreCompleto || '',
            item.fechaApertura || '',
            item.estadoCuenta || '',
            Number(item.saldoCorte || 0)
          ]),

          anchos: [
            30,
            26,
            18,
            18,
            40,
            16,
            16,
            18
          ]
        }

      ]

    });
  }
}
