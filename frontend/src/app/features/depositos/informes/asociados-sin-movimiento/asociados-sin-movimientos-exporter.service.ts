import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

import {
  AsociadosSinMovimientosItem,
  AsociadosSinMovimientosResumen
} from './asociados-sin-movimientos.api';

@Injectable({
  providedIn: 'root'
})
export class AsociadosSinMovimientosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    items: AsociadosSinMovimientosItem[],
    resumen: AsociadosSinMovimientosResumen | null,
    fechaCorte: string
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `asociados-sin-movimientos-${fechaCorte}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Sin movimientos',

          titulo:
            'ASOCIADOS SIN MOVIMIENTOS',

          filtros: [
            ['Fecha corte', fechaCorte]
          ],

          resumen: [
            ['Total cuentas', Number(resumen?.totalCuentas || 0)],
            ['Total con saldo', Number(resumen?.totalConSaldo || 0)],
            ['Total sin saldo', Number(resumen?.totalSinSaldo || 0)],
            ['Saldo total', Number(resumen?.saldoTotal || 0)]
          ],

          columnas: [
            'Agencia',
            'Cuenta',
            'Documento',
            'Nombre',
            'Forma',
            'Fecha apertura',
            'Último movimiento',
            'Días sin movimiento',
            'Saldo actual',
            'Estado'
          ],

          filas: items.map(item => [
            `${item.codigoAgencia || ''} - ${item.nombreAgencia || ''}`,
            item.codigoCuenta || '',
            item.documento || '',
            item.nombreCompleto || '',
            `${item.codigoForma || ''} - ${item.nombreForma || ''}`,
            item.fechaAperturaCuenta || '',
            item.fechaUltimoMovimiento || '',
            Number(item.diasSinMovimiento || 0),
            Number(item.saldoActual || 0),
            item.estadoCuenta || ''
          ]),

          anchos: [
            24,
            16,
            18,
            42,
            28,
            16,
            18,
            20,
            18,
            12
          ]
        }

      ]

    });
  }
}
