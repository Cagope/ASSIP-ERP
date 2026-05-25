import { Injectable } from '@angular/core';

import {
  ExtractoCuentaMovimiento,
  ExtractoCuentaResumen
} from './extracto-cuenta.api';

@Injectable({
  providedIn: 'root'
})
export class ExtractoCuentaPrintService {

  imprimir(
    resumen: ExtractoCuentaResumen,
    movimientos: ExtractoCuentaMovimiento[],
    fechaInicial: string,
    fechaFinal: string
  ): void {

    const html =
      this.construirHtml(resumen, movimientos, fechaInicial, fechaFinal);

    const iframe =
      document.createElement('iframe');

    iframe.style.position = 'fixed';
    iframe.style.right = '0';
    iframe.style.bottom = '0';
    iframe.style.width = '0';
    iframe.style.height = '0';
    iframe.style.border = '0';

    document.body.appendChild(iframe);

    const doc =
      iframe.contentWindow?.document;

    if (!doc) {
      document.body.removeChild(iframe);
      return;
    }

    doc.open();
    doc.write(html);
    doc.close();

    iframe.onload = () => {
      iframe.contentWindow?.focus();
      iframe.contentWindow?.print();

      setTimeout(() => {
        document.body.removeChild(iframe);
      }, 1000);
    };
  }

