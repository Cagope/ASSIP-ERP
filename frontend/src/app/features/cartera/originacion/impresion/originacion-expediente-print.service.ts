import { OriginacionReferenciasApi } from '../referencias/originacion-referencias.api';

import {
  Injectable
} from '@angular/core';

import {
  Observable,
  forkJoin,
  of,
  map,
  switchMap
} from 'rxjs';

import {
  OriginacionAprobacionApi
} from '../aprobacion/originacion-aprobacion.api';

import {
  OriginacionSolicitudApi
} from '../solicitud/originacion-solicitud.api';

import {
  SolicitudAprobacionActuacion
} from '../aprobacion/originacion-aprobacion.models';

import {
  OriginacionExpedientePrintData,
  OriginacionExpedientePrintOpciones,
  OriginacionExpedientePersona,
  ORIGINACION_EXPEDIENTE_PRINT_OPCIONES_DEFAULT
} from './originacion-expediente-print.models';

import {
  ORIGINACION_EXPEDIENTE_PRINT_STYLES
} from './originacion-expediente-print.styles';

import {
  OriginacionExpedientePrintDocument
} from './originacion-expediente-print.document';

import {
  ExpedienteAsociadoApi
} from '../../../gerencia/expediente-asociado/expediente-asociado.api';

import type {
  ExpedienteAsociado
} from '../../../gerencia/expediente-asociado/expediente-asociado.dto';


// =========================================================
// ASSIP ERP
// ORIGINACIÓN DE CARTERA
// SERVICIO DE IMPRESIÓN DEL EXPEDIENTE INTEGRAL
// =========================================================

@Injectable({
  providedIn: 'root'
})
export class OriginacionExpedientePrintService {

  // =========================================================
  // GENERADOR DEL DOCUMENTO
  // =========================================================

  private readonly documento =
    new OriginacionExpedientePrintDocument();


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(

    private readonly aprobacionApi:
      OriginacionAprobacionApi,

    private readonly solicitudApi:
      OriginacionSolicitudApi,

    private readonly expedienteAsociadoApi:
      ExpedienteAsociadoApi,

    private readonly referenciasApi:
      OriginacionReferenciasApi

  ) {}


  // =========================================================
  // IMPRIMIR EXPEDIENTE DE LA SOLICITUD
  //
  // Utiliza las fotografías actuales de originación.
  //
  // No requiere que la solicitud esté aprobada.
  // =========================================================

  imprimirSolicitud(

    idSolicitudCredito: number,

    opciones?:
      Partial<OriginacionExpedientePrintOpciones>

  ): Observable<void> {

    return this.imprimir(

      idSolicitudCredito,

      null,

      opciones

    );
  }


  // =========================================================
  // IMPRIMIR EXPEDIENTE DE UNA ACTUACIÓN
  //
  // Utiliza las fotografías históricas conservadas
  // en la actuación seleccionada.
  // =========================================================

  imprimirActuacion(

    idSolicitudCredito: number,

    idSolicitudAprobacion: number,

    opciones?:
      Partial<OriginacionExpedientePrintOpciones>

  ): Observable<void> {

    return this.imprimir(

      idSolicitudCredito,

      idSolicitudAprobacion,

      opciones

    );
  }


  // =========================================================
  // PROCESO GENERAL DE IMPRESIÓN
  // =========================================================

