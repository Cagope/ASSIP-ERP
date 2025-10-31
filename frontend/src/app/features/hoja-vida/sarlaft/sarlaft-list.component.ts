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

/**
 * 🧾 COMPONENTE: Listado SARLAFT
 * ------------------------------------------------------------
 * Combina la información personal con los registros SARLAFT asociados.
 * Permite:
 *  - Listar personas con su información SARLAFT.
 *  - Crear o editar registros SARLAFT.
 *  - Filtrar por documento o nombre.
 *  - Exportar a Excel.
 *  - Imprimir listado SARLAFT.
 */
@Component({
  selector: 'app-sarlaft-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './sarlaft-list.component.html',
  styleUrls: ['./sarlaft-list.component.scss']
})
export class SarlaftListComponent implements OnInit {

  // ============================================================
  // ⚙️ Inyección de dependencias
  // ============================================================
  private readonly dpApi = inject(DatosPersonalesApi);
  private readonly sarlaftApi = inject(SarlaftApi);
  private readonly router = inject(Router);
  private readonly printService = inject(SarlaftPrintService);
  private readonly exporter = inject(SarlaftExporterService);

  // ============================================================
  // 📦 Propiedades del componente
  // ============================================================
  personas: (DatosPersonales & { sarlaft?: Sarlaft | null })[] = [];
  filtradas: (DatosPersonales & { sarlaft?: Sarlaft | null })[] = [];

  cargando = false;
  filtro = '';
  error = '';

  // ============================================================
  // 🚀 Inicialización
  // ============================================================
  ngOnInit(): void {
    this.cargar();
  }

  // ============================================================
  // 🔄 Cargar datos personales + SARLAFT en paralelo
  // ============================================================
  cargar(): void {
    this.cargando = true;
    this.error = '';

    Promise.all([
      this.dpApi.listar().toPromise(),
      this.sarlaftApi.listar().toPromise()
    ])
      .then(([personas, sarlafts]) => {
        // Crear mapa de SARLAFT por ID de persona
        const mapaSarlaft = new Map<number, Sarlaft>();
        (sarlafts ?? []).forEach(s => {
          if (s.idDatosPersonal) mapaSarlaft.set(s.idDatosPersonal, s);
        });

        // Unir datos personales con SARLAFT
        this.personas = (personas ?? [])
          .sort((a, b) => {
            const fa = a.fechaEdicion ? new Date(a.fechaEdicion).getTime() : 0;
            const fb = b.fechaEdicion ? new Date(b.fechaEdicion).getTime() : 0;
            return fb - fa;
          })
          .map(p => ({
            ...p,
            sarlaft: p.idDatosPersonal
              ? mapaSarlaft.get(p.idDatosPersonal) ?? null
              : null
          }));

        this.filtrar();
      })
      .catch(err => {
        console.error('❌ Error cargando datos SARLAFT:', err);
        this.error = 'Error al cargar los datos SARLAFT.';
      })
      .finally(() => (this.cargando = false));
  }

  // ============================================================
  // 🔍 Filtro por documento o nombre
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
  // 🟢 Crear o editar registro SARLAFT
  // ============================================================
  gestionar(persona: DatosPersonales, sarlaft?: Sarlaft | null): void {
    if (sarlaft?.idSarlaft) {
      // Editar registro existente
      this.router.navigate(['/hoja-vida/sarlaft', sarlaft.idSarlaft, 'editar']);
    } else {
      // Crear nuevo registro SARLAFT para esta persona
      this.router.navigate(['/hoja-vida/sarlaft/nuevo'], {
        queryParams: { idDatosPersonal: persona.idDatosPersonal }
      });
    }
  }

  // ============================================================
  // 🖨️ Imprimir listado SARLAFT
  // ============================================================
  imprimir(): void {
    const datos = this.personas.map(p => ({
      ...p.sarlaft,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.printService.imprimir(
      datos.filter(s => !!s) as unknown as (
        Sarlaft & {
          documento?: string;
          nombrePersona?: string;
        }
      )[]
    );
  }

  // ============================================================
  // 📤 Exportar listado SARLAFT a Excel
  // ============================================================
  exportar(): void {
    const datos = this.personas.map(p => ({
      ...p.sarlaft,
      documento: p.documento,
      nombrePersona: `${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
    }));

    this.exporter.exportarExcel(
      datos.filter(s => !!s) as unknown as (
        Sarlaft & {
          documento?: string;
          nombrePersona?: string;
        }
      )[]
    );
  }
}
