import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormControl } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { DeptoCiudadComponent } from '../../../shared/catalogos/depto-ciudad/depto-ciudad.component';
import { CatalogosApi } from '../../../shared/catalogos/catalogos.api';
import { UbicacionesApi } from './ubicaciones.api';

// Reutilizamos APIs existentes de General
import { ZonasApi } from '../../general/zonas/zonas.api';
import { SubZonasApi } from '../../general/sub-zonas/sub-zonas.api';

type PaisDTO = { id_pais: number; nombre_pais: string };
type ZonaListDTO = { idZona: number; codigoZona?: string; nombreZona: string };
type SubZonaListDTO = { idSubZona: number; idZona: number | null; codigoSubZona?: string; nombreSubZona: string };

@Component({
  selector: 'app-ubicaciones-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, DeptoCiudadComponent],
  templateUrl: './ubicaciones-upsert.component.html',
  styleUrls: ['./ubicaciones-upsert.component.scss'],
})
export class UbicacionesUpsertComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private cat = inject(CatalogosApi);
  private ubicApi = inject(UbicacionesApi);
  private zonasApi = inject(ZonasApi);
  private subZonasApi = inject(SubZonasApi);

  idDatosPersonales = signal<number | null>(null);
  idUbicacion = signal<number | null>(null);

  // Catálogos
  paises = signal<PaisDTO[]>([]);
  zonas = signal<ZonaListDTO[]>([]);
  subZonas = signal<SubZonaListDTO[]>([]);

  // Form
  form = this.fb.nonNullable.group({
    direccion: ['', [Validators.required, Validators.maxLength(100)]],
    barrio: ['', [Validators.required, Validators.maxLength(100)]],

    telefono: ['', [Validators.pattern(/^[0-9]{7}$/)]],
    celular_uno: ['', [Validators.pattern(/^[0-9]{10}$/)]],
    celular_dos: ['', [Validators.pattern(/^[0-9]{10}$/)]],
    correo: ['', [Validators.email, Validators.maxLength(100)]],

    // ⬇️ Requeridos para bloquear “Siguiente”
    id_pais: [null as number | null, [Validators.required]],
    id_departamento: [null as number | null, [Validators.required]],
    id_ciudad: [null as number | null, [Validators.required]],

    // UI: Zona/Sub-zona (en back solo se guarda sub-zona)
    id_zona: [null as number | null],
    id_sub_zona: [null as number | null, [Validators.required]],
  });

  // Getters tipados para <app-depto-ciudad>
  get depCtrl(): FormControl<number | null> { return this.form.controls.id_departamento; }
  get ciuCtrl(): FormControl<number | null> { return this.form.controls.id_ciudad; }

  ngOnInit(): void {
    const idP = this.route.snapshot.paramMap.get('idDatosPersonales');
    const idU = this.route.snapshot.paramMap.get('id');
    this.idDatosPersonales.set(idP ? Number(idP) : null);
    this.idUbicacion.set(idU ? Number(idU) : null);

    this.cargarPaises();
    this.cargarZonas();

    if (this.idDatosPersonales()) {
      this.cargarUbicacion();
    }

    // Reaccionar a cambios de Zona
    this.form.controls.id_zona.valueChanges.subscribe((idZ) => {
      this.onZonaChange(idZ);
    });
  }

  // ===== Catálogos =====
  private cargarPaises() {
    this.cat.paises().subscribe({
      next: (list: any[]) => {
        const norm = (list ?? []).map(p => ({
          id_pais: Number(p?.id_pais ?? p?.id ?? 0),
          nombre_pais: p?.nombre_pais ?? p?.nombre ?? '',
        }))
        .filter(x => Number.isFinite(x.id_pais) && x.id_pais > 0) as PaisDTO[];
        this.paises.set(norm);
      },
      error: () => this.paises.set([]),
    });
  }

  private cargarZonas() {
    this.zonasApi.list().subscribe({
      next: (resp: any) => {
        const arr = Array.isArray(resp) ? resp : (resp?.items ?? resp?.content ?? []);
        const norm: ZonaListDTO[] = (arr ?? []).map((z: any) => ({
          idZona: Number(z?.idZona ?? z?.id_zona ?? z?.id ?? 0),
          codigoZona: z?.codigoZona ?? z?.codigo_zona ?? '',
          nombreZona: z?.nombreZona ?? z?.nombre_zona ?? '',
        }))
        .filter((z: ZonaListDTO) => Number.isFinite(z.idZona) && z.idZona > 0);
        this.zonas.set(norm);
      },
      error: () => this.zonas.set([]),
    });
  }

  private cargarSubZonasPorZona(idZona: number) {
    this.subZonasApi.list({ idZona }).subscribe({
      next: (resp: any) => {
        const arr = Array.isArray(resp) ? resp : (resp?.items ?? resp?.content ?? []);
        const norm: SubZonaListDTO[] = (arr ?? []).map((s: any) => ({
          idSubZona: Number(s?.idSubZona ?? s?.id_sub_zona ?? s?.id ?? 0),
          idZona: (s?.idZona ?? s?.id_zona) != null ? Number(s?.idZona ?? s?.id_zona) : null,
          codigoSubZona: s?.codigoSubZona ?? s?.codigo_sub_zona ?? '',
          nombreSubZona: s?.nombreSubZona ?? s?.nombre_sub_zona ?? '',
        }))
        .filter((s: SubZonaListDTO) => Number.isFinite(s.idSubZona) && s.idSubZona > 0);
        this.subZonas.set(norm);
      },
      error: () => this.subZonas.set([]),
    });
  }

  // ===== Carga / Guardado =====
  private cargarUbicacion() {
    this.ubicApi.getByPersona(this.idDatosPersonales()!).subscribe({
      next: (d: any) => {
        const data = d?.body ?? d;
        if (!data) return;

        const idPais = (data.id_pais != null) ? Number(data.id_pais) : null;
        const idDepto = (data.id_departamento != null) ? Number(data.id_departamento) : null;
        const idCiudad = (data.id_ciudad != null) ? Number(data.id_ciudad) : null;
        const idSub = (data.id_sub_zona != null) ? Number(data.id_sub_zona) : null;

        this.form.patchValue({
          direccion: data.direccion ?? '',
          barrio: data.barrio ?? '',
          telefono: data.telefono ?? '',
          celular_uno: data.celular_uno ?? '',
          celular_dos: data.celular_dos ?? '',
          correo: data.correo ?? '',

          id_pais: idPais,
          id_departamento: idDepto,
          id_ciudad: idCiudad,

          id_sub_zona: idSub,
        }, { emitEvent: false });

        // Si viene sub-zona, resolvemos la zona y cargamos sub-zonas de esa zona
        if (idSub && idSub > 0) {
          this.subZonasApi.get(idSub).subscribe({
            next: (det: any) => {
              const idZ = det?.idZona ?? det?.id_zona ?? null;
              if (idZ) {
                const zNum = Number(idZ);
                this.form.controls.id_zona.setValue(zNum, { emitEvent: false });
                this.cargarSubZonasPorZona(zNum);
                // Nota: no tocamos id_sub_zona; como ya está en el control
                // y ahora las opciones están cargadas, Angular selecciona la opción correcta.
              }
            },
            error: () => { /* no-op */ },
          });
        }
      },
      error: () => { /* nuevo → sin datos */ },
    });
  }

  onZonaChange(idZ: number | null) {
    if (idZ && idZ > 0) {
      this.cargarSubZonasPorZona(Number(idZ));
      // reset de sub-zona cuando cambia zona
      this.form.controls.id_sub_zona.setValue(null);
    } else {
      this.subZonas.set([]);
      this.form.controls.id_sub_zona.setValue(null);
    }
  }

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const personaId = this.idDatosPersonales()!;
    const raw = { ...this.form.value } as any;

    const payload: any = {
      direccion: (raw.direccion ?? '').trim(),
      barrio: (raw.barrio ?? '').trim(),
      telefono: (raw.telefono ?? '').trim() || null,
      celular_uno: (raw.celular_uno ?? '').trim() || null,
      celular_dos: (raw.celular_dos ?? '').trim() || null,
      correo: (raw.correo ?? '').trim() || null,
      id_pais: raw.id_pais != null ? Number(raw.id_pais) : null,
      id_departamento: Number(raw.id_departamento),
      id_ciudad: Number(raw.id_ciudad),
      id_sub_zona: Number(raw.id_sub_zona), // requerido → nunca null
    };

    this.ubicApi.getByPersona(personaId).subscribe({
      next: (existing: any) => {
        const e = existing?.body ?? existing;
        const existingId = e?.id_ubicacion ?? e?.id ?? null;

        if (existingId) {
          this.ubicApi.updateForPersona(personaId, existingId, payload).subscribe({
            next: () => this.router.navigate(['/hoja-vida/ubicaciones']),
            error: () => this.router.navigate(['/hoja-vida/ubicaciones']),
          });
        } else {
          this.ubicApi.createForPersona(personaId, payload).subscribe({
            next: () => this.router.navigate(['/hoja-vida/ubicaciones']),
            error: () => this.router.navigate(['/hoja-vida/ubicaciones']),
          });
        }
      },
      error: () => {
        this.ubicApi.createForPersona(personaId, payload).subscribe({
          next: () => this.router.navigate(['/hoja-vida/ubicaciones']),
          error: () => this.router.navigate(['/hoja-vida/ubicaciones']),
        });
      }
    });
  }

  cancel() {
    this.router.navigate(['/hoja-vida/ubicaciones']);
  }
}
