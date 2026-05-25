import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

export interface DepreciacionInformeExportMeta {
  agencia?: string;
  fechaIni?: string | null;
  fechaFin?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class DepreciacionInformeExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    rows: any[],
    meta?: DepreciacionInformeExportMeta
  ): void {

    if (!rows || rows.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const fechaTexto =
      new Date().toLocaleString('es-CO');

    this.excelExport.exportar({

      nombreArchivo:
        this.buildFileName(meta?.agencia),

      hojas: [

        {
          nombreHoja: 'Filtros',

          titulo: 'Filtros informe depreciación',

          columnas: [
            'Campo',
            'Valor'
          ],

          filas: [
            [
              'Agencia',
              meta?.agencia || 'TODAS'
            ],
            [
              'Fecha Inicial',
              meta?.fechaIni || ''
            ],
            [
              'Fecha Final',
              meta?.fechaFin || ''
            ],
            [
              'Generado',
              fechaTexto
            ],
            [
              'Total filas',
              rows.length
            ]
          ],

          anchos: [
            18,
            60
          ]
        },

        {
          nombreHoja: 'Depreciacion',

          titulo: 'Informe depreciación',

          columnas: [
            'Fecha',
            'Placa',
            'Activo',
            'Valor adquisición',
            'Dep. mensual',
            'Dep. acumulada',
            'Neto'
          ],

          filas: rows.map(r => [
            r?.fecha || '',
            r?.placa_activo || '',
            r?.nombre_activo || '',
            Number(r?.valor_adquisicion || 0),
            Number(r?.valor_depreciacion_mes || 0),
            Number(r?.valor_depreciacion_acumulada || 0),
            Number(r?.valor_neto || 0)
          ]),

          anchos: [
            12,
            14,
            40,
            18,
            16,
            18,
            14
          ]
        }

      ]

    });
  }

  private buildFileName(
    agencia?: string
  ): string {

    const ag =
      (agencia || 'TODAS')
        .toUpperCase()
        .replaceAll(' ', '_')
        .replaceAll('/', '-')
        .replaceAll('.', '');

    const hoy = new Date();

    const yyyy =
      hoy.getFullYear();

    const mm =
      String(hoy.getMonth() + 1)
        .padStart(2, '0');

    const dd =
      String(hoy.getDate())
        .padStart(2, '0');

    return `DEPRECIACION_${ag}_${yyyy}${mm}${dd}.xlsx`;
  }
}
