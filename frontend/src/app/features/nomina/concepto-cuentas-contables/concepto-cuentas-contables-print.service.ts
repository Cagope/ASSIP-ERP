import { Injectable } from '@angular/core';
import { ConceptoCuentaContableListDTO } from './concepto-cuentas-contables.api';

@Injectable({ providedIn: 'root' })
export class ConceptoCuentasContablesPrintService {

  imprimir(items: ConceptoCuentaContableListDTO[]): void {

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

  // =========================================================
  // HTML
  // =========================================================
  private buildHTML(items: ConceptoCuentaContableListDTO[]): string {

    const filasHTML = items.map(x => `
      <tr>
        <td><strong>${(x.codigoConcepto ?? '').toUpperCase()}</strong></td>
        <td>${(x.nombreAgencia ?? '').toUpperCase()}</td>
        <td>${(x.cuentaDebito ?? '').toUpperCase()}</td>
        <td>${(x.cuentaCredito ?? '').toUpperCase()}</td>
        <td class="center">${x.activo ? 'SI' : 'NO'}</td>
      </tr>
    `).join('');

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>Concepto · Cuentas Contables</title>

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

          .center {
            text-align: center;
          }
        </style>

      </head>
      <body>

        <div class="enc">
          <img src="${window.location.origin}/assets/LOGO_EMPRESA.png">
          <div class="titulo">Nómina – Concepto · Cuentas Contables</div>
        </div>

        <h2>Listado de Mapeo de Conceptos Contables</h2>

        <table>
          <thead>
            <tr>
              <th style="width:140px;">concepto</th>
              <th style="width:180px;">agencia</th>
              <th>cuenta débito</th>
              <th>cuenta crédito</th>
              <th style="width:80px;text-align:center;">activo</th>
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
