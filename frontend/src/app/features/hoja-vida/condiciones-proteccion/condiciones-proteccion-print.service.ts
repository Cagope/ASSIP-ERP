import { Injectable } from '@angular/core';

import {
  CondicionProteccion
} from './condiciones-proteccion.api';

/**
 * 🖨️ Servicio de impresión — Condiciones de Protección
 * ------------------------------------------------------------
 * Genera un informe en formato Legal horizontal (Landscape)
 * con el formato estándar del ERP ASSIP.
 */
@Injectable({
  providedIn: 'root'
})
export class CondicionesProteccionPrintService {

  imprimir(
    registros: (
      CondicionProteccion & {
        documento?: string;
        nombrePersona?: string;
      }
    )[]
  ): void {

    if (!registros || registros.length === 0) {
      alert(
        '⚠️ No hay registros de Condiciones de Protección para imprimir.'
      );
      return;
    }

    const siNo = (
      valor?: boolean | null
    ) => valor ? 'Sí' : 'No';

    const filas = registros
      .map((r, i) => `
        <tr>
          <td style="text-align:center;">${i + 1}</td>
          <td>${r.documento ?? ''}</td>
          <td>${r.nombrePersona ?? ''}</td>
          <td>${siNo(r.administraRecursosPublicos)}</td>
          <td>${siNo(r.grupoProteccionEspecialConstitucional)}</td>
          <td>${siNo(r.personaMayor60Anos)}</td>
          <td>${siNo(r.discapacidadFisica)}</td>
          <td>${siNo(r.victimaConflictoArmado)}</td>
          <td>${siNo(r.pobrezaExtrema)}</td>
          <td>${siNo(r.poblacionIndigena)}</td>
          <td>${siNo(r.poblacionAfrodescendiente)}</td>
          <td>${siNo(r.poblacionLgbtiqMas)}</td>
          <td>${siNo(r.perteneceGrupoProteccionConstitucional)}</td>
        </tr>
      `)
      .join('');

    const total = registros.length;

    const html = `
      <html>

      <head>

        <meta charset="utf-8">

        <title>
          Listado de Condiciones de Protección
        </title>

        <style>

          @page {
            size: Legal landscape;
            margin: 18mm;
          }

          body {
            font-family: Arial, sans-serif;
            color: #222;
            margin: 0;
            font-size: 12px;
          }

          header {
            text-align: center;
            margin-bottom: 10px;
          }

          img.logo {
            height: 60px;
            display: block;
            margin: 0 auto 5px auto;
          }

          h1 {
            font-size: 18px;
            margin: 4px 0;
          }

          table {
            width: 100%;
            border-collapse: collapse;
            border-spacing: 0;
          }

          th,
          td {
            padding: 5px 6px;
            border-bottom: .5px solid #ccc;
            vertical-align: middle;
          }

          th {
            background: #f3f3f3;
            text-align: left;
          }

          tbody tr:nth-child(even) {
            background: #fafafa;
          }

          tfoot td {
            text-align: right;
            font-weight: bold;
            padding-top: 8px;
          }

          footer {
            text-align: center;
            font-size: 10px;
            color: #555;
            margin-top: 10px;
            border-top: 1px solid #ccc;
            padding-top: 5px;
          }

        </style>

      </head>

      <body>

        <header>

          <img
            src="${window.location.origin}/assets/LOGO_EMPRESA.png"
            class="logo"
            alt="Logo Empresa">

          <h1>
            LISTADO DE CONDICIONES DE PROTECCIÓN
          </h1>

          <p
            style="font-size:11px;margin:0;">

            ERP ASSIP Solidaria y Financiera —
            Módulo Hoja de Vida

          </p>

        </header>

        <table>

          <thead>

            <tr>
              <th>#</th>
              <th>Documento</th>
              <th>Nombre Persona</th>
              <th>Recursos Públicos</th>
              <th>Grupo Protección Especial</th>
              <th>Mayor 60 Años</th>
              <th>Discapacidad</th>
              <th>Víctima Conflicto</th>
              <th>Pobreza Extrema</th>
              <th>Indígena</th>
              <th>Afrodescendiente</th>
              <th>LGBTIQ+</th>
              <th>Protección Constitucional</th>
            </tr>

          </thead>

          <tbody>

            ${filas}

          </tbody>

          <tfoot>

            <tr>
              <td colspan="13">
                Total registros: ${total}
              </td>
            </tr>

          </tfoot>

        </table>

        <footer>

          Impreso el
          ${new Date().toLocaleString()}

        </footer>

      </body>

      </html>
    `;

    const ventana = window.open(
      '',
      '_blank',
      'width=1400,height=850'
    );

    if (!ventana) {
      alert(
        '⚠️ Bloqueador de ventanas emergentes activo.'
      );
      return;
    }

    ventana.document.open();
    ventana.document.write(html);
    ventana.document.close();

    ventana.onload = () => ventana.print();
  }

}
