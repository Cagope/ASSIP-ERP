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
export class DatosFamiliaresUpsertComponent implements OnInit {

  // 🧩 Inyección de dependencias
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
            next: (data) => this.form.patchValue(data),
            error: (err) => console.error('❌ Error al cargar datos familiares:', err)
          });
        } else if (idDatosPersonalParam) {
          const idDatosPersonal = Number(idDatosPersonalParam);
          this.form.get('idDatosPersonal')?.setValue(idDatosPersonal);
          this.form.get('idDatosPersonal')?.disable();
        }
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

  // 🧱 Crear formulario reactivo
  private crearFormulario(): void {
    this.form = this.fb.group({
      idDatosFamiliares: [null],
      idDatosPersonal: [null, Validators.required],

      // === Datos básicos ===
      codigoParentesco: [null, Validators.required],
      nombreDatosFamiliar: ['', [Validators.required, Validators.maxLength(100)]],
      documentoDatosFamiliar: ['', [Validators.maxLength(20)]], // opcional
      direccionDatosFamiliar: ['', [Validators.required, Validators.maxLength(100)]], // ✅ obligatorio

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

    const accion = this.editando && id
      ? this.api.actualizar(id, parsed)
      : this.api.crear(parsed);

    accion.subscribe({
      next: () => {
        alert('✅ Información familiar guardada correctamente.');
        this.router.navigate(['/hoja-vida/datos-familiares']);
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
