import { Component, OnInit, Input, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { RouterModule } from '@angular/router';
import { FormBuilder, Validators, ReactiveFormsModule, FormControl, FormGroup } from '@angular/forms';

import { LaboralesApi, LaboralDetail, LaboralCreate, LaboralUpdate } from './laborales.api';
import { DatosPersonalesApi } from '../datos-personales/datos-personales.api';
import { CatalogosApi } from '../../../shared/catalogos/catalogos.api';
import { DeptoCiudadComponent } from '../../../shared/catalogos/depto-ciudad/depto-ciudad.component';

@Component({
  standalone: true,
  selector: 'app-laborales-upsert',
  imports: [CommonModule, ReactiveFormsModule, RouterModule, DeptoCiudadComponent],
  templateUrl: './laborales-upsert.component.html',
  styleUrls: ['./laborales-upsert.component.scss'],
})
export class LaboralesUpsertComponent implements OnInit {
  // Soporte “modo wizard” para NO llamar APIs sin ID
  @Input() inWizard = false;
  @Input() idDatosPersonal?: number | null;

  private api = inject(LaboralesApi);
  private datosApi = inject(DatosPersonalesApi);
  private catalogosApi = inject(CatalogosApi);
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  idPersona!: number;

  loading = signal<boolean>(false);
  error = signal<string | null>(null);
  okMsg = signal<string | null>(null);

  detalle: LaboralDetail | null = null;

  form: FormGroup = this.fb.group({
    nombreEmpresa: ['', [Validators.required, Validators.maxLength(100)]],
    direccion: ['', [Validators.required, Validators.maxLength(100)]],
    idPais: new FormControl<number | null>(null),
    idDepartamento: new FormControl<number | null>(null),
    idCiudad: new FormControl<number | null>(null),
    telefonoEmpresa: ['', []],
    celularEmpresa: ['', []],
    correoEmpresa: ['', []],
    codigoTipoEmpresa: ['', []],
    empleadoEntidad: [false, []],
    codigoTipoContrato: ['', []],
    codigoJornada: ['', []],
    nombreContacto: ['', [Validators.required, Validators.maxLength(100)]],
    celularContacto: ['', []],
    fechaVinculacion: ['', []],
  });

  depCtrl = this.form.get('idDepartamento') as FormControl<number | null>;
  ciuCtrl = this.form.get('idCiudad') as FormControl<number | null>;

  private _paises: any[] = [];
  private _tiposEmpresas: any[] = [];
  private _tiposContratos: any[] = [];
  private _jornadas: any[] = [];

  private _afiliadoNombre: string = '';

  ngOnInit(): void {
    // Determinar idPersona desde @Input o desde la ruta (compat)
    const idFromInput = this.idDatosPersonal ?? null;
    const idFromRoute = Number(
      this.route.snapshot.paramMap.get('idDatosPersonales') ??
      this.route.snapshot.paramMap.get('idPersona')
    );
    const id = (idFromInput ?? (Number.isFinite(idFromRoute) ? idFromRoute : NaN));

    // Guardar idPersona (o NaN si no es válido)
    this.idPersona = Number.isFinite(id) ? Number(id) : NaN;

    this.cargarCatalogos();

    // Si estamos en wizard y no hay ID válido, NO llamamos APIs de backend
    if (this.inWizard && (!Number.isFinite(this.idPersona) || this.idPersona <= 0)) {
      return;
    }

    // Fuera de wizard o con ID válido -> flujo normal
    this.cargarAfiliado();

    const forceCreate = this.route.snapshot.queryParamMap.get('create') === '1';
    if (forceCreate) {
      this.detalle = null;
      this.form.reset({
        nombreEmpresa: '',
        direccion: '',
        idPais: null,
        idDepartamento: null,
        idCiudad: null,
        telefonoEmpresa: '',
        celularEmpresa: '',
        correoEmpresa: '',
        codigoTipoEmpresa: '',
        empleadoEntidad: false,
        codigoTipoContrato: '',
        codigoJornada: '',
        nombreContacto: '',
        celularContacto: '',
        fechaVinculacion: '',
      });
    } else {
      this.load();
    }
  }

  // === catálogos ===
  private cargarCatalogos(): void {
    this.catalogosApi.paises().subscribe({ next: d => this._paises = Array.isArray(d) ? d : [] });
    this.catalogosApi.tiposEmpresas().subscribe({ next: d => this._tiposEmpresas = Array.isArray(d) ? d : [] });
    this.catalogosApi.tiposContratos().subscribe({ next: d => this._tiposContratos = Array.isArray(d) ? d : [] });

    // método disponible en tu CatalogosApi para jornadas
    const anyCat = this.catalogosApi as any;
    const jornadasFn = anyCat.jornadasLaborales ?? anyCat.jornadas ?? null;
    if (typeof jornadasFn === 'function') {
      jornadasFn.call(this.catalogosApi).subscribe({
        next: (d: any[]) => { this._jornadas = Array.isArray(d) ? d : []; },
        error: () => { this._jornadas = []; }
      });
    } else {
      this._jornadas = [];
    }
  }
  paises() { return this._paises; }
  tiposEmpresas() { return this._tiposEmpresas; }
  tiposContratos() { return this._tiposContratos; }
  jornadas() { return this._jornadas; }

  // === persona/afiliado ===
  private cargarAfiliado(): void {
    if (!Number.isFinite(this.idPersona) || this.idPersona <= 0) return;

    this.datosApi.get(this.idPersona).subscribe({
      next: (p: any) => {
        const nombres = (p?.nombres ?? `${p?.primerNombre ?? ''} ${p?.segundoNombre ?? ''}`).toString().trim();
        const apellidos = (p?.apellidos ?? `${p?.primerApellido ?? ''} ${p?.segundoApellido ?? ''}`).toString().trim();
        const nom = `${nombres} ${apellidos}`.replace(/\s+/g, ' ').trim();
        this._afiliadoNombre = nom || '';
      },
      error: () => { this._afiliadoNombre = ''; }
    });
  }
  afiliadoNombre() { return this._afiliadoNombre; }

  // === carga detalle laboral ===
  load(): void {
    if (!Number.isFinite(this.idPersona) || this.idPersona <= 0) return;

    this.loading.set(true);
    this.error.set(null);
    this.okMsg.set(null);

    this.api.getByPersona(this.idPersona).subscribe({
      next: (d: LaboralDetail | null) => {
        this.loading.set(false);
        if (!d) {
          this.detalle = null; // modo crear
          this.form.reset({
            nombreEmpresa: '',
            direccion: '',
            idPais: null,
            idDepartamento: null,
            idCiudad: null,
            telefonoEmpresa: '',
            celularEmpresa: '',
            correoEmpresa: '',
            codigoTipoEmpresa: '',
            empleadoEntidad: false,
            codigoTipoContrato: '',
            codigoJornada: '',
            nombreContacto: '',
            celularContacto: '',
            fechaVinculacion: '',
          });
          return;
        }
        this.detalle = d;
        this.form.patchValue({
          nombreEmpresa: d.nombreEmpresa ?? '',
          direccion: d.direccion ?? '',
          idPais: d.idPais ?? null,
          idDepartamento: d.idDepartamento ?? null,
          idCiudad: d.idCiudad ?? null,
          telefonoEmpresa: d.telefonoEmpresa ?? '',
          celularEmpresa: d.celularEmpresa ?? '',
          correoEmpresa: d.correoEmpresa ?? '',
          codigoTipoEmpresa: d.codigoTipoEmpresa ?? '',
          empleadoEntidad: !!d.empleadoEntidad,
          codigoTipoContrato: d.codigoTipoContrato ?? '',
          codigoJornada: d.codigoJornada ?? '',
          nombreContacto: d.nombreContacto ?? '',
          celularContacto: d.celularContacto ?? '',
          fechaVinculacion: d.fechaVinculacion ?? '',
        });
      },
      error: (err) => {
        this.loading.set(false);
        if (err?.status === 404 || err?.status === 500) {
          this.detalle = null; // crear
          this.form.reset({
            nombreEmpresa: '',
            direccion: '',
            idPais: null,
            idDepartamento: null,
            idCiudad: null,
            telefonoEmpresa: '',
            celularEmpresa: '',
            correoEmpresa: '',
            codigoTipoEmpresa: '',
            empleadoEntidad: false,
            codigoTipoContrato: '',
            codigoJornada: '',
            nombreContacto: '',
            celularContacto: '',
            fechaVinculacion: '',
          });
          return;
        }
        if (err?.status === 401) {
          this.error.set('Sesión no válida o expirada. Ingresa nuevamente.');
          return;
        }
        this.error.set(this.readErr(err));
      }
    });
  }

  // === helpers de template ===
  item() { return this.detalle; }

  guardar(): void {
    this.error.set(null);
    this.okMsg.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload: LaboralCreate | LaboralUpdate = {
      nombreEmpresa: this.form.value.nombreEmpresa!,
      direccion: this.form.value.direccion!,
      empleadoEntidad: !!this.form.value.empleadoEntidad,
      nombreContacto: this.form.value.nombreContacto!,

      idPais: this.form.value.idPais ?? null,
      idDepartamento: this.form.value.idDepartamento ?? null,
      idCiudad: this.form.value.idCiudad ?? null,

      telefonoEmpresa: this.form.value.telefonoEmpresa ?? null,
      celularEmpresa: this.form.value.celularEmpresa ?? null,
      correoEmpresa: this.form.value.correoEmpresa ?? null,

      codigoTipoEmpresa: this.form.value.codigoTipoEmpresa ?? null,
      codigoTipoContrato: this.form.value.codigoTipoContrato ?? null,
      codigoJornada: this.form.value.codigoJornada ?? null,

      celularContacto: this.form.value.celularContacto ?? null,
      fechaVinculacion: this.form.value.fechaVinculacion ?? null,
    };

    this.loading.set(true);

    const goBack = () => {
      this.loading.set(false);
      this.router.navigate(['/hoja-vida/laborales']);
    };

    if (this.detalle) {
      // UPDATE
      this.api.update(this.idPersona, this.detalle.idLaboral, payload).subscribe({
        next: () => goBack(),
        error: (err) => { this.loading.set(false); this.error.set(this.readErr(err)); }
      });
    } else {
      // CREATE
      this.api.create(this.idPersona, payload).subscribe({
        next: () => goBack(),
        error: (err) => { this.loading.set(false); this.error.set(this.readErr(err)); }
      });
    }
  }

  eliminar(): void {
    if (!this.detalle) return;

    // ✅ Confirmación antes de eliminar
    const nombre = this._afiliadoNombre || 'la persona';
    const ok = confirm(`¿Eliminar la información laboral de ${nombre}? Esta acción no se puede deshacer.`);
    if (!ok) return;

    // Evitar doble clic mientras elimina
    if (this.loading()) return;

    this.loading.set(true);
    this.error.set(null);
    this.okMsg.set(null);

    this.api.remove(this.idPersona, this.detalle.idLaboral).subscribe({
      next: () => {
        this.loading.set(false);
        // tras eliminar, volvemos al listado
        this.router.navigate(['/hoja-vida/laborales']);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(this.readErr(err));
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/hoja-vida/laborales']);
  }

  private readErr(err: any): string {
    if (!err) return 'Error desconocido';
    if (typeof err.error === 'string') return err.error;
    if (err.error?.message) return err.error.message;
    return `Error (${err.status ?? '??'}): ${err.statusText ?? 'Solicitud fallida'}`;
  }
}
