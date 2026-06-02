import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  ViewEncapsulation
} from '@angular/core';

import {
  LavadoActivosFormato,
  LavadoActivosModalApi
} from './lavado-activos-modal.api';

@Component({
  selector: 'app-lavado-activos-print',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './lavado-activos-print.component.html',
  styleUrl: './lavado-activos-print.component.scss',
  encapsulation: ViewEncapsulation.None
})
export class LavadoActivosPrintComponent implements OnChanges {

  @Input() visible = false;
  @Input() idFormato: number | null = null;

  @Output() cerrar = new EventEmitter<void>();

  cargando = false;
  error = '';

  formato: LavadoActivosFormato | null = null;

  constructor(
    private api: LavadoActivosModalApi
  ) {}

  ngOnChanges(): void {
    if (this.visible && this.idFormato) {
      this.cargarFormato();
    }
  }

  cargarFormato(): void {
    if (!this.idFormato) {
      return;
    }

    this.cargando = true;
    this.error = '';

    this.api.obtenerFormato(this.idFormato).subscribe({
      next: data => {
        this.formato = data;
        this.cargando = false;
      },
      error: err => {
        this.error =
          err?.error?.message
          || 'No fue posible cargar el formato de lavado de activos.';
        this.cargando = false;
      }
    });
  }

  nombreRealiza(): string {
    if (!this.formato) {
      return '';
    }

    return [
      this.formato.primerApellidoRealiza,
      this.formato.segundoApellidoRealiza,
      this.formato.primerNombreRealiza,
      this.formato.segundoNombreRealiza
    ]
      .filter(Boolean)
      .join(' ');
  }

  imprimir(): void {
    if (!this.formato?.idFormatoLavadoActivos) {
      window.print();
      return;
    }

    this.api.marcarImpreso(
      this.formato.idFormatoLavadoActivos
    ).subscribe({
      next: () => window.print(),
      error: () => window.print()
    });
  }

  cerrarVentana(): void {
    this.cerrar.emit();
  }
}
