import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SaldosCortePrintService {

  imprimir(
    resumenAgencias: any[],
    fechaCorte: string
  ): void {

    if (!resumenAgencias || resumenAgencias.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    const html = this.buildHTML(resumenAgencias, fechaCorte);

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
  private buildHTML(agencias: any[], fechaCorte: string): string {

    let nroPagina = 1;

    const bloques = agencias
      .map(ag => this.bloqueAgencia(ag, fechaCorte, nroPagina++))
      .join('<div style="page-break-after: always;"></div>');

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>Saldos a Corte</title>

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
            margin-bottom: 4px;
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

          .pagina {
            margin-left: auto;
            font-size: 11px;
            font-weight: bold;
          }

          /* ============================ */
          /* AGENCIA Y FORMAS            */
          /* ============================ */
          h2 {
            font-size: 14px;
            margin: 6px 0 3px;
            border-bottom: 1px solid #777;
            padding-bottom: 2px;
          }

          h3 {
            font-size: 12px;
            margin: 4px 0 2px;
          }

          table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 4px;
          }

          th {
            background: #f4f4f4;
            border-bottom: 1px solid #555;
            padding: 3px 4px;
            text-align: left;
          }

          td {
            padding: 3px 4px;
            border: none !important;   /* 🔥 SIN LÍNEAS */
          }

          .right {
            text-align: right;
          }

          .total-forma {
            font-weight: bold;
            text-align: right;
            margin-top: 3px;
            background: #eee;
            padding: 4px 2px;
            border-top: 1px solid #999;
          }

          .total-agencia {
            font-weight: bold;
            text-align: right;
            background: #ddd;
            padding: 6px;
            margin-top: 6px;
          }

        </style>

      </head>
      <body>

        ${bloques}

      </body>
      </html>
    `;
  }

  // ==========================================================
  // 🟦 BLOQUE AGENCIA CON ENCABEZADO + PÁGINA
  // ==========================================================
  private bloqueAgencia(ag: any, fecha: string, pagina: number): string {

    const totalAgencia = ag.formas
      .flatMap((f: any) => f.detalle)
      .reduce((acc: any, x: any) => acc + (x.saldoCorte ?? 0), 0);

    const formasHTML = ag.formas
      .map((f: any) => this.bloqueForma(f))
      .join('');

    return `
      <section>

        <!-- 🔥 ENCABEZADO REAL (Chrome sí lo imprime) -->
        <div class="enc">
          <img src="${window.location.origin}/assets/LOGO_EMPRESA.png">
          <div>
            <div class="tit">Saldos a una Fecha de Corte</div>
            <div class="sub">Fecha de corte: ${fecha}</div>
          </div>

          <div class="pagina">Página ${pagina}</div>
        </div>

        <h2>Agencia ${ag.codigoAgencia} — ${ag.nombreAgencia}</h2>

        ${formasHTML}

        <div class="total-agencia">
          TOTAL AGENCIA:
          ${totalAgencia.toLocaleString('es-CO', { minimumFractionDigits: 2 })}
        </div>

      </section>
    `;
  }

  // ==========================================================
  // 🟩 BLOQUE FORMA (compacto)
  // ==========================================================
  private bloqueForma(f: any): string {

    const orden = [...f.detalle].sort((a, b) =>
      (a.nombreCompleto ?? '').localeCompare(b.nombreCompleto ?? '')
    );

    const total = orden.reduce(
      (acc, x) => acc + (x.saldoCorte ?? 0),
      0
    );

    const filas = orden
      .map(d => `
         <tr>
            <td>${d.codigoCuenta}</td>
            <td>${d.documento}</td>
            <td>${d.nombreCompleto}</td>
            <td>${d.estadoCuentaNombre}</td>
            <td class="right">${d.saldoCorte.toLocaleString('es-CO')}</td>
         </tr>
      `).join('');

    return `
      <section style="page-break-inside: avoid;">

        <h3>Código Forma ${f.codigoForma} — ${f.nombreForma}</h3>

        <table>
          <thead>
            <tr>
              <th>Cuenta</th>
              <th>Documento</th>
              <th>Nombre</th>
              <th>Estado</th>
              <th class="right">Saldo a Corte</th>
            </tr>
          </thead>
          <tbody>${filas}</tbody>
        </table>

        <div class="total-forma">
          TOTAL ${f.nombreForma}:
          ${total.toLocaleString('es-CO', { minimumFractionDigits: 2 })}
        </div>

      </section>
    `;
  }

}
