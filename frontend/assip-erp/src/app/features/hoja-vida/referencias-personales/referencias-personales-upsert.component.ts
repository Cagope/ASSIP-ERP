import {
  Component, OnInit, OnChanges, SimpleChanges, computed,
  inject, signal, AfterViewInit, Input, Output, EventEmitter
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import {
  FormBuilder, ReactiveFormsModule, Validators,
  FormControl, FormGroup, ValidatorFn, AbstractControl
} from '@angular/forms';
import { DeptoCiudadComponent } from '../../../shared/catalogos/depto-ciudad/depto-ciudad.component';

import { SessionService } from '../../../core/auth/session.service';
import { ReferenciasPersonalesApi, ReferenciaPersonal } from './referencias-personales.api';
import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';
import { of, firstValueFrom, catchError } from 'rxjs';

@Component({
  standalone: true,
  selector: 'app-referencias-personales-upsert',
  imports: [CommonModule, ReactiveFormsModule, DeptoCiudadComponent],
  templateUrl: './referencias-personales-upsert.component.html',
  styleUrls: ['./referencias-personales-upsert.component.scss'],
})
export class ReferenciasPersonalesUpsertComponent implements OnInit, OnChanges, AfterViewInit {
  private fb = inject(FormBuilder);
  private api = inject(ReferenciasPersonalesApi);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private session = inject(SessionService);
  private personasApi = inject(DatosPersonalesApi);

  @Input() idDatosPersonalesInput: number | null = null;

  // Integración con wizard
  @Output() validChange = new EventEmitter<boolean>();
  @Output() valueChange = new EventEmitter<any>();

  idDatosPersonales = signal<number>(0);
  loading = signal<boolean>(true);
  guardando = signal<boolean>(false);
  errorMsg = signal<string | null>(null);

  idReferencia = signal<number | null>(null);
  afiliadoNombre = signal<string>('');

  // Validador que exige ambos: depto y ciudad
  private requireDeptoCiudad: ValidatorFn = (group: AbstractControl) => {
    const dep = group.get('id_departamento')?.value;
    const ciu = group.get('id_ciudad')?.value;
    const okDep = typeof dep === 'number' && dep > 0;
    const okCiu = typeof ciu === 'number' && ciu > 0;
    return okDep && okCiu ? null : { missingDeptoCiudad: true };
  };

  form: FormGroup = this.fb.group(
    {
      nombre_referencia_personal: ['', [Validators.required, Validators.maxLength(100)]],
      direccion_referencia_personal: ['', [Validators.required, Validators.maxLength(100)]],

      // Requeridos
      id_departamento: new FormControl<number | null>(null, { nonNullable: false, validators: [Validators.required] }),
      id_ciudad:       new FormControl<number | null>(null, { nonNullable: false, validators: [Validators.required] }),

      telefono_referencia_personal: ['' as string | null, [Validators.pattern(/^\d{7}$/)]],
      celular_referencia_personal:  ['' as string | null, [Validators.pattern(/^\d{10}$/)]],
    },
    { validators: [this.requireDeptoCiudad] }
  );

  get depCtrl(): FormControl<number | null> { return this.form.controls['id_departamento'] as FormControl<number | null>; }
  get ciuCtrl(): FormControl<number | null> { return this.form.controls['id_ciudad'] as FormControl<number | null>; }

  tieneRegistro = computed(() => this.idReferencia() != null);
  titulo = computed(() => (this.tieneRegistro() ? 'Editar referencia personal' : 'Nueva referencia personal'));

  private tryN(x: any): number { const n = Number(x ?? 0); return Number.isFinite(n) && n > 0 ? n : 0; }

  private resolveFk(): number {
    const a = this.tryN(this.idDatosPersonalesInput); if (a) return a;

    const rootQ = this.router.routerState.snapshot.root.queryParams;
    const rq = this.tryN(rootQ?.['idDatosPersonales']); if (rq) return rq;

    const qp = this.route.snapshot.queryParamMap;
    const b = this.tryN(qp.get('idDatosPersonales')); if (b) return b;

    const pm = this.route.snapshot.paramMap;
    const c = this.tryN(pm.get('idDatosPersonales')) || this.tryN(pm.get('id')); if (c) return c;

    try {
      const raw = sessionStorage.getItem('capturaHV_state');
      if (raw) {
        const obj = JSON.parse(raw);
        const p = obj?.payload ?? {};
        const dp = p?.datosPersonales ?? p;
        const d = this.tryN(dp?.id_datos_personal) || this.tryN(dp?.idDatosPersonal) || this.tryN(dp?.id);
        if (d) return d;
      }
    } catch {}

    return 0;
  }

  private loadForPersonaIfNeeded(fk: number): void {
    if (!fk || fk <= 0) { this.loading.set(false); this.emitToWizard(); return; }

    this.loadAfiliadoNombre(fk).catch(() => {});
    this.loading.set(true);
    this.errorMsg.set(null);

    this.depCtrl.enable({ emitEvent: false });
    this.ciuCtrl.enable({ emitEvent: false });

    this.api.getByPersona(fk).subscribe({
      next: (row: ReferenciaPersonal | null) => {
        this.idReferencia.set(row?.id_referencia_personal ?? null);

        const dep = row?.id_departamento != null ? Number(row.id_departamento) : null;
        const ciu = row?.id_ciudad != null ? Number(row.id_ciudad) : null;

        this.form.patchValue({
          nombre_referencia_personal: row?.nombre_referencia_personal ?? '',
          direccion_referencia_personal: row?.direccion_referencia_personal ?? '',
          id_departamento: dep,
          id_ciudad: ciu,
          telefono_referencia_personal: row?.telefono_referencia_personal ?? '',
          celular_referencia_personal: row?.celular_referencia_personal ?? '',
        }, { emitEvent: false });

        // dispara carga de ciudades en el hijo y luego setea ciudad
        this.depCtrl.setValue(dep, { emitEvent: true });
        setTimeout(() => {
          this.ciuCtrl.setValue(ciu, { emitEvent: true });
          this.emitToWizard();
        }, 0);

        this.loading.set(false);
      },
      error: () => {
        this.idReferencia.set(null);
        this.depCtrl.setValue(this.depCtrl.value ?? null, { emitEvent: true });
        setTimeout(() => this.ciuCtrl.setValue(this.ciuCtrl.value ?? null, { emitEvent: true }), 0);
        this.loading.set(false);
        this.emitToWizard();
      },
    });
  }

  async ngOnInit(): Promise<void> {
    // emite al wizard en cada cambio (incluye validador de grupo)
    this.form.valueChanges.subscribe(() => this.emitToWizard());

    const fk = this.resolveFk();
    this.idDatosPersonales.set(fk || 0);
    if (fk > 0) this.loadForPersonaIfNeeded(fk);
    else {
      this.depCtrl.enable({ emitEvent: false });
      this.ciuCtrl.enable({ emitEvent: false });
      this.loading.set(false);
      this.emitToWizard();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ('idDatosPersonalesInput' in changes) {
      const curr = this.tryN(this.idDatosPersonalesInput);
      const prev = this.tryN(changes['idDatosPersonalesInput']?.previousValue);
      if (curr > 0 && curr !== prev) {
        this.idDatosPersonales.set(curr);
        this.loadForPersonaIfNeeded(curr);
      }
    }
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.depCtrl.setValue(this.depCtrl.value ?? null, { emitEvent: true });
      setTimeout(() => this.ciuCtrl.setValue(this.ciuCtrl.value ?? null, { emitEvent: true }), 0);
      this.emitToWizard();
    }, 0);
  }

  private async loadAfiliadoNombre(id: number): Promise<void> {
    if (!id) { this.afiliadoNombre.set(''); return; }
    const p: any = await firstValueFrom(
      (this.personasApi as any).get(id).pipe(catchError(() => of(null)))
    );
    const nombres = (p?.nombres ?? `${p?.primerNombre ?? ''} ${p?.segundoNombre ?? ''}`).toString().trim();
    const apellidos = (p?.apellidos ?? `${p?.primerApellido ?? ''} ${p?.segundoApellido ?? ''}`).toString().trim();
    this.afiliadoNombre.set(`${nombres} ${apellidos}`.replace(/\s+/g, ' ').trim());
  }

  private emitToWizard(): void {
    this.form.updateValueAndValidity({ emitEvent: false });
    this.validChange.emit(this.form.valid);
    this.valueChange.emit(this.form.getRawValue());
  }

  goBack(): void {
    this.router.navigate(['/hoja-vida/referencias-personales', this.idDatosPersonales()]);
  }

  save(): void {
    this.saveFromParent().then(ok => { if (ok) this.goBack(); });
  }

  public saveFromParent(fkOverride?: number): Promise<boolean> {
    return new Promise<boolean>(resolve => {
      this.errorMsg.set(null);

      const fk = this.tryN(fkOverride) || this.idDatosPersonales() || this.resolveFk();
      this.idDatosPersonales.set(fk || 0);

      if (!fk) {
        console.error('[RefPers] Guardado abortado: FK=0. Revisa @Input/ruta/payload.');
        resolve(false);
        return;
      }
      if (this.form.invalid) {
        this.form.markAllAsTouched();
        this.emitToWizard();
        resolve(false);
        return;
      }

      const trim = (x: unknown) => (typeof x === 'string' ? x.trim() : x);
      const upper = (s: string) => s?.toLocaleUpperCase?.() ?? s?.toUpperCase?.() ?? s;
      const strOrNull = (x: unknown) => {
        const s = trim(x);
        return s === '' || s === null || s === undefined ? null : String(s);
      };

      const v = this.form.getRawValue() as any;

      const payload: ReferenciaPersonal = {
        id_datos_personal: fk,
        id_referencia_personal: this.idReferencia() ?? undefined,
        nombre_referencia_personal: upper(String(v.nombre_referencia_personal).trim()),
        direccion_referencia_personal: upper(String(v.direccion_referencia_personal).trim()),
        id_departamento: v.id_departamento ?? null,
        id_ciudad: v.id_ciudad ?? null,
        telefono_referencia_personal: strOrNull(v.telefono_referencia_personal),
        celular_referencia_personal: strOrNull(v.celular_referencia_personal),
      };

      this.guardando.set(true);

      const obs = this.tieneRegistro()
        ? this.api.updateForPersona(fk, this.idReferencia()!, payload)
        : this.api.createForPersona(fk, payload);

      const sub = obs.subscribe({
        next: (res: any) => {
          const newId = res?.id_referencia_personal ?? res?.idReferenciaPersonal ?? null;
          if (!this.idReferencia() && newId) this.idReferencia.set(Number(newId));
          if (!this.idReferencia()) {
            this.api.getByPersona(fk).subscribe({
              next: (row2: ReferenciaPersonal | null) => {
                if (row2?.id_referencia_personal) this.idReferencia.set(Number(row2.id_referencia_personal));
              },
              error: () => {},
            });
          }

          this.guardando.set(false);
          sub.unsubscribe();
          resolve(true);
        },
        error: async (err: unknown) => {
          this.guardando.set(false);
          sub.unsubscribe();
          let serverMsg = 'No fue posible guardar el registro.';
          try {
            const anyErr = err as any;
            if (anyErr?.error instanceof Blob) {
              const txt = await anyErr.error.text();
              if (txt) serverMsg = txt;
            } else if (typeof anyErr?.error === 'string') {
              serverMsg = anyErr.error;
            } else if (anyErr?.error?.message) {
              serverMsg = anyErr.error.message;
            } else if (anyErr?.message) {
              serverMsg = anyErr.message;
            }
          } catch {}
          this.errorMsg.set(serverMsg);
          console.error('Error API (ReferenciasPersonales):', err);
          resolve(false);
        },
      });
    });
  }
}
