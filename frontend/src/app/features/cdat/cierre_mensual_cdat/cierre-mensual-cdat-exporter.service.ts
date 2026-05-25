import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  CdatCierreMensualPreviewDTO
} from './cierre-mensual-cdat.api';

@Injectable({
  providedIn: 'root'
})
export class CierreMensualCdatExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    preview: CdatCierreMensualPreviewDTO
  ): void {

    if (!preview?.items?.length) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `cierre-mensual-cdat-${preview.fechaCorte || 'proceso'}.xlsx`,

      hojas: [

        {
          nombreHoja: 'Cierre CDAT',

          titulo: 'CIERRE MENSUAL CDAT',

          filtros: [
            ['Agencia', preview.idAgencia ?? ''],
            ['Fecha corte', preview.fechaCorte ?? ''],
            ['Año', preview.anio ?? ''],
            ['Mes', preview.mes ?? '']
          ],

          resumen: [
            ['Total CDAT', preview.totalCdats ?? 0],
            ['Total valor apertura', Number(preview.totalValorApertura || 0)],
            ['Total capital', Number(preview.totalCapital || 0)]
          ],

          columnas: [
            'CDAT',
            'Documento',
            'Nombre',
            'Documento cotitular',
            'Nombre cotitular',
            'Fecha apertura',
            'Fecha vencimiento',
            'Última liquidación',
            'Próxima liquidación',
            'Valor apertura',
            'Saldo actual',
            'Tasa nominal anual',
            'Tasa efectiva anual',
            'Tasa nominal mensual',
            'Tasa efectiva mensual',
            'Retención',
            'Modalidad',
            'Amortización',
            'Cuenta aportes',
            'Cuenta ahorro',
            'Cuenta conjunta',
            'Estado'
          ],

          filas: [
            ...preview.items.map(item => [
              item.codigoCdat || '',
              item.documento || '',
              item.nombreCompleto || '',
              item.documentoCotitular || '',
              item.nombreCotitular || '',
              item.fechaAperturaCdat || '',
              item.fechaVencimientoCdat || '',
              item.fechaUltimaLiquidacion || '',
              item.fechaProximaLiquidacion || '',
              Number(item.valorAperturaCdat || 0),
              Number(item.saldoActualCdat || 0),
              Number(item.tasaNominalAnual || 0),
              Number(item.tasaEfectivaAnual || 0),
              Number(item.tasaNominalMensual || 0),
              Number(item.tasaEfectivaMensual || 0),
              item.retencionFuenteCdat ? 'Sí' : 'No',
              item.modalidadCdat || '',
              item.amortizacionDeposito || '',
              item.codigoCuentaAportes || '',
              item.codigoCuentaAhorro || '',
              item.cuentaConjunta ? 'Sí' : 'No',
              item.estadoCdat || ''
            ]),

            [
              '',
              '',
              'TOTALES',
              '',
              '',
              '',
              '',
              '',
              '',
              Number(preview.totalValorApertura || 0),
              Number(preview.totalCapital || 0),
              '',
              '',
              '',
              '',
              '',
              '',
              '',
              '',
              '',
              '',
              ''
            ]
          ],

          anchos: [
            14,
            16,
            42,
            20,
            42,
            16,
            16,
            18,
            18,
            16,
            16,
            18,
            18,
            20,
            20,
            12,
            12,
            14,
            16,
            16,
            16,
            12
          ]
        }

      ]

    });
  }
}
