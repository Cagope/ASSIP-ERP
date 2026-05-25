import { Injectable } from '@angular/core';

import {
  EstadisticosCdatBloqueCompleto,
  EstadisticosCdatDetalle,
  EstadisticosCdatGrupo,
  EstadisticosCdatResumen,
  EstadisticosCdatTasa
} from './estadisticos-cdat.models';

@Injectable({
  providedIn: 'root'
})
export class EstadisticosCdatPrintService {

  imprimir(
    fechaCorteActual: string,
    fechaCorteAnterior: string,
    resumen: EstadisticosCdatResumen | null,
    rangos: EstadisticosCdatGrupo[],
    amortizacion: EstadisticosCdatGrupo[],
    plazos: EstadisticosCdatGrupo[],
    plazosDetalle: EstadisticosCdatGrupo[],
    tasas: EstadisticosCdatTasa[],
    tasasDetalle: EstadisticosCdatTasa[]
  ): void {

    const html = `
      <html>

      <head>

        <title>Estadísticos CDAT</title>

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

          h2 {
            margin-top: 28px;
            margin-bottom: 10px;
            font-size: 15px;
          }

          table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 8px;
            font-size: 11px;
          }

          th {
            background: #f1f5f9;
            border: 1px solid #cbd5e1;
            padding: 7px;
            text-align: left;
          }

          td {
            border: 1px solid #cbd5e1;
            padding: 6px;
          }

          .right {
            text-align: right;
          }

          .cards {
            display: grid;
            grid-template-columns: repeat(5, 1fr);
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
          }

          .footer {
            margin-top: 28px;
            padding-top: 10px;
            border-top: 1px solid #cbd5e1;
            text-align: center;
            font-size: 10px;
            color: #64748b;
          }

        </style>

      </head>

      <body>

        <h1>Estadísticos CDAT</h1>

        <p>
          <strong>Fecha corte actual:</strong>
          ${fechaCorteActual || ''}
        </p>

        <p>
          <strong>Fecha corte anterior:</strong>
          ${fechaCorteAnterior || ''}
        </p>

        <div class="cards">

          <div class="card">
            <div class="card-title">Total CDAT</div>
            <div class="card-value">
              ${this.numero(resumen?.totalCdats)}
            </div>
          </div>

          <div class="card">
            <div class="card-title">Valor captado</div>
            <div class="card-value">
              $ ${this.numero(resumen?.valorTotalCaptado)}
            </div>
          </div>

          <div class="card">
            <div class="card-title">Promedio tasa</div>
            <div class="card-value">
              ${this.decimal(resumen?.promedioTasa)}%
            </div>
          </div>

          <div class="card">
            <div class="card-title">Promedio plazo</div>
            <div class="card-value">
              ${this.numero(resumen?.promedioPlazo)} meses
            </div>
          </div>

          <div class="card">
            <div class="card-title">Vencen 30 días</div>
            <div class="card-value">
              ${this.numero(resumen?.vencen30Dias)}
            </div>
          </div>

        </div>

        ${this.generarTablaGrupo(
          'Informe por rangos',
          'Rango',
          rangos
        )}

        ${this.generarTablaGrupo(
          'Informe por amortización',
          'Amortización',
          amortizacion
        )}

        ${this.generarTablaGrupo(
          'CDAT corto y largo plazo',
          'Plazo',
          plazos
        )}

        ${this.generarTablaGrupo(
          'CDAT por plazo',
          'Plazo',
          plazosDetalle
        )}

        ${this.generarTablaTasa(
          'Tasas de captación',
          tasas
        )}

        ${this.generarTablaTasa(
          'Tasas de captación exacta',
          tasasDetalle
        )}

        <div class="footer">
          ASSIP ERP - Estadísticos CDAT
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


  imprimirBloque(
    bloque: EstadisticosCdatBloqueCompleto<any>
  ): void {

    const html = `
      <html>
      <head>
        <title>${bloque.titulo}</title>

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

          h2 {
            margin-top: 24px;
            margin-bottom: 8px;
            font-size: 15px;
            color: #0f172a;
          }

          h3 {
            margin-top: 18px;
            margin-bottom: 6px;
            font-size: 13px;
            color: #334155;
          }

          table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 8px;
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

          .grupo {
            margin-top: 22px;
            page-break-inside: avoid;
          }

          .footer {
            margin-top: 28px;
            padding-top: 10px;
            border-top: 1px solid #cbd5e1;
            text-align: center;
            font-size: 10px;
            color: #64748b;
          }
        </style>
      </head>

      <body>

        <h1>${bloque.titulo}</h1>

        <p>
          <strong>Fecha de corte:</strong>
          ${bloque.fechaCorte || ''}
        </p>

        ${bloque.grupos.map(g => `
          <div class="grupo">

            <h2>${g.concepto}</h2>

            ${this.generarResumenGrupo(g.resumen)}

            <h3>Detalle de títulos CDAT</h3>

            ${this.generarTablaDetalle(g.detalle)}

          </div>
        `).join('')}

        <div class="footer">
          ASSIP ERP - ${bloque.titulo}
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

  private generarTablaGrupo(
    titulo: string,
    concepto: string,
    datos: EstadisticosCdatGrupo[]
  ): string {

    return `
      <h2>${titulo}</h2>

      <table>

        <thead>
          <tr>
            <th>${concepto}</th>
            <th class="right">Cantidad</th>
            <th class="right">Valor total</th>
            <th class="right">Promedio tasa</th>
            <th class="right">% participación</th>
          </tr>
        </thead>

        <tbody>

          ${datos.map(r => `
            <tr>
              <td>${r.concepto}</td>
              <td class="right">${this.numero(r.cantidad)}</td>
              <td class="right">$ ${this.numero(r.valorTotal)}</td>
              <td class="right">${this.decimal(r.promedioTasa)}%</td>
              <td class="right">${this.decimal(r.participacion)}%</td>
            </tr>
          `).join('')}

        </tbody>

      </table>
    `;
  }

  private generarTablaTasa(
    titulo: string,
    datos: EstadisticosCdatTasa[]
  ): string {

    return `
      <h2>${titulo}</h2>

      <table>

        <thead>
          <tr>
            <th>Tasa</th>
            <th class="right">Cantidad</th>
            <th class="right">Valor total</th>
            <th class="right">% participación</th>
          </tr>
        </thead>

        <tbody>

          ${datos.map(r => `
            <tr>
              <td>${r.tasa}</td>
              <td class="right">${this.numero(r.cantidad)}</td>
              <td class="right">$ ${this.numero(r.valorTotal)}</td>
              <td class="right">${this.decimal(r.participacion)}%</td>
            </tr>
          `).join('')}

        </tbody>

      </table>
    `;
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

  private generarResumenGrupo(
    r: any
  ): string {

    return `
      <table>
        <thead>
          <tr>
            <th>Concepto</th>
            <th class="right">Cantidad</th>
            <th class="right">Valor total</th>
            <th class="right">Promedio tasa</th>
            <th class="right">% participación</th>
          </tr>
        </thead>

        <tbody>
          <tr>
            <td>${r.concepto || r.tasa || ''}</td>
            <td class="right">${this.numero(r.cantidad)}</td>
            <td class="right">$ ${this.numero(r.valorTotal)}</td>
            <td class="right">${this.decimal(r.promedioTasa)}%</td>
            <td class="right">${this.decimal(r.participacion)}%</td>
          </tr>
        </tbody>
      </table>
    `;
  }

  private generarTablaDetalle(
    detalle: EstadisticosCdatDetalle[]
  ): string {

    return `
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
          ${(detalle || []).map(d => `
            <tr>
              <td>${d.codigoCdat || ''}</td>
              <td>${d.documento || ''}</td>
              <td>${d.nombreCompleto || ''}</td>
              <td>${d.agencia || ''}</td>
              <td>${d.fechaAperturaCdat || ''}</td>
              <td>${d.fechaVencimientoCdat || ''}</td>
              <td class="right">${this.numero(d.plazoMeses)}</td>
              <td class="right">${this.decimal(d.tasaNominalAnual)}%</td>
              <td class="right">$ ${this.numero(d.saldoActualCdat)}</td>
              <td>${d.estadoCdat || ''}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
  }

}
