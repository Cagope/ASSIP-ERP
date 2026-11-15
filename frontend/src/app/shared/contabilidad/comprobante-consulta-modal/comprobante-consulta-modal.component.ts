
import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  inject
} from '@angular/core';
import { finalize } from 'rxjs';
import { ComprobantesConsultaService } from '../../../features/contabilidad/comprobantes/consulta/comprobantes-consulta.api';
import { ComprobanteCompleto } from '../../../features/contabilidad/comprobantes/consulta/comprobantes-consulta.models';

/** Identificación de un comprobante contable para su consulta. */
export interface ReferenciaComprobanteModal {
  idAgencia: number;
  tipoComprobante: string;
  numeroComprobante: string;
}

/**
 * Modal compartido para consultar comprobantes contables.
 * No permite editar ni modificar información financiera.
 */
@Component({
  selector: 'app-comprobante-consulta-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './comprobante-consulta-modal.component.html',
  styleUrl: './comprobante-consulta-modal.component.scss'
})
export class ComprobanteConsultaModalComponent implements OnChanges {
  private readonly api = inject(ComprobantesConsultaService);

  @Input() referencia: ReferenciaComprobanteModal | null = null;

  @Output() cerrar = new EventEmitter<void>();

  comprobante: ComprobanteCompleto | null = null;
  cargando = false;
  error = '';

  private solicitudActual = 0;

  private readonly formatoMoneda = new Intl.NumberFormat('es-CO', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });

  /**
   * Consulta automáticamente el comprobante cuando cambia su referencia.
   * Ignora resultados de solicitudes anteriores.
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['referencia']) {
      this.consultar();
    }
  }

  /** Recupera el comprobante completo utilizando la API existente. */
  private consultar(): void {
    const referencia = this.referencia;
    const solicitud = ++this.solicitudActual;

    this.comprobante = null;
    this.error = '';
    this.cargando = false;

    if (!referencia) {
      return;
    }

    if (
      !Number.isSafeInteger(referencia.idAgencia) ||
      referencia.idAgencia <= 0 ||
      !referencia.tipoComprobante?.trim() ||
      !referencia.numeroComprobante?.trim()
    ) {
      this.error = 'La identificación del comprobante es inválida.';
      return;
    }

    this.cargando = true;

    this.api.consultar(
      referencia.idAgencia,
      referencia.tipoComprobante,
      referencia.numeroComprobante
    )
      .pipe(
        finalize(() => {
          if (solicitud === this.solicitudActual) {
            this.cargando = false;
          }
        })
      )
      .subscribe({
        next: datos => {
          if (solicitud !== this.solicitudActual) {
            return;
          }

          this.comprobante = datos;
        },
        error: err => {
          if (solicitud !== this.solicitudActual) {
            return;
          }

          this.comprobante = null;
          this.error = err.error?.message ||
            'No se pudo consultar el comprobante contable.';
        }
      });
  }

  /** Presenta valores monetarios con formato colombiano. */
  dinero(valor: number | null | undefined): string {
    return valor == null
      ? '—'
      : this.formatoMoneda.format(Number(valor));
  }

  /** Presenta fechas ISO sin desplazamientos por zona horaria. */
  formatearFecha(fecha: string | null | undefined): string {
    if (!fecha) {
      return '—';
    }

    const fechaIso = fecha.substring(0, 10);

    if (!/^\d{4}-\d{2}-\d{2}$/.test(fechaIso)) {
      return fecha;
    }

    const [ano, mes, dia] = fechaIso.split('-');
    return `${dia}/${mes}/${ano}`;
  }

  /** Calcula los totales del comprobante sin modificar sus movimientos. */
  total(campo: 'valorDebito' | 'valorCredito'): number {
    return (this.comprobante?.movimientos || []).reduce(
      (acumulado, movimiento) =>
        acumulado + Number(movimiento[campo] || 0),
      0
    );
  }

  /** Notifica al componente origen que debe cerrar el modal. */
  cerrarVentana(): void {
    this.cerrar.emit();
  }
}
