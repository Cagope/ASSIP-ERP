
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { SessionService } from '../../../../../core/auth/session.service';
import { LibroDiarioResumidoService } from './libro-diario-resumido.service';
import { LibroDiarioResumidoExporterService } from './libro-diario-resumido-exporter.service';
import {
  LibroDiarioResumidoFiltro,
  LibroDiarioResumidoResponse,
  LIBRO_DIARIO_RESUMIDO_MODALIDADES
} from './libro-diario-resumido.models';

/** Agencia autorizada para el usuario autenticado. */
interface AgenciaOpcion {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
}

/**
 * Consulta del Libro diario resumido.
 * Permite agrupar los movimientos activos por cuenta contable
 * o por tipo de comprobante, dentro de una agencia y período.
 */
@Component({
  selector: 'app-libro-diario-resumido',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './libro-diario-resumido.component.html',
  styleUrl: './libro-diario-resumido.component.scss'
})
export class LibroDiarioResumidoComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly libroDiarioResumidoService = inject(LibroDiarioResumidoService);
  private readonly exporter = inject(LibroDiarioResumidoExporterService);

  readonly modalidades = LIBRO_DIARIO_RESUMIDO_MODALIDADES;

  agencias: AgenciaOpcion[] = [];
  cargando = false;
  consultado = false;
  mensajeError = '';
  informe: LibroDiarioResumidoResponse | null = null;

  filtros: LibroDiarioResumidoFiltro = {
    idAgencia: 0,
    fechaInicial: '',
    fechaFinal: '',
    modalidad: 'CUENTA'
  };

  /**
   * Inicializa las agencias autorizadas y selecciona la agencia
   * activa de la sesión. Establece el mes actual como período.
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

  /**
   * Descarta el resultado anterior cuando cambia la agencia.
   * Conserva el período y la modalidad seleccionados.
   */
  cambiarAgencia(): void {
    this.mensajeError = '';
    this.limpiarResultado();
  }

  /**
   * Descarta el resultado anterior cuando cambia el período
   * o la modalidad de agrupación.
   */
  cambiarFiltros(): void {
    this.mensajeError = '';
    this.limpiarResultado();
  }

  /**
   * Consulta el informe después de validar la agencia autorizada,
   * el rango de fechas y la modalidad de agrupación.
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

    this.libroDiarioResumidoService.consultar({
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
          || 'No fue posible consultar el Libro diario resumido.';
      }
    });
  }

  /**
   * Exporta el resultado vigente mediante el servicio especializado.
   * Utiliza los mismos datos consultados y conserva la modalidad seleccionada.
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

  /**
   * Verifica que el resultado corresponda a los filtros actuales.
   * Evita presentar datos de una consulta anterior.
   */
  get resultadoVigente(): boolean {
    if (!this.informe || !this.consultado) {
      return false;
    }

    return (
      this.informe.idAgencia === this.filtros.idAgencia
      && this.informe.fechaInicial === this.filtros.fechaInicial
      && this.informe.fechaFinal === this.filtros.fechaFinal
      && this.informe.modalidad === this.filtros.modalidad
    );
  }

  /** Indica si el informe vigente contiene filas para presentar. */
  get tieneResultados(): boolean {
    return this.resultadoVigente
      && (this.informe?.filas.length ?? 0) > 0;
  }

  /** Obtiene el nombre de la agencia correspondiente al informe. */
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

  /** Obtiene la descripción de la modalidad consultada. */
  get nombreModalidadInforme(): string {
    if (!this.informe) {
      return '';
    }

    return this.modalidades.find(
      modalidad => modalidad.value === this.informe!.modalidad
    )?.label ?? '';
  }

  /**
   * Valida los filtros antes de enviar la solicitud.
   * El backend vuelve a comprobar permisos y agencia.
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

    if (!this.modalidades.some(
      modalidad => modalidad.value === this.filtros.modalidad
    )) {
      return 'Debe seleccionar una modalidad de agrupación válida.';
    }

    return '';
  }

  /** Descarta el resultado sin modificar los filtros seleccionados. */
  private limpiarResultado(): void {
    this.informe = null;
    this.consultado = false;
  }
}
