import { Injectable } from '@angular/core';
import { SubZona } from './sub-zonas.api';

@Injectable({ providedIn: 'root' })
export class SubZonasPrintService {
  imprimir(subzonas: SubZona[]) {
    const win = window.open('', '_blank');
    if (!win) return;

    win.document.write(`
      <html>
      <head>
        <title>Listado de Subzonas</title>
        <style>
          body { font-family: Arial, sans-serif; padding: 20px; }
          table { border-collapse: collapse; width: 100%; font-size: 12px; }
          th, td { border: 1px solid #ccc; padding: 6px; text-align: left; }
          th { background: #f5f5f5; }
        </style>
      </head>
      <body>
        <h3>Listado de Subzonas</h3>
        <table>
          <thead>
            <tr>
              <th>Código</th>
              <th>Nombre</th>
              <th>Zona</th>
              <th>Comentario</th>
            </tr>
          </thead>
          <tbody>
            ${subzonas.map(s => `
              <tr>
                <td>${s.codigoSubZona ?? ''}</td>
                <td>${s.nombreSubZona ?? ''}</td>
                <td>${s.zona?.nombreZona ?? ''}</td>
                <td>${s.comentarioSubZona ?? ''}</td>
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
