import { Injectable } from '@angular/core';
import { Agencia } from './agencia.api';

@Injectable({ providedIn: 'root' })
export class AgenciasPrintService {
  /** Genera e imprime el listado de agencias */
  imprimirListado(agencias: (Agencia & { nombreDepartamento?: string; nombreCiudad?: string })[]): void {
    if (!agencias || agencias.length === 0) {
      alert('⚠️ No hay agencias para imprimir.');
      return;
    }

    // 🔹 Construir cuerpo de la tabla
    const filas = agencias.map((a, i) => `
      <tr>
        <td>${String(a.codigoAgencia ?? '').padStart(2, '0')}</td>
        <td>${a.nombreAgencia ?? ''}</td>
        <td>${a.siglaAgencia ?? ''}</td>
        <td>${a.direccionAgencia ?? ''}</td>
        <td>${a.nombreDepartamento ?? ''}</td>
        <td>${a.nombreCiudad ?? ''}</td>
        <td>${a.correoAgencia ?? ''}</td>
        <td>${a.celularAgencia ?? ''}</td>
        <td>${a.telefonoAgencia ?? ''}</td>
      </tr>
    `).join('');

    // 🔹 Total al final
    const total = agencias.length;

    // 🔹 HTML completo de impresión
    const html = `
      <html>
        <head>
          <meta charset="utf-8">
          <title>Listado de Agencias</title>
          <style>
            @page { size: Letter; margin: 20mm; }
            body { font-family: Arial, sans-serif; margin: 15px 25px; color: #222; }
            h1 { text-align: center; margin: 10px 0 15px 0; font-size: 18px; }
            table { width: 100%; border-collapse: collapse; font-size: 12px; }
            th, td { border: 1px solid #666; padding: 6px 4px; }
            th { background: #f3f3f3; }
            tfoot td { font-weight: bold; }
            img.logo { height: 55px; display: block; margin: 0 auto 5px auto; }
            footer {
              text-align: center;
              font-size: 10px;
              color: #555;
              margin-top: 15px;
              border-top: 1px solid #ccc;
              padding-top: 5px;
            }
          </style>
        </head>
        <body>
          <img src="${window.location.origin}/assets/LOGO_EMPRESA.png" class="logo" alt="Logo Empresa">
          <h1>Listado de Agencias</h1>

          <table>
            <thead>
              <tr>
                <th>Código</th>
                <th>Nombre</th>
                <th>Sigla</th>
                <th>Dirección</th>
                <th>Departamento</th>
                <th>Ciudad</th>
                <th>Correo</th>
                <th>Celular</th>
                <th>Teléfono</th>
              </tr>
            </thead>
            <tbody>${filas}</tbody>
            <tfoot>
              <tr>
                <td colspan="9" style="text-align:right; font-weight:bold;">
                  Total agencias: ${total}
                </td>
              </tr>
            </tfoot>
          </table>

          <footer>
            ERP ASSIP Solidaria y Financiera — Módulo General / Agencias<br>
            Impreso el ${new Date().toLocaleString()}
          </footer>
        </body>
      </html>
    `;

    // 🔹 Abrir en nueva pestaña para imprimir
    const ventana = window.open('', '_blank', 'width=900,height=700');
    if (!ventana) {
      alert('⚠️ Bloqueador de ventanas emergentes activo.');
      return;
    }
    ventana.document.open();
    ventana.document.write(html);
    ventana.document.close();

    // Esperar a que el contenido cargue antes de imprimir
    ventana.onload = () => ventana.print();
  }
}
