import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class RangosPrintService {

  /**
   * Imprime el informe en una nueva ventana
   */
  imprimir(titulo: string, datos: any[]) {

    if (!datos || datos.length === 0) {
      console.warn('No hay datos para imprimir');
      return;
    }

    // Columnas a imprimir
    const columnas = [
      'codigoCuenta',
      'codigoAgencia',
      'nombreAgencia',
      'codigoForma',
      'nombreForma',
      'documento',
      'nombreCompleto',
      'saldo',
      'edadAnios',
      'antiguedadAnios'
    ];

    // Generar contenido HTML
    let tablaHtml = `
      <table border="1" cellspacing="0" cellpadding="4" style="border-collapse: collapse; width: 100%; font-size: 11px;">
        <thead>
          <tr style="background: #f0f0f0; font-weight: bold;">
            ${columnas.map(c => `<th>${c}</th>`).join('')}
          </tr>
        </thead>
        <tbody>
          ${datos.map(item => `
            <tr>
              ${columnas.map(c => `<td>${item[c] ?? ''}</td>`).join('')}
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;

    // Abrir la ventana y escribir el contenido
    const ventana = window.open('', '_blank', 'width=900,height=700');
    if (!ventana) return;

    ventana.document.write(`
      <html>
        <head>
          <title>${titulo}</title>
          <style>
            body { font-family: Arial, sans-serif; margin: 20px; }
            h1 { font-size: 18px; margin-bottom: 10px; }
            table { width: 100%; border-collapse: collapse; }
            th, td { border: 1px solid #ccc; padding: 6px; text-align: left; }
            th { background: #f5f5f5; }
          </style>
        </head>
        <body>
          <h1>${titulo}</h1>
          ${tablaHtml}
        </body>
      </html>
    `);

    ventana.document.close();
    ventana.focus();

    // Auto imprimir
    setTimeout(() => {
      ventana.print();
    }, 300);
  }
}
