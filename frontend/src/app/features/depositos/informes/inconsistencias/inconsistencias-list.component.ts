import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { InconsistenciasApi, InconsistenciasRequest } from './inconsistencias.api';
import { InconsistenciasExporterService } from './inconsistencias-exporter.service';

// 🔹 Necesario para el header superior
import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';

@Component({
  selector: 'app-inconsistencias-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './inconsistencias-list.component.html',
  styleUrls: ['./inconsistencias-list.component.scss']
})
export class InconsistenciasListComponent {

  private api = inject(InconsistenciasApi);
  private exporter = inject(InconsistenciasExporterService);

  cargando = false;
  error = '';
  items: any[] = [];

  agencias = [
    { id: '0', nombre: 'Todas' },
    { id: '1', nombre: 'Agencia 01' },
    { id: '2', nombre: 'Agencia 02' },
    { id: '3', nombre: 'Agencia 03' }
  ];

  filtros: InconsistenciasRequest = {
    agencia: '0',
    fechaCorte: ''
  };

  // ============================================================
  // 🔍 Buscar inconsistencias
  // ============================================================
  buscar() {

    if (!this.filtros.fechaCorte) {
      this.error = 'Debe seleccionar una fecha de corte';
      return;
    }

    this.cargando = true;
    this.error = '';
    this.items = [];

    this.api.consultar(this.filtros).subscribe({
      next: (res: any[]) => {
        this.items = res || [];
        this.cargando = false;
      },
      error: () => {
        this.error = 'Error consultando inconsistencias';
        this.cargando = false;
      }
    });
  }

  // ============================================================
  // 🧹 Limpiar formulario
  // ============================================================
  limpiar() {
    this.filtros = {
      agencia: '0',
      fechaCorte: ''
    };
    this.items = [];
    this.error = '';
  }

  // ============================================================
  // 📥 Exportar a Excel
  // ============================================================
  exportar() {
    if (!this.items || this.items.length === 0) return;
    this.exporter.exportar(this.items);
  }
}
