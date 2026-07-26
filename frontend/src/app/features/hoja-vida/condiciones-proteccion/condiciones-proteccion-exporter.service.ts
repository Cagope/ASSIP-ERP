import { Injectable } from '@angular/core';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  CondicionProteccion
} from './condiciones-proteccion.api';

@Injectable({
  providedIn: 'root'
})
export class CondicionesProteccionExporterService {

  constructor(
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    registros: (CondicionProteccion & {
      documento?: string;
      nombrePersona?: string;
    })[]
  ): void {

    if (!registros || registros.length === 0) {
      alert(
        'No hay registros de Condiciones de Protección para exportar.'
      );
      return;
    }

    this.excelExport.exportar({

      nombreArchivo:
        `condiciones_proteccion_${this.fechaArchivo()}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'CondicionesProteccion',

          titulo:
            'CONDICIONES DE PROTECCIÓN',

          columnas: [
            'Documento',
            'Nombre Persona',
            'Administra Recursos Públicos',
            'Grupo de Protección Especial Constitucional',
            'Persona Mayor de 60 Años',
            'Discapacidad Física',
            'Víctima del Conflicto Armado',
            'Pobreza Extrema',
            'Población Indígena',
            'Población Afrodescendiente',
            'Población LGBTIQ+',
            'Pertenece a Grupo de Protección Constitucional',
            'Observaciones',
            'Fecha Creación',
            'Fecha Edición'
          ],

          filas: registros.map(p => [
            p.documento || '',
            p.nombrePersona || '',

            p.administraRecursosPublicos
              ? 'Sí'
              : 'No',

            p.grupoProteccionEspecialConstitucional
              ? 'Sí'
              : 'No',

            p.personaMayor60Anos
              ? 'Sí'
              : 'No',

            p.discapacidadFisica
              ? 'Sí'
              : 'No',

            p.victimaConflictoArmado
              ? 'Sí'
              : 'No',

            p.pobrezaExtrema
              ? 'Sí'
              : 'No',

            p.poblacionIndigena
              ? 'Sí'
              : 'No',

            p.poblacionAfrodescendiente
              ? 'Sí'
              : 'No',

            p.poblacionLgbtiqMas
              ? 'Sí'
              : 'No',

            p.perteneceGrupoProteccionConstitucional
              ? 'Sí'
              : 'No',

            p.observaciones || '',

            p.fechaCreacion
              ? new Date(p.fechaCreacion).toLocaleString()
              : '',

            p.fechaEdicion
              ? new Date(p.fechaEdicion).toLocaleString()
              : ''
          ]),

          anchos: [
            16,
            30,
            28,
            38,
            24,
            22,
            28,
            20,
            22,
            28,
            22,
            42,
            50,
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

    return `${fecha.getFullYear()}${String(
      fecha.getMonth() + 1
    ).padStart(2, '0')}${String(
      fecha.getDate()
    ).padStart(2, '0')}`;
  }
}
