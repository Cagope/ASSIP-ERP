import {
  CommonModule
} from '@angular/common';

import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  CierreMensualCartera,
  CierreMensualCarteraApi
} from '../cierre-mensual-cartera.api';

import {
  Anexo2CierreApi,
  DetalleAnexo2,
  MoraAnexo2,
  ResultadoPreparacionAnexo2,
  ResumenAnexo2,
  TrabajoAnexo2
} from './anexo2-cierre.api';


@Component({
  selector: 'app-anexo2-cierre',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './anexo2-cierre.component.html',

  styleUrls: [
    './anexo2-cierre.component.scss'
  ]
})
export class Anexo2CierreComponent implements OnInit {

  // =========================================================
  // API
  // =========================================================

  private readonly cierreApi =
    inject(CierreMensualCarteraApi);

  private readonly anexo2Api =
    inject(Anexo2CierreApi);


  // =========================================================
  // CIERRES
  // =========================================================

  cierres:
    CierreMensualCartera[] = [];

  idCierreSeleccionado:
    number | null = null;

  cierreSeleccionado:
    CierreMensualCartera | null = null;


  // =========================================================
  // MODELOS PE
  // =========================================================

  modelos:
    number[] = [];

  idModeloSeleccionado:
    number | null = null;


  // =========================================================
  // INFORMACIÓN CONSULTADA
  // =========================================================

  detalle:
    DetalleAnexo2[] = [];

  trabajo:
    TrabajoAnexo2[] = [];

  mora:
    MoraAnexo2[] = [];

  resumen:
    ResumenAnexo2[] = [];


  // =========================================================
  // RESULTADO DEL PROCESAMIENTO
  // =========================================================

  resultado:
    ResultadoPreparacionAnexo2 | null = null;


  // =========================================================
  // ESTADO DE PANTALLA
  // =========================================================

  cargando = false;

  procesando = false;

  consultando = false;

  mensaje = '';

  error = '';


  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {

    this.cargarCierres();

  }


  // =========================================================
  // CARGAR CIERRES
  // =========================================================

