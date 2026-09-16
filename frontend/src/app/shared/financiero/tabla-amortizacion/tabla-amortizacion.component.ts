import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';
import { CommonModule } from '@angular/common';

import { TablaAmortizacionApi } from './tabla-amortizacion.api';
import {
  TablaAmortizacionRequest,
  TablaAmortizacionResponse
} from './tabla-amortizacion.models';

@Component({
  selector: 'app-tabla-amortizacion',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './tabla-amortizacion.component.html',
  styleUrls: ['./tabla-amortizacion.component.scss']
})
export class TablaAmortizacionComponent {

  @Input() titulo = 'Tabla de amortización';

  @Output() cerrado =
    new EventEmitter<void>();

  visible = false;
  cargando = false;
  error = '';

  requestActual:
    TablaAmortizacionRequest | null = null;

  resultado:
    TablaAmortizacionResponse | null = null;

  constructor(
    private readonly api: TablaAmortizacionApi
  ) {}

  abrir(
    request: TablaAmortizacionRequest
  ): void {

    this.requestActual = {
      ...request
    };

    this.resultado = null;
    this.error = '';
    this.visible = true;

    this.calcular();
  }

  recalcular(): void {
    if (!this.requestActual || this.cargando) {
      return;
    }

    this.calcular();
  }

  cerrar(): void {
    if (this.cargando) {
      return;
    }

    this.visible = false;
    this.error = '';
    this.cerrado.emit();
  }

  private calcular(): void {
    if (!this.requestActual) {
      return;
    }

    this.cargando = true;
    this.error = '';

    this.api
      .calcular(this.requestActual)
      .subscribe({
        next: respuesta => {
          this.resultado = respuesta;
          this.cargando = false;
        },
        error: error => {
          this.resultado = null;
          this.cargando = false;
          this.error = this.obtenerMensajeError(
            error,
            'No fue posible calcular la tabla de amortización.'
          );
        }
      });
  }

  private obtenerMensajeError(
    error: unknown,
    predeterminado: string
  ): string {

    if (!error || typeof error !== 'object') {
      return predeterminado;
    }

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
      respuesta.error?.mensaje
      ?? respuesta.error?.message
      ?? respuesta.error?.error
      ?? respuesta.message
      ?? predeterminado
    );
  }
}
