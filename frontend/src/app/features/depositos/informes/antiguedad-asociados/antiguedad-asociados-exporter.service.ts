import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

import {
  AntiguedadAsociadosItem,
  AntiguedadAsociadosResumen
} from './antiguedad-asociados.api';

@Injectable({
  providedIn: 'root'
})
export class AntiguedadAsociadosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    resumen: AntiguedadAsociadosResumen[],
    items: AntiguedadAsociadosItem[],
    fechaCorte: string
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `antiguedad-asociados-${fechaCorte}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Resumen',

          titulo:
            'Antigüedad de asociados',

          filtros: [
            [
              'Fecha corte',
              fechaCorte
            ]
          ],

          columnas: [
            'Rango',
            'Cantidad asociados',
            'Saldo total',
            'Saldo promedio',
            'Edad promedio'
          ],

          filas: (resumen || []).map(item => [

            item.rangoAntiguedad || '',

            Number(item.cantidadAsociados || 0),

            Number(item.saldoTotalAportes || 0),

            Number(item.saldoPromedioAportes || 0),

            Number(item.edadPromedio || 0)

          ]),

          anchos: [
            28,
            18,
            18,
            18,
            16
          ]
        },

        {
          nombreHoja:
            'Detalle',

          titulo:
            'Detalle individual',

          filtros: [
            [
              'Fecha corte',
              fechaCorte
            ]
          ],

          columnas: [
            'Documento',
            'Nombre',
            'Fecha vinculación',
            'Años asociado',
            'Edad',
            'Saldo aportes',
            'Ciudad',
            'Departamento',
            'Celular',
            'Correo',
            'Agencia'
          ],

          filas: items.map(item => [

            item.documento || '',

            item.nombreCompleto || '',

            item.fechaVinculacion || '',

            Number(item.aniosAsociado || 0),

            Number(item.edad || 0),

            Number(item.saldoAportes || 0),

            item.ciudad || '',

            item.departamento || '',

            item.celular || '',

            item.correo || '',

            item.nombreAgencia || ''

          ]),

          anchos: [
            18,
            40,
            18,
            18,
            12,
            18,
            24,
            24,
            18,
            36,
            28
          ]
        }

      ]

    });
  }
}
