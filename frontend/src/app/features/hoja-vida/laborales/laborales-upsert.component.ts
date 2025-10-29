import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { tap } from 'rxjs/operators';

import { LaboralesApi } from './laborales.api';
import { Laboral } from '../../../shared/models/laboral.model';
import { CatalogosApi, CodigoNombreDTO, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';

/**
 * 💼 Componente Upsert (Crear / Editar) — Laborales
 * ------------------------------------------------------------
 * Gestiona la información laboral asociada a una persona.
 * Secciones principales:
 *   - Información de la empresa o actividad
 *   - Contacto y contrato
 *   - Ubicación (país, departamento, ciudad)
 */
@Component({
  selector: 'app-laborales-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './laborales-upsert.component.html',
  styleUrls: ['./laborales-upsert.component.scss']
})
export class LaboralesUpsertComponent implements OnInit {
  // ================================================================
  // 🧩 Inyección de dependencias
  // ================================================================
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(LaboralesApi);
  private readonly catalogos = inject(CatalogosApi);

  // ================================================================
  // 📋 Propiedades
  // ================================================================
  form!: FormGroup;
  editando = false;

  // === 📚 Catálogos (para combos dependientes) ===
  paises: CodigoNombreDTO[] = [];
  departamentos: Departamento[] = [];
  ciudades: Ciudad[] = [];
  tiposEmpresas: CodigoNombreDTO[] = [];
  tiposContratos: CodigoNombreDTO[] = [];
  jornadasLaborales: CodigoNombreDTO[] = [];

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
        // 2️⃣ Si estamos editando, cargar el registro existente
        if (id) {
          this.editando = true;
          this.api.obtener(+id).subscribe({
            next: (data) => this.form.patchValue(data),
            error: (err) => console.error('❌ Error al cargar datos laborales:', err)
          });
        } else if (idDatosPersonalParam) {
          // Si viene desde el listado de personas (relación 1:1)
          const idDatosPersonal = Number(idDatosPersonalParam);
          this.form.get('idDatosPersonal')?.setValue(idDatosPersonal);
          this.form.get('idDatosPersonal')?.disable();
        }
      },
      error: (err) => console.error('❌ Error cargando catálogos:', err)
    });

    // === Escuchar dependencias ===
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
  // 🧱 Configuración del formulario reactivo
  // ================================================================
  private crearFormulario(): void {
    this.form = this.fb.group({
      idLaboral: [null],
      idDatosPersonal: [null, Validators.required],

      // --- Información de la empresa o actividad ---
      nombreEmpresa: ['', [Validators.required, Validators.maxLength(100)]],
      direccion: ['', [Validators.required, Validators.maxLength(100)]],
      idPais: [null, Validators.required],
      idDepartamento: [null, Validators.required],
      idCiudad: [null, Validators.required],

      // --- Contacto ---
      telefonoEmpresa: ['', Validators.pattern(/^[0-9]{7}$/)],
      celularEmpresa: ['', Validators.pattern(/^[0-9]{10}$/)],
      correoEmpresa: ['', Validators.email],

      // --- Contrato y tipo ---
      codigoTipoEmpresa: [null],
      empleadoEntidad: [false],
      codigoTipoContrato: [null],
      codigoJornada: [null],

      // --- Persona de contacto ---
      nombreContacto: ['', Validators.maxLength(100)],
      celularContacto: ['', Validators.pattern(/^[0-9]{10}$/)],

      // --- Fechas y seguridad ---
      fechaVinculacion: [null],
      fkSeguridadCreacion: [1],
      fkSeguridadEdicion: [1],
    });
  }

  // ================================================================
  // 📦 Cargar catálogos base (paises, departamentos, tipos, jornadas)
  // ================================================================
  private cargarCatalogos() {
    return forkJoin({
      paises: this.catalogos.listarPaises(),
      departamentos: this.catalogos.listarDepartamentos(),
      tiposEmpresas: this.catalogos.listarTiposEmpresas(),
      tiposContratos: this.catalogos.listarTiposContratos(),
      jornadasLaborales: this.catalogos.listarJornadasLaborales(),
    }).pipe(
      tap((res) => {
        this.paises = res.paises;
        this.departamentos = res.departamentos;
        this.tiposEmpresas = res.tiposEmpresas;
        this.tiposContratos = res.tiposContratos;
        this.jornadasLaborales = res.jornadasLaborales;
      })
    );
  }

  // ================================================================
  // 💾 Guardar o actualizar registro
  // ================================================================
  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      alert('⚠️ Complete los campos obligatorios o revise los formatos.');
      return;
    }

    const raw = { ...this.form.getRawValue() } as Laboral;
    const id = this.form.get('idLaboral')?.value;

    // Si el formulario tiene idDatosPersonal deshabilitado, se incluye igual
    if (!raw.idDatosPersonal) {
      const idDP = this.route.snapshot.queryParamMap.get('idDatosPersonal');
      if (idDP) raw.idDatosPersonal = +idDP;
    }

    const accion = this.editando && id
      ? this.api.actualizar(id, raw)
      : this.api.crear(raw);

    accion.subscribe({
      next: () => {
        alert('✅ Información laboral guardada correctamente.');
        this.router.navigate(['/hoja-vida/laborales']);
      },
      error: (err) => console.error('❌ Error al guardar información laboral:', err)
    });
  }

  // ================================================================
  // 🔙 Volver al listado
  // ================================================================
  volver(): void {
    this.router.navigate(['/hoja-vida/laborales']);
  }
}
