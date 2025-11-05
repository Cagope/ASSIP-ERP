import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { tap } from 'rxjs/operators';

import { ReferenciasPersonalesApi } from './referencias-personales.api';
import { ReferenciaPersonal } from '../../../shared/models/referencia-personal.model';
import { CatalogosApi, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';
import { Output, EventEmitter, Input, OnChanges, SimpleChanges } from '@angular/core';

/**
 * 👥 Componente Upsert (Crear / Editar) — Referencias Personales
 * ------------------------------------------------------------
 * Permite registrar o actualizar las referencias personales de una persona.
 *
 * Secciones:
 *  - Datos de la referencia (nombre y dirección)
 *  - Ubicación (departamento y ciudad)
 *  - Contacto (teléfono y celular)
 */
@Component({
  selector: 'app-referencias-personales-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './referencias-personales-upsert.component.html',
  styleUrls: ['./referencias-personales-upsert.component.scss']
})
export class ReferenciasPersonalesUpsertComponent implements OnInit, OnChanges {

  // ================================================================
  // ⚙️ Inyección de dependencias
  // ================================================================
  @Output() formularioValido = new EventEmitter<boolean>();
  @Input() idDatosPersonal?: number;
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(ReferenciasPersonalesApi);
  private readonly catalogos = inject(CatalogosApi);

  // ================================================================
  // 📋 Propiedades
  // ================================================================
  form!: FormGroup;
  editando = false;

  departamentos: Departamento[] = [];
  ciudades: Ciudad[] = [];

  // ================================================================
  // 🚀 Inicialización
  // ================================================================
  ngOnInit(): void {
    this.crearFormulario();

    const id = this.route.snapshot.paramMap.get('id');
    const idDatosPersonalParam = this.route.snapshot.queryParamMap.get('idDatosPersonal');

    // 1️⃣ Cargar catálogos base
    this.cargarCatalogos().subscribe({
      next: () => {
        // 2️⃣ Si estamos editando
        if (id) {
          this.editando = true;
          this.api.obtener(+id).subscribe({
            next: (data) => {
              this.form.patchValue(data);

              // 🟦 Cargar ciudades según el departamento
              if (data.idDepartamento) {
                this.catalogos.listarCiudadesPorDepartamento(data.idDepartamento).subscribe({
                  next: (res) => (this.ciudades = res),
                  error: () => (this.ciudades = [])
                });
              }

              // ✅ Emitir validez inicial
              queueMicrotask(() => {
                this.formularioValido.emit(this.form.valid);
              });
            },
            error: (err) => console.error('❌ Error al cargar referencia personal:', err)
          });
        } else {
          // 🟢 Nuevo registro — desde wizard o query param
          const idDatosPersonal = idDatosPersonalParam
            ? Number(idDatosPersonalParam)
            : this.idDatosPersonal;

          if (idDatosPersonal) {
            this.form.get('idDatosPersonal')?.setValue(idDatosPersonal);
            this.form.updateValueAndValidity({ emitEvent: true }); // 🔹 nueva línea
          }

          // ✅ Emitir estado inicial (form vacío)
          queueMicrotask(() => {
            this.formularioValido.emit(this.form.valid);
          });
        }

        // 🟡 Escuchar cambios de validez
        this.form.statusChanges.subscribe(() => {
          this.formularioValido.emit(this.form.valid);
        });
      },
      error: (err) => console.error('❌ Error cargando catálogos:', err)
    });

    // 🟩 Dependencia: Departamento → Ciudades
    this.form.get('idDepartamento')?.valueChanges.subscribe((idDepto) => {
      if (idDepto) {
        this.catalogos.listarCiudadesPorDepartamento(idDepto).subscribe({
          next: (data) => (this.ciudades = data),
          error: () => (this.ciudades = [])
        });
      } else {
        this.ciudades = [];
      }
    });
  }

  // ================================================================
  // 🔗 Detectar cambios en el ID recibido desde el wizard
  // ================================================================
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['idDatosPersonal'] && this.idDatosPersonal) {
      console.log('🔗 Recibido idDatosPersonal desde wizard:', this.idDatosPersonal);
      this.form.get('idDatosPersonal')?.setValue(this.idDatosPersonal);
      this.form.updateValueAndValidity();
      this.formularioValido.emit(this.form.valid);
    }
  }

  // ================================================================
  // 🧱 Configuración del formulario
  // ================================================================
  private crearFormulario(): void {
    this.form = this.fb.group({
      idReferenciaPersonal: [null],
      idDatosPersonal: [null, Validators.required],
      nombreReferenciaPersonal: ['', [Validators.required, Validators.maxLength(100)]],
      direccionReferenciaPersonal: ['', [Validators.required, Validators.maxLength(100)]],
      idDepartamento: [null],
      idCiudad: [null],
      telefonoReferenciaPersonal: ['', Validators.pattern(/^[0-9]{7}$/)],
      celularReferenciaPersonal: ['', Validators.pattern(/^[0-9]{10}$/)],
      fkSeguridadCreacion: [1],
      fkSeguridadEdicion: [1],
    });

    // ✅ Emitir estado inicial
    this.formularioValido.emit(this.form.valid);

    // ✅ Detectar cambios en tiempo real
    this.form.statusChanges.subscribe(() => {
      this.formularioValido.emit(this.form.valid);
    });
  }


  // ================================================================
  // 📦 Cargar catálogos base
  // ================================================================
  private cargarCatalogos() {
    return forkJoin({
      departamentos: this.catalogos.listarDepartamentos(),
    }).pipe(
      tap((res) => {
        this.departamentos = res.departamentos;
      })
    );
  }

  // ================================================================
  // 💾 Guardar o actualizar
  // ================================================================
  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      alert('⚠️ Complete los campos obligatorios o revise los formatos.');
      return;
    }

    // ✅ Si el formulario NO tiene idDatosPersonal y el wizard lo pasó, lo asignamos
    if (!this.form.get('idDatosPersonal')?.value && this.idDatosPersonal) {
      this.form.get('idDatosPersonal')?.setValue(this.idDatosPersonal);
    }

    const raw = { ...this.form.getRawValue() } as ReferenciaPersonal;
    const id = this.form.get('idReferenciaPersonal')?.value;

    // 🔹 Recuperar idDatosPersonal si vino por query param (modo CRUD)
    if (!raw.idDatosPersonal) {
      const idDP = this.route.snapshot.queryParamMap.get('idDatosPersonal');
      if (idDP) raw.idDatosPersonal = +idDP;
    }

    const accion = this.editando && id
      ? this.api.actualizar(id, raw)
      : this.api.crear(raw);

    accion.subscribe({
      next: () => {
        alert('✅ Referencia personal guardada correctamente.');

        if (this.idDatosPersonal) {
          // 🟢 Modo wizard — no redirigir
          console.log('🟢 Referencia vinculada a ID:', this.idDatosPersonal);
        } else {
          // 🟡 Modo CRUD individual
          this.router.navigate(['/hoja-vida/referencias-personales']);
        }
      },
      error: (err) => console.error('❌ Error al guardar referencia personal:', err)
    });
  }


  // ================================================================
  // 🔙 Volver
  // ================================================================
  volver(): void {
    this.router.navigate(['/hoja-vida/referencias-personales']);
  }
}