  private construirHtml(
    resumen: ExtractoCuentaResumen,
    movimientos: ExtractoCuentaMovimiento[],
    fechaInicial: string,
    fechaFinal: string
  ): string {

    const filas =
      movimientos
        .map(m => `
          <tr>
            <td>${this.valor(m.fechaMovimiento)}</td>
            <td>${this.valor(m.tipoMovimiento)}</td>
            <td>${this.valor(m.descripcionMovimiento)}</td>
            <td>${this.valor(m.tipoComprobante)}</td>
            <td>${this.valor(m.numeroComprobante)}</td>
            <td class="num">${this.numero(m.debito)}</td>
            <td class="num">${this.numero(m.credito)}</td>
            <td class="num">${this.numero(m.saldo)}</td>
          </tr>
        `)
        .join('');

    return `
      <!doctype html>
      <html>
      <head>
        <meta charset="utf-8">
        <title>Extracto de cuenta</title>

        <style>
          @page {
            size: letter;
            margin: 12mm 10mm;
          }

          body {
            font-family: Arial, sans-serif;
            font-size: 11px;
            color: #111827;
          }

          .header {
            display: grid;
            grid-template-columns: 190px 1fr;
            gap: 22px;
            align-items: center;

            border: 1px solid #d1d5db;
            border-bottom: 4px solid #0f5132;

            padding: 16px 20px;
            margin-bottom: 14px;
          }

          .logo-box {
            width: 160px;
            height: 120px;

            display: flex;
            align-items: center;
            justify-content: center;

            border: 1px solid #e5e7eb;
            border-radius: 8px;

            padding: 8px;
            overflow: hidden;
          }

          .logo-box img {
            width: 100%;
            height: 100%;
            object-fit: contain;
          }

          .empresa-box {
            text-align: center;
            border-left: 1px solid #d1d5db;
            padding-left: 22px;
          }

          h1 {
            margin: 0;
            font-size: 22px;
            line-height: 1.25;
            font-weight: 900;
            text-transform: uppercase;
          }

          .agencia {
            margin-top: 8px;
            font-size: 14px;
            font-weight: 800;
            color: #0f6b3a;
          }

          .empresa-info {
            margin-top: 8px;
            font-size: 12px;
            color: #374151;
          }

          .titulo {
            margin-top: 16px;
            font-size: 16px;
            font-weight: 900;
            color: #0f6b3a;
            text-transform: uppercase;

            display: flex;
            align-items: center;
            gap: 14px;
            justify-content: center;

            white-space: nowrap;
          }

          .titulo::before,
          .titulo::after {
            content: "";
            height: 2px;
            background: #0f6b3a;
            width: 90px;
          }

          .section-title {
            margin-top: 12px;
            margin-bottom: 5px;
            font-size: 12px;
            font-weight: 800;
            color: #111827;
            border-bottom: 1px solid #d1d5db;
            padding-bottom: 3px;
          }

          .box {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 6px;
            margin-bottom: 8px;
          }

          .box.cuenta {
            grid-template-columns: repeat(4, 1fr);
          }

          .item {
            border: 1px solid #d1d5db;
            padding: 6px;
          }

          .label {
            color: #6b7280;
            font-size: 10px;
          }

          .value {
            font-weight: bold;
            margin-top: 2px;
          }

          .resumen {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 6px;
            margin-bottom: 10px;
          }

          table {
            width: 100%;
            border-collapse: collapse;
            font-size: 10px;
          }

          th,
          td {
            border: 1px solid #d1d5db;
            padding: 4px;
            vertical-align: top;
          }

          th {
            background: #f3f4f6;
            font-weight: bold;
            text-align: center;
          }

          .num {
            text-align: right;
            white-space: nowrap;
          }

          .footer {
            margin-top: 12px;
            font-size: 10px;
            color: #6b7280;
            text-align: center;
          }
        </style>
      </head>

      <body>

        <div class="header">

          <div class="logo-box">
            ${
              resumen.logoUrl
                ? `<img src="${this.valor(resumen.logoUrl)}" alt="Logo">`
                : 'LOGO'
            }
          </div>

          <div class="empresa-box">

            <h1>
              ${this.valor(resumen.razonSocial)}
            </h1>

            <div class="agencia">
              ${this.valor(resumen.nombreAgencia)}
            </div>

            <div class="empresa-info">

              NIT:
              ${this.valor(resumen.documentoEmpresa)}

              ${resumen.digitoVerificacion
                ? '- ' + this.valor(resumen.digitoVerificacion)
                : ''}

            </div>

            <div class="titulo">
              EXTRACTO DE MOVIMIENTOS
            </div>

          </div>

        </div>


        <div class="box">

          <div class="item">
            <div class="label">Señor(a)</div>
            <div class="value">${this.valor(resumen.nombreCompleto)}</div>
          </div>

          <div class="item">
            <div class="label">Documento</div>
            <div class="value">${this.valor(resumen.documento)}</div>
          </div>

          <div class="item">
            <div class="label">Dirección</div>
            <div class="value">${this.valor(resumen.direccion)}</div>
          </div>

        </div>


        <div class="box cuenta">

          <div class="item">
            <div class="label">Cuenta</div>
            <div class="value">${this.valor(resumen.codigoCuenta)}</div>
          </div>

          <div class="item">
            <div class="label">Tipo de depósito</div>
            <div class="value">${this.valor(resumen.codigoForma)} - ${this.valor(resumen.nombreForma)}</div>
          </div>

          <div class="item">
            <div class="label">Fecha inicial</div>
            <div class="value">${this.valor(fechaInicial)}</div>
          </div>

          <div class="item">
            <div class="label">Fecha final</div>
            <div class="value">${this.valor(fechaFinal)}</div>
          </div>

        </div>

        <div class="resumen">

          <div class="item">
            <div class="label">Saldo inicial</div>
            <div class="value num">${this.numero(resumen.saldoInicial)}</div>
          </div>

          <div class="item">
            <div class="label">Total créditos</div>
            <div class="value num">${this.numero(resumen.totalCreditos)}</div>
          </div>

          <div class="item">
            <div class="label">Total débitos</div>
            <div class="value num">${this.numero(resumen.totalDebitos)}</div>
          </div>

          <div class="item">
            <div class="label">Nuevo saldo</div>
            <div class="value num">${this.numero(resumen.saldoFinal)}</div>
          </div>

        </div>

        <table>
          <thead>
            <tr>
              <th rowspan="2">Fecha</th>
              <th colspan="2">Concepto</th>
              <th colspan="2">Comprobante</th>
              <th rowspan="2">Débito</th>
              <th rowspan="2">Crédito</th>
              <th rowspan="2">Saldo</th>
            </tr>
            <tr>
              <th>Tipo</th>
              <th>Descripción</th>
              <th>Tipo</th>
              <th>Número</th>
            </tr>
          </thead>

          <tbody>
            ${filas}
          </tbody>
        </table>

        <div class="footer">
          Documento generado por ASSIP ERP.
        </div>

      </body>
      </html>
    `;
  }

  private valor(valor: any): string {
    return valor === null || valor === undefined || valor === ''
      ? ''
      : String(valor);
  }

  private numero(valor: any): string {
    return Number(valor || 0).toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  }

}
