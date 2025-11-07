import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { DatosPersonalesApi, DatosPersonales } from '../datos-personales/datos-personales.api';
import { PermisosEspecialesApi } from './permisos-especiales.api';
import { PermisoEspecial } from '../../../shared/models/permisos-especiales.model';
import { PermisosEspecialesPrintService } from './permisos-especiales-print.service';
import { PermisosEspecialesExporterService } from './permisos-especiales-exporter.service';

@Component({
  selector: 'app-permisos-especiales-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './permisos-especiales-list.component.html',
  styleUrls: ['./permisos-especiales-list.component.scss']
})
export class PermisosEspecialesListComponent implements OnInit {
  private readonly dpApi = inject(DatosPersonalesApi);
  private readonly permisosApi = inject(PermisosEspecialesApi);
  private readonly router = inject(Router);
  private readonly printService = inject(PermisosEspecialesPrintService);
  private readonly exporter = inject(PermisosEspecialesExporterService);

  personas: (DatosPersonales & { permisos?: PermisoEspecial | null })[] = [];
  filtradas: (DatosPersonales & { permisos?: PermisoEspecial | null })[] = [];

  cargando = false;
  filtro = '';
  error = '';

  // 🔹 Paginación local
  pagina = 1;
  tamanoPagina = 20;

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';

    Promise.all([
      this.dpApi.listar().toPromise(),
      this.permisosApi.listar().toPromise()
    ])
      .then(([personas, permisos]) => {
        const mapaPermisos = new Map<number, PermisoEspecial>();
        (permisos ?? []).forEach(p => {
          if (p.idDatosPersonal) mapaPermisos.set(p.idDatosPersonal, p);
        });

        this.personas = (personas ?? [])
          .sort((a, b) => {
            const fa = a.fechaActualizacion ? new Date(a.fechaActualizacion).getTime() : 0;
            const fb = b.fechaActualizacion ? new Date(b.fechaActualizacion).getTime() : 0;
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

  get paginadas(): (DatosPersonales & { permisos?: PermisoEspecial | null })[] {
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

  gestionar(persona: DatosPersonales, permisos?: PermisoEspecial | null): void {
    if (permisos?.idPermisoEspecial) {
      this.router.navigate(['/hoja-vida/permisos-especiales', permisos.idPermisoEspecial, 'editar']);
    } else {
      this.router.navigate(['/hoja-vida/permisos-especiales/nuevo'], {
        queryParams: { idDatosPersonal: persona.idDatosPersonal }
      });
    }
  }

  imprimir(): void {
    const datos = this.personas.map(p => ({
      ...p.permisos,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.printService.imprimir(
      datos.filter(s => !!s) as unknown as (
        PermisoEspecial & { documento?: string; nombrePersona?: string }
      )[]
    );
  }

  exportar(): void {
    const datos = this.personas.map(p => ({
      ...p.permisos,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.exporter.exportarExcel(
      datos.filter(s => !!s) as unknown as (
        PermisoEspecial & { documento?: string; nombrePersona?: string }
      )[]
    );
  }
}
