import { Injectable } from '@angular/core';
import { Zona } from './zonas.api';

@Injectable({ providedIn: 'root' })
export class ZonasPrintService {
  imprimir(zonas: Zona[]) {
    const win = window.open('', '_blank');
    if (!win) return;

    win.document.write(`
      <html>
      <head>
        <title>Listado de Zonas</title>
        <style>
          body { font-family: Arial, sans-serif; padding: 20px; }
          table { border-collapse: collapse; width: 100%; font-size: 12px; }
          th, td { border: 1px solid #ccc; padding: 6px; text-align: left; }
          th { background: #f5f5f5; }
        </style>
      </head>
      <body>
        <h3>Listado de Zonas</h3>
        <table>
          <thead>
            <tr><th>Código</th><th>Nombre</th><th>Comentario</th></tr>
          </thead>
          <tbody>
            ${zonas.map(z => `
              <tr>
                <td>${z.codigoZona ?? ''}</td>
                <td>${z.nombreZona ?? ''}</td>
                <td>${z.comentarioZona ?? ''}</td>
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
