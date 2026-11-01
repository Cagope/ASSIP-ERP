
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { ComprobantesExportacionApi } from '../exportacion/comprobantes-exportacion.api';
import { ComprobantesExporterService } from '../exportacion/comprobantes-exporter.service';
import { SessionService } from '../../../../core/auth/session.service';
import { TipoComprobante, TiposComprobantesApi } from '../../tipos-comprobantes/tipos-comprobantes.api';
import { ComprobanteCompleto, FiltrosComprobantes, ResumenComprobante } from './comprobantes-consulta.models';
import { ComprobantesConsultaService } from './comprobantes-consulta.api';

/**
 * Consulta comprobantes contables de todos los módulos y estados.
 * Permite editar exclusivamente comprobantes manuales activos.
 */
@Component({
  selector: 'app-comprobantes-consulta',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './comprobantes-consulta.component.html',
  styleUrls: ['./comprobantes-consulta.component.scss']
})
export class ComprobantesConsultaComponent implements OnInit {
  private readonly api = inject(ComprobantesConsultaService);
  private readonly exportacionApi = inject(ComprobantesExportacionApi);
  private readonly exporter = inject(ComprobantesExporterService);
  private readonly tiposApi = inject(TiposComprobantesApi);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  @Input() modoSeleccion = false;
  @Output() comprobanteSeleccionado = new EventEmitter<ComprobanteCompleto>();

  agencias = this.session.agenciasSig()
    .filter(agencia => agencia.idAgencia != null);
  tiposComprobantes: TipoComprobante[] = [];
  cargandoTipos = false;

  filtros: FiltrosComprobantes = {};
  filtrosAplicados: FiltrosComprobantes = {};
  exportando = false;
  resultados: ResumenComprobante[] = [];
  seleccionado: ComprobanteCompleto | null = null;

  buscando = false;
  cargando = false;
  consultado = false;
  error = '';

  readonly moneda = new Intl.NumberFormat('es-CO', {
    maximumFractionDigits: 2
  });

  /** Inicializa la agencia de sesión y carga los tipos de comprobante. */
  ngOnInit(): void {
    this.inicializarAgencia();
    this.cargarTiposComprobantes();
  }

  /** Selecciona por defecto una agencia autorizada de la sesión. */
  private inicializarAgencia(): void {
    const agenciaActiva = this.session.agenciaActivaSig()?.idAgencia;
    if (agenciaActiva != null &&
        this.agencias.some(agencia => agencia.idAgencia === agenciaActiva)) {
      this.filtros.idAgencia = agenciaActiva;
      return;
    }
    if (this.agencias.length === 1) {
      this.filtros.idAgencia = this.agencias[0].idAgencia!;
    }
  }

  /**
   * Carga tipos de comprobante por agencia.
   * Cuando se consultan varias agencias, elimina códigos duplicados.
   */
  cargarTiposComprobantes(): void {
    this.tiposComprobantes = [];
    this.filtros.tipoComprobante = undefined;
    this.error = '';

    const agenciasConsulta = this.filtros.idAgencia != null
      ? [this.filtros.idAgencia]
      : this.agencias
          .map(agencia => agencia.idAgencia)
          .filter((id): id is number => id != null);

    if (agenciasConsulta.length === 0) return;

    this.cargandoTipos = true;
    const agenciaSeleccionada = this.filtros.idAgencia;
    const solicitudes = agenciasConsulta.map(
      idAgencia => this.tiposApi.listarPorAgencia(idAgencia)
    );

    forkJoin(solicitudes)
      .pipe(finalize(() => { this.cargandoTipos = false; }))
      .subscribe({
        next: respuestas => {
          if (this.filtros.idAgencia !== agenciaSeleccionada) return;
          const tipos = respuestas.flat()
            .filter(tipo => tipo.comprobanteActivo !== false);
          const tiposUnicos = new Map<string, TipoComprobante>();
          for (const tipo of tipos) {
            if (!tiposUnicos.has(tipo.tipoComprobante)) {
              tiposUnicos.set(tipo.tipoComprobante, tipo);
            }
          }
          this.tiposComprobantes = Array.from(tiposUnicos.values())
            .sort((a, b) => a.tipoComprobante.localeCompare(b.tipoComprobante));
        },
        error: () => {
          this.tiposComprobantes = [];
          this.error = 'No se pudieron consultar los tipos de comprobante.';
        }
      });
  }

  /** Busca comprobantes y valida previamente el rango de fechas. */
  buscar(): void {
    if (this.buscando || this.cargando) return;
    this.error = '';
    this.seleccionado = null;
    this.consultado = false;

    if (this.filtros.fechaDesde && this.filtros.fechaHasta &&
        this.filtros.fechaDesde > this.filtros.fechaHasta) {
      this.error = 'La fecha inicial no puede ser posterior a la final.';
      return;
    }

    this.buscando = true;
    const filtrosConsulta = { ...this.filtros };
    this.api.buscar(filtrosConsulta)
      .pipe(finalize(() => { this.buscando = false; }))
      .subscribe({
        next: datos => {
          this.resultados = datos;
          this.filtrosAplicados = filtrosConsulta;
          this.consultado = true;
        },
        error: err => {
          this.resultados = [];
          this.error = err.error?.message ||
            'No se pudieron consultar los comprobantes.';
        }
      });
  }