  private imprimir(

    idSolicitudCredito: number,

    idSolicitudAprobacion: number | null,

    opciones?:
      Partial<OriginacionExpedientePrintOpciones>

  ): Observable<void> {

    return new Observable<void>(

      subscriber => {

        // ---------------------------------------------------
        // VALIDAR IDENTIFICADORES
        // ---------------------------------------------------

        if (

          !Number.isInteger(idSolicitudCredito)

          || idSolicitudCredito <= 0

        ) {

          subscriber.error(

            new Error(
              'Debe seleccionar una solicitud válida para imprimir.'
            )

          );

          return;

        }


        if (

          idSolicitudAprobacion !== null

          && (

            !Number.isInteger(idSolicitudAprobacion)

            || idSolicitudAprobacion <= 0

          )

        ) {

          subscriber.error(

            new Error(
              'La actuación de aprobación no es válida.'
            )

          );

          return;

        }


        // ---------------------------------------------------
        // ABRIR VENTANA DESDE LA ACCIÓN DEL USUARIO
        //
        // Se abre antes de las peticiones HTTP para evitar
        // que el navegador bloquee la ventana emergente.
        // ---------------------------------------------------

        const ventana =
          window.open(

            '',

            '_blank'

          );


        if (!ventana) {

          subscriber.error(

            new Error(
              'El navegador bloqueó la ventana de impresión. ' +
              'Permita las ventanas emergentes para ASSIP ERP.'
            )

          );

          return;

        }


        // ---------------------------------------------------
        // MOSTRAR ESTADO DE CARGA
        // ---------------------------------------------------

        this.mostrarCargando(
          ventana
        );


        // ---------------------------------------------------
        // OBTENER LAS FOTOGRAFÍAS
        // ---------------------------------------------------

        const fotos$ =

          idSolicitudAprobacion === null

            ? this.aprobacionApi.obtenerFotos(
                idSolicitudCredito
              )

            : this.aprobacionApi.obtenerFotosActuacion(

                idSolicitudCredito,

                idSolicitudAprobacion

              );


        // ---------------------------------------------------
        // RECUPERAR DATOS DE LA SOLICITUD,
        // FOTOGRAFÍAS E HISTORIAL
        // ---------------------------------------------------

        const consulta =

          forkJoin({

            solicitud:
              this.solicitudApi.buscarSolicitudPorId(
                idSolicitudCredito
              ),

            fotos:
              fotos$,

            actuaciones:
              this.aprobacionApi.listarHistorial(
                idSolicitudCredito
              ),

            // Las referencias pertenecen a la gestión actual.
            // No se incorporan a impresiones de actuaciones históricas.
            referencias: idSolicitudAprobacion === null
              ? this.referenciasApi.listarReferencias(idSolicitudCredito)
              : of([]),

            procesoReferencias: idSolicitudAprobacion === null
              ? this.referenciasApi.consultarSolicitud(idSolicitudCredito)
              : of(null)

          })
          .pipe(
            switchMap(resultado => {

              // Consultar una sola vez cada persona de la fotografía
              // seleccionada (solicitud actual o actuación histórica).
              const ids = [
                ...new Set(
                  (resultado.fotos?.fotoDeudores || [])
                    .map(deudor => Number(deudor['id_datos_personal']))
                    .filter(id => Number.isInteger(id) && id > 0)
                )
              ];

              if (!ids.length) {
                return of({ ...resultado, personas: [] as OriginacionExpedientePersona[] });
              }

              return forkJoin(
                ids.map(id =>
                  this.expedienteAsociadoApi.consultarPorIdDatosPersonal(id)
                    .pipe(map(expediente => this.mapearPersona(id, expediente)))
                )
              ).pipe(
                map(personas => ({
                  ...resultado,
                  personas: personas.filter(
                    (persona): persona is OriginacionExpedientePersona => persona !== null
                  )
                }))
              );

            })
          )
          .subscribe({

            next: resultado => {

              try {

                // -------------------------------------------
                // VALIDAR FOTOGRAFÍAS
                // -------------------------------------------

                if (!resultado.fotos) {

                  throw new Error(

                    'La solicitud no tiene información ' +
                    'disponible para generar el expediente.'

                  );

                }


                // -------------------------------------------
                // OPCIONES DEL DOCUMENTO
                // -------------------------------------------

                const opcionesFinales:
                  OriginacionExpedientePrintOpciones = {

                    ...ORIGINACION_EXPEDIENTE_PRINT_OPCIONES_DEFAULT,

                    ...opciones

                  };


                // -------------------------------------------
                // HISTORIAL CORRESPONDIENTE
                // -------------------------------------------

                const actuaciones =

                  this.obtenerActuacionesImprimibles(

                    resultado.actuaciones,

                    idSolicitudAprobacion

                  );


                // -------------------------------------------
                // HOJA DE VIDA DE LOS PARTICIPANTES
                // -------------------------------------------

                const personas = resultado.personas;


                // -------------------------------------------
                // FECHA LOCAL DE IMPRESIÓN
                // -------------------------------------------

                const fechaImpresion =
                  this.fechaLocalActual();


                // -------------------------------------------
                // DATOS PARA EL DOCUMENTO
                // -------------------------------------------

                const data:
                  OriginacionExpedientePrintData = {

                    identificacion: {

                      idSolicitudCredito:

                        resultado.solicitud.idSolicitudCredito,

                      numeroSolicitud:

                        resultado.solicitud.numeroSolicitud,

                      idSolicitudAprobacion,

                      fechaImpresion

                    },

                    modo:

                      idSolicitudAprobacion === null

                        ? 'SOLICITUD'

                        : 'ACTUACION',

                    fotos:

                      resultado.fotos,

                    actuaciones,

                    personas,

                    referencias: resultado.referencias,

                    procesoReferencias: resultado.procesoReferencias,

                    opciones:

                      opcionesFinales

                  };


                // -------------------------------------------
                // CONSTRUIR CONTENIDO
                // -------------------------------------------

                const contenido =

                  this.documento.construir(
                    data
                  );


                // -------------------------------------------
                // CONSTRUIR HTML COMPLETO
                // -------------------------------------------

                const html =

                  this.construirHtml(

                    contenido,

                    resultado.solicitud.numeroSolicitud

                  );


                // -------------------------------------------
                // PRESENTAR DOCUMENTO
                // -------------------------------------------

                this.mostrarDocumento(

                  ventana,

                  html

                );


                // -------------------------------------------
                // FINALIZAR
                // -------------------------------------------

                subscriber.next();

                subscriber.complete();

              } catch (error) {

                this.mostrarError(

                  ventana,

                  error

                );

                subscriber.error(
                  error
                );

              }

            },


            error: error => {

              this.mostrarError(

                ventana,

                error

              );

              subscriber.error(
                error
              );

            }

          });


        // ---------------------------------------------------
        // CANCELACIÓN
        // ---------------------------------------------------

        return () => {

          consulta.unsubscribe();

        };

      }

    );

  }


