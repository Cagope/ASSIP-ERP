import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { ActualizacionExporterService } from './actualizacion-exporter.service';
import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';


interface ActualizacionDTO {
  idDatosPersonal: number;
  documento: string;
  nombreCompleto: string;
  fechaActualizacion: string;
  diasDesactualizado: number;
  saldoAportes: number;
  estado: string;

  telefono: string;
  celularUno: string;
  celularDos: string;
  correoPersonal: string;

  nombreZona: string;
  nombreSubZona: string;

  recibeLlamadas: boolean;
  recibeMsm: boolean;
  recibeEmails: boolean;
  recibeCartas: boolean;
  recibeRedesSociales: boolean;

  fechaAperturaCuenta: string;
}

@Component({
  standalone: true,
  selector: 'app-actualizacion-list',
  imports: [
    CommonModule,
    FormsModule,

    // 🔥 AGREGA ESTA LÍNEA
    HeaderActionsComponent
  ],
  templateUrl: './actualizacion-list.component.html',
  styleUrls: ['./actualizacion-list.component.scss']
})
export class ActualizacionListComponent {

  private readonly http = inject(HttpClient);
  private readonly exporter = inject(ActualizacionExporterService);

  cargando = false;
  error = '';
  registros: ActualizacionDTO[] = [];

  filtros = {
    estado: '',
    desde: '',
    hasta: ''
  };

  resumen = {
    total: 0,
    actualizados: 0,
    desactualizados: 0,
    pctActualizados: 0,
    pctDesactualizados: 0,
    promedioDias: 0,
    maxDias: 0,
    minDias: 0,
    totalSaldoActualizados: 0,
    totalSaldoDesactualizados: 0
  };

  // ==========================================
  // 🔎 Buscar informe SARLAFT
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
      tipo: 'PERSONAS_DESACTUALIZADAS',
      filtros: {
        estado: this.filtros.estado || null,
        desde: this.filtros.desde || null,
        hasta: this.filtros.hasta || null
      }
    };

    this.http.post<ActualizacionDTO[]>(
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
    this.filtros = { estado: '', desde: '', hasta: '' };
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
      actualizados: 0,
      desactualizados: 0,
      pctActualizados: 0,
      pctDesactualizados: 0,
      promedioDias: 0,
      maxDias: 0,
      minDias: 0,
      totalSaldoActualizados: 0,
      totalSaldoDesactualizados: 0
    };
  }

  private calcularResumen() {
    const lista = this.registros;
    const total = lista.length;

    if (total === 0) {
      this.resetResumen();
      return;
    }

    const actualizados = lista.filter(r => r.estado === 'ACTUALIZADO');
    const desactualizados = lista.filter(r => r.estado === 'DESACTUALIZADO');

    const sumaActualizados = actualizados.reduce((s, r) => s + (r.saldoAportes ?? 0), 0);
    const sumaDesactualizados = desactualizados.reduce((s, r) => s + (r.saldoAportes ?? 0), 0);

    const diasArr = lista.map(r => r.diasDesactualizado ?? 0);

    const sumaDias = diasArr.reduce((a, b) => a + b, 0);
    const promedioDias = Math.round(sumaDias / diasArr.length);

    this.resumen = {
      total,
      actualizados: actualizados.length,
      desactualizados: desactualizados.length,
      pctActualizados: Math.round((actualizados.length / total) * 10000) / 100,
      pctDesactualizados: Math.round((desactualizados.length / total) * 10000) / 100,
      promedioDias,
      maxDias: Math.max(...diasArr),
      minDias: Math.min(...diasArr),
      totalSaldoActualizados: sumaActualizados,
      totalSaldoDesactualizados: sumaDesactualizados
    };
  }

  // ==========================================
  // 📤 Exportar Excel
  // ==========================================
  exportar() {
    if (this.registros.length === 0) {
      this.error = 'No hay datos para exportar.';
      return;
    }

    this.exporter.exportarListado(
      this.filtros.desde || 'sin_fecha',
      this.registros
    );
  }
}
