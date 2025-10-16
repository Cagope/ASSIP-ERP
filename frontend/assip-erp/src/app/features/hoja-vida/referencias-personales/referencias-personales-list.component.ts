// src/app/features/hoja-vida/referencias-personales/referencias-personales-list.component.ts
import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { SessionService } from '../../../core/auth/session.service';
import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';
import { ReferenciasPersonalesApi, ReferenciaPersonal } from './referencias-personales.api';
import { ReferenciasPersonalesExportButtonComponent } from './referencias-personales-export-button.component';

// Extendemos el tipo para permitir nombre_ciudad opcional (inyectado por el front si es necesario)
type RefWithCiudad = ReferenciaPersonal & { nombre_ciudad?: string };

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
  selector: 'app-referencias-personales-list',
  imports: [CommonModule, RouterLink, ReferenciasPersonalesExportButtonComponent],
  templateUrl: './referencias-personales-list.component.html',
  styleUrls: ['./referencias-personales-list.component.scss'],
})
export class ReferenciasPersonalesListComponent implements OnInit {
  private api = inject(ReferenciasPersonalesApi);
  private personasApi = inject(DatosPersonalesApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private session = inject(SessionService);
  private http = inject(HttpClient);

  idDatosPersonales = signal<number | null>(null);

  loading = signal<boolean>(true);
  error = signal<string | null>(null);

  // Selector de personas
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

  // Estado 1:1 por persona
  refEstado = signal<Record<number, 'loading' | 'yes' | 'no'>>({});

  // Detalle (1:1) — con nombre_ciudad opcional
  item = signal<RefWithCiudad | null>(null);

  // Nombre del afiliado
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
      // Vista detalle
      this.loadAfiliadoNombre(id);
      this.loadReferencia(id);
    } else {
      // Vista selector
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

  /** Chequeo 1:1 con back (secuencial y acotado) */
  private async chequearRefParaLista(): Promise<void> {
    const personas = this.personas().slice(0, 60); // límite de seguridad
    const estado = { ...this.refEstado() };

    for (const p of personas) {
      const id = this.personaId(p);
      if (!id) continue;

      estado[id] = 'loading';
      this.refEstado.set({ ...estado });

      try {
        const row = await new Promise<ReferenciaPersonal | null>((resolve) => {
          this.api.getByPersona(id).subscribe({
            next: (r) => resolve(r ?? null),
            error: () => resolve(null),
          });
        });
        estado[id] = row ? 'yes' : 'no';
      } catch {
        estado[id] = 'no';
      }
      this.refEstado.set({ ...estado });

      // micro pausa
      await new Promise(r => setTimeout(r, 10));
    }
  }

  personaId(p: PersonaItem): number {
    const candidates = [
      (p as any)?.id,
      (p as any)?.idDatosPersonales,
      (p as any)?.id_datos_personales,
      (p as any)?.idDatosPersonal,
    ];
    const found = candidates.find(v => typeof v === 'number' && !Number.isNaN(v) && v > 0);
    return (found ?? 0) as number;
  }

  estadoPersona(id: number | null | undefined): 'loading' | 'yes' | 'no' | null {
    if (!id) return null;
    return this.refEstado()[id] ?? null;
  }

  // Navegaciones
  irReferencia(p: PersonaItem): void {
    const id = this.personaId(p);
    if (!id) {
      alert('No se encontró el identificador de la persona.');
      return;
    }
    this.router.navigate(['/hoja-vida/referencias-personales', id]);
  }

  irNuevaReferencia(p: PersonaItem): void {
    const id = this.personaId(p);
    if (!id) {
      alert('No se encontró el identificador de la persona.');
      return;
    }
    this.router.navigate(['/hoja-vida/referencias-personales', id, 'new']);
  }

  // Vista referencia (detalle 1:1)
  loadReferencia(idDatos: number): void {
    this.loading.set(true);
    this.error.set(null);

    this.api.getByPersona(idDatos).subscribe({
      next: (row: ReferenciaPersonal | null) => {
        // seteamos lo que venga
        this.item.set((row ?? null) as RefWithCiudad);
        this.loading.set(false);

        const curr = this.item();
        if (!curr) return;

        // Si YA viene nombre_ciudad desde el back, listo
        if (curr.nombre_ciudad && curr.nombre_ciudad.trim() !== '') return;

        // Tomar el id de ciudad del registro (aceptamos variantes con 'as any')
        const idCiudad: number | null =
          Number(
            (curr as any)?.id_ciudad ??
            (curr as any)?.idCiudad ??
            (curr as any)?.idCiudadResidencia ??
            0
          ) || null;

        if (!idCiudad || idCiudad <= 0) return;

        // Resolver UNA VEZ; si no se puede, dejaremos el id como texto
        this.resolveCiudadNombre(idCiudad);
      },
      error: () => {
        this.item.set(null);
        this.error.set('No fue posible cargar la referencia personal.');
        this.loading.set(false);
      },
    });
  }

  /** Intenta resolver el nombre de la ciudad; si falla, usa el ID como texto */
  private resolveCiudadNombre(idCiudad: number): void {
    const urls = [
      `/api/v1/general/ciudades/${idCiudad}`,
      `/api/v1/hoja-vida/ciudades/${idCiudad}`,
    ];

    const tryUrl = (i: number) => {
      if (i >= urls.length) {
        // Último recurso: que se vea el ID en vez de '—'
        const curr = this.item();
        if (curr) this.item.set({ ...(curr as any), nombre_ciudad: String(idCiudad) });
        return;
      }
      this.http.get<any>(urls[i]).subscribe({
        next: (r) => {
          const nombre =
            r?.nombre_ciudad ??
            r?.nombreCiudad ??
            r?.descripcion_ciudad ??
            r?.descripcionCiudad ??
            r?.nombre ??
            r?.descripcion ??
            (Array.isArray(r) && r[0]
              ? (r[0].nombre_ciudad ?? r[0].nombre ?? r[0].descripcion)
              : null);

          const curr = this.item();
          if (!curr) return;

          if (nombre) {
            this.item.set({ ...(curr as any), nombre_ciudad: String(nombre) });
          } else {
            tryUrl(i + 1);
          }
        },
        error: () => tryUrl(i + 1),
      });
    };

    tryUrl(0);
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
    const nombres =
      p.nombres ??
      [p.primerNombre, p.segundoNombre].filter(Boolean).join(' ').trim();
    const apellidos =
      p.apellidos ??
      [p.primerApellido, p.segundoApellido].filter(Boolean).join(' ').trim();
    return [nombres, apellidos].filter(Boolean).join(' ').trim();
  }

  // Regresar
  goBackToSelector(): void {
    this.router.navigate(['/hoja-vida/referencias-personales']);
  }

  // Acciones en detalle
  idRef(): number | null {
    return this.item()?.id_referencia_personal ?? null;
  }

  onDelete(): void {
    const idPer = this.idDatosPersonales();
    const idRef = this.idRef();
    if (!idPer || !idRef) return;
    if (!confirm('¿Eliminar la referencia personal?')) return;

    this.api.deleteForPersona(idPer, idRef).subscribe({
      next: () => this.loadReferencia(idPer),
      error: () => alert('No fue posible eliminar el registro.'),
    });
  }
}
