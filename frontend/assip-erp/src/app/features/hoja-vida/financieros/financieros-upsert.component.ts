import { Component, OnInit, OnChanges, OnDestroy, SimpleChanges, Input, Output, EventEmitter, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';

import { FinancierosApi, FinancieroRequest, FinancieroResponse } from './financieros.api';
import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';

type NumKey =
  | 'valorSalario' | 'valorPension' | 'ingresosArriendo' | 'ingresosComisiones' | 'otrosIngresos'
  | 'egresosFamiliares' | 'egresosArriendo' | 'egresosCredito' | 'otrosEgresos'
  | 'deudaRelacionFinanciera' | 'totalActivos' | 'totalPasivos';

@Component({
  standalone: true,
  selector: 'app-financieros-upsert',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './financieros-upsert.component.html',
  styleUrls: ['./financieros-upsert.component.scss'],
})
export class FinancierosUpsertComponent implements OnInit, OnChanges, OnDestroy {
  // Wizard support
  @Input() captureMode: boolean = false;
  @Input() idPersonaOverride?: number | null;

  @Output() valueChange = new EventEmitter<FinancieroRequest>();
  @Output() validChange = new EventEmitter<boolean>();

  private fb = inject(FormBuilder);
  private api = inject(FinancierosApi);
  private personasApi = inject(DatosPersonalesApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  idDatosPersonales = signal<number>(0);
  idFinanciero = signal<number | null>(null);
  afiliadoNombre = signal<string>('');

  loading = signal<boolean>(true);
  guardando = signal<boolean>(false);
  error = signal<string | null>(null);
  mode = signal<'new'|'edit'>('new');

  // --- control de formato on-blur ---
  private focused: Record<NumKey, boolean> = {
    valorSalario: false, valorPension: false, ingresosArriendo: false, ingresosComisiones: false, otrosIngresos: false,
    egresosFamiliares: false, egresosArriendo: false, egresosCredito: false, otrosEgresos: false,
    deudaRelacionFinanciera: false, totalActivos: false, totalPasivos: false,
  };

  // ===== Validadores =====
  private nonNegNumber(c: AbstractControl): ValidationErrors | null {
    const n = this.toNum(c.value);
    return (n === null || n < 0) ? { nonNeg: true } : null;
  }
  private crossRulesValidator(group: AbstractControl): ValidationErrors | null {
    const g: any = group;
    const otrosI = this.toNum(g.get('otrosIngresos')?.value) ?? 0;
    const otrosE = this.toNum(g.get('otrosEgresos')?.value) ?? 0;
    const eCred  = this.toNum(g.get('egresosCredito')?.value) ?? 0;
    const deuda  = this.toNum(g.get('deudaRelacionFinanciera')?.value) ?? 0;
    const rel    = (g.get('relacionFinanciera')?.value ?? '').toString().trim();
    const origen = (g.get('origenFondos')?.value ?? '').toString().trim();

    const totIng = this.totalIngresosFromGroup(g);

    const errors: any = {};
    if (otrosI > 0 && !(g.get('comentarioOtrosIngresos')?.value ?? '').toString().trim()) {
      errors['comentarioOtrosIngresos'] = 'Requerido cuando otros ingresos > 0';
    }
    if (otrosE > 0 && !(g.get('comentarioOtrosEgresos')?.value ?? '').toString().trim()) {
      errors['comentarioOtrosEgresos'] = 'Requerido cuando otros egresos > 0';
    }
    if (totIng > 0 && !origen) {
      errors['origenFondos'] = 'Requerido cuando el total de ingresos es > 0';
    }
    if (eCred > 0) {
      if (!rel) errors['relacionFinanciera'] = 'Requerido cuando egresos crédito > 0';
      if (deuda <= 0 || isNaN(deuda)) errors['deudaRelacionFinanciera'] = 'Debe ser > 0 cuando egresos crédito > 0';
    }

    return Object.keys(errors).length ? errors : null;
  }

  // ===== Form =====
  form = this.fb.group({
    valorSalario:            ['0', [this.nonNegNumber.bind(this)]],
    valorPension:            ['0', [this.nonNegNumber.bind(this)]],
    ingresosArriendo:        ['0', [this.nonNegNumber.bind(this)]],
    ingresosComisiones:      ['0', [this.nonNegNumber.bind(this)]],
    otrosIngresos:           ['0', [this.nonNegNumber.bind(this)]],
    comentarioOtrosIngresos: [''],

    egresosFamiliares:       ['0', [this.nonNegNumber.bind(this)]],
    egresosArriendo:         ['0', [this.nonNegNumber.bind(this)]],
    egresosCredito:          ['0', [this.nonNegNumber.bind(this)]],
    otrosEgresos:            ['0', [this.nonNegNumber.bind(this)]],
    comentarioOtrosEgresos:  [''],
    relacionFinanciera:      [''],
    deudaRelacionFinanciera: ['0', [this.nonNegNumber.bind(this)]],

    totalActivos:            ['0', [this.nonNegNumber.bind(this)]],
    totalPasivos:            ['0', [this.nonNegNumber.bind(this)]],
    origenFondos:            [''],
  }, { validators: [this.crossRulesValidator.bind(this)] });

  get fc() { return this.form.controls as any; }

  // ====== Persistencia (wizard snapshot) ======
  private readonly SNAP_KEY = 'capturaHV_state';

  private hydrateFromWizardSnapshot(): void {
    try {
      const raw = sessionStorage.getItem(this.SNAP_KEY);
      if (!raw) return;
      const obj = JSON.parse(raw);
      const snap = obj?.payload?.financieros ?? null;
      if (!snap) return;

      this.form.patchValue({
        valorSalario:            this.displayFromNum(snap.valorSalario),
        valorPension:            this.displayFromNum(snap.valorPension),
        ingresosArriendo:        this.displayFromNum(snap.ingresosArriendo),
        ingresosComisiones:      this.displayFromNum(snap.ingresosComisiones),
        otrosIngresos:           this.displayFromNum(snap.otrosIngresos),
        comentarioOtrosIngresos: snap.comentarioOtrosIngresos ?? '',
        egresosFamiliares:       this.displayFromNum(snap.egresosFamiliares),
        egresosArriendo:         this.displayFromNum(snap.egresosArriendo),
        egresosCredito:          this.displayFromNum(snap.egresosCredito),
        otrosEgresos:            this.displayFromNum(snap.otrosEgresos),
        comentarioOtrosEgresos:  snap.comentarioOtrosEgresos ?? '',
        relacionFinanciera:      snap.relacionFinanciera ?? '',
        deudaRelacionFinanciera: this.displayFromNum(snap.deudaRelacionFinanciera),
        totalActivos:            this.displayFromNum(snap.totalActivos),
        totalPasivos:            this.displayFromNum(snap.totalPasivos),
        origenFondos:            snap.origenFondos ?? '',
      }, { emitEvent: false });

      this.formatAllNumbers();
    } catch {}
  }

  private persistIntoWizardSnapshot(): void {
    try {
      const raw = sessionStorage.getItem(this.SNAP_KEY);
      const obj = raw ? JSON.parse(raw) : {};
      if (!obj.payload) obj.payload = {};
      obj.payload.financieros = this.buildRequest();
      sessionStorage.setItem(this.SNAP_KEY, JSON.stringify(obj));
    } catch {}
  }

  /** Forzamos persistencia en cada input/change (además de valueChanges) */
  persistNow() {
    this.persistIntoWizardSnapshot();
    this.emitAll();
  }

  // ===== Ciclo de vida =====
  ngOnInit(): void {
    // Resolver id persona
    const idFromRoute = Number(this.route.snapshot.paramMap.get('idDatosPersonales') ?? 0);
    const idFromOverride = Number(this.idPersonaOverride ?? 0);
    const idPer =
      (Number.isFinite(idFromOverride) && idFromOverride > 0) ? idFromOverride :
      ((Number.isFinite(idFromRoute) && idFromRoute > 0) ? idFromRoute : 0);
    this.idDatosPersonales.set(idPer);

    // Edición directa
    const idFParam = this.route.snapshot.paramMap.get('idFinanciero');
    const idF = idFParam ? Number(idFParam) : null;
    this.idFinanciero.set((idF && idF > 0) ? idF : null);

    // Hidratar
    this.hydrateFromWizardSnapshot();

    // Wizard sin id: no tocar API
    if (this.captureMode && (!idPer || idPer <= 0)) {
      this.mode.set('new');
      this.loading.set(false);
      this.formatAllNumbers();
      this.emitAll();
    } else {
      // Nombre afiliado
      if (idPer > 0) this.loadAfiliadoNombre(idPer);

      // Cargar 1:1
      if (idPer > 0) {
        this.api.getByPersona(idPer).subscribe({
          next: (resp) => {
            const row: FinancieroResponse | null = resp?.status === 204 ? null : (resp?.body ?? null);
            if (row) {
              this.mode.set('edit');
              const idFRow = Number((row as any).idFinanciero ?? (row as any).id_financiero ?? 0);
              this.idFinanciero.set(Number.isFinite(idFRow) && idFRow > 0 ? idFRow : null);

              this.form.patchValue({
                valorSalario:            this.displayFromNum(row.valor_salario),
                valorPension:            this.displayFromNum(row.valor_pension),
                ingresosArriendo:        this.displayFromNum(row.ingresos_arriendo),
                ingresosComisiones:      this.displayFromNum(row.ingresos_comisiones),
                otrosIngresos:           this.displayFromNum(row.otros_ingresos),
                comentarioOtrosIngresos: row.comentario_otros_ingresos ?? '',

                egresosFamiliares:       this.displayFromNum(row.egresos_familiares),
                egresosArriendo:         this.displayFromNum(row.egresos_arriendo),
                egresosCredito:          this.displayFromNum(row.egresos_credito),
                otrosEgresos:            this.displayFromNum(row.otros_egresos),
                comentarioOtrosEgresos:  row.comentario_otros_egresos ?? '',
                relacionFinanciera:      row.relacion_financiera ?? '',
                deudaRelacionFinanciera: this.displayFromNum(row.deuda_relacion_financiera),

                totalActivos:            this.displayFromNum(row.total_activos),
                totalPasivos:            this.displayFromNum(row.total_pasivos),
                origenFondos:            row.origen_fondos ?? '',
              }, { emitEvent: false });
            } else {
              this.mode.set('new');
            }
            this.loading.set(false);
            this.formatAllNumbers();
            this.emitAll();
          },
          error: () => {
            this.mode.set('new');
            this.loading.set(false);
            this.formatAllNumbers();
            this.emitAll();
          }
        });
      } else {
        this.mode.set('new');
        this.loading.set(false);
        this.formatAllNumbers();
        this.emitAll();
      }
    }

    // Emitir + Persistir snapshot en cada cambio del form
    this.form.valueChanges.subscribe(() => { this.emitAll(); this.persistIntoWizardSnapshot(); });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ('idPersonaOverride' in changes) {
      const curr = Number(this.idPersonaOverride ?? 0);
      if (curr > 0 && curr !== this.idDatosPersonales()) {
        this.idDatosPersonales.set(curr);
        // en wizard, si ahora hay id, no tocamos API (el paso final/parent decide),
        // pero mantenemos snapshot y emisiones.
        this.emitAll();
        this.persistIntoWizardSnapshot();
      }
    }
  }

  ngOnDestroy(): void {
    // respaldo final por si hay blur/cambio pendiente
    this.persistIntoWizardSnapshot();
  }

  // ===== Errores del form a nivel grupo =====
  errors(): Record<string, string> | null {
    const e = this.form.errors as any;
    return e || null;
  }

  // ==== Formato y parsing ====
  private toNum(v: any): number | null {
    if (v === null || v === undefined || v === '') return 0;
    let s = String(v).trim();
    s = s.replace(/\s|\u00A0|\u202F/g, '');
    const lastComma = s.lastIndexOf(',');
    const lastDot = s.lastIndexOf('.');
    const decSep = lastComma > lastDot ? ',' : '.';
    if (decSep === ',') { s = s.replace(/\./g, ''); s = s.replace(/,/g, '.'); }
    else { s = s.replace(/,/g, ''); }
    const n = Number(s);
    return Number.isFinite(n) ? Number(n.toFixed(2)) : null;
  }
  fmt(n: number): string {
    return new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(Number(n || 0));
  }
  private displayFromNum(v: any): string {
    const n = this.toNum(v) ?? 0;
    return this.fmt(n);
  }

  onFocus(k: NumKey) {
    this.focused[k] = true;
    const ctrl = this.form.get(k);
    if (!ctrl) return;
    const n = this.toNum(ctrl.value) ?? 0;
    ctrl.setValue(String(n), { emitEvent: false });
  }
  onBlur(k: NumKey) {
    this.focused[k] = false;
    const ctrl = this.form.get(k);
    if (!ctrl) return;
    const n = this.toNum(ctrl.value) ?? 0;
    ctrl.setValue(this.fmt(n), { emitEvent: true });
  }
  private formatAllNumbers() {
    (Object.keys(this.focused) as NumKey[]).forEach(k => {
      const c = this.form.get(k);
      if (c && !this.focused[k]) {
        c.setValue(this.displayFromNum(c.value), { emitEvent: false });
      }
    });
  }

  // ==== Cálculos ====
  private totalIngresosFromGroup(g: any): number {
    return (this.toNum(g.get('valorSalario')?.value) ?? 0)
         + (this.toNum(g.get('valorPension')?.value) ?? 0)
         + (this.toNum(g.get('ingresosArriendo')?.value) ?? 0)
         + (this.toNum(g.get('ingresosComisiones')?.value) ?? 0)
         + (this.toNum(g.get('otrosIngresos')?.value) ?? 0);
  }
  totalIngresos(): number { return this.totalIngresosFromGroup(this.form); }
  totalEgresos(): number {
    return (this.toNum(this.fc.egresosFamiliares.value) ?? 0)
         + (this.toNum(this.fc.egresosArriendo.value) ?? 0)
         + (this.toNum(this.fc.egresosCredito.value) ?? 0)
         + (this.toNum(this.fc.otrosEgresos.value) ?? 0);
  }
  totalPatrimonio(): number {
    return (this.toNum(this.fc.totalActivos.value) ?? 0)
         - (this.toNum(this.fc.totalPasivos.value) ?? 0);
  }

  // ==== Emit (Wizard) ====
  private isValidForWizard(): boolean {
    this.form.updateValueAndValidity({ onlySelf: false, emitEvent: false });
    return this.form.valid && !this.form.errors;
  }
  private buildRequest(): FinancieroRequest {
    return {
      valorSalario:            this.toNum(this.fc.valorSalario.value) ?? 0,
      valorPension:            this.toNum(this.fc.valorPension.value) ?? 0,
      ingresosArriendo:        this.toNum(this.fc.ingresosArriendo.value) ?? 0,
      ingresosComisiones:      this.toNum(this.fc.ingresosComisiones.value) ?? 0,
      otrosIngresos:           this.toNum(this.fc.otrosIngresos.value) ?? 0,
      comentarioOtrosIngresos: (this.fc.comentarioOtrosIngresos.value ?? '').toString().trim(),

      egresosFamiliares:       this.toNum(this.fc.egresosFamiliares.value) ?? 0,
      egresosArriendo:         this.toNum(this.fc.egresosArriendo.value) ?? 0,
      egresosCredito:          this.toNum(this.fc.egresosCredito.value) ?? 0,
      otrosEgresos:            this.toNum(this.fc.otrosEgresos.value) ?? 0,
      comentarioOtrosEgresos:  (this.fc.comentarioOtrosEgresos.value ?? '').toString().trim(),
      relacionFinanciera:      (this.fc.relacionFinanciera.value ?? '').toString().trim(),
      deudaRelacionFinanciera: this.toNum(this.fc.deudaRelacionFinanciera.value) ?? 0,

      totalActivos:            this.toNum(this.fc.totalActivos.value) ?? 0,
      totalPasivos:            this.toNum(this.fc.totalPasivos.value) ?? 0,
      origenFondos:            (this.fc.origenFondos.value ?? '').toString().trim(),
    };
  }
  private emitAll(): void {
    const payload = this.buildRequest();

    // Autocompletados suaves (UX)
    if (this.totalIngresos() === 0 && !payload.origenFondos) payload.origenFondos = 'Sin ingresos';
    if (payload.egresosCredito === 0 && !payload.relacionFinanciera) payload.relacionFinanciera = 'No aplica';
    if (payload.otrosIngresos <= 0 && !payload.comentarioOtrosIngresos) payload.comentarioOtrosIngresos = 'No aplica';
    if (payload.otrosEgresos <= 0 && !payload.comentarioOtrosEgresos) payload.comentarioOtrosEgresos = 'No aplica';

    this.valueChange.emit({ ...payload });
    this.validChange.emit(this.isValidForWizard());
  }

  // ==== CRUD/Wizard save ====
  onSubmit(): void {
    if (this.guardando()) return;
    if (!this.form.valid || this.form.errors) {
      this.form.markAllAsTouched();
      return;
    }

    const idPer = this.idDatosPersonales();
    if (!idPer) {
      if (!this.captureMode) this.error.set('Falta id de persona.');
      return;
    }

    let payload = this.buildRequest();
    if (this.totalIngresos() === 0 && !payload.origenFondos) payload.origenFondos = 'Sin ingresos';
    if (payload.egresosCredito === 0 && !payload.relacionFinanciera) payload.relacionFinanciera = 'No aplica';
    if (payload.otrosIngresos <= 0 && !payload.comentarioOtrosIngresos) payload.comentarioOtrosIngresos = 'No aplica';
    if (payload.otrosEgresos <= 0 && !payload.comentarioOtrosEgresos) payload.comentarioOtrosEgresos = 'No aplica';

    this.guardando.set(true);
    const done = () => { this.guardando.set(false); this.persistIntoWizardSnapshot(); this.emitAll(); if (!this.captureMode) this.goBackToList(); };
    const fail = (e: any) => { this.guardando.set(false); this.error.set(String(e?.error || 'No fue posible guardar.')); };

    if (this.mode() === 'new') {
      this.api.create(idPer, payload).subscribe({
        next: (newId?: number) => {
          if (typeof newId === 'number' && newId > 0) { this.idFinanciero.set(newId); this.mode.set('edit'); }
          done();
        },
        error: fail
      });
    } else {
      const idF = this.idFinanciero();
      if (!idF) { this.error.set('No se encontró ID de registro.'); this.guardando.set(false); return; }
      this.api.update(idPer, idF, payload).subscribe({ next: done, error: fail });
    }
  }

  /** Método público para el Wizard (no navega). Devuelve true si guardó. */
  public saveFromParent(fkOverride?: number): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      if (!this.form.valid || this.form.errors) {
        this.form.markAllAsTouched();
        this.validChange.emit(false);
        return resolve(false);
      }
      const fk = Number(fkOverride ?? this.idDatosPersonales() ?? 0) || 0;
      if (!fk) return resolve(false);

      let payload = this.buildRequest();
      if (this.totalIngresos() === 0 && !payload.origenFondos) payload.origenFondos = 'Sin ingresos';
      if (payload.egresosCredito === 0 && !payload.relacionFinanciera) payload.relacionFinanciera = 'No aplica';
      if (payload.otrosIngresos <= 0 && !payload.comentarioOtrosIngresos) payload.comentarioOtrosIngresos = 'No aplica';
      if (payload.otrosEgresos <= 0 && !payload.comentarioOtrosEgresos) payload.comentarioOtrosEgresos = 'No aplica';

      this.guardando.set(true);
      const ok = () => { this.guardando.set(false); this.persistIntoWizardSnapshot(); this.emitAll(); resolve(true); };
      const ko = (e: any) => { this.guardando.set(false); this.error.set(String(e?.error || 'No fue posible guardar.')); resolve(false); };

      if (this.mode() === 'new') {
        this.api.create(fk, payload).subscribe({
          next: (newId?: number) => {
            if (typeof newId === 'number' && newId > 0) { this.idFinanciero.set(newId); this.mode.set('edit'); }
            ok();
          },
          error: ko
        });
      } else {
        const idF = this.idFinanciero();
        if (!idF) { this.error.set('No se encontró ID de registro.'); this.guardando.set(false); return resolve(false); }
        this.api.update(fk, idF, payload).subscribe({ next: ok, error: ko });
      }
    });
  }

  // ==== Afiliado ====
  private loadAfiliadoNombre(idDatos: number) {
    if (!idDatos || idDatos <= 0) { this.afiliadoNombre.set(''); this.emitAll(); return; }
    const maybeGet = (this.personasApi as any)?.get;
    if (typeof maybeGet === 'function') {
      (maybeGet as Function).call(this.personasApi, idDatos).subscribe({
        next: (p: any) => { this.afiliadoNombre.set(this.buildNombre(p)); this.emitAll(); },
        error: () => { this.fetchNombreFromList(idDatos); }
      });
    } else {
      this.fetchNombreFromList(idDatos);
    }
  }
  private fetchNombreFromList(idDatos: number) {
    this.personasApi.list({ size: 1000 }).subscribe({
      next: (data: any) => {
        const arr: any[] = Array.isArray(data) ? data : (data?.items ?? data?.content ?? []);
        const p = (arr ?? []).find((it: any) =>
          [it?.id, it?.idDatosPersonal, it?.id_datos_personales, it?.idDatosPersonales]
            .some((v: any) => Number(v) === idDatos)
        );
        this.afiliadoNombre.set(this.buildNombre(p));
        this.emitAll();
      },
      error: () => { this.afiliadoNombre.set(''); this.emitAll(); },
    });
  }
  private buildNombre(p: any): string {
    if (!p) return '';
    const nombres = p.nombres ?? [p.primerNombre, p.segundoNombre].filter(Boolean).join(' ').trim();
    const apellidos = p.apellidos ?? [p.primerApellido, p.segundoApellido].filter(Boolean).join(' ').trim();
    return [nombres, apellidos].filter(Boolean).join(' ').trim();
  }

  // ==== Navegación CRUD ====
  private goBackToList(): void { this.router.navigate(['/hoja-vida/financieros']); }
  onDelete(): void {
    const idPer = this.idDatosPersonales();
    const idF = this.idFinanciero();
    if (!idPer || !idF) { this.error.set('No se encontró el registro a eliminar.'); return; }
    if (!confirm('¿Deseas eliminar este registro financiero? Esta acción no se puede deshacer.')) return;

    this.guardando.set(true);
    this.api.delete(idPer, idF).subscribe({
      next: () => { this.guardando.set(false); this.goBackToList(); },
      error: (e: any) => { this.guardando.set(false); this.error.set(String(e?.error || 'No fue posible eliminar.')); }
    });
  }
}
