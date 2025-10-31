import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { DatosPersonalesApi, DatosPersonales } from '../datos-personales/datos-personales.api';
import { PermisosEspecialesApi } from './permisos-especiales.api';
import { PermisoEspecial } from '../../../shared/models/permisos-especiales.model'; // ✅ corregido (plural)
import { PermisosEspecialesPrintService } from './permisos-especiales-print.service';
import { PermisosEspecialesExporterService } from './permisos-especiales-exporter.service';

/**
 * 🧾 COMPONENTE: Listado Permisos Especiales
 * ------------------------------------------------------------
 * Combina la información personal con los permisos de contacto
 * asociados a cada persona.
 * Permite:
 *  - Listar personas y sus permisos especiales.
 *  - Crear o editar registros.
 *  - Filtrar por documento o nombre.
 *  - Exportar a Excel.
 *  - Imprimir el listado.
 */
@Component({
  selector: 'app-permisos-especiales-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './permisos-especiales-list.component.html',
  styleUrls: ['./permisos-especiales-list.component.scss']
})
export class PermisosEspecialesListComponent implements OnInit {

  // ============================================================
  // ⚙️ Inyección de dependencias
  // ============================================================
  private readonly dpApi = inject(DatosPersonalesApi);
  private readonly permisosApi = inject(PermisosEspecialesApi);
  private readonly router = inject(Router);
  private readonly printService = inject(PermisosEspecialesPrintService);
  private readonly exporter = inject(PermisosEspecialesExporterService);

  // ============================================================
  // 📦 Propiedades del componente
  // ============================================================
  personas: (DatosPersonales & { permisos?: PermisoEspecial | null })[] = [];
  filtradas: (DatosPersonales & { permisos?: PermisoEspecial | null })[] = [];

  cargando = false;
  filtro = '';
  error = '';

  // ============================================================
  // 🚀 Inicialización
  // ============================================================
  ngOnInit(): void {
    this.cargar();
  }

  // ============================================================
  // 🔄 Cargar datos personales + Permisos Especiales
  // ============================================================
  cargar(): void {
    this.cargando = true;
    this.error = '';

    Promise.all([
      this.dpApi.listar().toPromise(),
      this.permisosApi.listar().toPromise()
    ])
      .then(([personas, permisos]) => {
        // Crear mapa de permisos por ID persona
        const mapaPermisos = new Map<number, PermisoEspecial>();
        (permisos ?? []).forEach(p => {
          if (p.idDatosPersonal) mapaPermisos.set(p.idDatosPersonal, p);
        });

        // Unir datos personales + permisos
        this.personas = (personas ?? [])
          .sort((a, b) => {
            const fa = a.fechaEdicion ? new Date(a.fechaEdicion).getTime() : 0;
            const fb = b.fechaEdicion ? new Date(b.fechaEdicion).getTime() : 0;
            return fb - fa;
          })
          .map(p => ({
            ...p,
            permisos: p.idDatosPersonal
              ? mapaPermisos.get(p.idDatosPersonal) ?? null
              : null
          }));

        this.filtrar();
      })
      .catch(err => {
        console.error('❌ Error cargando permisos especiales:', err);
        this.error = 'Error al cargar los datos de permisos especiales.';
      })
      .finally(() => (this.cargando = false));
  }

  // ============================================================
  // 🔍 Filtro por nombre o documento
  // ============================================================
  filtrar(): void {
    const term = this.filtro.toLowerCase().trim();
    this.filtradas = !term
      ? this.personas
      : this.personas.filter(p =>
          `${p.documento} ${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
            .toLowerCase()
            .includes(term)
        );
  }

  // ============================================================
  // 🟢 Crear o editar registro de Permisos
  // ============================================================
  gestionar(persona: DatosPersonales, permisos?: PermisoEspecial | null): void {
    if (permisos?.idPermisoEspecial) {
      // Editar registro existente
      this.router.navigate(['/hoja-vida/permisos-especiales', permisos.idPermisoEspecial, 'editar']);
    } else {
      // Crear nuevo registro
      this.router.navigate(['/hoja-vida/permisos-especiales/nuevo'], {
        queryParams: { idDatosPersonal: persona.idDatosPersonal }
      });
    }
  }

  // ============================================================
  // 🖨️ Imprimir listado
  // ============================================================
  imprimir(): void {
    const datos = this.personas.map(p => ({
      ...p.permisos,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.printService.imprimir(
      datos.filter(s => !!s) as unknown as (
        PermisoEspecial & {
          documento?: string;
          nombrePersona?: string;
        }
      )[]
    );
  }

  // ============================================================
  // 📤 Exportar listado a Excel
  // ============================================================
  exportar(): void {
    const datos = this.personas.map(p => ({
      ...p.permisos,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.exporter.exportarExcel(
      datos.filter(s => !!s) as unknown as (
        PermisoEspecial & {
          documento?: string;
          nombrePersona?: string;
        }
      )[]
    );
  }
}
