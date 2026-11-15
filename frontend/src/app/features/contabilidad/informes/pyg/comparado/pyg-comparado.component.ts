
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { SessionService } from '../../../../../core/auth/session.service';
import { PyGComparadoApi } from './pyg-comparado.api';
import {
  PyGComparadoFila,
  PyGComparadoFiltro,
  PyGComparadoRespuesta
} from './pyg-comparado.models';

interface PyGComparadoAgencia {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
}

/**
 * Consulta y exporta el Estado de Excedentes o Pérdidas comparado.
 * Utiliza las agencias autorizadas y dos períodos independientes.
 */
@Component({
  selector: 'app-pyg-comparado',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pyg-comparado.component.html',
  styleUrls: ['./pyg-comparado.component.scss']
})
export class PyGComparadoComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly api = inject(PyGComparadoApi);

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

  private readonly fechaActual = new Date();
  readonly anoActual = this.fechaActual.getFullYear();

  readonly formatoNumero = new Intl.NumberFormat('es-CO', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });

  agencias: PyGComparadoAgencia[] = [];
  idAgencia: number | null = null;

  anoPeriodo1 = this.anoActual;
  mesPeriodo1 = this.fechaActual.getMonth() + 1;
  anoPeriodo2 = this.anoActual - 1;
  mesPeriodo2 = this.fechaActual.getMonth() + 1;

  consultando = false;
  exportando: 'pdf' | 'excel' | null = null;
  incluirFirmas = true;
  mensajeError = '';
  resultado: PyGComparadoRespuesta | null = null;

  private filtroConsultado: PyGComparadoFiltro | null = null;
  private versionConsulta = 0;

  /**
   * Obtiene las agencias autorizadas y precarga la agencia activa.
   * Excluye el consolidado institucional de esta consulta por agencia.
   */
  ngOnInit(): void {
    this.agencias = this.session.agenciasSig()
      .filter(agencia =>
        agencia.idAgencia !== null &&
        agencia.idAgencia !== 1
      )
      .map(agencia => ({
        idAgencia: agencia.idAgencia!,
        codigoAgencia: agencia.codigoAgencia ?? '',
        nombreAgencia: agencia.nombreAgencia ?? ''
      }));

    const agenciaActiva = this.session.agenciaActivaSig()?.idAgencia;

    this.idAgencia = this.agencias.find(
      agencia => agencia.idAgencia === agenciaActiva
    )?.idAgencia
      ?? (this.agencias.length === 1
        ? this.agencias[0].idAgencia
        : null);
  }

  /** Comprueba el permiso funcional de consulta contable. */
  get tienePermiso(): boolean {
    return this.session.tienePermiso('CONTABILIDAD_VIEW');
  }

  /** Valida la agencia y ambos períodos antes de consultar. */
  get puedeConsultar(): boolean {
    return this.tienePermiso
      && !this.consultando
      && this.exportando === null
      && this.idAgencia !== null
      && this.agencias.some(
        agencia => agencia.idAgencia === Number(this.idAgencia)
      )
      && this.periodoValido(this.anoPeriodo1, this.mesPeriodo1)
      && this.periodoValido(this.anoPeriodo2, this.mesPeriodo2);
  }

  /**
   * Permite exportar exclusivamente el resultado consultado.
   * Los cambios de filtros invalidan el informe anterior.
   */
  get puedeExportar(): boolean {
    if (
      !this.tienePermiso ||
      this.consultando ||
      this.exportando !== null ||
      this.resultado === null ||
      this.filtroConsultado === null
    ) {
      return false;
    }

    const filtro = this.filtroConsultado;

    return Number(this.idAgencia) === filtro.idAgencia
      && Number(this.anoPeriodo1) === filtro.anoPeriodo1
      && Number(this.mesPeriodo1) === filtro.mesPeriodo1
      && Number(this.anoPeriodo2) === filtro.anoPeriodo2
      && Number(this.mesPeriodo2) === filtro.mesPeriodo2;
  }

  /** Verifica que el año y el mes sean numéricamente válidos. */
  private periodoValido(ano: number, mes: number): boolean {
    const anoNumero = Number(ano);
    const mesNumero = Number(mes);

    return Number.isInteger(anoNumero)
      && anoNumero >= 1900
      && anoNumero <= 9999
      && Number.isInteger(mesNumero)
      && mesNumero >= 1
      && mesNumero <= 12;
  }

  /** Descarta resultados anteriores al modificar cualquier filtro. */
  limpiarResultado(): void {
    this.versionConsulta++;
    this.resultado = null;
    this.filtroConsultado = null;
    this.mensajeError = '';
  }

  /**
   * Consulta los dos períodos y conserva los filtros utilizados.
   * Ignora respuestas antiguas cuando el usuario modifica los filtros.
   */
  consultar(): void {
    if (!this.puedeConsultar || this.idAgencia === null) {
      this.mensajeError =
        'Seleccione una agencia y dos períodos válidos.';
      return;
    }

    const filtro: PyGComparadoFiltro = {
      idAgencia: Number(this.idAgencia),
      anoPeriodo1: Number(this.anoPeriodo1),
      mesPeriodo1: Number(this.mesPeriodo1),
      anoPeriodo2: Number(this.anoPeriodo2),
      mesPeriodo2: Number(this.mesPeriodo2)
    };

    const version = ++this.versionConsulta;

    this.consultando = true;
    this.mensajeError = '';
    this.resultado = null;
    this.filtroConsultado = null;

    this.api.consultar(filtro)
      .pipe(finalize(() => {
        this.consultando = false;
      }))
      .subscribe({
        next: respuesta => {
          if (version !== this.versionConsulta) {
            return;
          }

          this.resultado = respuesta;
          this.filtroConsultado = { ...filtro };
        },
        error: (error: HttpErrorResponse) => {
          if (version !== this.versionConsulta) {
            return;
          }

          this.mensajeError = this.obtenerMensajeError(error);
        }
      });
  }

  /** Solicita el documento PDF del resultado consultado. */
  exportarPdf(): void {
    this.exportar('pdf');
  }

  /** Solicita el Excel del resultado consultado. */
  exportarExcel(): void {
    this.exportar('excel');
  }

  /**
   * Descarga el informe con los períodos previamente consultados.
   * Respeta la opción de incluir firmas institucionales.
   */
  private exportar(tipo: 'pdf' | 'excel'): void {
    if (!this.puedeExportar || !this.filtroConsultado) {
      return;
    }

    const filtro = { ...this.filtroConsultado };
    const incluirFirmas = this.incluirFirmas;

    this.exportando = tipo;
    this.mensajeError = '';

    const solicitud = tipo === 'pdf'
      ? this.api.exportarPdf(filtro, incluirFirmas)
      : this.api.exportarExcel(filtro, incluirFirmas);

    solicitud
      .pipe(finalize(() => {
        this.exportando = null;
      }))
      .subscribe({
        next: archivo => {
          const extension = tipo === 'pdf' ? 'pdf' : 'xlsx';

          const nombreArchivo =
            `pyg_comparado_${filtro.idAgencia}_` +
            `${filtro.anoPeriodo1}-` +
            `${String(filtro.mesPeriodo1).padStart(2, '0')}_` +
            `${filtro.anoPeriodo2}-` +
            `${String(filtro.mesPeriodo2).padStart(2, '0')}.` +
            extension;

          this.descargarArchivo(archivo, nombreArchivo);
        },
        error: (error: HttpErrorResponse) => {
          this.mensajeError = this.obtenerMensajeError(error);
        }
      });
  }

  /** Descarga el archivo recibido y libera su URL temporal. */
  private descargarArchivo(archivo: Blob, nombreArchivo: string): void {
    const url = URL.createObjectURL(archivo);
    const enlace = document.createElement('a');

    enlace.href = url;
    enlace.download = nombreArchivo;

    document.body.appendChild(enlace);
    enlace.click();
    enlace.remove();

    URL.revokeObjectURL(url);
  }

  /** Obtiene el código y nombre de la agencia seleccionada. */
  get nombreAgencia(): string {
    const agencia = this.agencias.find(
      item => item.idAgencia === Number(this.idAgencia)
    );

    return agencia
      ? `${agencia.codigoAgencia} - ${agencia.nombreAgencia}`
      : '';
  }

  /** Presenta el primer período con nombre de mes y año. */
  get nombrePeriodo1(): string {
    return this.nombrePeriodo(this.anoPeriodo1, this.mesPeriodo1);
  }

  /** Presenta el segundo período con nombre de mes y año. */
  get nombrePeriodo2(): string {
    return this.nombrePeriodo(this.anoPeriodo2, this.mesPeriodo2);
  }

  /** Obtiene la etiqueta legible de un período. */
  private nombrePeriodo(ano: number, mes: number): string {
    const nombreMes = this.meses.find(
      item => item.numero === Number(mes)
    )?.nombre ?? '';

    return `${nombreMes} ${ano}`.trim();
  }

  /** Determina si el resultado final representa excedente o pérdida. */
  get esExcedente(): boolean {
    const resultadoFinal = this.resultado?.filas.find(
      fila => fila.codigo === 'TT'
    );

    return Number(resultadoFinal?.periodo1 ?? 0) >= 0;
  }

  /** Identifica cada renglón por su código histórico. */
  identificarFila(_indice: number, fila: PyGComparadoFila): string {
    return fila.codigo;
  }

  /** Presenta importes monetarios con formato colombiano. */
  formatearValor(valor: number | null | undefined): string {
    return this.formatoNumero.format(Number(valor ?? 0));
  }

  /** Presenta variaciones porcentuales sin inventar valores indefinidos. */
  formatearPorcentaje(valor: number | null | undefined): string {
    return valor == null
      ? 'N/D'
      : `${this.formatoNumero.format(valor)} %`;
  }

  /** Traduce los errores HTTP a mensajes comprensibles. */
  private obtenerMensajeError(error: HttpErrorResponse): string {
    if (error.status === 403) {
      return 'No tiene autorización para consultar esta agencia.';
    }

    if (error.status === 401) {
      return 'Su sesión no está autorizada. Ingrese nuevamente.';
    }

    if (error.status === 400) {
      return typeof error.error?.message === 'string'
        ? error.error.message
        : 'Los parámetros de consulta no son válidos.';
    }

    return 'No fue posible completar la operación del Estado de Excedentes o Pérdidas comparado.';
  }
}
