import { Injectable } from '@angular/core';
import { EmpleadoContratoListDTO } from './empleado-contratos.api';
import { EmpleadoListDTO } from '../empleados/empleados.api';
import { PersonaBusquedaDTO } from '../../../shared/personas/personas.api';

@Injectable({ providedIn: 'root' })
export class EmpleadoContratosPrintService {

  imprimir(
    items: EmpleadoContratoListDTO[],
    empleadosMap: Map<number, EmpleadoListDTO>,
    personasMap: Map<number, PersonaBusquedaDTO>
  ): void {

    if (!items || items.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    const win = window.open('', '_blank', 'width=1200,height=800');
    if (!win) return;

    win.document.open();
    win.document.write(this.buildHTML(items, empleadosMap, personasMap));
    win.document.close();

    win.onload = () => win.print();
  }

  private buildHTML(
    items: EmpleadoContratoListDTO[],
    empleadosMap: Map<number, EmpleadoListDTO>,
    personasMap: Map<number, PersonaBusquedaDTO>
  ): string {

    const filasHTML = items.map(x => {
      const emp = empleadosMap.get(x.idEmpleado);
      const per = emp?.idDatosPersonal ? personasMap.get(emp.idDatosPersonal) : null;

      return `
        <tr>
          <td>${x.idContrato ?? ''}</td>
          <td>${x.idEmpleado ?? ''}</td>
          <td>${per?.documento ?? ''}</td>
          <td>${(per?.nombreCompleto ?? '').toUpperCase()}</td>
          <td class="center">${x.fechaInicio ?? ''}</td>
          <td class="center">${x.fechaFin ?? ''}</td>
          <td class="center">${(x.periodoPago ?? '').toUpperCase()}</td>
          <td class="right">${x.salarioBase ?? 0}</td>
          <td class="center">${x.activo ? 'SI' : 'NO'}</td>
        </tr>
      `;
    }).join('');

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>Contratos</title>

        <style>
          @page { size: letter portrait; margin: 10mm 12mm; }

          body { font-family: Arial, sans-serif; font-size: 11px; margin: 0; color: #222; }
          .enc { display:flex; align-items:center; margin-bottom:10px; }
          .enc img { height:45px; margin-right:12px; }
          .titulo { font-size:18px; font-weight:bold; margin:0; }
          h2 { font-size:14px; margin:10px 0 5px; padding-bottom:3px; border-bottom:1px solid #777; }

          table { width:100%; border-collapse:collapse; margin-top:6px; }
          th { background:#f0f0f0; border-bottom:1px solid #555; padding:5px; text-align:left; font-size:11px; text-transform:lowercase; }
          td { padding:4px 5px; border-bottom:0.5px solid #ddd; font-size:11px; text-transform:uppercase; }

          .center { text-align:center; }
          .right { text-align:right; }
        </style>
      </head>

      <body>
        <div class="enc">
          <img src="${window.location.origin}/assets/LOGO_EMPRESA.png">
          <div class="titulo">Nómina – Contratos de Empleado</div>
        </div>

        <h2>Listado de Contratos</h2>

        <table>
          <thead>
            <tr>
              <th style="width:70px;">id</th>
              <th style="width:80px;">empleado</th>
              <th style="width:110px;">documento</th>
              <th>nombre</th>
              <th style="width:85px;text-align:center;">inicio</th>
              <th style="width:85px;text-align:center;">fin</th>
              <th style="width:85px;text-align:center;">periodo</th>
              <th style="width:90px;text-align:right;">salario</th>
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
