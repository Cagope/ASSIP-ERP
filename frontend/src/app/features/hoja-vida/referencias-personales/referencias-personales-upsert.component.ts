import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { tap } from 'rxjs/operators';

import { ReferenciasPersonalesApi } from './referencias-personales.api';
import { ReferenciaPersonal } from '../../../shared/models/referencia-personal.model';
import { CatalogosApi, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';

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
export class ReferenciasPersonalesUpsertComponent implements OnInit {

  // ================================================================
  // ⚙️ Inyección de dependencias
  // ================================================================
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

    // 1️⃣ Cargar catálogos
    this.cargarCatalogos().subscribe({
      next: () => {
        // 2️⃣ Si es edición → cargar registro existente
        if (id) {
          this.editando = true;
          this.api.obtener(+id).subscribe({
            next: (data) => {
              this.form.patchValue(data);

              // Cargar ciudades según el departamento
              if (data.idDepartamento) {
                this.catalogos.listarCiudadesPorDepartamento(data.idDepartamento).subscribe({
                  next: (res) => (this.ciudades = res),
                  error: () => (this.ciudades = [])
                });
              }
            },
            error: (err) => console.error('❌ Error al cargar referencia personal:', err)
          });
        } else if (idDatosPersonalParam) {
          // 3️⃣ Si viene desde listado de personas
          const idDatosPersonal = Number(idDatosPersonalParam);
          this.form.get('idDatosPersonal')?.setValue(idDatosPersonal);
          this.form.get('idDatosPersonal')?.disable();
        }
      },
      error: (err) => console.error('❌ Error cargando catálogos:', err)
    });

    // 🟩 Departamento → Ciudades
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

    const raw = { ...this.form.getRawValue() } as ReferenciaPersonal;
    const id = this.form.get('idReferenciaPersonal')?.value;

    const accion = this.editando && id
      ? this.api.actualizar(id, raw)
      : this.api.crear(raw);

    accion.subscribe({
      next: () => {
        alert('✅ Referencia personal guardada correctamente.');
        this.router.navigate(['/hoja-vida/referencias-personales']);
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
