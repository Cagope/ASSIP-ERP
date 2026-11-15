
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { SessionService } from '../../../../core/auth/session.service';
import { CierreDiarioApi } from './cierre-diario.api';
import { CierreDiarioPrintService } from './cierre-diario-print.service';
import { CierreDiarioExporterService } from './cierre-diario-exporter.service';
import {
  CierreDiarioDia,
  CierreDiarioInforme,
  CierreDiarioMensual,
  CierreDiarioResumen
} from './cierre-diario.models';

interface AgenciaOpcion {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
}

/**
 * Control mensual de cierres diarios de Contabilidad.
 * Permite consultar, cerrar y reabrir una fecha seleccionada.
 */
@Component({
  selector: 'app-cierre-diario',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cierre-diario.component.html',
  styleUrl: './cierre-diario.component.scss'
})
export class CierreDiarioComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly api = inject(CierreDiarioApi);
  private readonly printService = inject(CierreDiarioPrintService);
  private readonly exporterService = inject(CierreDiarioExporterService);

  readonly meses = [
    { valor: 1, nombre: 'Enero' },
    { valor: 2, nombre: 'Febrero' },
    { valor: 3, nombre: 'Marzo' },
    { valor: 4, nombre: 'Abril' },
    { valor: 5, nombre: 'Mayo' },
    { valor: 6, nombre: 'Junio' },
    { valor: 7, nombre: 'Julio' },
    { valor: 8, nombre: 'Agosto' },
    { valor: 9, nombre: 'Septiembre' },
    { valor: 10, nombre: 'Octubre' },
    { valor: 11, nombre: 'Noviembre' },
    { valor: 12, nombre: 'Diciembre' }
  ];

  agencias: AgenciaOpcion[] = [];
  idAgencia = 0;
  ano = 0;
  mes = 0;
  filtroEstado = 'TODOS';

  mensual: CierreDiarioMensual | null = null;
  diaSeleccionado: CierreDiarioDia | null = null;
  resumen: CierreDiarioResumen | null = null;
  informe: CierreDiarioInforme | null = null;
  justificacion = '';

  cargando = false;
  cargandoDetalle = false;
  cargandoInforme = false;
  mensajeError = '';
  mensajeExito = '';

  private versionConsulta = 0;

  /** Precarga agencias autorizadas y el período actual. */
  ngOnInit(): void {
    this.agencias = this.session.agenciasSig()
      .filter(a => a.idAgencia !== null)
      .map(a => ({
        idAgencia: a.idAgencia!,
        codigoAgencia: a.codigoAgencia ?? '',
        nombreAgencia: a.nombreAgencia ?? ''
      }));

    const activa = this.session.agenciaActivaSig()?.idAgencia;

    this.idAgencia = this.agencias.find(a => a.idAgencia === activa)?.idAgencia
      ?? (this.agencias.length === 1 ? this.agencias[0].idAgencia : 0);

    const hoy = new Date();
    this.ano = hoy.getFullYear();
    this.mes = hoy.getMonth() + 1;

    if (this.idAgencia > 0) {
      this.consultarMes();
    }
  }

  /** Invalida resultados anteriores cuando cambian los filtros. */
  cambiarFiltros(): void {
    this.versionConsulta++;
    this.mensual = null;
    this.diaSeleccionado = null;
    this.resumen = null;
    this.informe = null;
    this.justificacion = '';
    this.mensajeError = '';
    this.mensajeExito = '';
  }

  /** Valida agencia y período antes de consultar el backend. */
  private filtrosValidos(): boolean {
    if (!this.agencias.some(a => a.idAgencia === this.idAgencia)) {
      this.mensajeError = 'Seleccione una agencia autorizada.';
      return false;
    }

    if (!Number.isInteger(this.ano) || this.ano < 1900 || this.ano > 9999) {
      this.mensajeError = 'Seleccione un año contable válido.';
      return false;
    }

    if (!Number.isInteger(this.mes) || this.mes < 1 || this.mes > 12) {
      this.mensajeError = 'Seleccione un mes válido.';
      return false;
    }

    return true;
  }

  /**
   * Consulta todos los días del mes.
   * Puede conservar una fecha seleccionada y el mensaje de una operación.
   */
  consultarMes(
    fechaPreferida?: string,
    conservarMensaje = false
  ): void {
    if (this.cargando) {
      return;
    }

    this.mensajeError = '';

    if (!conservarMensaje) {
      this.mensajeExito = '';
    }

    if (!this.filtrosValidos()) {
      return;
    }

    const idAgencia = this.idAgencia;
    const ano = this.ano;
    const mes = this.mes;
    const version = ++this.versionConsulta;

    this.cargando = true;

    this.api.consultarMes(idAgencia, ano, mes)
      .pipe(finalize(() => this.cargando = false))
      .subscribe({
        next: resultado => {
          if (
            version !== this.versionConsulta ||
            idAgencia !== this.idAgencia ||
            ano !== this.ano ||
            mes !== this.mes
          ) {
            return;
          }

          this.mensual = resultado;
          this.diaSeleccionado = null;
          this.resumen = null;
          this.informe = null;

          if (fechaPreferida) {
            const dia = resultado.dias.find(
              d => d.fecha === fechaPreferida
            );

            if (dia) {
              this.seleccionarDia(dia, conservarMensaje);
            }
          }
        },
        error: error => {
          if (version === this.versionConsulta) {
            this.mostrarError(
              error,
              'No fue posible consultar los cierres del mes.'
            );
          }
        }
      });
  }

  /**
   * Abre el detalle de una fecha y oculta la lista mensual.
   * Consulta los movimientos contables correspondientes.
   */
  seleccionarDia(
    dia: CierreDiarioDia,
    conservarMensaje = false
  ): void {
    if (this.cargando || this.cargandoDetalle) {
      return;
    }

    this.diaSeleccionado = dia;
    this.resumen = null;
    this.informe = null;
    this.justificacion = '';
    this.mensajeError = '';

    if (!conservarMensaje) {
      this.mensajeExito = '';
    }

    const fecha = dia.fecha;
    const idAgencia = this.idAgencia;
    const version = this.versionConsulta;

    this.cargandoDetalle = true;

    this.api.consultar(idAgencia, fecha)
      .pipe(finalize(() => this.cargandoDetalle = false))
      .subscribe({
        next: resultado => {
          if (
            version === this.versionConsulta &&
            this.diaSeleccionado?.fecha === fecha &&
            this.idAgencia === idAgencia
          ) {
            this.resumen = resultado;
          }
        },
        error: error => {
          if (version === this.versionConsulta) {
            this.mostrarError(
              error,
              'No fue posible consultar el detalle del día.'
            );
          }
        }
      });
  }

  /**
   * Regresa al control mensual sin repetir la consulta.
   * Conserva agencia, período y filtro de estado.
   */
  volverAlMes(): void {
    if (this.cargando || this.cargandoDetalle) {
      return;
    }

    this.diaSeleccionado = null;
    this.resumen = null;
    this.informe = null;
    this.justificacion = '';
    this.mensajeError = '';
    this.mensajeExito = '';
  }

  /**
   * Obtiene el informe contable completo del día seleccionado.
   * Descarta respuestas anteriores si cambian la agencia o la fecha.
   */
  consultarInforme(): void {
    if (!this.resultadoVigente || this.cargandoInforme ||
        this.cargando || this.cargandoDetalle || !this.diaSeleccionado) {
      return;
    }

    const fecha = this.diaSeleccionado.fecha;
    const idAgencia = this.idAgencia;
    const version = this.versionConsulta;

    this.informe = null;
    this.cargandoInforme = true;
    this.mensajeError = '';

    this.api.consultarInforme(idAgencia, fecha)
      .pipe(finalize(() => this.cargandoInforme = false))
      .subscribe({
        next: resultado => {
          if (version === this.versionConsulta &&
              this.diaSeleccionado?.fecha === fecha &&
              this.idAgencia === idAgencia) {
            this.informe = resultado;
          }
        },
        error: error => {
          if (version === this.versionConsulta &&
              this.diaSeleccionado?.fecha === fecha) {
            this.mostrarError(error, 'No fue posible consultar el informe del día.');
          }
        }
      });
  }

  /** Confirma que el informe corresponde a la fecha y agencia actuales. */
  get informeVigente(): boolean {
    return !!this.informe && !!this.diaSeleccionado &&
      this.informe.idAgencia === this.idAgencia &&
      this.informe.fecha === this.diaSeleccionado.fecha;
  }

  /** Imprime exclusivamente el informe vigente de la agencia y fecha seleccionadas. */
  imprimirInforme(): void {
    if (!this.informeVigente || !this.informe || this.cargandoInforme ||
        this.cargando || this.cargandoDetalle) {
      this.mensajeError = 'Consulte primero el informe del día seleccionado.';
      return;
    }
    this.mensajeError = '';
    try {
      this.printService.imprimir(this.informe);
    } catch (error) {
      this.mensajeError = error instanceof Error
        ? error.message
        : 'No fue posible abrir el informe para impresión.';
    }
  }

  /** Exporta el informe vigente con control, resumen y detalle en Excel. */
  exportarExcel(): void {
    if (!this.informeVigente || !this.informe || this.cargandoInforme ||
        this.cargando || this.cargandoDetalle) {
      this.mensajeError = 'Consulte primero el informe del día seleccionado.';
      return;
    }
    this.mensajeError = '';
    try {
      this.exporterService.exportar(this.informe);
    } catch (error) {
      this.mensajeError = error instanceof Error
        ? error.message
        : 'No fue posible exportar el informe a Excel.';
    }
  }

  /** Confirma y ejecuta el cierre de la fecha seleccionada. */
  cerrar(): void {
    if (!this.puedeCerrar || !this.diaSeleccionado) {
      return;
    }

    const fecha = this.diaSeleccionado.fecha;
    const idAgencia = this.idAgencia;

    if (!window.confirm(
      `¿Confirma el cierre contable del ${fecha} para ${this.nombreAgencia}?`
    )) {
      return;
    }

    this.informe = null;
    this.cargando = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    this.api.cerrar({ idAgencia, fecha })
      .pipe(finalize(() => this.cargando = false))
      .subscribe({
        next: () => {
          this.mensajeExito = 'El cierre diario se registró correctamente.';
          this.actualizarMes(fecha);
        },
        error: error => {
          this.resumen = null;
          this.mostrarError(
            error,
            'No fue posible cerrar el día. Consulte nuevamente.'
          );
        }
      });
  }

  /** Solicita justificación y confirma la reapertura excepcional. */
  reabrir(): void {
    if (!this.puedeReabrir || !this.diaSeleccionado) {
      return;
    }

    const motivo = this.justificacion.trim();

    if (!motivo) {
      this.mensajeError =
        'La justificación de la reapertura es obligatoria.';
      return;
    }

    const fecha = this.diaSeleccionado.fecha;
    const idAgencia = this.idAgencia;

    if (!window.confirm(
      `¿Confirma la reapertura del ${fecha} para ${this.nombreAgencia}?`
    )) {
      return;
    }

    this.informe = null;
    this.cargando = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    this.api.reabrir({
      idAgencia,
      fecha,
      justificacion: motivo
    })
      .pipe(finalize(() => this.cargando = false))
      .subscribe({
        next: () => {
          this.justificacion = '';
          this.mensajeExito =
            'La fecha contable se reabrió correctamente.';
          this.actualizarMes(fecha);
        },
        error: error => {
          this.resumen = null;
          this.mostrarError(
            error,
            'No fue posible reabrir el día. Consulte nuevamente.'
          );
        }
      });
  }

  /**
   * Actualiza el mes después de cerrar o reabrir.
   * Mantiene la fecha seleccionada y el mensaje de confirmación.
   */
  private actualizarMes(fecha: string): void {
    this.cargando = false;
    this.consultarMes(fecha, true);
  }

  /** Presenta los errores funcionales del backend. */
  private mostrarError(error: any, alternativo: string): void {
    this.mensajeError =
      error?.error?.message ??
      error?.error?.detail ??
      alternativo;
  }

  /** Filtra los días del control mensual por su estado o validación. */
  get diasFiltrados(): CierreDiarioDia[] {
    const dias = this.mensual?.dias ?? [];

    switch (this.filtroEstado) {
      case 'ABIERTOS':
        return dias.filter(d => d.estado === 'A');

      case 'CERRADOS':
        return dias.filter(d => d.estado === 'C');

      case 'DIFERENCIAS':
        return dias.filter(d => !d.cuadrado);

      case 'SIN_MOVIMIENTOS':
        return dias.filter(d => !d.tieneMovimientos);

      default:
        return dias;
    }
  }

  /** Obtiene el nombre legible de la agencia seleccionada. */
  get nombreAgencia(): string {
    const agencia = this.agencias.find(
      a => a.idAgencia === this.idAgencia
    );

    return agencia
      ? `${agencia.codigoAgencia} - ${agencia.nombreAgencia}`
      : '';
  }

  /** Verifica que el detalle corresponda a la fecha seleccionada. */
  get resultadoVigente(): boolean {
    return !!this.resumen &&
      !!this.diaSeleccionado &&
      this.resumen.idAgencia === this.idAgencia &&
      this.resumen.fecha === this.diaSeleccionado.fecha;
  }

  /** Verifica el permiso administrativo de períodos. */
  sessionTienePermisoAdmin(): boolean {
    return this.session.tienePermiso(
      'CONTABILIDAD_PERIODOS_ADMIN'
    );
  }

  /** Verifica el permiso de reapertura de períodos. */
  sessionTienePermisoReopen(): boolean {
    return this.session.tienePermiso(
      'CONTABILIDAD_PERIODOS_REOPEN'
    );
  }

  /**
   * Permite cerrar días abiertos y equilibrados.
   * El backend puede crear automáticamente el período mensual.
   */
  get puedeCerrar(): boolean {
    return !this.cargando &&
      !this.cargandoDetalle &&
      this.resultadoVigente &&
      this.sessionTienePermisoAdmin() &&
      (
        this.resumen?.estadoPeriodoMensual === 'ABIERTO' ||
        this.resumen?.estadoPeriodoMensual === 'NO_CREADO'
      ) &&
      this.diaSeleccionado?.estado === 'A' &&
      this.resumen?.estadoDia === 'A' &&
      Number(this.resumen?.diferencia) === 0;
  }

  /** Permite reabrir días cerrados de períodos mensuales abiertos. */
  get puedeReabrir(): boolean {
    return !this.cargando &&
      !this.cargandoDetalle &&
      this.resultadoVigente &&
      this.sessionTienePermisoReopen() &&
      this.resumen?.estadoPeriodoMensual === 'ABIERTO' &&
      this.diaSeleccionado?.estado === 'C' &&
      this.resumen?.estadoDia === 'C';
  }
}
