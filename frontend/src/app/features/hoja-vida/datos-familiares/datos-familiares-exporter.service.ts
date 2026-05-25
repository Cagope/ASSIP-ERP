import { Injectable } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  DatosFamiliar
} from '../../../shared/models/datos-familiar.model';

import {
  CatalogosApi,
  Departamento,
  Ciudad
} from '../../../shared/catalogos/catalogos.api';

@Injectable({
  providedIn: 'root'
})
export class DatosFamiliaresExporterService {

  constructor(
    private catalogos: CatalogosApi,
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    familiares: (DatosFamiliar & {
      documento?: string;
      nombrePersona?: string;
    })[]
  ): void {

    if (!familiares || familiares.length === 0) {
      alert('No hay registros familiares para exportar.');
      return;
    }

    const idsDepartamentos = [
      ...new Set(
        familiares
          .map(f => f.idDepartamento)
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
      departamentos:
        this.catalogos.listarDepartamentos(),

      ciudadesPorDepto:
        forkJoin(observablesCiudades)
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
            `datos_familiares_${this.fechaArchivo()}.xlsx`,

          hojas: [

            {
              nombreHoja:
                'DatosFamiliares',

              titulo:
                'DATOS FAMILIARES',

              columnas: [
                'Documento Titular',
                'Nombre Titular',
                'Nombre Familiar',
                'Parentesco',
                'Documento Familiar',
                'Dirección',
                'Teléfono',
                'Celular',
                'Departamento',
                'Ciudad',
                'Ingresos Mensuales',
                'Egresos Mensuales',
                'Referencia Familiar',
                'Fecha Creación',
                'Fecha Edición'
              ],

              filas: familiares.map(f => [
                f.documento || '',
                f.nombrePersona || '',
                f.nombreDatosFamiliar || '',
                f.codigoParentesco || '',
                f.documentoDatosFamiliar || '',
                f.direccionDatosFamiliar || '',
                f.telefonoDatosFamiliar || '',
                f.celularDatosFamiliar || '',
                mapDeptos.get(f.idDepartamento || 0) || '',
                mapCiudades.get(f.idCiudad || 0) || '',
                Number(f.ingresosDatosFamiliar || 0),
                Number(f.egresosDatosFamiliar || 0),
                f.referenciaFamiliar ? 'Sí' : 'No',
                f.fechaCreacion
                  ? new Date(f.fechaCreacion).toLocaleString()
                  : '',
                f.fechaEdicion
                  ? new Date(f.fechaEdicion).toLocaleString()
                  : ''
              ]),

              anchos: [
                16,
                30,
                30,
                14,
                18,
                32,
                12,
                12,
                22,
                22,
                18,
                18,
                18,
                22,
                22
              ]
            }

          ]

        });
      },

      error: err => {
        console.error('Error exportando información familiar:', err);
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
