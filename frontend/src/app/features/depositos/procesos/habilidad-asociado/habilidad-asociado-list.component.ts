import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';

// 🔹 API habilidad asociado
import { HabilidadAsociadoApi } from './habilidad-asociado.api';

// 🔹 Servicios compartidos
import { GeneralApi } from '../../../../shared/general/general.api';

// 🔹 Exportar e imprimir
import { HabilidadAsociadoExporterService } from './habilidad-asociado-exporter.service';
import { HabilidadAsociadoPrintService } from './habilidad-asociado-print.service';

@Component({
  selector: 'app-habilidad-asociado-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './habilidad-asociado-list.component.html',
  styleUrls: ['./habilidad-asociado-list.component.scss']
})
export class HabilidadAsociadoListComponent implements OnInit {

  private readonly api = inject(HabilidadAsociadoApi);
  private readonly generalApi = inject(GeneralApi);

  private readonly exporter = inject(HabilidadAsociadoExporterService);
  private readonly printService = inject(HabilidadAsociadoPrintService);

  agencias: any[] = [];

  filtros = {
    agenciaId: '0',
    fechaInicio: '',
    fechaFin: '',
    valorMenores: '',
    valorMayores: '',
    valorJuridicas: ''
  };

  cargando = false;
  error = '';
  resultados: any[] = [];

  // ==========================================================
  // 📊 RESUMEN ESTADÍSTICO
  // ==========================================================
  resumen = {
    habiles: 0,
    inhabiles: 0,
    totalHabilesAportes: 0,
    totalInhabilesAportes: 0,
    total: 0,
    porcentajeHabiles: 0,
    porcentajeInhabiles: 0
  };

  // ==========================================================
  // 🔄 INIT
  // ==========================================================
  async ngOnInit(): Promise<void> {
    this.error = '';
    this.cargando = true;

    try {
      const ag = await this.generalApi.listarAgencias().toPromise();
      this.agencias = ag ?? [];
    } catch (e: any) {
      console.error('❌ Error cargando agencias:', e);
      this.error = 'No se pudieron cargar las agencias.';
      this.agencias = [];
    } finally {
      this.cargando = false;
    }
  }

  // ==========================================================
  // 📌 CALCULAR ESTADÍSTICA
  // ==========================================================
  private calcularResumen(): void {

    if (!this.resultados.length) {
      this.resumen = {
        habiles: 0,
        inhabiles: 0,
        totalHabilesAportes: 0,
        totalInhabilesAportes: 0,
        total: 0,
        porcentajeHabiles: 0,
        porcentajeInhabiles: 0
      };
      return;
    }

    let hab = 0;
    let inh = 0;
    let aportHab = 0;
    let aportInh = 0;

    for (const r of this.resultados) {
      const esHabil = r.resultado.startsWith('HÁBIL');

      if (esHabil) {
        hab++;
        aportHab += Number(r.totalAportes || 0);
      } else {
        inh++;
        aportInh += Number(r.totalAportes || 0);
      }
    }

    const total = hab + inh;

    this.resumen = {
      habiles: hab,
      inhabiles: inh,
      totalHabilesAportes: aportHab,
      totalInhabilesAportes: aportInh,
      total,
      porcentajeHabiles: total > 0 ? (hab / total) * 100 : 0,
      porcentajeInhabiles: total > 0 ? (inh / total) * 100 : 0
    };
  }

  // ==========================================================
  // ⚙️ EJECUTAR PROCESO
  // ==========================================================
  async ejecutar(): Promise<void> {
    this.error = '';

    if (!this.filtros.fechaInicio) {
      this.error = 'Debe seleccionar la fecha inicial.';
      return;
    }
    if (!this.filtros.fechaFin) {
      this.error = 'Debe seleccionar la fecha final.';
      return;
    }
    if (!this.filtros.valorMenores || !this.filtros.valorMayores || !this.filtros.valorJuridicas) {
      this.error = 'Debe capturar todos los valores de evaluación.';
      return;
    }

    this.cargando = true;
    this.resultados = [];

    try {
      const body = {
        agenciaId: Number(this.filtros.agenciaId),
        fechaInicio: this.filtros.fechaInicio,
        fechaFin: this.filtros.fechaFin,
        valorMenores: Number(this.filtros.valorMenores),
        valorMayores: Number(this.filtros.valorMayores),
        valorJuridicas: Number(this.filtros.valorJuridicas)
      };

      const res = await this.api.evaluar(body);
      this.resultados = Array.isArray(res) ? res : (res ?? []);

      // 🟩 Calcular resumen
      this.calcularResumen();

    } catch (e: any) {
      console.error('❌ Error en habilidad asociado:', e);

      const texto =
        e?.error?.message ??
        (typeof e?.error === 'string' ? e.error : null) ??
        e?.message ??
        '';

      this.error = texto || 'No se pudo ejecutar el proceso.';
    } finally {
      this.cargando = false;
    }
  }

  // ==========================================================
  // 🧹 LIMPIAR FORMULARIO
  // ==========================================================
  limpiar(): void {
    this.filtros = {
      agenciaId: '0',
      fechaInicio: '',
      fechaFin: '',
      valorMenores: '',
      valorMayores: '',
      valorJuridicas: ''
    };
    this.resultados = [];
    this.error = '';

    // 🔄 Reset resumen
    this.calcularResumen();
  }

  // ==========================================================
  // 📤 EXPORTAR
  // ==========================================================
  exportar(): void {
    if (!this.resultados.length) {
      alert('No hay datos para exportar.');
      return;
    }

    const agenciaSel = this.agencias.find(
      a => a.idAgencia === Number(this.filtros.agenciaId)
    );

    const codigoAgencia =
      agenciaSel?.codigoAgencia
        ? String(agenciaSel.codigoAgencia).padStart(2, '0')
        : '00';

    this.exporter.exportarExcel(this.resultados, {
      codigoAgencia,
      nombreAgencia: agenciaSel?.nombreAgencia ?? '',
      fechaInicio: this.filtros.fechaInicio,
      fechaFin: this.filtros.fechaFin
    });
  }

  // ==========================================================
  // 🖨 IMPRIMIR
  // ==========================================================
  imprimir(): void {
    if (!this.resultados.length) {
      alert('No hay información para imprimir.');
      return;
    }

    const agenciaSel = this.agencias.find(
      a => a.idAgencia === Number(this.filtros.agenciaId)
    );

    const filtrosExtendidos = {
      ...this.filtros,
      codigoAgencia: agenciaSel?.codigoAgencia ?? '',
      nombreAgencia: agenciaSel?.nombreAgencia ?? ''
    };

    this.printService.imprimir(this.resultados, filtrosExtendidos);
  }

}
