
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  DestroyRef,
  inject,
  signal
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { SessionService } from '../../../core/auth/session.service';

import {
  Formato9013Api,
  Formato9013DTO
} from './formato9013.api';

import {
  Formato9013ExporterService
} from './formato9013-exporter.service';

interface MesContable {
  numero: number;
  nombre: string;
}

interface TotalesFormato9013 {
  registros: number;
  saldoAportes: number;
  valorAporteMensual: number;
  aportesOrdinarios: number;
  aportesExtraordinarios: number;
  valorRevalorizacion: number;
  montoPromedio: number;
  contribuciones: number;
}

@Component({
  selector: 'app-formato9013',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './formato9013.component.html',
  styleUrl: './formato9013.component.scss'
})
export class Formato9013Component {

  readonly sessionSES = inject(SessionService);

  private readonly api = inject(Formato9013Api);
  private readonly exporter = inject(Formato9013ExporterService);
  private readonly destroyRef = inject(DestroyRef);

  readonly cargando = signal(false);
  readonly generando = signal(false);

  readonly mensajeError = signal('');
  readonly mensajeExito = signal('');

  readonly meses: MesContable[] = [
    { numero: 1, nombre: 'Enero' },
    { numero: 2, nombre: 'Febrero' },
    { numero: 3, nombre: 'Marzo' },
    { numero: 4, nombre: 'Abril' },
    { numero: 5, nombre: 'Mayo' },
    { numero: 6, nombre: 'Junio' },
    { numero: 7, nombre: 'Julio' },
    { numero: 8, nombre: 'Agosto' },
    { numero: 9, nombre: 'Septiembre' },
    { numero: 10, nombre: 'Octubre' },
    { numero: 11, nombre: 'Noviembre' },
    { numero: 12, nombre: 'Diciembre' }
  ];

  anoSeleccionado: number;
  mesSeleccionado: number;

  items: Formato9013DTO[] = [];

  totales: TotalesFormato9013 =
    this.crearTotalesVacios();

  private fechaConsultada = '';

  constructor() {
    const hoy = new Date();

    const mesAnterior = new Date(
      hoy.getFullYear(),
      hoy.getMonth() - 1,
      1
    );

    this.anoSeleccionado =
      mesAnterior.getFullYear();

    this.mesSeleccionado =
      mesAnterior.getMonth() + 1;
  }

