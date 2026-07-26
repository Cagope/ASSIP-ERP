import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  computed,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  CondicionProteccion,
  CondicionesProteccionApi,
  nuevaCondicionProteccion
} from './condiciones-proteccion.api';

/**
 * 🛡️ Componente Upsert — Condiciones de Protección
 * ------------------------------------------------------------
 * Reglas:
 *  - Cada persona puede tener un solo registro.
 *  - Todos los campos de condición se manejan como Sí / No.
 *  - Un registro nuevo inicia con todas las condiciones en false.
 *  - Puede funcionar como CRUD independiente o dentro del wizard.
 *  - El botón Guardar solo se habilita cuando el formulario es válido.
 */
@Component({
  selector:
    'app-condiciones-proteccion-upsert',

  standalone: true,

  imports: [
    CommonModule,
    ReactiveFormsModule
  ],

  templateUrl:
    './condiciones-proteccion-upsert.component.html',

  styleUrls: [
    './condiciones-proteccion-upsert.component.scss'
  ]
})
export class CondicionesProteccionUpsertComponent
  implements OnInit, OnChanges {

  @Output()
  formularioValido =
    new EventEmitter<boolean>();

  @Input()
  idDatosPersonal?: number;

  private readonly fb =
    inject(FormBuilder);

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly api =
    inject(CondicionesProteccionApi);

  form!: FormGroup;

  editando = false;

  cargando = false;

  guardando = false;

  error = '';

  /**
   * Mantiene el mismo contrato visual de los demás
   * formularios del wizard.
   */
  puedeGuardar = computed(
    () =>
      Boolean(
        this.form?.valid
        && !this.guardando
        && !this.cargando
      )
  );

  ngOnInit(): void {
    this.crearFormulario();
    this.configurarEscuchaEstado();

    const id =
      this.route.snapshot.paramMap.get(
        'id'
      );

    const idDatosPersonalParam =
      this.route.snapshot.queryParamMap.get(
        'idDatosPersonal'
      );

    if (id) {
      this.cargarRegistro(
        Number(id)
      );

      return;
    }

    const idPersona =
      idDatosPersonalParam
        ? Number(idDatosPersonalParam)
        : this.idDatosPersonal;

    if (
      idPersona != null
      && Number.isFinite(idPersona)
      && idPersona > 0
    ) {
      this.asignarPersona(
        idPersona
      );
    }

    queueMicrotask(() => {
      this.emitirValidez();
    });
  }

  // ============================================================
  // CAMBIOS DESDE EL WIZARD
  // ============================================================

  ngOnChanges(
    changes: SimpleChanges
  ): void {

    if (
      !changes['idDatosPersonal']
      || !this.idDatosPersonal
      || !this.form
    ) {
      return;
    }

    this.asignarPersona(
      this.idDatosPersonal
    );

    this.emitirValidez();
  }

  // ============================================================
  // FORMULARIO
  // ============================================================

  private crearFormulario(): void {
    const modelo =
      nuevaCondicionProteccion();

    this.form = this.fb.group({
      idCondicionProteccion: [
        modelo.idCondicionProteccion
        ?? null
      ],

      idDatosPersonal: [
        modelo.idDatosPersonal
        ?? null,
        Validators.required
      ],

      administraRecursosPublicos: [
        modelo.administraRecursosPublicos
        ?? false,
        Validators.required
      ],

      grupoProteccionEspecialConstitucional: [
        modelo
          .grupoProteccionEspecialConstitucional
        ?? false,
        Validators.required
      ],

      personaMayor60Anos: [
        modelo.personaMayor60Anos
        ?? false,
        Validators.required
      ],

      discapacidadFisica: [
        modelo.discapacidadFisica
        ?? false,
        Validators.required
      ],

      victimaConflictoArmado: [
        modelo.victimaConflictoArmado
        ?? false,
        Validators.required
      ],

      pobrezaExtrema: [
        modelo.pobrezaExtrema
        ?? false,
        Validators.required
      ],

      poblacionIndigena: [
        modelo.poblacionIndigena
        ?? false,
        Validators.required
      ],

      poblacionAfrodescendiente: [
        modelo.poblacionAfrodescendiente
        ?? false,
        Validators.required
      ],

      poblacionLgbtiqMas: [
        modelo.poblacionLgbtiqMas
        ?? false,
        Validators.required
      ],

      perteneceGrupoProteccionConstitucional: [
        modelo
          .perteneceGrupoProteccionConstitucional
        ?? false,
        Validators.required
      ],

      observaciones: [
        modelo.observaciones
        ?? null
      ],

      fkSeguridadCreacion: [
        modelo.fkSeguridadCreacion
        ?? null
      ],

      fechaCreacion: [
        modelo.fechaCreacion
        ?? null
      ],

      fkSeguridadEdicion: [
        modelo.fkSeguridadEdicion
        ?? null
      ],

      fechaEdicion: [
        modelo.fechaEdicion
        ?? null
      ]
    });

    this.emitirValidez();
  }

  private configurarEscuchaEstado(): void {
    this.form.statusChanges.subscribe(
      () => {
        this.emitirValidez();
      }
    );
  }

  private asignarPersona(
    idDatosPersonal: number
  ): void {

    this.form.get(
      'idDatosPersonal'
    )?.setValue(
      idDatosPersonal
    );

    this.form.updateValueAndValidity({
      emitEvent: true
    });
  }

  private emitirValidez(): void {
    this.formularioValido.emit(
      this.form?.valid ?? false
    );
  }

  // ============================================================
  // CARGAR REGISTRO
  // ============================================================

  private cargarRegistro(
    idCondicionProteccion: number
  ): void {

    if (
      !Number.isFinite(
        idCondicionProteccion
      )
      || idCondicionProteccion <= 0
    ) {
      this.error =
        'El identificador de la condición de protección no es válido.';

      this.emitirValidez();
      return;
    }

    this.editando = true;
    this.cargando = true;
    this.error = '';

    this.api.obtener(
      idCondicionProteccion
    )
      .subscribe({
        next: data => {
          this.form.patchValue({
            idCondicionProteccion:
              data.idCondicionProteccion,

            idDatosPersonal:
              data.idDatosPersonal,

            administraRecursosPublicos:
              data.administraRecursosPublicos
              ?? false,

            grupoProteccionEspecialConstitucional:
              data
                .grupoProteccionEspecialConstitucional
              ?? false,

            personaMayor60Anos:
              data.personaMayor60Anos
              ?? false,

            discapacidadFisica:
              data.discapacidadFisica
              ?? false,

            victimaConflictoArmado:
              data.victimaConflictoArmado
              ?? false,

            pobrezaExtrema:
              data.pobrezaExtrema
              ?? false,

            poblacionIndigena:
              data.poblacionIndigena
              ?? false,

            poblacionAfrodescendiente:
              data.poblacionAfrodescendiente
              ?? false,

            poblacionLgbtiqMas:
              data.poblacionLgbtiqMas
              ?? false,

            perteneceGrupoProteccionConstitucional:
              data
                .perteneceGrupoProteccionConstitucional
              ?? false,

            observaciones:
              data.observaciones
              ?? null,

            fkSeguridadCreacion:
              data.fkSeguridadCreacion
              ?? null,

            fechaCreacion:
              data.fechaCreacion
              ?? null,

            fkSeguridadEdicion:
              data.fkSeguridadEdicion
              ?? null,

            fechaEdicion:
              data.fechaEdicion
              ?? null
          });

          this.form.markAsPristine();
          this.form.markAsUntouched();

          queueMicrotask(() => {
            this.emitirValidez();
          });
        },

        error: err => {
          console.error(
            'Error al cargar condiciones de protección:',
            err
          );

          this.error =
            'No fue posible cargar las condiciones de protección.';
        },

        complete: () => {
          this.cargando = false;
        }
      });
  }

  // ============================================================
  // GUARDAR / ACTUALIZAR
  // ============================================================

  guardar(): void {
    this.error = '';

    if (
      !this.form.get(
        'idDatosPersonal'
      )?.value
      && this.idDatosPersonal
    ) {
      this.asignarPersona(
        this.idDatosPersonal
      );
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();

      alert(
        '⚠️ Complete los campos obligatorios antes de guardar.'
      );

      this.emitirValidez();
      return;
    }

    const raw =
      this.construirPayload();

    const id =
      this.form.get(
        'idCondicionProteccion'
      )?.value;

    this.guardando = true;

    const accion =
      this.editando && id
        ? this.api.actualizar(
            Number(id),
            raw
          )
        : this.api.crear(
            raw
          );

    accion.subscribe({
      next: respuesta => {
        alert(
          '✅ Condiciones de protección guardadas correctamente.'
        );

        this.form.patchValue(
          respuesta
        );

        this.form.markAsPristine();
        this.form.markAsUntouched();

        this.emitirValidez();

        if (this.idDatosPersonal) {
          console.log(
            'Condiciones de protección vinculadas a la persona:',
            this.idDatosPersonal
          );

          return;
        }

        this.router.navigate([
          '/hoja-vida/condiciones-proteccion'
        ]);
      },

      error: err => {
        console.error(
          'Error al guardar condiciones de protección:',
          err
        );

        this.error =
          err?.error?.message
          ?? err?.error?.mensaje
          ?? 'No fue posible guardar las condiciones de protección.';
      },

      complete: () => {
        this.guardando = false;
      }
    });
  }

  private construirPayload():
    CondicionProteccion {

    const raw =
      this.form.getRawValue();

    return {
      idCondicionProteccion:
        raw.idCondicionProteccion
        ?? null,

      idDatosPersonal:
        Number(
          raw.idDatosPersonal
        ),

      administraRecursosPublicos:
        Boolean(
          raw.administraRecursosPublicos
        ),

      grupoProteccionEspecialConstitucional:
        Boolean(
          raw
            .grupoProteccionEspecialConstitucional
        ),

      personaMayor60Anos:
        Boolean(
          raw.personaMayor60Anos
        ),

      discapacidadFisica:
        Boolean(
          raw.discapacidadFisica
        ),

      victimaConflictoArmado:
        Boolean(
          raw.victimaConflictoArmado
        ),

      pobrezaExtrema:
        Boolean(
          raw.pobrezaExtrema
        ),

      poblacionIndigena:
        Boolean(
          raw.poblacionIndigena
        ),

      poblacionAfrodescendiente:
        Boolean(
          raw.poblacionAfrodescendiente
        ),

      poblacionLgbtiqMas:
        Boolean(
          raw.poblacionLgbtiqMas
        ),

      perteneceGrupoProteccionConstitucional:
        Boolean(
          raw
            .perteneceGrupoProteccionConstitucional
        ),

      observaciones:
        this.limpiarTexto(
          raw.observaciones
        ),

      fkSeguridadCreacion:
        raw.fkSeguridadCreacion
        ?? null,

      fechaCreacion:
        raw.fechaCreacion
        ?? null,

      fkSeguridadEdicion:
        raw.fkSeguridadEdicion
        ?? null,

      fechaEdicion:
        raw.fechaEdicion
        ?? null
    };
  }

  // ============================================================
  // NAVEGACIÓN
  // ============================================================

  volver(): void {
    this.router.navigate([
      '/hoja-vida/condiciones-proteccion'
    ]);
  }

  // ============================================================
  // UTILIDADES
  // ============================================================

  private limpiarTexto(
    valor:
      string
      | null
      | undefined
  ): string | null {

    const texto =
      String(
        valor ?? ''
      ).trim();

    return texto.length > 0
      ? texto
      : null;
  }
}
