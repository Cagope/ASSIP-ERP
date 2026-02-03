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

  id: number | null = null;
  loading = false;
  guardando = false;

  // catálogos
  empleados: EmpleadoListDTO[] = [];
  empleadosDisplay: { idEmpleado: number; documento: string; nombre: string }[] = [];

  secciones: SeccionNominaDTO[] = [];
  cargos: CargoListDTO[] = [];

  eps: EpsListDTO[] = [];
  afp: AfpListDTO[] = [];
  arl: ArlListDTO[] = [];
  cajas: CajaCompensacionListDTO[] = [];
cesantias: CesantiasDTO[] = [];


  // ⚠️ tipos de contrato (local)
  tiposContrato = [
    { id: 1, nombre: 'INDEFINIDO' },
    { id: 2, nombre: 'FIJO' },
    { id: 3, nombre: 'OBRA / LABOR' },
    { id: 4, nombre: 'APRENDIZAJE' },
  ];

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

    cuentaNominaDisplay: null,
    idCuentaAhorroNomina: null,

    fechaEnvioNotaRenovacion: null,

    claseRiesgoArl: 1,
    porcentajeArl: 0,

    activo: true
  };

  ngOnInit(): void {
    this.cargarCatalogos();

    const idParam = this.route.snapshot.paramMap.get('id');
    this.id = idParam ? +idParam : null;

    if (this.id) this.cargar(this.id);
  }

  cargarCatalogos(): void {
    // empleados
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

    // secciones
    this.seccionesApi.listar().subscribe({ next: d => this.secciones = d ?? [], error: () => this.secciones = [] });

    // cargos
    this.cargosApi.listar().subscribe({ next: d => this.cargos = d ?? [], error: () => this.cargos = [] });

    // eps/afp/arl/caja/cesantias
    this.epsApi.listar().subscribe({ next: d => this.eps = d ?? [], error: () => this.eps = [] });
    this.afpApi.listar().subscribe({ next: d => this.afp = d ?? [], error: () => this.afp = [] });
    this.arlApi.listar().subscribe({ next: d => this.arl = d ?? [], error: () => this.arl = [] });
    this.cajaApi.listar().subscribe({ next: d => this.cajas = d ?? [], error: () => this.cajas = [] });
    this.cesantiasApi.listar().subscribe({ next: d => this.cesantias = d ?? [], error: () => this.cesantias = [] });
  }

  private pintarEmpleadosDisplay(): void {
    // construir display (idEmpleado -> documento/nombre) consultando persona
    const idsPersona = Array.from(new Set(this.empleados.map(x => x.idDatosPersonal).filter(Boolean)));

    if (idsPersona.length === 0) {
      this.empleadosDisplay = this.empleados.map(e => ({
        idEmpleado: e.idEmpleado,
        documento: '',
        nombre: `EMPLEADO #${e.idEmpleado}`
      }));
      return;
    }

    // trae uno por uno (simple, estable)
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
      const p = e.idDatosPersonal ? mapPersonas.get(e.idDatosPersonal) : null;
      return {
        idEmpleado: e.idEmpleado,
        documento: p?.documento ?? '',
        nombre: p?.nombreCompleto ?? `EMPLEADO #${e.idEmpleado}`
      };
    }).sort((a, b) => (a.nombre ?? '').localeCompare(b.nombre ?? ''));
  }

  cargar(id: number): void {
    this.loading = true;

    this.api.obtener(id).subscribe({
      next: (data) => {
        this.form = {
          ...this.form,
          ...data,
          idContrato: data.idContrato,
          idEmpleado: (data as any).idEmpleado ?? null,
          idTipoContrato: (data as any).idTipoContrato ?? null,
          salarioBase: (data as any).salarioBase ?? 0,
          salarioIntegral: (data as any).salarioIntegral ?? false,
          periodoPago: (data as any).periodoPago ?? 'MENSUAL',
          claseRiesgoArl: (data as any).claseRiesgoArl ?? 1,
          porcentajeArl: (data as any).porcentajeArl ?? 0,
          activo: (data as any).activo ?? true,
        };

        this.loading = false;
      },
      error: (err) => {
        console.error('Error cargando contrato', err);
        this.loading = false;
        alert('No se pudo cargar el contrato.');
        this.volver();
      }
    });
  }

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

    if (!this.form.periodoPago) {
      this.form.periodoPago = 'MENSUAL';
    }

    this.guardando = true;

    if (this.id) {
      this.api.actualizar(this.id, this.form).subscribe({
        next: () => {
          this.guardando = false;
          this.volver();
        },
        error: (err) => {
          console.error('Error actualizando contrato', err);
          this.guardando = false;
          alert('No se pudo actualizar el contrato.');
        }
      });
    } else {
      this.api.crear(this.form).subscribe({
        next: () => {
          this.guardando = false;
          this.volver();
        },
        error: (err) => {
          console.error('Error creando contrato', err);
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
