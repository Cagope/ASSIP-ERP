// src/app/features/hoja-vida/sarlaft/sarlaft-upsert.component.ts
import { Component, OnInit, inject, signal, computed, Input, ViewChild, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';

import { SarlaftApi, SarlaftRequest, SarlaftResponse } from './sarlaft.api';
import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';
import { CatalogosApi, CodigoNombreDTO } from '../../../shared/catalogos/catalogos.api';

type View = {
  exoneracionUiaf: boolean;
  fechaExoneracion: string;
  asociadoPeps: boolean;
  tipoPeps: string;
  observacionesPeps: string;
  fechaInicialPeps: string;
  fechaFinalPeps: string;
  familiaPeps: boolean;
  tipoFamiliaPeps: string;
  cedulaFamiliaPeps: string;
  nombreFamiliaPeps: string;
  codigoParentesco: string;
  monedaExtranjera: boolean;
  observacionMonedaExtranjera: string;
  cuentaExtranjero: boolean;
  tipoMonedaExtranjera: string;
  numeroCuentaExtranjero: string;
  nombreBancoExtranjero: string;
  ciudadCuentaExtranjero: string;
  paisCuentaExtranjero: string;
};

@Component({
  selector: 'app-sarlaft-upsert',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sarlaft-upsert.component.html',
  styleUrls: ['./sarlaft-upsert.component.scss'],
})
export class SarlaftUpsertComponent implements OnInit {
  // === Modo wizard ===
  @Input() captureMode: boolean = false;
  @Input() idPersonaOverride: number | null | undefined;

  // Exponer el NgForm como FormGroup para el wizard
  @ViewChild('frm', { static: true }) frm!: NgForm;
  public get form(): FormGroup | null {
    return (this.frm?.form as unknown as FormGroup) ?? null;
  }

  // === Salidas para el wizard ===
  @Output() valueChange = new EventEmitter<View>();
  @Output() validChange = new EventEmitter<boolean>();

  // servicios
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private api = inject(SarlaftApi);
  private personasApi = inject(DatosPersonalesApi);
  private cat = inject(CatalogosApi);

  // estado ruta
  idDatosPersonales = signal<number>(0);
  idSarlaft = signal<number | null>(null);
  mode = signal<'new' | 'edit'>('new');

  // ui
  afiliadoNombre = signal<string>('');
  loading = signal<boolean>(true);
  guardando = signal<boolean>(false);
  errorMsg = signal<string | null>(null);

  // catálogos
  tiposPeps = signal<CodigoNombreDTO[]>([]);
  parentescos = signal<CodigoNombreDTO[]>([]);

  // modelo
  view = signal<View>({
    exoneracionUiaf: false,
    fechaExoneracion: '',
    asociadoPeps: false,
    tipoPeps: '',
    observacionesPeps: '',
    fechaInicialPeps: '',
    fechaFinalPeps: '',
    familiaPeps: false,
    tipoFamiliaPeps: '',
    cedulaFamiliaPeps: '',
    nombreFamiliaPeps: '',
    codigoParentesco: '',
    monedaExtranjera: false,
    observacionMonedaExtranjera: '',
    cuentaExtranjero: false,
    tipoMonedaExtranjera: '',
    numeroCuentaExtranjero: '',
    nombreBancoExtranjero: '',
    ciudadCuentaExtranjero: '',
    paisCuentaExtranjero: '',
  });

  titulo = computed(() => (this.mode() === 'new' ? 'Nuevo SARLAFT' : 'Editar SARLAFT'));

  private todayStr(): string {
    try { return new Date().toISOString().slice(0, 10); }
    catch { return new Date().toLocaleDateString('en-CA'); }
  }

  ngOnInit(): void {
    // IDs desde ruta + override del wizard
    const idFromRoute = Number(this.route.snapshot.paramMap.get('idDatosPersonales') ?? 0);
    const idFromInput = Number(this.idPersonaOverride ?? 0);
    const idPer = (Number.isFinite(idFromInput) && idFromInput > 0) ? idFromInput : idFromRoute;

    const idSarParam = this.route.snapshot.paramMap.get('idSarlaft');
    const idSar = idSarParam ? Number(idSarParam) : null;

    this.idDatosPersonales.set(idPer);
    this.idSarlaft.set(idSar);
    this.mode.set(idSar ? 'edit' : 'new');

    // catálogos
    this.cat.tiposPeps().subscribe({ next: (rows) => this.tiposPeps.set(rows ?? []), error: () => this.tiposPeps.set([]) });
    this.cat.parentescos().subscribe({ next: (rows) => this.parentescos.set(rows ?? []), error: () => this.parentescos.set([]) });

    // wizard sin id -> no GETs ni error
    if (this.captureMode && (!idPer || idPer <= 0)) {
      this.afiliadoNombre.set('');
      this.loading.set(false);
      this.emitAll(); // emite estado inicial
      return;
    }

    // afiliado si hay id
    if (idPer > 0) this.loadAfiliadoNombre(idPer);

    if (!this.captureMode && !idPer) {
      this.errorMsg.set('Falta el ID de datos personales.');
      this.loading.set(false);
      this.emitAll();
      return;
    }

    // cargar existente por persona, o quedar en NEW
    if (idPer > 0) {
      this.loading.set(true);
      this.api.getByPersona(idPer).subscribe({
        next: (resp) => {
          const row: SarlaftResponse | null = resp?.status === 204 ? null : (resp.body ?? null);
          if (!row) {
            this.mode.set('new');
            this.loading.set(false);
            this.emitAll();
            return;
          }
          this.idSarlaft.set(row.id_sarlaft ?? null);
          this.mode.set('edit');
          this.applyResponse(row);
          this.loading.set(false);
          this.emitAll();
        },
        error: () => {
          if (this.captureMode) {
            this.mode.set('new');
            this.loading.set(false);
            this.emitAll();
            return;
          }
          this.errorMsg.set('No fue posible cargar el registro SARLAFT.');
          this.loading.set(false);
          this.emitAll();
        },
      });
    } else {
      this.loading.set(false);
      this.emitAll();
    }
  }

  // ===== Emisiones al wizard =====
  private isValidForWizard(v: View): boolean {
    // En wizard no bloqueamos por reglas complejas: basta con que el objeto exista.
    // Si quieres bloquear “Siguiente” por reglas reales, evalúalas aquí y devuelve false.
    return true;
  }
  private emitAll(): void {
    const v = this.view();
    this.valueChange.emit(v);
    this.validChange.emit(this.isValidForWizard(v));
  }

  // ===== helpers =====
  private loadAfiliadoNombre(id: number): void {
    if (!id) { this.afiliadoNombre.set(''); return; }
    (this.personasApi as any).get?.(id)?.subscribe({
      next: (p: any) => {
        const nombres = (p?.nombres ?? `${p?.primerNombre ?? ''} ${p?.segundoNombre ?? ''}`).toString().trim();
        const apellidos = (p?.apellidos ?? `${p?.primerApellido ?? ''} ${p?.segundoApellido ?? ''}`).toString().trim();
        this.afiliadoNombre.set(`${nombres} ${apellidos}`.replace(/\s+/g, ' ').trim());
        this.emitAll();
      },
      error: () => { this.afiliadoNombre.set(''); this.emitAll(); },
    });
  }

  private dstr(d?: string | null): string {
    if (!d) return '';
    return String(d).slice(0, 10);
  }

  private applyResponse(r: SarlaftResponse): void {
    this.view.set({
      exoneracionUiaf: !!r.exoneracion_uiaf,
      fechaExoneracion: this.dstr(r.fecha_exoneracion),
      asociadoPeps: !!r.asociado_peps,
      tipoPeps: r.tipo_peps ?? '',
      observacionesPeps: r.observaciones_peps ?? '',
      fechaInicialPeps: this.dstr(r.fecha_inicial_peps),
      fechaFinalPeps: this.dstr(r.fecha_final_peps),
      familiaPeps: !!r.familia_peps,
      tipoFamiliaPeps: r.tipo_familia_peps ?? '',
      cedulaFamiliaPeps: r.cedula_familia_peps ?? '',
      nombreFamiliaPeps: r.nombre_familia_peps ?? '',
      codigoParentesco: r.codigo_parentesco ?? '',
      monedaExtranjera: !!r.moneda_extranjera,
      observacionMonedaExtranjera: r.observacion_moneda_extranjera ?? '',
      cuentaExtranjero: !!r.cuenta_extranjero,
      tipoMonedaExtranjera: r.tipo_moneda_extranjera ?? '',
      numeroCuentaExtranjero: r.numero_cuenta_extranjero ?? '',
      nombreBancoExtranjero: r.nombre_banco_extranjero ?? '',
      ciudadCuentaExtranjero: r.ciudad_cuenta_extranjero ?? '',
      paisCuentaExtranjero: r.pais_cuenta_extranjero ?? '',
    });
  }

  // boton cancelar (CRUD)
  goBack(): void {
    const id = this.idDatosPersonales();
    this.router.navigate(['/hoja-vida/sarlaft', id]);
  }

  // ===== guardar CRUD (oculto en wizard por CSS) =====
  save(form: NgForm): void {
    if (this.guardando()) return;
    this.errorMsg.set(null);

    const idDatos = this.idDatosPersonales();
    if (!idDatos) {
      this.errorMsg.set('Falta el ID de datos personales.');
      return;
    }

    const v = this.view();
    const today = this.todayStr();

    const req: SarlaftRequest = {
      exoneracionUiaf: !!v.exoneracionUiaf,
      fechaExoneracion: v.exoneracionUiaf ? (v.fechaExoneracion || today) : today,
      asociadoPeps: !!v.asociadoPeps,
      tipoPeps: v.asociadoPeps ? (v.tipoPeps || '') : '000',
      observacionesPeps: v.asociadoPeps ? (v.observacionesPeps || '') : 'No aplica',
      fechaInicialPeps: v.asociadoPeps ? (v.fechaInicialPeps || today) : today,
      fechaFinalPeps: v.asociadoPeps ? (v.fechaFinalPeps || today) : today,
      familiaPeps: !!v.familiaPeps,
      tipoFamiliaPeps: v.familiaPeps ? (v.tipoFamiliaPeps || '') : '000',
      cedulaFamiliaPeps: v.familiaPeps ? (v.cedulaFamiliaPeps || '') : 'No aplica',
      nombreFamiliaPeps: v.familiaPeps ? (v.nombreFamiliaPeps || '') : 'No aplica',
      codigoParentesco: v.familiaPeps ? (v.codigoParentesco || '') : '0',
      monedaExtranjera: !!v.monedaExtranjera,
      observacionMonedaExtranjera: v.monedaExtranjera ? (v.observacionMonedaExtranjera || '') : 'NO POSEE',
      cuentaExtranjero: !!v.cuentaExtranjero,
      tipoMonedaExtranjera: v.cuentaExtranjero ? (v.tipoMonedaExtranjera || '') : 'NO POSEE',
      numeroCuentaExtranjero: v.cuentaExtranjero ? (v.numeroCuentaExtranjero || '') : 'NO POSEE',
      nombreBancoExtranjero: v.cuentaExtranjero ? (v.nombreBancoExtranjero || '') : 'NO POSEE',
      ciudadCuentaExtranjero: v.cuentaExtranjero ? (v.ciudadCuentaExtranjero || '') : 'NO POSEE',
      paisCuentaExtranjero: v.cuentaExtranjero ? (v.paisCuentaExtranjero || '') : 'NO POSEE',
    };

    // Validaciones condicionales mínimas
    if (v.asociadoPeps && (!req.tipoPeps || !req.observacionesPeps || !req.fechaInicialPeps || !req.fechaFinalPeps)) {
      this.errorMsg.set('Completa los campos obligatorios del bloque: El asociado es PEP.');
      return;
    }
    if (v.familiaPeps && (!req.tipoFamiliaPeps || !req.cedulaFamiliaPeps || !req.nombreFamiliaPeps || !req.codigoParentesco)) {
      this.errorMsg.set('Completa los campos obligatorios del bloque: El asociado tiene familiar PEP.');
      return;
    }
    if (v.monedaExtranjera && !req.observacionMonedaExtranjera) {
      this.errorMsg.set('Completa la observación de moneda extranjera.');
      return;
    }
    if (v.cuentaExtranjero && (!req.tipoMonedaExtranjera || !req.numeroCuentaExtranjero || !req.nombreBancoExtranjero || !req.ciudadCuentaExtranjero || !req.paisCuentaExtranjero)) {
      this.errorMsg.set('Completa todos los campos del bloque de cuentas en el extranjero.');
      return;
    }

    this.guardando.set(true);

    const obs: Observable<any> =
      (this.mode() === 'edit' && this.idSarlaft())
        ? (this.api.update(idDatos, this.idSarlaft()!, req) as unknown as Observable<any>)
        : (this.api.create(idDatos, req) as unknown as Observable<any>);

    obs.subscribe({
      next: () => {
        this.guardando.set(false);
        this.router.navigate(['/hoja-vida/sarlaft', idDatos]);
      },
      error: (e: any) => {
        this.guardando.set(false);
        const msg = (e?.error?.message ?? e?.error ?? e?.message ?? 'No fue posible guardar.').toString();
        this.errorMsg.set(msg);
      },
    });
  }

  // toggle helper
  onToggle(key: keyof View, ev: Event): void {
    const checked = (ev.target as HTMLInputElement).checked;
    const today = this.todayStr();

    this.view.update(v => {
      const next = { ...v, [key]: checked };

      if (key === 'exoneracionUiaf') {
        next.fechaExoneracion = checked ? (v.fechaExoneracion || today) : '';
      }
      if (key === 'asociadoPeps') {
        if (checked) {
          if (!v.fechaInicialPeps) next.fechaInicialPeps = today;
          if (!v.fechaFinalPeps) next.fechaFinalPeps = today;
        } else {
          next.tipoPeps = '';
          next.observacionesPeps = '';
          next.fechaInicialPeps = '';
          next.fechaFinalPeps = '';
        }
      }
      if (key === 'familiaPeps' && !checked) {
        next.tipoFamiliaPeps = '';
        next.cedulaFamiliaPeps = '';
        next.nombreFamiliaPeps = '';
        next.codigoParentesco = '';
      }
      if (key === 'monedaExtranjera' && !checked) {
        next.observacionMonedaExtranjera = '';
      }
      if (key === 'cuentaExtranjero' && !checked) {
        next.tipoMonedaExtranjera = '';
        next.numeroCuentaExtranjero = '';
        next.nombreBancoExtranjero = '';
        next.ciudadCuentaExtranjero = '';
        next.paisCuentaExtranjero = '';
      }
      return next;
    });

    this.emitAll();
  }
}
