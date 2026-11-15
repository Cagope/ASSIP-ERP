import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { Formato9027Api, Formato9027DTO } from './formato9027.api';
import { CAMPOS_SES_9027, Formato9027ExporterService } from './formato9027-exporter.service';

@Component({
  selector: 'app-formato9027',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './formato9027.component.html',
  styleUrl: './formato9027.component.scss'
})
export class Formato9027Component {
  private readonly api = inject(Formato9027Api);
  private readonly exporter = inject(Formato9027ExporterService);
  private readonly destroyRef = inject(DestroyRef);

  readonly cargando = signal(false);
  readonly error = signal('');
  readonly campos = CAMPOS_SES_9027;
  readonly meses = [
    'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
  ];
  readonly hoy = new Date();
  anio = this.hoy.getFullYear();
  mes = this.hoy.getMonth() === 0 ? 12 : this.hoy.getMonth();
  registros: Formato9027DTO[] = [];
  consultado = '';
  mostrarDetalle = false;

  get saldoCapital(): number {
    return this.sumarCampo('SALDOCAPITAL');
  }

  get saldoIntereses(): number {
    return this.sumarCampo('SALDOINTERESES');
  }

  get valorPrestamos(): number {
    return this.sumarCampo('VALORPRESTAMO');
  }

  private sumarCampo(campo: string): number {
    return this.registros.reduce((total, registro) => {
      const valor = registro[campo] ?? '';
      return total + (/^-?\d+(?:\.\d+)?$/.test(valor.trim()) ? Number(valor) : 0);
    }, 0);
  }

  constructor() {
    if (this.hoy.getMonth() === 0) {
      this.anio--;
    }
  }

  get fechaCorte(): string {
    const fecha = new Date(Date.UTC(this.anio, this.mes, 0));
    return fecha.toISOString().substring(0, 10);
  }

  get consultaVigente(): boolean {
    return this.consultado === this.fechaCorte && this.registros.length > 0;
  }

  cambiarPeriodo(): void {
    this.registros = [];
    this.consultado = '';
    this.error.set('');
  }

  limpiar(): void {
    this.cambiarPeriodo();
    this.mostrarDetalle = false;
  }

  consultar(): void {
    this.cambiarPeriodo();
    const fecha = this.fechaCorte;
    if (fecha > new Date().toISOString().substring(0, 10)) {
      this.error.set('No se puede consultar un período futuro.');
      return;
    }
    this.cargando.set(true);
    this.api.consultar(fecha)
      .pipe(
        finalize(() => this.cargando.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (registros) => {
          this.registros = registros;
          this.consultado = fecha;
          if (!registros.length) {
            this.error.set('El cierre no contiene créditos para reportar.');
          }
        },
        error: (respuesta: HttpErrorResponse) => {
          this.error.set(
            typeof respuesta.error === 'string'
              ? respuesta.error
              : respuesta.error?.message ?? 'No fue posible consultar el cierre de Cartera.'
          );
        }
      });
  }

  exportarExcel(): void {
    if (!this.consultaVigente) {
      this.error.set('Consulte el período antes de exportar.');
      return;
    }
    try {
      this.exporter.exportarExcel(this.registros, this.consultado);
      this.error.set('');
    } catch (error) {
      this.error.set(error instanceof Error ? error.message : 'No fue posible exportar a Excel.');
    }
  }

  exportar(): void {
    if (!this.consultaVigente) {
      this.error.set('Consulte el período antes de exportar.');
      return;
    }
    this.exporter.exportarCsv(this.registros, this.consultado);
  }
}
