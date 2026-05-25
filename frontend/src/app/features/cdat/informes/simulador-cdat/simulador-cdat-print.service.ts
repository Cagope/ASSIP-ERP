import { Injectable } from '@angular/core';

import {
  SimuladorAsociado,
  SimuladorCdatModel,
  SimuladorFlujoItem,
  SimuladorResumen
} from './simulador-cdat.models';

@Injectable({
  providedIn: 'root'
})
export class SimuladorCdatPrintService {

  imprimir(
    persona: SimuladorAsociado | null,
    model: SimuladorCdatModel,
    flujo: SimuladorFlujoItem[],
    resumen: SimuladorResumen | null
  ): void {

    const filas = (flujo || []).map((item: SimuladorFlujoItem) => `
      <tr>
        <td class="center">${item.periodo}</td>
        <td>${item.fechaPago || ''}</td>
        <td class="right">${this.formatoEntero(item.dias)}</td>
        <td class="right">${this.formatoNumero(item.capital)}</td>
        <td class="right">${this.formatoNumero(item.interesBruto)}</td>
        <td class="right">${this.formatoNumero(item.valorRetencion)}</td>
        <td class="right">${this.formatoNumero(item.interesNeto)}</td>
        <td class="right">${this.formatoNumero(item.pagoCliente)}</td>
        <td class="right saldo">${this.formatoNumero(item.saldoFinal)}</td>
      </tr>
    `).join('');

    const html = `
      <html>
      <head>
        <title>Simulación CDAT</title>

        <style>
          body { font-family: Arial, sans-serif; margin: 24px; color: #0f172a; }
          .header { text-align: center; margin-bottom: 18px; }
          .header h1 { margin: 0; font-size: 22px; }
          .header h2 { margin: 4px 0 0; font-size: 15px; font-weight: normal; }

          .info {
            margin-bottom: 14px;
            border: 1px solid #cbd5e1;
            padding: 12px;
          }

          .info-title {
            font-size: 13px;
            font-weight: bold;
            margin-bottom: 8px;
            color: #0b2545;
          }

          .info-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 6px 20px;
            font-size: 12px;
          }

          table {
            width: 100%;
            border-collapse: collapse;
            font-size: 10.5px;
          }

          th {
            background: #f1f5f9;
            border: 1px solid #cbd5e1;
            padding: 6px;
            text-align: left;
          }

          td {
            border: 1px solid #cbd5e1;
            padding: 5px;
          }

          .right { text-align: right; }
          .center { text-align: center; }
          .saldo { font-weight: bold; }

          .summary {
            margin-top: 16px;
            display: flex;
            justify-content: flex-end;
          }

          .summary table {
            width: 360px;
            font-size: 12px;
          }

          .summary td {
            padding: 6px 8px;
          }

          .footer {
            margin-top: 22px;
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
          <h1>Simulación CDAT</h1>
          <h2>Proyección informativa de intereses</h2>
        </div>

        <div class="info">
          <div class="info-title">Datos del asociado</div>

          <div class="info-grid">
            <div><strong>Asociado:</strong> ${persona?.nombre_completo || ''}</div>
            <div><strong>Documento:</strong> ${persona?.documento || ''}</div>
            <div><strong>Cuenta:</strong> ${persona?.codigo_cuenta || ''}</div>
            <div><strong>Forma:</strong> ${persona?.nombre_forma_ahorro || ''}</div>
            <div><strong>Agencia:</strong> ${persona?.agencia || ''}</div>
            <div><strong>Saldo cuenta:</strong> ${this.formatoNumero(persona?.saldo_actual_cuenta)}</div>
          </div>
        </div>

        <div class="info">
          <div class="info-title">Datos de la simulación</div>

          <div class="info-grid">
            <div><strong>Valor CDAT:</strong> ${this.formatoNumero(model.valorCdat)}</div>
            <div><strong>Tasa nominal anual:</strong> ${this.formatoNumero(model.tasaNominalAnual)}%</div>
            <div><strong>Tasa efectiva anual:</strong> ${this.formatoNumero(model.tasaEfectivaAnual)}%</div>
            <div><strong>Fecha apertura:</strong> ${model.fechaApertura || ''}</div>
            <div><strong>Fecha vencimiento:</strong> ${model.fechaVencimiento || ''}</div>
            <div><strong>Plazo meses:</strong> ${model.plazoMeses || ''}</div>
            <div><strong>Pago intereses:</strong> ${model.formaPagoInteres || ''}</div>
            <div><strong>Aplica retención:</strong> ${model.aplicaRetencion ? 'Sí' : 'No'}</div>
            <div><strong>Base retención:</strong> ${this.formatoNumero(model.baseRetencion)}</div>
            <div><strong>Porcentaje retención:</strong> ${this.formatoNumero(model.porcentajeRetencion)}%</div>
          </div>
        </div>

        <table>
          <thead>
            <tr>
              <th>Periodo</th>
              <th>Fecha pago</th>
              <th class="right">Días</th>
              <th class="right">Capital</th>
              <th class="right">Interés bruto</th>
              <th class="right">Retención</th>
              <th class="right">Interés neto</th>
              <th class="right">Pago cliente</th>
              <th class="right">Saldo final</th>
            </tr>
          </thead>

          <tbody>
            ${filas}
          </tbody>
        </table>

        <div class="summary">
          <table>
            <tbody>
              <tr>
                <td><strong>Capital</strong></td>
                <td class="right">${this.formatoNumero(resumen?.capital)}</td>
              </tr>
              <tr>
                <td><strong>Total interés bruto</strong></td>
                <td class="right">${this.formatoNumero(resumen?.totalInteresBruto)}</td>
              </tr>
              <tr>
                <td><strong>Total retención</strong></td>
                <td class="right">${this.formatoNumero(resumen?.totalRetencion)}</td>
              </tr>
              <tr>
                <td><strong>Total interés neto</strong></td>
                <td class="right">${this.formatoNumero(resumen?.totalInteresNeto)}</td>
              </tr>
              <tr>
                <td><strong>Total pago cliente</strong></td>
                <td class="right">${this.formatoNumero(resumen?.totalPagoCliente)}</td>
              </tr>
              <tr>
                <td><strong>Valor al vencimiento</strong></td>
                <td class="right saldo">${this.formatoNumero(resumen?.valorAlVencimiento)}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="footer">
          ASSIP ERP - Simulador CDAT. Documento informativo, no genera CDAT ni movimientos contables.
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

  private formatoEntero(valor: any): string {
    const numero = Number(valor || 0);

    return numero.toLocaleString('es-CO', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    });
  }
}
