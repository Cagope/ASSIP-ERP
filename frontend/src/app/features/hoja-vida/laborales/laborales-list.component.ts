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

@Component({
  selector: 'app-laborales-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './laborales-list.component.html',
  styleUrls: ['./laborales-list.component.scss']
})
export class LaboralesListComponent implements OnInit {

  private readonly dpApi = inject(DatosPersonalesApi);
  private readonly labApi = inject(LaboralesApi);
  private readonly router = inject(Router);
  private readonly printService = inject(LaboralesPrintService);
  private readonly exporter = inject(LaboralesExporterService);

  personas: (DatosPersonales & { laboral?: Laboral | null })[] = [];
  filtradas: (DatosPersonales & { laboral?: Laboral | null })[] = [];

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
      this.labApi.listar().toPromise()
    ])
      .then(([personas, laborales]) => {
        const mapaLaborales = new Map<number, Laboral>();
        (laborales ?? []).forEach(l => {
          if (l.idDatosPersonal) mapaLaborales.set(l.idDatosPersonal, l);
        });

        // 🔸 Ordenar por fechaActualizacion (no por fechaEdicion)
        this.personas = (personas ?? [])
          .sort((a, b) => {
            const fa = a.fechaActualizacion ? new Date(a.fechaActualizacion).getTime() : 0;
            const fb = b.fechaActualizacion ? new Date(b.fechaActualizacion).getTime() : 0;
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

  get paginadas(): (DatosPersonales & { laboral?: Laboral | null })[] {
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

  gestionar(persona: DatosPersonales, laboral?: Laboral | null): void {
    if (laboral?.idLaboral) {
      this.router.navigate(['/hoja-vida/laborales', laboral.idLaboral, 'editar']);
    } else {
      this.router.navigate(['/hoja-vida/laborales/nuevo'], {
        queryParams: { idDatosPersonal: persona.idDatosPersonal }
      });
    }
  }

  imprimir(): void {
    const datos = this.personas.map(p => ({
      ...p.laboral,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.printService.imprimir(
      datos.filter(l => !!l) as unknown as Laboral[]
    );
  }

  exportar(): void {
    const datos = this.personas.map(p => ({
      ...p.laboral,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.exporter.exportarExcel(
      datos.filter(l => !!l) as unknown as Laboral[]
    );
  }
}
