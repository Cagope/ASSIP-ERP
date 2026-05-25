import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  Agencia
} from './agencia.api';

@Injectable({
  providedIn: 'root'
})
export class AgenciasExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    agencias: Agencia[]
  ): void {

    if (!agencias || agencias.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `agencias_${this.fechaArchivo()}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Agencias',

          titulo:
            'LISTADO DE AGENCIAS',

          columnas: [
            'Código',
            'Nombre de Agencia',
            'Sigla',
            'Dirección',
            'Departamento',
            'Ciudad',
            'Correo',
            'Celular',
            'Teléfono',
            'Fecha Creación',
            'Fecha Edición'
          ],

          filas: agencias.map(a => [

            a.codigoAgencia || '',

            a.nombreAgencia || '',

            a.siglaAgencia || '',

            a.direccionAgencia || '',

            (a as any).nombreDepartamento ?? a.idDepartamento ?? '',

            (a as any).nombreCiudad ?? a.idCiudad ?? '',

            a.correoAgencia || '',

            a.celularAgencia || '',

            a.telefonoAgencia || '',

            a.fechaCreacion
              ? new Date(a.fechaCreacion).toLocaleString()
              : '',

            a.fechaEdicion
              ? new Date(a.fechaEdicion).toLocaleString()
              : ''

          ]),

          anchos: [
            10,
            32,
            10,
            40,
            24,
            24,
            28,
            14,
            14,
            22,
            22
          ]
        }

      ]

    });
  }

  private fechaArchivo(): string {

    const d =
      new Date();

    return `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}`;
  }
}
