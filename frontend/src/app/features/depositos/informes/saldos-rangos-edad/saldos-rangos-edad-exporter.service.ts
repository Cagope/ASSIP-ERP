import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

import {
  SaldosRangosEdadItem,
  SaldosRangosEdadResumen
} from './saldos-rangos-edad.api';

@Injectable({
  providedIn: 'root'
})
export class SaldosRangosEdadExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    resumen: SaldosRangosEdadResumen[],
    items: SaldosRangosEdadItem[],
    fechaCorte: string
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `saldos-rangos-edad-${fechaCorte}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Resumen',

          titulo:
            'Saldos por rangos de edad',

          filtros: [
            ['Fecha corte', fechaCorte]
          ],

          columnas: [
            'Rango',
            'Edad inicial',
            'Edad final',
            'Asociados',
            'Saldo total aportes',
            'Saldo promedio aportes',
            'Salario promedio'
          ],

          filas: (resumen || []).map(r => [
            r.nombreRango || '',
            Number(r.edadInicial || 0),
            Number(r.edadFinal || 0),
            Number(r.cantidadAsociados || 0),
            Number(r.saldoTotalAportes || 0),
            Number(r.saldoPromedioAportes || 0),
            Number(r.salarioPromedio || 0)
          ]),

          anchos: [
            20,
            18,
            18,
            14,
            22,
            24,
            20
          ]
        },

        {
          nombreHoja:
            'Detalle',

          titulo:
            'Detalle individual',

          filtros: [
            ['Fecha corte', fechaCorte]
          ],

          columnas: [
            'Rango',
            'Documento',
            'Nombre',
            'Edad',
            'Ocupación',
            'Sector económico',
            'Salario',
            'Otros ingresos',
            'Total ingresos',
            'Ciudad',
            'Celular',
            'Correo',
            'Saldo aportes',
            'Agencia'
          ],

          filas: items.map(item => [
            item.nombreRango || '',
            item.documento || '',
            item.nombreCompleto || '',
            Number(item.edad || 0),
            item.ocupacion || '',
            item.sectorEconomico || '',
            Number(item.salario || 0),
            Number(item.otrosIngresos || 0),
            Number(item.totalIngresos || 0),
            item.ciudad || '',
            item.celular || '',
            item.correo || '',
            Number(item.saldoAportes || 0),
            item.nombreAgencia || ''
          ]),

          anchos: [
            20,
            18,
            40,
            10,
            28,
            28,
            18,
            18,
            18,
            24,
            18,
            34,
            18,
            28
          ]
        }

      ]

    });
  }
}
