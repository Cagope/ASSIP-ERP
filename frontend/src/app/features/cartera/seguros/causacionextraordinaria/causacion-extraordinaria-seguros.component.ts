import { CommonModule } from '@angular/common';
import { Component, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProrrogasNumericFormatDirective } from '../../prorrogas_v1/prorrogas-numeric-format.directive';
import { Subject, takeUntil } from 'rxjs';
import {
  ConsultaCreditosAsociado,
  ConsultaCreditosBuscadorComponent
} from '../../consulta-creditos/buscador/consulta-creditos-buscador.component';
import { ConsultaCreditoResumen } from '../../consulta-creditos/consulta-creditos.models';
import { CausacionExtraordinariaSegurosApi } from './causacion-extraordinaria-seguros.api';
import {
  CausacionExtraordinariaSegurosResultado,
  CausacionExtraordinariaSegurosSolicitud,
  CausacionExtraordinariaSegurosSugerencia,
  CausacionExtraordinariaSegurosVistaPrevia
} from './causacion-extraordinaria-seguros.models';

/** Captura una causación extraordinaria y exige revisar su NC antes de confirmarla. */
@Component({
  selector: 'app-causacion-extraordinaria-seguros',
  standalone: true,
  imports: [CommonModule, FormsModule, ConsultaCreditosBuscadorComponent, ProrrogasNumericFormatDirective],
  templateUrl: './causacion-extraordinaria-seguros.component.html',
  styleUrls: ['./causacion-extraordinaria-seguros.component.scss']
})
export class CausacionExtraordinariaSegurosComponent implements OnDestroy {
  private readonly destruir$ = new Subject<void>();
  private versionConsulta = 0;

  asociadoSeleccionado: ConsultaCreditosAsociado | null = null;
  creditos: ConsultaCreditoResumen[] = [];
  creditoSeleccionado: ConsultaCreditoResumen | null = null;
  sugerencia: CausacionExtraordinariaSegurosSugerencia | null = null;
  solicitud: CausacionExtraordinariaSegurosSolicitud | null = null;
  vistaPrevia: CausacionExtraordinariaSegurosVistaPrevia | null = null;
  resultado: CausacionExtraordinariaSegurosResultado | null = null;

  cargando = false;
  procesando = false;
  error: string | null = null;
  aviso: string | null = null;

  constructor(private readonly api: CausacionExtraordinariaSegurosApi) {}

  /** Consulta los créditos activos del asociado seleccionado. */
  seleccionarAsociado(asociado: ConsultaCreditosAsociado): void {
    if (this.procesando) return;
    const version = ++this.versionConsulta;
    this.asociadoSeleccionado = asociado;
    this.creditos = [];
    this.limpiarCredito();
    this.error = null;
    this.aviso = null;
    this.cargando = true;
    this.api.listarCreditosPorAsociado(asociado.idDatosPersonal)
      .pipe(takeUntil(this.destruir$))
      .subscribe({
        next: creditos => {
          if (version !== this.versionConsulta) return;
          this.creditos = (creditos ?? []).filter(credito =>
            credito.codigoEstadoCartera === 'A' && Number(credito.saldoActual ?? 0) > 0
          );
          this.cargando = false;
        },
        error: error => {
          if (version !== this.versionConsulta) return;
          this.cargando = false;
          this.error = this.mensajeError(error, 'No fue posible consultar los créditos.');
        }
      });
  }

  /** Obtiene los valores sugeridos de la configuración vigente del seguro. */
  seleccionarCredito(credito: ConsultaCreditoResumen): void {
    if (this.procesando) return;
    const idCredito = credito.idCarteraCredito;
    if (!idCredito) {
      this.error = 'El crédito seleccionado no es válido.';
      return;
    }
    const version = ++this.versionConsulta;
    this.limpiarCredito();
    this.creditoSeleccionado = credito;
    this.error = null;
    this.aviso = null;
    this.cargando = true;
    this.api.consultarSugerencia(idCredito)
      .pipe(takeUntil(this.destruir$))
      .subscribe({
        next: sugerencia => {
          if (version !== this.versionConsulta) return;
          this.sugerencia = sugerencia;
          this.solicitud = {
            idCredito,
            fechaMovimiento: sugerencia.fechaSugerida,
            saldoBase: Number(sugerencia.saldoBase),
            valor: Number(sugerencia.valorSugerido),
            justificacion: ''
          };
          this.actualizarConcepto();
          this.cargando = false;
        },
        error: error => {
          if (version !== this.versionConsulta) return;
          this.cargando = false;
          this.error = this.mensajeError(error, 'No fue posible consultar el seguro del crédito.');
        }
      });
  }

  /** Permite seleccionar otro crédito sin repetir la búsqueda del asociado. */
  cambiarCredito(): void {
    if (this.procesando) return;
    ++this.versionConsulta;
    this.limpiarCredito();
    this.cargando = false;
    this.error = null;
    this.aviso = null;
  }

  /** Restablece la búsqueda y descarta toda vista previa anterior. */
  limpiarBusqueda(): void {
    if (this.procesando) return;
    ++this.versionConsulta;
    this.asociadoSeleccionado = null;
    this.creditos = [];
    this.limpiarCredito();
    this.cargando = false;
    this.error = null;
    this.aviso = null;
  }

  /** Recalcula el valor sugerido cuando cambia la base; el importe sigue siendo editable. */
  cambiarSaldoBase(): void {
    this.invalidarVistaPrevia();
    if (!this.solicitud || !this.sugerencia) return;
    const saldo = Number(this.solicitud.saldoBase);
    const porcentaje = Number(this.sugerencia.porcentajeAplicar);
    this.solicitud.valor = Number.isFinite(saldo) && saldo >= 0 && Number.isFinite(porcentaje)
      ? Math.round(saldo * porcentaje / 100)
      : 0;
    this.actualizarConcepto();
  }

