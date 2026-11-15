
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
import { finalize, switchMap } from 'rxjs';

import { SessionService } from '../../../core/auth/session.service';

import {
  Formato9999Api,
  AsociadoSES,
  FormatoAsociadosSES
} from './formato9999.api';

import {
  Formato9999ExporterService
} from './formato9999-exporter.service';

interface MesContable {
  numero: number;
  nombre: string;
}

interface EstadisticaAsociados {
  masculino: number;
  femenino: number;
  juridica: number;
  otro: number;
  total: number;
}

@Component({
  selector: 'app-formato9999',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './formato9999.component.html',
  styleUrl: './formato9999.component.scss'
})
export class Formato9999Component {

  readonly sessionSES = inject(SessionService);

  private readonly api = inject(Formato9999Api);
  private readonly exporter = inject(Formato9999ExporterService);
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

  items: AsociadoSES[] = [];
  estadistica: EstadisticaAsociados | null = null;

  private fechaConsultada = '';

  constructor() {
    const hoy = new Date();

    const mesAnterior = new Date(
      hoy.getFullYear(),
      hoy.getMonth() - 1,
      1
    );

    this.anoSeleccionado = mesAnterior.getFullYear();
    this.mesSeleccionado = mesAnterior.getMonth() + 1;
  }

