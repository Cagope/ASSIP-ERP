import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { EmpleadoContratosApi, EmpleadoContratoFormDTO } from './empleado-contratos.api';

import { EmpleadosApi, EmpleadoListDTO } from '../empleados/empleados.api';
import { PersonasApi, PersonaBusquedaDTO } from '../../../shared/personas/personas.api';

import { CargoApi, CargoListDTO } from '../cargos/cargos.api';
import { SeccionesNominaApi, SeccionNominaDTO } from '../secciones/secciones.api';

import { EpsApi, EpsListDTO } from '../eps/eps.api';
import { AfpApi, AfpListDTO } from '../afp/afp.api';
import { ArlApi, ArlListDTO } from '../arl/arl.api';
import { CajaCompensacionApi, CajaCompensacionListDTO } from '../caja-compensacion/caja-compensacion.api';
import { CesantiasApi, CesantiasDTO } from '../cesantias/cesantias.api';

import {
  CuentasAhorroApi,
  CuentaAhorroSelectDTO
} from '../../../shared/cuentas-ahorro/cuentas-ahorro.api';

import { TiposContratosApi, TipoContratoDTO } from '../tipos-contratos/tipos-contratos.api';

@Component({
  standalone: true,
  selector: 'app-empleado-contratos-upsert',
  templateUrl: './empleado-contratos-upsert.component.html',
  styleUrls: ['./empleado-contratos-upsert.component.scss'],
  imports: [CommonModule, FormsModule, RouterModule, HeaderActionsComponent],
})
export class EmpleadoContratosUpsertComponent implements OnInit {

