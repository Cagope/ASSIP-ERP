import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

import {
  SimuladorAsociado,
  SimuladorCdatModel,
  SimuladorFlujoItem,
  SimuladorResumen
} from './simulador-cdat.models';

@Injectable({
  providedIn: 'root'
})
export class SimuladorCdatExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    persona: SimuladorAsociado | null,
    model: SimuladorCdatModel,
    flujo: SimuladorFlujoItem[],
    resumen: SimuladorResumen | null
  ): void {

    if (!flujo || flujo.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `simulacion-cdat-${persona?.documento || 'cliente'}.xlsx`,

      hojas: [

        {
          nombreHoja: 'Simulación CDAT',

          titulo: 'SIMULACIÓN CDAT',

          filtros: [
            ['Asociado', persona?.nombre_completo || ''],
            ['Documento', persona?.documento || ''],
            ['Cuenta', persona?.codigo_cuenta || ''],
            ['Forma ahorro', persona?.nombre_forma_ahorro || ''],
            ['Agencia', persona?.agencia || ''],
            ['Saldo cuenta', Number(persona?.saldo_actual_cuenta || 0)],
            ['Valor CDAT', Number(model.valorCdat || 0)],
            ['Tasa nominal anual', Number(model.tasaNominalAnual || 0)],
            ['Tasa efectiva anual', Number(model.tasaEfectivaAnual || 0)],
            ['Fecha apertura', model.fechaApertura || ''],
            ['Fecha vencimiento', model.fechaVencimiento || ''],
            ['Plazo meses', Number(model.plazoMeses || 0)],
            ['Pago intereses', model.formaPagoInteres || ''],
            ['Aplica retención', model.aplicaRetencion ? 'Sí' : 'No'],
            ['Base retención', Number(model.baseRetencion || 0)],
            ['Porcentaje retención', Number(model.porcentajeRetencion || 0)]
          ],

          resumen: [
            ['Capital', Number(resumen?.capital || 0)],
            ['Total interés bruto', Number(resumen?.totalInteresBruto || 0)],
            ['Total retención', Number(resumen?.totalRetencion || 0)],
            ['Total interés neto', Number(resumen?.totalInteresNeto || 0)],
            ['Total pago cliente', Number(resumen?.totalPagoCliente || 0)],
            ['Valor al vencimiento', Number(resumen?.valorAlVencimiento || 0)]
          ],

          columnas: [
            'Periodo',
            'Fecha pago',
            'Días',
            'Capital',
            'Interés bruto',
            'Retención',
            'Interés neto',
            'Pago cliente',
            'Saldo final'
          ],

          filas: flujo.map((item: SimuladorFlujoItem) => [
            item.periodo,
            item.fechaPago || '',
            Number(item.dias || 0),
            Number(item.capital || 0),
            Number(item.interesBruto || 0),
            Number(item.valorRetencion || 0),
            Number(item.interesNeto || 0),
            Number(item.pagoCliente || 0),
            Number(item.saldoFinal || 0)
          ]),

          anchos: [
            12,
            18,
            10,
            18,
            18,
            18,
            18,
            18,
            18
          ]
        }

      ]

    });
  }
}
