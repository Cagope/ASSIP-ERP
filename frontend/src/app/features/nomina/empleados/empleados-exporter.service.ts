import { Injectable, inject } from '@angular/core';
import * as XLSX from 'xlsx';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { EmpleadoListDTO } from './empleados.api';
import { PersonasApi, PersonaBusquedaDTO } from '../../../shared/personas/personas.api';

@Injectable({ providedIn: 'root' })
export class EmpleadosExporterService {

  private readonly personasApi = inject(PersonasApi);

  exportar(items: EmpleadoListDTO[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para exportar.');
      return;
    }

    // ✅ traer personas por idDatosPersonal
    const ids = Array.from(new Set(
      items.map(x => x.idDatosPersonal).filter(x => !!x)
    ));

    const requests = ids.map(id =>
      this.personasApi.obtenerPorId(id).pipe(
        map(p => ({ id, persona: p })),
        catchError(() => of({ id, persona: null }))
      )
    );

    forkJoin(requests).subscribe({
      next: (rows) => {

        const mapPersonas = new Map<number, PersonaBusquedaDTO>();
        for (const r of rows) {
          if (r.persona) mapPersonas.set(r.id, r.persona);
        }

        const data = items.map(x => {
          const p = mapPersonas.get(x.idDatosPersonal);
          return {
            'ID Empleado': x.idEmpleado,
            'ID Persona': x.idDatosPersonal,
            'Documento': p?.documento ?? '',
            'Nombre Completo': p?.nombreCompleto ?? '',
            'ID Agencia': x.idAgencia,
            'Activo': x.activo ? 'SI' : 'NO',
          };
        });

        const ws = XLSX.utils.json_to_sheet(data);
        const wb = XLSX.utils.book_new();

        XLSX.utils.book_append_sheet(wb, ws, 'Empleados');
        XLSX.writeFile(wb, 'empleados.xlsx');
      },
      error: () => {
        alert('No se pudo exportar la información.');
      }
    });
  }
}
