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
  Formato9026Api,
  Formato9026DTO
} from './formato9026.api';

import {
  Formato9026ExporterService
} from './formato9026-exporter.service';

/**
 * Mes disponible para seleccionar el período
 * del informe.
 */
interface MesContable {
  numero: number;
  nombre: string;
}

/**
 * Totales del Formato SES 9026.
 *
 * Separa los productos de Depósitos y CDAT
 * y conserva los valores consolidados.
 */
interface TotalesFormato9026 {
  registros: number;
  cuentasAhorro: number;
  cdats: number;
  saldoDepositos: number;
  saldoCdats: number;
  saldoTotal: number;
  depositoInicialTotal: number;
  interesesCausadosTotal: number;
}

@Component({
  selector: 'app-formato9026',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './formato9026.component.html',
  styleUrl: './formato9026.component.scss'
})
export class Formato9026Component {

  readonly sessionSES = inject(SessionService);

  private readonly api = inject(Formato9026Api);
  private readonly exporter = inject(Formato9026ExporterService);
  private readonly destroyRef = inject(DestroyRef);

  readonly cargando = signal(false);
  readonly generando = signal(false);

  readonly mensajeError = signal('');
  readonly mensajeExito = signal('');

  readonly mostrarDetalle = signal(false);

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

  items: Formato9026DTO[] = [];

  totales: TotalesFormato9026 =
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
   * Consulta el Formato SES 9026 para el período
   * seleccionado.
   *
   * El backend obtiene la información histórica
   * de los cierres mensuales de Depósitos y CDAT.
   *
   * No consulta los saldos actuales de las cuentas.
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

