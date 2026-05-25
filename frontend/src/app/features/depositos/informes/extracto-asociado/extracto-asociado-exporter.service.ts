import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

import {
  ExtractoAsociadoCuenta,
  ExtractoAsociadoMovimiento,
  ExtractoAsociadoResumen
} from './extracto-asociado.api';

@Injectable({
  providedIn: 'root'
})
export class ExtractoAsociadoExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    resumen: ExtractoAsociadoResumen | null,
    cuentas: ExtractoAsociadoCuenta[],
    movimientos: ExtractoAsociadoMovimiento[],
    fechaInicial: string,
    fechaFinal: string
  ): void {

    if (!resumen) {
      alert('No hay datos para exportar.');
      return;
    }

    const hojas: ExcelSheetOptions[] = [];

    hojas.push(
      this.crearHojaResumen(
        resumen,
        fechaInicial,
        fechaFinal
      )
    );

    hojas.push(
      this.crearHojaCuentas(
        cuentas || []
      )
    );

    hojas.push(
      this.crearHojaMovimientos(
        movimientos || []
      )
    );

    this.excelExport.exportar({

      nombreArchivo:
        `extracto-asociado-${resumen.documento}-${fechaInicial}-${fechaFinal}.xlsx`,

      hojas

    });

  }

  private crearHojaResumen(
    resumen: ExtractoAsociadoResumen,
    fechaInicial: string,
    fechaFinal: string
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'Resumen',

      titulo:
        'EXTRACTO CONSOLIDADO ASOCIADO',

      filtros: [
        ['Documento', resumen.documento || ''],
        ['Nombre', resumen.nombreCompleto || ''],
        ['Dirección', resumen.direccion || ''],
        ['Fecha inicial', fechaInicial],
        ['Fecha final', fechaFinal]
      ],

      resumen: [
        ['Total cuentas', Number(resumen.totalCuentas || 0)],
        ['Saldo inicial', Number(resumen.saldoInicial || 0)],
        ['Total créditos', Number(resumen.totalCreditos || 0)],
        ['Total débitos', Number(resumen.totalDebitos || 0)],
        ['Saldo final', Number(resumen.saldoFinal || 0)]
      ],

      columnas: [
        'Indicador',
        'Valor'
      ],

      filas: [],

      anchos: [
        28,
        22
      ]

    };

  }

  private crearHojaCuentas(
    cuentas: ExtractoAsociadoCuenta[]
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'Cuentas',

      titulo:
        'CUENTAS',

      columnas: [
        'Cuenta',
        'Forma',
        'Agencia',
        'Saldo inicial',
        'Créditos',
        'Débitos',
        'Saldo final'
      ],

      filas: cuentas.map(c => [

        c.codigoCuenta || '',

        `${c.codigoForma || ''} - ${c.nombreForma || ''}`,

        c.nombreAgencia || '',

        Number(c.saldoInicial || 0),

        Number(c.totalCreditos || 0),

        Number(c.totalDebitos || 0),

        Number(c.saldoFinal || 0)

      ]),

      anchos: [
        18,
        28,
        24,
        18,
        18,
        18,
        18
      ]

    };

  }

  private crearHojaMovimientos(
    movimientos: ExtractoAsociadoMovimiento[]
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'Movimientos',

      titulo:
        'MOVIMIENTOS',

      columnas: [
        'Fecha',
        'Hora',
        'Cuenta',
        'Forma',
        'Agencia',
        'Tipo movimiento',
        'Descripción',
        'Tipo comp.',
        'Número comp.',
        'Débito',
        'Crédito',
        'Saldo cuenta'
      ],

      filas: movimientos.map(m => [

        m.fechaMovimiento || '',

        m.horaMovimiento || '',

        m.codigoCuenta || '',

        `${m.codigoForma || ''} - ${m.nombreForma || ''}`,

        m.nombreAgencia || '',

        m.tipoMovimiento || '',

        m.descripcionMovimiento || '',

        m.tipoComprobante || '',

        m.numeroComprobante || '',

        Number(m.debito || 0),

        Number(m.credito || 0),

        Number(m.saldoCuenta || 0)

      ]),

      anchos: [
        16,
        14,
        16,
        26,
        22,
        18,
        34,
        14,
        16,
        18,
        18,
        18
      ]

    };

  }

}
