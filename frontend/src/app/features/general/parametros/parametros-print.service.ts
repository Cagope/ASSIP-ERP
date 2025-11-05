import { Injectable } from '@angular/core';
import { Parametro } from './parametros.api';

@Injectable({ providedIn: 'root' })
export class ParametrosPrintService {
  imprimir(parametros: Parametro[]) {
    const win = window.open('', '_blank');
    if (!win) return;

    win.document.write(`
      <html>
      <head>
        <title>Listado de Parámetros</title>
        <style>
          body { font-family: Arial, sans-serif; padding: 20px; }
          table { border-collapse: collapse; width: 100%; font-size: 12px; }
          th, td { border: 1px solid #ccc; padding: 6px; text-align: left; }
          th { background: #f5f5f5; }
        </style>
      </head>
      <body>
        <h3>Listado de Parámetros</h3>
        <table>
          <thead>
            <tr>
              <th>Agencia</th>
              <th>Código</th>
              <th>Nombre</th>
              <th>Valor</th>
              <th>Tipo de Valor</th>
            </tr>
          </thead>
          <tbody>
            ${parametros.map(p => `
              <tr>
                <td>${p.idAgencia ?? ''}</td>
                <td>${p.codigoParametro ?? ''}</td>
                <td>${p.nombreParametro ?? ''}</td>
                <td>${p.valorParametro ?? ''}</td>
                <td>${p.tipoValor ? 'Porcentaje' : 'Valor'}</td>
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
}
