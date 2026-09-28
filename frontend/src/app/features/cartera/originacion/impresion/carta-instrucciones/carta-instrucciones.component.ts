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
  selector: 'app-carta-instrucciones',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './carta-instrucciones.component.html',
  styleUrl: './carta-instrucciones.component.scss'
})
export class CartaInstruccionesComponent implements OnInit {

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

    this.consultarCarta();
  }


  // =======================================================
  // CONSULTAR DATOS
  //
  // La carta utiliza la información del pagaré constituido
  // y sus firmantes.
  // =======================================================

  consultarCarta(): void {

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
              'La carta de instrucciones no tiene '
              + 'un deudor principal registrado.';

            return;
          }

        },

        error: (err) => {

          this.error =
            err?.error?.message ||
            err?.error?.mensaje ||
            'No fue posible consultar los datos '
            + 'para la carta de instrucciones.';

        }

      });
  }


  // =======================================================
  // ORGANIZAR FIRMANTES
  //
  // Conservamos exactamente el mismo modelo utilizado
  // por las impresiones del pagaré.
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
  // DISPONIBILIDAD
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
  // FIRMANTES DE LA CARTA
  //
  // El reporte histórico contempla el deudor principal
  // y los codeudores asociados al pagaré.
  // =======================================================

  get firmantesCarta(): PagareFirmante[] {

    const firmantes: PagareFirmante[] = [];

    if (this.deudorPrincipal) {
      firmantes.push(
        this.deudorPrincipal
      );
    }

    firmantes.push(
      ...this.codeudores
    );

    return firmantes;
  }


  // =======================================================
  // CODEUDOR POR POSICIÓN
  //
  // Facilita reproducir en el HTML las posiciones
  // utilizadas por la carta histórica.
  // =======================================================

  codeudor(
    posicion: number
  ): PagareFirmante | null {

    return this.codeudores[posicion] ?? null;
  }


  // =======================================================
  // TELÉFONO
  //
  // Misma estructura del modelo real PagareFirmante.
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
  // La carta se imprime en tamaño carta:
  // 216 mm x 279 mm.
  // =======================================================

  imprimir(): void {

    if (!this.puedeImprimir) {
      return;
    }

    const carta = document.querySelector(
      'app-carta-instrucciones .hoja-carta'
    );

    if (!carta) {
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
      .map(elemento => elemento.outerHTML)
      .join('\n');

    const contenido =
      carta.outerHTML;

    ventanaImpresion.document.open();

    ventanaImpresion.document.write(`
      <!doctype html>

      <html lang="es">

        <head>

          <meta charset="utf-8">

          <title>Carta de instrucciones</title>

          ${estilos}

          <style>

            @page {
              size: 216mm 279mm;
              margin: 0;
            }

            html,
            body {
              margin: 0 !important;
              padding: 0 !important;

              width: 216mm;

              background: #ffffff;
            }

            .hoja-carta {
              display: block !important;

              box-sizing: border-box !important;

              width: 216mm !important;
              height: 279mm !important;
              min-height: 279mm !important;

              margin: 0 !important;
              padding: 0 !important;

              background: #ffffff !important;

              box-shadow: none !important;

              overflow: hidden !important;
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
  //
  // La carta se abre desde Formalización en una pestaña
  // independiente.
  // =======================================================

  cerrar(): void {

    window.close();
  }

}
