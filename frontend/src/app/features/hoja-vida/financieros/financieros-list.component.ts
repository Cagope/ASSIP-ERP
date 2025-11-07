import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { DatosPersonalesApi, DatosPersonales } from '../datos-personales/datos-personales.api';
import { FinancierosApi } from './financieros.api';
import { Financiero } from '../../../shared/models/financiero.model';
import { FinancierosPrintService } from './financieros-print.service';
import { FinancierosExporterService } from './financieros-exporter.service';

@Component({
  selector: 'app-financieros-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './financieros-list.component.html',
  styleUrls: ['./financieros-list.component.scss']
})
export class FinancierosListComponent implements OnInit {
  private readonly dpApi = inject(DatosPersonalesApi);
  private readonly finApi = inject(FinancierosApi);
  private readonly router = inject(Router);
  private readonly printService = inject(FinancierosPrintService);
  private readonly exporter = inject(FinancierosExporterService);

  personas: (DatosPersonales & { financiero?: Financiero | null })[] = [];
  filtradas: (DatosPersonales & { financiero?: Financiero | null })[] = [];

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
      this.finApi.listar().toPromise()
    ])
      .then(([personas, financieros]) => {
        const mapaFinancieros = new Map<number, Financiero>();
        (financieros ?? []).forEach(f => {
          if (f.idDatosPersonal) mapaFinancieros.set(f.idDatosPersonal, f);
        });

        this.personas = (personas ?? [])
          .sort((a, b) => {
            const fa = a.fechaActualizacion ? new Date(a.fechaActualizacion).getTime() : 0;
            const fb = b.fechaActualizacion ? new Date(b.fechaActualizacion).getTime() : 0;
            return fb - fa;
          })
          .map(p => ({
            ...p,
            financiero: p.idDatosPersonal ? mapaFinancieros.get(p.idDatosPersonal) ?? null : null
          }));

        this.filtrar();
      })
      .catch(err => {
        console.error('❌ Error cargando datos financieros:', err);
        this.error = 'Error al cargar los datos financieros.';
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

  // 🔹 Registros paginados
  get paginadas(): (DatosPersonales & { financiero?: Financiero | null })[] {
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

  gestionar(persona: DatosPersonales, financiero?: Financiero | null): void {
    if (financiero?.idFinanciero) {
      this.router.navigate(['/hoja-vida/financieros', financiero.idFinanciero, 'editar']);
    } else {
      this.router.navigate(['/hoja-vida/financieros/nuevo'], {
        queryParams: { idDatosPersonal: persona.idDatosPersonal }
      });
    }
  }

  imprimir(): void {
    const datos = this.personas.map(p => ({
      ...p.financiero,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.printService.imprimir(
      datos.filter(f => !!f) as unknown as (Financiero & { documento?: string; nombrePersona?: string })[]
    );
  }

  exportar(): void {
    const datos = this.personas.map(p => ({
      ...p.financiero,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.exporter.exportarExcel(
      datos.filter(f => !!f) as unknown as (Financiero & { documento?: string; nombrePersona?: string })[]
    );
  }
}
