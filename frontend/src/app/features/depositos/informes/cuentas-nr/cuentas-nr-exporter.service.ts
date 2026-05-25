import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class CuentasNRExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    items: any[],
    tipoInforme: 'NUEVAS' | 'RETIRADAS'
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const agencias =
      this.agrupar(items);

    const hojas: ExcelSheetOptions[] = [];

    for (const ag of agencias) {

      hojas.push(
        this.crearHojaAgencia(
          ag,
          tipoInforme
        )
      );

    }

    this.excelExport.exportar({

      nombreArchivo:
        tipoInforme === 'NUEVAS'
          ? 'cuentas_nuevas.xlsx'
          : 'cuentas_retiradas.xlsx',

      hojas

    });

  }

  private crearHojaAgencia(
    agencia: any,
    tipoInforme: 'NUEVAS' | 'RETIRADAS'
  ): ExcelSheetOptions {

    const filas: any[][] = [];

    for (const forma of agencia.formas) {

      filas.push([
        `FORMA ${forma.codigoForma}`,
        forma.nombreForma
      ]);

      filas.push([]);

      forma.detalle.forEach((d: any) => {

        if (tipoInforme === 'NUEVAS') {

          filas.push([
            d.codigo_cuenta || '',
            d.documento || '',
            d.nombre_completo || '',
            d.agencia || '',
            d.forma || '',
            d.fecha_apertura || '',
            Number(d.saldo_inicial || 0)
          ]);

        } else {

          filas.push([
            d.codigo_cuenta || '',
            d.documento || '',
            d.nombre_completo || '',
            d.agencia || '',
            d.forma || '',
            d.fecha_retiro || '',
            Number(d.ultimo_saldo || 0)
          ]);

        }

      });

      const totalForma =
        forma.detalle.reduce(
          (acc: number, x: any) => {

            const valor =
              tipoInforme === 'NUEVAS'
                ? Number(x.saldo_inicial || 0)
                : Number(x.ultimo_saldo || 0);

            return acc + valor;

          },
          0
        );

      filas.push([
        '',
        '',
        '',
        '',
        '',
        `TOTAL ${forma.nombreForma}`,
        totalForma
      ]);

      filas.push([]);
      filas.push([]);

    }

    const totalAgencia =
      agencia.formas
        .flatMap((f: any) => f.detalle)
        .reduce((acc: number, x: any) => {

          const valor =
            tipoInforme === 'NUEVAS'
              ? Number(x.saldo_inicial || 0)
              : Number(x.ultimo_saldo || 0);

          return acc + valor;

        }, 0);

    filas.push([
      '',
      '',
      '',
      '',
      '',
      'TOTAL AGENCIA',
      totalAgencia
    ]);

    return {

      nombreHoja:
        this.nombreHojaSegura(
          `${agencia.codigoAgencia}-${agencia.nombreAgencia}`
        ),

      titulo:
        tipoInforme === 'NUEVAS'
          ? 'CUENTAS NUEVAS'
          : 'CUENTAS RETIRADAS',

      filtros: [
        ['Agencia', agencia.codigoAgencia],
        ['Nombre agencia', agencia.nombreAgencia]
      ],

      columnas:
        tipoInforme === 'NUEVAS'
          ? [
            'Cuenta',
            'Documento',
            'Nombre',
            'Agencia',
            'Forma',
            'Fecha apertura',
            'Saldo inicial'
          ]
          : [
            'Cuenta',
            'Documento',
            'Nombre',
            'Agencia',
            'Forma',
            'Fecha retiro',
            'Último saldo'
          ],

      filas,

      anchos: [
        12,
        14,
        32,
        24,
        20,
        14,
        16
      ]

    };

  }

  private agrupar(
    items: any[]
  ): any[] {

    const map: any = {};

    for (const it of items) {

      const keyAg =
        it.codigo_agencia || it.codigoAgencia;

      const keyFo =
        it.codigo_forma || it.codigoForma;

      if (!map[keyAg]) {

        map[keyAg] = {

          codigoAgencia: keyAg,

          nombreAgencia: it.agencia,

          formas: {}

        };

      }

      if (!map[keyAg].formas[keyFo]) {

        map[keyAg].formas[keyFo] = {

          codigoForma: keyFo,

          nombreForma: it.forma,

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

    return String(nombre || 'Detalle')
      .replace(/[\\/?*[\]:]/g, '')
      .substring(0, 31);

  }

}
