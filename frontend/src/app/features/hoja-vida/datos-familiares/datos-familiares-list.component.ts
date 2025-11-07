import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { DatosPersonalesApi, DatosPersonales } from '../datos-personales/datos-personales.api';
import { DatosFamiliaresApi } from './datos-familiares.api';
import { DatosFamiliar } from '../../../shared/models/datos-familiar.model';
import { DatosFamiliaresPrintService } from './datos-familiares-print.service';
import { DatosFamiliaresExporterService } from './datos-familiares-exporter.service';

@Component({
  selector: 'app-datos-familiares-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './datos-familiares-list.component.html',
  styleUrls: ['./datos-familiares-list.component.scss']
})
export class DatosFamiliaresListComponent implements OnInit {
  private readonly dpApi = inject(DatosPersonalesApi);
  private readonly famApi = inject(DatosFamiliaresApi);
  private readonly router = inject(Router);
  private readonly printService = inject(DatosFamiliaresPrintService);
  private readonly exporter = inject(DatosFamiliaresExporterService);

  personas: (DatosPersonales & { familiares?: DatosFamiliar[] | null })[] = [];
  filtradas: (DatosPersonales & { familiares?: DatosFamiliar[] | null })[] = [];

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
      this.famApi.listar().toPromise()
    ])
      .then(([personas, familiares]) => {
        const mapaFamiliares = new Map<number, DatosFamiliar[]>();
        (familiares ?? []).forEach(f => {
          if (f.idDatosPersonal != null) {
            const lista = mapaFamiliares.get(f.idDatosPersonal) ?? [];
            lista.push(f);
            mapaFamiliares.set(f.idDatosPersonal, lista);
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
            familiares: mapaFamiliares.get(p.idDatosPersonal ?? 0) ?? []
          }));

        this.filtrar();
      })
      .catch(err => {
        console.error('❌ Error cargando datos familiares:', err);
        this.error = 'Error al cargar los datos familiares.';
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

  get paginadas(): (DatosPersonales & { familiares?: DatosFamiliar[] | null })[] {
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

  gestionar(persona: DatosPersonales, familiar?: DatosFamiliar | null): void {
    if (familiar?.idDatosFamiliares) {
      this.router.navigate(['/hoja-vida/datos-familiares', familiar.idDatosFamiliares, 'editar']);
    } else {
      this.router.navigate(['/hoja-vida/datos-familiares/nuevo'], {
        queryParams: { idDatosPersonal: persona.idDatosPersonal }
      });
    }
  }

  imprimir(): void {
    const datos = this.personas.flatMap(p =>
      (p.familiares ?? []).map(f => ({
        ...f,
        documento: p.documento,
        nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
      }))
    );

    this.printService.imprimir(
      datos.filter(f => !!f) as unknown as (
        DatosFamiliar & { documento?: string; nombrePersona?: string }
      )[]
    );
  }

  exportar(): void {
    const datos = this.personas.flatMap(p =>
      (p.familiares ?? []).map(f => ({
        ...f,
        documento: p.documento,
        nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
      }))
    );

    this.exporter.exportarExcel(
      datos.filter(f => !!f) as unknown as (
        DatosFamiliar & { documento?: string; nombrePersona?: string }
      )[]
    );
  }
}
