import { Injectable } from '@angular/core';
import { ConvenioRecaudo } from './convenios-recaudo.api';

@Injectable({ providedIn: 'root' })
export class ConveniosRecaudoPrintService {

  imprimir(convenios: ConvenioRecaudo[]): void {

    const win = window.open('', '_blank');

    if (!win) {
      return;
    }

    win.document.write(`
      <html>
      <head>
        <title>Listado de Convenios de Recaudo</title>
        <style>
          body {
            font-family: Arial, sans-serif;
            padding: 20px;
          }

          h3 {
            margin-bottom: 14px;
          }

          table {
            border-collapse: collapse;
            width: 100%;
            font-size: 12px;
          }

          th,
          td {
            border: 1px solid #ccc;
            padding: 6px;
            text-align: left;
          }

          th {
            background: #f5f5f5;
          }

          .right {
            text-align: right;
          }
        </style>
      </head>
      <body>

        <h3>Listado de Convenios de Recaudo</h3>

        <table>
          <thead>
            <tr>
              <th>Agencia</th>
              <th>Código</th>
              <th>Convenio</th>
              <th>Documento</th>
              <th>Titular</th>
              <th>Cuenta</th>
              <th>Forma</th>
              <th>Saldo</th>
              <th>Estado</th>
            </tr>
          </thead>

          <tbody>
            ${convenios.map(item => `
              <tr>
                <td>${this.safe(item.codigoAgencia)} - ${this.safe(item.nombreAgencia)}</td>
                <td>${this.safe(item.codigoConvenio)}</td>
                <td>${this.safe(item.nombreConvenio)}</td>
                <td>${this.safe(item.documento)}</td>
                <td>${this.safe(item.nombreTitular)}</td>
                <td>${this.safe(item.codigoCuenta)}</td>
                <td>${this.safe(item.codigoForma)} - ${this.safe(item.nombreForma)}</td>
                <td class="right">${this.money(item.saldoActual)}</td>
                <td>${item.estado === 'A' ? 'Activo' : 'Inactivo'}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>

      </body>
      </html>
    `);

    win.document.close();
    win.print();
  }

  private safe(value: unknown): string {
    return value === null || value === undefined
      ? ''
      : String(value);
  }

  private money(value: unknown): string {
    const numero = Number(value || 0);

    return numero.toLocaleString(
      'en-US',
      {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }
    );
  }
}
