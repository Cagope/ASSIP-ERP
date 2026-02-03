import { Injectable } from '@angular/core';

export interface ResumenMovimientosPrintMeta {
  nombreAgencia?: string;
  nombreMovimiento?: string;
}

@Injectable({ providedIn: 'root' })
export class ResumenMovimientosPrintService {

  imprimir(rows: any[], meta?: ResumenMovimientosPrintMeta): void {

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
  // 🖨 Construcción del HTML del informe
  // ✅ Carta Horizontal (LANDSCAPE)
  // ============================================================
  private buildHTML(rows: any[], meta?: ResumenMovimientosPrintMeta): string {

    const fecha = new Date().toLocaleString('es-CO');

    const filasHTML = rows.map(r => `
      <tr>
        <td class="nowrap">${this.val(r?.periodo_mes)}</td>
        <td>${this.val(r?.nombre_agencia)}</td>

        <td class="nowrap">${this.val(r?.codigo_movimiento)}</td>
        <td>${this.val(r?.nombre_movimiento)}</td>

        <td class="right nowrap">${this.num(r?.cantidad_movimientos)}</td>
        <td class="right nowrap">${this.num(r?.cantidad_activos)}</td>

        <td class="right nowrap">${this.money(r?.total_debito)}</td>
        <td class="right nowrap">${this.money(r?.total_credito)}</td>
        <td class="right nowrap">${this.money(r?.neto)}</td>
      </tr>
    `).join('');

    const nombreAgencia = meta?.nombreAgencia || 'TODAS';
    const nombreMovimiento = meta?.nombreMovimiento || 'TODOS';

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>Resumen Movimientos</title>

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
            margin-bottom: 10px;
            gap: 12px;
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

          .filtros {
            font-size: 10px;
            margin-top: 4px;
            color: #333;
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

        <div class="enc">

          <div class="enc-left">
            <img src="${window.location.origin}/assets/LOGO_EMPRESA.png">
            <div>
              <div class="titulo">Activos Fijos – Resumen de Movimientos</div>
              <div class="filtros">
                <strong>Agencia:</strong> ${this.val(nombreAgencia)}
                &nbsp; | &nbsp;
                <strong>Movimiento:</strong> ${this.val(nombreMovimiento)}
              </div>
            </div>
          </div>

          <div class="meta">
            <div><strong>Fecha:</strong> ${fecha}</div>
            <div><strong>Total filas:</strong> ${rows.length}</div>
          </div>

        </div>

        <h2>Resumen mensual</h2>

        <table>
          <thead>
            <tr>
              <th>periodo</th>
              <th>agencia</th>
              <th>cod mov</th>
              <th>movimiento</th>
              <th style="text-align:right;">cant. mov</th>
              <th style="text-align:right;">cant. activos</th>
              <th style="text-align:right;">debito</th>
              <th style="text-align:right;">credito</th>
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
    return String(v ?? '');
  }

  private num(v: any): string {
    const n = Number(v ?? 0);
    return n.toLocaleString('es-CO', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
  }

  private money(v: any): string {
    const n = Number(v ?? 0);
    return n.toLocaleString('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}
