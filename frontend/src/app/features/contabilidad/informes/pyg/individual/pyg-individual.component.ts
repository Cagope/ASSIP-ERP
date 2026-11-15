
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { SessionService } from '../../../../../core/auth/session.service';
import { PyGIndividualApi } from './pyg-individual.api';
import {
  PyGIndividualFila,
  PyGIndividualFiltro,
  PyGIndividualRespuesta
} from './pyg-individual.models';

interface PyGIndividualAgencia {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
}

/**
 * Consulta y exporta el Estado de Excedentes o Pérdidas individual.
 * Utiliza agencias autorizadas, un período y detalle opcional.
 */
@Component({
  selector: 'app-pyg-individual',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pyg-individual.component.html',
  styleUrls: ['./pyg-individual.component.scss']
})
export class PyGIndividualComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly api = inject(PyGIndividualApi);

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

  agencias: PyGIndividualAgencia[] = [];
  idAgencia: number | null = null;

  ano = this.anoActual;
  mes = this.fechaActual.getMonth() + 1;

  incluirDetalle = false;
  incluirFirmas = true;

  consultando = false;
  exportando: 'pdf' | 'excel' | null = null;

  mensajeError = '';
  resultado: PyGIndividualRespuesta | null = null;

  private filtroConsultado: PyGIndividualFiltro | null = null;
  private versionConsulta = 0;

  /**
   * Carga las agencias autorizadas y selecciona la agencia activa.
   * Excluye la agencia institucional de consolidación.
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

  /** Comprueba el permiso funcional del informe contable. */
  get tienePermiso(): boolean {
    return this.session.tienePermiso('CONTABILIDAD_VIEW');
  }

  /** Valida la agencia y el período antes de consultar. */
  get puedeConsultar(): boolean {
    return this.tienePermiso
      && !this.consultando
      && this.exportando === null
      && this.idAgencia !== null
      && this.agencias.some(
        agencia => agencia.idAgencia === Number(this.idAgencia)
      )
      && this.periodoValido(this.ano, this.mes);
  }

  /**
   * Permite exportar únicamente el informe consultado.
   * Los filtros actuales deben coincidir con los utilizados.
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
      && Number(this.ano) === filtro.ano
      && Number(this.mes) === filtro.mes
      && this.incluirDetalle === filtro.incluirDetalle;
  }

  /** Comprueba que el año y el mes sean válidos. */
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

  /**
   * Descarta resultados anteriores cuando cambia un filtro.
   * Invalida también las respuestas de consultas anteriores.
   */
  limpiarResultado(): void {
    this.versionConsulta++;
    this.resultado = null;
    this.filtroConsultado = null;
    this.mensajeError = '';
  }

  /**
   * Consulta el período seleccionado y conserva sus parámetros.
   * Ignora respuestas antiguas si se modificaron los filtros.
   */
  consultar(): void {
    if (!this.puedeConsultar || this.idAgencia === null) {
      this.mensajeError =
        'Seleccione una agencia y un período válido.';
      return;
    }

    const filtro: PyGIndividualFiltro = {
      idAgencia: Number(this.idAgencia),
      ano: Number(this.ano),
      mes: Number(this.mes),
      incluirDetalle: this.incluirDetalle
    };

    const version = ++this.versionConsulta;

    this.consultando = true;
    this.mensajeError = '';
    this.resultado = null;
    this.filtroConsultado = null;

    this.api.consultar(filtro)
      .pipe(finalize(() => {
        if (version === this.versionConsulta) {
          this.consultando = false;
        }
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

  /** Solicita el PDF con firmas opcionales. */
  exportarPdf(): void {
    this.exportar('pdf');
  }

  /** Solicita el Excel sin firmas institucionales. */
  exportarExcel(): void {
    this.exportar('excel');
  }

  /**
   * Exporta exclusivamente los filtros del informe consultado.
   * La opción de firmas se aplica únicamente al PDF.
   */
  private exportar(tipo: 'pdf' | 'excel'): void {
    if (!this.puedeExportar || !this.filtroConsultado) {
      return;
    }

    const filtro = { ...this.filtroConsultado };

    this.exportando = tipo;
    this.mensajeError = '';

    const solicitud = tipo === 'pdf'
      ? this.api.exportarPdf(filtro, this.incluirFirmas)
      : this.api.exportarExcel(filtro);

    solicitud
      .pipe(finalize(() => {
        this.exportando = null;
      }))
      .subscribe({
        next: archivo => {
          const agencia = this.agencias.find(
            item => item.idAgencia === filtro.idAgencia
          );

          const codigoAgencia = agencia?.codigoAgencia
            ?? String(filtro.idAgencia);

          const extension = tipo === 'pdf' ? 'pdf' : 'xlsx';

          const nombreArchivo =
            `pyg_individual_${codigoAgencia}_` +
            `${filtro.ano}_` +
            `${String(filtro.mes).padStart(2, '0')}` +
            `${filtro.incluirDetalle ? '_detallado' : ''}.` +
            extension;

          this.descargarArchivo(archivo, nombreArchivo);
        },
        error: (error: HttpErrorResponse) => {
          this.mensajeError = this.obtenerMensajeError(error);
        }
      });
  }

  /** Descarga el documento recibido y libera su URL temporal. */
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

  /** Presenta el período con nombre del mes y año. */
  get nombrePeriodo(): string {
    const nombreMes = this.meses.find(
      item => item.numero === Number(this.mes)
    )?.nombre ?? '';

    return `${nombreMes} ${this.ano}`.trim();
  }

  /** Obtiene el resultado final del ejercicio. */
  get resultadoEjercicio(): number {
    const fila = this.resultado?.filas.find(
      item => item.codigo === 'TT'
    );

    return Number(fila?.valor ?? 0);
  }

  /** Determina si el ejercicio presenta excedente o pérdida. */
  get esExcedente(): boolean {
    return this.resultadoEjercicio >= 0;
  }

  /** Presenta el concepto del resultado del ejercicio. */
  get nombreResultado(): string {
    return this.esExcedente
      ? 'Excedente del ejercicio'
      : 'Pérdida del ejercicio';
  }

  /** Identifica las filas por su código contable o histórico. */
  identificarFila(_indice: number, fila: PyGIndividualFila): string {
    return fila.codigo;
  }

  /** Formatea importes con separadores colombianos. */
  formatearValor(valor: number | null | undefined): string {
    return this.formatoNumero.format(Number(valor ?? 0));
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

    return 'No fue posible completar la operación del Estado de Excedentes o Pérdidas individual.';
  }
}
