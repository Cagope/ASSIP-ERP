
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { SessionService } from '../../../../../core/auth/session.service';
import { SecuencialComprobantesService } from './secuencial-comprobantes.service';
import { SecuencialComprobantesExporterService } from './secuencial-comprobantes-exporter.service';
import {
  SecuencialComprobantesFiltro,
  SecuencialComprobantesFila,
  SecuencialComprobantesGrupo,
  SecuencialComprobantesResponse
} from './secuencial-comprobantes.models';

interface AgenciaOpcion {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
}

/**
 * Consulta y presenta el Secuencial de comprobantes.
 * Organiza los resultados por tipo, con detalle y subtotales por corte.
 */
@Component({
  selector: 'app-secuencial-comprobantes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './secuencial-comprobantes.component.html',
  styleUrl: './secuencial-comprobantes.component.scss'
})
export class SecuencialComprobantesComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly secuencialService = inject(SecuencialComprobantesService);
  private readonly exporter = inject(SecuencialComprobantesExporterService);

  agencias: AgenciaOpcion[] = [];
  grupos: SecuencialComprobantesGrupo[] = [];
  cargando = false;
  consultado = false;
  mensajeError = '';
  informe: SecuencialComprobantesResponse | null = null;

  /** Advertencias asociadas a la fila posterior a cada salto. */
  private readonly advertenciasConsecutivos = new WeakMap<SecuencialComprobantesFila, string>();

  filtros: SecuencialComprobantesFiltro = {
    idAgencia: 0,
    fechaInicial: '',
    fechaFinal: ''
  };

  /**
   * Inicializa las agencias autorizadas y el período del mes actual.
   * Selecciona la agencia activa de la sesión cuando está disponible.
   */
  ngOnInit(): void {
    this.agencias = this.session.agenciasSig()
      .filter(agencia => agencia.idAgencia !== null)
      .map(agencia => ({
        idAgencia: agencia.idAgencia!,
        codigoAgencia: agencia.codigoAgencia ?? '',
        nombreAgencia: agencia.nombreAgencia ?? ''
      }));

    const agenciaActiva = this.session.agenciaActivaSig()?.idAgencia;

    this.filtros.idAgencia = this.agencias.some(
      agencia => agencia.idAgencia === agenciaActiva
    )
      ? agenciaActiva!
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
   * Limpia los resultados anteriores cuando cambia la agencia.
   * Evita presentar información de otra agencia como vigente.
   */
  cambiarAgencia(): void {
    this.mensajeError = '';
    this.limpiarResultado();
  }

  /**
   * Invalida los resultados anteriores cuando cambia el período.
   * Obliga a realizar una nueva consulta con los filtros actuales.
   */
  cambiarFiltros(): void {
    this.mensajeError = '';
    this.limpiarResultado();
  }

  /**
   * Valida los filtros y consulta los comprobantes consolidados.
   * Construye los cortes y detecta saltos después de una respuesta exitosa.
   */
  consultar(): void {
    if (this.cargando) {
      return;
    }

    this.mensajeError = '';
    this.limpiarResultado();

    const errorValidacion = this.validarFiltros();
    if (errorValidacion) {
      this.mensajeError = errorValidacion;
      return;
    }

    this.cargando = true;

    this.secuencialService.consultar({ ...this.filtros })
      .pipe(finalize(() => this.cargando = false))
      .subscribe({
        next: resultado => {
          this.informe = resultado;
          this.grupos = this.construirGrupos(resultado.filas);
          this.consultado = true;
        },
        error: error => {
          this.mensajeError =
            error?.error?.message ||
            error?.error?.detail ||
            'No fue posible consultar el Secuencial de comprobantes.';
        }
      });
  }

  /**
   * Agrupa los comprobantes por tipo y calcula los subtotales.
   * Mantiene el orden entregado por el backend.
   */
  private construirGrupos(
    filas: SecuencialComprobantesFila[]
  ): SecuencialComprobantesGrupo[] {
    const grupos = new Map<string, SecuencialComprobantesGrupo>();

    for (const fila of filas) {
      const tipo = fila.tipoComprobante.trim();
      let grupo = grupos.get(tipo);

      if (!grupo) {
        grupo = {
          tipoComprobante: tipo,
          nombreTipoComprobante: fila.nombreTipoComprobante,
          comprobantes: [],
          cantidadComprobantes: 0,
          totalDebitos: 0,
          totalCreditos: 0,
          diferencia: 0
        };
        grupos.set(tipo, grupo);
      }

      grupo.comprobantes.push(fila);
      grupo.cantidadComprobantes++;
      grupo.totalDebitos += fila.totalDebitos;
      grupo.totalCreditos += fila.totalCreditos;
      grupo.diferencia = grupo.totalDebitos - grupo.totalCreditos;
    }

    for (const grupo of grupos.values()) {
      this.detectarSaltos(grupo.comprobantes);
    }

    return Array.from(grupos.values());
  }

  /**
   * Detecta saltos entre números consecutivos del mismo tipo.
   * Resalta únicamente la fila posterior al salto y no altera importes.
   */
  private detectarSaltos(comprobantes: SecuencialComprobantesFila[]): void {
    let anterior: bigint | null = null;
    let anchoAnterior = 0;

    for (const comprobante of comprobantes) {
      const numeroTexto = comprobante.numeroComprobante.trim();

      if (!/^\d+$/.test(numeroTexto)) {
        anterior = null;
        anchoAnterior = 0;
        continue;
      }

      const actual = BigInt(numeroTexto);

      if (anterior !== null && actual > anterior + 1n) {
        const inicial = anterior + 1n;
        const final = actual - 1n;
        const ancho = Math.max(anchoAnterior, numeroTexto.length);
        const numeroInicial = inicial.toString().padStart(ancho, '0');
        const numeroFinal = final.toString().padStart(ancho, '0');

        const advertencia = inicial === final
          ? `Consecutivo ${numeroInicial} no encontrado en la consulta.`
          : `Consecutivos ${numeroInicial} al ${numeroFinal} no encontrados en la consulta.`;

        this.advertenciasConsecutivos.set(comprobante, advertencia);
      }

      anterior = actual;
      anchoAnterior = numeroTexto.length;
    }
  }

  /**
   * Indica si una fila corresponde al primer comprobante posterior a un salto.
   * Se utiliza para aplicar el resaltado visual en el HTML.
   */
  tieneSaltoConsecutivo(fila: SecuencialComprobantesFila): boolean {
    return this.advertenciasConsecutivos.has(fila);
  }

  /** Obtiene el detalle del salto para el título informativo de la fila. */
  descripcionSaltoConsecutivo(fila: SecuencialComprobantesFila): string {
    return this.advertenciasConsecutivos.get(fila) ?? '';
  }

  /**
   * Exporta el informe vigente mediante el servicio especializado.
   * Conserva los cortes y utiliza únicamente los resultados consultados.
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

    this.exporter.exportar(this.informe, this.grupos, {
      codigoAgencia: agencia.codigoAgencia,
      nombreAgencia: agencia.nombreAgencia
    });
  }

  /**
   * Comprueba que el resultado corresponde a los filtros visibles.
   * Impide presentar datos anteriores como si fueran actuales.
   */
  get resultadoVigente(): boolean {
    if (!this.informe || !this.consultado) {
      return false;
    }

    return this.informe.idAgencia === this.filtros.idAgencia
      && this.informe.fechaInicial === this.filtros.fechaInicial
      && this.informe.fechaFinal === this.filtros.fechaFinal;
  }

  /** Indica si existen comprobantes para mostrar en el informe. */
  get tieneResultados(): boolean {
    return this.resultadoVigente && this.grupos.length > 0;
  }

  /** Obtiene el código y nombre de la agencia consultada. */
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

  /** Valida la agencia autorizada y el rango de fechas solicitado. */
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

  /** Descarta el informe y los cortes de la consulta anterior. */
  private limpiarResultado(): void {
    this.informe = null;
    this.grupos = [];
    this.consultado = false;
  }
}
