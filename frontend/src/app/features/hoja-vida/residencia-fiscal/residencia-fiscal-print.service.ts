import {
  Injectable
} from '@angular/core';

import {
  ResidenciaFiscal
} from './residencia-fiscal.api';

/**
 * Servicio de impresión — Residencia Fiscal FATCA / CRS
 * ------------------------------------------------------
 * Genera un informe en formato Legal horizontal
 * con el diseño general del ERP ASSIP.
 */
@Injectable({
  providedIn: 'root'
})
export class ResidenciaFiscalPrintService {

  imprimir(
    registros: (
      ResidenciaFiscal & {
        documento?: string;
        nombrePersona?: string;
      }
    )[]
  ): void {

    if (
      !registros
      || registros.length === 0
    ) {
      alert(
        'No hay registros de residencia fiscal para imprimir.'
      );

      return;
    }

    const filas = registros
      .map(
        (registro, indice) => `
          <tr>
            <td class="centrado">
              ${indice + 1}
            </td>

            <td>
              ${this.escaparHtml(
                registro.documento
              )}
            </td>

            <td>
              ${this.escaparHtml(
                registro.nombrePersona
              )}
            </td>

            <td class="centrado">
              ${
                registro.ciudadanoEstadosUnidos
                  ? 'Sí'
                  : 'No'
              }
            </td>

            <td class="centrado">
              ${
                registro.residenteFiscalEstadosUnidos
                  ? 'Sí'
                  : 'No'
              }
            </td>

            <td class="centrado">
              ${
                registro.residenteFiscalExterior
                  ? 'Sí'
                  : 'No'
              }
            </td>

            <td>
              ${this.escaparHtml(
                registro.paisResidenciaFiscal
              )}
            </td>

            <td>
              ${this.escaparHtml(
                registro.tipoIdentificacionFiscal
              )}
            </td>

            <td>
              ${this.escaparHtml(
                registro.numeroIdentificacionFiscal
              )}
            </td>

            <td>
              ${this.escaparHtml(
                registro.ciudadResidenciaFiscal
              )}
            </td>

            <td>
              ${this.escaparHtml(
                registro.direccionResidenciaFiscal
              )}
            </td>

            <td>
              ${this.escaparHtml(
                registro.observaciones
              )}
            </td>
          </tr>
        `
      )
      .join('');

    const total = registros.length;

    const fechaImpresion =
      new Date().toLocaleString(
        'es-CO'
      );

    const html = `
      <!DOCTYPE html>

      <html lang="es">

        <head>

          <meta charset="utf-8">

          <title>
            Listado de Residencia Fiscal
          </title>

          <style>

            @page {
              size: Legal landscape;
              margin: 14mm;
            }

            * {
              box-sizing: border-box;
            }

            body {
              margin: 0;
              font-family: Arial, sans-serif;
              color: #222;
              font-size: 10px;
            }

            header {
              text-align: center;
              margin-bottom: 10px;
            }

            img.logo {
              display: block;
              height: 58px;
              margin: 0 auto 5px auto;
            }

            h1 {
              margin: 4px 0;
              font-size: 17px;
            }

            .subtitulo {
              margin: 0;
              font-size: 10px;
            }

            table {
              width: 100%;
              border-collapse: collapse;
              table-layout: auto;
            }

            th,
            td {
              padding: 4px 5px;
              border: 0.5px solid #d1d5db;
              vertical-align: middle;
              overflow-wrap: anywhere;
            }

            th {
              background-color: #f3f4f6;
              text-align: left;
              font-size: 9px;
            }

            tbody tr:nth-child(even) {
              background-color: #fafafa;
            }

            .centrado {
              text-align: center;
            }

            .col-documento {
              min-width: 90px;
            }

            .col-nombre {
              min-width: 160px;
            }

            .col-observaciones {
              min-width: 160px;
            }

            tfoot td {
              text-align: right;
              font-weight: bold;
              padding-top: 7px;
            }

            footer {
              margin-top: 10px;
              padding-top: 5px;
              border-top: 1px solid #ccc;
              text-align: center;
              font-size: 9px;
              color: #555;
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
              LISTADO DE RESIDENCIA FISCAL — FATCA / CRS
            </h1>

            <p class="subtitulo">
              ERP ASSIP Solidaria y Financiera —
              Módulo Hoja de Vida
            </p>

          </header>

          <table>

            <thead>

              <tr>

                <th>#</th>

                <th class="col-documento">
                  Documento
                </th>

                <th class="col-nombre">
                  Nombre de la persona
                </th>

                <th>
                  Ciudadano EE.UU.
                </th>

                <th>
                  Residente fiscal EE.UU.
                </th>

                <th>
                  Residente fiscal exterior
                </th>

                <th>
                  País
                </th>

                <th>
                  Tipo identificación fiscal
                </th>

                <th>
                  Número identificación fiscal
                </th>

                <th>
                  Ciudad
                </th>

                <th>
                  Dirección
                </th>

                <th class="col-observaciones">
                  Observaciones
                </th>

              </tr>

            </thead>

            <tbody>
              ${filas}
            </tbody>

            <tfoot>

              <tr>

                <td colspan="12">
                  Total registros: ${total}
                </td>

              </tr>

            </tfoot>

          </table>

          <footer>
            Impreso el ${fechaImpresion}
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
        'El bloqueador de ventanas emergentes está activo.'
      );

      return;
    }

    ventana.document.open();
    ventana.document.write(html);
    ventana.document.close();

    ventana.onload = () => {
      ventana.focus();
      ventana.print();
    };
  }

  /**
   * Evita que valores capturados por el usuario
   * sean interpretados como etiquetas HTML.
   */
  private escaparHtml(
    valor:
      string
      | number
      | null
      | undefined
  ): string {

    return String(valor ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }
}
