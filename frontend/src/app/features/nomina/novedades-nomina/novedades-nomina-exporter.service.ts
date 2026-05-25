import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

@Injectable({
  providedIn: 'root'
})
export class NovedadesNominaExporterService {

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
        'novedades_nomina.xlsx',

      hojas: [

        {
          nombreHoja:
            'Novedades',

          titulo:
            'NOVEDADES NÓMINA',

          columnas: [
            'ID Novedad',
            'ID Empleado',
            'Documento',
            'Nombre Empleado',
            'ID Contrato',
            'Concepto',
            'Fecha Inicial',
            'Fecha Final',
            'Cantidad',
            'Valor',
            'Estado',
            'Observación',
            'Año',
            'Mes',
            'Número Período',
            'Tipo Período',
            'Agencia'
          ],

          filas: items.map(x => [
            x.idNovedad ?? '',
            x.idEmpleado ?? '',
            x.documentoEmpleado ?? '',
            x.nombreEmpleado ?? '',
            x.idContrato ?? '',
            x.codigoConcepto ?? '',
            x.fechaInicial ?? '',
            x.fechaFinal ?? '',
            Number(x.cantidad ?? 0),
            Number(x.valor ?? 0),
            x.estado ?? '',
            x.observacion ?? '',
            x.anio ?? '',
            x.mes ?? '',
            x.numeroPeriodo ?? '',
            x.tipoPeriodo ?? '',
            x.fkAgencia ?? ''
          ]),

          anchos: [
            14,
            14,
            18,
            40,
            14,
            16,
            16,
            16,
            14,
            18,
            16,
            42,
            10,
            10,
            16,
            18,
            14
          ]
        }

      ]

    });
  }
}
