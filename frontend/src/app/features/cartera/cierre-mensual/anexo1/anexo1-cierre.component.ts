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
  Anexo1CierreApi,
  ResultadoAnexo1
} from './anexo1-cierre.api';


@Component({
  selector: 'app-anexo1-cierre',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './anexo1-cierre.component.html',

  styleUrls: [
    './anexo1-cierre.component.scss'
  ]
})
export class Anexo1CierreComponent implements OnInit {

  // =========================================================
  // API
  // =========================================================

  private readonly cierreApi =
    inject(CierreMensualCarteraApi);

  private readonly anexo1Api =
    inject(Anexo1CierreApi);


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
  // ESTADO DE PANTALLA
  // =========================================================

  cargando = false;

  procesando = false;

  cerrandoAnexo1 = false;

  mensaje = '';

  error = '';


  // =========================================================
  // RESULTADO DEL ÚLTIMO PROCESAMIENTO
  // =========================================================

  resultado:
    ResultadoAnexo1 | null = null;


  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {

    this.cargarCierres();

  }


  // =========================================================
  // CARGAR CIERRES
  // =========================================================

  cargarCierres(
    conservarSeleccion = false
  ): void {

    const idActual =
      conservarSeleccion
        ? this.idCierreSeleccionado
        : null;

    this.cargando = true;

    this.error = '';

    this.cierreApi
      .listar()
      .subscribe({

        next: (data) => {

          this.cierres =
            data ?? [];

          if (
            conservarSeleccion
            &&
            idActual
          ) {

            this.idCierreSeleccionado =
              idActual;

            this.cierreSeleccionado =
              this.cierres.find(
                cierre =>
                  Number(
                    cierre.idCierreCartera
                  )
                  ===
                  Number(
                    idActual
                  )
              )
              ?? null;

          }

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

    this.resultado =
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

  }


  // =========================================================
  // FOTOGRAFÍA EN FIRME
  // =========================================================

  fotoEnFirme(): boolean {

    return (
      this.cierreSeleccionado
        ?.estadoFotografia
        ?? ''
    )
      .trim()
      .toUpperCase()
      === 'C';

  }


  // =========================================================
  // CÁLCULOS EN FIRME
  // =========================================================

  calculosEnFirme(): boolean {

    return (
      this.cierreSeleccionado
        ?.estadoCalculos
        ?? ''
    )
      .trim()
      .toUpperCase()
      === 'C';

  }


  // =========================================================
  // ESTADO ANEXO 1
  // =========================================================

  estadoAnexo1(): string {

    return (
      this.cierreSeleccionado
        ?.estadoAnexo1
        ?? 'P'
    )
      .trim()
      .toUpperCase();

  }


  anexo1Pendiente(): boolean {

    return this.estadoAnexo1()
      === 'P';

  }


  anexo1EnProceso(): boolean {

    return this.estadoAnexo1()
      === 'E';

  }


  anexo1EnFirme(): boolean {

    return this.estadoAnexo1()
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
      this.calculosEnFirme()
      &&
      !this.anexo1EnFirme()
      &&
      !this.procesando
      &&
      !this.cerrandoAnexo1
    );

  }


  // =========================================================
  // PUEDE CERRAR ANEXO 1
  // =========================================================

  puedeCerrarAnexo1(): boolean {

    return (
      !!this.idCierreSeleccionado
      &&
      !!this.cierreSeleccionado
      &&
      this.calculosEnFirme()
      &&
      this.anexo1EnProceso()
      &&
      !this.procesando
      &&
      !this.cerrandoAnexo1
    );

  }


  // =========================================================
  // PROCESAR ANEXO 1
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
        'La fotografía del cierre debe estar cerrada en firme antes de procesar Anexo 1.';

      return;
    }


    if (!this.calculosEnFirme()) {

      this.error =
        'Los cálculos del cierre deben estar cerrados en firme antes de procesar Anexo 1.';

      return;
    }


    if (this.anexo1EnFirme()) {

      this.error =
        'El Anexo 1 ya se encuentra cerrado en firme.';

      return;
    }


    if (
      this.procesando
      ||
      this.cerrandoAnexo1
    ) {
      return;
    }


    const esRecalculo =
      this.anexo1EnProceso();


    const confirmar =
      window.confirm(
        esRecalculo
          ?
          'Se recalculará Anexo 1 para el cierre seleccionado'
          + this.descripcionFechaConfirmacion()
          + '.\n\n'
          + 'Se calculará nuevamente:\n'
          + '- Edad contable / ley de arrastre\n'
          + '- Deterioro de capital\n'
          + '- Deterioro de intereses\n\n'
          + '¿Desea continuar?'
          :
          'Se ejecutará Anexo 1 para el cierre seleccionado'
          + this.descripcionFechaConfirmacion()
          + '.\n\n'
          + 'El proceso calculará:\n'
          + '- Edad contable / ley de arrastre\n'
          + '- Deterioro de capital\n'
          + '- Deterioro de intereses\n\n'
          + '¿Desea continuar?'
      );


    if (!confirmar) {
      return;
    }


    this.procesando =
      true;

    this.resultado =
      null;

    this.mensaje =
      esRecalculo
        ? 'Recalculando Anexo 1...'
        : 'Procesando Anexo 1...';


    this.anexo1Api
      .procesar(
        this.idCierreSeleccionado
      )
      .subscribe({

        next: (resultado) => {

          this.procesando =
            false;

          this.resultado =
            resultado ?? null;

          this.mensaje =
            'Anexo 1 procesado correctamente.';

          this.cargarCierres(
            true
          );

        },


        error: (err) => {

          this.procesando =
            false;

          this.resultado =
            null;

          this.mensaje =
            '';

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible procesar Anexo 1.'
            );

        }

      });

  }


  // =========================================================
  // CERRAR ANEXO 1 EN FIRME
  // =========================================================

  cerrarAnexo1(): void {

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


    if (!this.calculosEnFirme()) {

      this.error =
        'Los cálculos deben estar cerrados en firme antes de cerrar Anexo 1.';

      return;
    }


    if (!this.anexo1EnProceso()) {

      this.error =
        'Anexo 1 debe estar procesado antes de cerrarlo en firme.';

      return;
    }


    if (
      this.procesando
      ||
      this.cerrandoAnexo1
    ) {
      return;
    }


    const confirmar =
      window.confirm(
        'Se cerrará Anexo 1 en firme para el cierre seleccionado'
        + this.descripcionFechaConfirmacion()
        + '.\n\n'
        + 'Después de cerrarlo no podrá recalcularse.\n\n'
        + '¿Desea continuar?'
      );


    if (!confirmar) {
      return;
    }


    this.cerrandoAnexo1 =
      true;

    this.mensaje =
      'Cerrando Anexo 1 en firme...';


    this.anexo1Api
      .cerrar(
        this.idCierreSeleccionado
      )
      .subscribe({

        next: () => {

          this.cerrandoAnexo1 =
            false;

          this.mensaje =
            'Anexo 1 quedó cerrado en firme.';

          this.cargarCierres(
            true
          );

        },


        error: (err) => {

          this.cerrandoAnexo1 =
            false;

          this.mensaje =
            '';

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible cerrar Anexo 1 en firme.'
            );

        }

      });

  }


  // =========================================================
  // TIENE RESULTADO
  // =========================================================

  tieneResultado(): boolean {

    return this.resultado
      !== null;

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
        return 'Pendiente';

      case 'E':
        return 'En proceso';

      case 'C':
        return 'Cerrado';

      default:
        return estado ?? '';

    }

  }


  // =========================================================
  // FORMATO FECHA
  // =========================================================

  formatearFecha(
    fecha:
      string | null | undefined
  ): string {

    if (!fecha) {
      return '';
    }


    const valor =
      fecha.substring(
        0,
        10
      );


    const partes =
      valor.split('-');


    if (partes.length !== 3) {
      return fecha;
    }


    return (
      `${partes[2]}/${partes[1]}/${partes[0]}`
    );

  }


  // =========================================================
  // FORMATO ENTERO
  // =========================================================

  formatearEntero(
    valor:
      number | null | undefined
  ): string {

    return new Intl.NumberFormat(
      'es-CO',
      {
        maximumFractionDigits: 0
      }
    )
      .format(
        Number(
          valor ?? 0
        )
      );

  }


  // =========================================================
  // FECHA PARA MENSAJE DE CONFIRMACIÓN
  // =========================================================

  private descripcionFechaConfirmacion(): string {

    if (
      !this.cierreSeleccionado
        ?.fechaCorte
    ) {

      return '';

    }


    return (
      ' correspondiente al '
      +
      this.formatearFecha(
        this.cierreSeleccionado
          .fechaCorte
      )
    );

  }


  // =========================================================
  // LIMPIAR MENSAJES
  // =========================================================

  private limpiarMensajes(): void {

    this.error =
      '';

    this.mensaje =
      '';

  }


  // =========================================================
  // MENSAJE DE ERROR DEL BACKEND
  // =========================================================

  private obtenerMensajeError(
    err: any,
    mensajeDefecto: string
  ): string {

    if (
      typeof err?.error === 'string'
      &&
      err.error.trim()
    ) {

      return err.error;

    }


    if (
      typeof err?.error?.message === 'string'
      &&
      err.error.message.trim()
    ) {

      return err.error.message;

    }


    if (
      typeof err?.message === 'string'
      &&
      err.message.trim()
    ) {

      return err.message;

    }


    return mensajeDefecto;

  }

}
