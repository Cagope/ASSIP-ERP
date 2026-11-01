
import { CommonModule } from '@angular/common';
import { Component, OnDestroy, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin, Subject, takeUntil } from 'rxjs';
import {
  ConsultaCreditosBuscadorComponent,
  ConsultaCreditosAsociado
} from '../consulta-creditos/buscador/consulta-creditos-buscador.component';
import { PagoCuotasCreditoResumen } from '../pago-cuotas_v1/pago-cuotas_v1.models';
import { ReversionPagosApi } from './reversion-pagos.api';
import {
  ReversionPagoOriginal,
  ReversionVistaPrevia,
  ReversionComprobanteContrario,
  ReversionEstadoRestitucion,
  ReversionResultado,
  ConfirmarReversionRequest
} from './reversion-pagos.models';

/**
 * Gestiona la reversión de pagos de cuotas.
 * Las validaciones y escrituras financieras pertenecen al backend.
 */
@Component({
  selector: 'app-reversion-pagos',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ConsultaCreditosBuscadorComponent
  ],
  templateUrl: './reversion-pagos.component.html',
  styleUrls: ['./reversion-pagos.component.scss']
})
export class ReversionPagosComponent implements OnDestroy {
  private readonly api = inject(ReversionPagosApi);
  private readonly destroy$ = new Subject<void>();
  private secuencia = 0;

  asociado: ConsultaCreditosAsociado | null = null;
  creditos: PagoCuotasCreditoResumen[] = [];
  creditoSeleccionado: PagoCuotasCreditoResumen | null = null;
  pagos: ReversionPagoOriginal[] = [];
  pagoSeleccionado: ReversionPagoOriginal | null = null;
  vistaPrevia: ReversionVistaPrevia | null = null;
  comprobante: ReversionComprobanteContrario | null = null;
  restitucion: ReversionEstadoRestitucion | null = null;
  resultado: ReversionResultado | null = null;

  fechaReversion = this.fechaLocal();
  motivo = '';
  confirmacionAceptada = false;
  cargandoCreditos = false;
  cargandoPagos = false;
  cargandoAnalisis = false;
  confirmando = false;
  error = '';
  mensaje = '';

  /** Indica si existe una consulta o escritura en ejecución. */
  get ocupado(): boolean {
    return this.cargandoCreditos ||
      this.cargandoPagos ||
      this.cargandoAnalisis ||
      this.confirmando;
  }

  /** Habilita la confirmación solo con validaciones favorables. */
  get puedeConfirmar(): boolean {
    return !this.ocupado &&
      this.pagoSeleccionado !== null &&
      this.vistaPrevia?.elegibleParaAnalisis === true &&
      this.comprobante !== null &&
      this.restitucion?.consistente === true &&
      this.confirmacionAceptada &&
      this.fechaReversion.length === 10 &&
      this.motivo.trim().length > 0 &&
      this.resultado === null;
  }

