import localeEsCo from '@angular/common/locales/es-CO';
import { registerLocaleData } from '@angular/common';
registerLocaleData(localeEsCo);

import { CommonModule } from '@angular/common';
import {
  HttpErrorResponse,
  HttpResponse
} from '@angular/common/http';
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
import { ConsultaFormato3, Formato3Api, PeriodoFormato3 } from './formato3.api';
import { Formato3ExporterService } from './formato3-exporter.service';

@Component({
  selector: 'app-formato3',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './formato3.component.html',
  styleUrl: './formato3.component.scss'
})
export class Formato3Component {

  readonly sessionSES = inject(SessionService);

  private readonly api = inject(Formato3Api);
  private readonly exporter = inject(Formato3ExporterService);
  private readonly destroyRef = inject(DestroyRef);

  readonly generando = signal(false);
  readonly consultando = signal(false);
  readonly periodos = signal<PeriodoFormato3[]>([]);
  readonly consulta = signal<ConsultaFormato3 | null>(null);
  filtroDetalle = '';
  get detalleFiltrado() {
    const filtro = this.filtroDetalle.trim().toLowerCase();
    return (this.consulta()?.detalle ?? []).filter(r =>
      !filtro || r.codigoRenglon.toLowerCase().includes(filtro) || r.descripcion.toLowerCase().includes(filtro)
    );
  }
  get resumenClases() {
    const detalle = this.consulta()?.detalle ?? [];
    return ['1', '2', '3', '4', '5', '6'].map(clase => ({
      clase,
      saldo: detalle.find(r => r.codigoRenglon === clase.padEnd(6, '0'))?.saldo ?? 0
    }));
  }

  // El resultado ya viene calculado por el backend en el renglón SES 350500.
  // No se recalculan ingresos, gastos ni costos en el frontend.
  get excedenteEjercicio(): number {
    return this.consulta()?.detalle.find(r => r.codigoRenglon === '350500')?.saldo ?? 0;
  }

  readonly mensajeError = signal('');
  readonly mensajeExito = signal('');

