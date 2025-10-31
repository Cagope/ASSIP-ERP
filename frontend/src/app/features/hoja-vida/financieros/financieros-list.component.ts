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

/**
 * 💰 COMPONENTE: Listado de Información Financiera
 * ------------------------------------------------------------
 * Combina la información personal con los datos financieros asociados
 * a cada persona.
 */
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
          if (f.idDatosPersonal) mapaFinancieros.set(f.idDatosPersonal, f); // ✅ plural (modelo Financiero)
        });

        this.personas = (personas ?? [])
          .sort((a, b) => {
            const fa = a.fechaActualizacion ? new Date(a.fechaActualizacion).getTime() : 0;
            const fb = b.fechaActualizacion ? new Date(b.fechaActualizacion).getTime() : 0;
            return fb - fa;
          })
          .map(p => ({
            ...p,
            financiero: p.idDatosPersonal // ✅ singular (modelo DatosPersonales)
              ? mapaFinancieros.get(p.idDatosPersonal) ?? null
              : null
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
  }

  gestionar(persona: DatosPersonales, financiero?: Financiero | null): void {
    if (financiero?.idFinanciero) {
      this.router.navigate(['/hoja-vida/financieros', financiero.idFinanciero, 'editar']);
    } else {
      this.router.navigate(['/hoja-vida/financieros/nuevo'], {
        queryParams: { idDatosPersonal: persona.idDatosPersonal } // ✅ singular
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
      datos.filter(f => !!f) as unknown as (
        Financiero & { documento?: string; nombrePersona?: string }
      )[]
    );
  }

  exportar(): void {
    const datos = this.personas.map(p => ({
      ...p.financiero,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.exporter.exportarExcel(
      datos.filter(f => !!f) as unknown as (
        Financiero & { documento?: string; nombrePersona?: string }
      )[]
    );
  }
}
