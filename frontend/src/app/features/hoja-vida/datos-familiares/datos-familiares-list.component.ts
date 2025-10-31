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

/**
 * 👨‍👩‍👧‍👦 COMPONENTE: Listado de Datos Familiares
 * ------------------------------------------------------------
 * Muestra las personas registradas junto con los datos familiares
 * asociados (nombre, parentesco, contacto, referencia, etc.).
 *
 * Permite:
 *  - Listar personas con sus familiares asociados.
 *  - Crear o editar registros familiares.
 *  - Filtrar por documento o nombre.
 *  - Exportar a Excel.
 *  - Imprimir reporte familiar.
 */
@Component({
  selector: 'app-datos-familiares-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './datos-familiares-list.component.html',
  styleUrls: ['./datos-familiares-list.component.scss']
})
export class DatosFamiliaresListComponent implements OnInit {

  // ============================================================
  // ⚙️ Inyección de dependencias
  // ============================================================
  private readonly dpApi = inject(DatosPersonalesApi);
  private readonly famApi = inject(DatosFamiliaresApi);
  private readonly router = inject(Router);
  private readonly printService = inject(DatosFamiliaresPrintService);
  private readonly exporter = inject(DatosFamiliaresExporterService);

  // ============================================================
  // 📦 Propiedades del componente
  // ============================================================
  personas: (DatosPersonales & { familiares?: DatosFamiliar[] | null })[] = [];
  filtradas: (DatosPersonales & { familiares?: DatosFamiliar[] | null })[] = [];

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
  // 🔄 Cargar datos personales y familiares
  // ============================================================
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
            const fa = a.fechaEdicion ? new Date(a.fechaEdicion).getTime() : 0;
            const fb = b.fechaEdicion ? new Date(b.fechaEdicion).getTime() : 0;
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
  // 🟢 Crear o editar registro familiar
  // ============================================================
  gestionar(persona: DatosPersonales, familiar?: DatosFamiliar | null): void {
    if (familiar?.idDatosFamiliares) {
      this.router.navigate(['/hoja-vida/datos-familiares', familiar.idDatosFamiliares, 'editar']);
    } else {
      this.router.navigate(['/hoja-vida/datos-familiares/nuevo'], {
        queryParams: { idDatosPersonal: persona.idDatosPersonal }
      });
    }
  }

  // ============================================================
  // 🖨️ Imprimir reporte familiar
  // ============================================================
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

  // ============================================================
  // 📤 Exportar a Excel
  // ============================================================
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
