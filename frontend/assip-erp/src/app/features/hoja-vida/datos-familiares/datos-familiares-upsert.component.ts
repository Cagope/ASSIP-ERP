import { Component, OnInit, computed, inject, signal, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators, FormControl, FormGroup } from '@angular/forms';

import { DatosFamiliaresApi, DatosFamiliar } from './datos-familiares.api';
import { SessionService } from '../../../core/auth/session.service';

import { CatalogosApi, CodigoNombreDTO } from '../../../shared/catalogos/catalogos.api';
import { DeptoCiudadComponent } from '../../../shared/catalogos/depto-ciudad/depto-ciudad.component';
import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';

@Component({
  standalone: true,
  selector: 'app-datos-familiares-upsert',
  imports: [CommonModule, ReactiveFormsModule, DeptoCiudadComponent],
  templateUrl: './datos-familiares-upsert.component.html',
  styleUrls: ['./datos-familiares-upsert.component.scss'],
})
export class DatosFamiliaresUpsertComponent implements OnInit {
  private fb = inject(FormBuilder);
  private api = inject(DatosFamiliaresApi);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private session = inject(SessionService);
  private cat = inject(CatalogosApi);
  private personasApi = inject(DatosPersonalesApi);

  // 👉 Señales para el wizard
  @Output() validChange = new EventEmitter<boolean>();
  @Output() valueChange = new EventEmitter<any>();

  idDatosPersonales = signal<number>(0);       // FK desde ruta
  id = signal<number | null>(null);            // id_datos_familiares
  afiliadoNombre = signal<string>('');         // nombre completo afiliado en encabezado

  loading = signal<boolean>(true);
  guardando = signal<boolean>(false);
  errorMsg = signal<string | null>(null);

  parentescos = signal<CodigoNombreDTO[]>([]);

  form: FormGroup = this.fb.group({
    codigo_parentesco: ['' as string | null],
    nombre_datos_familiar: ['', [Validators.required, Validators.maxLength(100)]],
    documento_datos_familiar: ['' as string | null, [Validators.pattern(/^\d+$/), Validators.maxLength(20)]],
    fecha_nacimiento: ['' as string | null],
    telefono_datos_familiar: ['' as string | null, [Validators.pattern(/^\d{7}$/)]],
    celular_datos_familiar: ['' as string | null, [Validators.pattern(/^\d{10}$/)]],
    direccion_datos_familiar: ['', [Validators.required, Validators.maxLength(100)]],

    // ✅ Requeridos
    id_departamento: new FormControl<number | null>(null, { nonNullable: false, validators: [Validators.required] }),
    id_ciudad:       new FormControl<number | null>(null, { nonNullable: false, validators: [Validators.required] }),

    ingresos_datos_familiar: new FormControl<number | null>(0),
    egresos_datos_familiar: new FormControl<number | null>(0),

    referencia_familiar: new FormControl<boolean | null>(false),
  });

  get depCtrl(): FormControl<number | null> { return this.form.controls['id_departamento'] as FormControl<number | null>; }
  get ciuCtrl(): FormControl<number | null> { return this.form.controls['id_ciudad'] as FormControl<number | null>; }

  canEdit = computed(() => {
    const me = this.session.me?.();
    if (!me) return true;
    const roles: string[] = (me as any)?.roles ?? [];
    const permisos: string[] = (me as any)?.permisos ?? [];
    return roles.includes('ADMIN') || permisos.includes('HV_FAMILIA_EDIT');
  });

  titulo = computed(() => (this.id() ? 'Editar familiar' : 'Nuevo familiar'));