  // =========================================================
  // ADAPTAR EXPEDIENTE EXISTENTE A LOS DATOS DE IMPRESIÓN
  // =========================================================

  private mapearPersona(
    idDatosPersonal: number,
    expediente: ExpedienteAsociado
  ): OriginacionExpedientePersona | null {

    if (!expediente.encontrado) {
      return null;
    }

    const resumen = expediente.resumenGeneral;
    const contacto = expediente.contacto;

    return {

      // -----------------------------------------------------
      // IDENTIFICACIÓN
      // -----------------------------------------------------

      idDatosPersonal,

      tipoDocumento:
        resumen?.tipoDocumento
        ?? expediente.tipoDocumento
        ?? null,

      documento:
        resumen?.documento
        ?? expediente.documento
        ?? null,

      primerApellido:
        resumen?.primerApellido ?? null,

      segundoApellido:
        resumen?.segundoApellido ?? null,

      nombres:
        resumen?.nombres ?? null,

      nombreCompleto:
        resumen?.nombreCompleto
        ?? expediente.nombreCompleto
        ?? null,


      // -----------------------------------------------------
      // VINCULACIÓN
      // -----------------------------------------------------

      fechaAfiliacion:
        resumen?.fechaAfiliacion ?? null,

      cuentaAsociado: null,


      // -----------------------------------------------------
      // INFORMACIÓN PERSONAL
      // -----------------------------------------------------

      fechaNacimiento:
        resumen?.fechaNacimiento ?? null,

      estadoCivil:
        resumen?.estadoCivil ?? null,

      escolaridad:
        resumen?.nombreEscolaridad ?? null,

      numeroHijos:
        resumen?.numeroHijos ?? null,

      personasACargo: null,

      nombreConyuge: null,


      // -----------------------------------------------------
      // CONTACTO
      // -----------------------------------------------------

      direccion:
        contacto?.direccionPrincipal
        ?? resumen?.direccion
        ?? null,

      telefono:
        contacto?.telefonoResidencia
        ?? resumen?.telefono
        ?? null,

      celular:
        contacto?.celularPrincipal
        ?? resumen?.celular
        ?? null,

      correoElectronico:
        contacto?.correoPrincipal
        ?? resumen?.correoElectronico
        ?? null,

      ciudad:
        contacto?.nombreCiudad
        ?? resumen?.ciudad
        ?? null,

      departamento:
        contacto?.nombreDepartamento
        ?? resumen?.departamento
        ?? null,


      // -----------------------------------------------------
      // ACTIVIDAD ECONÓMICA
      // -----------------------------------------------------

      actividadEconomica:
        resumen?.nombreActividadDian
        ?? resumen?.nombreActividadSes
        ?? null,

      sectorEconomico:
        resumen?.nombreSectorEconomico ?? null,

      ocupacion:
        resumen?.ocupacion ?? null,

      profesion: null,

      // -----------------------------------------------------
      // INFORMACIÓN LABORAL
      // -----------------------------------------------------

      empresa:
        contacto?.empresa
        ?? resumen?.empresa
        ?? null,

      cargo:
        contacto?.cargo ?? null,

      direccionLaboral:
        contacto?.direccionEmpresa ?? null,

      telefonoLaboral:
        contacto?.telefonoEmpresa
        ?? contacto?.telefonoTrabajo
        ?? null,

      celularLaboral: null,

      fechaVinculacionLaboral: null

    };
  }

  // =========================================================
  // HISTORIAL PARA IMPRESIÓN
  //
  // SOLICITUD:
  //   Se presenta el historial disponible.
  //
  // ACTUACIÓN:
  //   No se incorporan decisiones posteriores a la
  //   actuación histórica seleccionada.
  // =========================================================

