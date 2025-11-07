import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { DatosPersonalesApi, DatosPersonales } from '../datos-personales/datos-personales.api';
import { ReferenciasPersonalesApi } from './referencias-personales.api';
import { ReferenciasPersonalesPrintService } from './referencias-personales-print.service';
import { ReferenciasPersonalesExporterService } from './referencias-personales-exporter.service';
import { ReferenciaPersonal } from '../../../shared/models/referencia-personal.model';

@Component({
  selector: 'app-referencias-personales-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './referencias-personales-list.component.html',
  styleUrls: ['./referencias-personales-list.component.scss']
})
export class ReferenciasPersonalesListComponent implements OnInit {

  private readonly dpApi = inject(DatosPersonalesApi);
  private readonly refApi = inject(ReferenciasPersonalesApi);
  private readonly router = inject(Router);
  private readonly printService = inject(ReferenciasPersonalesPrintService);
  private readonly exporter = inject(ReferenciasPersonalesExporterService);

  personas: (DatosPersonales & { referencias?: ReferenciaPersonal[] | null })[] = [];
  filtradas: (DatosPersonales & { referencias?: ReferenciaPersonal[] | null })[] = [];

  cargando = false;
  filtro = '';
  error = '';

  // 📄 Paginación
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
      this.refApi.listar().toPromise()
    ])
      .then(([personas, referencias]) => {
        const mapaReferencias = new Map<number, ReferenciaPersonal[]>();
        (referencias ?? []).forEach(r => {
          if (r.idDatosPersonal != null) {
            const lista = mapaReferencias.get(r.idDatosPersonal) ?? [];
            lista.push(r);
            mapaReferencias.set(r.idDatosPersonal, lista);
          }
        });

        this.personas = (personas ?? [])
          .sort((a, b) => {
            const fa = a.fechaActualizacion ? new Date(a.fechaActualizacion).getTime() : 0;
            const fb = b.fechaActualizacion ? new Date(b.fechaActualizacion).getTime() : 0;
            return fb - fa;
          })
          .map(p => ({
            ...p,
            referencias: mapaReferencias.get(p.idDatosPersonal ?? 0) ?? []
          }));

        this.filtrar();
      })
      .catch(err => {
        console.error('❌ Error cargando datos:', err);
        this.error = 'Error al cargar las referencias personales.';
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

  get paginadas(): (DatosPersonales & { referencias?: ReferenciaPersonal[] | null })[] {
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

  gestionar(persona: DatosPersonales, referencia?: ReferenciaPersonal | null): void {
    if (referencia?.idReferenciaPersonal) {
      this.router.navigate(['/hoja-vida/referencias-personales', referencia.idReferenciaPersonal, 'editar']);
    } else {
      this.router.navigate(['/hoja-vida/referencias-personales/nuevo'], {
        queryParams: { idDatosPersonal: persona.idDatosPersonal }
      });
    }
  }

  imprimir(): void {
    const datos = this.personas.flatMap(p =>
      (p.referencias ?? []).map(r => ({
        ...r,
        documento: p.documento,
        nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
      }))
    );

    this.printService.imprimir(
      datos.filter(r => !!r) as unknown as (
        ReferenciaPersonal & { documento?: string; nombrePersona?: string }
      )[]
    );
  }

  exportar(): void {
    const datos = this.personas.flatMap(p =>
      (p.referencias ?? []).map(r => ({
        ...r,
        documento: p.documento,
        nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
      }))
    );

    this.exporter.exportarExcel(
      datos.filter(r => !!r) as unknown as (
        ReferenciaPersonal & { documento?: string; nombrePersona?: string }
      )[]
    );
  }
}
