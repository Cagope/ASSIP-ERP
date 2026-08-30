import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import {
  ExpedienteAsociado
} from '../expediente-asociado.dto';

@Component({
  selector: 'app-expediente-encabezado',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './expediente-encabezado.component.html',
  styleUrls: ['./expediente-encabezado.component.scss']
})
export class ExpedienteEncabezadoComponent {

  @Input()
  expediente: ExpedienteAsociado | null = null;

  @Input()
  cargando = false;

  @Output()
  readonly recargar =
    new EventEmitter<void>();

  @Output()
  readonly volverBuscador =
    new EventEmitter<void>();

  get resumen() {
    return this.expediente?.resumenGeneral ?? null;
  }

  get documento(): string {
    return (
      this.resumen?.documento ??
      this.expediente?.documento ??
      ''
    );
  }

  get tipoDocumento(): string {
    return (
      this.resumen?.nombreTipoDocumento ??
      this.expediente?.nombreTipoDocumento ??
      this.resumen?.tipoDocumento ??
      this.expediente?.tipoDocumento ??
      ''
    );
  }

  get nombreCompleto(): string {
    return (
      this.resumen?.nombreCompleto ??
      this.expediente?.nombreCompleto ??
      ''
    );
  }

  get tipoPersona(): string {

    const codigo = String(
      this.resumen?.tipoPersona ?? ''
    )
      .trim()
      .toUpperCase();

    switch (codigo) {

      case '1':
        return 'Natural';

      case '2':
        return 'Jurídica';

      case '':
        return 'Sin información';

      default:
        return String(
          this.resumen?.tipoPersona ?? ''
        ).trim() || 'Sin información';
    }
  }

  get nombreEstado(): string {
    return this.resumen?.nombreEstadoAsociado ??
      'Sin estado';
  }

  get codigoEstado(): string {
    return this.resumen?.codigoEstadoAsociado ??
      '';
  }

  get nombreAgencia(): string {
    return this.resumen?.nombreAgencia ??
      'Sin agencia';
  }

  get fechaAfiliacion(): string | null {
    return this.resumen?.fechaAfiliacion ??
      null;
  }

  get fechaActualizacion(): string | null {
    return this.resumen?.fechaActualizacionHojaVida ??
      null;
  }

  get activo(): boolean {
    return this.resumen?.activo === true;
  }

  get claseEstado(): string {
    const estado = String(
      this.codigoEstado ?? ''
    )
      .trim()
      .toUpperCase();

    if (
      this.activo ||
      estado === 'A' ||
      estado === 'ACTIVO' ||
      estado === 'VIGENTE'
    ) {
      return 'estado--activo';
    }

    if (
      estado === 'I' ||
      estado === 'INACTIVO' ||
      estado === 'SUSPENDIDO'
    ) {
      return 'estado--inactivo';
    }

    if (
      estado === 'R' ||
      estado === 'RETIRADO' ||
      estado === 'FALLECIDO'
    ) {
      return 'estado--retirado';
    }

    return 'estado--neutral';
  }

  solicitarRecarga(): void {
    if (this.cargando) {
      return;
    }

    this.recargar.emit();
  }

  solicitarVolverBuscador(): void {
    this.volverBuscador.emit();
  }
}
