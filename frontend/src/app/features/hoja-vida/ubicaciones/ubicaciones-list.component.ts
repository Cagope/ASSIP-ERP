import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { DatosPersonalesApi, DatosPersonales } from '../datos-personales/datos-personales.api';
import { UbicacionesPrintService } from './ubicaciones-print.service';
import { UbicacionesExporterService } from './ubicaciones-exporter.service';
import { UbicacionesApi } from './ubicaciones.api';
import { Ubicacion } from '../../../shared/models/ubicacion.model';

/**
 * 🧭 COMPONENTE: Listado de Ubicaciones
 * ------------------------------------------------------------
 * Este componente combina la información personal con las
 * ubicaciones asociadas a cada persona.
 *
 * Permite:
 *  - Listar todas las personas con su dirección y contactos.
 *  - Crear o editar la ubicación de cada persona.
 *  - Filtrar por documento o nombre.
 *  - Exportar el listado a Excel.
 *  - Imprimir un informe en formato horizontal.
 */
@Component({
  selector: 'app-ubicaciones-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './ubicaciones-list.component.html',
  styleUrls: ['./ubicaciones-list.component.scss']
})
export class UbicacionesListComponent implements OnInit {

  // ============================================================
  // ⚙️ Inyección de dependencias
  // ============================================================
  private readonly dpApi = inject(DatosPersonalesApi);
  private readonly ubApi = inject(UbicacionesApi);
  private readonly router = inject(Router);
  private readonly printService = inject(UbicacionesPrintService);
  private readonly exporter = inject(UbicacionesExporterService);

  // ============================================================
  // 📦 Propiedades del componente
  // ============================================================
  personas: (DatosPersonales & { ubicacion?: Ubicacion | null })[] = [];
  filtradas: (DatosPersonales & { ubicacion?: Ubicacion | null })[] = [];

  cargando = false;
  filtro = '';
  error = '';

  // ============================================================
  // 🚀 Ciclo de vida: Inicialización
  // ============================================================
  ngOnInit(): void {
    this.cargar();
  }

  // ============================================================
  // 🔄 Cargar datos personales y ubicaciones (en paralelo)
  // ============================================================
  cargar(): void {
    this.cargando = true;
    this.error = '';

    // Combinar consultas: datos personales + ubicaciones
    Promise.all([
      this.dpApi.listar().toPromise(),
      this.ubApi.listar().toPromise()
    ])
      .then(([personas, ubicaciones]) => {
        // Crear un mapa para acceder rápidamente a ubicaciones por ID de persona
        const mapaUbicaciones = new Map<number, Ubicacion>();
        (ubicaciones ?? []).forEach(u => {
          if (u.idDatosPersonal) mapaUbicaciones.set(u.idDatosPersonal, u);
        });

        // Unir personas + ubicaciones, ordenadas por fecha de edición
        this.personas = (personas ?? [])
          .sort((a, b) => {
            const fa = a.fechaEdicion ? new Date(a.fechaEdicion).getTime() : 0;
            const fb = b.fechaEdicion ? new Date(b.fechaEdicion).getTime() : 0;
            return fb - fa;
          })
          .map(p => ({
            ...p,
            ubicacion: p.idDatosPersonal
              ? mapaUbicaciones.get(p.idDatosPersonal) ?? null
              : null
          }));

        this.filtrar();
      })
      .catch(err => {
        console.error('❌ Error cargando datos:', err);
        this.error = 'Error al cargar datos.';
      })
      .finally(() => (this.cargando = false));
  }

  // ============================================================
  // 🔍 Filtrado por documento o nombre
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
  // 🟢 Crear o editar ubicación asociada
  // ============================================================
  gestionar(persona: DatosPersonales, ubicacion?: Ubicacion | null): void {
    if (ubicacion?.idUbicacion) {
      // Si ya tiene una ubicación, redirige al formulario de edición
      this.router.navigate(['/hoja-vida/ubicaciones', ubicacion.idUbicacion, 'editar']);
    } else {
      // Si no tiene ubicación, abre el formulario en modo creación
      this.router.navigate(['/hoja-vida/ubicaciones/nuevo'], {
        queryParams: { idDatosPersonal: persona.idDatosPersonal }
      });
    }
  }

  // ============================================================
  // 🖨️ Imprimir listado de ubicaciones
  // ============================================================
  imprimir(): void {
    const datos = this.personas.map(p => ({
      ...p.ubicacion,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.printService.imprimir(
      datos.filter(u => !!u) as unknown as (
        Ubicacion & {
          documento?: string;
          nombrePersona?: string;
          nombrePais?: string;
          nombreDepartamento?: string;
          nombreCiudad?: string;
          nombreSubZona?: string;
        }
      )[]
    );
  }

  // ============================================================
  // 📤 Exportar listado a Excel
  // ============================================================
  exportar(): void {
    const datos = this.personas.map(p => ({
      ...p.ubicacion,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.exporter.exportarExcel(
      datos.filter(u => !!u) as unknown as (
        Ubicacion & {
          documento?: string;
          nombrePersona?: string;
          nombrePais?: string;
          nombreDepartamento?: string;
          nombreCiudad?: string;
          nombreSubZona?: string;
        }
      )[]
    );
  }
}
