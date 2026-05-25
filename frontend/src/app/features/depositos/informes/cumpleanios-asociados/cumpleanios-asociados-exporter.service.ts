import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

import {
  CumpleaniosAsociadosItem,
  CumpleaniosAsociadosResumen
} from './cumpleanios-asociados.api';

@Injectable({
  providedIn: 'root'
})
export class CumpleaniosAsociadosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    resumen: CumpleaniosAsociadosResumen | null,
    items: CumpleaniosAsociadosItem[],
    fechaInicial: string,
    fechaFinal: string
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `cumpleanios-asociados-${fechaInicial}-${fechaFinal}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Cumpleaños',

          titulo:
            'Cumpleaños de asociados',

          filtros: [
            ['Fecha inicial', fechaInicial],
            ['Fecha final', fechaFinal]
          ],

          resumen: [
            ['Total asociados', Number(resumen?.totalAsociados || 0)],
            ['Rango fechas', resumen?.rangoFechas || '']
          ],

          columnas: [
            'Documento',
            'Nombre',
            'Fecha nacimiento',
            'Día',
            'Edad',
            'Ciudad',
            'Celular',
            'Correo',
            'Saldo aportes',
            'Agencia'
          ],

          filas: items.map(item => [
            item.documento || '',
            item.nombreCompleto || '',
            item.fechaNacimiento || '',
            Number(item.diaCumpleanios || 0),
            Number(item.edad || 0),
            item.ciudad || '',
            item.celular || '',
            item.correo || '',
            Number(item.saldoAportes || 0),
            item.nombreAgencia || ''
          ]),

          anchos: [
            18,
            40,
            18,
            10,
            10,
            24,
            18,
            36,
            18,
            28
          ]
        }

      ]

    });
  }
}
