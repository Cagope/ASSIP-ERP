import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { tap } from 'rxjs/operators';

import { DatosFamiliaresApi } from './datos-familiares.api';
import { DatosFamiliar } from '../../../shared/models/datos-familiar.model';
import { CatalogosApi, CodigoNombreDTO, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';
import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';
import { Output, EventEmitter, Input, OnChanges, SimpleChanges } from '@angular/core';


/**
 * 👨‍👩‍👧‍👦 Componente Upsert (Crear / Editar) — Datos Familiares
 * ------------------------------------------------------------
 * Gestiona la información de los familiares de una persona.
 * Incluye datos personales, ubicación, contacto y situación económica.
 */
@Component({
  selector: 'app-datos-familiares-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NumericFormatDirective],
  templateUrl: './datos-familiares-upsert.component.html',
  styleUrls: ['./datos-familiares-upsert.component.scss']
})
export class DatosFamiliaresUpsertComponent implements OnInit, OnChanges {

  // 🧩 Inyección de dependencias
  @Output() formularioValido = new EventEmitter<boolean>();
  @Input() idDatosPersonal?: number;
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(DatosFamiliaresApi);
  private readonly catalogos = inject(CatalogosApi);

  // 📋 Propiedades
  form!: FormGroup;
  editando = false;

  // 📚 Catálogos
  departamentos: Departamento[] = [];
  ciudades: Ciudad[] = [];
  parentescos: CodigoNombreDTO[] = [];

  // 🚀 Inicialización
  ngOnInit(): void {
    this.crearFormulario();

    const id = this.route.snapshot.paramMap.get('id');
    const idDatosPersonalParam = this.route.snapshot.queryParamMap.get('idDatosPersonal');

    // 1️⃣ Cargar catálogos base
    this.cargarCatalogos().subscribe({
      next: () => {
        // 2️⃣ Si estamos editando, cargar el registro existente
        if (id) {
          this.editando = true;
          this.api.obtener(+id).subscribe({
            next: (data) => {
              this.form.patchValue(data);

              // ✅ Emitir validez inicial después de aplicar valores
              queueMicrotask(() => {
                this.formularioValido.emit(this.form.valid);
              });
            },
            error: (err) => console.error('❌ Error al cargar datos familiares:', err)
          });
        } else {
          // 🟢 Caso nuevo — desde wizard o ruta con query param
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

        // 🟡 Escuchar cambios de validez (después de inicialización)
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

  // 🧱 Crear formulario reactivo
  private crearFormulario(): void {
    this.form = this.fb.group({
      idDatosFamiliares: [null],
      idDatosPersonal: [null, Validators.required],

      // === Datos básicos ===
      codigoParentesco: [null, Validators.required],
      nombreDatosFamiliar: ['', [Validators.required, Validators.maxLength(100)]],
      documentoDatosFamiliar: ['', [Validators.maxLength(20)]],
      direccionDatosFamiliar: ['', [Validators.required, Validators.maxLength(100)]],

      // === Ubicación y contacto ===
      idDepartamento: [null, Validators.required],
      idCiudad: [null, Validators.required],
      telefonoDatosFamiliar: ['', Validators.pattern(/^[0-9]{7}$/)],
      celularDatosFamiliar: ['', Validators.pattern(/^[0-9]{10}$/)],

      // === Situación económica ===
      ingresosDatosFamiliar: [0],
      egresosDatosFamiliar: [0],

      // === Referencia Familiar ===
      referenciaFamiliar: [false, Validators.required],

      // === Auditoría ===
      fkSeguridadCreacion: [1],
      fkSeguridadEdicion: [1]
    });

    // ✅ Emitir estado inicial
    this.formularioValido.emit(this.form.valid);

    // ✅ Detectar cambios en tiempo real
    this.form.statusChanges.subscribe(() => {
      this.formularioValido.emit(this.form.valid);
    });
  }


  // 📦 Cargar catálogos base
  private cargarCatalogos() {
    return forkJoin({
      parentescos: this.catalogos.listarParentescos(),
      departamentos: this.catalogos.listarDepartamentos()
    }).pipe(
      tap((res) => {
        this.parentescos = res.parentescos;
        this.departamentos = res.departamentos;
      })
    );
  }

  // 💾 Guardar o actualizar registro
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

    const raw = { ...this.form.getRawValue() } as DatosFamiliar;

    // 🧹 Limpieza antes de enviar
    const parsed: any = {
      ...raw,
      idDatosPersonal: Number(raw.idDatosPersonal),
      idDepartamento: raw.idDepartamento ? Number(raw.idDepartamento) : null,
      idCiudad: raw.idCiudad ? Number(raw.idCiudad) : null,
      codigoParentesco: raw.codigoParentesco ? Number(raw.codigoParentesco) : null,
      ingresosDatosFamiliar: Number(raw.ingresosDatosFamiliar || 0),
      egresosDatosFamiliar: Number(raw.egresosDatosFamiliar || 0),
      telefonoDatosFamiliar: raw.telefonoDatosFamiliar?.trim() || null,
      celularDatosFamiliar: raw.celularDatosFamiliar?.trim() || null,
      documentoDatosFamiliar: raw.documentoDatosFamiliar?.trim() || null,
      direccionDatosFamiliar: raw.direccionDatosFamiliar?.trim(),
    };

    const id = this.form.get('idDatosFamiliares')?.value;

    // 🔹 Recuperar idDatosPersonal si vino por query param (modo CRUD)
    if (!parsed.idDatosPersonal) {
      const idDP = this.route.snapshot.queryParamMap.get('idDatosPersonal');
      if (idDP) parsed.idDatosPersonal = +idDP;
    }

    const accion = this.editando && id
      ? this.api.actualizar(id, parsed)
      : this.api.crear(parsed);

    accion.subscribe({
      next: () => {
        alert('✅ Información familiar guardada correctamente.');

        if (this.idDatosPersonal) {
          // 🟢 Modo wizard (no navegar, solo confirmar)
          console.log('🟢 Datos familiares vinculados al ID:', this.idDatosPersonal);
        } else {
          // 🟡 Modo CRUD individual
          this.router.navigate(['/hoja-vida/datos-familiares']);
        }
      },
      error: (err) => {
        console.error('❌ Error al guardar información familiar:', err);
        alert('⚠️ Error al guardar. Revise los datos o consulte la consola.');
      }
    });
  }


  // 🔙 Volver al listado
  volver(): void {
    this.router.navigate(['/hoja-vida/datos-familiares']);
  }
}
