import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { ConsultaCreditosBuscadorComponent, ConsultaCreditosAsociado } from '../consulta-creditos/buscador/consulta-creditos-buscador.component';
import { PagoCuotasCreditoResumen } from '../pago-cuotas_v1/pago-cuotas_v1.models';
import { ReversionProrrogasApi } from './reversion-prorrogas.api';
import { ProrrogaResumen, ResultadoReversionProrroga, VistaPreviaReversionProrroga } from './reversion-prorrogas.models';

/** Selecciona asociado, crédito y prórroga; confirma únicamente contra el backend. */
@Component({
  selector: 'app-reversion-prorrogas',
  standalone: true,
  imports: [CommonModule, FormsModule, ConsultaCreditosBuscadorComponent],
  templateUrl: './reversion-prorrogas.component.html',
  styleUrls: ['./reversion-prorrogas.component.scss']
})
export class ReversionProrrogasComponent implements OnDestroy {
  private readonly api = inject(ReversionProrrogasApi);
  private readonly destroy$ = new Subject<void>();
  private secuencia = 0;
  asociado: ConsultaCreditosAsociado | null = null;
  creditos: PagoCuotasCreditoResumen[] = [];
  credito: PagoCuotasCreditoResumen | null = null;
  prorrogas: ProrrogaResumen[] = [];
  seleccionada: ProrrogaResumen | null = null;
  resultado: ResultadoReversionProrroga | null = null;
  vistaPrevia: VistaPreviaReversionProrroga | null = null;
  consultandoVistaPrevia = false;
  private secuenciaVistaPrevia = 0;
  fechaReversion = this.fechaLocal();
  motivo = '';
  aceptado = false;
  cargando = false;
  confirmando = false;
  error = '';
  mensaje = '';

  get ocupado(): boolean { return this.cargando || this.confirmando; }
  get puedeConfirmar(): boolean {
    return !this.ocupado && !this.consultandoVistaPrevia && this.vistaPrevia?.elegiblePreliminar === true && this.credito !== null && this.seleccionada?.estado === 'A'
      && this.resultado === null && this.aceptado && /^\d{4}-\d{2}-\d{2}$/.test(this.fechaReversion)
      && this.motivo.trim().length > 0;
  }

  /** Selecciona un asociado y carga sus créditos sin solicitar IDs al funcionario. */
  seleccionarAsociado(asociado: ConsultaCreditosAsociado): void {
    if (this.ocupado) return;
    const secuencia = ++this.secuencia;
    this.limpiar();
    this.asociado = asociado;
    this.cargando = true;
    this.api.listarCreditos(asociado.idDatosPersonal).pipe(takeUntil(this.destroy$)).subscribe({
      next: creditos => {
        if (secuencia !== this.secuencia) return;
        this.creditos = creditos ?? [];
        this.cargando = false;
        if (!this.creditos.length) this.mensaje = 'El asociado no tiene créditos disponibles.';
      },
      error: e => this.mostrarError(e, secuencia)
    });
  }

  /** Consulta el historial de prórrogas del crédito seleccionado. */
  seleccionarCredito(credito: PagoCuotasCreditoResumen): void {
    if (this.ocupado) return;
    const secuencia = ++this.secuencia;
    this.credito = credito;
    this.prorrogas = [];
    this.limpiarSeleccion();
    this.cargando = true;
    this.api.listarProrrogas(credito.idCarteraCredito).pipe(takeUntil(this.destroy$)).subscribe({
      next: prorrogas => {
        if (secuencia !== this.secuencia) return;
        this.prorrogas = prorrogas ?? [];
        this.cargando = false;
        if (!this.prorrogas.length) this.mensaje = 'El crédito no registra prórrogas.';
      },
      error: e => this.mostrarError(e, secuencia)
    });
  }

  /** Selecciona una prórroga y reinicia la confirmación anterior. */
  seleccionarProrroga(prorroga: ProrrogaResumen): void {
    if (this.ocupado || this.resultado) return;
    this.limpiarSeleccion();
    this.seleccionada = prorroga;
    if (prorroga.estado !== 'A' || !this.credito) return;
    const idCredito = this.credito.idCarteraCredito;
    const secuencia = ++this.secuenciaVistaPrevia;
    this.consultandoVistaPrevia = true;
    this.api.consultarVistaPrevia(idCredito, prorroga.idProrroga)
      .pipe(takeUntil(this.destroy$)).subscribe({
        next: vista => {
          if (secuencia !== this.secuenciaVistaPrevia) return;
          this.vistaPrevia = vista;
          this.consultandoVistaPrevia = false;
        },
        error: e => {
          if (secuencia !== this.secuenciaVistaPrevia) return;
          this.consultandoVistaPrevia = false;
          this.vistaPrevia = null;
          this.mostrarError(e, this.secuencia);
        }
      });
  }

  /** Ejecuta la reversión una sola vez y muestra el comprobante generado. */
  confirmar(): void {
    if (!this.puedeConfirmar || !this.credito || !this.seleccionada) return;
    const secuencia = this.secuencia;
    this.confirmando = true;
    this.error = '';
    this.mensaje = '';
    this.api.confirmar(this.credito.idCarteraCredito, this.seleccionada.idProrroga, {
      fechaReversion: this.fechaReversion,
      motivo: this.motivo.trim()
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: resultado => {
        if (secuencia !== this.secuencia) return;
        this.resultado = resultado;
        this.confirmando = false;
        this.aceptado = false;
        this.mensaje = 'La reversión fue registrada correctamente.';
      },
      error: e => this.mostrarError(e, secuencia)
    });
  }

  /** Vuelve al buscador conservando las reglas de seguridad del backend. */
  nuevaBusqueda(): void {
    if (this.ocupado) return;
    ++this.secuencia;
    this.limpiar();
    this.asociado = null;
  }

  /** Actualiza las prórrogas del crédito tras una reversión. */
  actualizar(): void {
    if (this.credito && !this.ocupado) this.seleccionarCredito(this.credito);
  }

  private limpiar(): void {
    this.creditos = [];
    this.credito = null;
    this.prorrogas = [];
    this.limpiarSeleccion();
  }
  private limpiarSeleccion(): void {
    ++this.secuenciaVistaPrevia;
    this.vistaPrevia = null;
    this.consultandoVistaPrevia = false;
    this.seleccionada = null;
    this.resultado = null;
    this.aceptado = false;
    this.motivo = '';
    this.fechaReversion = this.fechaLocal();
    this.error = '';
    this.mensaje = '';
  }
  private fechaLocal(): string {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
  private mostrarError(e: unknown, secuencia: number): void {
    if (secuencia !== this.secuencia) return;
    this.cargando = false;
    this.confirmando = false;
    this.aceptado = false;
    if (e instanceof HttpErrorResponse) {
      if (e.status === 403) { this.error = 'No tiene autorización para esta operación.'; return; }
      if (e.status === 0) { this.error = 'No hay comunicación con el servidor.'; return; }
      if (typeof e.error === 'string' && e.error.trim()) { this.error = e.error; return; }
      if (typeof e.error?.message === 'string') { this.error = e.error.message; return; }
      if (typeof e.error?.mensaje === 'string') { this.error = e.error.mensaje; return; }
    }
    this.error = 'No fue posible completar la operación.';
  }
  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }
}