  /** Actualiza el concepto al editar manualmente el valor de la causación. */
  cambiarValor(): void {
    this.invalidarVistaPrevia();
    this.actualizarConcepto();
  }

  /** Genera el concepto con los cinco datos definidos para la causación. */
  private actualizarConcepto(): void {
    if (!this.solicitud || !this.sugerencia || !this.creditoSeleccionado || !this.asociadoSeleccionado) return;
    const dinero = (valor: number): string => new Intl.NumberFormat('es-CO', {
      style: 'currency', currency: 'COP', maximumFractionDigits: 0
    }).format(Number(valor) || 0);
    const porcentaje = new Intl.NumberFormat('es-CO', {
      minimumFractionDigits: 2, maximumFractionDigits: 4
    }).format(Number(this.sugerencia.porcentajeAplicar) || 0);
    this.solicitud.justificacion =
      `CAUSACIÓN EXTRAORDINARIA DE SEGURO DE VIDA DEUDORES. ` +
      `CÉDULA: ${this.asociadoSeleccionado.documento}. ` +
      `PAGARÉ: ${this.creditoSeleccionado.pagareCartera || '—'}. ` +
      `BASE DE CÁLCULO: ${dinero(this.solicitud.saldoBase)}. ` +
      `PORCENTAJE APLICADO: ${porcentaje} %. ` +
      `VALOR CAUSADO: ${dinero(this.solicitud.valor)}.`;
  }

  /** Invalida la NC propuesta cuando cambia cualquier dato de la solicitud. */
  invalidarVistaPrevia(): void {
    this.vistaPrevia = null;
    this.resultado = null;
    this.error = null;
    this.aviso = null;
  }

  /** Valida los datos editables antes de solicitar la vista previa o confirmar. */
  private solicitudValida(): CausacionExtraordinariaSegurosSolicitud | null {
    const s = this.solicitud;
    if (!s || !this.sugerencia || !this.creditoSeleccionado) return null;
    if (!s.fechaMovimiento || !/^\d{4}-\d{2}-\d{2}$/.test(s.fechaMovimiento)) {
      this.error = 'Debe indicar una fecha de causación válida.';
      return null;
    }
    if (!Number.isFinite(Number(s.saldoBase)) || Number(s.saldoBase) < 0) {
      this.error = 'El saldo base debe ser un valor válido no negativo.';
      return null;
    }
    if (!Number.isFinite(Number(s.valor)) || Number(s.valor) <= 0 || !Number.isInteger(Number(s.valor))) {
      this.error = 'El valor de causación debe ser un entero positivo.';
      return null;
    }
    if (!s.justificacion?.trim()) {
      this.error = 'La justificación de la causación extraordinaria es obligatoria.';
      return null;
    }
    return {
      idCredito: s.idCredito,
      fechaMovimiento: s.fechaMovimiento,
      saldoBase: Number(s.saldoBase),
      valor: Number(s.valor),
      justificacion: s.justificacion.trim()
    };
  }

  /** Genera la vista previa de la NC sin registrar movimientos. */
  previsualizar(): void {
    if (this.cargando || this.procesando) return;
    this.invalidarVistaPrevia();
    const solicitud = this.solicitudValida();
    if (!solicitud) return;
    this.procesando = true;
    this.api.previsualizar(solicitud)
      .pipe(takeUntil(this.destruir$))
      .subscribe({
        next: vista => {
          this.vistaPrevia = vista;
          this.procesando = false;
        },
        error: error => {
          this.procesando = false;
          this.error = this.mensajeError(error, 'No fue posible generar la vista previa de la NC.');
        }
      });
  }

  /** Confirma únicamente una solicitud idéntica a la vista previa mostrada. */
  confirmar(): void {
    if (this.cargando || this.procesando || !this.vistaPrevia) return;
    this.error = null;
    const solicitud = this.solicitudValida();
    const vista = this.vistaPrevia;
    if (!solicitud) return;
    if (solicitud.idCredito !== vista.idCredito ||
        solicitud.fechaMovimiento !== vista.fechaMovimiento ||
        solicitud.saldoBase !== Number(vista.saldoBase) ||
        solicitud.valor !== Number(vista.valor) ||
        solicitud.justificacion !== vista.justificacion) {
      this.vistaPrevia = null;
      this.error = 'Los datos cambiaron. Genere nuevamente la vista previa.';
      return;
    }
    this.procesando = true;
    this.api.confirmar(solicitud)
      .pipe(takeUntil(this.destruir$))
      .subscribe({
        next: resultado => {
          this.resultado = resultado;
          this.vistaPrevia = null;
          this.solicitud = null;
          this.procesando = false;
          this.aviso = `Causación registrada con comprobante ${resultado.tipoComprobante} ${resultado.numeroComprobante}.`;
        },
        error: error => {
          this.procesando = false;
          this.vistaPrevia = null;
          this.error = this.mensajeError(error, 'No fue posible confirmar la causación. Consulte los movimientos antes de reintentar.');
        }
      });
  }

  /** Limpia la selección sin afectar la lista de créditos consultada. */
  private limpiarCredito(): void {
    this.creditoSeleccionado = null;
    this.sugerencia = null;
    this.solicitud = null;
    this.vistaPrevia = null;
    this.resultado = null;
  }

  /** Obtiene un mensaje funcional del backend. */
  private mensajeError(error: any, alternativo: string): string {
    return typeof error?.error?.message === 'string'
      ? error.error.message
      : typeof error?.error === 'string' ? error.error : alternativo;
  }

  ngOnDestroy(): void {
    this.destruir$.next();
    this.destruir$.complete();
  }
}
