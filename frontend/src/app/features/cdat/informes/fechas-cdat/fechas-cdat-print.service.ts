import { Injectable } from '@angular/core';

import {
  FechasCdatItem,
  FechasCdatResumen,
  FechasCdatTipoInforme
} from './fechas-cdat.models';

@Injectable({
  providedIn: 'root'
})
export class FechasCdatPrintService {

  imprimir(
    tipoInforme: FechasCdatTipoInforme,
    fechaInicial: string,
    fechaFinal: string,
    resumen: FechasCdatResumen,
    resultados: FechasCdatItem[]
  ): void {

    const html = `
      <html>
      <head>
        <title>Informes por fechas CDAT</title>

        <style>
          body {
            font-family: Arial, sans-serif;
            margin: 24px;
            color: #0f172a;
          }

          h1 {
            margin: 0;
            text-align: center;
            font-size: 22px;
          }

          .subtitulo {
            margin-top: 6px;
            text-align: center;
            font-size: 12px;
            color: #64748b;
          }

          .filtros {
            margin-top: 18px;
            font-size: 12px;
          }

          .cards {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 12px;
            margin-top: 18px;
            margin-bottom: 20px;
          }

          .card {
            border: 1px solid #cbd5e1;
            border-radius: 8px;
            padding: 10px;
          }

          .card-title {
            font-size: 11px;
            color: #64748b;
          }

          .card-value {
            margin-top: 6px;
            font-size: 16px;
            font-weight: bold;
            text-align: right;
          }

          table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 12px;
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

          .right {
            text-align: right;
          }

          .footer {
            margin-top: 28px;
            padding-top: 10px;
            border-top: 1px solid #cbd5e1;
            text-align: center;
            font-size: 10px;
            color: #64748b;
          }

          @page {
            size: letter landscape;
            margin: 14mm;
          }
        </style>
      </head>

      <body>

        <h1>Informes por fechas CDAT</h1>

        <div class="subtitulo">
          Informe operativo de títulos CDAT
        </div>

        <div class="filtros">
          <p><strong>Tipo informe:</strong> ${this.nombreTipo(tipoInforme)}</p>
          <p><strong>Fecha inicial:</strong> ${fechaInicial || ''}</p>
          <p><strong>Fecha final:</strong> ${fechaFinal || ''}</p>
          <p><strong>Fecha impresión:</strong> ${new Date().toLocaleString('es-CO')}</p>
        </div>

        <div class="cards">

          <div class="card">
            <div class="card-title">Cantidad títulos</div>
            <div class="card-value">${this.numero(resumen?.cantidad)}</div>
          </div>

          <div class="card">
            <div class="card-title">Valor total</div>
            <div class="card-value">$ ${this.numero(resumen?.valorTotal)}</div>
          </div>

          <div class="card">
            <div class="card-title">Promedio tasa</div>
            <div class="card-value">${this.decimal(resumen?.promedioTasa)}%</div>
          </div>

          <div class="card">
            <div class="card-title">Promedio plazo</div>
            <div class="card-value">${this.numero(resumen?.promedioPlazo)} meses</div>
          </div>

        </div>

        <table>
          <thead>
            <tr>
              <th>CDAT</th>
              <th>Documento</th>
              <th>Asociado</th>
              <th>Agencia</th>
              <th>F. Apertura</th>
              <th>F. Vencimiento</th>
              <th class="right">Plazo</th>
              <th class="right">Tasa</th>
              <th class="right">Valor</th>
              <th>Estado</th>
            </tr>
          </thead>

          <tbody>
            ${(resultados || []).map(r => `
              <tr>
                <td>${r.codigoCdat || ''}</td>
                <td>${r.documento || ''}</td>
                <td>${r.nombreCompleto || ''}</td>
                <td>${r.agencia || ''}</td>
                <td>${r.fechaApertura || ''}</td>
                <td>${r.fechaVencimiento || ''}</td>
                <td class="right">${this.numero(r.plazoMeses)}</td>
                <td class="right">${this.decimal(r.tasa)}%</td>
                <td class="right">$ ${this.numero(r.valor)}</td>
                <td>${r.estado || ''}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>

        <div class="footer">
          ASSIP ERP - Informes por fechas CDAT
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

    const ventana =
      window.open('', '_blank', 'width=1400,height=900');

    if (!ventana) {
      return;
    }

    ventana.document.open();
    ventana.document.write(html);
    ventana.document.close();
  }

  private nombreTipo(
    tipo: FechasCdatTipoInforme
  ): string {

    switch (tipo) {
      case 'NUEVOS':
        return 'CDAT nuevos';
      case 'CANCELADOS':
        return 'CDAT cancelados';
      case 'VENCER':
        return 'Próximos a vencer';
      case 'VENCIDOS':
        return 'CDAT vencidos';
      case 'RENOVADOS':
        return 'CDAT renovados';
      default:
        return tipo;
    }
  }

  private numero(valor: any): string {

    return Number(valor || 0)
      .toLocaleString('es-CO', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      });
  }

  private decimal(valor: any): string {

    return Number(valor || 0)
      .toLocaleString('es-CO', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      });
  }

}
