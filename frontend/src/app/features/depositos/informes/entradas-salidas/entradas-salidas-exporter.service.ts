import { Injectable } from '@angular/core';

import {
  ExcelExportService,
  ExcelSheetOptions
} from '../../../../shared/services/excel-export.service';

import {
  EntradasSalidasItem,
  EntradasSalidasResumen
} from './entradas-salidas.api';

@Injectable({
  providedIn: 'root'
})
export class EntradasSalidasExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    items: EntradasSalidasItem[],
    resumen: EntradasSalidasResumen | null,
    fechaInicial: string,
    fechaFinal: string,
    itemsFormas: EntradasSalidasItem[] = [],
    itemsDetalle: EntradasSalidasItem[] = [],
    fechaSeleccionada: string = '',
    formaSeleccionada: string = '',
    resumenDetalle: EntradasSalidasResumen | null = null
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const hojas: ExcelSheetOptions[] = [
      this.crearHojaResumenFechas(
        items,
        resumen,
        fechaInicial,
        fechaFinal
      )
    ];

    if (itemsFormas && itemsFormas.length > 0) {
      hojas.push(
        this.crearHojaResumenFormas(
          itemsFormas,
          fechaSeleccionada
        )
      );
    }

    if (itemsDetalle && itemsDetalle.length > 0) {
      hojas.push(
        this.crearHojaDetalle(
          itemsDetalle,
          fechaSeleccionada,
          formaSeleccionada,
          resumenDetalle
        )
      );
    }

    this.excelExport.exportar({

      nombreArchivo:
        `entradas-salidas-${fechaInicial}-${fechaFinal}.xlsx`,

      hojas

    });
  }

  private crearHojaResumenFechas(
    items: EntradasSalidasItem[],
    resumen: EntradasSalidasResumen | null,
    fechaInicial: string,
    fechaFinal: string
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'Resumen fechas',

      titulo:
        'ENTRADAS Y SALIDAS',

      filtros: [
        ['Fecha inicial', fechaInicial || ''],
        ['Fecha final', fechaFinal || '']
      ],

      resumen: [
        ['Total movimientos', Number(resumen?.totalMovimientos || 0)],
        ['Total entradas', Number(resumen?.totalEntradas || 0)],
        ['Total salidas', Number(resumen?.totalSalidas || 0)]
      ],

      columnas: [
        'Fecha',
        'Movimientos',
        'Entradas',
        'Salidas',
        'Neto'
      ],

      filas: items.map(item => [

        item.fechaMovimiento || '',

        Number(item.cantidadMovimientos || 0),

        Number(item.entradas || 0),

        Number(item.salidas || 0),

        Number(item.neto || 0)

      ]),

      anchos: [
        18,
        16,
        24,
        24,
        18
      ]

    };

  }

  private crearHojaResumenFormas(
    itemsFormas: EntradasSalidasItem[],
    fechaSeleccionada: string
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'Resumen formas',

      titulo:
        `Resumen por forma — ${fechaSeleccionada || ''}`,

      columnas: [
        'Forma',
        'Movimientos',
        'Entradas',
        'Salidas',
        'Neto'
      ],

      filas: itemsFormas.map(forma => [

        `${forma.codigoForma || ''} - ${forma.nombreForma || ''}`,

        Number(forma.cantidadMovimientos || 0),

        Number(forma.entradas || 0),

        Number(forma.salidas || 0),

        Number(forma.neto || 0)

      ]),

      anchos: [
        32,
        16,
        24,
        24,
        18
      ]

    };

  }

  private crearHojaDetalle(
    itemsDetalle: EntradasSalidasItem[],
    fechaSeleccionada: string,
    formaSeleccionada: string,
    resumenDetalle: EntradasSalidasResumen | null
  ): ExcelSheetOptions {

    return {

      nombreHoja:
        'Detalle',

      titulo:
        `Detalle movimientos — ${fechaSeleccionada || ''} — ${formaSeleccionada || ''}`,

      resumen: [
        ['Total movimientos', Number(resumenDetalle?.totalMovimientos || itemsDetalle.length)],
        ['Total salidas', Number(resumenDetalle?.totalSalidas || 0)],
        ['Total entradas', Number(resumenDetalle?.totalEntradas || 0)]
      ],

      columnas: [
        'Cuenta',
        'Documento',
        'Nombre',
        'Tipo movimiento',
        'Número',
        'Débito',
        'Crédito'
      ],

      filas: itemsDetalle.map(d => [

        d.codigoCuenta || '',

        d.documento || '',

        d.nombreCompleto || '',

        `${d.codigoMovimiento || ''} - ${d.nombreMovimiento || ''}`,

        d.numeroMovimiento || '',

        Number(d.debito || 0),

        Number(d.credito || 0)

      ]),

      anchos: [
        18,
        16,
        36,
        32,
        18,
        18,
        18
      ]

    };

  }

}
