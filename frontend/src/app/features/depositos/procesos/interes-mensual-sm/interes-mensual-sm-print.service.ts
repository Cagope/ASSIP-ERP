import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class InteresMensualSmPrintService {

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
        <title>Interés Mensual SM</title>

        <style>

          @page { size: letter portrait; margin: 10mm 12mm; }

          body {
            font-family: Arial, sans-serif;
            font-size: 11px;
            margin: 0;
            color: #222;
          }

          /* ============================ */
          /* ENCABEZADO REAL              */
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

    const fechaProceso = filtros.fechaProceso ?? '';
    const fechaLiquidacion = filtros.fechaLiquidacion ?? '';

    const forma = `${filtros.codigoForma ?? ''} — ${filtros.nombreForma ?? ''}`;

    const agencia = filtros.codigoAgencia
      ? `${filtros.codigoAgencia} — ${filtros.nombreAgencia ?? ''}`
      : '';

    return `
      <div class="enc">
        <img src="${window.location.origin}/assets/LOGO_EMPRESA.png" />
        <div>
          ${agencia ? `<div class="tit">${agencia}</div>` : ''}
          <div class="tit">Proceso — Interés Mensual SM</div>
          <div class="sub">Fecha proceso: ${fechaProceso}</div>
          <div class="sub">Fecha liquidación: ${fechaLiquidacion}</div>
          <div class="sub">Forma: ${forma}</div>
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

            <th class="right">Saldo mínimo mes</th>
            <th class="right">Interés bruto</th>
            <th class="right">Retención</th>
            <th class="right">Interés neto</th>

            <th class="right">Tasa (%)</th>
            <th class="right">Mínimo forma</th>
            <th>Aplica retención</th>
          </tr>
        </thead>

        <tbody>
          ${rows.map(r => `
            <tr>
              <td>${r.documento}</td>
              <td>${r.nombreCompleto}</td>

              <td class="right">${Number(r.saldoMinimoMes).toLocaleString('es-CO')}</td>
              <td class="right">${Number(r.interesBruto).toLocaleString('es-CO')}</td>
              <td class="right">${Number(r.retencion).toLocaleString('es-CO')}</td>
              <td class="right">${Number(r.interesNeto).toLocaleString('es-CO')}</td>

              <td class="right">${Number(r.tasaInteres).toLocaleString('es-CO')}</td>
              <td class="right">${Number(r.minimoForma).toLocaleString('es-CO')}</td>
              <td>${r.aplicaRetencion ? 'Sí' : 'No'}</td>
            </tr>
          `).join('')}
        </tbody>

      </table>
    `;
  }
}
