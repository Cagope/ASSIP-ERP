import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

import { EmpleadoAutocompleteComponent } from '../busqueda/empleado-autocomplete.component';
import {
  EmpleadoContratosApi,
  EmpleadoContratoListDTO
} from '../empleado-contratos/empleado-contratos.api';

import {
  EventosLiquidacionApi,
  EventoLiquidacionSaveDTO
} from './eventos-liquidacion.api';

@Component({
  standalone: true,
  selector: 'app-eventos-liquidacion-upsert',
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    EmpleadoAutocompleteComponent
  ],
  templateUrl: './eventos-liquidacion-upsert.component.html',
  styleUrls: ['./eventos-liquidacion-upsert.component.scss']
})
export class EventosLiquidacionUpsertComponent implements OnInit {

  private fb = inject(FormBuilder);
  private api = inject(EventosLiquidacionApi);
  private contratosApi = inject(EmpleadoContratosApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  id: number | null = null;
  cargando = false;
  guardando = false;
  error = '';

  contratosEmpleado: EmpleadoContratoListDTO[] = [];
  contratoActivo: EmpleadoContratoListDTO | null = null;

  // 🔹 apoyo para autocomplete
  idEmpleadoSeleccionado: number | null = null;

  tiposEvento = [
    'INCAPACIDAD_EPS',
    'LICENCIA_MATERNIDAD',
    'LICENCIA_ARL',
    'LICENCIA_REMUNERADA',
    'LICENCIA_NO_REMUNERADA'
  ];

  responsablesPago = ['EMPRESA', 'EPS', 'ARL'];
  estados = ['ACTIVO', 'INACTIVO', 'ANULADO'];

  form = this.fb.group({
    idEventoLiquidacion: [null as number | null],
    idEmpleado: [null as number | null, Validators.required],
    idContrato: [null as number | null, Validators.required],

    fechaDocumento: [null as string | null, Validators.required],
    fechaInicio: [null as string | null, Validators.required],
    fechaFin: [null as string | null, Validators.required],
    totalDias: [null as number | null, Validators.required],

    tipoEvento: ['INCAPACIDAD_EPS' as string | null, Validators.required],
    numeroSoporte: [null as string | null],
    responsablePago: ['EPS' as string | null, Validators.required],

    porcentajeResponsable: [66 as number | null, Validators.required],
    porcentajeEmpresa: [34 as number | null, Validators.required],
    diasEmpresa100: [2 as number | null, Validators.required],

    generaCxc: [true as boolean | null],
    liquidaArl: [true as boolean | null],
    esRemunerado: [true as boolean | null],

    observacion: [null as string | null],
    estado: ['ACTIVO' as string | null, Validators.required]
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.id = idParam ? Number(idParam) : null;

    this.form.get('tipoEvento')?.valueChanges.subscribe(tipo => {
      this.aplicarDefaultsPorTipo(tipo || '');
    });

    if (this.id) {
      this.cargar(this.id);
    }
  }

  cargar(id: number): void {
    this.cargando = true;
    this.error = '';

    this.api.obtener(id).subscribe({
      next: (data) => {
        this.form.patchValue(data);

        this.idEmpleadoSeleccionado = this.form.get('idEmpleado')?.value ?? null;

        const idEmpleado = this.form.get('idEmpleado')?.value;
        const idContrato = this.form.get('idContrato')?.value;

        if (idEmpleado) {
          this.cargarContratosEmpleado(idEmpleado, idContrato);
        }

        this.cargando = false;
      },
      error: (err) => {
        console.error(err);
        this.error = err?.error?.message || 'No fue posible cargar el registro.';
        this.cargando = false;
      }
    });
  }

  onEmpleadoChange(): void {
    const idEmpleado = this.idEmpleadoSeleccionado;

    this.form.patchValue({
      idEmpleado,
      idContrato: null
    }, { emitEvent: false });

    this.contratosEmpleado = [];
    this.contratoActivo = null;

    if (!idEmpleado) {
      return;
    }

    this.cargarContratosEmpleado(idEmpleado);
  }

  onContratoChange(): void {
    const idContrato = this.form.get('idContrato')?.value;

    this.contratoActivo =
      this.contratosEmpleado.find(c => c.idContrato === idContrato) ?? null;
  }

  private cargarContratosEmpleado(
    idEmpleado: number,
    idContratoPreferido: number | null = null
  ): void {
    this.contratosApi.listarPorEmpleado(idEmpleado).subscribe({
      next: (contratos) => {
        this.contratosEmpleado = contratos ?? [];
        this.contratoActivo = null;

        if (idContratoPreferido != null) {
          const contratoSeleccionado =
            this.contratosEmpleado.find(c => c.idContrato === idContratoPreferido) ?? null;

          if (contratoSeleccionado) {
            this.form.patchValue({
              idContrato: contratoSeleccionado.idContrato
            }, { emitEvent: false });

            this.contratoActivo = contratoSeleccionado;
            return;
          }
        }

        if (this.contratosEmpleado.length === 1) {
          this.form.patchValue({
            idContrato: this.contratosEmpleado[0].idContrato
          }, { emitEvent: false });

          this.contratoActivo = this.contratosEmpleado[0];
        }
      },
      error: (err) => {
        console.error(err);
        this.contratosEmpleado = [];
        this.contratoActivo = null;
        this.form.patchValue({
          idContrato: null
        }, { emitEvent: false });
      }
    });
  }

  aplicarDefaultsPorTipo(tipo: string): void {
    switch (tipo) {
      case 'INCAPACIDAD_EPS':
        this.form.patchValue({
          responsablePago: 'EPS',
          porcentajeResponsable: 66,
          porcentajeEmpresa: 34,
          diasEmpresa100: 2,
          generaCxc: true,
          liquidaArl: true,
          esRemunerado: true
        }, { emitEvent: false });
        break;

      case 'LICENCIA_MATERNIDAD':
        this.form.patchValue({
          responsablePago: 'EPS',
          porcentajeResponsable: 100,
          porcentajeEmpresa: 0,
          diasEmpresa100: 0,
          generaCxc: true,
          liquidaArl: true,
          esRemunerado: true
        }, { emitEvent: false });
        break;

      case 'LICENCIA_ARL':
        this.form.patchValue({
          responsablePago: 'ARL',
          porcentajeResponsable: 100,
          porcentajeEmpresa: 0,
          diasEmpresa100: 0,
          generaCxc: true,
          liquidaArl: true,
          esRemunerado: true
        }, { emitEvent: false });
        break;

      case 'LICENCIA_REMUNERADA':
        this.form.patchValue({
          responsablePago: 'EMPRESA',
          porcentajeResponsable: 100,
          porcentajeEmpresa: 0,
          diasEmpresa100: 0,
          generaCxc: false,
          liquidaArl: false,
          esRemunerado: true
        }, { emitEvent: false });
        break;

      case 'LICENCIA_NO_REMUNERADA':
        this.form.patchValue({
          responsablePago: 'EMPRESA',
          porcentajeResponsable: 0,
          porcentajeEmpresa: 0,
          diasEmpresa100: 0,
          generaCxc: false,
          liquidaArl: false,
          esRemunerado: false
        }, { emitEvent: false });
        break;
    }
  }

  recalcularDias(): void {
    const inicio = this.form.get('fechaInicio')?.value;
    const fin = this.form.get('fechaFin')?.value;

    if (!inicio || !fin) return;

    const fi = new Date(inicio + 'T00:00:00');
    const ff = new Date(fin + 'T00:00:00');

    if (ff < fi) return;

    const ms = ff.getTime() - fi.getTime();
    const dias = Math.floor(ms / (1000 * 60 * 60 * 24)) + 1;

    this.form.patchValue({
      totalDias: dias
    }, { emitEvent: false });
  }

  guardar(): void {
    this.error = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando = true;

    const raw = this.form.getRawValue();

    const dto: EventoLiquidacionSaveDTO = {
      idEventoLiquidacion: raw.idEventoLiquidacion,
      idContrato: raw.idContrato,
      fechaDocumento: raw.fechaDocumento,
      fechaInicio: raw.fechaInicio,
      fechaFin: raw.fechaFin,
      totalDias: raw.totalDias,
      tipoEvento: raw.tipoEvento,
      numeroSoporte: raw.numeroSoporte,
      responsablePago: raw.responsablePago,
      porcentajeResponsable: raw.porcentajeResponsable,
      porcentajeEmpresa: raw.porcentajeEmpresa,
      diasEmpresa100: raw.diasEmpresa100,
      generaCxc: raw.generaCxc,
      liquidaArl: raw.liquidaArl,
      esRemunerado: raw.esRemunerado,
      observacion: raw.observacion,
      estado: raw.estado
    };

    this.api.guardar(dto).subscribe({
      next: () => {
        this.guardando = false;
        this.router.navigate(['/nomina/eventos-liquidacion']);
      },
      error: (err) => {
        console.error(err);
        this.error = err?.error?.message || 'No fue posible guardar el registro.';
        this.guardando = false;
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/nomina/eventos-liquidacion']);
  }

  campoInvalido(name: string): boolean {
    const control = this.form.get(name);
    return !!control && control.invalid && (control.dirty || control.touched);
  }
}
