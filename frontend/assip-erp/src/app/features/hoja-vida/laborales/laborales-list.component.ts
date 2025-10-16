// src/app/features/hoja-vida/laborales/laborales-list.component.ts
import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';
import { LaboralesApi } from './laborales.api';
import { LaboralesExportButtonComponent } from './laborales-export-button.component';

type PersonaItem = {
  id?: number;
  idDatosPersonales?: number;
  id_datos_personales?: number;
  idDatosPersonal?: number;
  documento?: string;
  tipo_persona?: string | number;
  tipoPersona?: string | number;
  primerNombre?: string;
  segundoNombre?: string;
  primerApellido?: string;
  segundoApellido?: string;
  nombres?: string;
  apellidos?: string;
  [k: string]: any;
};

@Component({
  standalone: true,
  selector: 'app-laborales-list',
  imports: [CommonModule, RouterModule, LaboralesExportButtonComponent],
  templateUrl: './laborales-list.component.html',
  styleUrls: ['./laborales-list.component.scss'],
})
export class LaboralesListComponent implements OnInit {
  private personasApi = inject(DatosPersonalesApi);
  private laboralesApi = inject(LaboralesApi);
  private router = inject(Router);

  loading = signal<boolean>(true);
  error = signal<string | null>(null);

  personas = signal<PersonaItem[]>([]);
  term = signal<string>('');
  filteredPersonas = computed(() => {
    const t = this.term().trim().toLowerCase();
    const base = this.personas();
    if (!t) return base;
    return base.filter(p => {
      const full =
        `${p.nombres ?? ''} ${p.apellidos ?? ''} ${p.primerNombre ?? ''} ${p.segundoNombre ?? ''} ${p.primerApellido ?? ''} ${p.segundoApellido ?? ''}`
          .replace(/\s+/g, ' ')
          .trim()
          .toLowerCase();
      return String(p.documento ?? '').toLowerCase().includes(t) || full.includes(t);
    });
  });

  // Estado de si la persona tiene registro laboral (yes/no) o si está cargando
  refEstado = signal<Record<number, 'loading' | 'yes' | 'no'>>({});

  ngOnInit(): void {
    this.loadPersonas();
  }

  onSearch(ev: Event): void {
    const v = (ev.target as HTMLInputElement)?.value ?? '';
    this.term.set(v);
  }

  private loadPersonas(): void {
    this.loading.set(true);
    this.error.set(null);

    this.personasApi.list({ sort: 'idDatosPersonal,desc', size: 100 }).subscribe({
      next: (data: any) => {
        const arr: PersonaItem[] = Array.isArray(data) ? data : (data?.items ?? data?.content ?? []);
        // Filtra solo personas naturales (tipoPersona != '2')
        const naturales = (arr ?? []).filter(p => String(p?.tipo_persona ?? p?.tipoPersona ?? '').trim() !== '2');
        this.personas.set(naturales);
        this.loading.set(false);
        this.chequearRefParaLista();
      },
      error: () => {
        this.error.set('No fue posible cargar el listado de personas.');
        this.loading.set(false);
      },
    });
  }

  private async chequearRefParaLista(): Promise<void> {
    // Limita verificaciones para no saturar
    const personas = this.personas().slice(0, 60);
    const estado = { ...this.refEstado() };

    for (const p of personas) {
      const id = this.personaId(p);
      if (!id) continue;

      estado[id] = 'loading';
      this.refEstado.set({ ...estado });

      try {
        const row = await new Promise<any | null>((resolve) => {
          this.laboralesApi.getByPersona(id).subscribe({
            next: (resp: any) => resolve(resp ?? null),
            error: () => resolve(null), // gracias al catchError del API casi no entra aquí
          });
        });
        estado[id] = row ? 'yes' : 'no';
      } catch {
        estado[id] = 'no';
      }
      this.refEstado.set({ ...estado });
      await new Promise(r => setTimeout(r, 10));
    }
  }

  personaId(p: PersonaItem): number {
    const candidates = [p?.id, p?.idDatosPersonales, p?.id_datos_personales, p?.idDatosPersonal] as const;
    const found = candidates.find(v => typeof v === 'number' && !Number.isNaN(v) && v > 0);
    return (found ?? 0) as number;
  }

  estadoPersona(id: number | null | undefined): 'loading' | 'yes' | 'no' | null {
    if (!id) return null;
    return this.refEstado()[id] ?? null;
  }

  // Navega al upsert explícito con /edit (evita ambigüedad)
  // Si quieres crear, pasa { queryParams: { create: 1 } } desde la plantilla/botón "Nuevo"
  gestionar(p: PersonaItem, isNew: boolean) {
    const id = this.personaId(p);
    if (!id) return alert('No se encontró el identificador de la persona.');
    this.router.navigate(
      ['/hoja-vida/laborales', id, 'edit'],
      { queryParams: isNew ? { create: 1 } : undefined }
    );
  }
}