        next: (respuesta: Formato9026DTO[]) => {

          this.items = respuesta ?? [];

          this.fechaConsultada = fechaCorte;

          this.totales =
            this.calcularTotales(this.items);

          if (this.items.length === 0) {
            this.mensajeExito.set(
              'La consulta finalizó sin registros de ahorros o CDAT.'
            );
            return;
          }

          this.mensajeExito.set(
            'Consulta finalizada. ' +
            `${this.items.length} registros encontrados: ` +
            `${this.totales.cuentasAhorro} cuentas de ahorro y ` +
            `${this.totales.cdats} CDAT.`
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
   *
   * Incluye los registros consolidados de
   * Depósitos y CDAT y el resumen financiero.
   *
   * Valida el permiso SES_FORMATOS_GENERAR.
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
        'Se generó el archivo Excel del Formato 9026.'
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
   * Genera el archivo plano TXT del Formato 9026.
   *
   * Utiliza los resultados de la consulta vigente.
   *
   * El exportador valida las 26 columnas SES
   * y los campos regulatorios obligatorios.
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
        'Se generó el archivo TXT del Formato 9026.'
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
   * Determina si existen resultados disponibles
   * para exportar.
   *
   * Verifica:
   * - Consulta correspondiente al período actual.
   * - Existencia de registros.
   * - Ausencia de operaciones en ejecución.
   * - Permiso de generación SES.
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
   * Valida los requisitos para generar archivos.
   *
   * Evita exportar información de un período
   * diferente al seleccionado.
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
   * Obtiene un mensaje legible cuando ocurre
   * un error durante la exportación.
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
   * Limpia los resultados y mensajes.
   *
   * Conserva el año y mes seleccionados.
   */
  limpiar(): void {

    if (this.cargando() || this.generando()) {
      return;
    }

    this.limpiarResultados();
    this.limpiarMensajes();
    this.mostrarDetalle.set(false);
  }

  /**
   * Invalida la consulta cuando el usuario
   * modifica el año o el mes.
   *
   * Impide exportar resultados anteriores
   * con una fecha de corte diferente.
   */
  cambiarPeriodo(): void {

    if (this.cargando() || this.generando()) {
      return;
    }

    this.limpiarResultados();
    this.limpiarMensajes();
  }

  /**
   * Obtiene el último día calendario
   * del mes seleccionado.
   *
   * Formato de salida: yyyy-MM-dd.
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
   *
   * Se utiliza en el encabezado y filtros.
   */
  obtenerFechaCorteVisual(): string {

    if (!this.validarPeriodoSinMensaje()) {
      return '';
    }

    const partes =
      this.obtenerFechaCorte().split('-');

    return (
      partes[2] +
      '/' +
      partes[1] +
      '/' +
      partes[0]
    );
  }

  /**
   * Comprueba que los resultados correspondan
   * al período seleccionado actualmente.
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
   * Calcula las estadísticas del Formato 9026.
   *
   * Separa los registros según el módulo de origen:
   * - DEPOSITOS: cuentas de ahorro.
   * - CDAT: certificados de depósito a término.
   *
   * Los saldos provienen de los cierres históricos.
   * Este método únicamente realiza sumatorias.
   */
  private calcularTotales(
    registros: Formato9026DTO[]
  ): TotalesFormato9026 {

    const resultado =
      this.crearTotalesVacios();

    resultado.registros = registros.length;

    for (const registro of registros) {

      const saldo = registro.saldo ?? 0;

      resultado.depositoInicialTotal +=
        registro.depositoInicial ?? 0;

      resultado.interesesCausadosTotal +=
        registro.interesesCausados ?? 0;

      if (registro.moduloOrigen === 'DEPOSITOS') {

        resultado.cuentasAhorro++;

        resultado.saldoDepositos += saldo;

      } else if (registro.moduloOrigen === 'CDAT') {

        resultado.cdats++;

        resultado.saldoCdats += saldo;
      }

      resultado.saldoTotal += saldo;
    }

    return resultado;
  }

  /**
   * Inicializa las estadísticas del informe.
   *
   * Se utiliza al crear el componente
   * y al limpiar una consulta.
   */
  private crearTotalesVacios(): TotalesFormato9026 {

    return {
      registros: 0,
      cuentasAhorro: 0,
      cdats: 0,
      saldoDepositos: 0,
      saldoCdats: 0,
      saldoTotal: 0,
      depositoInicialTotal: 0,
      interesesCausadosTotal: 0
    };
  }

  /**
   * Valida el período antes de consultar.
   *
   * Comprueba:
   * - Año válido.
   * - Mes entre 1 y 12.
   * - Período no futuro.
   * - Fecha de cierre no futura.
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

    const fechaCorte = new Date(
      ano,
      mes,
      0
    );

    if (fechaCorte > hoy) {
      this.mensajeError.set(
        'La fecha de corte todavía no ha finalizado.'
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
   * Elimina los registros y sus acumulados.
   *
   * Invalida las exportaciones del período anterior
   * y oculta la tabla de detalle.
   */
  private limpiarResultados(): void {

    this.items = [];

    this.totales =
      this.crearTotalesVacios();

    this.fechaConsultada = '';

    this.mostrarDetalle.set(false);
  }

  /**
   * Limpia los mensajes informativos
   * y de error de la pantalla.
   */
  limpiarMensajes(): void {

    this.mensajeError.set('');
    this.mensajeExito.set('');
  }

  /**
   * Procesa los errores HTTP del backend.
   *
   * Presenta mensajes específicos para:
   * - Error de conexión.
   * - Fecha o cierre inválido.
   * - Sesión expirada.
   * - Permisos insuficientes.
   * - Información no encontrada.
   * - Errores internos del servidor.
   *
   * Si el backend devuelve un mensaje
   * de negocio, se presenta al usuario.
   */
  private async procesarError(
    error: HttpErrorResponse
  ): Promise<void> {

    let mensaje =
      'No fue posible consultar el Formato 9026.';

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
        'No tiene permisos para consultar el Formato 9026.';

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

        // Conserva el mensaje HTTP correspondiente.
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

  /**
   * Muestra u oculta el detalle de ahorros y CDAT.
   * No realiza consultas adicionales al backend.
   */
  alternarDetalle(): void {
    this.mostrarDetalle.update(valor => !valor);
  }

}
