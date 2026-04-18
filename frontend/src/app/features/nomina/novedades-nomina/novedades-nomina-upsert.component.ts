import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';

import { EmpleadoAutocompleteComponent } from '../busqueda/empleado-autocomplete.component';

import {
  NovedadesNominaApi,
  NovedadNominaFormDTO,
  NovedadNominaListDTO
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
  contratosEmpleado: EmpleadoContratoListDTO[] = [];
  contratoActivo: EmpleadoContratoListDTO | null = null;
  periodoActivoTexto = '';

  novedadesContrato: NovedadNominaListDTO[] = [];
  loadingNovedades = false;

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

    if (!this.form.observacion || !this.form.observacion.trim()) {
      this.form.observacion =
        `Novedad ${this.conceptoSeleccionado.nombreConcepto} - Período ${this.periodoActivoTexto}`;
    }

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
        this.requiereCantidad = true;
        this.requiereFechas = false;
        break;

      case 'AUX_TRANSPORTE':
        this.valorEditable = false;
        this.requiereCantidad = true;
        this.requiereFechas = false;
        this.form.cantidad = this.form.cantidad && this.form.cantidad > 0
          ? this.form.cantidad
          : 1;
        break;

      case 'POR_PORCENTAJE':
        this.valorEditable = false;
        break;

      default:
        this.valorEditable = true;
        break;
    }

    if (!this.valorEditable) {
      this.form.valor = 0;
    }
  }

  onEmpleadoChange(): void {

    if (!this.form.idEmpleado) {
      this.form.idContrato = null;
      this.contratoActivo = null;
      this.contratosEmpleado = [];
      this.novedadesContrato = [];
      return;
    }

    this.cargarContratoActivo(this.form.idEmpleado);
  }

  onContratoChange(): void {

    if (!this.form.idContrato) {
      this.contratoActivo = null;
      this.novedadesContrato = [];
      return;
    }

    this.contratoActivo =
      this.contratosEmpleado.find(c => c.idContrato === this.form.idContrato) ?? null;

    if (this.form.idEmpleado && this.form.idContrato) {
      this.cargarNovedadesContrato(this.form.idEmpleado, this.form.idContrato);
    }

    if (this.form.codigoConcepto) {
      this.recalcular();
    }
  }

  // =========================================================
  // CARGAR EDICIÓN
  // =========================================================
  cargar(id: number): void {

    this.loading = true;

    this.api.obtener(id).subscribe({
      next: (data) => {

        this.form = { ...this.form, ...data };

        if (data.idEmpleado) {
          this.cargarContratoActivo(data.idEmpleado, data.idContrato ?? null);
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
  // 🔒 VALIDACIÓN FORMULARIO
  // =========================================================
  get formularioValido(): boolean {

    if (!this.form.idEmpleado) return false;
    if (!this.form.idContrato) return false;
    if (!this.form.codigoConcepto) return false;
    if (!this.form.observacion || !this.form.observacion.trim()) return false;

    if (this.requiereCantidad && (!this.form.cantidad || this.form.cantidad <= 0)) {
      return false;
    }

    if (this.requiereFechas && (!this.form.fechaInicial || !this.form.fechaFinal)) {
      return false;
    }

    if (this.valorEditable && (this.form.valor == null || this.form.valor < 0)) {
      return false;
    }

    return true;
  }

  // =========================================================
  // GUARDAR
  // =========================================================
  guardar(): void {

    if (!this.formularioValido) {
      alert('Complete los datos obligatorios de la novedad.');
      return;
    }

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

    if (!this.form.observacion || !this.form.observacion.trim()) {
      alert('La observación es obligatoria.');
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
  private cargarContratoActivo(
    idEmpleado: number,
    idContratoPreferido: number | null = null
  ): void {

    this.contratoActivo = null;
    this.form.idContrato = null;
    this.contratosEmpleado = [];
    this.novedadesContrato = [];

    this.contratosApi.listarPorEmpleado(idEmpleado)
      .subscribe({
        next: (contratos: EmpleadoContratoListDTO[]) => {

          if (!contratos || contratos.length === 0) {
            alert('El empleado no tiene contrato.');
            return;
          }

          this.contratosEmpleado = contratos;

          if (idContratoPreferido != null) {
            const contratoSeleccionado =
              contratos.find(c => c.idContrato === idContratoPreferido) ?? null;

            if (contratoSeleccionado) {
              this.form.idContrato = contratoSeleccionado.idContrato;
              this.contratoActivo = contratoSeleccionado;

              this.cargarNovedadesContrato(
                idEmpleado,
                contratoSeleccionado.idContrato
              );

              if (this.form.codigoConcepto) {
                this.recalcular();
              }
              return;
            }
          }

          if (contratos.length === 1) {
            this.form.idContrato = contratos[0].idContrato;
            this.contratoActivo = contratos[0];

            this.cargarNovedadesContrato(
              idEmpleado,
              contratos[0].idContrato
            );

            if (this.form.codigoConcepto) {
              this.recalcular();
            }
          }
        },
        error: () => {
          alert('No se pudo cargar el contrato.');
          this.form.idContrato = null;
          this.contratoActivo = null;
          this.contratosEmpleado = [];
          this.novedadesContrato = [];
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

    const concepto = this.conceptos.find(
      c => c.codigoConcepto === this.form.codigoConcepto
    );

    if (!concepto) {
      return;
    }

    if (concepto.tipoCalculo === 'MANUAL') {
      return;
    }

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

  private cargarNovedadesContrato(idEmpleado: number, idContrato: number): void {

    const empleadoSeleccionado = this.empleados.find(
      e => e.idEmpleado === idEmpleado
    );

    const idAgencia = empleadoSeleccionado?.idAgencia;

    if (!idAgencia) {
      this.novedadesContrato = [];
      this.loadingNovedades = false;
      return;
    }

    this.loadingNovedades = true;

    this.api.listar(idAgencia, idEmpleado).subscribe({
      next: (data) => {
        this.novedadesContrato = (data ?? [])
          .filter(n => n.idContrato === idContrato);

        this.loadingNovedades = false;
      },
      error: () => {
        this.novedadesContrato = [];
        this.loadingNovedades = false;
      }
    });
  }

  seleccionarCantidad(event: Event): void {
    const input = event.target as HTMLInputElement | null;
    if (!input) return;

    setTimeout(() => input.select(), 0);
  }
}
