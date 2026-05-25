import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

import {
  PromediosForma,
  PromediosItem,
  PromediosResumen
} from './promedios.api';

@Injectable({
  providedIn: 'root'
})
export class PromediosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    resumen: PromediosResumen | null,
    formas: PromediosForma[],
    items: PromediosItem[],
    fechaCorte: string
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const hojas: ExcelSheetOptions[] = [
      this.crearHojaResumen(
        resumen,
        fechaCorte
      ),
      this.crearHojaFormas(
        formas || []
      ),
      this.crearHojaDetalle(
        items
      )
    ];

    this.excelExport.exportar({
      nombreArchivo:
        `promedios-${fechaCorte}.xlsx`,
      hojas
    });
  }

  private crearHojaResumen(
    resumen: PromediosResumen | null,
    fechaCorte: string
  ): ExcelSheetOptions {

    return {
      nombreHoja: 'Resumen',
      titulo: 'Informe de promedios',
      filtros: [
        ['Fecha corte', fechaCorte]
      ],
      columnas: [
        'Indicador',
        'Valor'
      ],
      filas: [
        ['Total cuentas', Number(resumen?.totalCuentas || 0)],
        ['Total saldos', Number(resumen?.totalSaldos || 0)],
        ['Saldo promedio', Number(resumen?.saldoPromedio || 0)],
        ['Saldo mayor', Number(resumen?.saldoMayor || 0)],
        ['Saldo menor', Number(resumen?.saldoMenor || 0)]
      ],
      anchos: [
        30,
        20
      ]
    };
  }

  private crearHojaFormas(
    formas: PromediosForma[]
  ): ExcelSheetOptions {

    return {
      nombreHoja: 'Resumen forma',
      titulo: 'Resumen por forma',
      columnas: [
        'Forma',
        'Total cuentas',
        'Total saldos',
        'Saldo promedio',
        'Saldo mayor',
        'Saldo menor'
      ],
      filas: formas.map(forma => [
        `${forma.codigoForma || ''} - ${forma.nombreForma || ''}`,
        Number(forma.totalCuentas || 0),
        Number(forma.totalSaldos || 0),
        Number(forma.saldoPromedio || 0),
        Number(forma.saldoMayor || 0),
        Number(forma.saldoMenor || 0)
      ]),
      anchos: [
        30,
        18,
        18,
        20,
        20,
        20
      ]
    };
  }

  private crearHojaDetalle(
    items: PromediosItem[]
  ): ExcelSheetOptions {

    return {
      nombreHoja: 'Detalle',
      titulo: 'Detalle completo',
      columnas: [
        'Forma',
        'Cuenta',
        'Documento',
        'Nombre',
        'Saldo corte'
      ],
      filas: items.map(item => [
        `${item.codigoForma || ''} - ${item.nombreForma || ''}`,
        item.codigoCuenta || '',
        item.documento || '',
        item.nombreCompleto || '',
        Number(item.saldoCorte || 0)
      ]),
      anchos: [
        30,
        18,
        18,
        42,
        20
      ]
    };
  }
}
