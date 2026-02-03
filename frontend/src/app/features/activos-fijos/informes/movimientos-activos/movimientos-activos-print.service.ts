import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class MovimientosActivosPrintService {

  imprimir(rows: any[], filtros?: {
    nombreAgencia?: string;
    nombreMovimiento?: string;
    fechaIni?: string | null;
    fechaFin?: string | null;
    idActivoFijo?: number | null;
    placaActivo?: string | null;
    nombreActivo?: string | null;
  }): void {

    if (!rows || rows.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    const win = window.open('', '_blank', 'width=1400,height=900');
    if (!win) return;

    win.document.open();
    win.document.write(this.buildHTML(rows, filtros));
    win.document.close();

    win.onload = () => win.print();
  }

  // ============================================================
  // 🖨 Construcción del HTML del informe
  // ✅ Carta Horizontal (LANDSCAPE)
  // ============================================================
  private buildHTML(rows: any[], filtros?: any): string {

    const fecha = new Date().toLocaleString('es-CO');

    const nombreAgencia = filtros?.nombreAgencia || 'TODAS';
    const nombreMovimiento = filtros?.nombreMovimiento || 'TODOS';
    const rangoFechas = (filtros?.fechaIni || filtros?.fechaFin)
      ? `${filtros?.fechaIni ?? '...'} → ${filtros?.fechaFin ?? '...'}`
      : 'TODAS';

    const activoTxt = filtros?.idActivoFijo
      ? `ID ${filtros?.idActivoFijo} ${filtros?.placaActivo ? '— ' + filtros?.placaActivo : ''} ${filtros?.nombreActivo ? '— ' + filtros?.nombreActivo : ''}`
      : 'TODOS';

    const totalDebito = rows.reduce((acc, r) => acc + Number(r?.valor_debito ?? 0), 0);
    const totalCredito = rows.reduce((acc, r) => acc + Number(r?.valor_credito ?? 0), 0);
    const neto = totalDebito - totalCredito;

    const filasHTML = rows.map(r => `
      <tr>
        <td class="nowrap">${r.fecha ?? ''}</td>
        <td class="nowrap">${r.hora ?? ''}</td>

        <td class="nowrap">${r.id_activo_fijo ?? ''}</td>
        <td class="nowrap"><strong>${r.placa_activo ?? ''}</strong></td>
        <td>${r.nombre_activo ?? ''}</td>

        <td>${r.nombre_agencia ?? ''}</td>

        <td class="nowrap">
          ${r.codigo_movimiento ?? ''} - ${r.nombre_movimiento ?? ''}
        </td>

        <td class="nowrap">
          ${r.tipo_comprobante ?? ''} ${r.numero_comprobante ?? ''}
        </td>

        <td class="right nowrap">${this.fmt(r.valor_debito)}</td>
        <td class="right nowrap">${this.fmt(r.valor_credito)}</td>
      </tr>
    `).join('');

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>Movimientos de Activos</title>

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

          .meta {
            text-align: right;
            font-size: 10px;
            line-height: 1.35;
          }

          .submeta {
            margin-top: 4px;
            font-size: 10px;
            color: #444;
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

          .right {
            text-align: right;
          }

          .nowrap {
            white-space: nowrap;
          }

          tfoot td {
            font-weight: bold;
            border-top: 1px solid #777;
            background: #fafafa;
          }

        </style>

      </head>
      <body>

        <div class="enc">
          <div class="enc-left">
            <img src="${window.location.origin}/assets/LOGO_EMPRESA.png">
            <div class="titulo">Activos Fijos – Movimientos de Activos</div>
          </div>

          <div class="meta">
            <div><strong>Agencia:</strong> ${nombreAgencia}</div>
            <div><strong>Movimiento:</strong> ${nombreMovimiento}</div>
            <div><strong>Fechas:</strong> ${rangoFechas}</div>
            <div><strong>Activo:</strong> ${activoTxt}</div>
            <div class="submeta"><strong>Fecha impresión:</strong> ${fecha}</div>
            <div><strong>Total filas:</strong> ${rows.length}</div>
          </div>
        </div>

        <h2>Detalle de Movimientos</h2>

        <table>
          <thead>
            <tr>
              <th>fecha</th>
              <th>hora</th>
              <th>id</th>
              <th>placa</th>
              <th>activo</th>
              <th>agencia</th>
              <th>movimiento</th>
              <th>comprobante</th>
              <th style="text-align:right;">débito</th>
              <th style="text-align:right;">crédito</th>
            </tr>
          </thead>

          <tbody>
            ${filasHTML}
          </tbody>

          <tfoot>
            <tr>
              <td colspan="8" class="right">TOTALES</td>
              <td class="right nowrap">${this.fmt(totalDebito)}</td>
              <td class="right nowrap">${this.fmt(totalCredito)}</td>
            </tr>
            <tr>
              <td colspan="8" class="right">NETO (D - C)</td>
              <td colspan="2" class="right nowrap">${this.fmt(neto)}</td>
            </tr>
          </tfoot>
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
