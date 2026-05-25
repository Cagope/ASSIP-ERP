import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class RevalorizacionExporterService {

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
      `${filtros.fechaInicio}_al_${filtros.fechaFin}`;

    const codigoAgencia =
      filtros.codigoAgencia ?? '00';

    const codigoForma =
      filtros.codigoForma ?? 'XX';

    this.excelExport.exportar({

      nombreArchivo:
        `revalorizacion_aportes_${fecha}_${codigoAgencia}_${codigoForma}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Revalorizacion',

          titulo:
            'REVALORIZACIÓN DE APORTES',

          filtros: [
            ['Fecha inicio', filtros.fechaInicio || ''],
            ['Fecha fin', filtros.fechaFin || ''],
            ['Agencia', codigoAgencia],
            ['Forma', codigoForma]
          ],

          columnas: [
            'Documento',
            'Nombre Completo',
            'Saldo Actual',
            'Valor Promedio',
            'Valor Revalorización',
            'Nuevo Saldo',
            'Tasa (%)',
            'Tiempo Liquidación',
            'Mínimo Forma',
            'Estado Cuenta'
          ],

          filas: items.map((x: any) => [
            x.documento || '',
            x.nombreCompleto || '',
            Number(x.saldoActual || 0),
            Number(x.valorPromedio || 0),
            Number(x.valorRevalorizacion || 0),
            Number(x.nuevoSaldo || 0),
            Number(x.tasaRevalorizacion || 0),
            x.tiempoLiquidacion || '',
            Number(x.minimoForma || 0),
            x.estadoCuenta || ''
          ]),

          anchos: [
            18,
            42,
            18,
            18,
            20,
            18,
            12,
            18,
            18,
            18
          ]
        }

      ]

    });
  }
}
