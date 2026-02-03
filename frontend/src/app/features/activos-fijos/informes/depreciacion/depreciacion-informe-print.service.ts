import { Injectable } from '@angular/core';

export interface DepreciacionInformePrintMeta {
  agencia?: string;
  fechaIni?: string | null;
  fechaFin?: string | null;
}

@Injectable({ providedIn: 'root' })
export class DepreciacionInformePrintService {

  imprimir(rows: any[], meta?: DepreciacionInformePrintMeta): void {

    if (!rows || rows.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    const win = window.open('', '_blank', 'width=1400,height=900');
    if (!win) return;

    win.document.open();
    win.document.write(this.buildHTML(rows, meta));
    win.document.close();

    // ✅ disparar impresión
    win.onload = () => win.print();
  }

  // ============================================================
  // 🖨 Construcción HTML
  // ✅ Carta Horizontal (LANDSCAPE)
  // ============================================================
  private buildHTML(rows: any[], meta?: DepreciacionInformePrintMeta): string {

    const fechaGen = new Date().toLocaleString('es-CO');

    const agencia = meta?.agencia || 'TODAS';
    const fechaIni = meta?.fechaIni || '';
    const fechaFin = meta?.fechaFin || '';

    const filasHTML = rows.map(r => `
      <tr>
        <td class="nowrap">${this.val(r?.fecha)}</td>
        <td>${this.val(r?.nombre_activo)}</td>
        <td class="nowrap"><strong>${this.val(r?.placa_activo)}</strong></td>

        <td class="right nowrap">${this.num(r?.valor_adquisicion)}</td>
        <td class="right nowrap">${this.num(r?.valor_depreciacion_mes)}</td>
        <td class="right nowrap">${this.num(r?.valor_depreciacion_acumulada)}</td>
        <td class="right nowrap">${this.num(r?.valor_neto)}</td>
      </tr>
    `).join('');

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>Informe Depreciación</title>

        <style>

          /* ✅ CARTA HORIZONTAL */
          @page { size: letter landscape; margin: 10mm 10mm; }

          body {
            font-family: Arial, sans-serif;
            font-size: 10px;
            margin: 0;
            color: #222;
          }

          .enc {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            margin-bottom: 10px;
          }

          .enc-left {
            display: flex;
            align-items: center;
            gap: 12px;
          }

          .enc img {
            height: 42px;
          }

          .titulo {
            font-size: 16px;
            font-weight: bold;
            margin: 0;
          }

          .subtitulo {
            font-size: 12px;
            margin-top: 2px;
            color: #444;
          }

          .meta {
            text-align: right;
            font-size: 10px;
            line-height: 1.3;
          }

          h2 {
            font-size: 13px;
            margin: 10px 0 6px;
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
            font-size: 10px;
            text-transform: lowercase;
            white-space: nowrap;
          }

          td {
            padding: 4px 5px;
            border-bottom: 0.5px solid #ddd;
            font-size: 10px;
            vertical-align: top;
          }

          .right { text-align: right; }
          .nowrap { white-space: nowrap; }

        </style>

      </head>
      <body>

        <!-- ENCABEZADO -->
        <div class="enc">

          <div class="enc-left">
            <img src="${window.location.origin}/assets/LOGO_EMPRESA.png">
            <div>
              <div class="titulo">Activos Fijos – Informe Depreciación</div>
              <div class="subtitulo">Entre fechas</div>
            </div>
          </div>

          <div class="meta">
            <div><strong>Agencia:</strong> ${this.val(agencia)}</div>
            <div><strong>Rango:</strong> ${this.val(fechaIni)} → ${this.val(fechaFin)}</div>
            <div><strong>Generado:</strong> ${this.val(fechaGen)}</div>
            <div><strong>Total:</strong> ${rows.length}</div>
          </div>

        </div>

        <h2>Detalle de Depreciación</h2>

        <table>
          <thead>
            <tr>
              <th>fecha</th>
              <th>activo</th>
              <th>placa</th>
              <th style="text-align:right;">valor adquisición</th>
              <th style="text-align:right;">dep. mensual</th>
              <th style="text-align:right;">dep. acumulada</th>
              <th style="text-align:right;">neto</th>
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

  // ============================================================
  // ✅ Helpers
  // ============================================================
  private val(v: any): string {
    return String(v ?? '').replaceAll('<', '&lt;').replaceAll('>', '&gt;');
  }

  private num(v: any): string {
    const n = Number(v ?? 0);
    return n.toLocaleString('es-CO', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    });
  }
}