  cargarCierres(): void {

    this.cargando = true;

    this.error = '';

    this.cierreApi
      .listar()
      .subscribe({

        next: (data) => {

          this.cierres =
            data ?? [];

          this.cargando =
            false;

        },


        error: (err) => {

          this.cargando =
            false;

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible cargar los cierres mensuales.'
            );

        }

      });

  }


  // =========================================================
  // SELECCIONAR CIERRE
  // =========================================================

  seleccionarCierre(): void {

    this.limpiarMensajes();

    this.limpiarResultadoProceso();

    this.limpiarConsultas();

    this.modelos =
      [];

    this.idModeloSeleccionado =
      null;


    if (!this.idCierreSeleccionado) {

      this.cierreSeleccionado =
        null;

      return;

    }


    this.cierreSeleccionado =
      this.cierres.find(
        cierre =>
          Number(
            cierre.idCierreCartera
          )
          ===
          Number(
            this.idCierreSeleccionado
          )
      )
      ?? null;


    if (
      this.cierreSeleccionado
      &&
      this.fotoEnFirme()
    ) {

      this.cargarModelos();

    }

  }


  // =========================================================
  // FOTO EN FIRME
  //
  // C = fotografía cerrada
  // =========================================================

  fotoEnFirme(): boolean {

    return (
      this.cierreSeleccionado
        ?.estadoCierre
      ?? ''
    )
      .trim()
      .toUpperCase()
      === 'C';

  }


  // =========================================================
  // PUEDE PROCESAR
  // =========================================================

  puedeProcesar(): boolean {

    return (
      !!this.idCierreSeleccionado
      &&
      !!this.cierreSeleccionado
      &&
      this.fotoEnFirme()
      &&
      !this.procesando
      &&
      !this.consultando
    );

  }


  // =========================================================
  // PROCESAR ANEXO 2 / PE
  // =========================================================

  procesar(): void {

    this.limpiarMensajes();


    if (!this.idCierreSeleccionado) {

      this.error =
        'Debe seleccionar un cierre de cartera.';

      return;

    }


    if (!this.cierreSeleccionado) {

      this.error =
        'No fue posible identificar el cierre seleccionado.';

      return;

    }


    if (!this.fotoEnFirme()) {

      this.error =
        'La fotografía del cierre debe estar cerrada en firme antes de procesar Anexo 2.';

      return;

    }


    if (this.procesando) {
      return;
    }


    const confirmar =
      window.confirm(
        'Se ejecutará Anexo 2 / Pérdida Esperada para el cierre seleccionado'
        + this.descripcionFechaConfirmacion()
        + '.\n\n'
        + 'El proceso calculará nuevamente:\n'
        + '- Población PE\n'
        + '- Vector histórico de 40 períodos\n'
        + '- Variables de los modelos\n'
        + '- Puntaje y calificación\n'
        + '- PI\n'
        + '- VEA\n'
        + '- PDI\n'
        + '- Pérdida esperada\n'
        + '- Homologación y alineación\n'
        + '- Deterioros PE\n\n'
        + '¿Desea continuar?'
      );


    if (!confirmar) {
      return;
    }


    this.procesando =
      true;

    this.resultado =
      null;

    this.limpiarConsultas();

    this.modelos =
      [];

    this.idModeloSeleccionado =
      null;


    this.anexo2Api
      .preparar(
        this.idCierreSeleccionado
      )
      .subscribe({

        next: (resultado) => {

          this.procesando =
            false;

          this.resultado =
            resultado ?? null;

          this.mensaje =
            'Anexo 2 / Pérdida Esperada procesado correctamente.';

          this.cargarModelos();

        },


        error: (err) => {

          this.procesando =
            false;

          this.resultado =
            null;

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible procesar Anexo 2 / Pérdida Esperada.'
            );

        }

      });

  }


  // =========================================================
  // CARGAR MODELOS DEL CIERRE
  // =========================================================

  cargarModelos(): void {

    if (!this.idCierreSeleccionado) {

      this.modelos =
        [];

      this.idModeloSeleccionado =
        null;

      return;

    }


    this.consultando =
      true;


    this.anexo2Api
      .obtenerModelos(
        this.idCierreSeleccionado
      )
      .subscribe({

        next: (data) => {

          this.consultando =
            false;

          this.modelos =
            data ?? [];


          if (this.modelos.length === 1) {

            this.idModeloSeleccionado =
              this.modelos[0];

            this.consultarModelo();

          }

        },


        error: () => {

          /*
           * Un cierre cerrado puede todavía no tener
           * resultados PE procesados.
           *
           * No se muestra como error de pantalla porque
           * el usuario puede ejecutar el proceso desde aquí.
           */

          this.consultando =
            false;

          this.modelos =
            [];

          this.idModeloSeleccionado =
            null;

        }

      });

  }


  // =========================================================
  // SELECCIONAR MODELO
  // =========================================================

  seleccionarModelo(): void {

    this.limpiarMensajes();

    this.limpiarConsultas();


    if (!this.idModeloSeleccionado) {
      return;
    }


    this.consultarModelo();

  }


  // =========================================================
  // CONSULTAR MODELO
  // =========================================================

  consultarModelo(): void {

    if (
      !this.idCierreSeleccionado
      ||
      !this.idModeloSeleccionado
    ) {

      return;

    }


    this.limpiarConsultas();

    this.consultando =
      true;

    this.cargarResumen();

  }


  // =========================================================
  // CARGAR RESUMEN
  // =========================================================

  private cargarResumen(): void {

    const parametros =
      this.obtenerParametrosConsulta();

    if (!parametros) {

      this.consultando =
        false;

      return;

    }


    this.anexo2Api
      .obtenerResumen(
        parametros.idCierreCartera,
        parametros.idModeloPe
      )
      .subscribe({

        next: (data) => {

          this.resumen =
            data ?? [];

          this.cargarDetalle();

        },


        error: (err) => {

          this.finalizarConsultaConError(
            err,
            'No fue posible consultar el resumen del modelo PE.'
          );

        }

      });

  }


  // =========================================================
  // CARGAR RESULTADO
  // =========================================================

  private cargarDetalle(): void {

    const parametros =
      this.obtenerParametrosConsulta();

    if (!parametros) {

      this.consultando =
        false;

      return;

    }


    this.anexo2Api
      .obtenerDetalle(
        parametros.idCierreCartera,
        parametros.idModeloPe
      )
      .subscribe({

        next: (data) => {

          this.detalle =
            data ?? [];

          this.cargarTrabajo();

        },


        error: (err) => {

          this.finalizarConsultaConError(
            err,
            'No fue posible consultar el resultado del modelo PE.'
          );

        }

      });

  }


  // =========================================================
  // CARGAR HOJA DE TRABAJO
  // =========================================================

  private cargarTrabajo(): void {

    const parametros =
      this.obtenerParametrosConsulta();

    if (!parametros) {

      this.consultando =
        false;

      return;

    }


    this.anexo2Api
      .obtenerTrabajo(
        parametros.idCierreCartera,
        parametros.idModeloPe
      )
      .subscribe({

        next: (data) => {

          this.trabajo =
            data ?? [];

          this.cargarMora();

        },


        error: (err) => {

          this.finalizarConsultaConError(
            err,
            'No fue posible consultar la hoja de trabajo del modelo PE.'
          );

        }

      });

  }


  // =========================================================
  // CARGAR MORA
  // =========================================================

  private cargarMora(): void {

    const parametros =
      this.obtenerParametrosConsulta();

    if (!parametros) {

      this.consultando =
        false;

      return;

    }


    this.anexo2Api
      .obtenerMora(
        parametros.idCierreCartera,
        parametros.idModeloPe
      )
      .subscribe({

        next: (data) => {

          this.mora =
            data ?? [];

          this.consultando =
            false;

        },


        error: (err) => {

          this.finalizarConsultaConError(
            err,
            'No fue posible consultar el histórico de mora del modelo PE.'
          );

        }

      });

  }


  // =========================================================
  // PARÁMETROS DE CONSULTA
  // =========================================================

  private obtenerParametrosConsulta():
    {
      idCierreCartera: number;
      idModeloPe: number;
    }
    | null {

    if (
      !this.idCierreSeleccionado
      ||
      !this.idModeloSeleccionado
    ) {

      return null;

    }


    return {
      idCierreCartera:
        this.idCierreSeleccionado,

      idModeloPe:
        this.idModeloSeleccionado
    };

  }


  // =========================================================
  // FINALIZAR CONSULTA CON ERROR
  // =========================================================

  private finalizarConsultaConError(
    err: any,
    mensajeDefault: string
  ): void {

    this.consultando =
      false;

    this.error =
      this.obtenerMensajeError(
        err,
        mensajeDefault
      );

  }


  // =========================================================
  // TIENE RESULTADO DE PROCESAMIENTO
  // =========================================================

  tieneResultado(): boolean {

    return this.resultado
      !== null;

  }


  // =========================================================
  // TIENE MODELOS
  // =========================================================

  tieneModelos(): boolean {

    return this.modelos.length
      > 0;

  }


  // =========================================================
  // TIENE INFORMACIÓN DEL MODELO
  // =========================================================

  tieneInformacionModelo(): boolean {

    return (
      this.detalle.length > 0
      ||
      this.trabajo.length > 0
      ||
      this.mora.length > 0
      ||
      this.resumen.length > 0
    );

  }


  // =========================================================
  // NOMBRE MODELO SELECCIONADO
  // =========================================================

  nombreModeloSeleccionado(): string {

    if (this.detalle.length > 0) {

      return this.detalle[0]
        .nombreModeloPe
        ?? '';

    }


    if (this.resumen.length > 0) {

      return this.resumen[0]
        .nombreModeloPe
        ?? '';

    }


    if (this.trabajo.length > 0) {

      return this.trabajo[0]
        .nombreModeloPe
        ?? '';

    }


    return this.idModeloSeleccionado
      ? `Modelo ${this.idModeloSeleccionado}`
      : '';

  }


  // =========================================================
  // DESCRIPCIÓN DEL ESTADO
  // =========================================================

  descripcionEstado(
    estado:
      string | null | undefined
  ): string {

    switch (
      (estado ?? '')
        .trim()
        .toUpperCase()
    ) {

      case 'P':
        return 'En proceso';

      case 'C':
        return 'Cerrado';

      default:
        return estado ?? '';

    }

  }


  // =========================================================
  // FECHA PARA CONFIRMACIÓN
  // =========================================================

  private descripcionFechaConfirmacion(): string {

    const fecha =
      this.cierreSeleccionado
        ?.fechaCorte;


    if (!fecha) {
      return '';
    }


    return ` (${fecha})`;

  }


  // =========================================================
  // LIMPIAR CONSULTAS
  // =========================================================

  private limpiarConsultas(): void {

    this.detalle =
      [];

    this.trabajo =
      [];

    this.mora =
      [];

    this.resumen =
      [];

  }


  // =========================================================
  // LIMPIAR RESULTADO DEL PROCESO
  // =========================================================

  private limpiarResultadoProceso(): void {

    this.resultado =
      null;

  }


  // =========================================================
  // LIMPIAR MENSAJES
  // =========================================================

  private limpiarMensajes(): void {

    this.mensaje =
      '';

    this.error =
      '';

  }


  // =========================================================
  // MENSAJE DE ERROR
  // =========================================================

  private obtenerMensajeError(
    err: any,
    mensajeDefault: string
  ): string {

    if (
      typeof err?.error
      === 'string'
      &&
      err.error.trim()
    ) {

      return err.error;

    }


    if (
      typeof err?.error?.message
      === 'string'
      &&
      err.error.message.trim()
    ) {

      return err.error.message;

    }


    if (
      typeof err?.message
      === 'string'
      &&
      err.message.trim()
    ) {

      return err.message;

    }


    return mensajeDefault;

  }

}
