import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class RevalorizacionPrintService {

  imprimir(resultados: any[], filtros: any) {

    if (!resultados || resultados.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    const html = this.buildHTML(resultados, filtros);

    const win = window.open('', '_blank', 'width=1200,height=800');
    if (!win) {
      alert('Bloqueador de ventanas emergentes activo.');
      return;
    }

    win.document.open();
    win.document.write(html);
    win.document.close();
    win.onload = () => win.print();
  }

  // ==========================================================
  // 🖨️ HTML COMPLETO
  // ==========================================================
  private buildHTML(resultados: any[], filtros: any): string {

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>Revalorización de Aportes</title>

        <style>

          @page { size: letter portrait; margin: 10mm 12mm; }

          body {
            font-family: Arial, sans-serif;
            font-size: 11px;
            margin: 0;
            color: #222;
          }

          /* ============================ */
          /* ENCABEZADO REAL (FUNCIONA)   */
          /* ============================ */
          .enc {
            display: flex;
            align-items: center;
            margin-bottom: 10px;
          }

          .enc img {
            height: 40px;
            margin-right: 10px;
          }

          .enc .tit {
            font-size: 16px;
            font-weight: bold;
            margin: 0;
            line-height: 1.1;
          }

          .enc .sub {
            font-size: 11px;
            margin: 1px 0 0 0;
          }

          table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 6px;
            margin-bottom: 15px;
          }

          th {
            background: #f4f4f4;
            border-bottom: 1px solid #555;
            padding: 4px 4px;
            text-align: left;
          }

          td {
            padding: 3px 4px;
            border-bottom: 1px solid #eee;
          }

          .right {
            text-align: right;
          }

        </style>

      </head>
      <body>

        ${this.buildHeader(filtros)}

        ${this.buildTable(resultados)}

      </body>
      </html>
    `;
  }

  // ==========================================================
  // 🟦 Encabezado con datos del proceso
  // ==========================================================
  private buildHeader(filtros: any): string {

    const fechaInicio = filtros.fechaInicio ?? '';
    const fechaFin = filtros.fechaFin ?? '';
    const fechaCont = filtros.fechaContabilizacion ?? '';
    const tasa = filtros.tasa ?? '';

    return `
      <div class="enc">
        <img src="${window.location.origin}/assets/LOGO_EMPRESA.png" />
        <div>
          <div class="tit">Proceso — Revalorización de Aportes</div>
          <div class="sub">Periodo: ${fechaInicio} → ${fechaFin}</div>
          <div class="sub">Fecha contabilización: ${fechaCont}</div>
          <div class="sub">Tasa aplicada: ${tasa}%</div>
        </div>
      </div>
    `;
  }

  // ==========================================================
  // 📋 Tabla de resultados
  // ==========================================================
  private buildTable(rows: any[]): string {

    return `
      <table>
        <thead>
          <tr>
            <th>Documento</th>
            <th>Nombre completo</th>
            <th class="right">Saldo actual</th>
            <th class="right">Promedio</th>
            <th class="right">Revalorización</th>
            <th>Estado</th>
          </tr>
        </thead>

        <tbody>
          ${rows.map(r => `
            <tr>
              <td>${r.documento}</td>
              <td>${r.nombreCompleto}</td>
              <td class="right">${Number(r.saldoActual).toLocaleString('es-CO')}</td>
              <td class="right">${Number(r.valorPromedio).toLocaleString('es-CO')}</td>
              <td class="right">${Number(r.valorRevalorizacion).toLocaleString('es-CO')}</td>
              <td>${r.estadoCuenta}</td>
            </tr>
          `).join('')}
        </tbody>

      </table>
    `;
  }
}
