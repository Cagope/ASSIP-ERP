import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { SessionService } from '../../../../../core/auth/session.service';
import { VerificarPartidaDobleService } from './verificar-partida-doble.service';
import { VerificarPartidaDobleExporterService } from './verificar-partida-doble-exporter.service';
import { VerificarPartidaDobleFiltro, VerificarPartidaDobleResponse } from './verificar-partida-doble.models';

interface AgenciaOpcion {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
}

/** Ejecuta la verificación por agencia y período y resalta los comprobantes descuadrados. */
@Component({
  selector: 'app-verificar-partida-doble',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './verificar-partida-doble.component.html',
  styleUrl: './verificar-partida-doble.component.scss'
})
export class VerificarPartidaDobleComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly servicio = inject(VerificarPartidaDobleService);
  private readonly exporter = inject(VerificarPartidaDobleExporterService);
  agencias: AgenciaOpcion[] = [];
  cargando = false;
  mensajeError = '';
  informe: VerificarPartidaDobleResponse | null = null;
  filtros: VerificarPartidaDobleFiltro = { idAgencia: 0, fechaInicial: '', fechaFinal: '' };

  /** Selecciona la agencia activa y el período del mes en curso. */
  ngOnInit(): void {
    this.agencias = this.session.agenciasSig()
      .filter(a => a.idAgencia !== null)
      .map(a => ({ idAgencia: a.idAgencia!, codigoAgencia: a.codigoAgencia ?? '', nombreAgencia: a.nombreAgencia ?? '' }));
    const activa = this.session.agenciaActivaSig()?.idAgencia;
    this.filtros.idAgencia = this.agencias.some(a => a.idAgencia === activa)
      ? activa! : this.agencias.length === 1 ? this.agencias[0].idAgencia : 0;
    const hoy = new Date();
    const anio = hoy.getFullYear();
    const mes = String(hoy.getMonth() + 1).padStart(2, '0');
    const dia = String(hoy.getDate()).padStart(2, '0');
    this.filtros.fechaInicial = `${anio}-${mes}-01`;
    this.filtros.fechaFinal = `${anio}-${mes}-${dia}`;
  }

  /** Invalida resultados anteriores al cambiar cualquiera de los filtros. */
  cambiarFiltros(): void {
    this.informe = null;
    this.mensajeError = '';
  }

  /** Valida parámetros y ejecuta la verificación contable. */
  ejecutar(): void {
    if (this.cargando) { return; }
    this.cambiarFiltros();
    if (!this.agencias.some(a => a.idAgencia === this.filtros.idAgencia)) {
      this.mensajeError = 'Debe seleccionar una agencia autorizada.';
      return;
    }
    if (!this.filtros.fechaInicial || !this.filtros.fechaFinal ||
        this.filtros.fechaInicial > this.filtros.fechaFinal) {
      this.mensajeError = 'Debe indicar un rango de fechas válido.';
      return;
    }
    this.cargando = true;
    const filtrosConsultados = { ...this.filtros };
    this.servicio.consultar(filtrosConsultados).pipe(finalize(() => this.cargando = false))
      .subscribe({
        next: informe => {
          if (this.filtros.idAgencia === filtrosConsultados.idAgencia &&
              this.filtros.fechaInicial === filtrosConsultados.fechaInicial &&
              this.filtros.fechaFinal === filtrosConsultados.fechaFinal) {
            this.informe = informe;
          }
        },
        error: error => {
          this.mensajeError = error?.error?.message || error?.error?.detail ||
            'No fue posible verificar la partida doble.';
        }
      });
  }

  /** Comprueba que el informe corresponde exactamente a los filtros visibles. */
  get resultadoVigente(): boolean {
    return !!this.informe && this.informe.idAgencia === this.filtros.idAgencia &&
      this.informe.fechaInicial === this.filtros.fechaInicial &&
      this.informe.fechaFinal === this.filtros.fechaFinal;
  }

  /** Devuelve la etiqueta legible de la agencia consultada. */
  get nombreAgenciaInforme(): string {
    const agencia = this.agencias.find(a => a.idAgencia === this.informe?.idAgencia);
    return agencia ? `${agencia.codigoAgencia} - ${agencia.nombreAgencia}` : '';
  }

  /** Exporta el mismo resultado vigente que se muestra en pantalla. */
  exportarExcel(): void {
    if (this.cargando || !this.resultadoVigente || !this.informe || !this.informe.filas.length) {
      return;
    }
    this.exporter.exportar(this.informe, this.nombreAgenciaInforme);
  }
}
