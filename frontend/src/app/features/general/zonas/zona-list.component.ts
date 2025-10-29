import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { ZonasApi, Zona } from './zonas.api';
import { ZonasExporterService } from './zonas-exporter.service';
import { printDirect } from '../../../shared/print/print-base.template';
import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';

@Component({
  selector: 'app-zona-list',
  standalone: true,
  imports: [CommonModule, HeaderActionsComponent],
  templateUrl: './zona-list.component.html',
  styleUrls: ['./zona-list.component.scss']
})
export class ZonaListComponent implements OnInit {
  private readonly api = inject(ZonasApi);
  private readonly exporter = inject(ZonasExporterService);
  private readonly router = inject(Router);

  zonas: Zona[] = [];
  cargando = false;
  error?: string;

  async ngOnInit(): Promise<void> {
    this.cargando = true;
    try {
      const data = await this.api.listar().toPromise();
      this.zonas = data ?? [];
    } catch (err) {
      this.error = (err as HttpErrorResponse).message || 'Error cargando zonas';
    } finally {
      this.cargando = false;
    }
  }

  nuevaZona(): void {
    this.router.navigate(['/general/zonas/nuevo']);
  }

  editarZona(id: number): void {
    this.router.navigate([`/general/zonas/${id}/editar`]);
  }

  eliminarZona(id: number): void {
    if (!confirm('¿Desea eliminar esta zona de forma permanente?')) return;

    this.api.eliminar(id).subscribe({
      next: () => this.ngOnInit(),
      error: (err) => alert(`❌ Error eliminando la zona: ${err.message}`),
    });
  }

  exportarExcel(): void {
    this.exporter.exportar(this.zonas);
  }

  /** 🖨️ Imprimir listado limpio de zonas */
  imprimirListado(): void {
    if (!this.zonas || this.zonas.length === 0) {
      alert('⚠️ No hay zonas para imprimir.');
      return;
    }

    setTimeout(() => {
      const tablaEl = document.getElementById('tabla-zonas');
      if (!tablaEl) {
        alert('⚠️ No se encontró la tabla de zonas.');
        return;
      }

      const tabla = tablaEl.cloneNode(true) as HTMLElement;

      // Limpia la columna de acciones antes de imprimir
      tabla.querySelectorAll('th, td').forEach(el => {
        if (el.textContent?.trim() === 'Acciones' ||
            el.textContent?.includes('✏️') ||
            el.textContent?.includes('🗑️')) {
          el.remove();
        }
      });

      printDirect(tabla.outerHTML, {
        title: 'INFORME DE ZONAS',
        detalle1: 'Listado general de zonas registradas en el sistema',
        detalle2: '',
        user: 'admin'
      });
    }, 300);
  }
}
