import { Injectable } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  ReferenciaPersonal
} from '../../../shared/models/referencia-personal.model';

import {
  CatalogosApi,
  Departamento,
  Ciudad
} from '../../../shared/catalogos/catalogos.api';

@Injectable({
  providedIn: 'root'
})
export class ReferenciasPersonalesExporterService {

  constructor(
    private catalogos: CatalogosApi,
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    referencias: (ReferenciaPersonal & {
      documento?: string;
      nombrePersona?: string;
    })[]
  ): void {

    if (!referencias || referencias.length === 0) {
      alert('No hay referencias personales para exportar.');
      return;
    }

    const idsDepartamentos = [
      ...new Set(
        referencias
          .map(r => r.idDepartamento)
          .filter((id): id is number => !!id)
      )
    ];

    const observablesCiudades =
      idsDepartamentos.length > 0
        ? idsDepartamentos.map(id =>
          this.catalogos
            .listarCiudadesPorDepartamento(Number(id))
            .pipe(catchError(() => of([] as Ciudad[])))
        )
        : [of([] as Ciudad[])];

    forkJoin({
      departamentos: this.catalogos.listarDepartamentos(),
      ciudadesPorDepto: forkJoin(observablesCiudades)
    }).subscribe({

      next: cat => {

        const todasCiudades =
          (cat.ciudadesPorDepto || []).flat();

        const mapDeptos =
          new Map<number, string>(
            (cat.departamentos || []).map((d: Departamento) => [
              d.idDepartamento,
              d.nombreDepartamento
            ])
          );

        const mapCiudades =
          new Map<number, string>(
            todasCiudades.map((c: Ciudad) => [
              c.idCiudad,
              c.nombreCiudad
            ])
          );

        this.excelExport.exportar({

          nombreArchivo:
            `referencias_personales_${this.fechaArchivo()}.xlsx`,

          hojas: [

            {
              nombreHoja:
                'ReferenciasPersonales',

              titulo:
                'REFERENCIAS PERSONALES',

              columnas: [
                'Documento',
                'Nombre Persona',
                'Nombre Referencia',
                'Dirección',
                'Departamento',
                'Ciudad',
                'Teléfono',
                'Celular',
                'Fecha Creación',
                'Fecha Edición'
              ],

              filas: referencias.map(r => [
                r.documento || '',
                r.nombrePersona || '',
                r.nombreReferenciaPersonal || '',
                r.direccionReferenciaPersonal || '',
                mapDeptos.get(r.idDepartamento || 0) || '',
                mapCiudades.get(r.idCiudad || 0) || '',
                r.telefonoReferenciaPersonal || '',
                r.celularReferenciaPersonal || '',
                r.fechaCreacion
                  ? new Date(r.fechaCreacion).toLocaleString()
                  : '',
                r.fechaEdicion
                  ? new Date(r.fechaEdicion).toLocaleString()
                  : ''
              ]),

              anchos: [
                14,
                30,
                30,
                40,
                22,
                22,
                14,
                14,
                22,
                22
              ]
            }

          ]

        });
      },

      error: err => {
        console.error('Error exportando referencias personales:', err);
        alert('No se pudieron cargar los catálogos de referencia.');
      }

    });
  }

  private fechaArchivo(): string {

    const fecha =
      new Date();

    return `${fecha.getFullYear()}${String(fecha.getMonth() + 1).padStart(2, '0')}${String(fecha.getDate()).padStart(2, '0')}`;
  }
}
