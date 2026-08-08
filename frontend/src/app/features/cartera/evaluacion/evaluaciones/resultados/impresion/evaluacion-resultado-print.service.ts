import {
  Injectable
} from '@angular/core';

import {
  Observable
} from 'rxjs';

import {
  EvaluacionCartera,
  EvaluacionCreditoResultado,
  EvaluacionCriterioResultado,
  ACCION_EVALUACION_DESCRIPCION
} from '../../evaluacion-cartera.models';


// =========================================================
// FORMATO A IMPRIMIR
// =========================================================

export interface EvaluacionFormatoImpresion {

  credito:
    EvaluacionCreditoResultado;

  criterios:
    EvaluacionCriterioResultado[];
}


@Injectable({
  providedIn: 'root'
})
export class EvaluacionResultadoPrintService {

  // =========================================================
  // IMPRESIÓN INDIVIDUAL
  //
  // Utiliza exactamente el mismo motor del proceso masivo.
  // =========================================================

  imprimirIndividual(
    evaluacion:
      EvaluacionCartera,
    credito:
      EvaluacionCreditoResultado,
    criterios:
      EvaluacionCriterioResultado[]
  ): Observable<void> {

    return this.imprimirMasivo(
      evaluacion,
      [
        {
          credito,
          criterios:
            Array.isArray(criterios)
              ? criterios
              : []
        }
      ]
    );
  }


  // =========================================================
  // IMPRESIÓN MASIVA
  //
  // REGLA:
  // 1 crédito = 1 formato = 1 hoja.
  //
  // Puede utilizarse para:
  // R = Reclasificar
  // H = Habilitar
  // M = Mantener
  // =========================================================

  imprimirMasivo(
    evaluacion:
      EvaluacionCartera,
    formatos:
      EvaluacionFormatoImpresion[]
  ): Observable<void> {

    return new Observable<void>(
      subscriber => {

        try {

          if (!evaluacion) {

            throw new Error(
              'No existe información de la evaluación para imprimir.'
            );
          }


          const formatosValidos =
            Array.isArray(formatos)
              ? formatos.filter(
                  formato =>
                    formato
                    && formato.credito
                )
              : [];


          if (
            formatosValidos.length === 0
          ) {

            throw new Error(
              'No existen créditos para imprimir.'
            );
          }


          const html =
            this.construirDocumento(
              evaluacion,
              formatosValidos
            );


          this.imprimirEnIframe(
            html,

            () => {

              subscriber.next();

              subscriber.complete();
            },

            error => {

              subscriber.error(
                error
              );
            }
          );

        } catch (error) {

          subscriber.error(
            error
          );
        }


        return () => {
          // No requiere cancelación adicional.
        };
      }
    );
  }


  // =========================================================
  // CONSTRUIR DOCUMENTO COMPLETO
  // =========================================================

