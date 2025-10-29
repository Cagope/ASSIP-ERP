import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AgenciasApi, Agencia } from './agencia.api';
import { CatalogosApi, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-agencia-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './agencia-upsert.component.html',
  styleUrls: ['./agencia-upsert.component.scss']
})
export class AgenciaUpsertComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(AgenciasApi);
  private readonly catalogos = inject(CatalogosApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  form!: FormGroup;
  id?: number;
  cargando = false;
  error?: string;
  titulo = 'nueva agencia';

  departamentos: Departamento[] = [];
  ciudades: Ciudad[] = [];

  ngOnInit(): void {
    this.form = this.fb.group({
      idAgencia: [],
      codigoAgencia: ['', Validators.required],
      nombreAgencia: ['', Validators.required],
      siglaAgencia: [''],
      direccionAgencia: [''],
      idDepartamento: [null],
      idCiudad: [null],
      correoAgencia: ['', Validators.email],
      celularAgencia: [''],
      telefonoAgencia: ['']
    });

    this.cargarCatalogosBase();

    // Detectar modo edición
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    if (this.id) {
      this.titulo = 'editar agencia';
      this.cargar();
    }
  }

  private cargarCatalogosBase(): void {
    this.catalogos.listarDepartamentos().subscribe({
      next: (data) => (this.departamentos = data || []),
      error: () => (this.departamentos = [])
    });

    // Reaccionar a cambio de departamento
    this.form.get('idDepartamento')?.valueChanges.subscribe((idDepto) => {
      this.form.patchValue({ idCiudad: null });
      if (idDepto) {
        this.catalogos.listarCiudadesPorDepartamento(idDepto).subscribe({
          next: (data) => (this.ciudades = data || []),
          error: () => (this.ciudades = [])
        });
      } else {
        this.ciudades = [];
      }
    });
  }

  private cargar(): void {
    this.cargando = true;
    this.api.obtener(this.id!).subscribe({
      next: (data) => {
        this.form.patchValue(data);
        this.cargando = false;

        // Cargar ciudades si hay departamento
        const idDepto = this.form.value.idDepartamento;
        if (idDepto) {
          this.catalogos.listarCiudadesPorDepartamento(idDepto).subscribe({
            next: (data) => (this.ciudades = data || [])
          });
        }
      },
      error: (err: HttpErrorResponse) => {
        this.error = err.message || 'Error cargando agencia';
        this.cargando = false;
      }
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      alert('⚠️ Por favor complete los campos obligatorios.');
      return;
    }

    const data: Agencia = this.form.value;
    this.cargando = true;

    const peticion = this.id
      ? this.api.actualizar(this.id, data)
      : this.api.crear(data);

    peticion.subscribe({
      next: () => {
        alert('✅ Agencia guardada correctamente.');
        this.router.navigateByUrl('/general/agencias');
      },
      error: (err: HttpErrorResponse) => {
        alert('❌ Error guardando la agencia: ' + (err.error?.message || err.message));
        this.cargando = false;
      }
    });
  }

  cancelar(): void {
    this.router.navigateByUrl('/general/agencias');
  }
}