  /**
   * Consulta el Formato 9013 para el período
   * seleccionado.
   *
   * El backend obtiene los saldos del cierre
   * mensual definitivo de Depósitos.
   */
  buscar(): void {

    if (this.cargando() || this.generando()) {
      return;
    }

    this.limpiarMensajes();

    if (
      !this.sessionSES.tienePermiso(
        'SES_FORMATOS_VIEW'
      )
    ) {
      this.mensajeError.set(
        'No tiene permisos para consultar formatos SES.'
      );
      return;
    }

    if (!this.validarPeriodo()) {
      return;
    }

    const fechaCorte = this.obtenerFechaCorte();

    this.limpiarResultados();

    this.cargando.set(true);

    this.api.consultar(fechaCorte)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cargando.set(false))
      )
      .subscribe({
        next: (respuesta: Formato9013DTO[]) => {

          this.items = respuesta ?? [];

          this.fechaConsultada = fechaCorte;

          this.totales =
            this.calcularTotales(this.items);

          if (this.items.length === 0) {
            this.mensajeExito.set(
              'La consulta finalizó sin registros de aportes sociales.'
            );
            return;
          }

          this.mensajeExito.set(
            `Consulta finalizada. ${this.items.length} asociados encontrados.`
          );
        },

        error: (error: HttpErrorResponse) => {

          this.limpiarResultados();

          void this.procesarError(error);
        }
      });
  }

  /**
   * Exporta los resultados vigentes a Excel.
   * Valida los permisos y la existencia de registros.
   */
  exportarExcel(): void {

    if (!this.validarExportacion()) {
      return;
    }

    this.limpiarMensajes();
    this.generando.set(true);

    try {

      this.exporter.exportarExcel(
        this.items,
        this.fechaConsultada
      );

      this.mensajeExito.set(
        'Se generó el archivo Excel del Formato 9013.'
      );

    } catch (error: unknown) {

      this.mensajeError.set(
        this.obtenerMensajeExportacion(error)
      );

    } finally {

      this.generando.set(false);
    }
  }

  /**
   * Genera el archivo plano TXT del Formato 9013.
   * Utiliza los registros de la consulta vigente.
   */
  generarPlano(): void {

    if (!this.validarExportacion()) {
      return;
    }

    this.limpiarMensajes();
    this.generando.set(true);

    try {

      this.exporter.exportarPlano(
        this.items,
        this.fechaConsultada
      );

      this.mensajeExito.set(
        'Se generó el archivo TXT del Formato 9013.'
      );

    } catch (error: unknown) {

      this.mensajeError.set(
        this.obtenerMensajeExportacion(error)
      );

    } finally {

      this.generando.set(false);
    }
  }

  /**
   * Indica si hay resultados disponibles para exportar.
   * Se utiliza para habilitar los botones de la pantalla.
   */
  get puedeGenerar(): boolean {

    return (
      this.consultaVigente &&
      this.items.length > 0 &&
      !this.cargando() &&
      !this.generando() &&
      this.sessionSES.tienePermiso(
        'SES_FORMATOS_GENERAR'
      )
    );
  }

  /**
   * Valida los permisos, el período consultado
   * y la disponibilidad de resultados.
   */
  private validarExportacion(): boolean {

    if (this.cargando() || this.generando()) {
      return false;
    }

    if (
      !this.sessionSES.tienePermiso(
        'SES_FORMATOS_GENERAR'
      )
    ) {
      this.mensajeError.set(
        'No tiene permisos para generar formatos SES.'
      );
      return false;
    }

    if (!this.consultaVigente) {
      this.mensajeError.set(
        'Debe consultar el período antes de exportar.'
      );
      return false;
    }

    if (this.items.length === 0) {
      this.mensajeError.set(
        'No existen registros para exportar.'
      );
      return false;
    }

    return true;
  }

  /**
   * Obtiene un mensaje legible cuando falla
   * la preparación de una exportación.
   */
  private obtenerMensajeExportacion(
    error: unknown
  ): string {

    if (error instanceof Error) {
      return error.message;
    }

    return 'No fue posible generar el archivo solicitado.';
  }

  /**
   * Limpia los resultados sin modificar
   * el período seleccionado.
   */
  limpiar(): void {

    if (this.cargando() || this.generando()) {
      return;
    }

    this.limpiarResultados();
    this.limpiarMensajes();
  }

  /**
   * Invalida la consulta cuando cambia
   * el año o el mes.
   */
  cambiarPeriodo(): void {

    if (this.cargando() || this.generando()) {
      return;
    }

    this.limpiarResultados();
    this.limpiarMensajes();
  }

  /**
   * Genera la fecha correspondiente al último
   * día calendario del mes seleccionado.
   *
   * Formato: yyyy-MM-dd.
   */
  obtenerFechaCorte(): string {

    const ano = Number(this.anoSeleccionado);
    const mes = Number(this.mesSeleccionado);

    const ultimoDia = new Date(
      ano,
      mes,
      0
    ).getDate();

    return (
      String(ano).padStart(4, '0') +
      '-' +
      String(mes).padStart(2, '0') +
      '-' +
      String(ultimoDia).padStart(2, '0')
    );
  }

  /**
   * Presenta la fecha de corte en formato
   * colombiano dd/MM/yyyy.
   */
  obtenerFechaCorteVisual(): string {

    if (!this.validarPeriodoSinMensaje()) {
      return '';
    }

    const fecha = this.obtenerFechaCorte();

    const partes = fecha.split('-');

    return (
      partes[2] +
      '/' +
      partes[1] +
      '/' +
      partes[0]
    );
  }

  /**
   * Verifica si los resultados corresponden
   * al período actualmente seleccionado.
   */
  get consultaVigente(): boolean {

    if (!this.validarPeriodoSinMensaje()) {
      return false;
    }

    return (
      this.fechaConsultada ===
      this.obtenerFechaCorte()
    );
  }

  /**
   * Calcula los totales financieros de
   * los asociados consultados.
   *
   * Los valores nulos no se convierten
   * en cifras individuales ficticias.
   * Solo se omiten de las sumatorias.
   */
  private calcularTotales(
    registros: Formato9013DTO[]
  ): TotalesFormato9013 {

    const resultado =
      this.crearTotalesVacios();

    resultado.registros = registros.length;

    for (const registro of registros) {

      resultado.saldoAportes +=
        registro.saldoAportes ?? 0;

      resultado.valorAporteMensual +=
        registro.valorAporteMensual ?? 0;

      resultado.aportesOrdinarios +=
        registro.aportesOrdinarios ?? 0;

      resultado.aportesExtraordinarios +=
        registro.aportesExtraordinarios ?? 0;

      resultado.valorRevalorizacion +=
        registro.valorRevalorizacion ?? 0;

      resultado.montoPromedio +=
        registro.montoPromedio ?? 0;

      resultado.contribuciones +=
        registro.contribuciones ?? 0;
    }

    return resultado;
  }

  /**
   * Inicializa las estadísticas financieras.
   */
  private crearTotalesVacios(): TotalesFormato9013 {

    return {
      registros: 0,
      saldoAportes: 0,
      valorAporteMensual: 0,
      aportesOrdinarios: 0,
      aportesExtraordinarios: 0,
      valorRevalorizacion: 0,
      montoPromedio: 0,
      contribuciones: 0
    };
  }

  /**
   * Valida el año, mes y fecha futura.
   */
  private validarPeriodo(): boolean {

    const ano = Number(this.anoSeleccionado);
    const mes = Number(this.mesSeleccionado);

    if (
      !Number.isInteger(ano) ||
      ano < 1900 ||
      ano > 9999
    ) {
      this.mensajeError.set(
        'Seleccione un año válido.'
      );
      return false;
    }

    if (
      !Number.isInteger(mes) ||
      mes < 1 ||
      mes > 12
    ) {
      this.mensajeError.set(
        'Seleccione un mes válido.'
      );
      return false;
    }

    const hoy = new Date();

    const periodoSeleccionado =
      ano * 100 + mes;

    const periodoActual =
      hoy.getFullYear() * 100 +
      hoy.getMonth() + 1;

    if (periodoSeleccionado > periodoActual) {
      this.mensajeError.set(
        'No se puede consultar un período futuro.'
      );
      return false;
    }

    return true;
  }

  /**
   * Valida el período sin modificar
   * los mensajes de la pantalla.
   */
  private validarPeriodoSinMensaje(): boolean {

    const ano = Number(this.anoSeleccionado);
    const mes = Number(this.mesSeleccionado);

    return (
      Number.isInteger(ano) &&
      ano >= 1900 &&
      ano <= 9999 &&
      Number.isInteger(mes) &&
      mes >= 1 &&
      mes <= 12
    );
  }

  /**
   * Elimina los datos y sus acumulados.
   */
  private limpiarResultados(): void {

    this.items = [];

    this.totales =
      this.crearTotalesVacios();

    this.fechaConsultada = '';
  }

  /**
   * Limpia los mensajes de la pantalla.
   */
  limpiarMensajes(): void {

    this.mensajeError.set('');
    this.mensajeExito.set('');
  }

  /**
   * Procesa los errores HTTP enviados
   * por el backend.
   */
  private async procesarError(
    error: HttpErrorResponse
  ): Promise<void> {

    let mensaje =
      'No fue posible consultar el Formato 9013.';

    if (error.status === 0) {

      mensaje =
        'No fue posible establecer comunicación con el servidor.';

    } else if (error.status === 400) {

      mensaje =
        'La fecha de corte no es válida o no corresponde a un cierre mensual.';

    } else if (error.status === 401) {

      mensaje =
        'La sesión ha expirado. Ingrese nuevamente.';

    } else if (error.status === 403) {

      mensaje =
        'No tiene permisos para consultar el Formato 9013.';

    } else if (error.status === 404) {

      mensaje =
        'No se encontró información para el período seleccionado.';

    } else if (error.status >= 500) {

      mensaje =
        'Se presentó un error al procesar la información financiera.';
    }

    if (error.error instanceof Blob) {

      try {

        const contenido =
          await error.error.text();

        const respuesta =
          JSON.parse(contenido);

        mensaje =
          respuesta.message ||
          respuesta.mensaje ||
          mensaje;

      } catch {

        // Conserva el mensaje HTTP.
      }

    } else if (
      error.error &&
      typeof error.error === 'object'
    ) {

      mensaje =
        error.error.message ||
        error.error.mensaje ||
        mensaje;
    }

    this.mensajeError.set(mensaje);
  }
}
