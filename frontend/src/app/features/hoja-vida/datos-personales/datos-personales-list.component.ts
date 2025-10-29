import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { DatosPersonalesApi, DatosPersonales } from './datos-personales.api';
import { DatosPersonalesPrintService } from './datos-personales-print.service';
import { DatosPersonalesExporterService } from './datos-personales-exporter.service'; // ✅ nuevo import

@Component({
  selector: 'app-datos-personales-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './datos-personales-list.component.html',
  styleUrls: ['./datos-personales-list.component.scss']
})
export class DatosPersonalesListComponent implements OnInit {
  private readonly api = inject(DatosPersonalesApi);
  private readonly router = inject(Router);
  private readonly printService = inject(DatosPersonalesPrintService);
  private readonly exporter = inject(DatosPersonalesExporterService); // ✅ nuevo servicio

  datos: DatosPersonales[] = [];
  filtrados: DatosPersonales[] = [];
  cargando = false;
  error = '';
  filtro = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.api.listar().subscribe({
      next: (data) => {
        // ✅ Ordenar por fecha_actualizacion (desc)
        this.datos = (data ?? []).sort((a, b) => {
          const fa = a.fechaActualizacion ? new Date(a.fechaActualizacion).getTime() : 0;
          const fb = b.fechaActualizacion ? new Date(b.fechaActualizacion).getTime() : 0;
          return fb - fa;
        });
        this.filtrar();
      },
      error: () => (this.error = 'Error al cargar datos personales'),
      complete: () => (this.cargando = false)
    });
  }

  filtrar(): void {
    const term = this.filtro.toLowerCase().trim();
    this.filtrados = !term
      ? this.datos
      : this.datos.filter(d =>
          `${d.documento} ${d.nombres} ${d.primerApellido} ${d.segundoApellido ?? ''}`
            .toLowerCase()
            .includes(term)
        );
  }

  nuevo(): void {
    this.router.navigate(['/hoja-vida/datos-personales/nuevo']);
  }

  editar(id: number): void {
    this.router.navigate(['/hoja-vida/datos-personales', id, 'editar']);
  }

  eliminar(id: number): void {
    if (!confirm('¿Eliminar este registro de datos personales?')) return;
    this.api.eliminar(id).subscribe(() => this.cargar());
  }

  // ===========================================================
  // 🖨️ Impresión y Exportación
  // ===========================================================
  imprimir(): void {
    if (!this.filtrados || this.filtrados.length === 0) {
      alert('⚠️ No hay registros para imprimir.');
      return;
    }
    this.printService.imprimir(this.filtrados);
  }

  exportar(): void {
    if (!this.filtrados || this.filtrados.length === 0) {
      alert('⚠️ No hay registros para exportar.');
      return;
    }
    this.exporter.exportarExcel(this.filtrados); // ✅ conexión real al servicio
  }
}
