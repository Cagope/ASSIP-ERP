import { Injectable } from '@angular/core';

import {
  Observable,
  forkJoin,
  map,
  switchMap
} from 'rxjs';

import { OriginacionAprobacionApi } from '../aprobacion/originacion-aprobacion.api';

import { OriginacionSolicitudApi } from '../solicitud/originacion-solicitud.api';

import { OriginacionContextoApi } from '../contexto/originacion-contexto.api';

import { OriginacionCompromisosPrintDocument } from './originacion-compromisos-print.document';

import { ORIGINACION_COMPROMISOS_PRINT_STYLES } from './originacion-compromisos-print.styles';

import type {
  OriginacionCompromisosPrintData
} from './originacion-compromisos-print.models';


@Injectable({
  providedIn: 'root'
})
export class OriginacionCompromisosPrintService {

  private readonly documento =
    new OriginacionCompromisosPrintDocument();


  constructor(
    private readonly aprobacionApi: OriginacionAprobacionApi,
    private readonly solicitudApi: OriginacionSolicitudApi,
    private readonly contextoApi: OriginacionContextoApi
  ) {}


  imprimirSolicitud(
    idSolicitudCredito: number
  ): Observable<void> {

    return new Observable<void>(subscriber => {

      if (
        !Number.isInteger(idSolicitudCredito) ||
        idSolicitudCredito <= 0
      ) {

        subscriber.error(
          new Error(
            'Seleccione una solicitud de crédito válida.'
          )
        );

        return;
      }


      // =====================================================
      // ABRIR VENTANA SINCRÓNICAMENTE
      // =====================================================

      const ventana =
        window.open('', '_blank');

      if (!ventana) {

        subscriber.error(
          new Error(
            'Permita ventanas emergentes para ASSIP ERP.'
          )
        );

        return;
      }

      ventana.document.open();

      ventana.document.write(
        '<!doctype html>' +
        '<html lang="es">' +
        '<meta charset="utf-8">' +
        '<title>Preparando compromisos...</title>' +
        '<body>Preparando documento...</body>' +
        '</html>'
      );

      ventana.document.close();


      // =====================================================
      // CONSULTAR SOLICITUD Y FOTOGRAFÍAS
      // =====================================================

      const consulta = forkJoin({

        solicitud:
          this.solicitudApi.buscarSolicitudPorId(
            idSolicitudCredito
          ),

        fotos:
          this.aprobacionApi.obtenerFotos(
            idSolicitudCredito
          )

      }).pipe(

        // ===================================================
        // CONSULTAR CONTEXTO DEL DEUDOR PRINCIPAL
        // ===================================================

        switchMap(({ solicitud, fotos }) => {

          if (!fotos) {

            throw new Error(
              'No se encontraron fotografías de la solicitud.'
            );

          }

          const idDatosPersonal =
            solicitud.idDatosPersonal;

          const idAgencia =
            solicitud.idAgencia;


          if (
            !Number.isInteger(idDatosPersonal) ||
            idDatosPersonal <= 0
          ) {

            throw new Error(
              'La solicitud no tiene un deudor principal válido.'
            );

          }


          if (
            !Number.isInteger(idAgencia) ||
            idAgencia <= 0
          ) {

            throw new Error(
              'La solicitud no tiene una agencia válida.'
            );

          }


          return this.contextoApi.consultar(
            idDatosPersonal,
            idAgencia
          ).pipe(

            map(contexto => ({

              solicitud,

              fotos,

              carteraActual:
                contexto.carteraActual ?? [],

              vectorResumen:
                contexto.vectorResumen ?? []

            }))

          );

        }),


        // ===================================================
        // CONSTRUIR DOCUMENTO
        // ===================================================

        map(({
          solicitud,
          fotos,
          carteraActual,
          vectorResumen
        }) => {

          const fecha =
            new Intl.DateTimeFormat(
              'es-CO',
              {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric'
              }
            ).format(
              new Date()
            );


          const data: OriginacionCompromisosPrintData = {

            idSolicitudCredito,

            numeroSolicitud:
              solicitud.numeroSolicitud ?? null,

            fechaImpresion:
              fecha,

            fotos,

            carteraActual,

            vectorResumen

          };


          return this.documento.construir(
            data
          );

        })

      ).subscribe({

        // ===================================================
        // DOCUMENTO GENERADO
        // ===================================================

        next: contenido => {

          try {

            if (ventana.closed) {

              throw new Error(
                'La ventana de impresión fue cerrada.'
              );

            }


            const html = `
              <!doctype html>
              <html lang="es">

              <head>

                <meta charset="utf-8">

                <meta
                  name="viewport"
                  content="width=device-width, initial-scale=1"
                >

                <title>
                  COOPVALLE - Compromisos y autorizaciones
                </title>

                <style>
                  ${ORIGINACION_COMPROMISOS_PRINT_STYLES}
                </style>

              </head>

              <body>

                <div class="acciones">

                  <button
                    type="button"
                    onclick="window.print()"
                  >
                    Imprimir / Guardar PDF
                  </button>

                </div>

                ${contenido}

              </body>

              </html>
            `;


            ventana.document.open();

            ventana.document.write(
              html
            );

            ventana.document.close();


            subscriber.next();

            subscriber.complete();

          } catch (error) {

            this.errorEnVentana(
              ventana,
              error
            );

            subscriber.error(
              error
            );

          }

        },


        // ===================================================
        // ERROR DE CONSULTA
        // ===================================================

        error: error => {

          this.errorEnVentana(
            ventana,
            error
          );

          subscriber.error(
            error
          );

        }

      });


      return () => consulta.unsubscribe();

    });

  }


  // =========================================================
  // ERROR EN VENTANA
  // =========================================================

  private errorEnVentana(
    ventana: Window,
    error: unknown
  ): void {

    if (ventana.closed) {
      return;
    }

    const mensaje =
      error instanceof Error
        ? error.message
        : 'No se pudo generar el documento.';


    ventana.document.open();

    ventana.document.write(
      '<!doctype html>' +
      '<html lang="es">' +
      '<meta charset="utf-8">' +
      '<body>' +
      '<h3>No se pudo generar el documento</h3>' +
      '<p></p>' +
      '</body>' +
      '</html>'
    );

    ventana.document.close();


    const parrafo =
      ventana.document.querySelector('p');


    if (parrafo) {

      parrafo.textContent =
        mensaje;

    }

  }

}
