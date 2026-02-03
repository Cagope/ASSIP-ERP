import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class MaestroActivosPrintService {

  imprimir(rows: any[], nombreAgencia?: string): void {

    if (!rows || rows.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    const win = window.open('', '_blank', 'width=1400,height=900');
    if (!win) return;

    win.document.open();
    win.document.write(this.buildHTML(rows, nombreAgencia));
    win.document.close();

    // ✅ Imprimir inmediato (patrón Zonas)
    win.print();
  }

  // ============================================================
  // 🖨 Construcción del HTML del informe
  // ✅ Carta Horizontal (LANDSCAPE)
  // ============================================================
  private buildHTML(rows: any[], nombreAgencia?: string): string {

    const fecha = new Date().toLocaleString();

    const filasHTML = rows.map(r => `
      <tr>
        <td class="nowrap">${r.id_activo_fijo ?? ''}</td>
        <td class="nowrap"><strong>${r.placa_activo ?? ''}</strong></td>
        <td>${r.nombre_activo ?? ''}</td>
        <td>${r.nombre_agencia ?? ''}</td>
        <td>${r.nombre_estado ?? ''}</td>
        <td>${r.nombre_responsable ?? ''}</td>

        <td class="right nowrap">${this.fmt(r.valor_adquisicion)}</td>
        <td class="right nowrap">${this.fmt(r.valor_depreciacion_acumulada)}</td>
        <td class="right nowrap">${this.fmt(r.valor_neto)}</td>
      </tr>
    `).join('');

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>Maestro Activos</title>

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
          }

          td {
            padding: 4px 5px;
            border-bottom: 0.5px solid #ddd;
            font-size: 10px;
            vertical-align: top;
          }

          .right {
            text-align: right;
          }

          .nowrap {
            white-space: nowrap;
          }

        </style>

      </head>
      <body>

        <div class="enc">

          <div class="enc-left">
            <img src="${window.location.origin}/assets/LOGO_EMPRESA.png">
            <div class="titulo">Activos Fijos – Maestro de Activos</div>
          </div>

          <div class="meta">
            <div><strong>Agencia:</strong> ${nombreAgencia || 'TODAS'}</div>
            <div><strong>Fecha:</strong> ${fecha}</div>
            <div><strong>Total:</strong> ${rows.length}</div>
          </div>

        </div>

        <h2>Listado Maestro</h2>

        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>placa</th>
              <th>activo</th>
              <th>agencia</th>
              <th>estado</th>
              <th>responsable</th>
              <th style="text-align:right;">adquisición</th>
              <th style="text-align:right;">dep. acum.</th>
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
  // ✅ Formato numérico
  // ============================================================
  private fmt(value: any): string {
    const n = Number(value ?? 0);
    return n.toLocaleString('es-CO', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
  }
}