  ngOnInit(): void {
    // catálogos
    this.cat.parentescos().subscribe({
      next: (rows) => this.parentescos.set(rows ?? []),
      error: () => this.parentescos.set([]),
    });

    // Emite validez y valor al wizard en cada cambio
    this.form.valueChanges.subscribe(() => {
      this.form.updateValueAndValidity({ emitEvent: false });
      this.validChange.emit(this.form.valid);
      this.valueChange.emit(this.form.getRawValue());
    });

    // Reacciona a parámetros de ruta (new/edit)
    this.route.paramMap.subscribe(pm => {
      const idDatos = Number(pm.get('idDatosPersonales') ?? 0);
      const id = pm.get('id');

      this.idDatosPersonales.set(idDatos);
      this.id.set(id ? Number(id) : null);

      // cargar nombre del afiliado
      this.loadAfiliadoNombre(idDatos);

      this.loading.set(true);
      this.errorMsg.set(null);

      if (this.id()) {
        // === EDITAR ===
        this.api.getForPersona(this.idDatosPersonales(), this.id()!).subscribe({
          next: (row) => {
            this.form.patchValue({
              codigo_parentesco: row.codigo_parentesco ?? '',
              nombre_datos_familiar: row.nombre_datos_familiar ?? '',
              documento_datos_familiar: row.documento_datos_familiar ?? '',
              fecha_nacimiento: row.fecha_nacimiento ?? '',
              telefono_datos_familiar: row.telefono_datos_familiar ?? '',
              celular_datos_familiar: row.celular_datos_familiar ?? '',
              direccion_datos_familiar: row.direccion_datos_familiar ?? '',
              id_departamento: row.id_departamento ?? null,
              id_ciudad: row.id_ciudad ?? null,
              ingresos_datos_familiar: (row.ingresos_datos_familiar ?? 0) as number,
              egresos_datos_familiar: (row.egresos_datos_familiar ?? 0) as number,
              referencia_familiar: !!row.referencia_familiar,
            }, { emitEvent: false });

            // Forzamos flujo Depto -> ciudades -> ciudad
            if (!this.canEdit()) this.form.disable({ emitEvent: false });
            else {
              this.form.enable({ emitEvent: false });
              this.depCtrl.enable({ emitEvent: false });
              this.ciuCtrl.enable({ emitEvent: false });
            }

            const dep = row.id_departamento ?? null;
            const ciu = row.id_ciudad ?? null;

            // 1) Dispara valueChanges para que el hijo cargue ciudades
            this.depCtrl.setValue(dep, { emitEvent: true });

            // 2) Tras un tick, asigna la ciudad ya cargada por el hijo
            setTimeout(() => {
              this.ciuCtrl.setValue(ciu, { emitEvent: true });
              // asegura cálculo de validez para el wizard
              this.form.updateValueAndValidity({ emitEvent: false });
              this.validChange.emit(this.form.valid);
              this.valueChange.emit(this.form.getRawValue());
            }, 0);

            this.loading.set(false);
          },
          error: () => {
            this.errorMsg.set('No fue posible cargar el registro.');
            this.loading.set(false);
            // emite estado mínimo igualmente
            this.validChange.emit(this.form.valid);
            this.valueChange.emit(this.form.getRawValue());
          },
        });
      } else {
        // === NUEVO ===
        if (!this.canEdit()) {
          this.form.disable({ emitEvent: false });
        } else {
          this.form.enable({ emitEvent: false });
          this.depCtrl.enable({ emitEvent: false });
          this.ciuCtrl.enable({ emitEvent: false });
        }
        this.loading.set(false);
        // emite estado inicial al wizard
        this.form.updateValueAndValidity({ emitEvent: false });
        this.validChange.emit(this.form.valid);
        this.valueChange.emit(this.form.getRawValue());
      }
    });
  }

  // ===== Nombre del afiliado (para el encabezado)
  private loadAfiliadoNombre(id: number): void {
    if (!id) {
      this.afiliadoNombre.set('');
      return;
    }
    (this.personasApi as any).get?.(id)?.subscribe({
      next: (p: any) => this.afiliadoNombre.set(this.composePersonaNombre(p)),
      error: () => this.afiliadoNombre.set(''),
    });
  }

  private composePersonaNombre(p: any): string {
    const nombres = (p?.nombres ?? `${p?.primerNombre ?? ''} ${p?.segundoNombre ?? ''}`).toString().trim();
    const apellidos = (p?.apellidos ?? `${p?.primerApellido ?? ''} ${p?.segundoApellido ?? ''}`).toString().trim();
    return `${nombres} ${apellidos}`.replace(/\s+/g, ' ').trim();
  }

  // ===== Navegación
  goBack(): void {
    this.router.navigate(['/hoja-vida/datos-familiares', this.idDatosPersonales()]);
  }

