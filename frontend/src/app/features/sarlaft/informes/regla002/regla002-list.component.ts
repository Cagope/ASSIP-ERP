import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { Regla002ExporterService } from './regla002-exporter.service';

interface Regla002DTO {
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
}

@Component({
  standalone: true,
  selector: 'app-regla002-list',
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './regla002-list.component.html',
  styleUrls: ['./regla002-list.component.scss']
})
export class Regla002ListComponent {

  private readonly http = inject(HttpClient);
  private readonly exporter = inject(Regla002ExporterService);

  cargando = false;
  error = '';
  registros: Regla002DTO[] = [];

  filtros = {
    desde: '',
    hasta: ''
  };

  resumen = {
    total: 0,
    menoresIrregulares: 0,
    adultosIrregulares: 0,
    pctMenores: 0,
    pctAdultos: 0,
    totalAportes: 0
  };

  // ==========================================
  // 🔎 Buscar
  // ==========================================
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
      tipo: 'REGLA_002_DOCUMENTO_EDAD',
      filtros: {
        desde: this.filtros.desde || null,
        hasta: this.filtros.hasta || null
      }
    };

    this.http.post<Regla002DTO[]>(
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

  // ==========================================
  // 📊 Resumen
  // ==========================================
  private resetResumen() {
    this.resumen = {
      total: 0,
      menoresIrregulares: 0,
      adultosIrregulares: 0,
      pctMenores: 0,
      pctAdultos: 0,
      totalAportes: 0
    };
  }

  private calcularResumen() {
    const lista = this.registros;
    const total = lista.length;

    if (total === 0) {
      this.resetResumen();
      return;
    }

    const menores = lista.filter(r => r.tipoDocumento === 'R' && r.edad >= 7);
    const adultos = lista.filter(r => r.tipoDocumento !== 'C' && r.edad >= 18);

    const totalAportes = lista.reduce((s, r) => s + (r.saldoAportes ?? 0), 0);

    this.resumen = {
      total,
      menoresIrregulares: menores.length,
      adultosIrregulares: adultos.length,
      pctMenores: Math.round((menores.length / total) * 10000) / 100,
      pctAdultos: Math.round((adultos.length / total) * 10000) / 100,
      totalAportes
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

    this.exporter.exportarListado(this.registros);
  }
}
