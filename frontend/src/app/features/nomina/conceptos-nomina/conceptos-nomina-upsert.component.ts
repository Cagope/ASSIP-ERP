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
          codigoConcepto: data.codigoConcepto ?? '',
          nombreConcepto: data.nombreConcepto ?? '',
          tipoConcepto: data.tipoConcepto ?? '',

          esFijo: data.esFijo ?? false,
          activo: data.activo ?? true,

          tipoCalculo: data.tipoCalculo ?? 'MANUAL',
          baseCalculo: data.baseCalculo ?? null,
          multiplicador: data.multiplicador ?? 1,

          afectaIbc: data.afectaIbc ?? false,
          afectaBaseCesantias: data.afectaBaseCesantias ?? false,
          afectaBasePrimaLegal: data.afectaBasePrimaLegal ?? false,
          afectaBaseVacaciones: data.afectaBaseVacaciones ?? false,
          afectaBasePrimaSemestral: data.afectaBasePrimaSemestral ?? false,
          afectaBaseArl: data.afectaBaseArl ?? false,
          afectaBaseParafiscales: data.afectaBaseParafiscales ?? false,

          smmlvDesde: data.smmlvDesde ?? null,
          smmlvHasta: data.smmlvHasta ?? null
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

    const codigo = (this.form.codigoConcepto ?? '').trim();
    const nombre = (this.form.nombreConcepto ?? '').trim();
    const tipo = (this.form.tipoConcepto ?? '').trim();

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

    if (!this.form.tipoCalculo) {
      alert('El tipo de cálculo es obligatorio.');
      return;
    }

    if (this.form.smmlvDesde != null && this.form.smmlvDesde < 0) {
      alert('SMMLV desde no puede ser negativo.');
      return;
    }

    if (this.form.smmlvHasta != null && this.form.smmlvHasta < 0) {
      alert('SMMLV hasta no puede ser negativo.');
      return;
    }

    if (
      this.form.smmlvDesde != null &&
      this.form.smmlvHasta != null &&
      this.form.smmlvHasta < this.form.smmlvDesde
    ) {
      alert('SMMLV hasta no puede ser menor que SMMLV desde.');
      return;
    }

    this.guardando = true;

    const payload: ConceptoNominaFormDTO = {
      ...this.form,
      codigoConcepto: this.codigo ? this.codigo : codigo,
      nombreConcepto: nombre,
      tipoConcepto: tipo,
      baseCalculo: this.form.baseCalculo || null,
      multiplicador: this.form.multiplicador ?? null,
      smmlvDesde: this.form.smmlvDesde ?? null,
      smmlvHasta: this.form.smmlvHasta ?? null
    };

    if (this.codigo) {

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

      this.api.crear(payload).subscribe({
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
      codigoConcepto: '',
      nombreConcepto: '',
      tipoConcepto: '',

      esFijo: false,
      activo: true,

      tipoCalculo: 'MANUAL',
      baseCalculo: null,
      multiplicador: 1,

      afectaIbc: false,
      afectaBaseCesantias: false,
      afectaBasePrimaLegal: false,
      afectaBaseVacaciones: false,
      afectaBasePrimaSemestral: false,
      afectaBaseArl: false,
      afectaBaseParafiscales: false,

      smmlvDesde: null,
      smmlvHasta: null
    };
  }
}
