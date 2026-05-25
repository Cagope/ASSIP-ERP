import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

import {
  FechasCdatItem,
  FechasCdatResumen,
  FechasCdatTipoInforme
} from './fechas-cdat.models';

@Injectable({
  providedIn: 'root'
})
export class FechasCdatExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    tipoInforme: FechasCdatTipoInforme,
    fechaInicial: string,
    fechaFinal: string,
    resumen: FechasCdatResumen,
    resultados: FechasCdatItem[]
  ): void {

    this.excelExport.exportar({

      nombreArchivo:
        `informe-fechas-cdat-${tipoInforme}-${fechaInicial}-${fechaFinal}.xlsx`,

      hojas: [

        {
          nombreHoja: 'Resumen',

          titulo: 'INFORMES POR FECHAS CDAT',

          filtros: [
            ['Tipo informe', this.nombreTipo(tipoInforme)],
            ['Fecha inicial', fechaInicial || ''],
            ['Fecha final', fechaFinal || ''],
            ['Fecha generación', new Date().toLocaleString('es-CO')]
          ],

          columnas: [
            'Indicador',
            'Valor'
          ],

          filas: [
            ['Cantidad títulos', Number(resumen?.cantidad || 0)],
            ['Valor total', Number(resumen?.valorTotal || 0)],
            ['Promedio tasa', Number(resumen?.promedioTasa || 0)],
            ['Promedio plazo', Number(resumen?.promedioPlazo || 0)]
          ],

          anchos: [
            28,
            24
          ]
        },

        {
          nombreHoja: 'Detalle',

          titulo: 'Detalle informe CDAT',

          columnas: [
            'CDAT',
            'Documento',
            'Asociado',
            'Agencia',
            'Fecha apertura',
            'Fecha vencimiento',
            'Plazo',
            'Tasa',
            'Valor',
            'Estado'
          ],

          filas: (resultados || []).map(r => [
            r.codigoCdat || '',
            r.documento || '',
            r.nombreCompleto || '',
            r.agencia || '',
            r.fechaApertura || '',
            r.fechaVencimiento || '',
            Number(r.plazoMeses || 0),
            Number(r.tasa || 0),
            Number(r.valor || 0),
            r.estado || ''
          ]),

          anchos: [
            14,
            16,
            38,
            24,
            16,
            16,
            10,
            10,
            18,
            18
          ]
        }

      ]

    });
  }

  private nombreTipo(
    tipo: FechasCdatTipoInforme
  ): string {

    switch (tipo) {
      case 'NUEVOS':
        return 'CDAT nuevos';
      case 'CANCELADOS':
        return 'CDAT cancelados';
      case 'VENCER':
        return 'Próximos a vencer';
      case 'VENCIDOS':
        return 'CDAT vencidos';
      case 'RENOVADOS':
        return 'CDAT renovados';
      default:
        return tipo;
    }
  }
}
