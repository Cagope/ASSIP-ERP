import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  LiquidacionV2Api,
  LiquidacionV2DetalleDTO,
  LiquidacionV2PreviewItemDTO,
  LiquidacionV2PreviewResponseDTO,
  LiquidacionV2RequestDTO
} from '../liquidacion-v2.api';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { SessionService } from '../../../../core/auth/session.service';

interface PeriodoAutomaticoDTO {
  idPeriodo: number;
  anio: number;
  mes: number;
  numeroPeriodo: number;
  tipoPeriodo: string;
}

@Component({
  standalone: true,
  selector: 'app-liquidacion-v2-preview',
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './liquidacion-v2-preview.component.html',
  styleUrls: ['./liquidacion-v2-preview.component.scss']
})
export class LiquidacionV2PreviewComponent implements OnInit {

  private readonly api = inject(LiquidacionV2Api);

  public readonly session = inject(SessionService);

  // =========================================================
  // ESTADO UI
  // =========================================================
  cargando = false;
  ejecutando = false;
  cargandoPeriodo = false;

  error = '';
  mensaje = '';

  // =========================================================
  // FILTROS
  // =========================================================
  form: LiquidacionV2RequestDTO = {
    fkAgencia: null,
    idPeriodoNomina: null
  };

  periodoLabel = 'Automático · Sin agencia seleccionada';

  // =========================================================
  // RESULTADO
  // =========================================================
  resultado: LiquidacionV2PreviewResponseDTO | null = null;

  // =========================================================
  // RESUMEN GLOBAL
  // =========================================================
  totalContratos = 0;
  totalDevengados = 0;
  totalDeducciones = 0;
  totalNeto = 0;

  // =========================================================
  // DETALLE
  // =========================================================
  expandidoContratoId: number | null = null;

  // =========================================================
  // INIT
  // =========================================================
  ngOnInit(): void {
    const agencias = this.session.getAgencias?.() ?? [];

    if (agencias.length === 1) {
      this.form.fkAgencia = agencias[0].idAgencia;
      this.onCambioAgencia();
    }
  }

  // =========================================================
  // GETTERS UI
  // =========================================================
  get items(): LiquidacionV2PreviewItemDTO[] {
    return this.resultado?.items ?? [];
  }

  get hayResultado(): boolean {
    return !!this.resultado;
  }

  get hayItems(): boolean {
    return this.items.length > 0;
  }

  get puedePrevisualizar(): boolean {
    return !this.cargando && !this.ejecutando && !this.cargandoPeriodo;
  }

  get puedeEjecutar(): boolean {
    return !this.cargando && !this.ejecutando && !this.cargandoPeriodo && this.hayItems;
  }

  // =========================================================
  // CAMBIO DE AGENCIA
  // =========================================================
  onCambioAgencia(): void {

    if (this.cargando || this.ejecutando) {
      return;
    }

    this.limpiarResultado();
    this.limpiarMensajes();

    this.form.idPeriodoNomina = null;

    if (!this.form.fkAgencia) {
      this.periodoLabel = 'Automático · Sin agencia seleccionada';
      return;
    }

    this.periodoLabel = 'Automático · Buscando período disponible...';
    this.cargandoPeriodo = true;

    this.api.primerPeriodoDisponible(this.form.fkAgencia).subscribe({
      next: (periodo: PeriodoAutomaticoDTO | null) => {
        this.cargandoPeriodo = false;

        if (!periodo) {
          this.form.idPeriodoNomina = null;
          this.periodoLabel = 'Automático · Sin período ABIERTO';
          return;
        }

        this.form.idPeriodoNomina = periodo.idPeriodo;
        this.periodoLabel = this.construirPeriodoLabel(periodo);
      },
      error: (err) => {
        this.cargandoPeriodo = false;
        this.form.idPeriodoNomina = null;
        this.periodoLabel = 'Automático · Error consultando período';
        this.error = this.extraerError(
          err,
          'No se pudo consultar el período disponible para la agencia seleccionada.'
        );
      }
    });
  }

  // =========================================================
  // PREVIEW
  // =========================================================
  preview(): void {

    if (this.cargando || this.ejecutando || this.cargandoPeriodo) {
      return;
    }

    this.limpiarMensajes();

    if (!this.form.fkAgencia) {
      this.error = 'Debe seleccionar la agencia.';
      return;
    }

    if (!this.form.idPeriodoNomina) {
      this.error = 'No existe un período ABIERTO disponible para la agencia seleccionada.';
      return;
    }

    this.cargando = true;
    this.expandidoContratoId = null;

    this.api.preview(this.form).subscribe({
      next: (data) => {
        this.resultado = data ?? null;
        this.recalcularTotales();
        this.cargando = false;

        if (!this.hayItems) {
          this.mensaje = 'No se encontraron contratos para la agencia seleccionada en el período disponible.';
        }
      },
      error: (err) => {
        this.error = this.extraerError(err, 'No se pudo generar el preview V2.');
        this.cargando = false;
      }
    });
  }