  private construirDocumento(
    evaluacion:
      EvaluacionCartera,
    formatos:
      EvaluacionFormatoImpresion[]
  ): string {

    const contenido =
      formatos
        .map(
          (
            formato,
            index
          ) => {

            const ultimo =
              index
              === formatos.length - 1;


            return this.construirFormato(
              evaluacion,
              formato.credito,
              formato.criterios,
              !ultimo
            );
          }
        )
        .join('');


    return `
      <!doctype html>

      <html lang="es">

        <head>

          <meta charset="utf-8">

          <meta
            name="viewport"
            content="width=device-width, initial-scale=1">

          <title>
            Evaluación de Cartera
          </title>

          <style>

            /* =================================================
               PÁGINA
               ================================================= */

            @page {
              size: A4 portrait;
              margin: 7mm;
            }


            * {
              box-sizing: border-box;
            }


            html,
            body {
              margin: 0;
              padding: 0;

              background: #ffffff;

              font-family:
                Arial,
                Helvetica,
                sans-serif;

              color: #000000;
            }


            body {
              width: 100%;
            }


            /* =================================================
               REGLA PRINCIPAL
               1 FORMATO = 1 HOJA
               ================================================= */

            .formato-evaluacion {
              display: block;

              width: 100%;

              margin: 0;
              padding: 0;

              background: #ffffff;

              font-size: 8px;

              break-inside: avoid;
              page-break-inside: avoid;
            }


            .formato-evaluacion--salto {
              break-after: page;
              page-break-after: always;
            }


            /* =================================================
               TABLAS
               ================================================= */

            table {
              width: 100%;

              margin: 0;

              border-collapse: collapse;

              table-layout: fixed;
            }


            td,
            th {
              border: 1px solid #000000;

              padding: 3px 4px;

              vertical-align: middle;
            }


            .fondo {
              background: #eeeeee;
            }


            .label {
              font-size: 7px;
              font-weight: 700;

              background: #eeeeee;
            }


            .valor {
              font-size: 7.5px;
            }


            .centro {
              text-align: center;
            }


            .derecha {
              text-align: right;
            }


            /* =================================================
               ENCABEZADO
               ================================================= */

            .logo-cell {
              width: 13%;

              height: 46px;

              padding: 3px;

              text-align: center;
              vertical-align: middle;
            }


            .logo-empresa {
              display: block;

              max-width: 95px;
              max-height: 42px;

              margin: 0 auto;

              object-fit: contain;
            }


            .titulo-cell {
              width: 49%;

              text-align: center;
            }


            .titulo-cell strong {
              font-size: 13px;
              font-weight: 700;
            }


            .encabezado-label {
              width: 18%;

              font-size: 7px;
              font-weight: 700;

              background: #eeeeee;
            }


            .encabezado-valor {
              width: 20%;

              text-align: center;

              font-size: 8px;
              font-weight: 700;
            }


            /* =================================================
               RESULTADO
               ================================================= */

            .calificacion-tabla {
              margin-top: -1px;
            }


            .calificacion {
              text-align: center;

              font-size: 11px;
              font-weight: 700;
            }


            .calificacion-destacada {
              font-size: 13px;
            }


            .puntaje-total {
              text-align: center;

              font-size: 10px;
              font-weight: 700;
            }


            .accion {
              text-align: center;

              font-size: 8px;
              font-weight: 700;

              text-transform: uppercase;
            }


            /* =================================================
               CRITERIOS
               ================================================= */

            .criterios {
              margin-top: 5px;
            }


            .criterios th {
              padding: 4px;

              font-size: 6.5px;
              font-weight: 700;

              text-align: center;

              background: #eeeeee;
            }


            .criterios td {
              padding: 3px 4px;

              font-size: 7px;
              line-height: 1.15;
            }


            .criterios tfoot td {
              padding: 4px;

              font-size: 7px;

              background: #eeeeee;
            }


            .criterio-numero {
              width: 5%;
            }


            .criterio-nombre {
              width: 30%;
            }


            .criterio-resultado {
              width: 55%;
            }


            .criterio-puntaje {
              width: 10%;
            }


            /* =================================================
               DECISIÓN
               ================================================= */

            .decision {
              margin-top: 5px;
            }


            .decision-label {
              width: 18%;
            }


            .decision-campo {
              width: 82%;

              height: 20px;
            }


            /* =================================================
               OBSERVACIONES
               ================================================= */

            .observaciones {
              margin-top: -1px;
            }


            .observaciones-label {
              padding: 4px;

              font-size: 7px;
              font-weight: 700;

              background: #eeeeee;
            }


            .observaciones-campo {
              height: 30px;

              padding: 5px;

              vertical-align: top;

              font-size: 6.5px;
              line-height: 1.3;
            }


            /* =================================================
               COMITÉ
               ================================================= */

            .comite {
              margin-top: 5px;
            }


            .comite-titulo {
              padding: 5px;

              text-align: center;

              font-size: 8px;
              font-weight: 700;

              background: #eeeeee;
            }


            .comite-label {
              width: 12%;

              font-size: 6.5px;
              font-weight: 700;
            }


            .comite-dato {
              font-size: 7px;
            }


            /* =================================================
               FIRMAS
               ================================================= */

            .firmas-espacio td {
              height: 70px;

              border-bottom: 0;
            }


            .firmas-linea td {
              padding:
                0 14px 3px;

              border-top: 0;
              border-bottom: 0;
            }


            .firmas-linea--segunda td {
              padding-top: 20px;
              padding-bottom: 6px;

              border-bottom: 1px solid #000000;
            }


            .firma-linea {
              width: 100%;
              height: 1px;

              border-top: 1px solid #000000;
            }


            /* =================================================
               IMPRESIÓN
               ================================================= */

            @media print {

              html,
              body {
                margin: 0 !important;
                padding: 0 !important;

                background: #ffffff !important;
              }


              .formato-evaluacion {
                break-inside: avoid;
                page-break-inside: avoid;
              }


              .formato-evaluacion--salto {
                break-after: page;
                page-break-after: always;
              }


              .fondo,
              .label,
              .encabezado-label,
              .criterios th,
              .criterios tfoot td,
              .observaciones-label,
              .comite-titulo {

                background: #eeeeee !important;

                -webkit-print-color-adjust: exact;
                print-color-adjust: exact;
              }

            }

          </style>

        </head>


        <body>

          ${contenido}

        </body>

      </html>
    `;
  }


