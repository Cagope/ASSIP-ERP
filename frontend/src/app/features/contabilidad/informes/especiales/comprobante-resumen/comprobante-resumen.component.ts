import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { SessionService } from '../../../../../core/auth/session.service';
import { ComprobanteResumenService } from './comprobante-resumen.service';
import { ComprobanteResumenExporterService } from './comprobante-resumen-exporter.service';
import { ComprobanteResumenFiltro, ComprobanteResumenResponse } from './comprobante-resumen.models';

interface AgenciaOpcion {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
}

@Component({
  selector: 'app-comprobante-resumen',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './comprobante-resumen.component.html',
  styleUrl: './comprobante-resumen.component.scss'
})
export class ComprobanteResumenComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly servicio = inject(ComprobanteResumenService);
  private readonly exporter = inject(ComprobanteResumenExporterService);
  agencias: AgenciaOpcion[] = [];
  cargando = false;
  mensajeError = '';
  informe: ComprobanteResumenResponse | null = null;
  filtros: ComprobanteResumenFiltro = { idAgencia: 0, fechaInicial: '', fechaFinal: '' };

  /** Carga agencias autorizadas y establece el período del mes actual. */
  ngOnInit(): void {
    this.agencias = this.session.agenciasSig()
      .filter(a => a.idAgencia !== null)
      .map(a => ({
        idAgencia: a.idAgencia!,
        codigoAgencia: a.codigoAgencia ?? '',
        nombreAgencia: a.nombreAgencia ?? ''
      }));
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

  /** Descarta los resultados cuando cambian los parámetros de consulta. */
  cambiarFiltros(): void {
    this.informe = null;
    this.mensajeError = '';
  }

  /** Valida los filtros y ejecuta la consulta de solo lectura. */
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
    const consultados = { ...this.filtros };
    this.servicio.consultar(consultados).pipe(finalize(() => this.cargando = false))
      .subscribe({
        next: informe => {
          if (this.filtros.idAgencia === consultados.idAgencia &&
              this.filtros.fechaInicial === consultados.fechaInicial &&
              this.filtros.fechaFinal === consultados.fechaFinal) {
            this.informe = informe;
          }
        },
        error: error => {
          this.mensajeError = error?.error?.message || error?.error?.detail ||
            'No fue posible generar el comprobante resumen.';
        }
      });
  }

  /** Comprueba que el resultado corresponde a los filtros actuales. */
  get resultadoVigente(): boolean {
    return !!this.informe && this.informe.idAgencia === this.filtros.idAgencia &&
      this.informe.fechaInicial === this.filtros.fechaInicial &&
      this.informe.fechaFinal === this.filtros.fechaFinal;
  }

  /** Muestra el código y nombre de la agencia sin exponer identificadores internos. */
  get nombreAgenciaInforme(): string {
    const agencia = this.agencias.find(a => a.idAgencia === this.informe?.idAgencia);
    return agencia ? `${agencia.codigoAgencia} - ${agencia.nombreAgencia}` : '';
  }

  /** Exporta exclusivamente los resultados vigentes que se muestran en pantalla. */
  exportarExcel(): void {
    if (this.cargando || !this.resultadoVigente || !this.informe || !this.informe.filas.length) {
      return;
    }
    this.exporter.exportar(this.informe, this.nombreAgenciaInforme);
  }
}
