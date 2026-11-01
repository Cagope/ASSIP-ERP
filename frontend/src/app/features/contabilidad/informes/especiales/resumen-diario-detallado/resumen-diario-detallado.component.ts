
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { SessionService } from '../../../../../core/auth/session.service';
import { ResumenDiarioDetalladoService } from './resumen-diario-detallado.service';
import { ResumenDiarioDetalladoExporterService } from './resumen-diario-detallado-exporter.service';
import {
  ResumenDiarioDetalladoFiltro,
  ResumenDiarioDetalladoResponse
} from './resumen-diario-detallado.models';

/** Agencia disponible para el usuario autenticado. */
interface AgenciaOpcion {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
}

/**
 * Consulta y exporta el Resumen diario detallado.
 * Presenta los auxiliares agrupados por fecha y sus totales contables.
 */
@Component({
  selector: 'app-resumen-diario-detallado',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './resumen-diario-detallado.component.html',
  styleUrl: './resumen-diario-detallado.component.scss'
})
export class ResumenDiarioDetalladoComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly resumenService = inject(ResumenDiarioDetalladoService);
  private readonly exporter = inject(ResumenDiarioDetalladoExporterService);

  agencias: AgenciaOpcion[] = [];
  cargando = false;
  consultado = false;
  mensajeError = '';
  informe: ResumenDiarioDetalladoResponse | null = null;

  filtros: ResumenDiarioDetalladoFiltro = {
    idAgencia: 0,
    fechaInicial: '',
    fechaFinal: ''
  };

  /**
   * Carga las agencias autorizadas, selecciona la agencia activa
   * y establece el período desde el primer día del mes hasta hoy.
   */
  ngOnInit(): void {
    this.agencias = this.session.agenciasSig()
      .filter(agencia => agencia.idAgencia !== null)
      .map(agencia => ({
        idAgencia: agencia.idAgencia!,
        codigoAgencia: agencia.codigoAgencia ?? '',
        nombreAgencia: agencia.nombreAgencia ?? ''
      }));

    const activa = this.session.agenciaActivaSig()?.idAgencia;

    this.filtros.idAgencia = this.agencias.some(
      agencia => agencia.idAgencia === activa
    )
      ? activa!
      : this.agencias.length === 1
        ? this.agencias[0].idAgencia
        : 0;

    const hoy = new Date();
    const anio = hoy.getFullYear();
    const mes = String(hoy.getMonth() + 1).padStart(2, '0');
    const dia = String(hoy.getDate()).padStart(2, '0');

    this.filtros.fechaInicial = `${anio}-${mes}-01`;
    this.filtros.fechaFinal = `${anio}-${mes}-${dia}`;
  }

  /** Descarta el informe anterior cuando cambia la agencia. */
  cambiarAgencia(): void {
    this.mensajeError = '';
    this.limpiarResultado();
  }

  /** Descarta el informe anterior cuando cambia alguna fecha. */
  cambiarFiltros(): void {
    this.mensajeError = '';
    this.limpiarResultado();
  }

  /**
   * Valida los filtros y consulta los movimientos contables.
   * Impide presentar resultados de una consulta anterior.
   */
  consultar(): void {
    if (this.cargando) {
      return;
    }

    this.mensajeError = '';
    this.limpiarResultado();

    const error = this.validarFiltros();

    if (error) {
      this.mensajeError = error;
      return;
    }

    this.cargando = true;

    this.resumenService.consultar({
      ...this.filtros
    }).pipe(
      finalize(() => this.cargando = false)
    ).subscribe({
      next: informe => {
        this.informe = informe;
        this.consultado = true;
      },
      error: error => {
        this.mensajeError =
          error?.error?.message
          || error?.error?.detail
          || 'No fue posible consultar el Resumen diario detallado.';
      }
    });
  }

  /**
   * Exporta únicamente el informe vigente con movimientos.
   * Reutiliza los datos consultados y la agencia autorizada.
   */
  exportarExcel(): void {
    if (this.cargando || !this.resultadoVigente || !this.informe || !this.tieneResultados) {
      this.mensajeError = 'Debe consultar un informe con resultados antes de exportar.';
      return;
    }

    const agencia = this.agencias.find(
      item => item.idAgencia === this.informe!.idAgencia
    );

    if (!agencia) {
      this.mensajeError = 'No se encontró la agencia correspondiente al informe.';
      return;
    }

    this.mensajeError = '';

    this.exporter.exportar(this.informe, {
      codigoAgencia: agencia.codigoAgencia,
      nombreAgencia: agencia.nombreAgencia
    });
  }

  /** Comprueba que el resultado corresponde a los filtros actuales. */
  get resultadoVigente(): boolean {
    if (!this.informe || !this.consultado) {
      return false;
    }

    return this.informe.idAgencia === this.filtros.idAgencia
      && this.informe.fechaInicial === this.filtros.fechaInicial
      && this.informe.fechaFinal === this.filtros.fechaFinal;
  }

  /** Indica si el informe vigente contiene movimientos. */
  get tieneResultados(): boolean {
    return this.resultadoVigente
      && (this.informe?.cantidadMovimientos ?? 0) > 0;
  }

  /** Devuelve el código y nombre de la agencia consultada. */
  get nombreAgenciaInforme(): string {
    if (!this.informe) {
      return '';
    }

    const agencia = this.agencias.find(
      item => item.idAgencia === this.informe!.idAgencia
    );

    return agencia
      ? `${agencia.codigoAgencia} - ${agencia.nombreAgencia}`
      : '';
  }

  /**
   * Valida la agencia autorizada y las fechas del período.
   * El backend realiza nuevamente la validación de acceso.
   */
  private validarFiltros(): string {
    if (!this.agencias.some(
      agencia => agencia.idAgencia === this.filtros.idAgencia
    )) {
      return 'Debe seleccionar una agencia autorizada.';
    }

    if (!this.filtros.fechaInicial || !this.filtros.fechaFinal) {
      return 'Debe indicar las fechas inicial y final.';
    }

    if (this.filtros.fechaInicial > this.filtros.fechaFinal) {
      return 'La fecha inicial no puede ser posterior a la fecha final.';
    }

    return '';
  }

  /** Limpia los resultados sin modificar los filtros seleccionados. */
  private limpiarResultado(): void {
    this.informe = null;
    this.consultado = false;
  }
}
