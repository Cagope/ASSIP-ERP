import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ActivatedRoute,
  ActivationEnd,
  NavigationEnd,
  Router,
  RouterOutlet
} from '@angular/router';
import { filter } from 'rxjs/operators';
import { CapturaState } from './captura-state.service';
import { CapturaApi } from './captura.api';
import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';
import { calcularDvNit } from '../../../shared/utils/calcular-dv-nit';
import { firstValueFrom } from 'rxjs';

const STEPS = [
  { path: 'datos-personales', label: 'Datos personales' }, // 0
  { path: 'ubicaciones',      label: 'Ubicaciones' },      // 1
  { path: 'laborales',        label: 'Laborales' },        // 2
  { path: 'financieros',      label: 'Financieros' },      // 3
  { path: 'datos-familiares', label: 'Familiares' },       // 4
  { path: 'referencias',      label: 'Referencias' },      // 5
  { path: 'sarlaft',          label: 'SARLAFT' },          // 6
  { path: 'permisos',         label: 'Permisos' },         // 7
  { path: 'resumen',          label: 'Resumen' },          // 8
];

@Component({
  standalone: true,
  selector: 'app-captura-shell',
  imports: [CommonModule, RouterOutlet],
  template: `
  <div class="wizard">
    <div class="wizard-header">
      <div class="title">Captura Hoja de Vida</div>
      <div class="subtitle">ID Captura: {{ capturaId() }}</div>
      <div class="steps">
        <div *ngFor="let s of steps; let i = index"
             class="step"
             [class.active]="i === currentIndex()"
             [class.done]="i < currentIndex()">
          <span class="dot">{{ i+1 }}</span>
          <span class="label">{{ s.label }}</span>
        </div>
      </div>
    </div>

    <div class="wizard-body">
      <router-outlet></router-outlet>
    </div>

    <div class="wizard-footer">
      <button class="btn" (click)="prev()" [disabled]="currentIndex() === 0">Anterior</button>
      <span class="spacer"></span>

      <button class="btn primary"
              *ngIf="currentIndex() < steps.length - 1"
              (click)="next()"
              [disabled]="!isCurrentStepValid() || advancing()">
        {{ advancing() ? 'Guardando…' : 'Siguiente' }}
      </button>

      <button class="btn success"
              *ngIf="currentIndex() === steps.length - 1"
              (click)="finalizar()"
              [disabled]="advancing()">
        {{ advancing() ? 'Guardando…' : 'Guardar en sistema' }}
      </button>
    </div>
  </div>
  `,
  styles: [`
    .wizard { background:#fff; border-radius:8px; box-shadow:0 2px 8px rgba(0,0,0,.06); }
    .wizard-header { padding:12px 16px; border-bottom:1px solid #eee; }
    .title { font-weight:600; font-size:18px; }
    .subtitle { color:#666; margin-top:4px; }
    .steps { display:flex; gap:10px; margin-top:10px; flex-wrap:wrap; }
    .step { display:flex; align-items:center; gap:6px; opacity:0.7; }
    .step.active { opacity:1; font-weight:600; }
    .step.done { opacity:0.9; }
    .dot { width:22px; height:22px; border-radius:50%; background:#e9ecef; display:inline-flex; align-items:center; justify-content:center; }
    .wizard-body { padding:16px; }
    .wizard-footer { padding:12px 16px; border-top:1px solid #eee; display:flex; align-items:center; }
    .spacer { flex:1; }
    .btn { padding:6px 12px; border:1px solid #ccc; border-radius:6px; background:#fafafa; cursor:pointer; }
    .btn.primary { background:#0d6efd; color:#fff; border-color:#0d6efd; }
    .btn.success { background:#198754; color:#fff; border-color:#198754; }
  `]
})
export class CapturaShellComponent implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private state = inject<CapturaState>(CapturaState);
  private api = inject<CapturaApi>(CapturaApi);
  private dpApi = inject(DatosPersonalesApi);

  steps = STEPS;

  private _currentIndex = signal<number>(0);
  currentIndex = this._currentIndex.asReadonly();

  capturaId = this.state.capturaId;

  // bloquea el botón Siguiente/Finalizar mientras guarda
  advancing = signal(false);

  isCurrentStepValid = computed(() => {
    const idx = this.currentIndex();
    return !!this.state.stepValid()[idx];
  });

  ngOnInit(): void {
    const capture = () => {
      let child = this.route.firstChild;
      while (child?.firstChild) child = child.firstChild;
      const path = child?.routeConfig?.path ?? 'datos-personales';
      const idx = this.steps.findIndex(s => s.path === path);
      const safeIdx = Math.max(0, idx);
      this._currentIndex.set(safeIdx);
      this.state.setStep(safeIdx);
    };

    capture();
    this.router.events
      .pipe(filter((e): e is ActivationEnd => e instanceof ActivationEnd))
      .subscribe(capture);
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe(capture);
  }

  // ===== helpers =====

  /** Normaliza Datos Personales (DV, cabezaFamilia, etc.) */
  private normalizeDatosPersonales(src: any): any {
    const base = { ...(src ?? {}) };

    const tp  = (base.tipoPersona ?? '').toString().trim();
    const gen = (base.codigoGenero ?? '').toString().trim();
    const sel = (base.cabezaFamilia ?? '').toString().trim();
    base.cabezaFamilia = (tp === '2' || gen === '1' || gen === '3') ? '0' : (sel === '1' ? '1' : '0');

    delete base.fechaApertura;
    delete base.fechaActualiza;

    const requeridoDV = (base.tipoPersona === '2') || (base.tipoPersona === '1' && !!base.tieneRut);
    if (requeridoDV) {
      let dv = (base.digitoVerificacion ?? '').toString().trim();
      if (!/^\d{1,2}$/.test(dv)) {
        const calc = calcularDvNit?.((base.documento ?? '').toString().trim());
        dv = (calc ?? '1').toString();
      }
      base.digitoVerificacion = dv;
    } else {
      delete base.digitoVerificacion;
    }
    return base;
  }

  /** Lee ID desde query o desde payload */
  private getPersonaIdStrict(): number | null {
    const rootQ = this.router.routerState.snapshot.root.queryParams;
    const q = Number(rootQ?.['idDatosPersonales'] ?? 0);
    if (q > 0) return q;

    const p: any = this.state.payload?.() ?? {};
    const id = Number(p?.datosPersonales?.id_datos_personal ?? 0);
    return id > 0 ? id : null;
  }

  /** Crea el ID si aún no existe */
  private async ensurePersonaId(): Promise<number | null> {
    let id = this.getPersonaIdStrict();
    if (id) return id;

    try { await this.state.runSaver(0); } catch {}
    id = this.getPersonaIdStrict();
    if (id) return id;

    const snap = (this.state.payload()?.datosPersonales) ?? null;
    if (snap) {
      try {
        const payload = this.normalizeDatosPersonales(snap);
        const resp: any = await firstValueFrom(this.dpApi.create(payload));
        const newId =
          (typeof resp === 'number') ? resp :
          resp?.id ?? resp?.idDatosPersonal ?? resp?.id_datos_personal ?? null;

        if (newId) {
          id = Number(newId);

          await this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { idDatosPersonales: id },
            queryParamsHandling: 'merge',
            replaceUrl: true,
          });

          this.state.payload.update((curr: any) => {
            const p = curr ?? {};
            const dp = p.datosPersonales ?? {};
            dp.id_datos_personal = id;
            return { ...p, datosPersonales: dp };
          });

          return id;
        }
      } catch (e) {
        console.error('[ensurePersonaId] Fallback create falló:', e);
      }
    }
    return null;
  }

  // ===== navegación =====

  async prev(): Promise<void> {
    const i = this.currentIndex();
    if (i <= 0) return;

    const personaId = this.getPersonaIdStrict();
    await this.router.navigate([this.steps[i - 1].path], {
      relativeTo: this.route,
      queryParams: personaId ? { idDatosPersonales: personaId } : {},
      queryParamsHandling: 'merge',
    });
  }

  async next(): Promise<void> {
    const i = this.currentIndex();
    if (i >= this.steps.length - 1) return;
    if (!this.isCurrentStepValid()) return;
    if (this.advancing()) return;

    this.advancing.set(true);
    try {
      const personaId = this.getPersonaIdStrict();
      if (personaId) {
        await this.router.navigate([], {
          relativeTo: this.route,
          queryParams: { idDatosPersonales: personaId },
          queryParamsHandling: 'merge',
          replaceUrl: true,
        });
      }

      const ok = await this.state.runSaver(i);
      if (!ok) { this.advancing.set(false); return; }

      await this.router.navigate([this.steps[i + 1].path], {
        relativeTo: this.route,
        queryParams: personaId ? { idDatosPersonales: personaId } : {},
        queryParamsHandling: 'merge',
      });
    } finally {
      this.advancing.set(false);
    }
  }

  // ===== Normalizadores =====
  private mapStr(obj: any, keys: string[], def = ''): string {
    for (const k of keys) {
      const v = obj?.[k];
      if (typeof v === 'string') return v;
    }
    return def;
  }
  private mapNum(obj: any, keys: string[]): number | null {
    for (const k of keys) {
      const v = obj?.[k];
      if (v !== undefined && v !== null && v !== '') return Number(v);
    }
    return null;
  }
  private mapBool(obj: any, keys: string[], def = false): boolean {
    for (const k of keys) {
      const v = obj?.[k];
      if (typeof v === 'boolean') return v;
      if (typeof v === 'string') {
        const t = v.trim().toLowerCase();
        if (['true','1','si','sí','verdadero','y','yes'].includes(t)) return true;
        if (['false','0','no','falso','n'].includes(t)) return false;
      }
      if (typeof v === 'number') return v === 1;
    }
    return def;
  }
  private onlyDigits(s: any, max?: number): string {
    const t = (s ?? '').toString().replace(/\D+/g,'');
    return max ? t.slice(0, max) : t;
  }
  private digitsOrNull(s: any, max: number): string | null {
    const t = this.onlyDigits(s, max);
    return t.length ? t : null;
  }
  private fecha(v: any): string | null {
    const s = (v ?? '').toString().trim();
    if (!s) return null;
    const d = s.slice(0, 10);
    return /^\d{4}-\d{2}-\d{2}$/.test(d) ? d : null;
  }

  private normalizeUbicaciones(src: any): any {
    const out = {
      direccion:       this.mapStr(src, ['direccion']),
      barrio:          this.mapStr(src, ['barrio']),
      telefono:        this.digitsOrNull(this.mapStr(src, ['telefono','teléfono']), 7),
      celular_uno:     this.digitsOrNull(this.mapStr(src, ['celular_uno']), 10),
      celular_dos:     this.digitsOrNull(this.mapStr(src, ['celular_dos']), 10),
      correo:          this.mapStr(src, ['correo']),
      id_pais:         this.mapNum(src, ['id_pais']),
      id_departamento: this.mapNum(src, ['id_departamento']),
      id_ciudad:       this.mapNum(src, ['id_ciudad']),
      id_sub_zona:     this.mapNum(src, ['id_sub_zona','idSubZona','id_zona'])
    };
    if (!out.direccion) out.direccion = 'N/A';
    if (!out.barrio) out.barrio = 'N/A';
    return out;
  }

  private normalizeLaborales(src: any): any {
    const out = {
      nombreEmpresa:     this.mapStr(src, ['nombreEmpresa']),
      direccion:         this.mapStr(src, ['direccion']),
      idPais:            this.mapNum(src, ['idPais']),
      idDepartamento:    this.mapNum(src, ['idDepartamento']),
      idCiudad:          this.mapNum(src, ['idCiudad']),
      telefonoEmpresa:   this.digitsOrNull(this.mapStr(src, ['telefonoEmpresa','teléfonoEmpresa']), 7),
      celularEmpresa:    this.digitsOrNull(this.mapStr(src, ['celularEmpresa']), 10),
      correoEmpresa:     this.mapStr(src, ['correoEmpresa']),
      codigoTipoEmpresa: this.mapStr(src, ['codigoTipoEmpresa']),
      empleadoEntidad:   this.mapBool(src, ['empleadoEntidad'], false),
      codigoTipoContrato:this.mapStr(src, ['codigoTipoContrato']),
      codigoJornada:     this.mapStr(src, ['codigoJornada','códigoJornada']),
      nombreContacto:    this.mapStr(src, ['nombreContacto']),
      celularContacto:   this.digitsOrNull(this.mapStr(src, ['celularContacto']), 10),
      fechaVinculacion:  this.fecha(src?.fechaVinculacion),
    };
    if (!out.nombreEmpresa) out.nombreEmpresa = 'INDEPENDIENTE';
    if (!out.direccion) out.direccion = 'N/A';
    if (!out.nombreContacto) out.nombreContacto = 'N/A';
    if (typeof out.empleadoEntidad !== 'boolean') out.empleadoEntidad = false;
    return out;
  }

  private normalizeFinancieros(src: any): any {
    const out = {
      valorSalario:            this.mapNum(src, ['valorSalario']) ?? 0,
      valorPension:            this.mapNum(src, ['valorPension','valorPensión']) ?? 0,
      ingresosArriendo:        this.mapNum(src, ['ingresosArriendo']) ?? 0,
      ingresosComisiones:      this.mapNum(src, ['ingresosComisiones']) ?? 0,
      otrosIngresos:           this.mapNum(src, ['otrosIngresos']) ?? 0,
      comentarioOtrosIngresos: this.mapStr(src, ['comentarioOtrosIngresos']),
      egresosFamiliares:       this.mapNum(src, ['egresosFamiliares']) ?? 0,
      egresosArriendo:         this.mapNum(src, ['egresosArriendo']) ?? 0,
      egresosCredito:          this.mapNum(src, ['egresosCredito','egresosCrédito']) ?? 0,
      otrosEgresos:            this.mapNum(src, ['otrosEgresos']) ?? 0,
      comentarioOtrosEgresos:  this.mapStr(src, ['comentarioOtrosEgresos']),
      totalActivos:            this.mapNum(src, ['totalActivos']) ?? 0,
      totalPasivos:            this.mapNum(src, ['totalPasivos']) ?? 0,
      origenFondos:            this.mapStr(src, ['origenFondos']),
      relacionFinanciera:      this.mapStr(src, ['relacionFinanciera']),
      deudaRelacionFinanciera: this.mapNum(src, ['deudaRelacionFinanciera']) ?? 0,
    };
    if (out.comentarioOtrosIngresos == null) out.comentarioOtrosIngresos = '';
    if (out.comentarioOtrosEgresos == null) out.comentarioOtrosEgresos = '';
    if (out.origenFondos == null) out.origenFondos = '';
    if (out.relacionFinanciera == null) out.relacionFinanciera = '';
    return out;
  }

  private normalizeDatosFamiliares(src: any): any {
    const out = {
      codigo_parentesco:        this.mapStr(src, ['codigo_parentesco']),
      nombre_datos_familiar:    this.mapStr(src, ['nombre_datos_familiar']),
      documento_datos_familiar: this.mapStr(src, ['documento_datos_familiar']),
      telefono_datos_familiar:  this.digitsOrNull(this.mapStr(src, ['telefono_datos_familiar']), 7),
      celular_datos_familiar:   this.digitsOrNull(this.mapStr(src, ['celular_datos_familiar']), 10),
      direccion_datos_familiar: this.mapStr(src, ['direccion_datos_familiar']),
      id_departamento:          this.mapNum(src, ['id_departamento']),
      id_ciudad:                this.mapNum(src, ['id_ciudad']),
      fecha_nacimiento:         this.fecha(src?.fecha_nacimiento),
      ingresos_datos_familiar:  this.mapNum(src, ['ingresos_datos_familiar']) ?? 0,
      egresos_datos_familiar:   this.mapNum(src, ['egresos_datos_familiar']) ?? 0,
      referencia_familiar:      this.mapBool(src, ['referencia_familiar'], false),
    };
    if (!out.codigo_parentesco) out.codigo_parentesco = '0';
    if (!out.nombre_datos_familiar) out.nombre_datos_familiar = 'N/A';
    if (!out.direccion_datos_familiar) out.direccion_datos_familiar = 'N/A';
    if (!out.fecha_nacimiento) out.fecha_nacimiento = '2000-01-01';
    if (out.id_departamento == null) out.id_departamento = 0;
    if (out.id_ciudad == null) out.id_ciudad = 0;
    return out;
  }

  private normalizeReferencias(src: any): any {
    const out = {
      nombre_referencia_personal:   this.mapStr(src, ['nombre_referencia_personal']),
      direccion_referencia_personal:this.mapStr(src, ['direccion_referencia_personal']),
      id_departamento:              this.mapNum(src, ['id_departamento']),
      id_ciudad:                    this.mapNum(src, ['id_ciudad']),
      telefono_referencia_personal: this.digitsOrNull(this.mapStr(src, ['telefono_referencia_personal']), 7),
      celular_referencia_personal:  this.digitsOrNull(this.mapStr(src, ['celular_referencia_personal']), 10),
    };
    if (!out.nombre_referencia_personal) out.nombre_referencia_personal = 'N/A';
    if (!out.direccion_referencia_personal) out.direccion_referencia_personal = 'N/A';
    return out;
  }

  private normalizeSarlaft(src: any): any {
    const out: any = {
      exoneracionUiaf:             this.mapBool(src, ['exoneracionUiaf','exoneraciónUiaf'], false),
      fechaExoneracion:            this.fecha(src?.fechaExoneracion),
      asociadoPeps:                this.mapBool(src, ['asociadoPeps'], false),
      tipoPeps:                    this.mapStr(src, ['tipoPeps']),
      observacionesPeps:           this.mapStr(src, ['observacionesPeps']),
      fechaInicialPeps:            this.fecha(src?.fechaInicialPeps),
      fechaFinalPeps:              this.fecha(src?.fechaFinalPeps),
      familiaPeps:                 this.mapBool(src, ['familiaPeps'], false),
      tipoFamiliaPeps:             this.mapStr(src, ['tipoFamiliaPeps']),
      cedulaFamiliaPeps:           this.mapStr(src, ['cedulaFamiliaPeps','cédulaFamiliaPeps']),
      codigoParentesco:            this.mapStr(src, ['codigoParentesco']),
      nombreFamiliaPeps:           this.mapStr(src, ['nombreFamiliaPeps']),
      monedaExtranjera:            this.mapBool(src, ['monedaExtranjera'], false),
      observacionMonedaExtranjera: this.mapStr(src, ['observacionMonedaExtranjera']),
      cuentaExtranjero:            this.mapBool(src, ['cuentaExtranjero'], false),
      tipoMonedaExtranjera:        this.mapStr(src, ['tipoMonedaExtranjera']),
      numeroCuentaExtranjero:      this.mapStr(src, ['numeroCuentaExtranjero']),
      nombreBancoExtranjero:       this.mapStr(src, ['nombreBancoExtranjero']),
      ciudadCuentaExtranjero:      this.mapStr(src, ['ciudadCuentaExtranjero']),
      paisCuentaExtranjero:        this.mapStr(src, ['paisCuentaExtranjero']),
    };

    const vacios = [
      'observacionesPeps',
      'tipoMonedaExtranjera',
      'numeroCuentaExtranjero',
      'nombreBancoExtranjero',
      'ciudadCuentaExtranjero',
      'paisCuentaExtranjero'
    ];
    for (const k of vacios) {
      if (out[k] == null) out[k] = '';
    }

    return out;
  }

  private normalizePermisos(src: any): any {
    return {
      recibeLlamadas:      this.mapBool(src, ['recibeLlamadas'], false),
      recibeMsm:           this.mapBool(src, ['recibeMsm'], false),
      recibeEmails:        this.mapBool(src, ['recibeEmails'], false),
      recibeCartas:        this.mapBool(src, ['recibeCartas'], false),
      recibeRedesSociales: this.mapBool(src, ['recibeRedesSociales'], false),
    };
  }

  private normalizePayloadForFinalizar(snap: any) {
    const out: any = {};
    if (snap?.ubicaciones)     out.ubicaciones     = this.normalizeUbicaciones(snap.ubicaciones);
    if (snap?.laborales)       out.laborales       = this.normalizeLaborales(snap.laborales);
    if (snap?.financieros)     out.financieros     = this.normalizeFinancieros(snap.financieros);
    if (snap?.datosFamiliares) out.datosFamiliares = this.normalizeDatosFamiliares(snap.datosFamiliares);
    if (snap?.referencias)     out.referencias     = this.normalizeReferencias(snap.referencias);
    if (snap?.sarlaft)         out.sarlaft         = this.normalizeSarlaft(snap.sarlaft);
    if (snap?.permisos)        out.permisos        = this.normalizePermisos(snap.permisos);
    return out;
  }

  // ====== FINALIZAR ======
  async finalizar(): Promise<void> {
    if (this.advancing()) return;
    this.advancing.set(true);

    try {
      const idPersona = await this.ensurePersonaId();
      if (!idPersona) {
        alert('No se puede finalizar: falta el ID de datos personales.');
        return;
      }

      try { await this.state.runSaver(this.currentIndex()); } catch {}

      const snap: any = this.state.payload() ?? {};
      const norm: any = this.normalizePayloadForFinalizar(snap);

      // DTO transaccional: incluimos el ID dentro de datosPersonales y a nivel raíz.
      const dto: any = {
        id_datos_personal: Number(idPersona),
        datosPersonales: { id_datos_personal: Number(idPersona) },
        ...norm,
      };

      console.debug('[Finalizar][DTO]', dto);

      this.api.finalizar(dto).subscribe({
        next: (res: any) => {
          const idOk = res?.idDatosPersonal ?? idPersona;
          alert(`Guardado con éxito. idDatosPersonal: ${idOk}`);
          this.state.reset();
          this.router.navigate(['/hoja-vida/datos-personales']);
        },
        error: async (err: any) => {
          let msg = 'Error al procesar la captura final.';
          try {
            if (err?.error instanceof Blob) {
              const t = await err.error.text();
              if (t) msg = t;
            } else if (typeof err?.error === 'string') {
              msg = err.error;
            } else if (err?.error?.message) {
              msg = err.error.message;
            }
          } catch {}
          console.error('[Finalizar] error:', err);
          alert(msg);
        }
      });
    } finally {
      this.advancing.set(false);
    }
  }
}
