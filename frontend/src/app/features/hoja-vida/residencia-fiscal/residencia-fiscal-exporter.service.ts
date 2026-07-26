import {
  Injectable
} from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  ResidenciaFiscal
} from './residencia-fiscal.api';

@Injectable({
  providedIn: 'root'
})
export class ResidenciaFiscalExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    registros: (
      ResidenciaFiscal & {
        documento?: string;
        nombrePersona?: string;
      }
    )[]
  ): void {

    if (!registros || registros.length === 0) {
      alert(
        'No hay registros de Residencia Fiscal para exportar.'
      );
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `residencia_fiscal_${this.fechaArchivo()}.xlsx`,

      hojas: [

        {

          nombreHoja:
            'ResidenciaFiscal',

          titulo:
            'RESIDENCIA FISCAL (FATCA / CRS)',

          columnas: [

            'Documento',

            'Nombre Persona',

            'Ciudadano Estados Unidos',

            'Residente Fiscal Estados Unidos',

            'Residente Fiscal Exterior',

            'País Residencia Fiscal',

            'Ciudad Residencia Fiscal',

            'Dirección Residencia Fiscal',

            'Tipo Identificación Fiscal',

            'Número Identificación Fiscal',

            'Observaciones',

            'Fecha Creación',

            'Fecha Edición'

          ],

          filas: registros.map(r => [

            r.documento || '',

            r.nombrePersona || '',

            r.ciudadanoEstadosUnidos
              ? 'Sí'
              : 'No',

            r.residenteFiscalEstadosUnidos
              ? 'Sí'
              : 'No',

            r.residenteFiscalExterior
              ? 'Sí'
              : 'No',

            r.paisResidenciaFiscal || '',

            r.ciudadResidenciaFiscal || '',

            r.direccionResidenciaFiscal || '',

            r.tipoIdentificacionFiscal || '',

            r.numeroIdentificacionFiscal || '',

            r.observaciones || '',

            r.fechaCreacion
              ? new Date(
                  r.fechaCreacion
                ).toLocaleString()
              : '',

            r.fechaEdicion
              ? new Date(
                  r.fechaEdicion
                ).toLocaleString()
              : ''

          ]),

          anchos: [

            15, // Documento

            32, // Nombre

            18, // Ciudadano USA

            20, // Residente USA

            18, // Exterior

            25, // País

            22, // Ciudad

            38, // Dirección

            24, // Tipo identificación

            24, // Número identificación

            45, // Observaciones

            20, // Fecha creación

            20  // Fecha edición

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
