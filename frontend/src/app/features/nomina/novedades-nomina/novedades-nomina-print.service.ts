import { Injectable } from '@angular/core';
import { NovedadNominaListDTO } from './novedades-nomina.api';

@Injectable({ providedIn: 'root' })
export class NovedadesNominaPrintService {

  imprimir(items: NovedadNominaListDTO[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    const win = window.open('', '_blank', 'width=1200,height=800');
    if (!win) return;

    win.document.open();
    win.document.write(this.buildHTML(items));
    win.document.close();

    win.onload = () => win.print();
  }

  private buildHTML(items: NovedadNominaListDTO[]): string {

    const filasHTML = items.map(x => `
      <tr>
        <td>${x.idNovedad ?? ''}</td>
        <td>${x.documentoEmpleado ?? ''}</td>
        <td>${(x.nombreEmpleado ?? '').toUpperCase()}</td>
        <td>${(x.codigoConcepto ?? '').toUpperCase()}</td>
        <td class="center">${x.fechaInicial ?? ''}</td>
        <td class="center">${x.fechaFinal ?? ''}</td>
        <td class="center">${x.cantidad ?? 0}</td>
        <td class="right">${x.valor ?? 0}</td>
        <td class="center">${(x.estado ?? '').toUpperCase()}</td>
        <td>${(x.observacion ?? '').toUpperCase()}</td>
      </tr>
    `).join('');

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>Novedades de Nómina</title>

        <style>
          @page { size: letter portrait; margin: 10mm 12mm; }

          body { font-family: Arial, sans-serif; font-size: 11px; margin: 0; color: #222; }
          .enc { display:flex; align-items:center; margin-bottom:10px; }
          .enc img { height:45px; margin-right:12px; }
          .titulo { font-size:18px; font-weight:bold; margin:0; }
          h2 { font-size:14px; margin:10px 0 5px; padding-bottom:3px; border-bottom:1px solid #777; }

          table { width:100%; border-collapse:collapse; margin-top:6px; }
          th { background:#f0f0f0; border-bottom:1px solid #555; padding:5px; text-align:left; font-size:11px; text-transform:lowercase; }
          td { padding:4px 5px; border-bottom:0.5px solid #ddd; font-size:10px; text-transform:uppercase; vertical-align:top; }

          .center { text-align:center; }
          .right { text-align:right; }
        </style>
      </head>

      <body>
        <div class="enc">
          <img src="${window.location.origin}/assets/LOGO_EMPRESA.png">
          <div class="titulo">Nómina – Novedades de Nómina</div>
        </div>

        <h2>Listado de Novedades</h2>

        <table>
          <thead>
            <tr>
              <th style="width:55px;">id</th>
              <th style="width:95px;">documento</th>
              <th style="width:190px;">empleado</th>
              <th style="width:95px;">concepto</th>
              <th style="width:85px;text-align:center;">inicial</th>
              <th style="width:85px;text-align:center;">final</th>
              <th style="width:70px;text-align:center;">cantidad</th>
              <th style="width:95px;text-align:right;">valor</th>
              <th style="width:80px;text-align:center;">estado</th>
              <th>observación</th>
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