  /**
   * Recupera el comprobante completo.
   * En modo selección, solo emite comprobantes manuales activos.
   */
  seleccionar(comprobante: ResumenComprobante): void {
    if (this.cargando) return;
    this.error = '';
    this.cargando = true;

    this.api.consultar(
      comprobante.idAgencia,
      comprobante.tipoComprobante,
      comprobante.numeroComprobante
    )
      .pipe(finalize(() => { this.cargando = false; }))
      .subscribe({
        next: datos => {
          this.seleccionado = datos;
          if (this.modoSeleccion) {
            if (!this.esEditable(datos)) {
              this.error = 'Este comprobante es de solo consulta. Solo pueden seleccionarse para edición los comprobantes manuales activos.';
              return;
            }
            this.comprobanteSeleccionado.emit(datos);
          }
        },
        error: err => {
          this.seleccionado = null;
          this.error = err.error?.message ||
            'No se pudo recuperar el comprobante.';
        }
      });
  }

  /**
   * Determina si el comprobante pertenece al proceso manual activo.
   * Esta validación visual no sustituye la autorización del backend.
   */
  /** Valida el permiso de edición y que el comprobante sea manual, activo y editable. */
  esEditable(comprobante: ComprobanteCompleto | null = this.seleccionado): boolean {
    if (!comprobante || !this.session.tienePermiso('CONTABILIDAD_EDIT')) {
      return false;
    }
    const cabecera = comprobante.cabecera;
    return cabecera.origenTipo === 'MANUAL' &&
      cabecera.moduloOrigen === 'CONTABILIDAD' &&
      cabecera.procesoOrigen === 'COMPROBANTE_MANUAL' &&
      cabecera.estadoOrigen === 'ACTIVO' &&
      comprobante.movimientos.length > 0 &&
      comprobante.movimientos.every(m => m.estadoMovimiento === 'A');
  }

  /** Regresa a los filtros y resultados conservando la búsqueda anterior. */
  volverAResultados(): void {
    this.seleccionado = null;
    this.error = '';
  }

  /** Abre la edición únicamente de un comprobante manual activo. */
  editar(): void {
    if (!this.esEditable()) {
      this.error = 'El comprobante seleccionado no está habilitado para edición.';
      return;
    }
    const cabecera = this.seleccionado!.cabecera;
    void this.router.navigate(
      ['/contabilidad/comprobantes/editar'],
      {
        queryParams: {
          idAgencia: cabecera.idAgencia,
          tipoComprobante: cabecera.tipoComprobante,
          numeroComprobante: cabecera.numeroComprobante
        }
      }
    );
  }

  /** Verifica el permiso de exportación, independiente del permiso de edición. */
  get puedeExportar(): boolean {
    return !this.modoSeleccion && this.session.tienePermiso('CONTABILIDAD_EXPORT');
  }

  /** Exporta todos los comprobantes de la última consulta, no solo los 100 visibles. */
  exportarConsulta(): void {
    if (!this.puedeExportar || !this.consultado || this.exportando || this.buscando || this.cargando) return;
    this.error = '';
    this.exportando = true;
    this.exportacionApi.exportarConsulta(this.filtrosAplicados)
      .pipe(finalize(() => { this.exportando = false; }))
      .subscribe({
        next: datos => this.exporter.exportarConsulta(datos, this.filtrosAplicados),
        error: err => {
          this.error = err.error?.message || 'No se pudo exportar la consulta de comprobantes.';
        }
      });
  }

  /** Exporta todos los comprobantes filtrados en bloques independientes con sus soportes. */
  exportarImpresionGeneral(): void {
    if (!this.puedeExportar || !this.consultado || this.exportando || this.buscando || this.cargando) return;
    this.error = '';
    this.exportando = true;
    this.exportacionApi.exportarConsulta(this.filtrosAplicados)
      .pipe(finalize(() => { this.exportando = false; }))
      .subscribe({
        next: datos => this.exporter.exportarImpresionGeneral(datos, this.filtrosAplicados),
        error: err => {
          this.error = err.error?.message || 'No se pudo exportar la impresión general.';
        }
      });
  }

  /** Exporta el comprobante seleccionado con todos sus movimientos y controles. */
  exportarComprobante(): void {
    if (!this.puedeExportar || !this.seleccionado || this.exportando || this.cargando) return;
    const cabecera = this.seleccionado.cabecera;
    this.error = '';
    this.exportando = true;
    this.exportacionApi.exportarComprobante(
      cabecera.idAgencia, cabecera.tipoComprobante, cabecera.numeroComprobante
    )
      .pipe(finalize(() => { this.exportando = false; }))
      .subscribe({
        next: datos => this.exporter.exportarComprobante(datos),
        error: err => {
          this.error = err.error?.message || 'No se pudo exportar el comprobante.';
        }
      });
  }

  /** Calcula los totales de los movimientos para presentación. */
  total(campo: 'valorDebito' | 'valorCredito'): number {
    return (this.seleccionado?.movimientos || [])
      .reduce((acumulado, movimiento) =>
        acumulado + Number(movimiento[campo] || 0), 0);
  }
}
