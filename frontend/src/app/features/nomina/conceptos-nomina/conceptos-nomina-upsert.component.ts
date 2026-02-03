import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { ConceptosNominaApi, ConceptoNominaFormDTO } from './conceptos-nomina.api';

@Component({
  standalone: true,
  selector: 'app-conceptos-nomina-upsert',
  templateUrl: './conceptos-nomina-upsert.component.html',
  styleUrls: ['./conceptos-nomina-upsert.component.scss'],
  imports: [CommonModule, FormsModule, RouterModule, HeaderActionsComponent],
})
export class ConceptosNominaUpsertComponent implements OnInit {

  private readonly api = inject(ConceptosNominaApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  codigo: string | null = null;

  loading = false;
  guardando = false;

  form: ConceptoNominaFormDTO = this.nuevo();

  ngOnInit(): void {
    const codigoParam = this.route.snapshot.paramMap.get('codigo');
    this.codigo = codigoParam ? codigoParam : null;

    if (this.codigo) {
      this.cargar(this.codigo);
    }
  }

  cargar(codigo: string): void {
    this.loading = true;

    this.api.obtener(codigo).subscribe({
      next: (data) => {
        this.form = {
          codigo: data.codigo ?? '',
          nombre: data.nombre ?? '',
          tipo: data.tipo ?? '',
          esFijo: data.esFijo ?? false,
          activo: data.activo ?? true
        };
        this.loading = false;
      },
      error: (err) => {
        console.error('Error cargando concepto nómina', err);
        this.loading = false;
        alert('No se pudo cargar el concepto.');
        this.volver();
      }
    });
  }

  guardar(): void {
    const codigo = (this.form.codigo ?? '').trim();
    const nombre = (this.form.nombre ?? '').trim();
    const tipo = (this.form.tipo ?? '').trim();

    if (!codigo) {
      alert('El código es obligatorio.');
      return;
    }
    if (!nombre) {
      alert('El nombre es obligatorio.');
      return;
    }
    if (!tipo) {
      alert('El tipo es obligatorio.');
      return;
    }

    this.guardando = true;

    if (this.codigo) {
      // ✅ editar: no dejamos cambiar codigo
      const payload: ConceptoNominaFormDTO = {
        ...this.form,
        codigo: this.codigo
      };

      this.api.actualizar(this.codigo, payload).subscribe({
        next: () => {
          this.guardando = false;
          this.volver();
        },
        error: (err) => {
          console.error('Error actualizando concepto', err);
          this.guardando = false;
          alert('No se pudo actualizar.');
        }
      });

    } else {
      this.api.crear(this.form).subscribe({
        next: () => {
          this.guardando = false;
          this.volver();
        },
        error: (err) => {
          console.error('Error creando concepto', err);
          this.guardando = false;
          alert('No se pudo crear.');
        }
      });
    }
  }

  volver(): void {
    this.router.navigate(['/nomina/conceptos-nomina']);
  }

  private nuevo(): ConceptoNominaFormDTO {
    return {
      codigo: '',
      nombre: '',
      tipo: '',
      esFijo: false,
      activo: true
    };
  }
}
