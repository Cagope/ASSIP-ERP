import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class CuentasNRPrintService {

  imprimir(
    resumenAgencias: any[],
    filtros: { fechaInicial: string; fechaFinal: string; tipoInforme: 'NUEVAS' | 'RETIRADAS' }
  ): void {

    if (!resumenAgencias || resumenAgencias.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    const html = this.buildHTML(resumenAgencias, filtros);

    const win = window.open('', '_blank', 'width=1200,height=800');
    if (!win) {
      alert('Bloqueador de ventanas activado.');
      return;
    }

    win.document.open();
    win.document.write(html);
    win.document.close();
    win.onload = () => win.print();
  }

  // ============================================================
  // 🖨 HTML COMPLETO
  // ============================================================
  private buildHTML(agencias: any[], filtros: any): string {

    const titulo =
      filtros.tipoInforme === 'NUEVAS'
        ? 'Cuentas Nuevas'
        : 'Cuentas Retiradas (saldo final 0)';

    let nroPagina = 1;

    const bloques = agencias
      .map(ag => this.bloqueAgencia(ag, filtros, titulo, nroPagina++))
      .join('<div style="page-break-after: always;"></div>');

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>${titulo}</title>

        <style>

          @page { size: letter portrait; margin: 10mm 12mm; }

          body {
            font-family: Arial, sans-serif;
            font-size: 11px;
            margin: 0;
            color: #222;
          }

          /* ENCABEZADO */
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
            border: none !important;
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

  // ============================================================
  // 🟦 BLOQUE POR AGENCIA
  // ============================================================
  private bloqueAgencia(ag: any, filtros: any, titulo: string, pagina: number): string {

    const totalAgencia = ag.formas
      .flatMap((f: any) => f.detalle)
      .reduce((acc: any, x: any) => {
        const valor =
          filtros.tipoInforme === 'NUEVAS'
            ? (x.saldo_inicial ?? 0)
            : (x.ultimo_saldo ?? 0);

        return acc + valor;
      }, 0);

    const formasHTML = ag.formas
      .map((f: any) => this.bloqueForma(f, filtros.tipoInforme))
      .join('');

    return `
      <section>

        <div class="enc">
          <img src="${window.location.origin}/assets/LOGO_EMPRESA.png">
          <div>
            <div class="tit">${titulo}</div>
            <div class="sub">
              Desde: ${filtros.fechaInicial} — Hasta: ${filtros.fechaFinal}
            </div>
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

  // ============================================================
  // 🟩 BLOQUE FORMA
  // ============================================================
  private bloqueForma(f: any, tipo: 'NUEVAS' | 'RETIRADAS'): string {

    const orden = [...f.detalle].sort((a, b) =>
      (a.nombre_completo ?? '').localeCompare(b.nombre_completo ?? '')
    );

    const total = orden.reduce((acc, x) => {
      const valor =
        tipo === 'NUEVAS'
          ? (x.saldo_inicial ?? 0)
          : (x.ultimo_saldo ?? 0);

      return acc + valor;
    }, 0);

    const filas = orden
      .map(d => `
         <tr>
            <td>${d.codigo_cuenta}</td>
            <td>${d.documento}</td>
            <td>${d.nombre_completo}</td>
            <td>${d.agencia}</td>
            <td>${d.forma}</td>

            ${
              tipo === 'NUEVAS'
                ? `<td>${d.fecha_apertura}</td>`
                : `<td>${d.fecha_retiro}</td>`
            }

            <td class="right">
              ${
                tipo === 'NUEVAS'
                  ? (d.saldo_inicial ?? 0).toLocaleString('es-CO')
                  : (d.ultimo_saldo ?? 0).toLocaleString('es-CO')
              }
            </td>
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
              <th>Agencia</th>
              <th>Forma</th>
              <th>${tipo === 'NUEVAS' ? 'Fecha apertura' : 'Fecha retiro'}</th>
              <th class="right">
                ${tipo === 'NUEVAS' ? 'Saldo inicial' : 'Último saldo'}
              </th>
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
