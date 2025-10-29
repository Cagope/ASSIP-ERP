import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { DatosPersonalesApi, DatosPersonales } from '../datos-personales/datos-personales.api';
import { LaboralesApi } from './laborales.api';
import { Laboral } from '../../../shared/models/laboral.model';
import { LaboralesPrintService } from './laborales-print.service';
import { LaboralesExporterService } from './laborales-exporter.service';


/**
 * 💼 COMPONENTE: Listado de Información Laboral
 * ------------------------------------------------------------
 * Combina la información personal con los datos laborales asociados
 * a cada persona.
 *
 * Permite:
 *  - Listar personas con su información laboral.
 *  - Crear o editar los registros laborales.
 *  - Filtrar por documento o nombre.
 *  - Exportar a Excel.
 *  - Imprimir reporte laboral.
 */
@Component({
  selector: 'app-laborales-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './laborales-list.component.html',
  styleUrls: ['./laborales-list.component.scss']
})
export class LaboralesListComponent implements OnInit {

  // ============================================================
  // ⚙️ Inyección de dependencias
  // ============================================================
  private readonly dpApi = inject(DatosPersonalesApi);
  private readonly labApi = inject(LaboralesApi);
  private readonly router = inject(Router);
  private readonly printService = inject(LaboralesPrintService);
  private readonly exporter = inject(LaboralesExporterService);

  // ============================================================
  // 📦 Propiedades del componente
  // ============================================================
  personas: (DatosPersonales & { laboral?: Laboral | null })[] = [];
  filtradas: (DatosPersonales & { laboral?: Laboral | null })[] = [];

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
  // 🔄 Cargar datos personales y laborales (en paralelo)
  // ============================================================
  cargar(): void {
    this.cargando = true;
    this.error = '';

    Promise.all([
      this.dpApi.listar().toPromise(),
      this.labApi.listar().toPromise()
    ])
      .then(([personas, laborales]) => {
        // Mapa para relacionar datos laborales por ID de persona
        const mapaLaborales = new Map<number, Laboral>();
        (laborales ?? []).forEach(l => {
          if (l.idDatosPersonal) mapaLaborales.set(l.idDatosPersonal, l);
        });

        // Unir personas + laborales, ordenadas por fecha de edición
        this.personas = (personas ?? [])
          .sort((a, b) => {
            const fa = a.fechaEdicion ? new Date(a.fechaEdicion).getTime() : 0;
            const fb = b.fechaEdicion ? new Date(b.fechaEdicion).getTime() : 0;
            return fb - fa;
          })
          .map(p => ({
            ...p,
            laboral: p.idDatosPersonal
              ? mapaLaborales.get(p.idDatosPersonal) ?? null
              : null
          }));

        this.filtrar();
      })
      .catch(err => {
        console.error('❌ Error cargando datos laborales:', err);
        this.error = 'Error al cargar los datos laborales.';
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
  // 🟢 Crear o editar registro laboral asociado
  // ============================================================
  gestionar(persona: DatosPersonales, laboral?: Laboral | null): void {
    if (laboral?.idLaboral) {
      // Si ya tiene un registro laboral, redirige a edición
      this.router.navigate(['/hoja-vida/laborales', laboral.idLaboral, 'editar']);
    } else {
      // Si no tiene, redirige a modo creación
      this.router.navigate(['/hoja-vida/laborales/nuevo'], {
        queryParams: { idDatosPersonal: persona.idDatosPersonal }
      });
    }
  }

  // ============================================================
  // 🖨️ Imprimir listado laboral
  // ============================================================
  imprimir(): void {
    const datos = this.personas.map(p => ({
      ...p.laboral,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.printService.imprimir(
      datos.filter(l => !!l) as unknown as (
        Laboral & {
          documento?: string;
          nombrePersona?: string;
          nombrePais?: string;
          nombreDepartamento?: string;
          nombreCiudad?: string;
          tipoEmpresa?: string;
          tipoContrato?: string;
          jornadaLaboral?: string;
        }
      )[]
    );
  }

  // ============================================================
  // 📤 Exportar listado a Excel
  // ============================================================
  exportar(): void {
    const datos = this.personas.map(p => ({
      ...p.laboral,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.exporter.exportarExcel(
      datos.filter(l => !!l) as unknown as (
        Laboral & {
          documento?: string;
          nombrePersona?: string;
          nombrePais?: string;
          nombreDepartamento?: string;
          nombreCiudad?: string;
          tipoEmpresa?: string;
          tipoContrato?: string;
          jornadaLaboral?: string;
        }
      )[]
    );
  }
}
