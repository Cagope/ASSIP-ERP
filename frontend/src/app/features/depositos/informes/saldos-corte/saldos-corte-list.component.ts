import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { SaldosCorteApi } from './saldos-corte.api';
import { GeneralApi } from '../../../../shared/general/general.api';
import { SaldosCorteExporterService } from './saldos-corte-exporter.service';
import { SaldosCortePrintService } from './saldos-corte-print.service';

@Component({
  selector: 'app-saldos-corte-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './saldos-corte-list.component.html',
  styleUrls: ['./saldos-corte-list.component.scss']
})
export class SaldosCorteListComponent implements OnInit {

  private readonly api = inject(SaldosCorteApi);
  private readonly generalApi = inject(GeneralApi);
  private readonly exporter = inject(SaldosCorteExporterService);
  private readonly printService = inject(SaldosCortePrintService);

  agencias: any[] = [];

  filtros = {
    agencia: '0',
    fechaCorte: ''
  };

  cargando = false;
  error = '';
  resumen: any = null;
  items: any[] = [];

  // 🆕 Resumen por agencia y forma
  resumenAgencias: any[] = [];

  // ============================================================
  // 🧩 Agrupar → { codigoAgencia, nombreAgencia, formas[] }
  // ============================================================
  private agruparResumen(data: any[]) {
    const grupos: any = {};

    for (const item of data) {
      const id = item.idAgencia;

      if (!grupos[id]) {
        grupos[id] = {
          codigoAgencia: item.codigoAgencia ?? item.idAgencia,  // << corregido
          nombreAgencia: item.nombreAgencia,
          formas: []
        };
      }

      grupos[id].formas.push({
        codigoForma: item.codigoForma,
        nombreForma: item.nombreForma,
        detalle: [] // se llenará después
      });
    }

    return Object.values(grupos);
  }

  // ============================================================
  // 🔄 Inicializar
  // ============================================================
  async ngOnInit() {
    try {
      const data = await this.generalApi.listarAgencias().toPromise();
      this.agencias = data ?? [];
    } catch (e) {
      console.error('Error cargando agencias:', e);
      this.agencias = [];
    }
  }

  // ============================================================
  // 🔍 Buscar
  // ============================================================
  async buscar() {
    this.error = '';

    if (!this.filtros.fechaCorte.trim()) {
      this.error = 'Debe seleccionar una fecha de corte.';
      return;
    }

    this.cargando = true;

    try {
      // 🔍 Consulta principal (detalle)
      const res = await this.api.consultar({
        agencia: Number(this.filtros.agencia),
        fechaCorte: this.filtros.fechaCorte
      });

      this.resumen = res.resumen;
      this.items = res.items;

      // ============================================================
      // 🆕 CONSULTAR SIEMPRE EL RESUMEN POR AGENCIA
      // ============================================================
      const resumenData =
        await this.api.resumenPorAgencia(this.filtros.fechaCorte) ?? [];

      // 🔍 Filtrar si es una sola agencia
      if (this.filtros.agencia !== '0') {
        this.resumenAgencias = this.agruparResumen(
          resumenData.filter(r => r.idAgencia == Number(this.filtros.agencia))
        );
      } else {
        this.resumenAgencias = this.agruparResumen(resumenData);
      }

      // ============================================================
      // 🟦 COMPLETAR DETALLE POR AGENCIA Y FORMA (para print)
      // ============================================================
      for (const ag of this.resumenAgencias) {
        for (const forma of ag.formas) {
          forma.detalle = this.items.filter(
            x =>
              x.codigoAgencia == ag.codigoAgencia &&
              x.codigoForma == forma.codigoForma
          );
        }
      }

    } catch (e) {
      console.error('Error consultando saldos corte:', e);
      this.error = 'No se pudo obtener el informe.';
    } finally {
      this.cargando = false;
    }
  }

  // ============================================================
  // 🧹 Limpiar
  // ============================================================
  limpiar() {
    this.filtros = { agencia: '0', fechaCorte: '' };
    this.items = [];
    this.resumen = null;
    this.resumenAgencias = [];
    this.error = '';
  }

  // ============================================================
  // 📤 Exportar a Excel
  // ============================================================
  exportar() {
    if (!this.items.length) {
      alert('No hay datos para exportar.');
      return;
    }
    this.exporter.exportarExcel(this.items);
  }

  // ============================================================
  // 🖨️ Imprimir
  // ============================================================
  imprimir() {
    if (!this.resumenAgencias || this.resumenAgencias.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    this.printService.imprimir(
      this.resumenAgencias,
      this.filtros.fechaCorte
    );
  }
}
