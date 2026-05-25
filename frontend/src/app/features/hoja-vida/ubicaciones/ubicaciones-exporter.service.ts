import { Injectable } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  Ubicacion
} from '../../../shared/models/ubicacion.model';

import {
  CatalogosApi,
  CodigoNombreDTO,
  Departamento,
  Ciudad
} from '../../../shared/catalogos/catalogos.api';

import {
  GeneralApi,
  SubZonaDTO
} from '../../../shared/general/general.api';

@Injectable({
  providedIn: 'root'
})
export class UbicacionesExporterService {

  constructor(
    private catalogos: CatalogosApi,
    private general: GeneralApi,
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    ubicaciones: (Ubicacion & {
      documento?: string;
      nombrePersona?: string;
    })[]
  ): void {

    if (!ubicaciones || ubicaciones.length === 0) {
      alert('No hay ubicaciones para exportar.');
      return;
    }

    const idsDepartamentos = [
      ...new Set(
        ubicaciones
          .map(u => u.idDepartamento)
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
      paises: this.catalogos.listarPaises(),
      departamentos: this.catalogos.listarDepartamentos(),
      subZonas: this.general.listarSubZonas(),
      ciudadesPorDepto: forkJoin(observablesCiudades)
    }).subscribe({

      next: cat => {

        const todasCiudades =
          (cat.ciudadesPorDepto || []).flat();

        const mapPaises =
          new Map<string | number, string>(
            (cat.paises || []).map((p: CodigoNombreDTO) => [
              p.codigo,
              p.nombre
            ])
          );

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

        const mapSubZonas =
          new Map<string | number, string>(
            (cat.subZonas || []).map((s: SubZonaDTO) => [
              s.idSubZona,
              s.nombreSubZona
            ])
          );

        this.excelExport.exportar({

          nombreArchivo:
            `ubicaciones_${this.fechaArchivo()}.xlsx`,

          hojas: [

            {
              nombreHoja:
                'Ubicaciones',

              titulo:
                'UBICACIONES',

              columnas: [
                'Documento',
                'Nombre Persona',
                'Dirección',
                'Barrio',
                'Teléfono',
                'Celular 1',
                'Celular 2',
                'Correo',
                'País',
                'Departamento',
                'Ciudad',
                'Sub Zona',
                'Fecha Creación',
                'Fecha Edición'
              ],

              filas: ubicaciones.map(u => [
                u.documento || '',
                u.nombrePersona || '',
                u.direccion || '',
                u.barrio || '',
                u.telefono || '',
                u.celularUno || '',
                u.celularDos || '',
                u.correo || '',
                mapPaises.get(u.idPais || '') || '',
                mapDeptos.get(u.idDepartamento || 0) || '',
                mapCiudades.get(u.idCiudad || 0) || '',
                mapSubZonas.get(u.idSubZona || '') || '',
                u.fechaCreacion
                  ? new Date(u.fechaCreacion).toLocaleString()
                  : '',
                u.fechaEdicion
                  ? new Date(u.fechaEdicion).toLocaleString()
                  : ''
              ]),

              anchos: [
                14,
                30,
                40,
                22,
                12,
                14,
                14,
                30,
                20,
                22,
                24,
                22,
                22,
                22
              ]
            }

          ]

        });
      },

      error: err => {
        console.error('Error exportando ubicaciones:', err);
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
