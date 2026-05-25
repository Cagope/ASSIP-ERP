import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class SaldosCorteExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    items: any[],
    fechaCorte?: string
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const agencias =
      this.agrupar(items);

    const hojas: ExcelSheetOptions[] =
      agencias.map((ag: any) =>
        this.crearHojaAgencia(
          ag,
          fechaCorte
        )
      );

    this.excelExport.exportar({

      nombreArchivo:
        `saldos-corte-${fechaCorte || 'informe'}.xlsx`,

      hojas

    });

  }

  private crearHojaAgencia(
    ag: any,
    fechaCorte?: string
  ): ExcelSheetOptions {

    const filas: any[][] = [];

    let totalCuentasAgencia = 0;
    let totalDebitosAgencia = 0;
    let totalCreditosAgencia = 0;
    let totalSaldoAgencia = 0;

    for (const forma of ag.formas) {

      filas.push([
        `${forma.codigoForma} - ${forma.nombreForma}`,
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        ''
      ]);

      let totalCuentasForma = 0;
      let totalDebitosForma = 0;
      let totalCreditosForma = 0;
      let totalSaldoForma = 0;

      for (const d of forma.detalle) {

        const debitos =
          Number(d.totalDebitos || 0);

        const creditos =
          Number(d.totalCreditos || 0);

        const saldo =
          Number(d.saldoCorte || 0);

        filas.push([
          d.codigoCuenta || '',
          d.documento || '',
          d.nombreCompleto || '',
          d.direccion || '',
          d.departamento || '',
          d.ciudad || '',
          d.zona || '',
          d.subZona || '',
          d.telefono || '',
          d.celular || '',
          d.celularDos || '',
          d.correo || '',
          d.estadoCuentaNombre || '',
          d.fechaApertura || '',
          d.fechaUltimoMovimiento || '',
          debitos,
          creditos,
          saldo
        ]);

        totalCuentasForma++;
        totalDebitosForma += debitos;
        totalCreditosForma += creditos;
        totalSaldoForma += saldo;
      }

      filas.push([
        '',
        '',
        `TOTAL ${forma.nombreForma}`,
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        totalDebitosForma,
        totalCreditosForma,
        totalSaldoForma
      ]);

      filas.push([
        '',
        '',
        'Cantidad cuentas',
        totalCuentasForma
      ]);

      filas.push([]);

      totalCuentasAgencia += totalCuentasForma;
      totalDebitosAgencia += totalDebitosForma;
      totalCreditosAgencia += totalCreditosForma;
      totalSaldoAgencia += totalSaldoForma;
    }

    filas.push([]);

    filas.push([
      '',
      '',
      'TOTAL AGENCIA',
      '',
      '',
      '',
      '',
      '',
      '',
      '',
      '',
      '',
      '',
      '',
      '',
      totalDebitosAgencia,
      totalCreditosAgencia,
      totalSaldoAgencia
    ]);

    filas.push([
      '',
      '',
      'Cantidad cuentas agencia',
      totalCuentasAgencia
    ]);

    return {

      nombreHoja:
        this.nombreHojaSegura(
          `${ag.codigoAgencia} ${ag.nombreAgencia}`
        ),

      titulo:
        'SALDOS A FECHA DE CORTE',

      filtros: [
        [
          'Agencia',
          `${ag.codigoAgencia} - ${ag.nombreAgencia}`
        ],
        [
          'Fecha corte',
          fechaCorte || ''
        ]
      ],

      columnas: [
        'Cuenta',
        'Documento',
        'Nombre completo',
        'Dirección',
        'Departamento',
        'Ciudad',
        'Zona',
        'Subzona',
        'Teléfono',
        'Celular 1',
        'Celular 2',
        'Correo',
        'Estado cuenta',
        'Fecha apertura',
        'Fecha último movimiento',
        'Créditos',
        'Débitos',
        'Saldo corte'
      ],

      filas,

      anchos: [
        12,
        16,
        38,
        34,
        20,
        20,
        18,
        18,
        16,
        16,
        16,
        30,
        18,
        16,
        18,
        18,
        18,
        18
      ]

    };

  }

  private agrupar(
    items: any[]
  ): any[] {

    const map: any = {};

    for (const it of items) {

      const keyAg =
        it.codigoAgencia || '00';

      const keyFo =
        it.codigoForma || '00';

      if (!map[keyAg]) {

        map[keyAg] = {
          codigoAgencia: it.codigoAgencia || '',
          nombreAgencia: it.agencia || '',
          formas: {}
        };

      }

      if (!map[keyAg].formas[keyFo]) {

        map[keyAg].formas[keyFo] = {
          codigoForma: it.codigoForma || '',
          nombreForma: it.forma || '',
          detalle: []
        };

      }

      map[keyAg]
        .formas[keyFo]
        .detalle
        .push(it);

    }

    return Object.values(map)
      .map((ag: any) => ({

        ...ag,

        formas:
          Object.values(ag.formas)
            .sort((a: any, b: any) =>
              String(a.codigoForma)
                .localeCompare(
                  String(b.codigoForma)
                )
            )

      }))
      .sort((a: any, b: any) =>
        String(a.codigoAgencia)
          .localeCompare(
            String(b.codigoAgencia)
          )
      );

  }

  private nombreHojaSegura(
    nombre: string
  ): string {

    return String(nombre || 'Agencia')
      .replace(/[\\/?*[\]:]/g, '')
      .substring(0, 31);

  }

}
