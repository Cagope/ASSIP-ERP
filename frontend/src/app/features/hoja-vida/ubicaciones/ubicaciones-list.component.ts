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
 * Muestra la lista de personas (datos personales) y permite
 * gestionar la ubicación asociada a cada una.
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

  // 🔹 Paginación
  pagina = 1;
  tamanoPagina = 20;

  // ============================================================
  // 🚀 Ciclo de vida
  // ============================================================
  ngOnInit(): void {
    this.cargar();
  }

  // ============================================================
  // 🔄 Cargar datos personales y ubicaciones
  // ============================================================
  cargar(): void {
    this.cargando = true;
    this.error = '';

    Promise.all([
      this.dpApi.listar().toPromise(),
      this.ubApi.listar().toPromise()
    ])
      .then(([personas, ubicaciones]) => {
        // Crear un mapa para acceder rápidamente a las ubicaciones
        const mapaUbicaciones = new Map<number, Ubicacion>();
        (ubicaciones ?? []).forEach(u => {
          if (u.idDatosPersonal) mapaUbicaciones.set(u.idDatosPersonal, u);
        });

        // 🔹 Unir datos personales y ubicaciones, ordenando por fechaActualizacion
        this.personas = (personas ?? [])
          .map(p => ({
            ...p,
            ubicacion: p.idDatosPersonal
              ? mapaUbicaciones.get(p.idDatosPersonal) ?? null
              : null
          }))
          .sort((a, b) => {
            const fa = a.fechaActualizacion ? new Date(a.fechaActualizacion).getTime() : 0;
            const fb = b.fechaActualizacion ? new Date(b.fechaActualizacion).getTime() : 0;
            return fb - fa;
          });

        this.filtrar();
      })
      .catch(err => {
        console.error('❌ Error cargando datos:', err);
        this.error = 'Error al cargar datos.';
      })
      .finally(() => (this.cargando = false));
  }

  // ============================================================
  // 🔍 Filtrado local
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
    this.pagina = 1;
  }

  // ============================================================
  // 📄 Paginación local (20 registros)
  // ============================================================
  get paginadas(): (DatosPersonales & { ubicacion?: Ubicacion | null })[] {
    const inicio = (this.pagina - 1) * this.tamanoPagina;
    return this.filtradas.slice(inicio, inicio + this.tamanoPagina);
  }

  totalPaginas(): number {
    return Math.ceil(this.filtradas.length / this.tamanoPagina);
  }

  cambiarPagina(p: number): void {
    if (p < 1 || p > this.totalPaginas()) return;
    this.pagina = p;
  }

  // ============================================================
  // 🟢 Crear o editar ubicación
  // ============================================================
  gestionar(persona: DatosPersonales, ubicacion?: Ubicacion | null): void {
    if (ubicacion?.idUbicacion) {
      this.router.navigate(['/hoja-vida/ubicaciones', ubicacion.idUbicacion, 'editar']);
    } else {
      this.router.navigate(['/hoja-vida/ubicaciones/nuevo'], {
        queryParams: { idDatosPersonal: persona.idDatosPersonal }
      });
    }
  }

  // ============================================================
  // 🖨️ Impresión
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
  // 📤 Exportación
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
