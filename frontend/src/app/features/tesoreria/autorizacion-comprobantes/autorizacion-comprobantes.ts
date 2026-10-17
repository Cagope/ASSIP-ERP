import {
  Component,
  DestroyRef,
  OnInit,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import {
  AgenciaSesion,
  SessionService
} from '../../../core/auth/session.service';

import {
  AutorizacionComprobantesApi
} from './autorizacion-comprobantes.api';

import {
  ComprobanteResumen,
  ComprobanteDetalle,
  ComprobanteDecisionRequest,
  ComprobanteDecisionResponse,
  DecisionComprobante
} from './autorizacion-comprobantes.models';

/**
 * Bandeja de autorización de comprobantes de Tesorería.
 *
 * Permite consultar pendientes, revisar sus detalles
 * y procesar decisiones según los permisos del usuario.
 *
 * La autorización contable y los movimientos de caja
 * se ejecutan exclusivamente en el backend.
 */
@Component({
  selector: 'app-autorizacion-comprobantes',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './autorizacion-comprobantes.html',
  styleUrl: './autorizacion-comprobantes.scss'
})
export class AutorizacionComprobantesComponent implements OnInit {

  private readonly api = inject(AutorizacionComprobantesApi);
  private readonly session = inject(SessionService);
  private readonly destroyRef = inject(DestroyRef);

  // =========================================================
  // PERMISOS
  // =========================================================

  readonly permisoConsultar =
    'TESORERIA_AUTORIZACION_COMPROBANTES_VIEW';

  readonly permisoEjecutar =
    'TESORERIA_AUTORIZACION_COMPROBANTES_EXECUTE';

  get puedeConsultar(): boolean {
    return this.session.tienePermiso(this.permisoConsultar)
      || this.session.tienePermiso(this.permisoEjecutar);
  }

  get puedeEjecutar(): boolean {
    return this.session.tienePermiso(this.permisoEjecutar);
  }

  // =========================================================
  // ESTADO
  // =========================================================

  agencias: AgenciaSesion[] = [];
  idAgencia: number | null = null;

  comprobantes: ComprobanteResumen[] = [];
  comprobanteSeleccionado: ComprobanteDetalle | null = null;

  cargando = false;
  cargandoDetalle = false;
  procesandoDecision = false;

  mensajeError = '';
  mensajeInformativo = '';

  // =========================================================
  // FILTROS
  // =========================================================

  filtroProceso = '';
  filtroSoporte = '';

  // =========================================================
  // DECISIONES
  // =========================================================

  decisionSeleccionada: DecisionComprobante | null = null;
  motivoDecision = '';

  mostrarConfirmacion = false;

  resultadoDecision: ComprobanteDecisionResponse | null = null;

  // =========================================================
  // CONTROL DE CONSULTAS
  // =========================================================

  private secuenciaConsulta = 0;
  private secuenciaDetalle = 0;

  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  /**
   * Obtiene las agencias de la sesión y consulta los
   * comprobantes pendientes de la agencia activa.
   */
  ngOnInit(): void {

    this.agencias = this.session.getAgencias();

    const agenciaActiva = this.session.getAgenciaActiva();

    if (!this.puedeConsultar) {
      this.mensajeError =
        'No tiene permiso para consultar comprobantes.';
      return;
    }

    if (
      agenciaActiva &&
      this.agencias.some(
        agencia =>
          agencia.idAgencia === agenciaActiva.idAgencia
      )
    ) {
      this.idAgencia = agenciaActiva.idAgencia;
    } else if (this.agencias.length === 1) {
      this.idAgencia = this.agencias[0].idAgencia;
    }

    if (this.idAgencia !== null) {
      this.consultarPendientes();
    }
  }

  // =========================================================
  // CONSULTA DE PENDIENTES
  // =========================================================

  /**
   * Cambia la agencia consultada, descarta el detalle
   * anterior y carga sus comprobantes pendientes.
   */
  cambiarAgencia(): void {

    this.secuenciaConsulta++;
    this.secuenciaDetalle++;

    this.comprobantes = [];
    this.comprobanteSeleccionado = null;

    this.cancelarDecision();
    this.limpiarMensajes();

    this.consultarPendientes();
  }

  /**
   * Consulta los comprobantes pendientes de la agencia.
   * Evita utilizar respuestas de consultas anteriores.
   */
  consultarPendientes(): void {

    if (!this.puedeConsultar) {
      this.mensajeError =
        'No tiene permiso para consultar comprobantes.';
      return;
    }

    const agencia = Number(this.idAgencia);

    if (
      !Number.isInteger(agencia) ||
      agencia <= 0 ||
      !this.agencias.some(a => a.idAgencia === agencia)
    ) {
      this.mensajeError =
        'Seleccione una agencia autorizada.';
      return;
    }

    const secuencia = ++this.secuenciaConsulta;

    this.cargando = true;
    this.mensajeError = '';

    this.api.listarPendientes(agencia)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          if (secuencia === this.secuenciaConsulta) {
            this.cargando = false;
          }
        })
      )
      .subscribe({
        next: comprobantes => {

          if (secuencia !== this.secuenciaConsulta) {
            return;
          }

          this.comprobantes = comprobantes ?? [];

          const seleccionado = this.comprobanteSeleccionado;

          if (
            seleccionado &&
            !this.comprobantes.some(
              c => c.idComprobante === seleccionado.idComprobante
            )
          ) {
            this.secuenciaDetalle++;
            this.comprobanteSeleccionado = null;
            this.cancelarDecision();
          }
        },
        error: error => {

          if (secuencia !== this.secuenciaConsulta) {
            return;
          }

          this.comprobantes = [];

          this.mensajeError = this.obtenerMensajeError(
            error,
            'No fue posible consultar los comprobantes pendientes.'
          );
        }
      });
  }

  // =========================================================
  // FILTROS LOCALES
  // =========================================================

  /**
   * Filtra los comprobantes ya consultados por
   * proceso de origen y número de soporte.
   */
  get comprobantesFiltrados(): ComprobanteResumen[] {

    const proceso = this.filtroProceso
      .trim()
      .toUpperCase();

    const soporte = this.filtroSoporte
      .trim()
      .toUpperCase();

    return this.comprobantes.filter(comprobante => {

      const coincideProceso =
        !proceso ||
        (comprobante.procesoOrigen ?? '')
          .toUpperCase()
          .includes(proceso);

      const coincideSoporte =
        !soporte ||
        (comprobante.documentoSoporte ?? '')
          .toUpperCase()
          .includes(soporte);

      return coincideProceso && coincideSoporte;
    });
  }

  /**
   * Calcula el valor total de los comprobantes
   * visibles en la bandeja.
   */
  get valorTotalFiltrado(): number {

    return this.comprobantesFiltrados.reduce(
      (total, comprobante) =>
        total + Number(comprobante.totalDebito ?? 0),
      0
    );
  }

  // =========================================================
  // DETALLE
  // =========================================================

  /**
   * Consulta el comprobante completo para revisar
   * sus partidas y movimientos antes de decidir.
   */
  seleccionarComprobante(
    comprobante: ComprobanteResumen
  ): void {

    if (this.procesandoDecision) {
      return;
    }

    const secuencia = ++this.secuenciaDetalle;

    this.comprobanteSeleccionado = null;
    this.cancelarDecision();
    this.limpiarMensajes();

    this.cargandoDetalle = true;

    this.api.consultarDetalle(comprobante.idComprobante)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          if (secuencia === this.secuenciaDetalle) {
            this.cargandoDetalle = false;
          }
        })
      )
      .subscribe({
        next: detalle => {

          if (secuencia !== this.secuenciaDetalle) {
            return;
          }

          if (
            detalle.idAgencia !== Number(this.idAgencia)
          ) {
            this.mensajeError =
              'El comprobante no corresponde a la agencia seleccionada.';
            return;
          }

          this.comprobanteSeleccionado = detalle;
        },
        error: error => {

          if (secuencia !== this.secuenciaDetalle) {
            return;
          }

          this.mensajeError = this.obtenerMensajeError(
            error,
            'No fue posible consultar el detalle del comprobante.'
          );
        }
      });
  }

  /**
   * Cierra el detalle seleccionado y cancela
   * cualquier decisión que no se haya enviado.
   */
  cerrarDetalle(): void {

    if (this.procesandoDecision) {
      return;
    }

    this.secuenciaDetalle++;

    this.comprobanteSeleccionado = null;
    this.cargandoDetalle = false;

    this.cancelarDecision();
  }

  // =========================================================
  // VALIDACIONES DE DECISIÓN
  // =========================================================

  /**
   * Comprueba si el comprobante sigue pendiente
   * y si el usuario dispone del permiso de ejecución.
   *
   * La validación definitiva corresponde al backend.
   */
  get puedeDecidir(): boolean {

    return this.puedeEjecutar &&
      this.comprobanteSeleccionado?.estado === 'PENDIENTE' &&
      !this.procesandoDecision;
  }

  /**
   * Valida el motivo requerido para rechazar o anular.
   */
  get motivoValido(): boolean {

    if (
      this.decisionSeleccionada === 'RECHAZAR' ||
      this.decisionSeleccionada === 'ANULAR'
    ) {
      const motivo = this.motivoDecision.trim();

      return motivo.length > 0 && motivo.length <= 500;
    }

    return true;
  }

  /**
   * Comprueba que exista una decisión preparada
   * y que sus datos sean válidos para enviarse.
   */
  get puedeConfirmarDecision(): boolean {

    return this.puedeDecidir &&
      this.decisionSeleccionada !== null &&
      this.motivoValido;
  }

  // =========================================================
  // PREPARAR DECISIÓN
  // =========================================================

  /**
   * Abre la confirmación de la decisión solicitada.
   * No envía ninguna operación al backend.
   */
  iniciarDecision(
    decision: DecisionComprobante
  ): void {

    if (!this.puedeDecidir) {
      return;
    }

    this.decisionSeleccionada = decision;
    this.motivoDecision = '';
    this.mostrarConfirmacion = true;

    this.mensajeError = '';
    this.mensajeInformativo = '';
  }

  /**
   * Descarta la decisión seleccionada y limpia
   * el motivo y el cuadro de confirmación.
   */
  cancelarDecision(): void {

    this.decisionSeleccionada = null;
    this.motivoDecision = '';
    this.mostrarConfirmacion = false;
  }

  // =========================================================
  // EJECUTAR DECISIÓN
  // =========================================================

  /**
   * Envía la decisión al backend.
   *
   * El backend debe verificar nuevamente el estado,
   * los permisos, el usuario elaborador y las condiciones
   * de contabilización o de movimientos de caja.
   */
  confirmarDecision(): void {

    if (!this.puedeConfirmarDecision) {
      this.mensajeError =
        'Revise la decisión y el motivo antes de continuar.';
      return;
    }

    const comprobante = this.comprobanteSeleccionado;
    const decision = this.decisionSeleccionada;

    if (!comprobante || !decision) {
      return;
    }

    const request: ComprobanteDecisionRequest = {
      decision,
      motivo: decision === 'AUTORIZAR'
        ? null
        : this.motivoDecision.trim()
    };

    this.procesandoDecision = true;
    this.mostrarConfirmacion = false;

    this.mensajeError = '';
    this.mensajeInformativo = '';
    this.resultadoDecision = null;

    this.api.procesarDecision(
      comprobante.idComprobante,
      request
    )
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.procesandoDecision = false;
        })
      )
      .subscribe({
        next: resultado => {

          this.resultadoDecision = resultado;

          this.mensajeInformativo =
            resultado.mensaje ||
            'La decisión fue procesada correctamente.';

          this.comprobanteSeleccionado = null;
          this.cancelarDecision();

          this.consultarPendientes();
        },
        error: error => {

          this.mensajeError = this.obtenerMensajeError(
            error,
            'No fue posible procesar la decisión.'
          );

          // El comprobante podría haber cambiado de estado
          // por otra operación. Se actualiza la bandeja.
          this.consultarPendientes();
        }
      });
  }

  // =========================================================
  // PRESENTACIÓN
  // =========================================================

  /**
   * Devuelve una descripción legible del proceso.
   */
  nombreProceso(proceso: string | null): string {

    const nombres: Record<string, string> = {
      RETIRO_BANCARIO: 'Retiro bancario',
      CONSIGNACION: 'Consignación bancaria',
      TRASLADO_CAJAS: 'Movimiento entre cajas',
      TRASLADO_BANCARIO: 'Traslado bancario'
    };

    return nombres[proceso ?? ''] ??
      (proceso ?? 'Sin proceso');
  }

  /**
   * Devuelve la diferencia contable de la cabecera.
   */
  get diferenciaContable(): number {

    const comprobante = this.comprobanteSeleccionado;

    if (!comprobante) {
      return 0;
    }

    return Number(comprobante.totalDebito ?? 0) -
      Number(comprobante.totalCredito ?? 0);
  }

  /**
   * Identifica si las partidas del comprobante
   * están cuadradas según los totales recibidos.
   */
  get comprobanteCuadrado(): boolean {

    return !!this.comprobanteSeleccionado &&
      Math.abs(this.diferenciaContable) < 0.005;
  }

  // =========================================================
  // MENSAJES
  // =========================================================

  /**
   * Limpia los mensajes y el resultado de una
   * decisión anterior.
   */
  limpiarMensajes(): void {

    this.mensajeError = '';
    this.mensajeInformativo = '';
    this.resultadoDecision = null;
  }

  /**
   * Extrae un mensaje legible de errores HTTP.
   * Conserva el mensaje de negocio cuando el backend
   * lo proporciona.
   */
  private obtenerMensajeError(
    error: unknown,
    mensajeDefecto: string
  ): string {

    if (error instanceof HttpErrorResponse) {

      const respuesta = error.error;

      if (typeof respuesta === 'string' && respuesta.trim()) {
        return respuesta;
      }

      if (respuesta && typeof respuesta === 'object') {

        const datos = respuesta as {
          message?: string;
          mensaje?: string;
          error?: string;
        };

        return datos.mensaje ||
          datos.message ||
          datos.error ||
          mensajeDefecto;
      }
    }

    return mensajeDefecto;
  }

  /**
   * Limpia el comprobante seleccionado, cancela cualquier
   * decisión pendiente y regresa a la bandeja.
   *
   * No modifica ni elimina registros del backend.
   */
  volverABandeja(): void {

    if (this.procesandoDecision) {
      return;
    }

    this.cerrarDetalle();

    this.filtroProceso = '';
    this.filtroSoporte = '';

    this.limpiarMensajes();

    this.consultarPendientes();
  }
}
