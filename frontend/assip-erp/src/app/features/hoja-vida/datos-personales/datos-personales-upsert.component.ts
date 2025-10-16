import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { DatosPersonalesApi, DatosPersonaCreate, DatosPersonaUpdate, DatosPersonalesDetail } from './datos-personales.api';
import { CatalogosApi, IdNombreDTO, CodigoNombreDTO, TipoRegimenDTO } from '../../../shared/catalogos/catalogos.api';
import { DeptoCiudadComponent } from '../../../shared/catalogos/depto-ciudad/depto-ciudad.component';

import { calcularDvNit } from '../../../shared/utils/calcular-dv-nit';
import { firstValueFrom } from 'rxjs';
import { docExistsValidator } from './doc-exists.validator';

@Component({
  selector: 'app-datos-personales-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, DeptoCiudadComponent],
  templateUrl: './datos-personales-upsert.component.html',
  styleUrls: ['./datos-personales-upsert.component.scss'],
})
export class DatosPersonalesUpsertComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private api = inject(DatosPersonalesApi);
  private cat = inject(CatalogosApi);

  id = signal<number | null>(null);
  loading = signal(false);
  guardando = signal(false);
  errorMsg = signal<string | null>(null);

  // Estado visible del chequeo de documento
  docChecking = signal(false);
  docInfo = signal<string>('');

  // Catálogos
  tiposDocumento = signal<CodigoNombreDTO[]>([]);
  paises = signal<IdNombreDTO[]>([]);
  generos = signal<CodigoNombreDTO[]>([]);
  estadosCiviles = signal<CodigoNombreDTO[]>([]);
  nivelesEscolares = signal<CodigoNombreDTO[]>([]);
  ocupaciones = signal<CodigoNombreDTO[]>([]);
  sectores = signal<CodigoNombreDTO[]>([]);
  tiposVivienda = signal<CodigoNombreDTO[]>([]);
  tiposRegimen = signal<TipoRegimenDTO[]>([]);

  // Búsquedas actividades
  sesQuery = signal<string>('');
  dianQuery = signal<string>('');
  sesResultados = signal<CodigoNombreDTO[]>([]);
  dianResultados = signal<CodigoNombreDTO[]>([]);
  buscandoSES = signal(false);
  buscandoDIAN = signal(false);

  form: FormGroup = this.fb.nonNullable.group({
    // Identificación
    tipoDocumento: this.fb.control('', {
      validators: [Validators.required, Validators.maxLength(2)],
      updateOn: 'blur',
      nonNullable: true
    }),
    documento: this.fb.control('', {
      validators: [Validators.required, Validators.pattern(/^[0-9]+$/), Validators.maxLength(20)],
      updateOn: 'blur',
      nonNullable: true
    }),
    tieneRut: [false],
    digitoVerificacion: [{ value: '', disabled: true }],
    tipoPersona: this.fb.control({ value: '1', disabled: true }, [Validators.required, Validators.pattern(/^[12]$/)]),

    // Fechas / País / Ciudades
    fechaDocumento: ['', [Validators.required]],
    idPaisDocumento: [null as number | null, [Validators.required]],
    idDepartamentoExpedicion: [null as number | null, [Validators.required]],
    idCiudadExpedicion: [null as number | null, [Validators.required]],

    // Nombres
    nombres: ['', [Validators.required, Validators.maxLength(100)]],
    primerApellido: ['', [Validators.required, Validators.maxLength(50)]],
    segundoApellido: ['', [Validators.maxLength(50)]],

    // Nacimiento
    fechaNacimiento: ['', [Validators.required]],
    idPaisNacimiento: [null as number | null, [Validators.required]],
    idDepartamentoNacimiento: [null as number | null, [Validators.required]],
    idCiudadNacimiento: [null as number | null, [Validators.required]],

    // Fechas servidor (no se envían)
    fechaApertura: [''],
    fechaActualiza: [''],

    // Catálogos
    codigoGenero: ['', [Validators.required, Validators.maxLength(1)]],
    codigoEstadoCivil: ['', [Validators.required, Validators.maxLength(1)]],
    codigoEscolaridad: ['', [Validators.required, Validators.maxLength(2)]],
    codigoTipoVivienda: ['', [Validators.required, Validators.maxLength(2)]],
    estratoSocial: [0, [Validators.min(0), Validators.max(6)]],
    numeroHijos: [0, [Validators.min(0), Validators.max(99)]],
    codigoOcupacion: ['', [Validators.required, Validators.maxLength(2)]],
    codigoSectorEconomico: ['', [Validators.required, Validators.maxLength(3)]],
    codigoActividadSes: ['', [Validators.required, Validators.maxLength(4)]],
    codigoActividadDian: ['', [Validators.required, Validators.maxLength(4)]],
    codigoRetencion: ['', [Validators.required, Validators.maxLength(2)]],

    // Cabeza de familia
    cabezaFamilia: ['0', [Validators.pattern(/^[01]$/)]],

    // Otros
    comentario: ['', [Validators.required, Validators.maxLength(250)]],
  });

  get depExpCtrl(): FormControl<number | null> {
    return this.form.get('idDepartamentoExpedicion') as FormControl<number | null>;
  }
  get ciuExpCtrl(): FormControl<number | null> {
    return this.form.get('idCiudadExpedicion') as FormControl<number | null>;
  }
  get depNacCtrl(): FormControl<number | null> {
    return this.form.get('idDepartamentoNacimiento') as FormControl<number | null>;
  }
  get ciuNacCtrl(): FormControl<number | null> {
    return this.form.get('idCiudadNacimiento') as FormControl<number | null>;
  }

  isFemenino(): boolean {
    return (this.form.get('codigoGenero')?.value ?? '') === '2';
  }

  private dvRequerido(): boolean {
    const tipoPersona = this.form.controls['tipoPersona'].value ?? '1';
    const tieneRut = !!this.form.controls['tieneRut'].value;
    return tipoPersona === '2' || (tipoPersona === '1' && tieneRut);
  }

  private updateDv() {
    const doc = (this.form.controls['documento'].value ?? '').trim();
    const requerido = this.dvRequerido();

    if (requerido) this.form.controls['digitoVerificacion'].enable({ emitEvent: false });
    else this.form.controls['digitoVerificacion'].disable({ emitEvent: false });

    if (requerido && doc) {
      const dv = calcularDvNit(doc);
      this.form.controls['digitoVerificacion'].setValue((dv ?? '').toString(), { emitEvent: false });
    } else {
      this.form.controls['digitoVerificacion'].setValue('', { emitEvent: false });
    }
  }

  private syncDvValidators() {
    const requerido = this.dvRequerido();
    const dvCtrl = this.form.get('digitoVerificacion')!;
    if (requerido) dvCtrl.setValidators([Validators.required, Validators.pattern(/^\d{1,2}$/)]);
    else { dvCtrl.clearValidators(); dvCtrl.setValue(''); }
    dvCtrl.updateValueAndValidity({ emitEvent: false });
  }

  private updateCabezaFamiliaDisabled(): void {
    const cCabeza = this.form.get('cabezaFamilia');
    if (!cCabeza) return;

    const femenino = this.isFemenino(); // <- antes llamaba this.esFemenino()

    if (femenino) {
      Promise.resolve().then(() => cCabeza.enable({ emitEvent: false }));
    } else {
      cCabeza.setValue('0', { emitEvent: false });
      cCabeza.disable({ emitEvent: false });
    }
  }

  private setupCabezaFamiliaDisableLogic(): void {
    this.updateCabezaFamiliaDisabled();
    this.form.controls['codigoGenero'].valueChanges.subscribe(() => this.updateCabezaFamiliaDisabled());
  }

  /** Forzar validación del FormGroup al perder foco en tipo/documento */
  checkDupOnBlur() {
    this.form.updateValueAndValidity({ onlySelf: false, emitEvent: false });
  }

  /** Chequeo inmediato visible: existe o disponible */
  checkDocImmediate() {
    // Solo verificar en modo NUEVO
    if (this.id() != null) {
      this.docInfo.set('');
      return;
    }

    const tipo = (this.form.get('tipoDocumento')?.value ?? '').toString().trim().toUpperCase();
    const doc  = (this.form.get('documento')?.value ?? '').toString().trim();

    if (!tipo || !doc) {
      this.docInfo.set('');
      const errs = { ...(this.form.errors || {}) };
      delete errs['docDuplicado'];
      this.form.setErrors(Object.keys(errs).length ? errs : null);
      return;
    }

    this.docChecking.set(true);
    this.api.list({ q: doc, size: 5 }).subscribe({
      next: (page) => {
        const exists = (page?.content ?? []).some(
          it =>
            String(it.documento).trim() === doc &&
            String(it.tipoDocumento).trim().toUpperCase() === tipo
        );

        if (exists) {
          this.docInfo.set('⚠️ Ya existe una persona con ese tipo y número de documento.');
          this.form.setErrors({
            ...(this.form.errors || {}),
            docDuplicado: true
          });
        } else {
          this.docInfo.set('✅ Documento disponible (nuevo).');
          const errs = { ...(this.form.errors || {}) };
          delete errs['docDuplicado'];
          this.form.setErrors(Object.keys(errs).length ? errs : null);
        }

        this.form.updateValueAndValidity({ onlySelf: true, emitEvent: false });
      },
      error: () => {
        this.docInfo.set('');
      },
      complete: () => this.docChecking.set(false)
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) this.id.set(Number(idParam));

    // Validador asíncrono a nivel de FormGroup (solo en modo "nuevo")
    this.form.setAsyncValidators(docExistsValidator(this.api, () => this.id()));
    this.form.updateValueAndValidity({ onlySelf: true, emitEvent: false });

    this.cargarCatalogos();

    // Validaciones cruzadas
    this.form.setValidators(this.fechasValidator.bind(this));
    this.form.controls['fechaNacimiento'].valueChanges
      .subscribe(() => this.form.updateValueAndValidity({ emitEvent: false }));
    this.form.controls['fechaDocumento'].valueChanges
      .subscribe(() => this.form.updateValueAndValidity({ emitEvent: false }));

    // Forzar cabezaFamilia="0" si no es femenino
    this.form.controls['codigoGenero'].valueChanges.subscribe(g => {
      if ((g ?? '') !== '2') {
        this.form.controls['cabezaFamilia'].setValue('0', { emitEvent: false });
      }
    });

    // DV dinámico
    this.form.controls['documento'].valueChanges.subscribe(() => { this.updateDv(); this.syncDvValidators(); });
    this.form.controls['tieneRut'].valueChanges.subscribe(() => { this.updateDv(); this.syncDvValidators(); });
    this.form.controls['tipoPersona'].valueChanges?.subscribe?.(() => { this.updateDv(); this.syncDvValidators(); });

    this.setupCabezaFamiliaDisableLogic();

    if (this.id()) this.cargar();
    else { this.updateDv(); this.syncDvValidators(); this.updateCabezaFamiliaDisabled(); }
  }

  private cargarCatalogos() {
    this.cat.tiposDocumentos().subscribe(res => this.tiposDocumento.set(res));
    this.cat.paises().subscribe(res => this.paises.set(res));
    this.cat.generos().subscribe(res => this.generos.set(res));
    this.cat.estadosCiviles().subscribe(res => this.estadosCiviles.set(res));
    this.cat.nivelesEscolares().subscribe(res => this.nivelesEscolares.set(res));
    this.cat.ocupaciones().subscribe(res => this.ocupaciones.set(res));
    this.cat.sectoresEconomicos().subscribe(res => this.sectores.set(res));
    this.cat.tiposVivienda().subscribe(res => this.tiposVivienda.set(res));
    this.cat.tiposRegimen().subscribe(res => this.tiposRegimen.set(res));
  }

  private cargar() {
    this.loading.set(true);
    this.api.get(this.id()!).subscribe({
      next: (d: DatosPersonalesDetail) => {
        this.form.patchValue({
          tipoDocumento: d.tipoDocumento ?? '',
          documento: d.documento ?? '',
          tieneRut: !!d.tieneRut,
          digitoVerificacion: d.digitoVerificacion ?? '',
          tipoPersona: (d as any).tipoPersona ?? this.form.controls['tipoPersona'].value,

          fechaDocumento: d.fechaDocumento ?? '',
          idPaisDocumento: d.idPaisDocumento ?? null,
          idDepartamentoExpedicion: d.idDepartamentoExpedicion ?? null,
          idCiudadExpedicion: d.idCiudadExpedicion ?? null,

          nombres: d.nombres ?? '',
          primerApellido: d.primerApellido ?? '',
          segundoApellido: d.segundoApellido ?? '',

          fechaNacimiento: d.fechaNacimiento ?? '',
          idPaisNacimiento: d.idPaisNacimiento ?? null,
          idDepartamentoNacimiento: d.idDepartamentoNacimiento ?? null,
          idCiudadNacimiento: d.idCiudadNacimiento ?? null,

          fechaApertura: (d as any).fechaApertura ?? '',
          fechaActualiza: (d as any).fechaActualiza ?? '',

          codigoGenero: d.codigoGenero ?? '',
          codigoEstadoCivil: d.codigoEstadoCivil ?? '',
          codigoEscolaridad: d.codigoEscolaridad ?? '',
          cabezaFamilia: (d as any).cabezaFamilia ?? '0',

          estratoSocial: d.estratoSocial ?? 0,
          codigoTipoVivienda: d.codigoTipoVivienda ?? '',
          numeroHijos: d.numeroHijos ?? 0,

          codigoOcupacion: d.codigoOcupacion ?? '',
          codigoSectorEconomico: d.codigoSectorEconomico ?? '',
          codigoActividadSes: d.codigoActividadSes ?? '',
          codigoActividadDian: d.codigoActividadDian ?? '',
          codigoRetencion: d.codigoRetencion ?? '',

          comentario: d.comentario ?? '',
        }, { emitEvent: false });

        this.updateDv();
        this.syncDvValidators();
        this.updateCabezaFamiliaDisabled();
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMsg.set(err?.error?.message ?? 'Error cargando');
        this.loading.set(false);
      }
    });
  }

  private async extractServerError(err: any): Promise<string> {
    try {
      if (err?.error instanceof Blob) {
        const txt = await err.error.text();
        return txt || (err.message ?? 'Error del servidor');
      }
      if (typeof err?.error === 'string') return err.error;
      if (err?.error?.message) return err.error.message;
      if (err?.message) return err.message;
    } catch {}
    return 'Error del servidor';
  }

  private buildPayloadForApi(): DatosPersonaCreate | DatosPersonaUpdate {
    const base = (this.form.getRawValue ? this.form.getRawValue() : { ...this.form.value }) as any;

    base.cabezaFamilia = this.resolverCabezaFamilia(
      base.tipoPersona,
      base.codigoGenero,
      base.cabezaFamilia
    );

    delete base.fechaApertura;
    delete base.fechaActualiza;
    delete base.idDepartamentoExpedicion;
    delete base.idDepartamentoNacimiento;

    const requeridoDV = (base.tipoPersona === '2') || (base.tipoPersona === '1' && !!base.tieneRut);
    if (requeridoDV) {
      let dv = (base.digitoVerificacion ?? '').toString().trim();
      if (!/^\d{1,2}$/.test(dv)) {
        const calc = calcularDvNit((base.documento ?? '').toString().trim());
        dv = (calc ?? '1').toString();
      }
      base.digitoVerificacion = dv;
    } else {
      delete base.digitoVerificacion;
    }

    return base as DatosPersonaCreate | DatosPersonaUpdate;
  }

  public async saveFromParent(): Promise<{ ok: boolean; id: number | null }> {
    this.errorMsg.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return { ok: false, id: null };
    }

    this.guardando.set(true);
    const payload = this.buildPayloadForApi();

    try {
      let id: number | null = null;

      if (this.id()) {
        const resp = await firstValueFrom(this.api.update(this.id()!, payload as DatosPersonaUpdate) as any);
        const maybeId =
          (resp as any)?.id ??
          (resp as any)?.idDatosPersonal ??
          (resp as any)?.id_datos_personal ??
          null;
        id = Number(maybeId || this.id()!);
      } else {
        const resp = await firstValueFrom(this.api.create(payload as DatosPersonaCreate) as any);
        const newId =
          (typeof resp === 'number') ? resp :
          (resp as any)?.id ??
          (resp as any)?.idDatosPersonal ??
          (resp as any)?.id_datos_personal ??
          null;

        if (!newId) {
          this.errorMsg.set('El servidor no devolvió un ID.');
          this.guardando.set(false);
          return { ok: false, id: null };
        }

        this.id.set(Number(newId));
        id = Number(newId);
      }

      this.guardando.set(false);
      return { ok: true, id };
    } catch (err: any) {
      const msg = await this.extractServerError(err);
      console.error('[DatosPersonales] Error API (saveFromParent):', msg, err);
      this.errorMsg.set(msg);
      this.guardando.set(false);
      return { ok: false, id: null };
    }
  }

  private handleError = async (err: any, fallback: string) => {
    const msg = await this.extractServerError(err);
    console.error('Error API datos-personales:', msg, err);
    this.errorMsg.set(msg || fallback);
    this.guardando.set(false);
  };

  guardar() {
    this.errorMsg.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.guardando.set(true);

    const payload = this.buildPayloadForApi();

    const after = (id?: number) => {
      const rid = this.id() ?? id!;
      this.api.get(rid).subscribe({
        next: (d) => {
          this.form.controls['digitoVerificacion'].setValue(d.digitoVerificacion ?? '', { emitEvent: false });
          this.guardando.set(false);
          this.router.navigate(['/hoja-vida/datos-personales']);
        },
        error: () => {
          this.guardando.set(false);
          this.router.navigate(['/hoja-vida/datos-personales']);
        }
      });
    };

    if (this.id()) {
      this.api.update(this.id()!, payload as DatosPersonaUpdate).subscribe({
        next: () => after(),
        error: (err) => this.handleError(err, 'Error actualizando')
      });
    } else {
      this.api.create(payload as DatosPersonaCreate).subscribe({
        next: (newId) => after(typeof newId === 'number' ? newId : (newId as any)?.id ?? (newId as any)?.idDatosPersonal ?? (newId as any)?.id_datos_personal ?? undefined),
        error: (err) => this.handleError(err, 'Error creando')
      });
    }
  }

  cancelar() {
    this.router.navigate(['/hoja-vida/datos-personales']);
  }

  private resolverCabezaFamilia(
    tipoPersona: string,
    codigoGenero: string,
    seleccionUsuario?: '0' | '1' | '' | null
  ): '0' | '1' {
    const tp  = (tipoPersona ?? '').trim();
    const gen = (codigoGenero ?? '').trim();

    if (tp === '2') return '0';
    if (gen === '1') return '0';
    if (gen === '3') return '0';

    return seleccionUsuario === '1' ? '1' : '0';
  }

  private normDate(v: unknown): string { return (typeof v === 'string' ? v.trim() : '') || ''; }
  private isAfter(a: string, b: string): boolean { return a !== '' && b !== '' && a > b; }
  private isBefore(a: string, b: string): boolean { return a !== '' && b !== '' && a < b; }

  private fechasValidator() {
    const fn = this.normDate(this.form.controls['fechaNacimiento'].value);
    const fd = this.normDate(this.form.controls['fechaDocumento'].value);
    const hoy = new Date().toISOString().slice(0, 10);

    this.form.controls['fechaNacimiento'].setErrors(null);
    this.form.controls['fechaDocumento'].setErrors(null);

    let hasError = false;

    if (this.isAfter(fn, hoy)) {
      this.form.controls['fechaNacimiento'].setErrors({ fechaFutura: true });
      hasError = true;
    }
    if (fn && fd && this.isBefore(fd, fn)) {
      const prev = this.form.controls['fechaDocumento'].errors || {};
      this.form.controls['fechaDocumento'].setErrors({ ...prev, menorQueNacimiento: true });
      hasError = true;
    }
    if (this.isAfter(fd, hoy)) {
      const prev = this.form.controls['fechaDocumento'].errors || {};
      this.form.controls['fechaDocumento'].setErrors({ ...prev, fechaFutura: true });
      hasError = true;
    }

    return hasError ? { fechasInvalidas: true } : null;
  }

  buscarSES() {
    const q = (this.sesQuery() ?? '').trim();
    if (!q) { this.sesResultados.set([]); return; }
    this.buscandoSES.set(true);
    this.cat.actividadesBuscar('SES', q, 20).subscribe({
      next: (list) => { this.sesResultados.set(list); this.buscandoSES.set(false); },
      error: () => { this.sesResultados.set([]); this.buscandoSES.set(false); }
    });
  }

  buscarDIAN() {
    const q = (this.dianQuery() ?? '').trim();
    if (!q) { this.dianResultados.set([]); return; }
    this.buscandoDIAN.set(true);
    this.cat.actividadesBuscar('DIAN', q, 20).subscribe({
      next: (list) => { this.dianResultados.set(list); this.buscandoDIAN.set(false); },
      error: () => { this.dianResultados.set([]); this.buscandoDIAN.set(false); }
    });
  }

  seleccionarSES(codigo: string) {
    this.form.controls['codigoActividadSes'].setValue(codigo);
    this.sesResultados.set([]);
  }

  seleccionarDIAN(codigo: string) {
    this.form.controls['codigoActividadDian'].setValue(codigo);
    this.dianResultados.set([]);
  }
}
