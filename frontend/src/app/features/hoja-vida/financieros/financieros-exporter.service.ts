import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  Financiero
} from '../../../shared/models/financiero.model';

@Injectable({
  providedIn: 'root'
})
export class FinancierosExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    financieros: (Financiero & {
      documento?: string;
      nombrePersona?: string;
    })[]
  ): void {

    if (!financieros || financieros.length === 0) {
      alert('No hay registros financieros para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `financieros_${this.fechaArchivo()}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Financieros',

          titulo:
            'INFORMACIÓN FINANCIERA',

          columnas: [
            'Documento',
            'Nombre Persona',
            'Salario',
            'Pensión',
            'Ingresos Arriendo',
            'Comisiones',
            'Otros Ingresos',
            'Comentario Otros Ingresos',
            'Origen de Fondos',
            'Total Ingresos',
            'Egresos Familiares',
            'Egresos Arriendo',
            'Egresos Crédito',
            'Otros Egresos',
            'Comentario Otros Egresos',
            'Total Egresos',
            'Total Activos',
            'Total Pasivos',
            'Patrimonio Neto',
            'Deuda Relación Financiera',
            'Relación Financiera',
            'Fecha Creación',
            'Fecha Actualización'
          ],

          filas: financieros.map(f => {

            const totalIngresos =
              Number(f.valorSalario || 0)
              + Number(f.valorPension || 0)
              + Number(f.ingresosArriendo || 0)
              + Number(f.ingresosComisiones || 0)
              + Number(f.otrosIngresos || 0);

            const totalEgresos =
              Number(f.egresosFamiliares || 0)
              + Number(f.egresosArriendo || 0)
              + Number(f.egresosCredito || 0)
              + Number(f.otrosEgresos || 0);

            const patrimonioNeto =
              Number(f.totalActivos || 0)
              - Number(f.totalPasivos || 0);

            return [
              f.documento || '',
              f.nombrePersona || '',
              Number(f.valorSalario || 0),
              Number(f.valorPension || 0),
              Number(f.ingresosArriendo || 0),
              Number(f.ingresosComisiones || 0),
              Number(f.otrosIngresos || 0),
              f.comentarioOtrosIngresos || '',
              f.origenFondos || '',
              totalIngresos,
              Number(f.egresosFamiliares || 0),
              Number(f.egresosArriendo || 0),
              Number(f.egresosCredito || 0),
              Number(f.otrosEgresos || 0),
              f.comentarioOtrosEgresos || '',
              totalEgresos,
              Number(f.totalActivos || 0),
              Number(f.totalPasivos || 0),
              patrimonioNeto,
              Number(f.deudaRelacionFinanciera || 0),
              f.relacionFinanciera || '',
              f.fechaCreacion
                ? new Date(f.fechaCreacion).toLocaleString()
                : '',
              f.fechaActualizacion
                ? new Date(f.fechaActualizacion).toLocaleString()
                : ''
            ];
          }),

          anchos: [
            14,
            26,
            12,
            12,
            14,
            12,
            14,
            24,
            20,
            16,
            14,
            14,
            14,
            14,
            24,
            16,
            14,
            14,
            14,
            20,
            20,
            20,
            20
          ]
        }

      ]

    });
  }

  private fechaArchivo(): string {

    const fecha =
      new Date();

    return `${fecha.getFullYear()}${String(fecha.getMonth() + 1).padStart(2, '0')}${String(fecha.getDate()).padStart(2, '0')}`;
  }
}
