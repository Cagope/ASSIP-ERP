import { Injectable } from '@angular/core';
import { VariablesVigenciaListDTO, VariablesVigenciaFormDTO } from './variables-vigencia.api';

type PrintDTO = VariablesVigenciaListDTO & Partial<VariablesVigenciaFormDTO>;

@Injectable({ providedIn: 'root' })
export class VariablesVigenciaPrintService {

  imprimir(items: PrintDTO[]): void {

    if (!items || items.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    const win = window.open('', '_blank', 'width=1200,height=800');
    if (!win) return;

    win.document.open();
    win.document.write(this.buildHTML(items));
    win.document.close();

    win.onload = () => win.print();
  }

  private buildHTML(items: PrintDTO[]): string {

    const bloques = items.map((x) => this.bloqueVigencia(x)).join('');

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>Variables Vigencia</title>

        <style>
          @page { size: letter portrait; margin: 10mm 12mm; }

          body {
            font-family: Arial, sans-serif;
            font-size: 11px;
            margin: 0;
            color: #222;
          }

          .enc {
            display: flex;
            align-items: center;
            margin-bottom: 10px;
          }

          .enc img {
            height: 45px;
            margin-right: 12px;
          }

          .titulo {
            font-size: 18px;
            font-weight: bold;
            margin: 0;
          }

          h2 {
            font-size: 14px;
            margin: 10px 0 5px;
            padding-bottom: 3px;
            border-bottom: 1px solid #777;
          }

          .vigencia {
            border: 1px solid #ddd;
            border-radius: 8px;
            padding: 10px;
            margin-bottom: 10px;
            page-break-inside: avoid;
          }

          .vigencia-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 6px;
          }

          .vigencia-title {
            font-size: 12px;
            font-weight: bold;
            text-transform: uppercase;
          }

          .estado {
            font-size: 11px;
            font-weight: bold;
            padding: 3px 8px;
            border-radius: 10px;
            border: 1px solid #ccc;
            text-transform: uppercase;
          }

          .ok { background: #e9fbe9; border-color: #7ad67a; }
          .off { background: #fdecec; border-color: #f59f9f; }

          .kv {
            width: 100%;
            border-collapse: collapse;
            margin-top: 6px;
          }

          .kv tr td {
            border-bottom: 0.5px solid #eee;
            padding: 4px 2px;
            vertical-align: top;
          }

          .k {
            width: 45%;
            color: #555;
            text-transform: lowercase;
          }

          .v {
            width: 55%;
            font-weight: 600;
            text-transform: uppercase;
          }

          .v.num { text-transform: none; text-align: right; font-weight: 700; }
          .v.bool { text-transform: uppercase; }

          .muted { color: #777; font-weight: normal; }
        </style>

      </head>
      <body>

        <div class="enc">
          <img src="${window.location.origin}/assets/LOGO_EMPRESA.png">
          <div>
            <div class="titulo">Nómina – Variables Vigencia</div>
            <div class="muted">Impresión por vigencias (cada campo en un renglón)</div>
          </div>
        </div>

        <h2>Listado</h2>

        ${bloques}

      </body>
      </html>
    `;
  }

  private bloqueVigencia(x: PrintDTO): string {

    const estadoClass = x.activo ? 'ok' : 'off';
    const estadoText = x.activo ? 'ACTIVO' : 'INACTIVO';

    const fechaIni = x.fechaInicial ?? '';
    const fechaFin = x.fechaFinal ?? '';

    // ✅ helper para imprimir null/undefined
    const val = (v: any) => (v === null || v === undefined || v === '' ? '—' : v);

    // ✅ helper para numericos
    const num = (v: any) => (v === null || v === undefined ? '—' : v);

    // ✅ boolean
    const bool = (v: any) => (v ? 'SI' : 'NO');

    return `
      <div class="vigencia">
        <div class="vigencia-header">
          <div class="vigencia-title">
            Vigencia #${val(x.idVariable)} — ${val(fechaIni)} → ${val(fechaFin)}
          </div>
          <div class="estado ${estadoClass}">${estadoText}</div>
        </div>

        <table class="kv">
          <tbody>
            <tr><td class="k">fecha inicial</td><td class="v">${val(fechaIni)}</td></tr>
            <tr><td class="k">fecha final</td><td class="v">${val(fechaFin)}</td></tr>

            <tr><td class="k">smmlv</td><td class="v num">${num((x as any).smmlv)}</td></tr>
            <tr><td class="k">aux transporte</td><td class="v num">${num((x as any).auxTransporte)}</td></tr>

            <tr><td class="k">% salud empleado</td><td class="v num">${num((x as any).porcSaludEmpleado)}</td></tr>
            <tr><td class="k">% salud empleador</td><td class="v num">${num((x as any).porcSaludEmpleador)}</td></tr>

            <tr><td class="k">% pension empleado</td><td class="v num">${num((x as any).porcPensionEmpleado)}</td></tr>
            <tr><td class="k">% pension empleador</td><td class="v num">${num((x as any).porcPensionEmpleador)}</td></tr>

            <tr><td class="k">% caja compensacion</td><td class="v num">${num((x as any).porcCajaCompensacion)}</td></tr>
            <tr><td class="k">% sena</td><td class="v num">${num((x as any).porcSena)}</td></tr>
            <tr><td class="k">% icbf</td><td class="v num">${num((x as any).porcIcbf)}</td></tr>

            <tr><td class="k">% provision prima</td><td class="v num">${num((x as any).porProvisionPrima)}</td></tr>
            <tr><td class="k">% provision vacaciones</td><td class="v num">${num((x as any).porProvisionVacaciones)}</td></tr>
            <tr><td class="k">% provision cesantias</td><td class="v num">${num((x as any).porProvisionCesantias)}</td></tr>
            <tr><td class="k">% provision interes cesantias</td><td class="v num">${num((x as any).porProvisionInteresCesantias)}</td></tr>

            <tr><td class="k">tope ibc minimo smmlv</td><td class="v num">${num((x as any).topeIbcMinSmmlv)}</td></tr>
            <tr><td class="k">tope ibc maximo smmlv</td><td class="v num">${num((x as any).topeIbcMaxSmmlv)}</td></tr>

            <tr><td class="k">exonerado salud</td><td class="v bool">${bool((x as any).exoneradoSalud)}</td></tr>
            <tr><td class="k">exonerado parafiscales</td><td class="v bool">${bool((x as any).exoneradoParafiscales)}</td></tr>

            <tr><td class="k">activo</td><td class="v bool">${bool(x.activo)}</td></tr>
          </tbody>
        </table>
      </div>
    `;
  }
}
