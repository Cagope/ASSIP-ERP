import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';
import { UbicacionesApi, UbicacionResponse } from './ubicaciones.api';
import { ReferenciasPersonalesExportButtonComponent } from '../referencias-personales/referencias-personales-export-button.component'; // Reusamos patrón de botón export

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
  selector: 'app-ubicaciones-list',
  imports: [CommonModule, RouterLink, ReferenciasPersonalesExportButtonComponent],
  templateUrl: './ubicaciones-list.component.html',
  styleUrls: ['./ubicaciones-list.component.scss'],
})
export class UbicacionesListComponent implements OnInit {
  private personasApi = inject(DatosPersonalesApi);
  private api = inject(UbicacionesApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  idDatosPersonales = signal<number | null>(null);

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
        `${p.nombres ?? ''} ${p.apellidos ?? ''} ${p.primerNombre ?? ''} ${p.segundoNombre ?? ''} ${p.primerApellido ?? ''} ${p.segundoApellido ?? ''}`.toLowerCase();
      return String(p.documento ?? '').toLowerCase().includes(t) || full.includes(t);
    });
  });

  refEstado = signal<Record<number, 'loading' | 'yes' | 'no'>>({});
  item = signal<UbicacionResponse | null>(null);
  afiliadoNombre = signal<string>('');

  ngOnInit(): void {
    this.route.paramMap.subscribe(pm => {
      const idStr = pm.get('idDatosPersonales');
      const id = idStr ? Number(idStr) : null;
      this.applyRouteId(id);
    });

    if (!this.route.snapshot.paramMap.get('idDatosPersonales')) {
      this.applyRouteId(null);
    }
  }

  private applyRouteId(id: number | null): void {
    this.idDatosPersonales.set(id);
    this.error.set(null);

    if (id) {
      this.loadAfiliadoNombre(id);
      this.loadUbicacion(id);
    } else {
      this.loadPersonas();
    }
  }

  onSearch(ev: Event): void {
    const v = (ev.target as HTMLInputElement)?.value ?? '';
    this.term.set(v);
  }

  // Personas
  loadPersonas(): void {
    this.loading.set(true);
    this.error.set(null);

    this.personasApi.list({ sort: 'idDatosPersonal,desc', size: 100 }).subscribe({
      next: (data: any) => {
        const arr: PersonaItem[] = Array.isArray(data) ? data : (data?.items ?? data?.content ?? []);
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

  /** Chequeo 1:1 (secuencial/acotado) */
  private async chequearRefParaLista(): Promise<void> {
    const personas = this.personas().slice(0, 60);
    const estado = { ...this.refEstado() };

    for (const p of personas) {
      const id = this.personaId(p);
      if (!id) continue;

      estado[id] = 'loading';
      this.refEstado.set({ ...estado });

      try {
        const row = await new Promise<UbicacionResponse | null>((resolve) => {
          this.api.getByPersona(id).subscribe({
            next: (resp) => resolve(resp.status === 204 ? null : (resp.body ?? null)),
            error: () => resolve(null),
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

  // Navegaciones
  verUbicacion(p: PersonaItem): void {
    const id = this.personaId(p);
    if (!id) return alert('No se encontró el identificador de la persona.');
    this.router.navigate(['/hoja-vida/ubicaciones', id]);
  }

  nuevaUbicacion(p: PersonaItem): void {
    const id = this.personaId(p);
    if (!id) return alert('No se encontró el identificador de la persona.');
    this.router.navigate(['/hoja-vida/ubicaciones', id, 'new']);
  }

  // Detalle 1:1
  loadUbicacion(idDatos: number): void {
    this.loading.set(true);
    this.error.set(null);

    this.api.getByPersona(idDatos).subscribe({
      next: (resp) => {
        this.item.set(resp.status === 204 ? null : (resp.body ?? null));
        this.loading.set(false);
      },
      error: () => {
        this.item.set(null);
        this.error.set('No fue posible cargar la ubicación.');
        this.loading.set(false);
      },
    });
  }

  // Afiliado
  private loadAfiliadoNombre(idDatos: number): void {
    const maybeGet = (this.personasApi as any)?.get;
    if (typeof maybeGet === 'function') {
      (maybeGet as Function).call(this.personasApi, idDatos).subscribe({
        next: (p: any) => this.afiliadoNombre.set(this.buildNombre(p)),
        error: () => this.fetchNombreFromList(idDatos),
      });
    } else {
      this.fetchNombreFromList(idDatos);
    }
  }

  private fetchNombreFromList(idDatos: number): void {
    this.personasApi.list({ size: 1000 }).subscribe({
      next: (data: any) => {
        const arr: any[] = Array.isArray(data) ? data : (data?.items ?? data?.content ?? []);
        const p = (arr ?? []).find((it: any) => {
          const ids = [it?.id, it?.idDatosPersonal, it?.id_datos_personales, it?.idDatosPersonales];
          return ids.some((v: any) => Number(v) === idDatos);
        });
        this.afiliadoNombre.set(this.buildNombre(p));
      },
      error: () => this.afiliadoNombre.set(''),
    });
  }

  private buildNombre(p: any): string {
    if (!p) return '';
    const nombres = p.nombres ?? [p.primerNombre, p.segundoNombre].filter(Boolean).join(' ').trim();
    const apellidos = p.apellidos ?? [p.primerApellido, p.segundoApellido].filter(Boolean).join(' ').trim();
    return [nombres, apellidos].filter(Boolean).join(' ').trim();
  }

  // Acciones en detalle
  idUbicacion(): number | null {
    return this.item()?.id_ubicacion ?? null;
  }

  onDelete(): void {
    const idPer = this.idDatosPersonales();
    const idUb = this.idUbicacion();
    if (!idPer || !idUb) return;
    if (!confirm('¿Eliminar la referencia de ubicación?')) return;

    this.api.deleteForPersona(idPer, idUb).subscribe({
      next: () => this.loadUbicacion(idPer),
      error: () => alert('No fue posible eliminar el registro.'),
    });
  }

  goBackToSelector(): void {
    this.router.navigate(['/hoja-vida/ubicaciones']);
  }
}
