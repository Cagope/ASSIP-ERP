import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { RevalorizacionApi } from './revalorizacion.api';
import { GeneralApi } from '../../../../shared/general/general.api';
import { RevalorizacionExporterService } from './revalorizacion-exporter.service';

// << NUEVO: servicio de impresión
import { RevalorizacionPrintService } from './revalorizacion-print.service';

@Component({
  selector: 'app-revalorizacion-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './revalorizacion-list.component.html',
  styleUrls: ['./revalorizacion-list.component.scss']
})
export class RevalorizacionListComponent implements OnInit {

  private readonly api = inject(RevalorizacionApi);
  private readonly generalApi = inject(GeneralApi);
  private readonly exporter = inject(RevalorizacionExporterService);

  // << NUEVO: inyección servicio impresión
  private readonly printService = inject(RevalorizacionPrintService);

  agencias: any[] = [];

  filtros = {
    agenciaId: '0',
    fechaInicio: '',
    fechaFin: '',
    fechaContabilizacion: '',
    tasa: ''
  };

  cargando = false;
  error = '';
  resultados: any[] = [];

  async ngOnInit(): Promise<void> {
    try {
      const ag = await this.generalApi.listarAgencias().toPromise();
      this.agencias = ag ?? [];
    } catch (e) {
      console.error('Error cargando agencias:', e);
      this.agencias = [];
    }
  }

  async ejecutar(): Promise<void> {
    this.error = '';

    if (!this.filtros.fechaInicio || !this.filtros.fechaFin) {
      this.error = 'Debe seleccionar la fecha inicio y fecha fin.';
      return;
    }

    if (!this.filtros.fechaContabilizacion) {
      this.error = 'Debe seleccionar la fecha de contabilización.';
      return;
    }

    if (!this.filtros.tasa || Number(this.filtros.tasa) <= 0) {
      this.error = 'Debe indicar una tasa válida.';
      return;
    }

    this.cargando = true;
    this.resultados = [];

    try {
      const body = {
        agenciaId: Number(this.filtros.agenciaId),
        fechaInicio: this.filtros.fechaInicio,
        fechaFin: this.filtros.fechaFin,
        fechaContabilizacion: this.filtros.fechaContabilizacion,
        tasaRevalorizacion: Number(this.filtros.tasa)
      };

      const res = await this.api.ejecutar(body);
      this.resultados = Array.isArray(res) ? res : (res ?? []);

    } catch (e) {
      console.error('Error ejecutando revalorización:', e);
      this.error = 'No se pudo ejecutar el proceso.';
    } finally {
      this.cargando = false;
    }
  }

  limpiar(): void {
    this.filtros = {
      agenciaId: '0',
      fechaInicio: '',
      fechaFin: '',
      fechaContabilizacion: '',
      tasa: ''
    };
    this.resultados = [];
    this.error = '';
  }

  // ============================================================
  // 📤 Exportar a Excel
  // ============================================================
  exportar(): void {
    if (!this.resultados || this.resultados.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    this.exporter.exportarExcel(this.resultados, {
      fechaInicio: this.filtros.fechaInicio,
      fechaFin: this.filtros.fechaFin
    });
  }

  // ============================================================
  // 🖨️ Imprimir  << NUEVO
  // ============================================================
  imprimir(): void {
    if (!this.resultados || this.resultados.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    this.printService.imprimir(this.resultados, this.filtros);
  }

}
