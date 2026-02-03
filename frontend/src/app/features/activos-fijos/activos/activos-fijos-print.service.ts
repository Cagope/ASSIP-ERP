import { Injectable } from '@angular/core';
import { ActivoFijoListDTO } from './activos-fijos.api';

@Injectable({ providedIn: 'root' })
export class ActivosFijosPrintService {

  imprimir(activos: ActivoFijoListDTO[]): void {

    if (!activos || activos.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    const win = window.open('', '_blank', 'width=1200,height=800');
    if (!win) return;

    win.document.open();
    win.document.write(this.buildHTML(activos));
    win.document.close();

    win.onload = () => win.print();
  }

  // ============================================================
  // 🖨 Construcción del HTML del informe
  // ============================================================
  private buildHTML(activos: ActivoFijoListDTO[]): string {

    const filasHTML = activos.map(a => `
      <tr>
        <td>${a.placaActivo ?? ''}</td>
        <td>${a.nombreActivo ?? ''}</td>
        <td>${a.nombreAgencia ?? ''}</td>
        <td>${a.fechaIngreso ?? ''}</td>
        <td class="right">${a.mesesDepreciacion ?? 0}</td>
        <td class="right">${a.valorAdquisicion?.toLocaleString('es-CO', { minimumFractionDigits: 2 }) ?? ''}</td>
        <td class="right">${a.valorMensual?.toLocaleString('es-CO', { minimumFractionDigits: 2 }) ?? ''}</td>
        <td>${a.nombreEstadoActivo ?? ''}</td>
      </tr>
    `).join('');

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>Activos Fijos</title>

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
            text-transform: uppercase;
            font-size: 11px;
          }

          .right {
            text-align: right;
          }

        </style>

      </head>
      <body>

        <div class="enc">
          <img src="${window.location.origin}/assets/LOGO_EMPRESA.png">
          <div class="titulo">Activos Fijos – Listado General</div>
        </div>

        <h2>Listado de Activos</h2>

        <table>
          <thead>
            <tr>
              <th>placa</th>
              <th>activo</th>
              <th>agencia</th>
              <th>ingreso</th>
              <th style="text-align:right;">meses</th>
              <th style="text-align:right;">valor</th>
              <th style="text-align:right;">depreciación</th>
              <th>estado</th>
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
