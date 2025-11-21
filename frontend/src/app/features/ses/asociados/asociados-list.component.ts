import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { AsociadosApi } from './asociados.api';
import { AsociadosExporterService } from './asociados-exporter.service';

@Component({
  selector: 'app-asociados-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './asociados-list.component.html',
  styleUrls: ['./asociados-list.component.scss']
})
export class AsociadosListComponent {

  // -----------------------------
  // 🔹 Servicios
  // -----------------------------
  private readonly api = inject(AsociadosApi);
  private readonly exporter = inject(AsociadosExporterService);

  // -----------------------------
  // 🔹 Estado del componente
  // -----------------------------
  fechaCorte = '';
  cargando = false;
  error = '';
  items: any[] = [];

  // 🔵 Estadística calculada
  estadistica: any = null;

  // -----------------------------
  // 🔍 Buscar
  // -----------------------------
  buscar() {
    if (!this.fechaCorte) {
      this.error = 'Debe seleccionar fecha de corte.';
      return;
    }

    this.error = '';
    this.cargando = true;

    this.api.consultar(this.fechaCorte).subscribe({
      next: res => {
        this.items = res || [];
        this.estadistica = this.calcularEstadisticas(this.items);
        this.cargando = false;
      },
      error: () => {
        this.error = 'Error consultando datos.';
        this.cargando = false;
      }
    });
  }

  // -----------------------------
  // 🧹 Limpiar filtros
  // -----------------------------
  limpiar() {
    this.fechaCorte = '';
    this.items = [];
    this.estadistica = null;
    this.error = '';
  }

  // -----------------------------
  // 📥 Exportar Excel
  // -----------------------------
  exportar() {
    if (!this.items.length) return;
    this.exporter.exportar(this.items, this.estadistica);
  }

  // -----------------------------
  // 📊 Calcular estadísticas
  // -----------------------------
  private calcularEstadisticas(lista: any[]) {
    return {
      masculino: lista.filter(p => Number(p.genero) === 1).length,
      femenino:  lista.filter(p => Number(p.genero) === 2).length,
      juridica:  lista.filter(p => Number(p.genero) === 3).length,
      total:     lista.length
    };
  }

}
