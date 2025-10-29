import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AgenciasApi, Agencia } from './agencia.api';
import { AgenciasExporterService } from './agencias-exporter.service';
import { CatalogosApi, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';
import { printDirect } from '../../../shared/print/print-base.template';
import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';


@Component({
  selector: 'app-agencia-list',
  standalone: true,
  imports: [CommonModule, HeaderActionsComponent],
  templateUrl: './agencia-list.component.html',
  styleUrls: ['./agencia-list.component.scss']
})
export class AgenciaListComponent implements OnInit {
  private readonly api = inject(AgenciasApi);
  private readonly exporter = inject(AgenciasExporterService);
  private readonly router = inject(Router);
  private readonly catalogos = inject(CatalogosApi);

  agencias: (Agencia & { nombreDepartamento?: string; nombreCiudad?: string })[] = [];
  departamentos: Departamento[] = [];
  ciudades: Ciudad[] = [];

  cargando = false;
  error?: string;

  async ngOnInit(): Promise<void> {
    this.cargando = true;
    try {
      const [deps, cits, ags] = await Promise.all([
        this.catalogos.listarDepartamentos().toPromise(),
        this.catalogos.listarTodasLasCiudades().toPromise(),
        this.api.listar().toPromise(),
      ]);

      this.departamentos = deps ?? [];
      this.ciudades = cits ?? [];

      this.agencias = (ags ?? []).map((a) => ({
        ...a,
        nombreDepartamento: this.obtenerNombreDepartamento(a.idDepartamento),
        nombreCiudad: this.obtenerNombreCiudad(a.idCiudad),
      }));
    } catch (err) {
      this.error = (err as HttpErrorResponse).message || 'Error cargando agencias';
    } finally {
      this.cargando = false;
    }
  }

  private obtenerNombreDepartamento(id?: number): string {
    const dep = this.departamentos.find((d) => d.idDepartamento === id);
    return dep?.nombreDepartamento ?? '';
  }

  private obtenerNombreCiudad(id?: number): string {
    const c = this.ciudades.find((ci) => ci.idCiudad === id);
    return c?.nombreCiudad ?? '';
  }

  nuevaAgencia(): void {
    this.router.navigate(['/general/agencias/nueva']);
  }

  editarAgencia(id: number): void {
    this.router.navigate(['/general/agencias', id]);
  }

  eliminarAgencia(id: number): void {
    if (!confirm('¿Desea eliminar esta agencia de forma permanente?')) return;

    this.api.eliminar(id).subscribe({
      next: () => this.ngOnInit(),
      error: (err) => alert(`❌ Error eliminando la agencia: ${err.message}`),
    });
  }

  exportarExcel(): void {
    this.exporter.exportarExcel(this.agencias);
  }

  /** 🖨️ Imprimir listado limpio de agencias */
  imprimirListado(): void {
    if (!this.agencias || this.agencias.length === 0) {
      alert('⚠️ No hay agencias para imprimir.');
      return;
    }

    // Espera un ciclo de render antes de capturar el HTML
    setTimeout(() => {
      const tablaEl = document.getElementById('tabla-agencias');
      if (!tablaEl) {
        alert('⚠️ No se encontró la tabla de agencias.');
        return;
      }

      // Clonar el contenido ya renderizado (asegura filas completas)
      const tabla = tablaEl.cloneNode(true) as HTMLElement;

      // Limpiar columna de acciones
      tabla.querySelectorAll('th, td').forEach(el => {
        if (el.textContent?.trim() === 'Acciones' || el.textContent?.includes('✏️') || el.textContent?.includes('🗑️')) {
          el.remove();
        }
      });

      // Pasar el HTML al generador
      printDirect(tabla.outerHTML, {
        title: 'INFORME DE AGENCIAS',
        detalle1: 'Listado general de agencias registradas en el sistema',
        detalle2: '', // opcional
        user: 'admin'
      });
    }, 300); // <-- deja 300 ms para garantizar que el DOM esté completo
  }

}
