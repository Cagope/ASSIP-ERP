import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

import {
  LiquidacionMovimientoContableDTO
} from '../liquidacion/liquidacion-contabilizacion.api';

@Injectable({
  providedIn: 'root'
})
export class AportesParafiscalesContabilizacionExporter {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarComprobante(
    movimientos: LiquidacionMovimientoContableDTO[],
    params: {
      periodo: number;
      anio: number;
      mes: number;
      numeroPeriodo: number;
      codigoAgencia: string | number;
      fechaContabilizacion: string | null;
      tipoComprobante: string;
      numeroComprobante: string;
    }
  ): void {

    if (!movimientos || movimientos.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const mes2 =
      String(params.mes).padStart(2, '0');

    const agencia2 =
      String(params.codigoAgencia).padStart(2, '0');

    const mesTexto =
      `${params.anio}-${mes2}`;

    this.excelExport.exportar({

      nombreArchivo:
        `comprobante_aportes_parafiscales_${params.anio}_${mes2}_${agencia2}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'AportesParafiscales',

          titulo:
            'COMPROBANTE CONTABLE APORTES EMPLEADOR Y PARAFISCALES',

          filtros: [
            ['Mes nómina', mesTexto],
            ['Fecha contabilización', params.fechaContabilizacion || ''],
            ['Documento', `${params.tipoComprobante} ${params.numeroComprobante}`]
          ],

          columnas: [
            '#',
            'Agencia',
            'Cuenta',
            'Nombre cuenta',
            'Tercero',
            'Empleado referencia',
            'Débito',
            'Crédito',
            'Base movimiento',
            'Documento tercero'
          ],

          filas: movimientos.map((m, i) => [
            i + 1,
            m.idAgencia ?? '',
            m.codigoCuenta || '',
            m.nombreCuenta || '',
            m.nombreTercero || '',
            m.nombreEmpleadoReferencia || '',
            Number(m.debito || 0),
            Number(m.credito || 0),
            Number(m.valorBase || 0),
            m.documentoTercero || ''
          ]),

          anchos: [
            8,
            12,
            18,
            38,
            34,
            34,
            18,
            18,
            18,
            20
          ]
        }

      ]

    });
  }
}