  // =========================================================
  // CONSTRUIR UN FORMATO
  //
  // Este método se utiliza tanto para:
  // - individual;
  // - masivo.
  //
  // No se duplica diseño.
  // =========================================================

  private construirFormato(
    evaluacion:
      EvaluacionCartera,
    credito:
      EvaluacionCreditoResultado,
    criterios:
      EvaluacionCriterioResultado[],
    aplicarSaltoPagina:
      boolean
  ): string {

    const detalle =
      Array.isArray(criterios)
        ? criterios
        : [];


    const filasCriterios =
      detalle
        .slice()
        .sort(
          (
            a,
            b
          ) =>
            Number(
              a.ordenEvaluacion ?? 0
            )
            -
            Number(
              b.ordenEvaluacion ?? 0
            )
        )
        .map(
          criterio => `
            <tr>

              <td class="centro">
                ${
                  this.escaparHtml(
                    criterio.ordenEvaluacion
                  )
                }
              </td>


              <td>
                ${
                  this.escaparHtml(
                    criterio.nombreCriterio
                  )
                }
              </td>


              <td>
                ${
                  this.escaparHtml(
                    criterio.descripcionResultado
                  )
                }
              </td>


              <td class="derecha">
                ${
                  this.formatearNumero(
                    criterio.puntajeObtenido,
                    0,
                    2
                  )
                }
              </td>

            </tr>
          `
        )
        .join('');


    const descripcionAccion =
      credito.descripcionAccion
      || ACCION_EVALUACION_DESCRIPCION[
        credito.accionEvaluacion
      ]
      || credito.accionEvaluacion
      || '';


    const agencia =
      this.unirCodigoNombre(
        credito.codigoAgencia,
        credito.nombreAgencia
      );


    const linea =
      this.unirCodigoNombre(
        credito.codigoLineaCredito,
        credito.nombreLineaCredito
      );


    const claseFormato =
      aplicarSaltoPagina
        ? 'formato-evaluacion formato-evaluacion--salto'
        : 'formato-evaluacion';


    return `
      <article
        class="${claseFormato}">


        <!-- =============================================== -->
        <!-- ENCABEZADO -->
        <!-- =============================================== -->

        <table>

          <tbody>

            <tr>

              <td
                rowspan="2"
                class="logo-cell">

                <img
                  src="${window.location.origin}/assets/LOGO_EMPRESA.png"
                  class="logo-empresa"
                  alt="Logo empresa">

              </td>


              <td
                rowspan="2"
                class="titulo-cell">

                <strong>
                  EVALUACIÓN DE CARTERA
                </strong>

              </td>


              <td class="encabezado-label">
                FECHA DE CORTE
              </td>

              <td class="encabezado-valor">

                ${
                  this.formatearFecha(
                    evaluacion.fechaCorte
                  )
                }

              </td>

            </tr>


            <tr>

              <td class="encabezado-label">
                FECHA PROCESO
              </td>

              <td class="encabezado-valor">

                ${
                  this.formatearFecha(
                    evaluacion.fechaEjecucion
                  )
                }

              </td>

            </tr>

          </tbody>

        </table>


        <!-- =============================================== -->
        <!-- AGENCIA -->
        <!-- =============================================== -->

        <table>

          <tbody>

            <tr>

              <td class="label">
                AGENCIA
              </td>

              <td
                colspan="3"
                class="valor">

                ${
                  this.escaparHtml(
                    agencia
                  )
                }

              </td>

            </tr>

          </tbody>

        </table>


        <!-- =============================================== -->
        <!-- DOCUMENTO / NOMBRE -->
        <!-- =============================================== -->

        <table>

          <tbody>

            <tr>

              <td class="label">
                DOCUMENTO
              </td>

              <td class="valor">

                ${
                  this.escaparHtml(
                    credito.documento
                  )
                }

              </td>


              <td class="label">
                NOMBRE
              </td>

              <td class="valor">

                ${
                  this.escaparHtml(
                    credito.nombreCompleto
                  )
                }

              </td>

            </tr>

          </tbody>

        </table>


        <!-- =============================================== -->
        <!-- LÍNEA / PAGARÉ -->
        <!-- =============================================== -->

        <table>

          <tbody>

            <tr>

              <td class="label">
                LÍNEA
              </td>

              <td class="valor">

                ${
                  this.escaparHtml(
                    linea
                  )
                }

              </td>


              <td class="label">
                PAGARÉ
              </td>

              <td class="valor">

                ${
                  this.escaparHtml(
                    credito.pagareCartera
                  )
                }

              </td>

            </tr>

          </tbody>

        </table>


        <!-- =============================================== -->
        <!-- SALDOS -->
        <!-- =============================================== -->

        <table>

          <tbody>

            <tr>

              <td class="label">
                SALDO INICIAL
              </td>

              <td class="valor derecha">

                ${
                  this.formatearMoneda(
                    credito.valorInicialCredito
                  )
                }

              </td>


              <td class="label">
                SALDO ACTUAL
              </td>

              <td class="valor derecha">

                ${
                  this.formatearMoneda(
                    credito.saldoActual
                  )
                }

              </td>

            </tr>

          </tbody>

        </table>


        <!-- =============================================== -->
        <!-- RESULTADO -->
        <!-- =============================================== -->

        <table class="calificacion-tabla">

          <tbody>

            <tr>

              <td class="label">
                EDAD DE RIESGO ACTUAL
              </td>

              <td class="calificacion">

                ${
                  this.escaparHtml(
                    credito.edadRiesgoAnterior
                  )
                }

              </td>


              <td class="label">
                EDAD DE RIESGO SUGERIDA
              </td>

              <td class="calificacion calificacion-destacada">

                ${
                  this.escaparHtml(
                    credito.edadRiesgoFinal
                  )
                }

              </td>


              <td class="label">
                ARRASTRE
              </td>

              <td class="calificacion">

                ${
                  this.escaparHtml(
                    credito.edadRiesgoArrastre
                  )
                }

              </td>


              <td class="label">
                PUNTAJE
              </td>

              <td class="puntaje-total">

                ${
                  this.formatearNumero(
                    credito.puntajeTotal,
                    0,
                    2
                  )
                }

              </td>


              <td class="label">
                RECOMENDACIÓN
              </td>

              <td class="accion">

                ${
                  this.escaparHtml(
                    descripcionAccion
                  )
                }

              </td>

            </tr>

          </tbody>

        </table>


        <!-- =============================================== -->
        <!-- CRITERIOS -->
        <!-- =============================================== -->

        <table class="criterios">

          <thead>

            <tr>

              <th class="criterio-numero">
                #
              </th>

              <th class="criterio-nombre">
                VARIABLE EVALUADA
              </th>

              <th class="criterio-resultado">
                RESULTADO
              </th>

              <th class="criterio-puntaje">
                PUNTAJE
              </th>

            </tr>

          </thead>


          <tbody>

            ${filasCriterios}

          </tbody>


          <tfoot>

            <tr>

              <td colspan="3">

                <strong>
                  PUNTAJE TOTAL DEUDOR
                </strong>

              </td>


              <td class="derecha">

                <strong>

                  ${
                    this.formatearNumero(
                      credito.puntajeTotal,
                      0,
                      2
                    )
                  }

                </strong>

              </td>

            </tr>

          </tfoot>

        </table>


        <!-- =============================================== -->
        <!-- NUEVA CALIFICACIÓN -->
        <!-- =============================================== -->

        <table class="decision">

          <tbody>

            <tr>

              <td class="label decision-label">
                NUEVA CALIFICACIÓN
              </td>

              <td class="decision-campo">
                &nbsp;
              </td>

            </tr>

          </tbody>

        </table>


        <!-- =============================================== -->
        <!-- OBSERVACIONES -->
        <!-- =============================================== -->

        <table class="observaciones">

          <tbody>

            <tr>

              <td class="observaciones-label">
                OBSERVACIONES
              </td>

            </tr>


            <tr>

              <td class="observaciones-campo">

                ${
                  this.escaparHtml(
                    credito.comentarioEvaluacion
                  )
                }

              </td>

            </tr>

          </tbody>

        </table>


        <!-- =============================================== -->
        <!-- COMITÉ DE RIESGOS -->
        <!-- =============================================== -->

        <table class="comite">

          <tbody>

            <tr>

              <td
                colspan="6"
                class="comite-titulo">

                RESPONSABLES DEL COMITÉ DE RIESGOS

              </td>

            </tr>


            <tr>

              <td class="comite-label">
                Acta Número:
              </td>

              <td
                colspan="2"
                class="comite-dato">

                ${
                  this.escaparHtml(
                    evaluacion.numeroActaRiesgos
                  )
                }

              </td>


              <td class="comite-label">
                Fecha:
              </td>

              <td
                colspan="2"
                class="comite-dato">

                ${
                  this.formatearFecha(
                    evaluacion.fechaComiteRiesgos
                  )
                }

              </td>

            </tr>


            <!-- ESPACIO DE FIRMA -->

            <tr class="firmas-espacio">

              <td colspan="2">
                &nbsp;
              </td>

              <td colspan="2">
                &nbsp;
              </td>

              <td colspan="2">
                &nbsp;
              </td>

            </tr>


            <!-- PRIMERA FILA -->

            <tr class="firmas-linea">

              <td colspan="2">
                <div class="firma-linea"></div>
              </td>

              <td colspan="2">
                <div class="firma-linea"></div>
              </td>

              <td colspan="2">
                <div class="firma-linea"></div>
              </td>

            </tr>


            <!-- SEGUNDA FILA -->

            <tr
              class="
                firmas-linea
                firmas-linea--segunda
              ">

              <td colspan="2">
                <div class="firma-linea"></div>
              </td>

              <td colspan="2">
                <div class="firma-linea"></div>
              </td>

              <td colspan="2">
                <div class="firma-linea"></div>
              </td>

            </tr>

          </tbody>

        </table>

      </article>
    `;
  }


