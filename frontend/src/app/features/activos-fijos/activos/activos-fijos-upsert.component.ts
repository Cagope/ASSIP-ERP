import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormControl
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';

import { EstadosActivoApi } from '../catalogos/estados-activo.api';
import { FormasDepreciacionApi } from '../catalogos/formas-depreciacion.api';

import {
  ActivosFijosApi,
  ActivoFijoFormDTO,
  ActivoFijoSaveDTO
} from './activos-fijos.api';

import { GeneralApi } from '../../../shared/general/general.api';
import {
  PersonasApi,
  PersonaBusquedaDTO
} from '../../../shared/personas/personas.api';

import {
  CuentasApi,
  CuentaAutocompleteDTO
} from '../../../shared/cuentas/cuentas.api';

import {
  LocalizacionesApi,
  LocalizacionListDTO
} from '../localizaciones/localizaciones.api';
import { BloquesApi, BloqueListDTO } from '../bloques/bloques.api';

import {
  TiposAdquisicionApi,
  TipoAdquisicionDTO
} from '../catalogos/tipos-adquisicion.api';


@Component({
  selector: 'app-activos-fijos-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './activos-fijos-upsert.component.html',
  styleUrls: ['./activos-fijos-upsert.component.scss']
})
export class ActivosFijosUpsertComponent implements OnInit {

  // =========================================================
  // INYECCIONES
  // =========================================================
  private api = inject(ActivosFijosApi);
  private generalApi = inject(GeneralApi);
  private localizacionesApi = inject(LocalizacionesApi);
  private bloquesApi = inject(BloquesApi);
  private estadosActivoApi = inject(EstadosActivoApi);
  private formasDepreciacionApi = inject(FormasDepreciacionApi);
  private personasApi = inject(PersonasApi);
  private cuentasApi = inject(CuentasApi);
  private fb = inject(FormBuilder);
  private tiposAdquisicionApi = inject(TiposAdquisicionApi);

  route = inject(ActivatedRoute);
  router = inject(Router);

  // =========================================================
  // COMBOS
  // =========================================================
  agencias: { idAgencia: number; nombreAgencia: string }[] = [];
  localizaciones: LocalizacionListDTO[] = [];
  bloques: BloqueListDTO[] = [];
  estadosActivo: any[] = [];
  formasDepreciacion: any[] = [];

  // =========================================================
  // AUTOCOMPLETE PERSONAS
  // =========================================================
  responsableCtrl = new FormControl<string>('', { nonNullable: true });
  proveedorCtrl = new FormControl<string>('', { nonNullable: true });

  responsables: PersonaBusquedaDTO[] = [];
  proveedores: PersonaBusquedaDTO[] = [];

  // =========================================================
  // AUTOCOMPLETE CUENTAS
  // =========================================================
  ctaActivoCtrl = new FormControl<string>('', { nonNullable: true });
  ctaDepreciacionCtrl = new FormControl<string>('', { nonNullable: true });
  ctaGastoCtrl = new FormControl<string>('', { nonNullable: true });
  ctaControlCtrl = new FormControl<string>('', { nonNullable: true });

  cuentasActivo: CuentaAutocompleteDTO[] = [];
  cuentasDepreciacion: CuentaAutocompleteDTO[] = [];
  cuentasGasto: CuentaAutocompleteDTO[] = [];
  cuentasControl: CuentaAutocompleteDTO[] = [];
  tiposAdquisicion: TipoAdquisicionDTO[] = [];

  // =========================================================
  // FORM
  // =========================================================
  form = this.fb.group({

    placaActivo: ['', Validators.required],
    nombreActivo: ['', Validators.required],

    fechaIngreso: ['', Validators.required],
    fechaGarantia: ['', Validators.required],
    fechaBaja: [null as string | null],

    idFormaDepreciacion: [null as number | null, Validators.required],

    mesesDepreciacion: [
      null as number | null,
      [Validators.required, Validators.min(1)]
    ],

    valorAdquisicion: [0, Validators.required],
    valorMensual: [0, Validators.required],

    idEstadoActivo: [null as number | null, Validators.required],
    idTipoAdquisicion: [null as number | null, Validators.required],

    idAgencia: [null as number | null, Validators.required],
    idUbicacion: [null as number | null, Validators.required],
    idBloque: [null as number | null],

    idDatosPersonalResponsable: [null as number | null],
    idDatosPersonalProveedor: [null as number | null],

    idCatalogoCuentaActivo: [null as number | null],
    idCatalogoCuentaDepreciacion: [null as number | null],
    idCatalogoCuentaGasto: [null as number | null],
    idCatalogoCuentaControl: [null as number | null],
  });

