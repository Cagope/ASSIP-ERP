import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

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
} from '../pagare-diligenciado/pagare-diligenciado.models';


@Component({
  selector: 'app-pagare-en-blanco',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './pagare-en-blanco.component.html',
  styleUrl: './pagare-en-blanco.component.scss'
})
export class PagareEnBlancoComponent implements OnInit {

  private readonly api = inject(
    OriginacionFormalizacionApi
  );

  private readonly route = inject(
    ActivatedRoute
  );


  // =======================================================
  // ESTADO
  // =======================================================

  idSolicitudCredito: number | null = null;

  cargando = false;

  error: string | null = null;

  datos: PagareDiligenciadoDatos | null = null;


  // =======================================================
  // FIRMANTES
  // =======================================================

  deudorPrincipal: PagareFirmante | null = null;

  codeudores: PagareFirmante[] = [];


  // =======================================================
  // INICIALIZACIÓN
  // =======================================================

  ngOnInit(): void {

    const id = Number(
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


  // =======================================================
  // CONSULTAR DATOS DEL PAGARÉ EN BLANCO
  // =======================================================

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

    this.api.consultarDatosPagareEnBlanco(
      this.idSolicitudCredito
    )
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({

        next: (respuesta) => {

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

        error: (err) => {

          this.error =
            err?.error?.message ||
            err?.error?.mensaje ||
            'No fue posible consultar los datos del pagaré en blanco.';

        }

      });
  }


  // =======================================================
  // ORGANIZAR DEUDOR PRINCIPAL Y CODEUDORES
  //
  // Misma lógica utilizada por el pagaré diligenciado.
  // =======================================================

  private organizarFirmantes(
    firmantes: PagareFirmante[]
  ): void {

    const ordenados = [...firmantes].sort(
      (a, b) =>
        (a.ordenDeudor ?? 0) -
        (b.ordenDeudor ?? 0)
    );

    this.deudorPrincipal =
      ordenados.find(
        firmante =>
          firmante.tipoDeudor === 'PRINCIPAL'
      ) ?? null;

    this.codeudores =
      ordenados.filter(
        firmante =>
          firmante.tipoDeudor === 'CODEUDOR'
      );
  }


  // =======================================================
  // INDICADORES PARA LA PLANTILLA
  // =======================================================

  get puedeImprimir(): boolean {

    return (
      !this.cargando &&
      !this.error &&
      this.datos !== null &&
      this.deudorPrincipal !== null
    );
  }


  // =======================================================
  // TELÉFONO DEL FIRMANTE
  //
  // Misma lógica utilizada por el pagaré diligenciado.
  // =======================================================

  telefonoFirmante(
    firmante: PagareFirmante | null
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


  // =======================================================
  // IMPRIMIR
  //
  // La plantilla contiene dos hojas iguales, reproduciendo:
  //
  // For I = 1 To 2
  //     ...
  //     Printer.NewPage
  // Next
  //
  // Se imprime solamente el contenido de .hoja-oficio.
  // =======================================================

  imprimir(): void {

    if (!this.puedeImprimir) {
      return;
    }

    const hojas = document.querySelectorAll(
      'app-pagare-en-blanco .hoja-oficio'
    );

    if (hojas.length === 0) {
      return;
    }

    const ventanaImpresion = window.open(
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

    const estilos = Array.from(
      document.querySelectorAll(
        'style, link[rel="stylesheet"]'
      )
    )
      .map(
        elemento =>
          elemento.outerHTML
      )
      .join('\n');

    const contenido = Array.from(
      hojas
    )
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

          <title>Pagaré en blanco</title>

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

              box-sizing: border-box !important;

              width: 216mm !important;
              height: 330mm !important;
              min-height: 330mm !important;

              margin: 0 !important;
              padding: 0 !important;

              background: #ffffff !important;

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

      ventanaImpresion.onafterprint = () => {
        ventanaImpresion.close();
      };
    };
  }


  // =======================================================
  // CERRAR
  // =======================================================

  cerrar(): void {

    window.close();
  }

}
