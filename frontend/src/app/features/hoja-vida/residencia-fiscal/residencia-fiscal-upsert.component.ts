import {
  Component,
  OnInit,
  OnChanges,
  Input,
  Output,
  EventEmitter,
  SimpleChanges,
  computed,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators
} from '@angular/forms';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  ResidenciaFiscalApi,
  ResidenciaFiscal
} from './residencia-fiscal.api';

@Component({
  selector: 'app-residencia-fiscal-upsert',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl:
    './residencia-fiscal-upsert.component.html',
  styleUrls: [
    './residencia-fiscal-upsert.component.scss'
  ]
})
export class ResidenciaFiscalUpsertComponent
  implements OnInit, OnChanges {

  @Input()
  idDatosPersonal?: number;

  @Output()
  formularioValido =
    new EventEmitter<boolean>();

  private readonly fb =
    inject(FormBuilder);

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly api =
    inject(ResidenciaFiscalApi);

  form!: FormGroup;

  editando = false;

  puedeGuardar = computed(
    () => this.form?.valid ?? false
  );

  // ============================================================
  // INIT
  // ============================================================

  ngOnInit(): void {

    this.crearFormulario();

    const id =
      this.route.snapshot.paramMap.get('id');

    const idDatosPersonalParam =
      this.route.snapshot.queryParamMap.get(
        'idDatosPersonal'
      );

    if (id) {

      this.editando = true;

      this.api.obtener(+id)
        .subscribe({

          next: data => {

            this.form.patchValue(data);

            this.configurarReglasFormulario();

            queueMicrotask(() => {
              this.formularioValido.emit(
                this.form.valid
              );
            });

          },

          error: err =>
            console.error(
              'Error cargando residencia fiscal',
              err
            )

        });

    } else {

      const idPersona =
        idDatosPersonalParam
          ? Number(idDatosPersonalParam)
          : this.idDatosPersonal;

      if (idPersona) {

        this.form
          .get('idDatosPersonal')
          ?.setValue(idPersona);

      }

      queueMicrotask(() => {

        this.formularioValido.emit(
          this.form.valid
        );

      });

    }

    this.form.statusChanges.subscribe(() => {

      this.formularioValido.emit(
        this.form.valid
      );

    });

    this.configurarListeners();
    this.configurarReglasFormulario();

  }

  // ============================================================
  // CAMBIOS WIZARD
  // ============================================================

  ngOnChanges(
    changes: SimpleChanges
  ): void {

    if (
      changes['idDatosPersonal']
      && this.idDatosPersonal
    ) {

      this.form
        .get('idDatosPersonal')
        ?.setValue(this.idDatosPersonal);

      this.formularioValido.emit(
        this.form.valid
      );

    }

  }

  // ============================================================
  // FORMULARIO
  // ============================================================

  private crearFormulario(): void {

    this.form = this.fb.group({

      idResidenciaFiscal: [null],

      idDatosPersonal: [
        null,
        Validators.required
      ],

      ciudadanoEstadosUnidos: [
        false
      ],

      residenteFiscalEstadosUnidos: [
        false
      ],

      residenteFiscalExterior: [
        false
      ],

      paisResidenciaFiscal: [''],

      ciudadResidenciaFiscal: [''],

      direccionResidenciaFiscal: [''],

      tipoIdentificacionFiscal: [''],

      numeroIdentificacionFiscal: [''],

      observaciones: ['']

    });

  }

  // ============================================================
  // LISTENERS
  // ============================================================

  private configurarListeners(): void {

    this.form
      .get('residenteFiscalExterior')
      ?.valueChanges
      .subscribe(() => {

        this.configurarReglasFormulario();

      });

    this.form
      .get('residenteFiscalEstadosUnidos')
      ?.valueChanges
      .subscribe(() => {

        this.configurarReglasFormulario();

      });

  }

  // ============================================================
  // REGLAS
  // ============================================================

  private configurarReglasFormulario(): void {

    const exterior =
      this.form.get(
        'residenteFiscalExterior'
      );

    const usa =
      this.form.get(
        'residenteFiscalEstadosUnidos'
      );

    const pais =
      this.form.get(
        'paisResidenciaFiscal'
      );

    const ciudad =
      this.form.get(
        'ciudadResidenciaFiscal'
      );

    const direccion =
      this.form.get(
        'direccionResidenciaFiscal'
      );

    const tipo =
      this.form.get(
        'tipoIdentificacionFiscal'
      );

    const numero =
      this.form.get(
        'numeroIdentificacionFiscal'
      );

    // --------------------------------------------------------

    if (exterior?.value) {

      pais?.enable({ emitEvent: false });
      ciudad?.enable({ emitEvent: false });
      direccion?.enable({ emitEvent: false });

      pais?.setValidators([
        Validators.required
      ]);

    } else {

      pais?.reset();
      ciudad?.reset();
      direccion?.reset();

      pais?.clearValidators();

      pais?.disable({ emitEvent: false });
      ciudad?.disable({ emitEvent: false });
      direccion?.disable({ emitEvent: false });

    }

    // --------------------------------------------------------

    if (usa?.value) {

      tipo?.enable({ emitEvent: false });
      numero?.enable({ emitEvent: false });

      tipo?.setValidators([
        Validators.required
      ]);

      numero?.setValidators([
        Validators.required
      ]);

    } else {

      tipo?.reset();
      numero?.reset();

      tipo?.clearValidators();
      numero?.clearValidators();

      tipo?.disable({ emitEvent: false });
      numero?.disable({ emitEvent: false });

    }

    pais?.updateValueAndValidity({
      emitEvent: false
    });

    tipo?.updateValueAndValidity({
      emitEvent: false
    });

    numero?.updateValueAndValidity({
      emitEvent: false
    });

  }

  // ============================================================
  // GUARDAR
  // ============================================================

  guardar(): void {

    if (this.form.invalid) {

      this.form.markAllAsTouched();

      alert(
        'Complete los campos obligatorios.'
      );

      return;

    }

    if (
      !this.form.get('idDatosPersonal')
        ?.value
      &&
      this.idDatosPersonal
    ) {

      this.form
        .get('idDatosPersonal')
        ?.setValue(
          this.idDatosPersonal
        );

    }

    const dto = this.form.getRawValue() as ResidenciaFiscal;

    const id =
      this.form.get(
        'idResidenciaFiscal'
      )?.value;

    const accion =
      this.editando && id
        ? this.api.actualizar(
            id,
            dto
          )
        : this.api.crear(dto);

    accion.subscribe({

      next: () => {

        alert(
          'Residencia fiscal guardada correctamente.'
        );

        if (!this.idDatosPersonal) {

          this.router.navigate([
            '/hoja-vida/residencia-fiscal'
          ]);

        }

      },

      error: err =>
        console.error(
          'Error guardando residencia fiscal',
          err
        )

    });

  }

  // ============================================================
  // VOLVER
  // ============================================================

  volver(): void {

    this.router.navigate([
      '/hoja-vida/residencia-fiscal'
    ]);

  }

}