  // =========================================================
  // EJECUTAR
  // =========================================================
  ejecutar(): void {

    if (this.cargando || this.ejecutando || this.cargandoPeriodo) {
      return;
    }

    this.limpiarMensajes();

    if (!this.form.fkAgencia) {
      this.error = 'Debe seleccionar la agencia.';
      return;
    }

    if (!this.form.idPeriodoNomina) {
      this.error = 'No existe un período ABIERTO disponible para la agencia seleccionada.';
      return;
    }

    if (!this.hayItems) {
      this.error = 'Debe generar el preview antes de ejecutar la liquidación.';
      return;
    }

    const agenciaTexto = this.agenciaSeleccionadaLabel();
    const totalContratos = this.items.length;

    const ok = confirm(
      `Se ejecutará la liquidación V2.\n\n` +
      `Agencia: ${agenciaTexto}\n` +
      `Período: ${this.periodoLabel}\n` +
      `Contratos a procesar: ${totalContratos}\n\n` +
      `¿Desea continuar?`
    );

    if (!ok) {
      return;
    }

    this.ejecutando = true;

    const body: LiquidacionV2RequestDTO = {
      fkAgencia: this.form.fkAgencia,
      idPeriodoNomina: this.resultado?.idPeriodoNomina ?? null
    };

    this.api.ejecutar(body).subscribe({
      next: () => {
        this.ejecutando = false;
        this.mensaje = 'La liquidación V2 fue ejecutada correctamente.';
        this.limpiarResultado();
        this.form.idPeriodoNomina = null;
        this.onCambioAgencia();
      },
      error: (err) => {
        this.error = this.extraerError(err, 'No se pudo ejecutar la liquidación V2.');
        this.ejecutando = false;
      }
    });
  }

  // =========================================================
  // DETALLE
  // =========================================================
  toggleDetalle(item: LiquidacionV2PreviewItemDTO): void {
    this.expandidoContratoId =
      this.expandidoContratoId === item.idContrato ? null : item.idContrato;
  }

  detalleVisible(item: LiquidacionV2PreviewItemDTO): boolean {
    return this.expandidoContratoId === item.idContrato;
  }

  // =========================================================
  // RESUMEN
  // =========================================================
  private recalcularTotales(): void {
    const items = this.resultado?.items ?? [];

    let devengados = 0;
    let deducciones = 0;
    let neto = 0;

    for (const item of items) {
      devengados += Number(item.totalDevengados ?? 0);
      deducciones += Number(item.totalDeducciones ?? 0);
      neto += Number(item.netoPagar ?? 0);
    }

    this.totalContratos = items.length;
    this.totalDevengados = devengados;
    this.totalDeducciones = deducciones;
    this.totalNeto = neto;
  }

  // =========================================================
  // LIMPIEZA
  // =========================================================
  limpiar(): void {
    this.form = {
      fkAgencia: null,
      idPeriodoNomina: null
    };

    this.periodoLabel = 'Automático · Sin agencia seleccionada';
    this.limpiarResultado();
    this.limpiarMensajes();
  }

  private limpiarResultado(): void {
    this.resultado = null;
    this.expandidoContratoId = null;
    this.totalContratos = 0;
    this.totalDevengados = 0;
    this.totalDeducciones = 0;
    this.totalNeto = 0;
  }

  private limpiarMensajes(): void {
    this.error = '';
    this.mensaje = '';
  }

  // =========================================================
  // APOYO UI
  // =========================================================
  private construirPeriodoLabel(periodo: PeriodoAutomaticoDTO): string {
    const mes = String(periodo.mes).padStart(2, '0');
    const numero = periodo.numeroPeriodo != null ? `Q${periodo.numeroPeriodo}` : '';
    const tipo = periodo.tipoPeriodo ? `(${periodo.tipoPeriodo})` : '';

    return `Automático · ${periodo.anio}-${mes} ${numero} ${tipo}`
      .replace(/\s+/g, ' ')
      .trim();
  }

  private agenciaSeleccionadaLabel(): string {
    const agencias = this.session.getAgencias?.() ?? [];
    const agencia = agencias.find((a: any) => Number(a.idAgencia) === Number(this.form.fkAgencia));

    if (!agencia) {
      return String(this.form.fkAgencia ?? '');
    }

    return `${agencia.codigoAgencia} — ${agencia.nombreAgencia}`;
  }

  private extraerError(err: any, defecto: string): string {
    return err?.error?.message || err?.error?.error || defecto;
  }

  // =========================================================
  // TRACK BY
  // =========================================================
  trackByContrato(_: number, item: LiquidacionV2PreviewItemDTO): number {
    return item.idContrato;
  }

  trackByDetalle(index: number, item: LiquidacionV2DetalleDTO): string {
    return `${item.tipo}-${item.codigoConcepto}-${index}`;
  }
}