  /** Selecciona el asociado y consulta sus créditos autorizados. */
  seleccionarAsociado(asociado: ConsultaCreditosAsociado): void {
    if (this.ocupado) return;
    const secuencia = ++this.secuencia;
    this.limpiarSeleccion();
    this.asociado = asociado;
    this.cargandoCreditos = true;
    this.api.listarCreditosPorAsociado(asociado.idDatosPersonal)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: creditos => {
          if (secuencia !== this.secuencia) return;
          this.creditos = creditos ?? [];
          this.cargandoCreditos = false;
          if (!this.creditos.length) {
            this.mensaje = 'El asociado no tiene créditos disponibles.';
          }
        },
        error: error => {
          if (secuencia !== this.secuencia) return;
          this.cargandoCreditos = false;
          this.error = this.mensajeError(error);
        }
      });
  }

  /** Limpia la selección cuando se reinicia el buscador. */
  limpiarAsociado(): void {
    if (this.ocupado) return;
    ++this.secuencia;
    this.limpiarSeleccion();
    this.asociado = null;
  }

  /** Consulta los pagos registrados del crédito seleccionado. */
  seleccionarCredito(credito: PagoCuotasCreditoResumen): void {
    if (this.ocupado) return;
    const secuencia = ++this.secuencia;
    this.limpiarPago();
    this.creditoSeleccionado = credito;
    this.pagos = [];
    this.cargandoPagos = true;
    this.api.listarPagos(credito.idCarteraCredito)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: pagos => {
          if (secuencia !== this.secuencia) return;
          this.pagos = pagos ?? [];
          this.cargandoPagos = false;
          if (!this.pagos.length) {
            this.mensaje = 'El crédito no tiene pagos registrados.';
          }
        },
        error: error => {
          if (secuencia !== this.secuencia) return;
          this.cargandoPagos = false;
          this.error = this.mensajeError(error);
        }
      });
  }

  /**
   * Consulta la elegibilidad inicial y, si procede, las
   * contrapartidas contables y el estado de restitución.
   */
  seleccionarPago(pago: ReversionPagoOriginal): void {
    if (this.ocupado || this.resultado !== null) return;
    const secuencia = ++this.secuencia;
    this.limpiarAnalisis();
    this.pagoSeleccionado = pago;
    this.cargandoAnalisis = true;
    this.api.consultarVistaPrevia(pago.idPago)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: vista => {
          if (secuencia !== this.secuencia) return;
          this.vistaPrevia = vista;
          if (!vista.elegibleParaAnalisis) {
            this.cargandoAnalisis = false;
            this.mensaje = vista.observacion;
            return;
          }
          this.consultarValidaciones(pago.idPago, secuencia);
        },
        error: error => {
          if (secuencia !== this.secuencia) return;
          this.cargandoAnalisis = false;
          this.error = this.mensajeError(error);
        }
      });
  }

  /** Obtiene conjuntamente las validaciones financieras de lectura. */
  private consultarValidaciones(idPago: number, secuencia: number): void {
    forkJoin({
      comprobante: this.api.consultarComprobanteContrario(idPago),
      restitucion: this.api.consultarEstadoRestitucion(idPago)
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: respuesta => {
          if (secuencia !== this.secuencia) return;
          this.comprobante = respuesta.comprobante;
          this.restitucion = respuesta.restitucion;
          this.cargandoAnalisis = false;
          if (!respuesta.restitucion.consistente) {
            this.mensaje = respuesta.restitucion.observacion;
          }
        },
        error: error => {
          if (secuencia !== this.secuencia) return;
          this.cargandoAnalisis = false;
          this.error = this.mensajeError(error);
        }
      });
  }

  /**
   * Solicita la reversión integral al backend.
   * Bloquea nuevos envíos mientras se resuelve la transacción.
   */
  confirmarReversion(): void {
    if (!this.puedeConfirmar || !this.pagoSeleccionado) return;
    const idPago = this.pagoSeleccionado.idPago;
    const solicitud: ConfirmarReversionRequest = {
      fechaReversion: this.fechaReversion,
      motivo: this.motivo.trim()
    };
    this.confirmando = true;
    this.error = '';
    this.mensaje = '';
    this.api.confirmarReversion(idPago, solicitud)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: resultado => {
          this.resultado = resultado;
          this.confirmando = false;
          this.confirmacionAceptada = false;
          this.mensaje = 'La reversión se registró correctamente.';
        },
        error: error => {
          this.confirmando = false;
          this.confirmacionAceptada = false;
          this.error = this.mensajeError(error);
          this.mensaje = '';
        }
      });
  }

  /** Permite consultar nuevamente el crédito después de finalizar. */
  actualizarPagos(): void {
    if (this.ocupado || !this.creditoSeleccionado) return;
    this.seleccionarCredito(this.creditoSeleccionado);
  }

  /** Elimina los datos dependientes de un asociado anterior. */
  private limpiarSeleccion(): void {
    this.creditos = [];
    this.creditoSeleccionado = null;
    this.pagos = [];
    this.limpiarPago();
  }

  /** Elimina el pago y las validaciones anteriores. */
  private limpiarPago(): void {
    this.pagoSeleccionado = null;
    this.limpiarAnalisis();
  }

  /** Restablece el formulario de confirmación y las consultas previas. */
  private limpiarAnalisis(): void {
    this.vistaPrevia = null;
    this.comprobante = null;
    this.restitucion = null;
    this.resultado = null;
    this.confirmacionAceptada = false;
    this.motivo = '';
    this.fechaReversion = this.fechaLocal();
    this.error = '';
    this.mensaje = '';
  }

  /** Obtiene la fecha del navegador en formato local ISO. */
  private fechaLocal(): string {
    const ahora = new Date();
    const anio = ahora.getFullYear();
    const mes = String(ahora.getMonth() + 1).padStart(2, '0');
    const dia = String(ahora.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }

  /** Convierte los errores HTTP en mensajes visibles para el operador. */
  private mensajeError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 403) {
        return 'No tiene autorización para realizar esta operación.';
      }
      if (error.status === 0) {
        return 'No fue posible establecer comunicación con el servidor.';
      }
      const respuesta = error.error;
      if (typeof respuesta === 'string' && respuesta.trim()) {
        return respuesta;
      }
      if (respuesta && typeof respuesta.message === 'string') {
        return respuesta.message;
      }
      if (respuesta && typeof respuesta.mensaje === 'string') {
        return respuesta.mensaje;
      }
    }
    return 'No fue posible completar la operación solicitada.';
  }

  /** Finaliza las suscripciones cuando se destruye el componente. */
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
