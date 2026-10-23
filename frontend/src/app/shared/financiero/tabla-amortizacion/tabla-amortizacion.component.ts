import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  Output
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

import { TablaAmortizacionApi } from './tabla-amortizacion.api';
import {
  TablaAmortizacionRequest,
  TablaAmortizacionResponse
} from './tabla-amortizacion.models';

@Component({
  selector: 'app-tabla-amortizacion',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tabla-amortizacion.component.html',
  styleUrls: ['./tabla-amortizacion.component.scss']
})
export class TablaAmortizacionComponent implements OnDestroy {

  @Input() titulo = 'Tabla de amortización';
  @Output() cerrado = new EventEmitter<void>();

  visible = false;
  cargando = false;
  error = '';

  requestActual: TablaAmortizacionRequest | null = null;
  resultado: TablaAmortizacionResponse | null = null;

  private consultaActual?: Subscription;
  private versionConsulta = 0;

  constructor(private readonly api: TablaAmortizacionApi) {}

  abrir(request: TablaAmortizacionRequest): void {
    this.cancelarConsulta();
    this.requestActual = { ...request };
    this.resultado = null;
    this.error = '';
    this.visible = true;
    this.calcular();
  }

  abrirCredito(idCarteraCredito: number): void {
    if (!Number.isInteger(idCarteraCredito) || idCarteraCredito <= 0) {
      this.error = 'El identificador del crédito no es válido.';
      return;
    }

    this.cancelarConsulta();
    this.requestActual = null;
    this.resultado = null;
    this.error = '';
    this.visible = true;
    this.cargando = true;

    const version = this.versionConsulta;
    this.consultaActual = this.api.consultarParametrosCredito(idCarteraCredito)
      .subscribe({
        next: parametros => {
          if (version !== this.versionConsulta) return;
          this.requestActual = { ...parametros };
          this.cargando = false;
          this.calcular();
        },
        error: error => {
          if (version !== this.versionConsulta) return;
          this.cargando = false;
          this.error = this.obtenerMensajeError(
            error,
            'No fue posible consultar las condiciones del crédito.'
          );
        }
      });
  }

  recalcular(): void {
    if (!this.requestActual || this.cargando) return;
    this.calcular();
  }

  cerrar(): void {
    if (this.cargando) return;
    this.cancelarConsulta();
    this.visible = false;
    this.error = '';
    this.cerrado.emit();
  }

  ngOnDestroy(): void {
    this.cancelarConsulta();
  }

  private calcular(): void {
    if (!this.requestActual) return;

    this.consultaActual?.unsubscribe();
    this.cargando = true;
    this.error = '';
    const version = this.versionConsulta;

    this.consultaActual = this.api.calcular(this.requestActual).subscribe({
      next: respuesta => {
        if (version !== this.versionConsulta) return;
        this.resultado = respuesta;
        this.cargando = false;
      },
      error: error => {
        if (version !== this.versionConsulta) return;
        this.resultado = null;
        this.cargando = false;
        this.error = this.obtenerMensajeError(
          error,
          'No fue posible calcular la tabla de amortización.'
        );
      }
    });
  }

  private cancelarConsulta(): void {
    this.versionConsulta++;
    this.consultaActual?.unsubscribe();
    this.consultaActual = undefined;
    this.cargando = false;
  }

  private obtenerMensajeError(error: unknown, predeterminado: string): string {
    if (!error || typeof error !== 'object') return predeterminado;

    const respuesta = error as {
      message?: string;
      error?: {
        mensaje?: string;
        message?: string;
        error?: string;
      } | string;
    };

    if (typeof respuesta.error === 'string') {
      return respuesta.error || predeterminado;
    }

    return (
      (typeof respuesta.error === 'object' ? respuesta.error?.mensaje : undefined)
      ?? (typeof respuesta.error === 'object' ? respuesta.error?.message : undefined)
      ?? (typeof respuesta.error === 'object' ? respuesta.error?.error : undefined)
      ?? respuesta.message
      ?? predeterminado
    );
  }
}
