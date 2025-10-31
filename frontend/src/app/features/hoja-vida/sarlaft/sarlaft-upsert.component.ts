  import { Component, OnInit, inject, signal, computed } from '@angular/core';
  import { CommonModule } from '@angular/common';
  import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
  import { ActivatedRoute, Router } from '@angular/router';
  import { forkJoin } from 'rxjs';
  import { tap } from 'rxjs/operators';

  import { SarlaftApi, Sarlaft } from './sarlaft.api';
  import { CatalogosApi, CodigoNombreDTO } from '../../../shared/catalogos/catalogos.api';

  /**
   * 🧾 Componente Upsert (Crear / Editar) — SARLAFT
   * ------------------------------------------------------------
   * Reglas:
   *  - Todo registro nuevo inicia con hoy en las fechas.
   *  - Si cambian “Sí / No”, se respetan los valores digitados.
   *  - Si la opción es “Sí”, se validan los campos relacionados.
   *  - Si la opción es “No”, se limpian los textos, fechas = hoy() y PEPS = “000”.
   *  - El botón Guardar solo se habilita cuando el formulario está válido.
   */
  @Component({
    selector: 'app-sarlaft-upsert',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './sarlaft-upsert.component.html',
    styleUrls: ['./sarlaft-upsert.component.scss']
  })
  export class SarlaftUpsertComponent implements OnInit {
    private readonly fb = inject(FormBuilder);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly api = inject(SarlaftApi);
    private readonly catalogos = inject(CatalogosApi);

    form!: FormGroup;
    editando = false;
    tiposPeps: CodigoNombreDTO[] = [];
    parentescos: CodigoNombreDTO[] = [];

    // ✅ Signal reactivo para habilitar botón guardar
    puedeGuardar = computed(() => this.form?.valid ?? false);

    ngOnInit(): void {
      this.crearFormulario();

      const id = this.route.snapshot.paramMap.get('id');
      const idDatosPersonalParam = this.route.snapshot.queryParamMap.get('idDatosPersonal');

      this.cargarCatalogos().subscribe({
        next: () => {
          if (id) {
            this.editando = true;
            this.api.obtener(+id).subscribe({
              next: (data) => this.form.patchValue(data),
              error: (err) => console.error('❌ Error al cargar SARLAFT:', err)
            });
          } else if (idDatosPersonalParam) {
            this.form.get('idDatosPersonal')?.setValue(+idDatosPersonalParam);
            this.form.get('idDatosPersonal')?.disable();
          }
        }
      });

      this.setupListeners();
    }

    private crearFormulario(): void {
      const hoy = this.hoyISO();

      this.form = this.fb.group({
        idSarlaft: [null],
        idDatosPersonal: [null, Validators.required],

        // 🟩 Exonerado UIAF
        exoneracionUiaf: [false],
        fechaExoneracion: [hoy],

        // 🟦 Asociado PEPS
        asociadoPeps: [false],
        tipoPeps: ['000'],
        observacionesPeps: ['', Validators.maxLength(300)],
        fechaInicialPeps: [hoy],
        fechaFinalPeps: [hoy],

        // 🟧 Familiares PEPS
        familiaPeps: [false],
        tipoFamiliaPeps: ['000'],
        codigoParentesco: ['0'],
        cedulaFamiliaPeps: ['', Validators.pattern(/^[0-9]*$/)],
        nombreFamiliaPeps: ['', Validators.maxLength(100)],

        // 🟨 Moneda extranjera
        monedaExtranjera: [false],
        observacionMonedaExtranjera: ['', Validators.maxLength(200)],

        // 🟫 Cuenta en el extranjero
        cuentaExtranjero: [false],
        tipoMonedaExtranjera: ['', Validators.maxLength(20)],
        numeroCuentaExtranjero: ['', Validators.maxLength(30)],
        nombreBancoExtranjero: ['', Validators.maxLength(100)],
        ciudadCuentaExtranjero: ['', Validators.maxLength(50)],
        paisCuentaExtranjero: ['', Validators.maxLength(50)],

        // 🔒 Auditoría
        fkSeguridadCreacion: [1],
        fkSeguridadEdicion: [1],
      });
    }

    private cargarCatalogos() {
      return forkJoin({
        tiposPeps: this.catalogos.listarTiposPeps(),
        parentescos: this.catalogos.listarParentescos(),
      }).pipe(
        tap((res) => {
          this.tiposPeps = res.tiposPeps;
          this.parentescos = res.parentescos;
        })
      );
    }

    private setupListeners(): void {
      const hoy = this.hoyISO();

      // 🟩 Exonerado UIAF
      this.form.get('exoneracionUiaf')?.valueChanges.subscribe((val) => {
        if (val && !this.form.get('fechaExoneracion')?.value)
          this.form.get('fechaExoneracion')?.setValue(hoy);
        // si es “no”, no se borra
      });

      // 🟦 Asociado PEPS
      this.form.get('asociadoPeps')?.valueChanges.subscribe((val) => {
        if (val) {
          this.form.get('tipoPeps')?.addValidators(Validators.required);
          this.form.get('fechaInicialPeps')?.addValidators(Validators.required);
          this.form.get('fechaFinalPeps')?.addValidators(Validators.required);
        } else {
          this.form.patchValue({
            tipoPeps: '000',
            observacionesPeps: '',
            fechaInicialPeps: hoy,
            fechaFinalPeps: hoy
          });
          this.form.get('tipoPeps')?.clearValidators();
          this.form.get('fechaInicialPeps')?.clearValidators();
          this.form.get('fechaFinalPeps')?.clearValidators();
        }
        this.form.get('tipoPeps')?.updateValueAndValidity();
        this.form.get('fechaInicialPeps')?.updateValueAndValidity();
        this.form.get('fechaFinalPeps')?.updateValueAndValidity();
      });

      // 🟧 Familiares PEPS
      this.form.get('familiaPeps')?.valueChanges.subscribe((val) => {
        if (val) {
          this.form.get('tipoFamiliaPeps')?.addValidators(Validators.required);
          this.form.get('codigoParentesco')?.addValidators(Validators.required);
        } else {
          this.form.patchValue({
            tipoFamiliaPeps: '000',
            codigoParentesco: '0',
            cedulaFamiliaPeps: '',
            nombreFamiliaPeps: ''
          });
          this.form.get('tipoFamiliaPeps')?.clearValidators();
          this.form.get('codigoParentesco')?.clearValidators();
        }
        this.form.get('tipoFamiliaPeps')?.updateValueAndValidity();
        this.form.get('codigoParentesco')?.updateValueAndValidity();
      });

      // 🟨 Moneda extranjera
      this.form.get('monedaExtranjera')?.valueChanges.subscribe((val) => {
        if (val) {
          this.form.get('observacionMonedaExtranjera')?.addValidators(Validators.required);
        } else {
          this.form.patchValue({ observacionMonedaExtranjera: '' });
          this.form.get('observacionMonedaExtranjera')?.clearValidators();
        }
        this.form.get('observacionMonedaExtranjera')?.updateValueAndValidity();
      });

      // 🟫 Cuenta en el extranjero
      this.form.get('cuentaExtranjero')?.valueChanges.subscribe((val) => {
        if (val) {
          this.form.get('tipoMonedaExtranjera')?.addValidators(Validators.required);
          this.form.get('numeroCuentaExtranjero')?.addValidators(Validators.required);
          this.form.get('nombreBancoExtranjero')?.addValidators(Validators.required);
        } else {
          this.form.patchValue({
            tipoMonedaExtranjera: '',
            numeroCuentaExtranjero: '',
            nombreBancoExtranjero: '',
            ciudadCuentaExtranjero: '',
            paisCuentaExtranjero: ''
          });
          this.form.get('tipoMonedaExtranjera')?.clearValidators();
          this.form.get('numeroCuentaExtranjero')?.clearValidators();
          this.form.get('nombreBancoExtranjero')?.clearValidators();
        }
        this.form.get('tipoMonedaExtranjera')?.updateValueAndValidity();
        this.form.get('numeroCuentaExtranjero')?.updateValueAndValidity();
        this.form.get('nombreBancoExtranjero')?.updateValueAndValidity();
      });
    }

    private hoyISO(): string {
      return new Date().toISOString().split('T')[0];
    }

    guardar(): void {
      if (this.form.invalid) {
        this.form.markAllAsTouched();
        alert('⚠️ Complete los campos obligatorios o revise los formatos.');
        return;
      }

      const raw = { ...this.form.getRawValue() } as Sarlaft;
      const id = this.form.get('idSarlaft')?.value;

      if (!raw.idDatosPersonal) {
        const idDP = this.route.snapshot.queryParamMap.get('idDatosPersonal');
        if (idDP) raw.idDatosPersonal = +idDP;
      }

      const accion = this.editando && id
        ? this.api.actualizar(id, raw)
        : this.api.crear(raw);

      accion.subscribe({
        next: () => {
          alert('✅ Información SARLAFT guardada correctamente.');
          this.router.navigate(['/hoja-vida/sarlaft']);
        },
        error: (err) => console.error('❌ Error al guardar SARLAFT:', err)
      });
    }

    volver(): void {
      this.router.navigate(['/hoja-vida/sarlaft']);
    }
  }