  readonly meses = [
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

  ngOnInit(): void {
    this.api.periodos().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: periodos => {
        this.periodos.set(periodos);
        if (periodos.length) {
          this.anoSeleccionado = periodos[0].ano;
          this.mesSeleccionado = periodos[0].mes;
        }
      },
      error: error => { void this.procesarError(error); }
    });
  }

  // Las opciones se derivan de los períodos cerrados que ya entrega el backend.
  get anosDisponibles(): number[] {
    return [...new Set(this.periodos().map(p => p.ano))].sort((a, b) => b - a);
  }

  get mesesDisponibles(): typeof this.meses {
    const disponibles = new Set(
      this.periodos().filter(p => p.ano === this.anoSeleccionado).map(p => p.mes)
    );
    return this.meses.filter(m => disponibles.has(m.numero));
  }

  seleccionarAno(ano: number): void {
    if (!this.anosDisponibles.includes(ano)) return;
    this.anoSeleccionado = ano;
    if (!this.mesesDisponibles.some(m => m.numero === this.mesSeleccionado)) {
      this.mesSeleccionado = this.mesesDisponibles[0]?.numero ?? 1;
    }
    this.consulta.set(null);
    this.filtroDetalle = '';
    this.limpiarMensajes();
  }

  seleccionarMes(mes: number): void {
    if (!this.mesesDisponibles.some(m => m.numero === mes)) return;
    this.mesSeleccionado = mes;
    this.consulta.set(null);
    this.filtroDetalle = '';
    this.limpiarMensajes();
  }

  consultar(): void {
    if (this.consultando() || this.generando()) return;
    this.limpiarMensajes();
    this.consulta.set(null);
    this.consultando.set(true);
    this.api.consultar(Number(this.anoSeleccionado), Number(this.mesSeleccionado))
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.consultando.set(false)))
      .subscribe({
        next: datos => this.consulta.set(datos),
        error: error => { void this.procesarError(error); }
      });
  }

  limpiar(): void {
    this.consulta.set(null);
    this.filtroDetalle = '';
    this.limpiarMensajes();
  }

  /**
   * Descarga el archivo plano oficial.
   */
  generarArchivo(): void {
    if (!this.consulta() || this.consultando()) return;
    if (!this.prepararGeneracion()) {
      return;
    }

    const ano = Number(this.anoSeleccionado);
    const mes = Number(this.mesSeleccionado);

    this.api.descargar(ano, mes)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.generando.set(false))
      )
      .subscribe({
        next: respuesta => {
          const archivo = respuesta.body;

          if (!archivo || !archivo.size) {
            this.mensajeError.set(
              'El servidor devolvió un archivo vacío.'
            );
            return;
          }

          try {
            const nombreArchivo =
              this.obtenerNombreArchivo(respuesta);

            this.descargarBlob(archivo, nombreArchivo);

            this.mensajeExito.set(
              'Archivo plano generado correctamente.'
            );
          } catch (error: unknown) {
            this.mensajeError.set(
              error instanceof Error
                ? error.message
                : 'No fue posible descargar el archivo plano.'
            );
          }
        },
        error: error => {
          void this.procesarError(error);
        }
      });
  }

  /**
   * Genera Excel usando el plano del backend.
   */
  exportarExcel(): void {
    if (!this.consulta() || this.consultando()) return;
    if (!this.prepararGeneracion()) {
      return;
    }

    const ano = Number(this.anoSeleccionado);
    const mes = Number(this.mesSeleccionado);

    this.api.descargar(ano, mes)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.generando.set(false))
      )
      .subscribe({
        next: respuesta => {
          void this.generarExcel(respuesta, ano, mes);
        },
        error: error => {
          void this.procesarError(error);
        }
      });
  }

  /**
   * Entrega el contenido al exportador especializado.
   */
  private async generarExcel(
    respuesta: HttpResponse<Blob>,
    ano: number,
    mes: number
  ): Promise<void> {
    try {
      const archivo = respuesta.body;

      if (!archivo || !archivo.size) {
        throw new Error(
          'El servidor devolvió un archivo vacío.'
        );
      }

      const nombreTxt =
        this.obtenerNombreArchivo(respuesta);

      const nombreExcel = nombreTxt.replace(
        /\.txt$/i,
        '.xlsx'
      );

      const contenido = await archivo.text();

      this.exporter.exportar(
        contenido,
        ano,
        mes,
        nombreExcel
      );

      this.mensajeExito.set(
        'Archivo Excel generado correctamente.'
      );
    } catch (error: unknown) {
      this.mensajeError.set(
        error instanceof Error
          ? error.message
          : 'No fue posible generar el archivo Excel.'
      );
    }
  }

  /**
   * Obtiene el nombre oficial desde Content-Disposition.
   */
  private obtenerNombreArchivo(
    respuesta: HttpResponse<Blob>
  ): string {
    const disposition =
      respuesta.headers.get('Content-Disposition');

    if (!disposition) {
      throw new Error(
        'El servidor no devolvió el nombre oficial del archivo.'
      );
    }

    const nombreCodificado = disposition.match(
      /filename\*\s*=\s*(?:UTF-8'')?([^;]+)/i
    );

    if (nombreCodificado) {
      const valor = nombreCodificado[1]
        .trim()
        .replace(/^["']|["']$/g, '');

      try {
        return this.validarNombreArchivo(
          decodeURIComponent(valor)
        );
      } catch {
        throw new Error(
          'El nombre del archivo contiene una codificación inválida.'
        );
      }
    }

    const nombreSimple = disposition.match(
      /filename\s*=\s*(?:"([^"]+)"|([^;]+))/i
    );

    if (nombreSimple) {
      return this.validarNombreArchivo(
        (nombreSimple[1] || nombreSimple[2]).trim()
      );
    }

    throw new Error(
      'No fue posible identificar el nombre oficial del archivo.'
    );
  }

  /**
   * Valida el nombre oficial del TXT.
   */
  private validarNombreArchivo(
    nombreArchivo: string
  ): string {
    const nombre = nombreArchivo.trim();

    if (
      !/^SES-[A-Za-z0-9_-]+-\d{8}-[A-Za-z0-9_-]+\.txt$/i.test(nombre)
    ) {
      throw new Error(
        'El servidor devolvió un nombre de archivo SES inválido.'
      );
    }

    return nombre;
  }

  /**
   * Valida el período e inicia la solicitud.
   */
  private prepararGeneracion(): boolean {
    if (this.generando()) {
      return false;
    }

    this.limpiarMensajes();

    const ano = Number(this.anoSeleccionado);
    const mes = Number(this.mesSeleccionado);

    if (
      !Number.isInteger(ano) ||
      ano < 1900 ||
      ano > 9999
    ) {
      this.mensajeError.set(
        'Seleccione un año contable válido.'
      );
      return false;
    }

    if (
      !Number.isInteger(mes) ||
      mes < 1 ||
      mes > 12
    ) {
      this.mensajeError.set(
        'Seleccione un mes contable válido.'
      );
      return false;
    }

    const hoy = new Date();
    const periodoSeleccionado = ano * 100 + mes;

    const periodoActual =
      hoy.getFullYear() * 100 + hoy.getMonth() + 1;

    if (periodoSeleccionado > periodoActual) {
      this.mensajeError.set(
        'No se puede generar un período futuro.'
      );
      return false;
    }

    this.generando.set(true);
    return true;
  }

  /**
   * Descarga el Blob recibido del backend.
   */
  private descargarBlob(
    archivo: Blob,
    nombreArchivo: string
  ): void {
    const url = URL.createObjectURL(archivo);
    const enlace = document.createElement('a');

    enlace.href = url;
    enlace.download = nombreArchivo;

    document.body.appendChild(enlace);
    enlace.click();
    document.body.removeChild(enlace);

    URL.revokeObjectURL(url);
  }

  /**
   * Procesa los errores HTTP.
   */
  private async procesarError(
    error: HttpErrorResponse
  ): Promise<void> {
    let mensaje =
      'No fue posible generar el Formato 3.';

    if (error.status === 0) {
      mensaje =
        'No fue posible establecer comunicación con el servidor.';
    } else if (error.status === 401) {
      mensaje =
        'La sesión ha expirado. Ingrese nuevamente.';
    } else if (error.status === 403) {
      mensaje =
        'No tiene permisos para generar el Formato 3.';
    } else if (error.status === 400) {
      mensaje =
        'El período seleccionado no es válido.';
    } else if (error.status === 409) {
      mensaje =
        'El período contable no está cerrado en todas las agencias.';
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

  /**
   * Limpia los mensajes anteriores.
   */
  limpiarMensajes(): void {
    this.mensajeError.set('');
    this.mensajeExito.set('');
  }
}
