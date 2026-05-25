import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class NovedadesConceptoInformeExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: any[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        'informe_novedades_concepto.xlsx',

      hojas: [

        {
          nombreHoja:
            'Informe',

          titulo:
            'INFORME NOVEDADES POR CONCEPTO',

          columnas: [
            'Año',
            'Mes',
            'Período',
            'Documento',
            'Empleado',
            'Agencia',
            'Concepto',
            'Nombre Concepto',
            'Tipo',
            'Fecha Inicial',
            'Fecha Final',
            'Cantidad',
            'Valor',
            'Origen',
            'Estado',
            'Observación'
          ],

          filas: items.map(x => [
            x.anio ?? '',
            x.mes ?? '',
            x.numeroPeriodo ?? '',
            x.documento || '',
            x.nombreEmpleado || '',
            `${x.codigoAgencia || ''} - ${x.nombreAgencia || ''}`,
            x.codigoConcepto || '',
            x.nombreConcepto || '',
            x.tipoConcepto || '',
            x.fechaInicial || '',
            x.fechaFinal || '',
            Number(x.cantidad || 0),
            Number(x.valor || 0),
            x.origen || '',
            x.estado || '',
            x.observacion || ''
          ]),

          anchos: [
            10,
            10,
            10,
            18,
            38,
            28,
            14,
            40,
            14,
            16,
            16,
            16,
            18,
            18,
            16,
            40
          ]
        }

      ]

    });
  }
}
