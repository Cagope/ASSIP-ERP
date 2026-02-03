import { Injectable } from '@angular/core';

export interface KardexActivoPrintMeta {
  agencia?: string;
  activo?: string;
  fechaIni?: string | null;
  fechaFin?: string | null;
}

@Injectable({ providedIn: 'root' })
export class KardexActivoPrintService {

  imprimir(rows: any[], meta?: KardexActivoPrintMeta): void {

    if (!rows || rows.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    const win = window.open('', '_blank', 'width=1400,height=900');
    if (!win) return;

    win.document.open();
    win.document.write(this.buildHTML(rows, meta));
    win.document.close();

    win.onload = () => win.print();
  }

  // ============================================================
  // 🖨 Carta horizontal
  // ============================================================
  private buildHTML(rows: any[], meta?: KardexActivoPrintMeta): string {

    const fecha = new Date().toLocaleString('es-CO');

    const filas = rows.map(r => `
      <tr>
        <td class="nowrap">${r?.fecha ?? ''}</td>
        <td class="nowrap">${r?.hora ?? ''}</td>

        <td>${(r?.codigo_movimiento ?? '')} - ${(r?.nombre_movimiento ?? '')}</td>

        <td class="nowrap">${r?.tipo_comprobante ?? ''}</td>
        <td class="nowrap">${r?.numero_comprobante ?? ''}</td>

        <td class="right nowrap">${this.fmt(r?.valor_debito)}</td>
        <td class="right nowrap">${this.fmt(r?.valor_credito)}</td>
      </tr>
    `).join('');

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>Kardex Activo</title>
        <style>

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
            margin-bottom: 10px;
            gap: 12px;
          }

          .enc-left {
            display: flex;
            align-items: center;
            gap: 12px;
          }

          .enc img { height: 42px; }

          .titulo {
            font-size: 16px;
            font-weight: bold;
            margin: 0;
          }

          .meta {
            text-align: right;
            font-size: 10px;
            line-height: 1.3;
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

        <div class="enc">
          <div class="enc-left">
            <img src="${window.location.origin}/assets/LOGO_EMPRESA.png">
            <div class="titulo">Activos Fijos – Kardex por Activo</div>
          </div>

          <div class="meta">
            <div><strong>Agencia:</strong> ${meta?.agencia ?? 'TODAS'}</div>
            <div><strong>Activo:</strong> ${meta?.activo ?? ''}</div>
            <div><strong>Rango:</strong> ${meta?.fechaIni ?? ''} → ${meta?.fechaFin ?? ''}</div>
            <div><strong>Fecha:</strong> ${fecha}</div>
            <div><strong>Total:</strong> ${rows.length}</div>
          </div>
        </div>

        <table>
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Hora</th>
              <th>Movimiento</th>
              <th>Tipo Comp</th>
              <th>Número</th>
              <th style="text-align:right;">Débito</th>
              <th style="text-align:right;">Crédito</th>
            </tr>
          </thead>

          <tbody>
            ${filas}
          </tbody>
        </table>

      </body>
      </html>
    `;
  }

  private fmt(value: any): string {
    const n = Number(value ?? 0);
    return n.toLocaleString('es-CO', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
  }
}
