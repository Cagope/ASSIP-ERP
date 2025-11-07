import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { DatosPersonalesApi, DatosPersonales } from '../datos-personales/datos-personales.api';
import { SarlaftApi } from './sarlaft.api';
import { Sarlaft } from '../../../shared/models/sarlaft.model';
import { SarlaftPrintService } from './sarlaft-print.service';
import { SarlaftExporterService } from './sarlaft-exporter.service';

@Component({
  selector: 'app-sarlaft-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './sarlaft-list.component.html',
  styleUrls: ['./sarlaft-list.component.scss']
})
export class SarlaftListComponent implements OnInit {
  private readonly dpApi = inject(DatosPersonalesApi);
  private readonly sarlaftApi = inject(SarlaftApi);
  private readonly router = inject(Router);
  private readonly printService = inject(SarlaftPrintService);
  private readonly exporter = inject(SarlaftExporterService);

  personas: (DatosPersonales & { sarlaft?: Sarlaft | null })[] = [];
  filtradas: (DatosPersonales & { sarlaft?: Sarlaft | null })[] = [];

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
      this.sarlaftApi.listar().toPromise()
    ])
      .then(([personas, sarlafts]) => {
        const mapaSarlaft = new Map<number, Sarlaft>();
        (sarlafts ?? []).forEach(s => {
          if (s.idDatosPersonal) mapaSarlaft.set(s.idDatosPersonal, s);
        });

        this.personas = (personas ?? [])
          .sort((a, b) => {
            const fa = a.fechaActualizacion ? new Date(a.fechaActualizacion).getTime() : 0;
            const fb = b.fechaActualizacion ? new Date(b.fechaActualizacion).getTime() : 0;
            return fb - fa;
          })
          .map(p => ({
            ...p,
            sarlaft: p.idDatosPersonal ? mapaSarlaft.get(p.idDatosPersonal) ?? null : null
          }));

        this.filtrar();
      })
      .catch(err => {
        console.error('❌ Error cargando datos SARLAFT:', err);
        this.error = 'Error al cargar los datos SARLAFT.';
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

  get paginadas(): (DatosPersonales & { sarlaft?: Sarlaft | null })[] {
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

  gestionar(persona: DatosPersonales, sarlaft?: Sarlaft | null): void {
    if (sarlaft?.idSarlaft) {
      this.router.navigate(['/hoja-vida/sarlaft', sarlaft.idSarlaft, 'editar']);
    } else {
      this.router.navigate(['/hoja-vida/sarlaft/nuevo'], {
        queryParams: { idDatosPersonal: persona.idDatosPersonal }
      });
    }
  }

  imprimir(): void {
    const datos = this.personas.map(p => ({
      ...p.sarlaft,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.printService.imprimir(
      datos.filter(s => !!s) as unknown as (Sarlaft & { documento?: string; nombrePersona?: string })[]
    );
  }

  exportar(): void {
    const datos = this.personas.map(p => ({
      ...p.sarlaft,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.exporter.exportarExcel(
      datos.filter(s => !!s) as unknown as (Sarlaft & { documento?: string; nombrePersona?: string })[]
    );
  }
}
