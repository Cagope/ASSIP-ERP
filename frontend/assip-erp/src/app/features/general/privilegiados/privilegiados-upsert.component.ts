import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { map } from 'rxjs/operators';

import { PrivilegiadosApi, PrivilegiadoCreateUpdate, PrivilegiadoDetailDTO } from './privilegiados.api';
import { DirectivosApi, DirectivoListItemDTO } from '../directivos/directivos.api';
import { DatosPersonalesApi } from '../../hoja-vida/datos-personales/datos-personales.api';
import { CatalogosApi, CodigoNombreDTO } from '../../../shared/catalogos/catalogos.api';

// ===== Tipos locales para los buscadores =====
type PersonaListItem = {
  idDatosPersonal: number;
  documento: string;
  nombres: string;
  primerApellido: string;
  segundoApellido: string;
  nombreDisplay?: string;
  tipoPersona?: string; // "1" natural, "2" jurídica
};

type DirectivoListItemLite = {
  idDirectivo: number;
  idDatosPersonal: number;
  documento: string;
  nombrePersona: string;
};

@Component({
  standalone: true,
  selector: 'app-privilegiados-upsert',
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './privilegiados-upsert.component.html',
  styleUrls: ['./privilegiados-upsert.component.scss']
})
export class PrivilegiadosUpsertComponent implements OnInit {
  // Inyecciones
  private fb = inject(FormBuilder);
  private api = inject(PrivilegiadosApi);
  private directivosApi = inject(DirectivosApi);
  private personasApi = inject(DatosPersonalesApi);
  private catalogos = inject(CatalogosApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  // Estado general
  loading = signal(false);
  guardando = signal(false);
  error = signal<string | null>(null);
  id = signal<number | null>(null);

  titulo = computed(() => this.id() ? 'Editar privilegiado' : 'Nuevo privilegiado');

  // Catálogo de parentescos
  parentescos = signal<CodigoNombreDTO[]>([]);

  // ===== Form =====
  form = this.fb.group({
    idDirectivo: [null as number | null, [Validators.required, Validators.min(1)]],
    idDatosPersonal: [null as number | null, [Validators.required, Validators.min(1)]],
    // código parentesco: normalmente 1–2 chars alfanuméricos
    codigoParentesco: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9]{1,2}$/)]],
  });
  get f() { return this.form.controls; }

  // ===== Buscador de DIRECTIVO =====
  buscarDir = signal<string>('');
  directivos = signal<DirectivoListItemLite[]>([]);
  buscandoDir = signal(false);
  activeDirIndex = signal(0);
  selectedDirectivo = signal<DirectivoListItemLite | null>(null);

  // ===== Buscador de PERSONA (privilegiada) =====
  buscarPer = signal<string>('');
  personas = signal<PersonaListItem[]>([]);
  buscandoPer = signal(false);
  activePerIndex = signal(0);
  selectedPersona = signal<PersonaListItem | null>(null);

  // ===== RÓTULOS (computed) =====
  personaLabel = computed(() => {
    const p = this.selectedPersona();
    if (!p) return '';
    const nombre = (p.nombreDisplay || [p.nombres, p.primerApellido, p.segundoApellido].filter(Boolean).join(' ')).trim();
    const doc = (p.documento || '').trim();
    return [doc, nombre].filter(Boolean).join(' — ');
  });

  directivoLabel = computed(() => {
    const d = this.selectedDirectivo();
    if (!d) return '';
    const doc = (d.documento || '').trim();
    const nom = (d.nombrePersona || '').trim();
    return [doc, nom].filter(Boolean).join(' — ');
  });

  // ==== UI helpers ====
  setActiveDir(i: number) { this.activeDirIndex.set(i); }
  setActivePer(i: number) { this.activePerIndex.set(i); }

  // Nombre display robusto para Persona
  nombreDisplayPersona = (p: any) => {
    const display = p?.nombreDisplay
      ?? p?.nombrePersona
      ?? p?.nombre_completo
      ?? p?.nombreCompleto
      ?? [p?.nombres, p?.primerApellido, p?.segundoApellido].filter(Boolean).join(' ');
    return (display || '').trim();
  };

  // Resaltado simple
  hl(q: string, text: string | null | undefined) {
    const query = (q || '').trim();
    const t = (text || '').toString();
    if (!query) return t;
    try {
      const safeQ = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
      const re = new RegExp(safeQ, 'ig');
      return t.replace(re, (m) => `<mark>${m}</mark>`);
    } catch {
      return t;
    }
  }

  // Normaliza persona
  private normalizePersona(r: any): PersonaListItem {
    const id = Number(r?.idDatosPersonal ?? r?.id ?? r?.id_datos_personal ?? 0);
    const doc = (r?.documento ?? '').toString();
    const nombrePersona = r?.nombrePersona ?? r?.nombre_completo ?? r?.nombreCompleto;
    const tipoPersona = r?.tipoPersona ?? r?.tipo_persona ?? undefined;

    if (nombrePersona) {
      const display = String(nombrePersona).trim();
      const partes = display.split(' ').filter(Boolean);
      const nombres = r?.nombres ? String(r.nombres).trim()
                   : (partes.slice(0, Math.max(1, partes.length - 2)).join(' ') || display);
      const primerApellido = r?.primerApellido ? String(r.primerApellido).trim()
                          : (partes.length > 1 ? partes[partes.length - 2] : '');
      const segundoApellido = r?.segundoApellido ? String(r.segundoApellido).trim()
                           : (partes.length > 2 ? partes[partes.length - 1] : '');
      return { idDatosPersonal: id, documento: doc, nombres, primerApellido, segundoApellido, nombreDisplay: display, tipoPersona };
    }

    const nombres = (r?.nombres ?? '').toString().trim();
    const a1 = (r?.primerApellido ?? '').toString().trim();
    const a2 = (r?.segundoApellido ?? '').toString().trim();
    const display = [nombres, a1, a2].filter(Boolean).join(' ').trim();

    return { idDatosPersonal: id, documento: doc, nombres, primerApellido: a1, segundoApellido: a2, nombreDisplay: display, tipoPersona };
  }

  // Normaliza directivo
  private normalizeDirectivo(d: DirectivoListItemDTO): DirectivoListItemLite {
    return {
      idDirectivo: d.idDirectivo,
      idDatosPersonal: d.idDatosPersonal,
      documento: d.documento,
      nombrePersona: d.nombrePersona,
    };
  }

  // ==== Eventos teclado: DIRECTIVO ====
  onSearchDirKeydown(ev: KeyboardEvent) {
    if (!this.directivos().length) return;
    const max = this.directivos().length - 1;
    const i = this.activeDirIndex();

    if (ev.key === 'ArrowDown') {
      ev.preventDefault();
      this.activeDirIndex.set(i >= max ? 0 : i + 1);
    } else if (ev.key === 'ArrowUp') {
      ev.preventDefault();
      this.activeDirIndex.set(i <= 0 ? max : i - 1);
    } else if (ev.key === 'Enter') {
      ev.preventDefault();
      const arr = this.directivos();
      if (arr.length) this.elegirDirectivo(arr[this.activeDirIndex()]);
    }
  }

  // ==== Eventos teclado: PERSONA ====
  onSearchPerKeydown(ev: KeyboardEvent) {
    if (!this.personas().length) return;
    const max = this.personas().length - 1;
    const i = this.activePerIndex();

    if (ev.key === 'ArrowDown') {
      ev.preventDefault();
      this.activePerIndex.set(i >= max ? 0 : i + 1);
    } else if (ev.key === 'ArrowUp') {
      ev.preventDefault();
      this.activePerIndex.set(i <= 0 ? max : i - 1);
    } else if (ev.key === 'Enter') {
      ev.preventDefault();
      const arr = this.personas();
      if (arr.length) this.elegirPersona(arr[this.activePerIndex()]);
    }
  }

  // ==== INIT ====
  ngOnInit() {
    // Cargar catálogo parentescos
    this.catalogos.parentescos().subscribe({
      next: rows => this.parentescos.set(rows ?? []),
      error: () => this.parentescos.set([])
    });

    // Si viene id -> cargar detalle
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam && idParam !== 'new') {
      const id = Number(idParam);
      if (!Number.isNaN(id)) {
        this.id.set(id);
        this.load(id);
      }
    }
  }

  // Carga detalle para editar
  load(id: number) {
    this.loading.set(true);
    this.api.get(id).subscribe({
      next: (dto: PrivilegiadoDetailDTO) => {
        this.form.patchValue({
          idDirectivo: dto.idDirectivo,
          idDatosPersonal: dto.idDatosPersonal,
          codigoParentesco: dto.codigoParentesco,
        });

        // Persona: llenar rótulo de inmediato con lo que trae el DTO
        if (dto.idDatosPersonal) {
          this.selectedPersona.set(this.normalizePersona({
            idDatosPersonal: dto.idDatosPersonal,
            documento: dto.documentoPersona ?? '',
            nombrePersona: dto.nombrePersona ?? ''
          }));
          // Refrescar datos de persona (opcional, por si quieres consistencia)
          this.loadPersonaInfo(dto.idDatosPersonal);
        }

        // Directivo: si el DTO no trae doc/nombre, resolver por búsqueda con id
        if (dto.idDirectivo) {
          this.resolveDirectivo(dto.idDirectivo);
        }

        this.loading.set(false);
      },
      error: (err: any) => {
        this.error.set(err?.error?.message ?? 'No se pudo cargar el privilegiado');
        this.loading.set(false);
      }
    });
  }

  // ====== Buscar DIRECTIVO ======
  buscarDirectivo() {
    const q = this.buscarDir().trim();
    if (!q) { this.directivos.set([]); return; }
    this.buscandoDir.set(true);
    this.directivosApi.list(q).subscribe({
      next: (res: any) => {
        const rows = Array.isArray(res) ? res : (res?.content ?? []);
        const lite = (rows as DirectivoListItemDTO[]).map(d => this.normalizeDirectivo(d));
        this.directivos.set(lite);
        this.activeDirIndex.set(0);
        this.buscandoDir.set(false);
      },
      error: () => { this.directivos.set([]); this.buscandoDir.set(false); }
    });
  }

  elegirDirectivo(d: DirectivoListItemLite) {
    this.selectedDirectivo.set(d);
    this.form.patchValue({ idDirectivo: d.idDirectivo });
    this.directivos.set([]);
    this.buscarDir.set('');
    this.activeDirIndex.set(0);
  }

  private resolveDirectivo(idDirectivo: number) {
    // Búsqueda simple por id como texto
    this.directivosApi.list(String(idDirectivo)).subscribe({
      next: (res: any) => {
        const rows = Array.isArray(res) ? res : (res?.content ?? []);
        const found = (rows as DirectivoListItemDTO[]).find(r => r.idDirectivo === idDirectivo);
        if (found) this.selectedDirectivo.set(this.normalizeDirectivo(found));
      },
      error: () => { /* opcional */ }
    });
  }

  // ====== Buscar PERSONA ======
  buscarPersona() {
    const q = this.buscarPer().trim();
    if (!q) { this.personas.set([]); return; }
    this.buscandoPer.set(true);
    this.personasApi.list({ q, size: 10 }).subscribe({
      next: (res: any) => {
        const rows = Array.isArray(res) ? res : res?.content ?? [];
        this.personas.set(rows.map((r: any) => this.normalizePersona(r)));
        this.activePerIndex.set(0);
        this.buscandoPer.set(false);
      },
      error: () => { this.personas.set([]); this.buscandoPer.set(false); }
    });
  }

  /** Carga datos por ID (para mostrar doc + nombre en UI si entramos a editar). */
  private loadPersonaInfo(idPersona: number) {
    if (!idPersona) return;

    const apiAny = this.personasApi as any;
    if (apiAny.get && typeof apiAny.get === 'function') {
      apiAny.get(idPersona).subscribe({
        next: (r: any) => {
          const p = this.normalizePersona(r);
          this.selectedPersona.set(p);
        },
        error: () => { /* opcional */ }
      });
      return;
    }

    // Fallback vía list(q = id)
    this.personasApi.list({ q: String(idPersona), size: 10 }).subscribe({
      next: (res: any) => {
        const rows = (Array.isArray(res) ? res : res?.content ?? []) as any[];
        const found = rows.find(rr => Number(rr?.idDatosPersonal ?? rr?.id ?? rr?.id_datos_personal) === Number(idPersona));
        if (found) {
          const p = this.normalizePersona(found);
          this.selectedPersona.set(p);
        }
      },
      error: () => { /* opcional */ }
    });
  }

  elegirPersona(p: PersonaListItem) {
    const norm = this.normalizePersona(p);
    this.selectedPersona.set(norm);
    this.form.patchValue({ idDatosPersonal: norm.idDatosPersonal });
    this.personas.set([]);
    this.buscarPer.set('');
    this.activePerIndex.set(0);
  }

  // ====== Submit ======
  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    const raw = this.form.getRawValue();
    const payload: PrivilegiadoCreateUpdate = {
      idDirectivo: raw.idDirectivo!,
      idDatosPersonal: raw.idDatosPersonal!,
      codigoParentesco: raw.codigoParentesco!,
    };

    this.guardando.set(true);
    this.error.set(null);

    const obs = this.id()
      ? this.api.update(this.id()!, payload).pipe(map(() => void 0))
      : this.api.create(payload).pipe(map(() => void 0));

    obs.subscribe({
      next: () => {
        this.guardando.set(false);
        // Volver al listado conservando idDirectivo para que no falle la lista
        this.router.navigate(['/general/privilegiados'], {
          queryParams: { idDirectivo: payload.idDirectivo }
        });
      },
      error: (err: any) => {
        this.guardando.set(false);
        this.error.set(err?.error?.message ?? 'No se pudo guardar');
      }
    });
  }

  cancelar() {
    const idDirectivo = this.form.getRawValue().idDirectivo || undefined;
    this.router.navigate(['/general/privilegiados'], {
      queryParams: { idDirectivo: idDirectivo || null }
    });
  }
}
