import { Injectable } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  Laboral
} from '../../../shared/models/laboral.model';

import {
  CatalogosApi,
  CodigoNombreDTO,
  Departamento,
  Ciudad
} from '../../../shared/catalogos/catalogos.api';

@Injectable({
  providedIn: 'root'
})
export class LaboralesExporterService {

  constructor(
    private catalogos: CatalogosApi,
    private excelExport: ExcelExportService
  ) {
  }

  exportarExcel(
    laborales: (Laboral & {
      documento?: string;
      nombrePersona?: string;
    })[]
  ): void {

    if (!laborales || laborales.length === 0) {
      alert('No hay registros laborales para exportar.');
      return;
    }

    const idsDepartamentos = [
      ...new Set(
        laborales
          .map(l => l.idDepartamento)
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

        this.excelExport.exportar({

          nombreArchivo:
            `laborales_${this.fechaArchivo()}.xlsx`,

          hojas: [

            {
              nombreHoja:
                'Laborales',

              titulo:
                'INFORMACIÓN LABORAL',

              columnas: [
                'Documento',
                'Nombre Persona',
                'Nombre Empresa / Actividad',
                'Dirección',
                'Teléfono Empresa',
                'Celular Empresa',
                'Correo Empresa',
                'País',
                'Departamento',
                'Ciudad',
                'Tipo Empresa',
                'Tipo Contrato',
                'Jornada',
                'Fecha Vinculación',
                'Fecha Creación',
                'Fecha Edición'
              ],

              filas: laborales.map(l => [
                l.documento || '',
                l.nombrePersona || '',
                l.nombreEmpresa || '',
                l.direccion || '',
                l.telefonoEmpresa || '',
                l.celularEmpresa || '',
                l.correoEmpresa || '',
                mapPaises.get(l.idPais || '') || '',
                mapDeptos.get(l.idDepartamento || 0) || '',
                mapCiudades.get(l.idCiudad || 0) || '',
                l.codigoTipoEmpresa || '',
                l.codigoTipoContrato || '',
                l.codigoJornada || '',
                l.fechaVinculacion
                  ? new Date(l.fechaVinculacion).toLocaleDateString()
                  : '',
                l.fechaCreacion
                  ? new Date(l.fechaCreacion).toLocaleString()
                  : '',
                l.fechaEdicion
                  ? new Date(l.fechaEdicion).toLocaleString()
                  : ''
              ]),

              anchos: [
                14,
                28,
                38,
                28,
                14,
                14,
                28,
                18,
                22,
                24,
                16,
                16,
                14,
                18,
                20,
                20
              ]
            }

          ]

        });
      },

      error: err => {
        console.error('Error exportando información laboral:', err);
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
