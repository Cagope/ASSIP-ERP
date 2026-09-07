import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

import {
  GmfSemanalItem,
  GmfSemanalResumen
} from './gmf-semanal.api';

@Injectable({
  providedIn: 'root'
})
export class GmfSemanalExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    items: GmfSemanalItem[],
    resumen: GmfSemanalResumen | null,
    fechaInicial: string,
    fechaFinal: string,
    numeroSemana: number,
    idAgencia: number,
    codigoForma: string
  ): void {

    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `gmf-semanal-${fechaInicial}-${fechaFinal}.xlsx`,

      hojas: [
        {
          nombreHoja: 'GMF semanal',
          titulo: 'INFORME SEMANAL GMF',

          filtros: [
            ['Fecha inicial', fechaInicial || ''],
            ['Fecha final', fechaFinal || ''],
            ['Número de semana', Number(numeroSemana || 0)],
            ['Agencia', idAgencia === 0 ? 'Todas' : idAgencia],
            ['Forma de ahorro', codigoForma === '0' ? 'Todas' : codigoForma]
          ],

          resumen: [
            ['Base asumida', Number(resumen?.totalBaseAsumido || 0)],
            ['GMF asumido', Number(resumen?.totalGmfAsumido || 0)],
            ['Base asociado', Number(resumen?.totalBaseAsociado || 0)],
            ['GMF asociado', Number(resumen?.totalGmfAsociado || 0)],
            ['Retiros gravados', Number(resumen?.totalBaseRetiro || 0)],
            ['GMF retiros', Number(resumen?.totalGmfRetiro || 0)],
            ['Valor exento', Number(resumen?.totalValorExento || 0)],
            ['Base cheque asumido', Number(resumen?.totalBaseChequeAsumido || 0)],
            ['GMF cheque asumido', Number(resumen?.totalGmfChequeAsumido || 0)],
            ['Base cheque exento', Number(resumen?.totalBaseChequeExento || 0)],
            ['GMF cheque exento', Number(resumen?.totalGmfChequeExento || 0)],
            ['Base gravada total', Number(resumen?.totalBaseGravada || 0)],
            ['GMF total', Number(resumen?.totalGmf || 0)]
          ],

          columnas: [
            'Fecha',
            'Cuenta',
            'Tipo movimiento',
            'Descripción movimiento',
            'Documento soporte',
            'Documento asociado',
            'Nombre asociado',
            'Código forma',
            'Nombre forma',
            'Estado GMF',
            'Base asumida',
            'GMF asumido',
            'Base asociado',
            'GMF asociado',
            'Base retiro',
            'GMF retiro',
            'Valor exento',
            'Base cheque asumido',
            'GMF cheque asumido',
            'Base cheque exento',
            'GMF cheque exento'
          ],

          filas: items.map(item => [
            item.fechaMovimiento || '',
            item.codigoCuenta || '',
            item.tipoMovimiento || '',
            item.descripcionMovimiento || '',
            item.documentoSoporte || '',
            item.documentoAsociado || '',
            item.nombreAsociado || '',
            item.codigoForma || '',
            item.nombreForma || '',
            item.estadoGmfCuenta || '',
            Number(item.baseAsumido || 0),
            Number(item.gmfAsumido || 0),
            Number(item.baseAsociado || 0),
            Number(item.gmfAsociado || 0),
            Number(item.baseRetiro || 0),
            Number(item.gmfRetiro || 0),
            Number(item.valorExento || 0),
            Number(item.baseChequeAsumido || 0),
            Number(item.gmfChequeAsumido || 0),
            Number(item.baseChequeExento || 0),
            Number(item.gmfChequeExento || 0)
          ]),

          anchos: [
            14,
            14,
            15,
            30,
            20,
            18,
            38,
            12,
            28,
            12,
            18,
            16,
            18,
            16,
            18,
            16,
            18,
            20,
            20,
            20,
            20
          ]
        }
      ]
    });
  }
}
