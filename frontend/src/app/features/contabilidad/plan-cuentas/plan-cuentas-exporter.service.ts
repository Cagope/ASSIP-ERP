import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  PlanCuenta
} from './plan-cuentas.api';

@Injectable({
  providedIn: 'root'
})
export class PlanCuentasExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: PlanCuenta[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo: 'plan_cuentas.xlsx',

      hojas: [

        {
          nombreHoja: 'Plan de Cuentas',

          titulo: 'PLAN DE CUENTAS',

          columnas: [
            'Agencia',
            'Código',
            'Nombre',
            'Naturaleza',
            'Nivel',
            'Operable',
            'Control Entrada/Salida',
            'Tipo Especial'
          ],

          filas: items.map(i => [

            i.idAgencia ?? '',

            i.codigoCuenta || '',

            i.nombre || '',

            i.naturaleza === 'D'
              ? 'Débito'
              : 'Crédito',

            i.nivel ?? '',

            i.operable
              ? 'SI'
              : 'NO',

            i.controlEntradaSalida
              ? 'SI'
              : 'NO',

            i.tipoEspecial || ''

          ]),

          anchos: [
            12,
            18,
            42,
            16,
            10,
            12,
            24,
            20
          ]
        }

      ]

    });
  }
}