  private readonly api = inject(EmpleadoContratosApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private readonly empleadosApi = inject(EmpleadosApi);
  private readonly personasApi = inject(PersonasApi);

  private readonly cargosApi = inject(CargoApi);
  private readonly seccionesApi = inject(SeccionesNominaApi);

  private readonly epsApi = inject(EpsApi);
  private readonly afpApi = inject(AfpApi);
  private readonly arlApi = inject(ArlApi);
  private readonly cajaApi = inject(CajaCompensacionApi);
  private readonly cesantiasApi = inject(CesantiasApi);

  private readonly cuentasApi = inject(CuentasAhorroApi);

  private readonly tiposContratoApi = inject(TiposContratosApi);

  id: number | null = null;
  loading = false;
  guardando = false;

  empleados: EmpleadoListDTO[] = [];
  empleadosDisplay: { idEmpleado: number; documento: string; nombre: string }[] = [];

  secciones: SeccionNominaDTO[] = [];
  cargos: CargoListDTO[] = [];

  eps: EpsListDTO[] = [];
  afp: AfpListDTO[] = [];
  arl: ArlListDTO[] = [];
  cajas: CajaCompensacionListDTO[] = [];
  cesantias: CesantiasDTO[] = [];

  cuentasNomina: CuentaAhorroSelectDTO[] = [];

  tiposContrato: TipoContratoDTO[] = [];

  form: EmpleadoContratoFormDTO = {
    idEmpleado: null,
    idSeccion: null,
    fechaInicio: '',
    fechaFin: null,
    idTipoContrato: null,
    idCargo: null,
    salarioBase: 0,
    salarioIntegral: false,
    periodoPago: 'MENSUAL',
    idEps: null,
    idAfp: null,
    idCesantias: null,
    idArl: null,
    idCajaCompensacion: null,
    idCuentaAhorroNomina: null,
    fechaEnvioNotaRenovacion: null,
    claseRiesgoArl: 1,
    porcentajeArl: 0,
    activo: true,
    liquidaPrimaSemestral: false
  };

  ngOnInit(): void {
    this.cargarCatalogos();

    const idParam = this.route.snapshot.paramMap.get('id');
    this.id = idParam ? +idParam : null;

    if (this.id) this.cargar(this.id);
  }

  // =========================
  // CUENTAS EMPLEADO
  // =========================

  cargarCuentasEmpleado(idEmpleado: number | null): void {

    this.cuentasNomina = [];

    if (!idEmpleado) return;

    const emp = this.empleados.find(
      e => e.idEmpleado === idEmpleado
    );

    if (!emp || !emp.idDatosPersonal) return;

    console.log('🔵 Cargando cuentas para:', emp.idDatosPersonal);

    this.cuentasApi
      .listarPorPersona(emp.idDatosPersonal)
      .subscribe({
        next: data => {
          console.log('🟢 Cuentas recibidas:', data);
          this.cuentasNomina = data ?? [];
        },
        error: err => {
          console.error('🔴 Error cuentas:', err);
          this.cuentasNomina = [];
        }
      });
  }

  // =========================
  // CATÁLOGOS
  // =========================

  cargarCatalogos(): void {

    this.empleadosApi.listar().subscribe({
      next: (data) => {
        this.empleados = data ?? [];
        this.pintarEmpleadosDisplay();
      },
      error: () => {
        this.empleados = [];
        this.empleadosDisplay = [];
      }
    });

    this.tiposContratoApi.listar().subscribe({
      next: d => this.tiposContrato = d ?? [],
      error: () => this.tiposContrato = []
    });

    this.seccionesApi.listar().subscribe({ next: d => this.secciones = d ?? [], error: () => this.secciones = [] });
    this.cargosApi.listar().subscribe({ next: d => this.cargos = d ?? [], error: () => this.cargos = [] });

    this.epsApi.listar().subscribe({ next: d => this.eps = d ?? [], error: () => this.eps = [] });
    this.afpApi.listar().subscribe({ next: d => this.afp = d ?? [], error: () => this.afp = [] });
    this.arlApi.listar().subscribe({ next: d => this.arl = d ?? [], error: () => this.arl = [] });
    this.cajaApi.listar().subscribe({ next: d => this.cajas = d ?? [], error: () => this.cajas = [] });
    this.cesantiasApi.listar().subscribe({ next: d => this.cesantias = d ?? [], error: () => this.cesantias = [] });
  }

  // =========================
  // EMPLEADOS DISPLAY
  // =========================

  private pintarEmpleadosDisplay(): void {

    const idsPersona = Array.from(new Set(
      this.empleados.map(x => x.idDatosPersonal).filter(Boolean)
    ));

    if (idsPersona.length === 0) {
      this.empleadosDisplay = this.empleados.map(e => ({
        idEmpleado: e.idEmpleado,
        documento: '',
        nombre: `EMPLEADO #${e.idEmpleado}`
      }));
      return;
    }

    const mapPersonas = new Map<number, PersonaBusquedaDTO>();

    let pending = idsPersona.length;

    for (const idDatosPersonal of idsPersona) {
      this.personasApi.obtenerPorId(idDatosPersonal).subscribe({
        next: (p) => {
          mapPersonas.set(idDatosPersonal, p);
          pending--;
          if (pending === 0) this.buildEmpleadoDisplay(mapPersonas);
        },
        error: () => {
          pending--;
          if (pending === 0) this.buildEmpleadoDisplay(mapPersonas);
        }
      });
    }
  }

  private buildEmpleadoDisplay(mapPersonas: Map<number, PersonaBusquedaDTO>): void {

    this.empleadosDisplay = this.empleados.map(e => {

      const p = e.idDatosPersonal
        ? mapPersonas.get(e.idDatosPersonal)
        : null;

      return {
        idEmpleado: e.idEmpleado,
        documento: p?.documento ?? '',
        nombre: p?.nombreCompleto ?? `EMPLEADO #${e.idEmpleado}`
      };

    }).sort((a, b) =>
      (a.nombre ?? '').localeCompare(b.nombre ?? '')
    );
  }

  // =========================
  // CARGAR CONTRATO
  // =========================

  cargar(id: number): void {

    this.loading = true;

    this.api.obtener(id).subscribe({
      next: (data) => {

        this.form = { ...this.form, ...data };

        this.cargarCuentasEmpleado(this.form.idEmpleado);

        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.volver();
      }
    });
  }

  // =========================
  // GUARDAR
  // =========================

  guardar(): void {

    if (!this.form.idEmpleado) {
      alert('Debe seleccionar un empleado.');
      return;
    }

    if (!this.form.fechaInicio) {
      alert('La fecha de inicio es obligatoria.');
      return;
    }

    if (!this.form.idTipoContrato) {
      alert('Debe seleccionar el tipo de contrato.');
      return;
    }

    if (!this.form.salarioBase || this.form.salarioBase <= 0) {
      alert('El salario base debe ser mayor a 0.');
      return;
    }

    if (!this.form.periodoPago) {
      alert('Debe seleccionar el período de pago.');
      return;
    }

    if (this.form.fechaFin && this.form.fechaFin < this.form.fechaInicio) {
      alert('La fecha fin no puede ser menor que la fecha inicio.');
      return;
    }

    if (!this.form.idCuentaAhorroNomina) {
      alert('Debe seleccionar la cuenta de nómina.');
      return;
    }

    if (!this.form.claseRiesgoArl || this.form.claseRiesgoArl < 1 || this.form.claseRiesgoArl > 5) {
      alert('La clase de riesgo ARL debe estar entre 1 y 5.');
      return;
    }

    if (this.form.porcentajeArl == null || this.form.porcentajeArl < 0) {
      alert('El porcentaje ARL no puede ser negativo.');
      return;
    }

    this.guardando = true;

    if (this.id) {

      this.api.actualizar(this.id, this.form)
        .subscribe({
          next: () => {
            this.guardando = false;
            this.volver();
          },
          error: () => {
            this.guardando = false;
            alert('No se pudo actualizar el contrato.');
          }
        });

    } else {

      this.api.crear(this.form)
        .subscribe({
          next: () => {
            this.guardando = false;
            this.volver();
          },
          error: () => {
            this.guardando = false;
            alert('No se pudo crear el contrato.');
          }
        });

    }
  }

  volver(): void {
    this.router.navigate(['/nomina/empleado-contratos']);
  }
}
