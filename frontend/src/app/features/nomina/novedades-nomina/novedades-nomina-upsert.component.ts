import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';

import { EmpleadoAutocompleteComponent } from '../busqueda/empleado-autocomplete.component';

import {
  NovedadesNominaApi,
  NovedadNominaFormDTO
} from './novedades-nomina.api';

import {
  EmpleadosApi,
  EmpleadoListDTO
} from '../empleados/empleados.api';

import {
  ConceptosNominaApi,
  ConceptoNominaListDTO
} from '../conceptos-nomina/conceptos-nomina.api';

import { EmpleadoContratosApi, EmpleadoContratoListDTO }
from '../empleado-contratos/empleado-contratos.api';

import {
  PeriodosNominaApi,
  PeriodoNominaListDTO
} from '../periodos-nomina/periodos-nomina.api';

@Component({
  standalone: true,
  selector: 'app-novedades-nomina-upsert',
  templateUrl: './novedades-nomina-upsert.component.html',
  styleUrls: ['./novedades-nomina-upsert.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    EmpleadoAutocompleteComponent
  ],
})
export class NovedadesNominaUpsertComponent implements OnInit {

  private readonly api = inject(NovedadesNominaApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private readonly empleadosApi = inject(EmpleadosApi);
  private readonly conceptosApi = inject(ConceptosNominaApi);
  private readonly contratosApi = inject(EmpleadoContratosApi);
  private readonly periodosApi = inject(PeriodosNominaApi);

  id: number | null = null;

  loading = false;
  guardando = false;

  empleados: EmpleadoListDTO[] = [];
  conceptos: ConceptoNominaListDTO[] = [];
  contratoActivo: EmpleadoContratoListDTO | null = null;
  periodoActivoTexto = '';

  // 🔥 comportamiento dinámico
  conceptoSeleccionado: ConceptoNominaListDTO | null = null;
  valorEditable = true;
  requiereCantidad = false;
  requiereFechas = false;

  form: NovedadNominaFormDTO = {
    idEmpleado: null,
    idContrato: null,
    codigoConcepto: null,
    fechaInicial: '',
    fechaFinal: '',
    cantidad: 0,
    valor: 0,
    observacion: null
  };

  ngOnInit(): void {
    this.cargarCatalogos();

    const idParam = this.route.snapshot.paramMap.get('id');
    this.id = idParam ? +idParam : null;

    if (this.id) {
      this.cargar(this.id);
    }
  }

  private cargarCatalogos(): void {

    this.empleadosApi.listar().subscribe({
      next: d => this.empleados = d ?? [],
      error: () => this.empleados = []
    });

    this.conceptosApi.listar().subscribe({
      next: d => this.conceptos = d ?? [],
      error: () => this.conceptos = []
    });

    // 🔥 SOLO PARA MOSTRAR EL PERÍODO ACTIVO
    this.periodosApi.listar().subscribe({
      next: (periodos: PeriodoNominaListDTO[]) => {

        const activo = (periodos ?? []).find(p =>
          p.estado === 'ABIERTO'
        );

        if (activo) {
          this.periodoActivoTexto =
            `${activo.anio}-${String(activo.mes).padStart(2, '0')} · Q${activo.numeroPeriodo}`;
        } else {
          this.periodoActivoTexto = 'No hay período activo';
        }
      },
      error: () => {
        this.periodoActivoTexto = 'No se pudo obtener período activo';
      }
    });
  }

  // =========================================================
  // 🔥 CAMBIO DE CONCEPTO (INTELIGENTE)
  // =========================================================
  onConceptoChange(): void {

    this.conceptoSeleccionado =
      this.conceptos.find(c => c.codigoConcepto === this.form.codigoConcepto) ?? null;

    this.valorEditable = true;
    this.requiereCantidad = false;
    this.requiereFechas = false;

    if (!this.conceptoSeleccionado) return;

    switch (this.conceptoSeleccionado.tipoCalculo) {

      case 'MANUAL':
        this.valorEditable = true;
        break;

      case 'POR_HORAS':
        this.valorEditable = false;
        this.requiereCantidad = true;
        break;

      case 'POR_DIAS':
        this.valorEditable = false;
        this.requiereCantidad = true;   // 🔥 activar cantidad
        this.requiereFechas = false;    // opcional
        break;

      case 'POR_PORCENTAJE':
        this.valorEditable = false;
        break;

      default:
        this.valorEditable = true;
        break;
    }

    // Si el valor no es editable, lo limpiamos visualmente
    if (!this.valorEditable) {
      this.form.valor = 0;
    }
  }

  onEmpleadoChange(): void {

    if (!this.form.idEmpleado) {
      this.form.idContrato = null;
      this.contratoActivo = null;
      return;
    }

    this.cargarContratoActivo(this.form.idEmpleado);
  }

  // =========================================================
  // CARGAR EDICIÓN
  // =========================================================
  cargar(id: number): void {

    this.loading = true;

    this.api.obtener(id).subscribe({
      next: (data) => {

        this.form = { ...this.form, ...data };

        // 🔥 IMPORTANTE: cargar contrato en edición
        if (data.idEmpleado) {
          this.cargarContratoActivo(data.idEmpleado);
        }

        this.onConceptoChange();

        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.volver();
      }
    });
  }

  // =========================================================
  // GUARDAR
  // =========================================================
  guardar(): void {

    console.log('Empleado seleccionado:', this.form.idEmpleado);

    if (!this.form.idEmpleado) {
      alert('Debe seleccionar un empleado.');
      return;
    }

    if (!this.form.codigoConcepto) {
      alert('Debe seleccionar un concepto.');
      return;
    }

    if (this.requiereCantidad && (!this.form.cantidad || this.form.cantidad <= 0)) {
      alert('Debe ingresar una cantidad válida.');
      return;
    }

    if (this.requiereFechas && (!this.form.fechaInicial || !this.form.fechaFinal)) {
      alert('Debe ingresar fecha inicial y final.');
      return;
    }

    this.guardando = true;

    if (this.id) {

      this.api.actualizar(this.id, this.form).subscribe({
        next: () => {
          this.guardando = false;
          this.volver();
        },
        error: () => {
          this.guardando = false;
          alert('No se pudo actualizar la novedad.');
        }
      });

    } else {

      this.api.crear(this.form).subscribe({
        next: () => {
          this.guardando = false;
          this.volver();
        },
        error: () => {
          this.guardando = false;
          alert('No se pudo crear la novedad.');
        }
      });

    }
  }

  volver(): void {
    this.router.navigate(['/nomina/novedades']);
  }

  // =========================================================
  // 🔥 CÁLCULO AUTOMÁTICO DE DÍAS (SOLO VISUAL)
  // =========================================================
  calcularDias(): void {

    if (!this.requiereFechas) return;

    if (!this.form.fechaInicial || !this.form.fechaFinal) {
      this.form.cantidad = 0;
      return;
    }

    const inicio = new Date(this.form.fechaInicial);
    const fin = new Date(this.form.fechaFinal);

    if (isNaN(inicio.getTime()) || isNaN(fin.getTime())) {
      this.form.cantidad = 0;
      return;
    }

    if (fin < inicio) {
      this.form.cantidad = 0;
      return;
    }

    const diffMs = fin.getTime() - inicio.getTime();
    const diffDias = Math.floor(diffMs / (1000 * 60 * 60 * 24)) + 1;

    this.form.cantidad = diffDias;
  }

  // =========================================================
  // 🔥 CARGAR CONTRATO ACTIVO AUTOMÁTICAMENTE
  // =========================================================
 private cargarContratoActivo(idEmpleado: number): void {

   // 🔥 limpiar antes de consultar (evita cálculos con contrato viejo)
   this.contratoActivo = null;
   this.form.idContrato = null;

   this.contratosApi.listarPorEmpleado(idEmpleado)
     .subscribe({
       next: (contratos: EmpleadoContratoListDTO[]) => {

         if (!contratos || contratos.length === 0) {
           alert('El empleado no tiene contrato.');
           return;
         }

         const contratoActivo =
           contratos.find(c => c.activo === true) ?? contratos[0];

         // ✅ Asignar primero
         this.form.idContrato = contratoActivo.idContrato;
         this.contratoActivo = contratoActivo;

         // ✅ Recalcular SOLO si ya hay concepto y período
         if (this.form.codigoConcepto) {
           this.recalcular();
         }
       },
       error: () => {
         alert('No se pudo cargar el contrato.');
         this.form.idContrato = null;
         this.contratoActivo = null;
       }
     });
 }

  // =========================================================
  // 🔥 RECALCULAR VALOR AUTOMÁTICO
  // =========================================================
  recalcular(): void {

    if (
      !this.form.idContrato ||
      !this.form.codigoConcepto
    ) {
      return;
    }

    // 🔥 Buscar concepto real
    const concepto = this.conceptos.find(
      c => c.codigoConcepto === this.form.codigoConcepto
    );

    if (!concepto) {
      return;
    }

    // 🔥 Si es MANUAL no calculamos
    if (concepto.tipoCalculo === 'MANUAL') {
      return;
    }

    // 🔥 Para horas debe haber cantidad
    if (
      concepto.tipoCalculo === 'POR_HORAS' &&
      (!this.form.cantidad || this.form.cantidad <= 0)
    ) {
      return;
    }

    this.api.calcular({
      idContrato: this.form.idContrato,
      codigoConcepto: this.form.codigoConcepto,
      cantidad: this.form.cantidad ?? 0
    }).subscribe({
      next: res => {
        this.form.valor = res.valorCalculado ?? 0;
      },
      error: () => {
        this.form.valor = 0;
      }
    });
  }
}
