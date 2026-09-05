import {
  CommonModule
} from '@angular/common';

import {
  Component,
  OnInit
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  finalize
} from 'rxjs/operators';

import {
  CierreMensualCartera,
  CierreMensualCarteraApi
} from './cierre-mensual-cartera.api';

import {
  ProcesamientoCierreComponent
} from './procesamiento/procesamiento-cierre.component';

import {
  ValidacionCierreComponent
} from './validacion/validacion-cierre.component';

import {
  CuadreCierreComponent
} from './cuadre/cuadre-cierre.component';


// =========================================================
// COMPONENTE
// =========================================================

@Component({
  selector: 'app-cierre-mensual-cartera',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
    ProcesamientoCierreComponent,
    ValidacionCierreComponent,
    CuadreCierreComponent
  ],

  templateUrl:
    './cierre-mensual-cartera.component.html',

  styleUrl:
    './cierre-mensual-cartera.component.scss'
})
export class CierreMensualCarteraComponent
  implements OnInit {

  // =========================================================
  // FILTRO / PROCESO
  // =========================================================

  fechaCorte = '';


  // =========================================================
  // RESULTADO DEL ÚLTIMO PROCESO
  // =========================================================

  cierreSeleccionado:
    CierreMensualCartera | null = null;


  // =========================================================
  // HISTÓRICO DE CIERRES
  // =========================================================

  cierres:
    CierreMensualCartera[] = [];


  // =========================================================
  // ESTADOS DE PANTALLA
  // =========================================================

  cargando = false;

  ejecutando = false;

  regenerando = false;

  cerrandoFotografia = false;

  error = '';

  mensaje = '';


  // =========================================================
  // SECCIÓN ACTIVA DEL PROCESO DE CIERRE
  // =========================================================

  seccionCierreActiva:
    'procesamiento'
    | 'validacion'
    | 'cuadre' = 'procesamiento';


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly api:
      CierreMensualCarteraApi
  ) {}


  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {

    this.cargarCierres();

  }


  // =========================================================
  // CARGAR HISTÓRICO
  // =========================================================

  cargarCierres(): void {

    this.cargando = true;

    this.error = '';

    this.api
      .listar()
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({

        next: (data) => {

          this.cierres =
            Array.isArray(data)
              ? data
              : [];

          if (this.cierreSeleccionado) {

            const actualizado =
              this.cierres.find(
                c =>
                  c.idCierreCartera ===
                  this.cierreSeleccionado
                    ?.idCierreCartera
              );

            if (actualizado) {

              this.cierreSeleccionado =
                actualizado;

            }

          }

        },

        error: (err) => {

          console.error(
            'Error cargando cierres de cartera:',
            err
          );

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible consultar los cierres mensuales de cartera.'
            );

        }

      });

  }


  // =========================================================
  // EJECUTAR CIERRE / GENERAR FOTO
  // =========================================================

  ejecutarCierre(): void {

    this.limpiarMensajes();

    if (!this.fechaCorte) {

      this.error =
        'Debe seleccionar la fecha de corte.';

      return;

    }

    if (
      this.ejecutando
      ||
      this.regenerando
      ||
      this.cerrandoFotografia
    ) {
      return;
    }

    this.ejecutando = true;

    this.api
      .ejecutar(
        this.fechaCorte
      )
      .pipe(
        finalize(() => {
          this.ejecutando = false;
        })
      )
      .subscribe({

        next: (cierre) => {

          this.cierreSeleccionado =
            cierre;

          this.mensaje =
            `La fotografía de cartera fue generada correctamente. ` +
            `Créditos fotografiados: ` +
            `${this.formatearEntero(cierre.cantidadCreditos)}.`;

          this.cargarCierres();

        },

        error: (err) => {

          console.error(
            'Error ejecutando cierre mensual de cartera:',
            err
          );

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible generar la fotografía mensual de cartera.'
            );

        }

      });

  }


  // =========================================================
  // REGENERAR FOTO
  // =========================================================

  regenerarFoto(
    cierre?: CierreMensualCartera | null
  ): void {

    this.limpiarMensajes();

    const cierreProceso =
      cierre
      ?? this.cierreSeleccionado
      ?? this.obtenerCierreFecha();

    if (!cierreProceso) {

      this.error =
        'Debe seleccionar un cierre para regenerar la fotografía.';

      return;

    }

    if (!this.puedeRegenerar(cierreProceso)) {

      this.error =
        'La fotografía solamente puede regenerarse mientras no se encuentre en firme.';

      return;

    }

    if (
      this.ejecutando
      ||
      this.regenerando
      ||
      this.cerrandoFotografia
    ) {
      return;
    }

    const confirmar =
      window.confirm(
        'Se eliminará la fotografía actual y la base de '
        + 'cálculos comunes del cierre '
        + this.formatearFecha(
          cierreProceso.fechaCorte
        )
        + ' y se generarán nuevamente.\n\n'
        + '¿Desea continuar?'
      );

    if (!confirmar) {
      return;
    }

    this.regenerando = true;

    this.mensaje =
      `Regenerando fotografía del cierre ` +
      `${this.formatearFecha(cierreProceso.fechaCorte)}. ` +
      `Este proceso puede tardar unos segundos...`;

    this.api
      .regenerar(
        cierreProceso.idCierreCartera
      )
      .pipe(
        finalize(() => {
          this.regenerando = false;
        })
      )
      .subscribe({

        next: (cierreActualizado) => {

          this.cierreSeleccionado =
            cierreActualizado;

          this.fechaCorte =
            cierreActualizado.fechaCorte;

          this.mensaje =
            `La fotografía fue regenerada correctamente. ` +
            `Créditos fotografiados: ` +
            `${this.formatearEntero(
              cierreActualizado.cantidadCreditos
            )}.`;

          this.cargarCierres();

        },

        error: (err) => {

          console.error(
            'Error regenerando fotografía de cartera:',
            err
          );

          this.mensaje = '';

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible regenerar la fotografía de cartera.'
            );

        }

      });

  }


  // =========================================================
  // CERRAR FOTOGRAFÍA EN FIRME
  // =========================================================

  cerrarFotografia(
    cierre?: CierreMensualCartera | null
  ): void {

    this.limpiarMensajes();

    const cierreProceso =
      cierre
      ?? this.cierreSeleccionado
      ?? this.obtenerCierreFecha();

    if (!cierreProceso) {

      this.error =
        'Debe seleccionar un cierre.';

      return;

    }

    const estado =
      (cierreProceso.estadoFotografia ?? '')
        .trim()
        .toUpperCase();

    if (estado === 'C') {

      this.error =
        'La fotografía ya se encuentra en firme.';

      return;

    }

    if (estado !== 'E') {

      this.error =
        'Debe generar la fotografía antes de dejarla en firme.';

      return;

    }

    if (
      this.ejecutando
      ||
      this.regenerando
      ||
      this.cerrandoFotografia
    ) {
      return;
    }

    const confirmar =
      window.confirm(
        'La fotografía quedará cerrada en firme y ya no podrá regenerarse.\n\n'
        + '¿Desea continuar?'
      );

    if (!confirmar) {
      return;
    }

    this.cerrandoFotografia = true;

    this.api
      .cerrarFotografia(
        cierreProceso.idCierreCartera
      )
      .pipe(
        finalize(() => {
          this.cerrandoFotografia = false;
        })
      )
      .subscribe({

        next: (actualizado) => {

          this.cierreSeleccionado =
            actualizado;

          this.fechaCorte =
            actualizado.fechaCorte;

          this.mensaje =
            'La fotografía quedó cerrada en firme correctamente.';

          this.cargarCierres();

        },

        error: (err) => {

          console.error(
            'Error cerrando fotografía de cartera:',
            err
          );

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible cerrar la fotografía en firme.'
            );

        }

      });

  }


  // =========================================================
  // VALIDAR SI PUEDE REGENERAR
  // =========================================================

  puedeRegenerar(
    cierre: CierreMensualCartera | null | undefined
  ): boolean {

    if (!cierre) {
      return false;
    }

    const estado =
      (cierre.estadoFotografia ?? '')
        .trim()
        .toUpperCase();

    return estado === 'P' || estado === 'E';

  }


  // =========================================================
  // VALIDAR SI LA FOTOGRAFÍA ESTÁ EN FIRME
  // =========================================================

  fotografiaEnFirme(
    cierre: CierreMensualCartera | null | undefined
  ): boolean {

    return (
      (cierre?.estadoFotografia ?? '')
        .trim()
        .toUpperCase() === 'C'
    );

  }


  // =========================================================
  // SELECCIONAR CIERRE DEL HISTÓRICO
  // =========================================================

  seleccionarCierre(
    cierre: CierreMensualCartera
  ): void {

    this.limpiarMensajes();

    this.cierreSeleccionado =
      cierre;

    this.fechaCorte =
      cierre.fechaCorte ?? '';

  }


  // =========================================================
  // REFRESCAR
  // =========================================================

  refrescar(): void {

    this.limpiarMensajes();

    this.cargarCierres();

  }


  // =========================================================
  // LIMPIAR SELECCIÓN
  // =========================================================

  limpiar(): void {

    if (
      this.ejecutando
      ||
      this.regenerando
      ||
      this.cerrandoFotografia
    ) {
      return;
    }

    this.fechaCorte = '';

    this.cierreSeleccionado = null;

    this.limpiarMensajes();

  }


  // =========================================================
  // VALIDAR SI YA EXISTE CIERRE PARA LA FECHA
  // =========================================================

  existeCierreFecha(): boolean {

    if (!this.fechaCorte) {
      return false;
    }

    return this.cierres.some(
      cierre =>
        cierre.fechaCorte ===
        this.fechaCorte
    );

  }


  // =========================================================
  // OBTENER CIERRE DE LA FECHA SELECCIONADA
  // =========================================================

  obtenerCierreFecha():
    CierreMensualCartera | null {

    if (!this.fechaCorte) {
      return null;
    }

    return this.cierres.find(
      cierre =>
        cierre.fechaCorte ===
        this.fechaCorte
    ) ?? null;

  }


  // =========================================================
  // TEXTO DEL ESTADO GENERAL
  // =========================================================

  descripcionEstado(
    estado: string | null | undefined
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
        return estado || '';

    }

  }


  // =========================================================
  // VALIDAR SI TIENE FOTO
  // =========================================================

  tieneFoto(
    cierre: CierreMensualCartera
  ): boolean {

    return !!cierre.fechaFotografia;

  }


  // =========================================================
  // FORMATO FECHA
  // =========================================================

  formatearFecha(
    fecha: string | null | undefined
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

    return `${partes[2]}/${partes[1]}/${partes[0]}`;

  }


  // =========================================================
  // FORMATO FECHA Y HORA
  // =========================================================

  formatearFechaHora(
    fecha: string | null | undefined
  ): string {

    if (!fecha) {
      return '';
    }

    const date =
      new Date(fecha);

    if (
      Number.isNaN(
        date.getTime()
      )
    ) {
      return fecha;
    }

    return date.toLocaleString(
      'es-CO',
      {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      }
    );

  }


  // =========================================================
  // FORMATO ENTERO
  // =========================================================

  formatearEntero(
    valor: number | null | undefined
  ): string {

    return new Intl.NumberFormat(
      'es-CO',
      {
        maximumFractionDigits: 0
      }
    ).format(
      Number(valor ?? 0)
    );

  }


  // =========================================================
  // FORMATO MONEDA
  // =========================================================

  formatearMoneda(
    valor: number | null | undefined
  ): string {

    return new Intl.NumberFormat(
      'es-CO',
      {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }
    ).format(
      Number(valor ?? 0)
    );

  }


  // =========================================================
  // TRACK BY
  // =========================================================

  trackByCierre(
    _index: number,
    cierre: CierreMensualCartera
  ): number {

    return cierre.idCierreCartera;

  }


  // =========================================================
  // NOMBRE DEL MES DEL CIERRE
  // =========================================================

  nombreMesCierre(
    fecha: string | null | undefined
  ): string {

    if (!fecha) {
      return '';
    }

    const partes =
      fecha.substring(0, 10).split('-');

    if (partes.length !== 3) {
      return '';
    }

    const anio =
      Number(partes[0]);

    const mes =
      Number(partes[1]);

    const meses = [
      '',
      'Enero',
      'Febrero',
      'Marzo',
      'Abril',
      'Mayo',
      'Junio',
      'Julio',
      'Agosto',
      'Septiembre',
      'Octubre',
      'Noviembre',
      'Diciembre'
    ];

    if (mes < 1 || mes > 12) {
      return '';
    }

    return `${meses[mes]} de ${anio}`;

  }


  // =========================================================
  // CAMBIAR SECCIÓN DEL PROCESO DE CIERRE
  // =========================================================

  cambiarSeccionCierre(
    seccion:
      'procesamiento'
      | 'validacion'
      | 'cuadre'
  ): void {

    this.seccionCierreActiva =
      seccion;

  }


  // =========================================================
  // LIMPIAR MENSAJES
  // =========================================================

  private limpiarMensajes(): void {

    this.error = '';

    this.mensaje = '';

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
