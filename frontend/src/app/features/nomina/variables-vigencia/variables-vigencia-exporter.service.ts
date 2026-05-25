import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  VariablesVigenciaListDTO,
  VariablesVigenciaFormDTO
} from './variables-vigencia.api';

type ExportDTO =
  VariablesVigenciaListDTO &
  Partial<VariablesVigenciaFormDTO>;

@Injectable({
  providedIn: 'root'
})
export class VariablesVigenciaExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportar(
    items: ExportDTO[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        'variables_vigencia.xlsx',

      hojas: [

        {
          nombreHoja:
            'VariablesVigencia',

          titulo:
            'VARIABLES DE VIGENCIA NÓMINA',

          columnas: [
            'ID',
            'Fecha inicial',
            'Fecha final',

            'SMMLV',
            'Aux transporte',

            '% Salud empleado',
            '% Salud empleador',

            '% Pensión empleado',
            '% Pensión empleador',

            '% Caja compensación',
            '% SENA',
            '% ICBF',

            '% Prov. prima',
            '% Prov. vacaciones',
            '% Prov. cesantías',
            '% Prov. interés cesantías',

            'Tope IBC mínimo (SMMLV)',
            'Tope IBC máximo (SMMLV)',

            'Exonerado salud',
            'Exonerado parafiscales',

            'Activo'
          ],

          filas: items.map(x => [

            x.idVariable ?? '',
            x.fechaInicial ?? '',
            x.fechaFinal ?? '',

            Number((x as any).smmlv ?? 0),
            Number((x as any).auxTransporte ?? 0),

            Number((x as any).porcSaludEmpleado ?? 0),
            Number((x as any).porcSaludEmpleador ?? 0),

            Number((x as any).porcPensionEmpleado ?? 0),
            Number((x as any).porcPensionEmpleador ?? 0),

            Number((x as any).porcCajaCompensacion ?? 0),
            Number((x as any).porcSena ?? 0),
            Number((x as any).porcIcbf ?? 0),

            Number((x as any).porProvisionPrima ?? 0),
            Number((x as any).porProvisionVacaciones ?? 0),
            Number((x as any).porProvisionCesantias ?? 0),
            Number((x as any).porProvisionInteresCesantias ?? 0),

            Number((x as any).topeIbcMinSmmlv ?? 0),
            Number((x as any).topeIbcMaxSmmlv ?? 0),

            (x as any).exoneradoSalud ? 'SI' : 'NO',
            (x as any).exoneradoParafiscales ? 'SI' : 'NO',

            x.activo ? 'SI' : 'NO'
          ]),

          anchos: [
            12,
            16,
            16,

            18,
            18,

            20,
            20,

            20,
            20,

            22,
            16,
            16,

            20,
            22,
            22,
            28,

            24,
            24,

            18,
            24,

            12
          ]
        }

      ]

    });
  }
}
