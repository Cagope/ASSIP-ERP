import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormsModule
} from '@angular/forms';

import {
  ExtractoCuentaSharedApi
} from './extracto-cuenta-shared.api';

import {
  ExtractoCuentaSharedEstadistica,
  ExtractoCuentaSharedMovimiento,
  ExtractoCuentaSharedRequest,
  ExtractoCuentaSharedResponse,
  ExtractoCuentaSharedResumen
} from './extracto-cuenta-shared.models';

@Component({
  selector: 'app-extracto-cuenta-shared',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './extracto-cuenta-shared.component.html',
  styleUrls: ['./extracto-cuenta-shared.component.scss']
})
export class ExtractoCuentaSharedComponent implements OnInit {

  @Input()
  idCuentaAhorro!: number;

  @Output()
  cerrar = new EventEmitter<void>();

  @ViewChild('extractoModal')
  extractoModal?: ElementRef<HTMLDivElement>;

  cargando = false;
  error = '';

  fechaInicial = '';
  fechaFinal = '';

  resumen: ExtractoCuentaSharedResumen | null = null;
  estadisticas: ExtractoCuentaSharedEstadistica | null = null;
  movimientos: ExtractoCuentaSharedMovimiento[] = [];

  constructor(
    private api: ExtractoCuentaSharedApi
  ) {
  }

  ngOnInit(): void {
    this.inicializarFechas();
    this.consultar();
  }

  consultar(): void {

    if (this.cargando) {
      return;
    }

    if (!this.idCuentaAhorro) {
      this.error = 'No se recibió la cuenta de ahorro para consultar.';
      return;
    }

    if (!this.validarFechas()) {
      return;
    }

    this.cargando = true;
    this.error = '';

    const request: ExtractoCuentaSharedRequest = {
      idCuentaAhorro: this.idCuentaAhorro,
      fechaInicial: this.fechaInicial,
      fechaFinal: this.fechaFinal
    };

    this.api.consultar(request)
      .subscribe({

        next: (response: ExtractoCuentaSharedResponse) => {

          this.resumen =
            response?.resumen || null;

          this.estadisticas =
            response?.estadisticas || null;

          this.movimientos =
            response?.movimientos || [];

          this.cargando = false;
        },

        error: err => {

          this.error =
            err?.error?.message
            || err?.error?.error
            || err?.message
            || 'Error consultando extracto.';

          this.cargando = false;
        }

      });
  }

  cerrarVentana(): void {
    this.cerrar.emit();
  }

  irInicio(): void {

    const modal =
      this.extractoModal?.nativeElement;

    if (!modal) {
      return;
    }

    modal.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  }

  private inicializarFechas(): void {

    const hoy = new Date();
    const inicio = new Date();

    inicio.setFullYear(
      hoy.getFullYear() - 1
    );

    this.fechaFinal =
      this.formatearFecha(hoy);

    this.fechaInicial =
      this.formatearFecha(inicio);
  }

  private validarFechas(): boolean {

    if (!this.fechaInicial) {
      this.error = 'Debe seleccionar la fecha inicial.';
      return false;
    }

    if (!this.fechaFinal) {
      this.error = 'Debe seleccionar la fecha final.';
      return false;
    }

    if (this.fechaInicial > this.fechaFinal) {
      this.error = 'La fecha inicial no puede ser mayor que la fecha final.';
      return false;
    }

    return true;
  }

  private formatearFecha(fecha: Date): string {
    return fecha.toISOString().substring(0, 10);
  }
}
