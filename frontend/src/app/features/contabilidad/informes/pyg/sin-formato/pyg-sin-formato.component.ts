
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import { SessionService } from '../../../../../core/auth/session.service';
import { PyGSinFormatoApi } from './pyg-sin-formato.api';
import {
  PyGSinFormatoFiltro,
  PyGSinFormatoRespuesta
} from './pyg-sin-formato.models';

interface PyGSinFormatoAgencia {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
}

/**
 * Consulta y exporta el Estado de Excedentes o Pérdidas sin formato.
 * Utiliza las agencias autorizadas y la agencia activa de la sesión.
 */
@Component({
  selector: 'app-pyg-sin-formato',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pyg-sin-formato.component.html',
  styleUrls: ['./pyg-sin-formato.component.scss']
})
export class PyGSinFormatoComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly api = inject(PyGSinFormatoApi);

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

  readonly anoActual = new Date().getFullYear();

  agencias: PyGSinFormatoAgencia[] = [];
  idAgencia: number | null = null;
  ano = this.anoActual;
  mes = new Date().getMonth() + 1;

  consultando = false;
  exportando: 'pdf' | 'excel' | null = null;
  incluirFirmas = true;
  mensajeError = '';
  resultado: PyGSinFormatoRespuesta | null = null;

  private filtroConsultado: PyGSinFormatoFiltro | null = null;

  /**
   * Carga las agencias permitidas y precarga la agencia activa.
   * Mantiene el mecanismo de sesión existente en Balances.
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

    const idAgenciaActiva =
      this.session.agenciaActivaSig()?.idAgencia;

    this.idAgencia =
      this.agencias.find(
        agencia => agencia.idAgencia === idAgenciaActiva
      )?.idAgencia
      ?? (this.agencias.length === 1
        ? this.agencias[0].idAgencia
        : null);
  }

  /** Obtiene el nombre del mes seleccionado. */
  get nombreMes(): string {
    return this.meses.find(
      item => item.numero === Number(this.mes)
    )?.nombre ?? '';
  }

  /** Valida filtros y evita consultas durante otra operación. */
  get puedeConsultar(): boolean {
    return !this.consultando
      && this.exportando === null
      && this.session.tienePermiso('CONTABILIDAD_VIEW')
      && this.idAgencia !== null
      && this.agencias.some(
        agencia => agencia.idAgencia === Number(this.idAgencia)
      )
      && Number.isInteger(Number(this.ano))
      && Number(this.ano) >= 1900
      && Number(this.ano) <= 9999
      && Number.isInteger(Number(this.mes))
      && Number(this.mes) >= 1
      && Number(this.mes) <= 12;
  }

  /** Solo permite exportar resultados de una consulta exitosa. */
  get puedeExportar(): boolean {
    return this.resultado !== null
      && this.filtroConsultado !== null
      && !this.consultando
      && this.exportando === null
      && this.idAgencia === this.filtroConsultado.idAgencia
      && Number(this.ano) === this.filtroConsultado.ano
      && Number(this.mes) === this.filtroConsultado.mes;
  }

  /** Invalida los resultados cuando cambia algún filtro. */
  limpiarResultado(): void {
    this.resultado = null;
    this.filtroConsultado = null;
    this.mensajeError = '';
  }

  /**
   * Consulta el informe mensual y conserva el filtro validado.
   * El backend comprueba permisos y alcance de agencia.
   */
  consultar(): void {
    if (!this.puedeConsultar || this.idAgencia === null) {
      this.mensajeError =
        'Seleccione una agencia, un año y un mes válidos.';
      return;
    }

    const filtro: PyGSinFormatoFiltro = {
      idAgencia: Number(this.idAgencia),
      ano: Number(this.ano),
      mes: Number(this.mes)
    };

    this.consultando = true;
    this.mensajeError = '';
    this.resultado = null;
    this.filtroConsultado = null;

    this.api.consultar(filtro)
      .pipe(
        finalize(() => {
          this.consultando = false;
        })
      )
      .subscribe({
        next: respuesta => {
          this.resultado = respuesta;
          this.filtroConsultado = { ...filtro };
        },
        error: (error: HttpErrorResponse) => {
          this.mensajeError = this.obtenerMensajeError(error);
        }
      });
  }

  /** Descarga el PDF formal con las firmas opcionales. */
  exportarPdf(): void {
    this.exportar('pdf');
  }

  /** Descarga el Excel con importes numéricos reales. */
  exportarExcel(): void {
    this.exportar('excel');
  }

  /**
   * Solicita el documento al backend y descarga el archivo recibido.
   * Usa exclusivamente el filtro de la última consulta exitosa.
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
      .pipe(
        finalize(() => {
          this.exportando = null;
        })
      )
      .subscribe({
        next: archivo => {
          const extension = tipo === 'pdf' ? 'pdf' : 'xlsx';
          const nombreArchivo =
            `pyg_sin_formato_${filtro.idAgencia}_` +
            `${filtro.ano}_${String(filtro.mes).padStart(2, '0')}.` +
            extension;

          this.descargarArchivo(archivo, nombreArchivo);
        },
        error: (error: HttpErrorResponse) => {
          this.mensajeError = this.obtenerMensajeError(error);
        }
      });
  }

  /** Genera una descarga local y libera la URL temporal. */
  private descargarArchivo(archivo: Blob, nombreArchivo: string): void {
    const url = URL.createObjectURL(archivo);
    const enlace = document.createElement('a');

    enlace.href = url;
    enlace.download = nombreArchivo;
    document.body.appendChild(enlace);
    enlace.click();
    document.body.removeChild(enlace);

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

  /** Determina si el resultado es excedente o pérdida. */
  get esExcedente(): boolean {
    return Number(this.resultado?.excedentePerdida ?? 0) >= 0;
  }

  /** Identifica cada cuenta en la tabla de resultados. */
  identificarCuenta(
    _indice: number,
    cuenta: { codigoCuenta: string }
  ): string {
    return cuenta.codigoCuenta;
  }

  /** Presenta importes con formato monetario colombiano. */
  formatearValor(valor: number | null | undefined): string {
    return new Intl.NumberFormat('es-CO', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(Number(valor ?? 0));
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

    return 'No fue posible completar la operación del Estado de Excedentes o Pérdidas.';
  }
}
