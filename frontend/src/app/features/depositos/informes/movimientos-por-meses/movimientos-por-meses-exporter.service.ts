import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

import {
  MovimientosPorMesesAsociado,
  MovimientosPorMesesResumen
} from './movimientos-por-meses.api';

@Injectable({
  providedIn: 'root'
})
export class MovimientosPorMesesExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    meses: string[],
    resumen: MovimientosPorMesesResumen[],
    detalleAsociado: MovimientosPorMesesAsociado[],
    tipoInforme: string,
    fechaCorte: string
  ): void {

    if (!meses || meses.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const hojas: ExcelSheetOptions[] = [
      this.crearHojaResumen(
        meses,
        resumen,
        tipoInforme,
        fechaCorte
      )
    ];

    if (
      tipoInforme === 'ASOCIADO'
      && detalleAsociado
      && detalleAsociado.length > 0
    ) {
      hojas.push(
        this.crearHojaDetalleAsociado(
          meses,
          detalleAsociado
        )
      );
    }

    this.excelExport.exportar({

      nombreArchivo:
        `movimientos-por-meses-${fechaCorte}.xlsx`,

      hojas

    });
  }

  private crearHojaResumen(
    meses: string[],
    resumen: MovimientosPorMesesResumen[],
    tipoInforme: string,
    fechaCorte: string
  ): ExcelSheetOptions {

    const columnas =
      meses.flatMap(mes => [
        `${mes} Entradas`,
        `${mes} Salidas`
      ]);

    const filaResumen =
      meses.flatMap(mes => {

        const item =
          resumen.find(r => r.mes === mes);

        return [
          Number(item?.entradas || 0),
          Number(item?.salidas || 0)
        ];
      });

    return {

      nombreHoja:
        'Resumen',

      titulo:
        'Movimientos por meses',

      filtros: [
        ['Fecha corte', fechaCorte],
        ['Tipo informe', tipoInforme]
      ],

      columnas,

      filas: [
        filaResumen
      ],

      anchos:
        meses.flatMap(() => [
          18,
          18
        ])

    };
  }

  private crearHojaDetalleAsociado(
    meses: string[],
    detalleAsociado: MovimientosPorMesesAsociado[]
  ): ExcelSheetOptions {

    const columnas = [
      'Cédula',
      'Nombre',
      'Código',
      ...meses.flatMap(mes => [
        `${mes} Entradas`,
        `${mes} Salidas`
      ])
    ];

    const asociados =
      this.obtenerAsociadosUnicos(
        detalleAsociado
      );

    const filas =
      asociados.map(asociado => {

        const fila: any[] = [
          asociado.documento || '',
          asociado.nombreCompleto || '',
          asociado.codigoForma || ''
        ];

        for (const mes of meses) {

          const item =
            detalleAsociado.find(d =>
              d.documento === asociado.documento
              && d.codigoCuenta === asociado.codigoCuenta
              && d.codigoForma === asociado.codigoForma
              && d.mes === mes
            );

          fila.push(Number(item?.entradas || 0));
          fila.push(Number(item?.salidas || 0));
        }

        return fila;
      });

    return {

      nombreHoja:
        'Detalle asociado',

      titulo:
        'Detalle por asociado',

      columnas,

      filas,

      anchos: [
        18,
        36,
        14,
        ...meses.flatMap(() => [
          18,
          18
        ])
      ]

    };
  }

  private obtenerAsociadosUnicos(
    items: MovimientosPorMesesAsociado[]
  ): MovimientosPorMesesAsociado[] {

    const mapa =
      new Map<string, MovimientosPorMesesAsociado>();

    for (const item of items || []) {

      const key =
        `${item.documento}|${item.codigoCuenta}|${item.codigoForma}`;

      if (!mapa.has(key)) {
        mapa.set(key, item);
      }
    }

    return Array.from(
      mapa.values()
    );
  }
}
