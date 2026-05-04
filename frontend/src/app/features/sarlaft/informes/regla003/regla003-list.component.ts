import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { Regla003ExporterService } from './regla003-exporter.service';

interface Regla003DTO {
  idDatosPersonal: number;
  documento: string;
  tipoDocumento: string;
  nombreTipoDocumento: string;
  nombreCompleto: string;

  edad: number;
  nombreZona: string;
  nombreSubZona: string;

  saldoAportes: number;
  fechaAperturaCuenta: string;

  formaAhorro: string;
  edadPermitida: number;
  motivo: string;
}

@Component({
  standalone: true,
  selector: 'app-regla003-list',
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
  ],
  templateUrl: './regla003-list.component.html',
  styleUrls: ['./regla003-list.component.scss']
})
export class Regla003ListComponent {

  private readonly http = inject(HttpClient);
  private readonly exporter = inject(Regla003ExporterService);

  cargando = false;
  error = '';
  registros: Regla003DTO[] = [];

  filtros = {
    desde: '',
    hasta: ''
  };

  resumen = {
    total: 0,
    irregulares: 0,
    pctIrregulares: 0,
    totalAportes: 0
  };

  buscar() {
    this.error = '';

    if (this.filtros.desde && this.filtros.hasta) {
      if (this.filtros.desde > this.filtros.hasta) {
        this.error = 'La fecha inicial no puede ser mayor que la final.';
        return;
      }
    }

    this.cargando = true;

    const body = {
      tipo: 'REGLA_003_FORMA_PROHIBIDA',
      filtros: {
        desde: this.filtros.desde || null,
        hasta: this.filtros.hasta || null
      }
    };


    this.http.post<Regla003DTO[]>(
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
    this.filtros = { desde: '', hasta: '' };
    this.registros = [];
    this.error = '';
    this.resetResumen();
  }

  private resetResumen() {
    this.resumen = {
      total: 0,
      irregulares: 0,
      pctIrregulares: 0,
      totalAportes: 0
    };
  }

  private calcularResumen() {
    const total = this.registros.length;

    if (total === 0) {
      this.resetResumen();
      return;
    }

    const irregulares = this.registros.length;

    const totalAportes = this.registros.reduce((s, r) => s + (r.saldoAportes ?? 0), 0);

    this.resumen = {
      total,
      irregulares,
      pctIrregulares: Math.round((irregulares / total) * 10000) / 100,
      totalAportes
    };
  }

  exportar() {
    if (this.registros.length === 0) {
      this.error = 'No hay datos para exportar.';
      return;
    }
    this.exporter.exportarListado(this.registros);
  }
}
