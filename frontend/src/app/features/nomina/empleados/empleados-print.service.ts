import { Injectable, inject } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { EmpleadoListDTO } from './empleados.api';
import { PersonasApi, PersonaBusquedaDTO } from '../../../shared/personas/personas.api';

@Injectable({ providedIn: 'root' })
export class EmpleadosPrintService {

  private readonly personasApi = inject(PersonasApi);

  imprimir(items: EmpleadoListDTO[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

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

        const win = window.open('', '_blank', 'width=1200,height=800');
        if (!win) return;

        win.document.open();
        win.document.write(this.buildHTML(items, mapPersonas));
        win.document.close();

        win.onload = () => win.print();
      },
      error: () => alert('No se pudo imprimir la información.')
    });
  }

  private buildHTML(items: EmpleadoListDTO[], mapPersonas: Map<number, PersonaBusquedaDTO>): string {

    const filasHTML = items.map(x => {
      const p = mapPersonas.get(x.idDatosPersonal);

      return `
        <tr>
          <td>${x.idEmpleado ?? ''}</td>
          <td>${x.idDatosPersonal ?? ''}</td>
          <td>${p?.documento ?? ''}</td>
          <td>${(p?.nombreCompleto ?? '').toUpperCase()}</td>
          <td class="center">${x.idAgencia ?? ''}</td>
          <td class="center">${x.activo ? 'SI' : 'NO'}</td>
        </tr>
      `;
    }).join('');

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>Empleados</title>

        <style>
          @page { size: letter portrait; margin: 10mm 12mm; }

          body {
            font-family: Arial, sans-serif;
            font-size: 11px;
            margin: 0;
            color: #222;
          }

          .enc {
            display: flex;
            align-items: center;
            margin-bottom: 10px;
          }

          .enc img {
            height: 45px;
            margin-right: 12px;
          }

          .titulo {
            font-size: 18px;
            font-weight: bold;
            margin: 0;
          }

          h2 {
            font-size: 14px;
            margin: 10px 0 5px;
            padding-bottom: 3px;
            border-bottom: 1px solid #777;
          }

          table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 6px;
          }

          th {
            background: #f0f0f0;
            border-bottom: 1px solid #555;
            padding: 5px;
            text-align: left;
            font-size: 11px;
            text-transform: lowercase;
          }

          td {
            padding: 4px 5px;
            border-bottom: 0.5px solid #ddd;
            font-size: 11px;
            text-transform: uppercase;
          }

          .center { text-align: center; }
        </style>

      </head>
      <body>

        <div class="enc">
          <img src="${window.location.origin}/assets/LOGO_EMPRESA.png">
          <div class="titulo">Nómina – Empleados</div>
        </div>

        <h2>Listado de Empleados</h2>

        <table>
          <thead>
            <tr>
              <th style="width:70px;">id</th>
              <th style="width:85px;">id persona</th>
              <th style="width:110px;">documento</th>
              <th>nombre completo</th>
              <th style="width:70px;text-align:center;">agencia</th>
              <th style="width:70px;text-align:center;">activo</th>
            </tr>
          </thead>
          <tbody>
            ${filasHTML}
          </tbody>
        </table>

      </body>
      </html>
    `;
  }
}