  // =========================================================
  // INIT
  // =========================================================
  ngOnInit(): void {

    // ===============================
    // COMBOS
    // ===============================
    this.cargarAgencias();
    this.cargarLocalizaciones();
    this.cargarBloques();

    this.estadosActivoApi.listar().subscribe(d => this.estadosActivo = d);
    this.formasDepreciacionApi.listar().subscribe(d => this.formasDepreciacion = d);
    this.tiposAdquisicionApi
      .listar()
      .subscribe(d => this.tiposAdquisicion = d);

    // ===============================
    // BLOQUE → MESES
    // ===============================
    this.form.get('idBloque')?.valueChanges.subscribe(idBloque => {
      const bloque = this.bloques.find(b => b.idBloque === idBloque);
      if (bloque?.mesesDepreciacionDefecto != null) {
        this.form.patchValue({ mesesDepreciacion: bloque.mesesDepreciacionDefecto });
      }
    });

    // ===============================
    // AUTOCOMPLETE PERSONAS
    // ===============================
    this.autocompletePersonas(this.responsableCtrl, this.responsables);
    this.autocompletePersonas(this.proveedorCtrl, this.proveedores);

    // ===============================
    // AUTOCOMPLETE CUENTAS
    // ===============================
    this.autocompleteCuentas(this.ctaActivoCtrl, this.cuentasActivo);
    this.autocompleteCuentas(this.ctaDepreciacionCtrl, this.cuentasDepreciacion);
    this.autocompleteCuentas(this.ctaGastoCtrl, this.cuentasGasto);
    this.autocompleteCuentas(this.ctaControlCtrl, this.cuentasControl);

    const esNuevo = !this.route.snapshot.paramMap.get('id');

    if (esNuevo) {

      this.form.get('valorAdquisicion')?.valueChanges.subscribe(() => {
        this.sugerirValorMensual();
      });

      this.form.get('mesesDepreciacion')?.valueChanges.subscribe(() => {
        this.sugerirValorMensual();
      });

    }

    // ===============================
    // EDICIÓN
    // ===============================
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.api.obtener(+id).subscribe((data: ActivoFijoFormDTO) => {

        // 1️⃣ Cargar IDs base en el form (incluye idAgencia)
        this.form.patchValue(data);

        // =================================================
        // ⚠️ CLAVE: idAgencia DEBE existir antes de pintar
        // =================================================
        const idAgencia = data.idAgencia;
        if (idAgencia) {
          this.form.patchValue({ idAgencia });
        }

        // ===============================
        // 2️⃣ Pintar responsable
        // ===============================
        this.cargarPersonaPorId(
          data.idDatosPersonalResponsable ?? null,
          this.responsableCtrl
        );

        // ===============================
        // 3️⃣ Pintar proveedor
        // ===============================
        this.cargarPersonaPorId(
          data.idDatosPersonalProveedor ?? null,
          this.proveedorCtrl
        );

        // ===============================
        // 4️⃣ Pintar cuentas contables
        // ===============================
        this.cargarCuentaPorId(
          data.idCatalogoCuentaActivo ?? null,
          this.ctaActivoCtrl
        );

        this.cargarCuentaPorId(
          data.idCatalogoCuentaDepreciacion ?? null,
          this.ctaDepreciacionCtrl
        );

        this.cargarCuentaPorId(
          data.idCatalogoCuentaGasto ?? null,
          this.ctaGastoCtrl
        );

        this.cargarCuentaPorId(
          data.idCatalogoCuentaControl ?? null,
          this.ctaControlCtrl
        );

      });
    }

  }

  // =========================================================
  // AUTOCOMPLETE HELPERS
  // =========================================================
  private autocompletePersonas(
    ctrl: FormControl<string>,
    target: PersonaBusquedaDTO[]
  ): void {
    ctrl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(text =>
        typeof text === 'string' && text.length >= 3
          ? this.personasApi.buscar(text)
          : of([])
      )
    ).subscribe(data => {
      target.splice(0, target.length, ...data);
    });
  }


  private autocompleteCuentas(
    ctrl: FormControl<string>,
    target: CuentaAutocompleteDTO[]
  ): void {

    ctrl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(text => {

        const idAgencia = this.form.get('idAgencia')?.value;

        if (
          !idAgencia ||
          typeof text !== 'string' ||
          text.length < 3
        ) {
          return of([]);
        }

        // 🔒 FILTRO ESTRICTO POR AGENCIA DEL ENCABEZADO
        return this.cuentasApi.buscar(idAgencia, text);
      })
    ).subscribe(data => {
      target.splice(0, target.length, ...data);
    });
  }

  // =========================================================
  // SELECCIÓN PERSONAS
  // =========================================================
  seleccionarResponsable(p: PersonaBusquedaDTO): void {
    this.form.patchValue({ idDatosPersonalResponsable: p.idDatosPersonal });
    this.responsableCtrl.setValue(
      `${p.nombreCompleto} (${p.documento})`,
      { emitEvent: false }
    );
    this.responsables.length = 0;
  }

  seleccionarProveedor(p: PersonaBusquedaDTO): void {
    this.form.patchValue({ idDatosPersonalProveedor: p.idDatosPersonal });
    this.proveedorCtrl.setValue(
      `${p.nombreCompleto} (${p.documento})`,
      { emitEvent: false }
    );
    this.proveedores.length = 0;
  }

  // =========================================================
  // SELECCIÓN CUENTAS
  // =========================================================
  seleccionarCuenta(
    campo: string,
    ctrl: FormControl,
    lista: CuentaAutocompleteDTO[],
    c: CuentaAutocompleteDTO
  ): void {
    this.form.patchValue({ [campo]: c.idCuenta });
    ctrl.setValue(
      `${c.codigoCuenta} — ${c.nombreCuenta}`,
      { emitEvent: false }
    );
    lista.length = 0;
  }

  // =========================================================
  // COMBOS
  // =========================================================
  private cargarAgencias(): void {
    this.generalApi.listarAgencias().subscribe(d => this.agencias = d);
  }

  private cargarLocalizaciones(): void {
    this.localizacionesApi.listar().subscribe(d => this.localizaciones = d);
  }

  private cargarBloques(): void {
    this.bloquesApi.listar().subscribe(d => this.bloques = d);
  }

  // =========================================================
  // ACCIONES
  // =========================================================
  guardar(): void {
    if (this.form.invalid) return;

    const payload = this.form.getRawValue() as ActivoFijoSaveDTO;
    const id = this.route.snapshot.paramMap.get('id');

    const req = id
      ? this.api.actualizar(+id, payload)
      : this.api.crear(payload);

    req.subscribe(() => {
      this.router.navigate(['/activos-fijos/activos']);
    });
  }

  private sugerirValorMensual(): void {

    const valor = this.form.get('valorAdquisicion')?.value ?? 0;
    const meses = this.form.get('mesesDepreciacion')?.value ?? 0;

    if (!valor || !meses || meses <= 0) return;

    const mensual = Math.round((valor / meses) * 100) / 100;

    this.form.patchValue(
      { valorMensual: mensual },
      { emitEvent: false }
    );
  }

  private cargarPersonaPorId(
    id: number | null,
    ctrl: FormControl<string>
  ): void {
    if (!id) return;

    this.personasApi.obtenerPorId(id).subscribe(p => {
      if (!p) return;

      ctrl.setValue(
        `${p.nombreCompleto} (${p.documento})`,
        { emitEvent: false }
      );
    });
  }


  private cargarCuentaPorId(
    idCuenta: number | null,
    ctrl: FormControl<string>
  ): void {

    if (!idCuenta) return;

    this.cuentasApi.obtenerPorId(idCuenta).subscribe(c => {

      ctrl.setValue(
        `${c.codigoCuenta} — ${c.nombreCuenta}`,
        { emitEvent: false } // 🔒 NO autocomplete
      );
    });
  }

  cancelar(): void {
    this.router.navigate(['/activos-fijos/activos']);
  }
}