  private obtenerActuacionesImprimibles(

    actuaciones:
      SolicitudAprobacionActuacion[],

    idSolicitudAprobacion:
      number | null

  ): SolicitudAprobacionActuacion[] {

    const historial =

      Array.isArray(actuaciones)

        ? [...actuaciones]

        : [];


    if (idSolicitudAprobacion === null) {

      return historial;

    }


    const seleccionada =

      historial.find(

        actuacion =>

          actuacion.idSolicitudAprobacion

          === idSolicitudAprobacion

      );


    if (!seleccionada) {

      throw new Error(

        'La actuación seleccionada no pertenece ' +
        'al historial de la solicitud.'

      );

    }


    const fechaSeleccionada =

      seleccionada.fechaDecision;


    return historial.filter(

      actuacion => {

        if (

          actuacion.idSolicitudAprobacion

          === idSolicitudAprobacion

        ) {

          return true;

        }


        if (

          !actuacion.fechaDecision

          || !fechaSeleccionada

        ) {

          return false;

        }


        return (

          actuacion.fechaDecision

          < fechaSeleccionada

        );

      }

    );

  }


  // =========================================================
  // CONSTRUIR DOCUMENTO HTML
  // =========================================================

  private construirHtml(

    contenido: string,

    numeroSolicitud: string | null

  ): string {

    const numero =

      this.esc(

        numeroSolicitud

        || 'SIN-NUMERO'

      );


    return `

      <!doctype html>

      <html lang="es">

        <head>

          <meta charset="utf-8">

          <meta
            name="viewport"
            content="width=device-width, initial-scale=1"
          >

          <title>
            COOPVALLE - Expediente de crédito ${numero}
          </title>

          <style>

            ${ORIGINACION_EXPEDIENTE_PRINT_STYLES}

          </style>

        </head>


        <body>

          <!-- =============================================
               ACCIONES DE IMPRESIÓN
               ============================================= -->

          <div class="expediente-print-actions no-imprimir">

            <button

              type="button"

              onclick="window.print()"

            >

              Imprimir / Guardar PDF

            </button>

          </div>


          <!-- =============================================
               EXPEDIENTE
               ============================================= -->

          ${contenido}


        </body>

      </html>

    `;

  }


  // =========================================================
  // PRESENTAR DOCUMENTO EN NUEVA VENTANA
  // =========================================================

  private mostrarDocumento(

    ventana: Window,

    html: string

  ): void {

    if (ventana.closed) {

      throw new Error(

        'La ventana de impresión fue cerrada.'

      );

    }


    ventana.document.open();

    ventana.document.write(
      html
    );

    ventana.document.close();

  }


  // =========================================================
  // MOSTRAR ESTADO DE CARGA
  // =========================================================

  private mostrarCargando(

    ventana: Window

  ): void {

    ventana.document.open();


    ventana.document.write(`

      <!doctype html>

      <html lang="es">

        <head>

          <meta charset="utf-8">

          <title>
            Preparando expediente
          </title>

          <style>

            body {

              margin: 0;

              padding: 40px;

              font-family:
                Arial,
                Helvetica,
                sans-serif;

              color: #174b35;

              text-align: center;

            }

          </style>

        </head>

        <body>

          <h3>
            COOPVALLE
          </h3>

          <p>
            Preparando expediente de crédito...
          </p>

        </body>

      </html>

    `);


    ventana.document.close();

  }


  // =========================================================
  // MOSTRAR ERROR EN LA VENTANA
  // =========================================================

  private mostrarError(

    ventana: Window,

    error: unknown

  ): void {

    if (ventana.closed) {

      return;

    }


    const mensaje =

      error instanceof Error

        ? error.message

        : 'No fue posible generar el expediente.';


    ventana.document.open();


    ventana.document.write(`

      <!doctype html>

      <html lang="es">

        <head>

          <meta charset="utf-8">

          <title>
            Error de impresión
          </title>

        </head>

        <body

          style="
            font-family: Arial, sans-serif;
            padding: 30px;
          "

        >

          <h3>
            No fue posible generar el expediente
          </h3>

          <p>
            ${this.esc(mensaje)}
          </p>

        </body>

      </html>

    `);


    ventana.document.close();

  }


  // =========================================================
  // FECHA LOCAL
  // =========================================================

  private fechaLocalActual(): string {

    const fecha =
      new Date();


    const anio =
      fecha.getFullYear();


    const mes =
      String(

        fecha.getMonth() + 1

      ).padStart(
        2,
        '0'
      );


    const dia =
      String(

        fecha.getDate()

      ).padStart(
        2,
        '0'
      );


    return `${anio}-${mes}-${dia}`;

  }


  // =========================================================
  // SEGURIDAD HTML
  // =========================================================

  private esc(

    valor: unknown

  ): string {

    return String(

      valor ?? ''

    )

      .replaceAll(
        '&',
        '&amp;'
      )

      .replaceAll(
        '<',
        '&lt;'
      )

      .replaceAll(
        '>',
        '&gt;'
      )

      .replaceAll(
        '"',
        '&quot;'
      )

      .replaceAll(
        "'",
        '&#39;'
      );

  }

}
