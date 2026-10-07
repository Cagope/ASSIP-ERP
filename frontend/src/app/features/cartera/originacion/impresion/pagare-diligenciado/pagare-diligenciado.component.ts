import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  HttpErrorResponse
} from '@angular/common/http';

import {
  ActivatedRoute
} from '@angular/router';

import {
  finalize
} from 'rxjs';

import {
  OriginacionFormalizacionApi
} from '../../formalizacion/originacion-formalizacion.api';

import {
  PagareDiligenciadoDatos,
  PagareFirmante
} from './pagare-diligenciado.models';


@Component({
  selector: 'app-pagare-diligenciado',
  standalone: true,

  imports: [
    CommonModule
  ],

  templateUrl:
    './pagare-diligenciado.component.html',

  styleUrl:
    './pagare-diligenciado.component.scss'
})
export class PagareDiligenciadoComponent
  implements OnInit {

  // =========================================================
  // DEPENDENCIAS
  // =========================================================

  private readonly api =
    inject(OriginacionFormalizacionApi);

  private readonly route =
    inject(ActivatedRoute);


  // =========================================================
  // ESTADO
  // =========================================================

  idSolicitudCredito:
    number | null = null;

  cargando = false;

  error:
    string | null = null;

  datos:
    PagareDiligenciadoDatos | null = null;


  // =========================================================
  // FIRMANTES
  // =========================================================

  deudorPrincipal:
    PagareFirmante | null = null;

  codeudores:
    PagareFirmante[] = [];


  // =========================================================
  // FORMATEADORES
  //
  // Uso exclusivo de presentación.
  // No modifica valores financieros ni fechas del crédito.
  // =========================================================

  private readonly formatoMoneda =
    new Intl.NumberFormat(
      'es-CO',
      {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }
    );

  private readonly formatoPorcentaje =
    new Intl.NumberFormat(
      'es-CO',
      {
        minimumFractionDigits: 2,
        maximumFractionDigits: 4
      }
    );


  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    const id =
      Number(
        this.route.snapshot.paramMap.get(
          'idSolicitudCredito'
        )
      );

    if (
      !Number.isInteger(id) ||
      id <= 0
    ) {

      this.error =
        'No se recibió un identificador válido de la solicitud.';

      return;
    }

    this.idSolicitudCredito = id;

    this.consultarPagare();
  }


  // =========================================================
  // CONSULTAR DATOS DEFINITIVOS DEL PAGARÉ
  // =========================================================

  consultarPagare(): void {

    if (
      this.idSolicitudCredito === null ||
      this.cargando
    ) {
      return;
    }

    this.cargando = true;

    this.error = null;

    this.datos = null;

    this.deudorPrincipal = null;

    this.codeudores = [];

    this.api
      .consultarDatosPagareDiligenciado(
        this.idSolicitudCredito
      )
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({

        next: respuesta => {

          this.datos = respuesta;

          this.organizarFirmantes(
            respuesta.firmantes ?? []
          );

          if (!this.deudorPrincipal) {

            this.error =
              'El pagaré no tiene un deudor principal registrado.';

            return;
          }
        },

        error: (
          error: HttpErrorResponse
        ) => {

          this.error =
            this.obtenerMensajeError(
              error
            );
        }

      });
  }


  // =========================================================
  // ORGANIZAR DEUDOR PRINCIPAL Y CODEUDORES
  // =========================================================

  private organizarFirmantes(
    firmantes:
      PagareFirmante[]
  ): void {

    const ordenados =
      [...firmantes].sort(
        (a, b) =>
          (a.ordenDeudor ?? 0) -
          (b.ordenDeudor ?? 0)
      );

    this.deudorPrincipal =
      ordenados.find(
        firmante =>
          firmante.tipoDeudor ===
          'PRINCIPAL'
      ) ?? null;

    this.codeudores =
      ordenados.filter(
        firmante =>
          firmante.tipoDeudor ===
          'CODEUDOR'
      );
  }


  // =========================================================
  // INDICADORES PARA LA PLANTILLA
  // =========================================================

  get puedeImprimir(): boolean {

    return (
      !this.cargando &&
      !this.error &&
      this.datos !== null &&
      this.deudorPrincipal !== null
    );
  }


  get numeroPagare(): string {

    return (
      this.datos?.pagare.pagareCartera ??
      ''
    );
  }


  // =========================================================
  // FORMATO DE VALORES
  //
  // Uso exclusivo de presentación.
  // No modifica valores financieros.
  // =========================================================

  formatearMoneda(
    valor:
      number |
      null |
      undefined
  ): string {

    if (
      valor === null ||
      valor === undefined
    ) {
      return '';
    }

    return this.formatoMoneda.format(
      valor
    );
  }


  formatearPorcentaje(
    valor:
      number |
      null |
      undefined
  ): string {

    if (
      valor === null ||
      valor === undefined
    ) {
      return '';
    }

    return this.formatoPorcentaje.format(
      valor
    );
  }


  // =========================================================
  // FORMATO DE FECHAS DEFINITIVAS
  //
  // Recibe yyyy-MM-dd desde el backend.
  //
  // No utiliza new Date() para evitar desplazamientos
  // por zona horaria.
  //
  // No calcula vencimientos ni modifica fechas.
  // =========================================================

  formatearFecha(
    fecha:
      string |
      null |
      undefined
  ): string {

    if (!fecha) {
      return '';
    }

    const partes =
      fecha.split('-');

    if (partes.length !== 3) {
      return fecha;
    }

    const [
      anio,
      mes,
      dia
    ] = partes;

    if (
      !/^\d{4}$/.test(anio) ||
      !/^\d{2}$/.test(mes) ||
      !/^\d{2}$/.test(dia)
    ) {
      return fecha;
    }

    return `${dia}/${mes}/${anio}`;
  }


  // =========================================================
  // TELÉFONO DEL FIRMANTE
  // =========================================================

  telefonoFirmante(
    firmante:
      PagareFirmante | null
  ): string {

    if (!firmante) {
      return '';
    }

    return (
      firmante.telefono ||
      firmante.celularUno ||
      firmante.celularDos ||
      ''
    );
  }


  // =========================================================
  // IMPRIMIR DESDE EL NAVEGADOR
  // =========================================================

  imprimir(): void {

    if (!this.puedeImprimir) {
      return;
    }

    const hojas =
      document.querySelectorAll(
        'app-pagare-diligenciado .hoja-oficio'
      );

    if (hojas.length === 0) {
      return;
    }

    const ventanaImpresion =
      window.open(
        '',
        '_blank',
        'width=900,height=1100'
      );

    if (!ventanaImpresion) {

      this.error =
        'El navegador bloqueó la ventana de impresión. '
        + 'Permita las ventanas emergentes para ASSIP ERP.';

      return;
    }

    const estilos =
      Array.from(
        document.querySelectorAll(
          'style, link[rel="stylesheet"]'
        )
      )
        .map(
          elemento =>
            elemento.outerHTML
        )
        .join('\n');

    const contenido =
      Array.from(hojas)
        .map(
          hoja =>
            hoja.outerHTML
        )
        .join('\n');

    ventanaImpresion.document.open();

    ventanaImpresion.document.write(`
      <!doctype html>

      <html lang="es">

        <head>

          <meta charset="utf-8">

          <title>
            Pagaré ${this.numeroPagare}
          </title>

          ${estilos}

          <style>

            @page {
              size: 216mm 330mm;
              margin: 0;
            }

            html,
            body {
              margin: 0 !important;
              padding: 0 !important;

              width: 216mm;

              background: #ffffff;
            }

            .hoja-oficio {
              display: block !important;

              width: 216mm !important;

              height: auto !important;
              min-height: 329mm !important;

              margin: 0 !important;
              padding: 0 !important;

              box-shadow: none !important;

              overflow: hidden !important;

              break-after: auto !important;
              page-break-after: auto !important;
            }

            .hoja-oficio + .hoja-oficio {
              break-before: page !important;
              page-break-before: always !important;
            }

            .no-imprimir {
              display: none !important;
            }

          </style>

        </head>

        <body>

          ${contenido}

        </body>

      </html>
    `);

    ventanaImpresion.document.close();

    ventanaImpresion.onload = () => {

      ventanaImpresion.focus();

      ventanaImpresion.print();

      ventanaImpresion.onafterprint =
        () => {

          ventanaImpresion.close();
        };
    };
  }


  // =========================================================
  // CERRAR
  // =========================================================

  cerrar(): void {

    window.close();
  }


  // =========================================================
  // COMPLETAR TEXTO DEL PAGARÉ
  // =========================================================

  completarConAsteriscos(
    texto:
      string |
      null |
      undefined,
    longitud = 87
  ): string {

    const valor =
      (texto ?? '').trim();

    if (
      valor.length >= longitud
    ) {
      return valor;
    }

    return valor.padEnd(
      longitud,
      '*'
    );
  }


  // =========================================================
  // ERRORES
  // =========================================================

  private obtenerMensajeError(
    error: HttpErrorResponse
  ): string {

    const respuesta =
      error.error;

    if (
      typeof respuesta === 'string'
    ) {

      return (
        respuesta ||
        'No fue posible consultar los datos del pagaré.'
      );
    }

    const mensaje =
      respuesta?.message ??
      respuesta?.mensaje ??
      error.message;

    return mensaje
      ? String(mensaje)
      : 'No fue posible consultar los datos del pagaré.';
  }

}
