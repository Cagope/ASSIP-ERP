import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { RevalorizacionApi } from './revalorizacion.api';
import { GeneralApi } from '../../../../shared/general/general.api';
import { AgenciaFormaApi } from '../../../../shared/agencia-forma/agencia-forma.api';

import { RevalorizacionExporterService } from './revalorizacion-exporter.service';
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
  private readonly agenciaFormaApi = inject(AgenciaFormaApi);

  private readonly exporter = inject(RevalorizacionExporterService);
  private readonly printService = inject(RevalorizacionPrintService);

  agencias: any[] = [];
  formas: any[] = [];  // << NUEVO

  filtros = {
    agenciaId: '0',
    formaId: '',       // << NUEVO
    fechaInicio: '',
    fechaFin: '',
    fechaContabilizacion: '',
    tasa: ''
  };

  cargando = false;
  error = '';
  resultados: any[] = [];

  // ============================================================
  // INIT
  // ============================================================
  async ngOnInit(): Promise<void> {
    try {
      const ag = await this.generalApi.listarAgencias().toPromise();
      this.agencias = ag ?? [];
      this.formas = [];
    } catch (e) {
      console.error('Error cargando agencias:', e);
      this.agencias = [];
    }
  }

  // ============================================================
  // 🔁 CASCADA: CUANDO CAMBIA AGENCIA
  // ============================================================
  async cargarFormas(idAgencia: string) {
    this.formas = [];
    this.filtros.formaId = '';

    const id = Number(idAgencia);

    if (!id || id === 0) {
      return;
    }

    try {
      const fr = await this.agenciaFormaApi.listarFormasPorAgencia(id).toPromise();
      this.formas = fr ?? [];
    } catch (e) {
      console.error('Error cargando formas:', e);
      this.formas = [];
    }
  }

  // ============================================================
  // EJECUTAR
  // ============================================================
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

    if (!this.filtros.formaId) {
      this.error = 'Debe seleccionar la forma.';
      return;
    }

    this.cargando = true;
    this.resultados = [];

    try {
      const body = {
        agenciaId: Number(this.filtros.agenciaId),
        formaId: Number(this.filtros.formaId), // << NUEVO
        fechaInicio: this.filtros.fechaInicio,
        fechaFin: this.filtros.fechaFin,
        fechaContabilizacion: this.filtros.fechaContabilizacion,
        tasaRevalorizacion: Number(this.filtros.tasa),
      };

      const res = await this.api.ejecutar(body);
      this.resultados = Array.isArray(res) ? res : (res ?? []);

    } catch (e: any) {

      console.error('Error ejecutando revalorización:', e);

      let texto = '';

      if (e && e.error && e.error.message) {
        texto = e.error.message;
      } else if (e && typeof e.error === 'string') {
        texto = e.error;
      } else if (e && e.message) {
        texto = e.message;
      }

      this.error = texto || 'No se pudo ejecutar el proceso.';

    } finally {
      this.cargando = false;
    }

  }

  // ============================================================
  // LIMPIAR
  // ============================================================
  limpiar(): void {
    this.filtros = {
      agenciaId: '0',
      formaId: '',
      fechaInicio: '',
      fechaFin: '',
      fechaContabilizacion: '',
      tasa: ''
    };
    this.resultados = [];
    this.error = '';
    this.formas = [];
  }

  // ============================================================
  // 📤 Exportar a Excel
  // ============================================================
  exportar(): void {
    if (!this.resultados || this.resultados.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const agenciaId = Number(this.filtros.agenciaId);

    // Buscar la agencia seleccionada
    const ag = this.agencias.find(a => a.idAgencia === agenciaId);

    // Usar ONLY el nombre correcto: codigoAgencia
    const codigoAgencia =
      agenciaId === 0
        ? '00'
        : (ag?.codigoAgencia ?? '00');

    this.exporter.exportarExcel(this.resultados, {
      fechaInicio: this.filtros.fechaInicio,
      fechaFin: this.filtros.fechaFin,
      codigoAgencia: String(codigoAgencia).padStart(2, '0')
    });
  }


  // ============================================================
  // IMPRIMIR
  // ============================================================
  imprimir(): void {
    if (!this.resultados || this.resultados.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    this.printService.imprimir(this.resultados, this.filtros);
  }

}