  // =========================================================
  // IMPRIMIR EN IFRAME OCULTO
  //
  // No abre about:blank.
  // Espera la carga del logo antes de imprimir.
  // =========================================================

  private imprimirEnIframe(
    html:
      string,
    alFinalizar?:
      () => void,
    alError?:
      (error: unknown) => void
  ): void {

    let iframe:
      HTMLIFrameElement | null = null;


    try {

      iframe =
        document.createElement(
          'iframe'
        );


      iframe.style.position =
        'fixed';

      iframe.style.right =
        '0';

      iframe.style.bottom =
        '0';

      iframe.style.width =
        '0';

      iframe.style.height =
        '0';

      iframe.style.border =
        '0';

      iframe.style.visibility =
        'hidden';


      iframe.setAttribute(
        'aria-hidden',
        'true'
      );


      document.body.appendChild(
        iframe
      );


      const ventana =
        iframe.contentWindow;

      const documento =
        iframe.contentDocument
        ?? ventana?.document;


      if (
        !ventana
        || !documento
      ) {

        throw new Error(
          'No fue posible crear el documento de impresión.'
        );
      }


      documento.open();

      documento.write(
        html
      );

      documento.close();


      let finalizado = false;


      const limpiar = () => {

        setTimeout(
          () => {

            if (
              iframe
              && iframe.parentNode
            ) {

              iframe.parentNode.removeChild(
                iframe
              );
            }

          },
          300
        );
      };


      const finalizar = () => {

        if (finalizado) {
          return;
        }


        finalizado = true;

        limpiar();

        alFinalizar?.();
      };


      const imprimirAhora = () => {

        try {

          ventana.focus();


          ventana.onafterprint =
            finalizar;


          setTimeout(
            () => {

              ventana.print();

            },
            100
          );

        } catch (error) {

          limpiar();

          alError?.(
            error
          );
        }
      };


      const esperarImagenes = () => {

        const imagenes =
          Array.from(
            documento.images
          );


        if (
          imagenes.length === 0
        ) {

          imprimirAhora();

          return;
        }


        const pendientes =
          imagenes.filter(
            imagen =>
              !imagen.complete
          );


        if (
          pendientes.length === 0
        ) {

          imprimirAhora();

          return;
        }


        let terminadas = 0;


        const imagenTerminada = () => {

          terminadas++;


          if (
            terminadas >=
            pendientes.length
          ) {

            imprimirAhora();
          }
        };


        pendientes.forEach(
          imagen => {

            imagen.addEventListener(
              'load',
              imagenTerminada,
              {
                once: true
              }
            );


            imagen.addEventListener(
              'error',
              imagenTerminada,
              {
                once: true
              }
            );
          }
        );


        /*
         * Respaldo:
         * si un recurso no reporta load/error,
         * no dejamos bloqueada la impresión.
         */

        setTimeout(
          () => {

            if (
              terminadas
              < pendientes.length
            ) {

              imprimirAhora();
            }

          },
          2000
        );
      };


      const prepararImpresion = () => {

        setTimeout(
          esperarImagenes,
          50
        );
      };


      if (
        documento.readyState ===
        'complete'
      ) {

        prepararImpresion();

      } else {

        iframe.onload =
          prepararImpresion;
      }

    } catch (error) {

      if (
        iframe
        && iframe.parentNode
      ) {

        iframe.parentNode.removeChild(
          iframe
        );
      }


      alError?.(
        error
      );
    }
  }


