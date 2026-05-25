import { Injectable, inject } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import {
  ExcelExportService
} from '../../../shared/services/excel-export.service';

import {
  EmpleadoListDTO
} from './empleados.api';

import {
  PersonasApi,
  PersonaBusquedaDTO
} from '../../../shared/personas/personas.api';

@Injectable({
  providedIn: 'root'
})
export class EmpleadosExporterService {

  private readonly personasApi =
    inject(PersonasApi);

  private readonly excelExport =
    inject(ExcelExportService);

  exportar(
    items: EmpleadoListDTO[]
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    const ids = Array.from(
      new Set(
        items
          .map(x => x.idDatosPersonal)
          .filter((x): x is number => !!x)
      )
    );

    const requests =
      ids.map(id =>
        this.personasApi.obtenerPorId(id).pipe(
          map(p => ({
            id,
            persona: p
          })),
          catchError(() =>
            of({
              id,
              persona: null
            })
          )
        )
      );

    forkJoin(requests).subscribe({
      next: rows => {

        const mapPersonas =
          new Map<number, PersonaBusquedaDTO>();

        for (const r of rows) {
          if (r.persona) {
            mapPersonas.set(
              r.id,
              r.persona
            );
          }
        }

        this.excelExport.exportar({

          nombreArchivo:
            'empleados.xlsx',

          hojas: [

            {
              nombreHoja:
                'Empleados',

              titulo:
                'LISTADO DE EMPLEADOS',

              columnas: [
                'ID Empleado',
                'ID Persona',
                'Documento',
                'Nombre Completo',
                'ID Agencia',
                'Activo'
              ],

              filas: items.map(x => {

                const p =
                  mapPersonas.get(x.idDatosPersonal);

                return [
                  x.idEmpleado ?? '',
                  x.idDatosPersonal ?? '',
                  p?.documento || '',
                  p?.nombreCompleto || '',
                  x.idAgencia ?? '',
                  x.activo ? 'SI' : 'NO'
                ];
              }),

              anchos: [
                14,
                14,
                18,
                42,
                14,
                12
              ]
            }

          ]

        });
      },

      error: () => {
        alert('No se pudo exportar la información.');
      }
    });
  }
}
