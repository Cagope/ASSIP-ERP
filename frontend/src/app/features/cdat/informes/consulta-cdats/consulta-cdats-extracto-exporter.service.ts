import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class ConsultaCdatsExtractoExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    cdat: any,
    fechaInicial: string,
    fechaFinal: string,
    movimientos: any[],
    resumenPorMovimiento: any[]
  ): void {

    if (!movimientos || movimientos.length === 0) {
      alert('No hay movimientos para exportar.');
      return;
    }

    const nombre =
      cdat?.nombre_completo_apellidos
      || cdat?.nombre_completo_nombres
      || cdat?.nombres
      || '';

    this.excelExport.exportar({

      nombreArchivo:
        `extracto-cdat-${cdat?.codigo_cdat || 'sin-codigo'}.xlsx`,

      hojas: [

        {
          nombreHoja: 'Extracto CDAT',

          titulo: 'EXTRACTO CDAT',

          filtros: [
            ['CDAT', cdat?.codigo_cdat || ''],
            ['Asociado', nombre],
            ['Documento', cdat?.documento || ''],
            ['Fecha inicial', fechaInicial],
            ['Fecha final', fechaFinal],
            ['Saldo actual', Number(cdat?.saldo_actual_cdat || 0)],
            ['Tasa nominal anual', Number(cdat?.tasa_nominal_anual || 0)]
          ],

          columnas: [
            'Fecha',
            'Comprobante',
            'Movimiento',
            'Débito',
            'Crédito'
          ],

          filas: movimientos.map((m: any) => [
            m.fecha_movimiento || '',
            `${m.tipo_comprobante || ''} - ${m.numero_comprobante || ''}`,
            m.descripcion_movimiento || '',
            Number(m.valor_debito || 0),
            Number(m.valor_credito || 0)
          ]),

          anchos: [
            14,
            22,
            42,
            16,
            16
          ]
        },

        {
          nombreHoja: 'Resumen',

          titulo: 'RESUMEN POR MOVIMIENTO',

          columnas: [
            'Tipo',
            'Movimiento',
            'Valor'
          ],

          filas: (resumenPorMovimiento || []).map((r: any) => [
            r.tipoMovimiento || '',
            r.descripcionMovimiento || '',
            Number(r.valor || 0)
          ]),

          anchos: [
            16,
            42,
            18
          ]
        }

      ]

    });
  }
}
