import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ConsultaCdatsExtractoPrintService {

  imprimir(
    cdat: any,
    fechaInicial: string,
    fechaFinal: string,
    movimientos: any[],
    resumenPorMovimiento: any[]
  ): void {

    const nombre =
      cdat?.nombre_completo_apellidos
      || cdat?.nombre_completo_nombres
      || cdat?.nombres
      || '';

    const filas = movimientos.map((m: any) => `
      <tr>
        <td>${m.fecha_movimiento || ''}</td>
        <td>${m.tipo_comprobante || ''} - ${m.numero_comprobante || ''}</td>
        <td>${m.descripcion_movimiento || ''}</td>
        <td class="right">${this.formatoNumero(m.valor_debito)}</td>
        <td class="right">${this.formatoNumero(m.valor_credito)}</td>
      </tr>
    `).join('');

    const filasResumenMovimiento = (resumenPorMovimiento || []).map((r: any) => `
      <tr>
        <td>${r.tipoMovimiento || ''}</td>
        <td>${r.descripcionMovimiento || ''}</td>
        <td class="right saldo">${this.formatoNumero(r.valor)}</td>
      </tr>
    `).join('');

    const html = `
      <html>
      <head>
        <title>Extracto CDAT ${cdat?.codigo_cdat || ''}</title>
        <style>
          body { font-family: Arial, sans-serif; margin: 24px; color: #0f172a; }
          .header { text-align: center; margin-bottom: 20px; }
          .header h1 { margin: 0; font-size: 22px; }
          .header h2 { margin: 4px 0 0; font-size: 16px; font-weight: normal; }

          .info { margin-bottom: 18px; border: 1px solid #cbd5e1; padding: 12px; }
          .info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 6px 20px; font-size: 12px; }

          table { width: 100%; border-collapse: collapse; font-size: 11px; }
          th { background: #f1f5f9; border: 1px solid #cbd5e1; padding: 7px; text-align: left; }
          td { border: 1px solid #cbd5e1; padding: 6px; }
          .right { text-align: right; }
          .saldo { font-weight: bold; }

          .summary { margin-top: 14px; display: flex; justify-content: flex-end; }
          .summary table { width: 320px; font-size: 12px; }
          .summary td { padding: 6px 8px; }

          .summary-movimientos { margin-top: 22px; }
          .summary-movimientos h3 { margin: 0 0 10px; font-size: 14px; }

          .footer {
            margin-top: 24px;
            padding-top: 10px;
            border-top: 1px solid #cbd5e1;
            text-align: center;
            font-size: 10px;
            color: #64748b;
          }
        </style>
      </head>

      <body>
        <div class="header">
          <h1>Extracto CDAT</h1>
          <h2>CDAT ${cdat?.codigo_cdat || ''}</h2>
        </div>

        <div class="info">
          <div class="info-grid">
            <div><strong>Asociado:</strong> ${nombre}</div>
            <div><strong>Documento:</strong> ${cdat?.documento || ''}</div>
            <div><strong>Fecha inicial:</strong> ${fechaInicial}</div>
            <div><strong>Fecha final:</strong> ${fechaFinal}</div>
            <div><strong>Saldo actual:</strong> ${this.formatoNumero(cdat?.saldo_actual_cdat)}</div>
            <div><strong>Tasa:</strong> ${this.formatoNumero(cdat?.tasa_nominal_anual)}%</div>
          </div>
        </div>

        <table>
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Comprobante</th>
              <th>Movimiento</th>
              <th class="right">Débito</th>
              <th class="right">Crédito</th>
            </tr>
          </thead>
          <tbody>${filas}</tbody>
        </table>

        <div class="summary-movimientos">
          <h3>Resumen por movimiento</h3>

          <table>
            <thead>
              <tr>
                <th>Tipo</th>
                <th>Movimiento</th>
                <th class="right">Valor</th>
              </tr>
            </thead>
            <tbody>${filasResumenMovimiento}</tbody>
          </table>
        </div>

        <div class="footer">
          ASSIP ERP - Extracto CDAT
        </div>

        <script>
          window.onload = () => {
            window.print();
            window.close();
          };
        </script>
      </body>
      </html>
    `;

    const ventana = window.open('', '_blank', 'width=1200,height=800');

    if (!ventana) {
      return;
    }

    ventana.document.open();
    ventana.document.write(html);
    ventana.document.close();
  }

  private formatoNumero(valor: any): string {
    const numero = Number(valor || 0);

    return numero.toLocaleString('es-CO', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  }
}