  // =========================================================
  // CÓDIGO + NOMBRE
  // =========================================================

  private unirCodigoNombre(
    codigo:
      string
      | null
      | undefined,
    nombre:
      string
      | null
      | undefined
  ): string {

    const codigoLimpio =
      String(
        codigo ?? ''
      ).trim();


    const nombreLimpio =
      String(
        nombre ?? ''
      ).trim();


    if (
      codigoLimpio
      && nombreLimpio
    ) {

      return (
        `${codigoLimpio} - ${nombreLimpio}`
      );
    }


    return (
      codigoLimpio
      || nombreLimpio
      || ''
    );
  }


  // =========================================================
  // FECHA
  // =========================================================

  private formatearFecha(
    valor:
      string
      | null
      | undefined
  ): string {

    if (!valor) {
      return '';
    }


    const fecha =
      String(valor)
        .substring(
          0,
          10
        );


    const partes =
      fecha.split('-');


    if (
      partes.length !== 3
    ) {

      return this.escaparHtml(
        valor
      );
    }


    return (
      `${partes[2]}/${partes[1]}/${partes[0]}`
    );
  }


  // =========================================================
  // MONEDA
  // =========================================================

  private formatearMoneda(
    valor:
      number
      | null
      | undefined
  ): string {

    const numero =
      Number(
        valor ?? 0
      );


    if (
      !Number.isFinite(numero)
    ) {

      return '$0';
    }


    return new Intl.NumberFormat(
      'es-CO',
      {
        style:
          'currency',

        currency:
          'COP',

        minimumFractionDigits:
          0,

        maximumFractionDigits:
          0
      }
    ).format(
      numero
    );
  }


  // =========================================================
  // NÚMERO
  // =========================================================

  private formatearNumero(
    valor:
      number
      | null
      | undefined,
    minimoDecimales = 0,
    maximoDecimales = 2
  ): string {

    const numero =
      Number(
        valor ?? 0
      );


    if (
      !Number.isFinite(numero)
    ) {

      return '0';
    }


    return new Intl.NumberFormat(
      'es-CO',
      {
        minimumFractionDigits:
          minimoDecimales,

        maximumFractionDigits:
          maximoDecimales
      }
    ).format(
      numero
    );
  }


  // =========================================================
  // ESCAPAR HTML
  // =========================================================

  private escaparHtml(
    valor:
      string
      | number
      | null
      | undefined
  ): string {

    return String(
      valor ?? ''
    )
      .replace(
        /&/g,
        '&amp;'
      )
      .replace(
        /</g,
        '&lt;'
      )
      .replace(
        />/g,
        '&gt;'
      )
      .replace(
        /"/g,
        '&quot;'
      )
      .replace(
        /'/g,
        '&#039;'
      );
  }
}
