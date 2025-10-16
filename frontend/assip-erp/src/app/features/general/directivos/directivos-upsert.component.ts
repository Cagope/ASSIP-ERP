import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormControl } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DirectivosApi, DirectivoCreateUpdate, DirectivoDetailDTO } from './directivos.api';
import { map } from 'rxjs/operators';
import { DatosPersonalesApi } from '../../hoja-vida/datos-personales/datos-personales.api';
import { CatalogosApi, CodigoNombreDTO } from '../../../shared/catalogos/catalogos.api';

type PersonaListItem = {
  idDatosPersonal: number;
  documento: string;
  nombres: string;
  primerApellido: string;
  segundoApellido: string;
  nombreDisplay?: string;
  // opcional si viene del backend
  tipoPersona?: string; // "1" natural, "2" jurídica
};

@Component({
  standalone: true,
  selector: 'app-directivos-upsert',
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './directivos-upsert.component.html',
  styleUrls: ['./directivos-upsert.component.scss']
})
export class DirectivosUpsertComponent implements OnInit {
  private fb = inject(FormBuilder);
  private api = inject(DirectivosApi);
  private personasApi = inject(DatosPersonalesApi);
  private catalogos = inject(CatalogosApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  loading = signal(false);
  guardando = signal(false);
  error = signal<string | null>(null);
  id = signal<number | null>(null);

  titulo = computed(() => this.id() ? 'Editar directivo' : 'Nuevo directivo');

  // Catálogo tipos de directivo
  tiposDirectivos = signal<CodigoNombreDTO[]>([]);

  // Form
  form = this.fb.group({
    idDatosPersonal: [null as number | null, [Validators.required, Validators.min(1)]],
    codigoTipoDirectivo: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9]{1,2}$/)]],
    calidadDirectivo: ['1' as '1' | '2', [Validators.required]],
    estadoDirectivo: ['1' as '1' | '2' | '3', [Validators.required]],
    actaAsamblea: ['', [Validators.required, Validators.maxLength(10)]],
    fechaAsamblea: ['', [Validators.required]],
    resolucionSes: ['', [Validators.required, Validators.maxLength(10)]],
    fechaResolucion: ['', [Validators.required]],
    fechaRetiro: [null as string | null],
    periodosVigencia: [0 as number | null, [Validators.min(0)]],
  });

  get f() { return this.form.controls; }

  // Buscador persona
  buscar = signal<string>('');
  personas = signal<PersonaListItem[]>([]);
  buscando = signal(false);
  selectedPersona = signal<PersonaListItem | null>(null);

  // Navegación y UI
  activeIndex = signal<number>(0);
  setActive(i: number) { this.activeIndex.set(i); }

  // Nombre display robusto
  nombreDisplay = (p: any) => {
    const display = p?.nombreDisplay
      ?? p?.nombrePersona
      ?? p?.nombre_completo
      ?? p?.nombreCompleto
      ?? [p?.nombres, p?.primerApellido, p?.segundoApellido].filter(Boolean).join(' ');
    return (display || '').trim();
  };

  // Resaltado simple
  hl = (text: string | null | undefined) => {
    const q = (this.buscar() || '').trim();
    const t = (text || '').toString();
    if (!q) return t;
    try {
      const safeQ = q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
      const re = new RegExp(safeQ, 'ig');
      return t.replace(re, (m) => `<mark>${m}</mark>`);
    } catch {
      return t;
    }
  };

  // Teclas en el input de búsqueda
  onSearchKeydown(ev: KeyboardEvent) {
    if (!this.personas().length) return;
    const max = this.personas().length - 1;
    const i = this.activeIndex();

    if (ev.key === 'ArrowDown') {
      ev.preventDefault();
      this.activeIndex.set(i >= max ? 0 : i + 1);
    } else if (ev.key === 'ArrowUp') {
      ev.preventDefault();
      this.activeIndex.set(i <= 0 ? max : i - 1);
    } else if (ev.key === 'Enter') {
      ev.preventDefault();
      const arr = this.personas();
      if (arr.length) this.elegirPersona(arr[this.activeIndex()]);
    }
  }

  ngOnInit() {
    // Cargar catálogo tipos directivos
    this.catalogos.tiposDirectivos().subscribe({
      next: rows => this.tiposDirectivos.set(rows ?? []),
      error: () => this.tiposDirectivos.set([])
    });

    // Cargar detalle si viene :id
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam && idParam !== 'new') {
      const id = Number(idParam);
      if (!Number.isNaN(id)) {
        this.id.set(id);
        this.load(id);
      }
    }

    // Reglas de estado → fechaRetiro obligatoria si 2/3
    this.f.estadoDirectivo.valueChanges.subscribe(v => {
      const ctrl = this.f.fechaRetiro as FormControl<string | null>;
      if (v === '2' || v === '3') {
        ctrl.addValidators([Validators.required]);
      } else {
        ctrl.clearValidators();
        ctrl.setValue(null);
      }
      ctrl.updateValueAndValidity();
    });
  }

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

  load(id: number) {
    this.loading.set(true);
    this.api.get(id).subscribe({
      next: (dto: DirectivoDetailDTO) => {
        this.form.patchValue(dto as any);
        // Inicialmente solo con ID; luego resolvemos datos completos
        this.selectedPersona.set({
          idDatosPersonal: dto.idDatosPersonal,
          documento: '',
          nombres: '',
          primerApellido: '',
          segundoApellido: '',
        });
        this.loadPersonaInfo(dto.idDatosPersonal);
        this.loading.set(false);
      },
      error: (err: any) => {
        this.error.set(err?.error?.message ?? 'No se pudo cargar el directivo');
        this.loading.set(false);
      }
    });
  }

  /** Intenta obtener datos de la persona por ID para mostrar cédula y nombre. */
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

  // Buscar persona por documento/nombre
  buscarPersona() {
    const q = this.buscar().trim();
    if (!q) { this.personas.set([]); return; }
    this.buscando.set(true);
    this.personasApi.list({ q, size: 10 }).subscribe({
      next: (res: any) => {
        const rows = Array.isArray(res) ? res : res?.content ?? [];
        this.personas.set(rows.map((r: any) => this.normalizePersona(r)));
        this.activeIndex.set(0);
        this.buscando.set(false);
      },
      error: () => { this.personas.set([]); this.buscando.set(false); }
    });
  }

  elegirPersona(p: PersonaListItem) {
    const norm = this.normalizePersona(p); // por si viene sin display/tipo
    this.selectedPersona.set(norm);
    this.form.patchValue({ idDatosPersonal: norm.idDatosPersonal });
    // limpiar resultados y query
    this.personas.set([]);
    this.buscar.set('');
    this.activeIndex.set(0);
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const raw = this.form.getRawValue();
    const payload: DirectivoCreateUpdate = {
      idDatosPersonal: raw.idDatosPersonal!,
      codigoTipoDirectivo: raw.codigoTipoDirectivo!,
      calidadDirectivo: raw.calidadDirectivo!,
      estadoDirectivo: raw.estadoDirectivo!,
      actaAsamblea: raw.actaAsamblea!,
      fechaAsamblea: raw.fechaAsamblea!,
      resolucionSes: raw.resolucionSes!,
      fechaResolucion: raw.fechaResolucion!,
      fechaRetiro: raw.fechaRetiro || null,
      periodosVigencia: raw.periodosVigencia ?? 0
    };

    this.guardando.set(true);
    this.error.set(null);

    const obs = this.id()
      ? this.api.update(this.id()!, payload).pipe(map(() => void 0))
      : this.api.create(payload).pipe(map(() => void 0));

    obs.subscribe({
      next: () => {
        this.guardando.set(false);
        this.router.navigate(['/general/directivos']);
      },
      error: (err: any) => {
        this.guardando.set(false);
        this.error.set(err?.error?.message ?? 'No se pudo guardar');
      }
    });
  }

  cancelar() { this.router.navigate(['/general/directivos']); }
}
