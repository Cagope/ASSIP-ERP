import { Injectable } from '@angular/core';
import { TipoComprobante } from './tipos-comprobantes.api';

@Injectable({ providedIn: 'root' })
export class TiposComprobantesPrintService {

  imprimir(tipos: TipoComprobante[]): void {

    if (!tipos || tipos.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    const win = window.open('', '_blank', 'width=1200,height=800');
    if (!win) return;

    win.document.open();
    win.document.write(this.buildHTML(tipos));
    win.document.close();

    win.onload = () => win.print();
  }

  // ============================================================
  // 🖨 Construcción del HTML del informe
  // ============================================================
  private buildHTML(tipos: TipoComprobante[]): string {

    const filasHTML = tipos
      .map(t => `
        <tr>
          <td>${t.idAgencia ?? ''}</td>
          <td>${t.tipoComprobante ?? ''}</td>
          <td>${t.nombreTipoComprobante ?? ''}</td>
          <td class="right">${t.cscComprobante ?? ''}</td>
          <td>${t.comprobanteActivo ? 'SI' : 'NO'}</td>
        </tr>
      `).join('');

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>Tipos de Comprobantes</title>

        <style>

          @page { size: letter portrait; margin: 10mm 12mm; }

          body {
            font-family: Arial, sans-serif;
            font-size: 11px;
            margin: 0;
            color: #222;
          }

          /* Encabezado */
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
            line-height: 1.2;
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
          <div class="titulo">Tipos de Comprobantes – Listado General</div>
        </div>

        <h2>Tipos de Comprobantes</h2>

        <table>
          <thead>
            <tr>
              <th>agencia</th>
              <th>tipo</th>
              <th>nombre</th>
              <th style="text-align:right;">consecutivo</th>
              <th>activo</th>
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
