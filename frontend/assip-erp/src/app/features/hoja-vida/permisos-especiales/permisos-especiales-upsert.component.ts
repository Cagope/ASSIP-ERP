// src/app/features/hoja-vida/permisos-especiales/permisos-especiales-upsert.component.ts
import { Component, OnInit, inject, signal, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Observable, from, isObservable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';
import { PermisosEspecialesApi } from './permisos-especiales.api';

type ViewPermisos = {
  recibeLlamadas: boolean;
  recibeMsm: boolean;
  recibeEmails: boolean;
  recibeCartas: boolean;
  recibeRedesSociales: boolean;
};

@Component({
  standalone: true,
  selector: 'app-permisos-especiales-upsert',
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="hdr">
      <div>
        <h2>{{ mode() === 'new' ? 'Nuevo permiso especial' : 'Editar permiso especial' }}</h2>
        <div class="sub">Afiliado : <b>{{ afiliadoNombre() || '—' }}</b></div>
      </div>
    </div>

    <div *ngIf="loading()" class="st">Cargando…</div>
    <div *ngIf="!loading() && error()" class="st st--err">{{ error() }}</div>

    <form *ngIf="!loading()" (ngSubmit)="onSubmit()" class="frm" novalidate>
      <fieldset class="checks-group">
        <legend>Canales permitidos</legend>

        <div class="check">
          <input id="ch1" type="checkbox" [checked]="view.recibeLlamadas" (change)="onToggle('recibeLlamadas', $event)" />
          <label for="ch1">Recibe llamadas</label>
        </div>

        <div class="check">
          <input id="ch2" type="checkbox" [checked]="view.recibeMsm" (change)="onToggle('recibeMsm', $event)" />
          <label for="ch2">Recibe SMS</label>
        </div>

        <div class="check">
          <input id="ch3" type="checkbox" [checked]="view.recibeEmails" (change)="onToggle('recibeEmails', $event)" />
          <label for="ch3">Recibe emails</label>
        </div>

        <div class="check">
          <input id="ch4" type="checkbox" [checked]="view.recibeCartas" (change)="onToggle('recibeCartas', $event)" />
          <label for="ch4">Recibe cartas</label>
        </div>

        <div class="check">
          <input id="ch5" type="checkbox" [checked]="view.recibeRedesSociales" (change)="onToggle('recibeRedesSociales', $event)" />
          <label for="ch5">Recibe redes sociales</label>
        </div>
      </fieldset>

      <div class="actions">
        <button type="submit" class="btn btn--primary" [disabled]="saving()">
          {{ saving() ? 'Guardando…' : (mode() === 'new' ? 'Crear' : 'Guardar') }}
        </button>
        <a class="btn ghost" [routerLink]="['/hoja-vida/permisos-especiales', idDatosPersonales()]">Cancelar</a>
      </div>
    </form>
  `,
  styles: [`
    .hdr{margin-bottom:12px}
    .hdr .sub{color:#555; margin-top:4px; font-size:.95rem}
    .st{padding:12px 0}.st--err{color:#b00020}
    .frm{max-width:640px}
    fieldset.checks-group{border:1px solid #e5e7eb;border-radius:10px;padding:12px;margin:8px 0 14px;}
    fieldset.checks-group > legend{padding:0 6px;font-weight:600;color:#111;}
    .check{display:flex;align-items:center;gap:10px;padding:8px 2px}
    .check input[type="checkbox"]{width:20px;height:20px;margin:0;accent-color:#0d6efd;border-radius:4px;cursor:pointer;appearance:auto;-webkit-appearance:auto;}
    .check input[type="checkbox"]:focus-visible{outline:2px solid #84b5ff;outline-offset:2px;border-radius:4px;}
    .check label{cursor:pointer;user-select:none;font-weight:600;color:#222}
    .actions{margin-top:16px;display:flex;gap:8px}
  `]
})
export class PermisosEspecialesUpsertComponent implements OnInit {
  @Input() captureMode: boolean = false;
  @Input() idPersonaOverride: number | null | undefined;

  @Output() valueChange = new EventEmitter<ViewPermisos>();
  @Output() validChange = new EventEmitter<boolean>();

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private personasApi = inject(DatosPersonalesApi);
  private api = inject(PermisosEspecialesApi);

  idDatosPersonales = signal<number>(0);
  idPermiso = signal<number | null>(null);
  mode = signal<'new'|'edit'>('new');

  afiliadoNombre = signal<string>('');
  loading = signal<boolean>(false);
  saving = signal<boolean>(false);
  error = signal<string | null>(null);

  // NEW por defecto: todos true
  view: ViewPermisos = {
    recibeLlamadas: true,
    recibeMsm: true,
    recibeEmails: true,
    recibeCartas: true,
    recibeRedesSociales: true,
  };

  private smsFieldName: 'recibe_sms' | 'recibe_msm' = 'recibe_sms';

  ngOnInit(): void {
    const idFromRoute = Number(this.route.snapshot.paramMap.get('idDatosPersonales') ?? 0);
    const idFromInput = Number(this.idPersonaOverride ?? 0);
    const idPer = (Number.isFinite(idFromInput) && idFromInput > 0) ? idFromInput : idFromRoute;

    const idPParam = this.route.snapshot.paramMap.get('idPermiso');
    const idP = idPParam ? Number(idPParam) : null;

    this.idDatosPersonales.set(idPer);
    this.idPermiso.set(idP);
    this.mode.set(idP ? 'edit' : 'new');

    if (this.captureMode && (!idPer || idPer <= 0)) {
      this.afiliadoNombre.set('');
      this.loading.set(false);
      this.emitState();
      this.validChange.emit(true);
      return;
    }

    if (idPer > 0) this.loadAfiliadoNombre(idPer);

    if (!this.captureMode && !idPer) {
      this.error.set('Falta el ID de datos personales.');
      return;
    }

    if (this.mode() === 'edit' && idP) {
      this.loading.set(true);
      this.loadForEdit(idPer, idP);
    } else {
      this.emitState();
      this.validChange.emit(true);
    }
  }

  private toBool(v: any): boolean {
    if (typeof v === 'boolean') return v;
    if (typeof v === 'number') return v !== 0;
    if (typeof v === 'string') {
      const s = v.trim().toLowerCase();
      if (s === '1' || s === 'true' || s === 's' || s === 'y' || s === 'si' || s === 'sí') return true;
      if (s === '0' || s === 'false' || s === 'n' || s === 'no') return false;
      return !!v && v !== '0';
    }
    return !!v;
  }
  private b(v: boolean): number { return v ? 1 : 0; }

  private loadForEdit(idDatos: number, idPermiso: number): void {
    this.tryGetByPersona(idDatos).subscribe((row) => {
      if (row) { this.applyRow(row); return; }

      this.tryGetGeneric(idDatos, idPermiso).subscribe((row2) => {
        if (row2) { this.applyRow(row2); return; }

        this.tryGetById(idPermiso).subscribe((row3) => {
          if (row3) { this.applyRow(row3); return; }

          this.tryListPick(idDatos, idPermiso).subscribe((row4) => {
            if (row4) { this.applyRow(row4); return; }
            this.error.set('No se encontró el registro para editar.');
            this.loading.set(false);
            this.emitState();
            this.validChange.emit(true);
          });
        });
      });
    });
  }

  private applyRow(raw: any) {
    const r = this.unwrapHttp(raw);
    if ('recibe_msm' in (r ?? {})) this.smsFieldName = 'recibe_msm';
    else this.smsFieldName = 'recibe_sms';

    this.view = {
      recibeLlamadas: this.toBool(r?.recibe_llamadas ?? r?.recibeLlamadas ?? r?.llamadas ?? r?.permiteLlamadas),
      recibeMsm:       this.toBool(r?.recibe_sms ?? r?.recibe_msm ?? r?.recibeMsm ?? r?.sms ?? r?.permiteSms),
      recibeEmails:    this.toBool(r?.recibe_emails ?? r?.recibeEmails ?? r?.emails ?? r?.permiteEmails),
      recibeCartas:    this.toBool(r?.recibe_cartas ?? r?.recibeCartas ?? r?.cartas ?? r?.permiteCartas),
      recibeRedesSociales: this.toBool(r?.recibe_redes_sociales ?? r?.recibeRedesSociales ?? r?.redes ?? r?.permiteRedes),
    };
    this.loading.set(false);
    this.emitState();
    this.validChange.emit(true);
  }

  onSubmit() {
    if (this.saving()) return;
    this.error.set(null);
    this.saving.set(true);

    const idPer = this.idDatosPersonales();
    if (!idPer) {
      this.error.set('Falta el ID de datos personales.');
      this.saving.set(false);
      return;
    }

    // === Payload base (enviamos SIEMPRE ambos nombres para SMS) ===
    const smsVal = this.b(this.view.recibeMsm);
    const payloadBase: any = {
      id_datos_personal: idPer,
      recibe_llamadas: this.b(this.view.recibeLlamadas),
      recibe_emails: this.b(this.view.recibeEmails),
      recibe_cartas: this.b(this.view.recibeCartas),
      recibe_redes_sociales: this.b(this.view.recibeRedesSociales),
      // Ambos campos para máxima compatibilidad:
      recibe_sms: smsVal,
      recibe_msm: smsVal,
      id_permiso_especial: this.idPermiso() ?? undefined,
    };

    const idP = this.idPermiso();
    const obs = (this.mode() === 'new')
      ? this.flexCreate(idPer, payloadBase)
      : this.flexUpdate(idPer, idP!, payloadBase);

    obs.pipe(
      catchError((e) => {
        this.saving.set(false);
        const msg = (e?.error?.message ?? e?.message ?? e?.error ?? 'No fue posible guardar el registro.').toString();
        this.error.set(msg);
        return of(null);
      })
    ).subscribe((ok) => {
      if (!ok) return;
      this.saving.set(false);
      this.router.navigate(['/hoja-vida/permisos-especiales', idPer]);
    });
  }

  private loadAfiliadoNombre(idDatos: number) {
    const maybeGet = (this.personasApi as any)?.get;
    if (typeof maybeGet === 'function') {
      (maybeGet as Function).call(this.personasApi, idDatos).subscribe({
        next: (p: any) => { this.afiliadoNombre.set(this.buildNombre(p)); },
        error: () => this.fetchNombreFromList(idDatos),
      });
    } else {
      this.fetchNombreFromList(idDatos);
    }
  }
  private fetchNombreFromList(idDatos: number) {
    (this.personasApi as any).list?.({ size: 1000 })?.subscribe({
      next: (data: any) => {
        const arr: any[] = Array.isArray(data) ? data : (data?.items ?? data?.content ?? []);
        const p = (arr ?? []).find((it: any) =>
          [it?.id, it?.idDatosPersonal, it?.id_datos_personales, it?.idDatosPersonales]
            .some((v: any) => Number(v) === idDatos)
        );
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

  onToggle<K extends keyof ViewPermisos>(key: K, ev: Event) {
    const checked = (ev.target as HTMLInputElement).checked;
    this.view = { ...this.view, [key]: checked };
    this.emitState();
    this.validChange.emit(true);
  }

  private emitState() {
    this.valueChange.emit({ ...this.view });
  }

  private asObsOrNull(result: any): Observable<any | null> {
    if (!result && result !== 0) return of(null);
    if (isObservable(result)) return result as Observable<any>;
    if (result && typeof result.then === 'function') return from(result as Promise<any>);
    return of(result);
  }
  private unwrapHttp(resp: any): any {
    return (resp && typeof resp === 'object' && 'body' in resp) ? (resp.body ?? null) : resp;
  }

  private tryGetByPersona(idDatos: number): Observable<any | null> {
    const a: any = this.api as any;
    const candidate =
      (typeof a.getByPersona === 'function' && a.getByPersona(idDatos)) ||
      (typeof a.getForPersonal === 'function' && a.getForPersonal(idDatos)) ||
      (typeof a.findOneByPersona === 'function' && a.findOneByPersona(idDatos));
    return this.asObsOrNull(candidate);
  }
  private tryGetGeneric(idDatos: number, idPermiso: number): Observable<any | null> {
    const a: any = this.api as any;
    const candidate =
      (typeof a.get === 'function' && (a.get.length >= 2 ? a.get(idDatos, idPermiso) : a.get(idPermiso))) ||
      null;
    return this.asObsOrNull(candidate);
  }
  private tryGetById(idPermiso: number): Observable<any | null> {
    const a: any = this.api as any;
    const candidate =
      (typeof a.getById === 'function' && a.getById(idPermiso)) ||
      (typeof a.findById === 'function' && a.findById(idPermiso)) ||
      null;
    return this.asObsOrNull(candidate);
  }
  private tryListPick(idDatos: number, idPermiso: number): Observable<any | null> {
    const a: any = this.api as any;
    const candidate =
      (typeof a.listByPersona === 'function' && a.listByPersona(idDatos)) ||
      (typeof a.list === 'function' && (a.list.length >= 1 ? a.list({ idDatosPersonales: idDatos, id_datos_personales: idDatos }) : a.list())) ||
      null;

    return this.asObsOrNull(candidate).pipe(
      map((data: any) => {
        if (!data) return null;
        const arr: any[] = Array.isArray(data) ? data : (data?.items ?? data?.content ?? []);
        if (!arr?.length) return null;
        const found = arr.find((it: any) =>
          [it?.id, it?.idPermisoEspecial, it?.id_permiso_especial].some((v: any) => Number(v) === idPermiso)
        );
        return found ?? arr[0];
      })
    );
  }

  private flexCreate(idDatos: number, payload: any): Observable<any> {
    const a: any = this.api as any;
    const candidate =
      (typeof a.create === 'function' && (a.create.length >= 2 ? a.create(idDatos, payload) : a.create(payload))) ||
      (typeof a.createForPersona === 'function' && a.createForPersona(idDatos, payload)) ||
      (typeof a.save === 'function' && (a.save.length >= 2 ? a.save(idDatos, payload) : a.save(payload))) ||
      (typeof a.insert === 'function' && (a.insert.length >= 2 ? a.insert(idDatos, payload) : a.insert(payload))) ||
      null;
    return this.asObsOrNull(candidate).pipe(map(v => v ?? { ok: true }));
  }
  private flexUpdate(idDatos: number, idPermiso: number, payload: any): Observable<any> {
    const a: any = this.api as any;
    const candidate =
      (typeof a.update === 'function'
        ? (a.update.length === 3 ? a.update(idDatos, idPermiso, payload)
           : a.update.length === 2 ? a.update(idPermiso, payload)
           : a.update(payload))
        : null) ||
      (typeof a.updateForPersona === 'function' && a.updateForPersona(idDatos, idPermiso, payload)) ||
      (typeof a.updateById === 'function' && a.updateById(idPermiso, payload)) ||
      (typeof a.save === 'function' && a.save(payload)) ||
      null;
    return this.asObsOrNull(candidate).pipe(map(v => v ?? { ok: true }));
  }
}
