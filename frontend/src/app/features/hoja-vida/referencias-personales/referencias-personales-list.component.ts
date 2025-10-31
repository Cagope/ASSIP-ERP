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

/**
 * 👥 COMPONENTE: Listado de Referencias Personales
 * ------------------------------------------------------------
 * Combina los datos personales con las referencias personales
 * asociadas a cada persona.
 *
 * Permite:
 *  - Listar todas las personas con sus referencias personales.
 *  - Crear o editar referencias personales.
 *  - Filtrar por documento o nombre.
 *  - Exportar a Excel.
 *  - Imprimir el listado completo.
 */
@Component({
  selector: 'app-referencias-personales-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './referencias-personales-list.component.html',
  styleUrls: ['./referencias-personales-list.component.scss']
})
export class ReferenciasPersonalesListComponent implements OnInit {

  // ============================================================
  // ⚙️ Inyección de dependencias
  // ============================================================
  private readonly dpApi = inject(DatosPersonalesApi);
  private readonly refApi = inject(ReferenciasPersonalesApi);
  private readonly router = inject(Router);
  private readonly printService = inject(ReferenciasPersonalesPrintService);
  private readonly exporter = inject(ReferenciasPersonalesExporterService);

  // ============================================================
  // 📦 Propiedades del componente
  // ============================================================
  personas: (DatosPersonales & { referencias?: ReferenciaPersonal[] | null })[] = [];
  filtradas: (DatosPersonales & { referencias?: ReferenciaPersonal[] | null })[] = [];

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
  // 🔄 Cargar datos personales + referencias personales
  // ============================================================
  cargar(): void {
    this.cargando = true;
    this.error = '';

    Promise.all([
      this.dpApi.listar().toPromise(),
      this.refApi.listar().toPromise()
    ])
      .then(([personas, referencias]) => {
        // Mapa de referencias por ID de persona
        const mapaReferencias = new Map<number, ReferenciaPersonal[]>();
        (referencias ?? []).forEach(r => {
          if (r.idDatosPersonal != null) {
            const lista = mapaReferencias.get(r.idDatosPersonal) ?? [];
            lista.push(r);
            mapaReferencias.set(r.idDatosPersonal, lista);
          }
        });

        // Unir personas + referencias, ordenadas por fecha de edición
        this.personas = (personas ?? [])
          .sort((a, b) => {
            const fa = a.fechaEdicion ? new Date(a.fechaEdicion).getTime() : 0;
            const fb = b.fechaEdicion ? new Date(b.fechaEdicion).getTime() : 0;
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

  // ============================================================
  // 🔍 Filtrar por documento o nombre
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
  // 🟢 Crear o editar referencia personal
  // ============================================================
  gestionar(persona: DatosPersonales, referencia?: ReferenciaPersonal | null): void {
    if (referencia?.idReferenciaPersonal) {
      this.router.navigate(['/hoja-vida/referencias-personales', referencia.idReferenciaPersonal, 'editar']);
    } else {
      this.router.navigate(['/hoja-vida/referencias-personales/nuevo'], {
        queryParams: { idDatosPersonal: persona.idDatosPersonal }
      });
    }
  }

  // ============================================================
  // 🖨️ Imprimir listado
  // ============================================================
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

  // ============================================================
  // 📤 Exportar listado a Excel
  // ============================================================
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
