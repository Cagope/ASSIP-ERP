import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { tap } from 'rxjs/operators';

import { UbicacionesApi } from './ubicaciones.api';
import { Ubicacion } from '../../../shared/models/ubicacion.model';
import { CatalogosApi, CodigoNombreDTO, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';
import { GeneralApi, ZonaDTO, SubZonaDTO } from '../../../shared/general/general.api';
import { Output, EventEmitter, Input, OnChanges, SimpleChanges } from '@angular/core';

/**
 * 🏠 Componente Upsert (Crear / Editar) — Ubicaciones
 * Gestiona la información geográfica y de contacto asociada a una persona.
 *
 * Secciones:
 *  - Dirección y contacto
 *  - Ubicación geográfica (País, Departamento, Ciudad, Zona, SubZona)
 */
@Component({
  selector: 'app-ubicaciones-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './ubicaciones-upsert.component.html',
  styleUrls: ['./ubicaciones-upsert.component.scss']
})
export class UbicacionesUpsertComponent implements OnInit, OnChanges {
  // === 🧩 Inyección de dependencias ===
  @Output() formularioValido = new EventEmitter<boolean>();
  @Input() idDatosPersonal?: number;
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(UbicacionesApi);
  private readonly catalogos = inject(CatalogosApi);
  private readonly general = inject(GeneralApi);

  // === 📋 Propiedades ===
  form!: FormGroup;
  editando = false;

  // === 📚 Catálogos ===
  paises: CodigoNombreDTO[] = [];
  departamentos: Departamento[] = [];
  ciudades: Ciudad[] = [];
  zonas: ZonaDTO[] = [];
  subZonasFiltradas: SubZonaDTO[] = [];

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
        // 2️⃣ Si estamos editando, cargar la ubicación existente
        if (id) {
          this.editando = true;
          this.api.obtener(+id).subscribe({
            next: (data) => {
              this.form.patchValue(data);

              // Si tiene zona, cargar las subzonas de esa zona
              if (data.idZona) {
                this.general.listarSubZonasPorZona(data.idZona).subscribe({
                  next: (res) => {
                    this.subZonasFiltradas = res;
                    this.form.get('idSubZona')?.setValue(data.idSubZona ?? null);
                  },
                  error: () => (this.subZonasFiltradas = [])
                });
              }

              // ✅ Emitir solo después de aplicar los valores reales
              queueMicrotask(() => {
                this.formularioValido.emit(this.form.valid);
              });
            },
            error: (err) => console.error('❌ Error al cargar ubicación:', err)
          });
        } else {
          // 🟩 Caso NUEVO (desde wizard)
          const idDatosPersonal = idDatosPersonalParam
            ? Number(idDatosPersonalParam)
            : this.idDatosPersonal;

          if (idDatosPersonal) {
            console.log('🔗 ID recibido (wizard o parámetro):', idDatosPersonal);
            this.form.get('idDatosPersonal')?.setValue(idDatosPersonal);
            this.form.get('idDatosPersonal')?.disable();
          }

          // ✅ Emitir estado inicial (formulario vacío o con ID recibido)
          queueMicrotask(() => {
            this.formularioValido.emit(this.form.valid);
          });
        }

        // ✅ Emitir estado final tras cargar catálogos (seguridad adicional)
        this.formularioValido.emit(this.form.valid);

        // 🟡 Escuchar cambios de validez (después de inicialización)
        this.form.statusChanges.subscribe(() => {
          this.formularioValido.emit(this.form.valid);
        });
      },
      error: (err) => console.error('❌ Error cargando catálogos:', err)
    });

    // === Escuchar dependencias ===

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

    this.form.get('idZona')?.valueChanges.subscribe((idZona) => {
      if (idZona) {
        this.general.listarSubZonasPorZona(idZona).subscribe({
          next: (data) => (this.subZonasFiltradas = data),
          error: () => (this.subZonasFiltradas = [])
        });
      } else {
        this.subZonasFiltradas = [];
        this.form.get('idSubZona')?.setValue(null);
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
  // 🧱 Configuración del formulario reactivo
  // ================================================================
  private crearFormulario(): void {
    this.form = this.fb.group({
      idUbicacion: [null],
      idDatosPersonal: [null, Validators.required],

      // --- Dirección y contacto ---
      direccion: ['', [Validators.required, Validators.maxLength(100)]],
      barrio: ['', Validators.maxLength(100)],
      telefono: ['', Validators.pattern(/^[0-9]{7}$/)],
      celularUno: ['', Validators.pattern(/^[0-9]{10}$/)],
      celularDos: ['', Validators.pattern(/^[0-9]{10}$/)],
      correo: ['', Validators.email],

      // --- Ubicación geográfica ---
      idPais: [null, Validators.required],
      idDepartamento: [null, Validators.required],
      idCiudad: [null, Validators.required],
      idZona: [null],
      idSubZona: [null],

      // --- Seguridad ---
      fkSeguridadCreacion: [1],
      fkSeguridadEdicion: [1],
    });
  }

  // ================================================================
  // 📦 Cargar catálogos base (paises, departamentos, zonas)
  // ================================================================
  private cargarCatalogos() {
    return forkJoin({
      paises: this.catalogos.listarPaises(),
      departamentos: this.catalogos.listarDepartamentos(),
      zonas: this.general.listarZonas(),
    }).pipe(
      tap((res) => {
        this.paises = res.paises;
        this.departamentos = res.departamentos;
        this.zonas = res.zonas;
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

    const raw = { ...this.form.getRawValue() } as Ubicacion;
    const id = this.form.get('idUbicacion')?.value;

    const accion = this.editando && id
      ? this.api.actualizar(id, raw)
      : this.api.crear(raw);

    accion.subscribe({
      next: () => {
        alert('✅ Ubicación guardada correctamente.');
        this.router.navigate(['/hoja-vida/ubicaciones']);
      },
      error: (err) => console.error('❌ Error al guardar ubicación:', err)
    });
  }

  // ================================================================
  // 🔙 Volver
  // ================================================================
  volver(): void {
    this.router.navigate(['/hoja-vida/ubicaciones']);
  }
}
