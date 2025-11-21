import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { CuentasNRApi } from './cuentas-nr.api';
import { GeneralApi } from '../../../../shared/general/general.api';
import { FormasAhorroApi } from '../../formas-ahorro/formas-ahorro.api';
import { CuentasNRExporterService } from './cuentas-nr-exporter.service';
import { CuentasNRPrintService } from './cuentas-nr-print.service';

@Component({
  selector: 'app-cuentas-nr-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './cuentas-nr-list.component.html',
  styleUrls: ['./cuentas-nr-list.component.scss']
})
export class CuentasNRListComponent implements OnInit {

  // APIs
  private readonly api = inject(CuentasNRApi);
  private readonly generalApi = inject(GeneralApi);
  private readonly formasApi = inject(FormasAhorroApi);
  private readonly exporter = inject(CuentasNRExporterService);
  private readonly printer = inject(CuentasNRPrintService);

  // Catálogos
  agencias: any[] = [];
  formas: any[] = [];

  // Filtros
  filtros: {
    agencia: string;
    forma: string;
    tipoInforme: 'NUEVAS' | 'RETIRADAS';
    fechaInicial: string;
    fechaFinal: string;
  } = {
    agencia: '0',
    forma: '',
    tipoInforme: 'NUEVAS',
    fechaInicial: '',
    fechaFinal: ''
  };

  cargando = false;
  error = '';
  resultados: any[] = [];

  // ============================================================
  // 🔄 Cargar combos
  // ============================================================
  async ngOnInit(): Promise<void> {
    try {
      // 🔹 Agencias
      const ag = await this.generalApi.listarAgencias().toPromise();
      this.agencias = ag ?? [];

      // 🔹 Formas de ahorro
      const fo = await this.formasApi.listar().toPromise();
      this.formas = fo ?? [];

    } catch (e) {
      console.error('Error cargando catálogos:', e);
      this.agencias = [];
      this.formas = [];
    }
  }

  // ============================================================
  // 🔍 Buscar
  // ============================================================
  async buscar(): Promise<void> {
    this.error = '';

    if (!this.filtros.forma) {
      this.error = 'Seleccione una forma.';
      return;
    }

    if (!this.filtros.fechaInicial || !this.filtros.fechaFinal) {
      this.error = 'Debe indicar el rango de fechas.';
      return;
    }

    this.cargando = true;

    try {
      const body = {
        agencia: this.filtros.agencia,
        forma: this.filtros.forma,
        tipoInforme: this.filtros.tipoInforme,
        fechaInicial: this.filtros.fechaInicial,
        fechaFinal: this.filtros.fechaFinal
      };

      const res: any = await this.api.consultar(body);
      this.resultados = Array.isArray(res) ? res : (res ?? []);

    } catch (e) {
      console.error('Error consultando cuentas NR:', e);
      this.error = 'No se pudo obtener la información.';
    } finally {
      this.cargando = false;
    }
  }

  // ============================================================
  // 🧹 Limpiar
  // ============================================================
  limpiar(): void {
    this.filtros = {
      agencia: '0',
      forma: '',
      tipoInforme: 'NUEVAS',
      fechaInicial: '',
      fechaFinal: ''
    };

    this.resultados = [];
    this.error = '';
  }

  // ============================================================
  // 📤 Exportar
  // ============================================================
  exportar(): void {
    if (!this.resultados.length) {
      alert('No hay datos para exportar.');
      return;
    }

    this.exporter.exportarExcel(
      this.resultados,
      this.filtros.tipoInforme
    );
  }

  // ============================================================
  // 🖨️ Imprimir
  // ============================================================
  imprimir(): void {
    if (!this.resultados.length) {
      alert('No hay datos para imprimir.');
      return;
    }

    // 🔵 Agrupar (usa el método del exporter)
    const agrupado = (this.exporter as any).agrupar(
      this.resultados,
      this.filtros.tipoInforme
    );

    // 🔵 Llamar al print con datos agrupados
    this.printer.imprimir(
      agrupado,
      {
        fechaInicial: this.filtros.fechaInicial,
        fechaFinal: this.filtros.fechaFinal,
        tipoInforme: this.filtros.tipoInforme
      }
    );
  }

}
