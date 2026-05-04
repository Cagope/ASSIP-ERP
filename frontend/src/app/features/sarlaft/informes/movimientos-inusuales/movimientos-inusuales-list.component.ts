import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { MovimientosInusualesExporterService } from './movimientos-inusuales-exporter.service';

interface MovimientoInusualDTO {
  idDatosPersonal: number;
  documento: string;
  nombreCompleto: string;

  ingresosMensuales: number;
  egresosMensuales: number;
  totalConsignaciones: number;

  pctVsIngresos: number;
  pctVsEgresos: number;

  estadoInusual: string;

  nombreZona: string;
  nombreSubZona: string;
  nombreAgencia: string;
  saldoAportes: number;
}

@Component({
  standalone: true,
  selector: 'app-movimientos-inusuales-list',
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './movimientos-inusuales-list.component.html',
  styleUrls: ['./movimientos-inusuales-list.component.scss']
})
export class MovimientosInusualesListComponent {

  private readonly http = inject(HttpClient);
  private readonly exporter = inject(MovimientosInusualesExporterService);

  cargando = false;
  error = '';
  registros: MovimientoInusualDTO[] = [];

  filtros = {
    fecha_inicio: '',
    fecha_fin: ''
  };

  resumen = {
    total: 0,
    inusuales: 0,
    normales: 0,
    pctInusuales: 0,
    pctNormales: 0,
    totalConsignado: 0
  };

  // ==========================================
  // 🔎 Buscar
  // ==========================================
  buscar() {
    this.error = '';

    if (this.filtros.fecha_inicio && this.filtros.fecha_fin) {
      if (this.filtros.fecha_inicio > this.filtros.fecha_fin) {
        this.error = 'La fecha inicial no puede ser mayor que la final.';
        return;
      }
    }

    this.cargando = true;

    const body = {
      tipo: 'MOVIMIENTOS_INUSUALES',
      filtros: {
        fecha_inicio: this.filtros.fecha_inicio || null,
        fecha_fin: this.filtros.fecha_fin || null
      }
    };

    this.http.post<MovimientoInusualDTO[]>(
      `${environment.apiUrl}/sarlaft/informes`,
      body
    ).subscribe({
      next: (res) => {
        this.registros = res || [];
        this.calcularResumen();
        this.cargando = false;
      },
      error: () => {
        this.error = 'Error al cargar el informe.';
        this.registros = [];
        this.resetResumen();
        this.cargando = false;
      }
    });
  }

  limpiar() {
    this.filtros = { fecha_inicio: '', fecha_fin: '' };
    this.registros = [];
    this.error = '';
    this.resetResumen();
  }

  // ==========================================
  // 📊 Resumen
  // ==========================================
  private resetResumen() {
    this.resumen = {
      total: 0,
      inusuales: 0,
      normales: 0,
      pctInusuales: 0,
      pctNormales: 0,
      totalConsignado: 0
    };
  }

  private calcularResumen() {
    const lista = this.registros;
    const total = lista.length;

    if (total === 0) {
      this.resetResumen();
      return;
    }

    const inusuales = lista.filter(r => r.estadoInusual === 'INUSUAL');
    const normales = lista.filter(r => r.estadoInusual === 'NORMAL');

    const totalConsignado = lista.reduce((s, r) => s + (r.totalConsignaciones ?? 0), 0);

    this.resumen = {
      total,
      inusuales: inusuales.length,
      normales: normales.length,
      pctInusuales: Math.round((inusuales.length / total) * 10000) / 100,
      pctNormales: Math.round((normales.length / total) * 10000) / 100,
      totalConsignado
    };
  }

  // ==========================================
  // 📤 Exportar
  // ==========================================
  exportar() {
    if (this.registros.length === 0) {
      this.error = 'No hay datos para exportar.';
      return;
    }

    this.exporter.exportarListado(
      this.filtros.fecha_inicio || 'sin_fecha',
      this.registros
    );
  }
}
