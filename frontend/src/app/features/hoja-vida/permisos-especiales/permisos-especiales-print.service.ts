import { Injectable } from '@angular/core';
import { PermisoEspecial } from './permisos-especiales.api';

/**
 * 🖨️ Servicio de impresión — Listado de Permisos Especiales
 * ------------------------------------------------------------
 * Genera un informe en formato Legal horizontal (Landscape)
 * con diseño uniforme del ERP ASSIP.
 */
@Injectable({ providedIn: 'root' })
export class PermisosEspecialesPrintService {
  imprimir(
    registros: (PermisoEspecial & {
      documento?: string;
      nombrePersona?: string;
    })[]
  ): void {
    if (!registros || registros.length === 0) {
      alert('⚠️ No hay registros de Permisos Especiales para imprimir.');
      return;
    }

    const filas = registros
      .map(
        (r, i) => `
        <tr>
          <td style="text-align:center;">${i + 1}</td>
          <td>${r.documento ?? ''}</td>
          <td>${r.nombrePersona ?? ''}</td>
          <td>${r.recibeLlamadas ? 'Sí' : 'No'}</td>
          <td>${r.fechaLlamadas ?? ''}</td>
          <td>${r.recibeMsm ? 'Sí' : 'No'}</td>
          <td>${r.fechaSms ?? ''}</td>
          <td>${r.recibeEmails ? 'Sí' : 'No'}</td>
          <td>${r.fechaEmails ?? ''}</td>
          <td>${r.recibeCartas ? 'Sí' : 'No'}</td>
          <td>${r.fechaCartas ?? ''}</td>
          <td>${r.recibeRedesSociales ? 'Sí' : 'No'}</td>
          <td>${r.fechaRedesSociales ?? ''}</td>
        </tr>`
      )
      .join('');

    const total = registros.length;

    const html = `
      <html>
        <head>
          <meta charset="utf-8">
          <title>Listado de Permisos Especiales</title>
          <style>
            @page { size: Legal landscape; margin: 18mm; }
            body {
              font-family: Arial, sans-serif;
              color: #222;
              margin: 0;
              font-size: 12px;
            }
            header {
              text-align: center;
              margin-bottom: 10px;
            }
            img.logo {
              height: 60px;
              display: block;
              margin: 0 auto 5px auto;
            }
            h1 {
              font-size: 18px;
              margin: 4px 0;
            }
            table {
              width: 100%;
              border-collapse: collapse;
              border-spacing: 0;
            }
            th, td {
              padding: 5px 6px;
              border-bottom: 0.5px solid #ccc;
              vertical-align: middle;
            }
            th {
              background-color: #f3f3f3;
              text-align: left;
            }
            tbody tr:nth-child(even) {
              background-color: #fafafa;
            }
            tfoot td {
              text-align: right;
              font-weight: bold;
              padding-top: 8px;
            }
            footer {
              text-align: center;
              font-size: 10px;
              color: #555;
              margin-top: 10px;
              border-top: 1px solid #ccc;
              padding-top: 5px;
            }
          </style>
        </head>
        <body>
          <header>
            <img src="${window.location.origin}/assets/LOGO_EMPRESA.png" class="logo" alt="Logo Empresa">
            <h1>LISTADO DE PERMISOS ESPECIALES</h1>
            <p style="font-size:11px; margin:0;">ERP ASSIP Solidaria y Financiera — Módulo Hoja de Vida</p>
          </header>

          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Documento</th>
                <th>Nombre Persona</th>
                <th>Recibe Llamadas</th>
                <th>Fecha Llamadas</th>
                <th>Recibe SMS</th>
                <th>Fecha SMS</th>
                <th>Recibe Emails</th>
                <th>Fecha Emails</th>
                <th>Recibe Cartas</th>
                <th>Fecha Cartas</th>
                <th>Recibe Redes Sociales</th>
                <th>Fecha Redes Sociales</th>
              </tr>
            </thead>
            <tbody>${filas}</tbody>
            <tfoot>
              <tr>
                <td colspan="13">Total registros: ${total}</td>
              </tr>
            </tfoot>
          </table>

          <footer>
            Impreso el ${new Date().toLocaleString()}
          </footer>
        </body>
      </html>
    `;

    const ventana = window.open('', '_blank', 'width=1200,height=800');
    if (!ventana) {
      alert('⚠️ Bloqueador de ventanas emergentes activo.');
      return;
    }
    ventana.document.open();
    ventana.document.write(html);
    ventana.document.close();
    ventana.onload = () => ventana.print();
  }
}