  /**
   * Consulta los asociados a la fecha de corte.
   */
  buscar(): void {
    if (this.cargando() || this.generando()) {
      return;
    }

    this.limpiarMensajes();

    if (!this.validarPeriodo()) {
      return;
    }

    const fechaCorte = this.obtenerFechaCorte();

    this.cargando.set(true);

    this.api.consultar(fechaCorte)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cargando.set(false))
      )
      .subscribe({
        next: (respuesta: AsociadoSES[]) => {
          this.items = respuesta ?? [];
          this.fechaConsultada = fechaCorte;

          this.estadistica =
            this.calcularEstadisticas(this.items);

          this.mensajeExito.set(
            `Consulta finalizada. ${this.items.length} asociados encontrados.`
          );
        },
        error: (error: HttpErrorResponse) => {
          this.items = [];
          this.estadistica = null;
          this.fechaConsultada = '';

          void this.procesarError(error);
        }
      });
  }

  /**
   * Genera el archivo plano TXT de Asociados.
   */
  generarPlano(): void {
    if (!this.sessionSES.tienePermiso('SES_FORMATOS_GENERAR')) {
      this.mensajeError.set(
        'No tiene permisos para generar formatos SES.'
      );
      return;
    }

    if (this.cargando() || this.generando()) {
      return;
    }

    this.limpiarMensajes();

    if (!this.validarPeriodo()) {
      return;
    }

    this.obtenerDatosParaExportar('plano');
  }

  /**
   * Genera el Excel de revisión de Asociados.
   */
  exportarExcel(): void {
    if (!this.sessionSES.tienePermiso('SES_FORMATOS_GENERAR')) {
      this.mensajeError.set(
        'No tiene permisos para exportar formatos SES.'
      );
      return;
    }

    if (this.cargando() || this.generando()) {
      return;
    }

    this.limpiarMensajes();

    if (!this.validarPeriodo()) {
      return;
    }

    this.obtenerDatosParaExportar('excel');
  }

  /**
   * Reutiliza los datos consultados cuando
   * corresponden al período seleccionado.
   */
  private obtenerDatosParaExportar(
    tipo: 'plano' | 'excel'
  ): void {
    const fechaCorte = this.obtenerFechaCorte();

    if (
      this.fechaConsultada === fechaCorte &&
      this.items.length > 0
    ) {
      this.consultarFormatoYExportar(tipo);
      return;
    }

    this.generando.set(true);

    this.api.consultar(fechaCorte)
      .pipe(
        switchMap((respuesta: AsociadoSES[]) => {
          this.items = respuesta ?? [];
          this.fechaConsultada = fechaCorte;

          this.estadistica =
            this.calcularEstadisticas(this.items);

          if (this.items.length === 0) {
            throw new Error(
              'No existen asociados para el período seleccionado.'
            );
          }

          return this.api.consultarFormato();
        }),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.generando.set(false))
      )
      .subscribe({
        next: (formato: FormatoAsociadosSES) => {
          this.ejecutarExportacion(
            tipo,
            formato.codigoFormato
          );
        },
        error: (error: unknown) => {
          this.mostrarErrorExportacion(error);
        }
      });
  }

  /**
   * Consulta el código oficial del formato.
   */
  private consultarFormatoYExportar(
    tipo: 'plano' | 'excel'
  ): void {
    this.generando.set(true);

    this.api.consultarFormato()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.generando.set(false))
      )
      .subscribe({
        next: (formato: FormatoAsociadosSES) => {
          this.ejecutarExportacion(
            tipo,
            formato.codigoFormato
          );
        },
        error: (error: unknown) => {
          this.mostrarErrorExportacion(error);
        }
      });
  }

  /**
   * Entrega los datos al exportador correspondiente.
   */
  private ejecutarExportacion(
    tipo: 'plano' | 'excel',
    codigoFormato: string
  ): void {
    if (this.items.length === 0) {
      this.mensajeError.set(
        'No existen asociados para el período seleccionado.'
      );
      return;
    }

    if (!codigoFormato?.trim()) {
      this.mensajeError.set(
        'No se encontró el código del formato SES.'
      );
      return;
    }

    try {
      if (tipo === 'plano') {
        this.exporter.exportarPlano(
          this.items,
          codigoFormato
        );

        this.mensajeExito.set(
          'Archivo plano generado correctamente.'
        );
      } else {
        this.exporter.exportar(
          this.items,
          this.estadistica,
          codigoFormato
        );

        this.mensajeExito.set(
          'Archivo Excel generado correctamente.'
        );
      }
    } catch (error: unknown) {
      this.mostrarErrorExportacion(error);
    }
  }

  /**
   * Presenta errores de consulta y exportación.
   */
  private mostrarErrorExportacion(
    error: unknown
  ): void {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 404) {
        this.mensajeError.set(
          'No existe un formato activo de Asociados en Supersolidaria.'
        );
        return;
      }

      void this.procesarError(error);
      return;
    }

    this.mensajeError.set(
      error instanceof Error
        ? error.message
        : 'No fue posible generar el archivo.'
    );
  }

  /**
   * Obtiene la fecha de corte yyyy-MM-dd.
   */
  private obtenerFechaCorte(): string {
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
   * Valida el período seleccionado.
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

    const periodoSeleccionado = ano * 100 + mes;

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
   * Calcula estadísticas por género.
   */
  private calcularEstadisticas(
    lista: AsociadoSES[]
  ): EstadisticaAsociados {
    const masculino = lista.filter(
      asociado => Number(asociado.genero) === 1
    ).length;

    const femenino = lista.filter(
      asociado => Number(asociado.genero) === 2
    ).length;

    const juridica = lista.filter(
      asociado => Number(asociado.genero) === 3
    ).length;

    const total = lista.length;

    const otro =
      total - masculino - femenino - juridica;

    return {
      masculino,
      femenino,
      juridica,
      otro,
      total
    };
  }

  /**
   * Limpia la consulta y los resultados.
   */
  limpiar(): void {
    if (this.cargando() || this.generando()) {
      return;
    }

    this.items = [];
    this.estadistica = null;
    this.fechaConsultada = '';

    this.limpiarMensajes();
  }

  /**
   * Invalida datos cuando cambia el período.
   */
  cambiarPeriodo(): void {
    this.items = [];
    this.estadistica = null;
    this.fechaConsultada = '';

    this.limpiarMensajes();
  }

  /**
   * Limpia mensajes.
   */
  limpiarMensajes(): void {
    this.mensajeError.set('');
    this.mensajeExito.set('');
  }

  /**
   * Procesa los errores HTTP.
   */
  private async procesarError(
    error: HttpErrorResponse
  ): Promise<void> {
    let mensaje =
      'No fue posible consultar los asociados.';

    if (error.status === 0) {
      mensaje =
        'No fue posible establecer comunicación con el servidor.';
    } else if (error.status === 401) {
      mensaje =
        'La sesión ha expirado. Ingrese nuevamente.';
    } else if (error.status === 403) {
      mensaje =
        'No tiene permisos para consultar Asociados SES.';
    } else if (error.status === 400) {
      mensaje =
        'La fecha de corte seleccionada no es válida.';
    }

    if (error.error instanceof Blob) {
      try {
        const contenido = await error.error.text();
        const respuesta = JSON.parse(contenido);

        mensaje =
          respuesta.message ||
          respuesta.mensaje ||
          mensaje;
      } catch {
        // Se conserva el mensaje HTTP.
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
