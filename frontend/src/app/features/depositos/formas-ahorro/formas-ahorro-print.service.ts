import { Injectable } from '@angular/core';
import { FormaAhorro } from './formas-ahorro.api';

@Injectable({ providedIn: 'root' })
export class FormaAhorroPrintService {

  imprimir(formas: FormaAhorro[]): void {

    if (!formas || formas.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    const win = window.open('', '_blank', 'width=1200,height=800');
    if (!win) return;

    win.document.open();
    win.document.write(this.buildHTML(formas));
    win.document.close();

    win.onload = () => win.print();
  }

  // ============================================================
  // 🖨 Construcción del HTML del informe
  // ============================================================
  private buildHTML(formas: FormaAhorro[]): string {

    const filasHTML = formas
      .map(f => `
        <tr>
          <td>${f.codigoForma ?? ''}</td>
          <td>${f.nombreForma ?? ''}</td>
          <td class="right">${f.consecutivoForma ?? ''}</td>
          <td>${f.tipoCaptacion ?? ''}</td>
          <td class="right">${f.tiempoLiquidacion ?? ''}</td>
          <td class="right">${f.valorMinimo?.toLocaleString('es-CO', {minimumFractionDigits: 2}) ?? ''}</td>
          <td class="right">${f.tasaInteresForma ?? ''}</td>
          <td>${f.autorizadoForma ? 'SI' : 'NO'}</td>
        </tr>
      `).join('');

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>Formas de Ahorro</title>

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
          <div class="titulo">Formas de Ahorro – Listado General</div>
        </div>

        <h2>Listado de Formas</h2>

        <table>
          <thead>
            <tr>
              <th>código</th>
              <th>nombre</th>
              <th style="text-align:right;">consecutivo</th>
              <th>tipo</th>
              <th style="text-align:right;">liquidación</th>
              <th style="text-align:right;">mínimo</th>
              <th style="text-align:right;">tasa</th>
              <th>autorizado</th>
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