  // ===== Guardado
  save(): void {
    this.errorMsg.set(null);

    const fk = this.idDatosPersonales();
    if (!fk || fk <= 0) {
      this.errorMsg.set('Falta el ID de datos personales. Vuelve a entrar desde el listado de Datos Familiares.');
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      // emite invalidez al wizard por si lo llama desde ahí
      this.validChange.emit(false);
      return;
    }

    const trim = (x: unknown) => (typeof x === 'string' ? x.trim() : x);
    const upper = (s: string) => s.toLocaleUpperCase?.() ?? s.toUpperCase?.() ?? s;
    const strOrNull = (x: unknown) => {
      const s = trim(x);
      return s === '' || s === null || s === undefined ? null : String(s);
    };
    const numOrNull = (x: unknown) =>
      x === null || x === undefined || x === '' ? null : Number(x);

    const v = this.form.getRawValue() as any;

    let codigoPar = strOrNull(v.codigo_parentesco);
    if (codigoPar != null) codigoPar = codigoPar.slice(0, 2);

    const documento = strOrNull(v.documento_datos_familiar);

    let fechaNac = strOrNull(v.fecha_nacimiento);
    try {
      const hoy = new Date().toISOString().slice(0, 10);
      if (!fechaNac || (typeof fechaNac === 'string' && fechaNac > hoy)) fechaNac = '1900-01-01';
    } catch {
      fechaNac = '1900-01-01';
    }

    const idDepartamento = v.id_departamento ?? null;
    const idCiudad = v.id_ciudad ?? null;

    const payloadSnake: DatosFamiliar = {
      id_datos_personal: fk,
      nombre_datos_familiar: upper(String(v.nombre_datos_familiar).trim()),
      direccion_datos_familiar: upper(String(v.direccion_datos_familiar).trim()),
      referencia_familiar: !!v.referencia_familiar,
      fecha_nacimiento: fechaNac,
      codigo_parentesco: codigoPar,
      documento_datos_familiar: documento,
      telefono_datos_familiar: strOrNull(v.telefono_datos_familiar),
      celular_datos_familiar: strOrNull(v.celular_datos_familiar),
      id_departamento: idDepartamento,
      id_ciudad: idCiudad,
      ingresos_datos_familiar: numOrNull(v.ingresos_datos_familiar),
      egresos_datos_familiar: numOrNull(v.egresos_datos_familiar),
      id_datos_familiares: this.id() ?? undefined,
    };

    this.guardando.set(true);

    const obs = this.id()
      ? this.api.updateForPersona(this.idDatosPersonales(), this.id()!, payloadSnake)
      : this.api.create(payloadSnake);

    obs.subscribe({
      next: () => {
        this.guardando.set(false);
        this.goBack();
      },
      error: async (err: unknown) => {
        this.guardando.set(false);
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
        console.error('Error API (DatosFamiliares):', err);
      },
    });
  }

  // ========= SOLO PARA FORMATO NUMÉRICO EN INPUT =========
  private isFocusIng = false;
  private isFocusEgr = false;

  moneyDisplay(ctrlName: 'ingresos_datos_familiar' | 'egresos_datos_familiar'): string {
    const v = this.form.controls[ctrlName].value;
    if (v === null || v === undefined || v === '') return '';
    const n = Number(v);
    if (!Number.isFinite(n)) return '';
    const isFocus = ctrlName === 'ingresos_datos_familiar' ? this.isFocusIng : this.isFocusEgr;
    if (isFocus) return String(n);
    return new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(n);
  }

  private moneyParseToNumber(text: string): number | null {
    if (text == null) return null;
    let s = String(text).trim();
    if (!s) return null;
    s = s.replace(/\s|\u00A0|\u202F/g, '');

    // separadores típicos
    const lastComma = s.lastIndexOf(',');
    const lastDot = s.lastIndexOf('.');
    const decSep = lastComma > lastDot ? ',' : '.';
    if (decSep === ',') { s = s.replace(/\./g, ''); s = s.replace(/,/g, '.'); }
    else { s = s.replace(/,/g, ''); }

    const n = Number(s);
    return Number.isFinite(n) ? Number(n.toFixed(2)) : null;
  }

  onMoneyInput(ctrlName: 'ingresos_datos_familiar' | 'egresos_datos_familiar', ev: Event): void {
    const val = (ev.target as HTMLInputElement)?.value ?? '';
    const n = this.moneyParseToNumber(val);
    this.form.controls[ctrlName].setValue(n, { emitEvent: true });
  }

  moneyFocus(ctrlName: 'ingresos_datos_familiar' | 'egresos_datos_familiar'): void {
    if (ctrlName === 'ingresos_datos_familiar') this.isFocusIng = true;
    else this.isFocusEgr = true;
  }

  moneyBlur(ctrlName: 'ingresos_datos_familiar' | 'egresos_datos_familiar'): void {
    if (ctrlName === 'ingresos_datos_familiar') this.isFocusIng = false;
    else this.isFocusEgr = false;
  }
}
