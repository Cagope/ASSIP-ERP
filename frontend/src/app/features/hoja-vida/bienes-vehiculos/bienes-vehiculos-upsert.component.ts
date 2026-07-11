import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';

import {
  BienVehiculo,
  nuevoBienVehiculo
} from './bienes-vehiculos.dto';

import { BienesVehiculosApi } from './bienes-vehiculos.api';

import {
  BienVehiculoSeguro,
  nuevoSeguro
} from './bienes-vehiculos-seguros.dto';

import { BienesVehiculosSegurosApi } from './bienes-vehiculos-seguros.api';

import {
  DatosPersonales,
  DatosPersonalesApi
} from '../datos-personales/datos-personales.api';

import {
  CatalogoIdCodigoNombre,
  CatalogosApi
} from '../../../shared/catalogos/catalogos.api';

import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-bienes-vehiculos-upsert',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    NumericFormatDirective
  ],
  templateUrl: './bienes-vehiculos-upsert.component.html',
  styleUrls: ['./bienes-vehiculos-upsert.component.scss']
})
export class BienesVehiculosUpsertComponent implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly location = inject(Location);

  private readonly bienesApi = inject(BienesVehiculosApi);
  private readonly segurosApi = inject(BienesVehiculosSegurosApi);
  private readonly datosApi = inject(DatosPersonalesApi);
  private readonly catalogosApi = inject(CatalogosApi);

  idDatosPersonal = 0;

  asociado: DatosPersonales | null = null;
  bienes: BienVehiculo[] = [];

  bienActual: BienVehiculo | null = null;

  modoFormulario: 'NINGUNO' | 'NUEVO' | 'EDITAR' = 'NINGUNO';

  cargando = false;
  guardando = false;
  error = '';
  mensaje = '';

  tiposBienes: CatalogoIdCodigoNombre[] = [];
  tiposVehiculos: CatalogoIdCodigoNombre[] = [];
  tiposGravamenes: CatalogoIdCodigoNombre[] = [];

  resumen = {
    cantidad: 0,
    valorComercial: 0,
    valorGravamen: 0,
    patrimonioNeto: 0
  };

  bienSeleccionadoSeguros: BienVehiculo | null = null;
  seguros: BienVehiculoSeguro[] = [];
  seguroActual: BienVehiculoSeguro | null = null;

  modoSeguro: 'NINGUNO' | 'NUEVO' | 'EDITAR' = 'NINGUNO';
  cargandoSeguros = false;
  guardandoSeguro = false;

  ngOnInit(): void {
    this.idDatosPersonal = Number(
      this.route.snapshot.paramMap.get('idDatosPersonal')
    );

    if (!this.idDatosPersonal) {
      this.error = 'No se recibió el asociado.';
      return;
    }

    this.cargarInicial();
  }

  cargarInicial(): void {
    this.cargando = true;
    this.error = '';
    this.mensaje = '';

    this.cargarCatalogos();
    this.cargarAsociado();
    this.cargarBienes();
  }

  cargarCatalogos(): void {
    forkJoin({
      tiposBienes: this.catalogosApi.listarTiposBienesHojaVida(),
      tiposVehiculos: this.catalogosApi.listarTiposVehiculosHojaVida(),
      tiposGravamenes: this.catalogosApi.listarTiposGravamenesHojaVida()
    }).subscribe({
      next: r => {
        this.tiposBienes = r.tiposBienes ?? [];
        this.tiposVehiculos = r.tiposVehiculos ?? [];
        this.tiposGravamenes = r.tiposGravamenes ?? [];
      },
      error: err => {
        console.error('Error cargando catálogos', err);
        this.error = 'No fue posible cargar los catálogos.';
      }
    });
  }

  cargarAsociado(): void {
    this.datosApi.obtener(this.idDatosPersonal).subscribe({
      next: (r: DatosPersonales) => this.asociado = r,
      error: err => {
        console.error('Error cargando asociado', err);
        this.error = 'No fue posible cargar el asociado.';
      }
    });
  }

  cargarBienes(): void {
    this.bienesApi.listarPorPersona(this.idDatosPersonal).subscribe({
      next: r => {
        this.bienes = r ?? [];
        this.calcularResumen();
      },
      error: err => {
        if (err?.status === 204) {
          this.bienes = [];
          this.calcularResumen();
          return;
        }

        console.error('Error cargando bienes vehículos', err);
        this.error = 'No fue posible cargar los bienes vehículos.';
      },
      complete: () => this.cargando = false
    });
  }

  nuevo(): void {
    this.error = '';
    this.mensaje = '';

    const dto = nuevoBienVehiculo(this.idDatosPersonal);

    const tipoVehiculoBien = this.tiposBienes.find(t =>
      (t.codigo ?? '').toUpperCase() === 'VEHICULO'
      || (t.codigo ?? '').toUpperCase() === 'VEHÍCULO'
      || (t.nombre ?? '').toUpperCase().includes('VEH')
    );

    if (tipoVehiculoBien?.id) {
      dto.idTipoBien = tipoVehiculoBien.id;
    }

    this.bienActual = dto;
    this.modoFormulario = 'NUEVO';
  }

  editar(bien: BienVehiculo): void {
    this.error = '';
    this.mensaje = '';

    this.bienActual = {
      ...bien,
      idDatosPersonal: this.idDatosPersonal
    };

    this.modoFormulario = 'EDITAR';
  }

  cancelar(): void {
    this.bienActual = null;
    this.modoFormulario = 'NINGUNO';
    this.guardando = false;
  }

  guardar(): void {
    if (!this.bienActual) return;

    this.error = '';
    this.mensaje = '';
    this.guardando = true;

    const dto = this.normalizarParaGuardar(this.bienActual);

    const peticion =
      this.modoFormulario === 'EDITAR' && dto.idBien
        ? this.bienesApi.actualizar(dto.idBien, dto)
        : this.bienesApi.crear(dto);

    peticion.subscribe({
      next: () => {
        this.mensaje = 'Bien vehículo guardado correctamente.';
        this.refrescar();
      },
      error: err => {
        console.error('Error guardando bien vehículo', err);
        this.error = this.extraerMensajeError(err);
        this.guardando = false;
      }
    });
  }

  eliminar(bien: BienVehiculo): void {
    if (!bien.idBien) return;

    const ok = confirm(
      `¿Eliminar el vehículo "${bien.placa}"?`
    );

    if (!ok) return;

    this.error = '';
    this.mensaje = '';

    this.bienesApi.eliminar(bien.idBien).subscribe({
      next: () => {
        this.mensaje = 'Bien vehículo eliminado correctamente.';
        this.cargarBienes();
      },
      error: err => {
        console.error('Error eliminando bien vehículo', err);
        this.error = this.extraerMensajeError(err);
      }
    });
  }

  refrescar(): void {
    this.cargarBienes();
    this.cancelar();
  }

  calcularResumen(): void {
    this.resumen.cantidad = this.bienes.length;

    this.resumen.valorComercial = this.bienes.reduce(
      (s, b) => s + Number(b.valorComercial || 0),
      0
    );

    this.resumen.valorGravamen = this.bienes.reduce(
      (s, b) => s + Number(b.valorGravamen || 0),
      0
    );

    this.resumen.patrimonioNeto =
      this.resumen.valorComercial - this.resumen.valorGravamen;
  }

  valorNeto(bien: BienVehiculo): number {
    return Number(bien.valorComercial || 0)
      - Number(bien.valorGravamen || 0);
  }

  volver(): void {
    this.location.back();
  }

  nombreAsociado(): string {
    if (!this.asociado) return '';

    return [
      this.asociado.nombres,
      this.asociado.primerApellido,
      this.asociado.segundoApellido
    ]
      .filter(Boolean)
      .join(' ');
  }

  private normalizarParaGuardar(dto: BienVehiculo): BienVehiculo {
    return {
      ...dto,
      idDatosPersonal: this.idDatosPersonal,

      porcentajePropiedad: Number(dto.porcentajePropiedad || 100),
      valorComercial: Number(dto.valorComercial || 0),
      valorGravamen: Number(dto.valorGravamen || 0),

      modelo: dto.modelo ? Number(dto.modelo) : null,

      placa: (dto.placa || '').trim().toUpperCase(),
      marca: dto.marca?.trim().toUpperCase() || null,
      linea: dto.linea?.trim().toUpperCase() || null,
      color: dto.color?.trim().toUpperCase() || null,

      numeroMotor: dto.numeroMotor?.trim().toUpperCase() || null,
      numeroChasis: dto.numeroChasis?.trim().toUpperCase() || null,
      numeroSerie: dto.numeroSerie?.trim().toUpperCase() || null,

      observaciones: dto.observaciones?.trim() || null
    };
  }

  abrirSeguros(bien: BienVehiculo): void {
    if (!bien.idBien) return;

    this.bienSeleccionadoSeguros = bien;
    this.seguroActual = null;
    this.modoSeguro = 'NINGUNO';

    this.cargarSeguros(bien.idBien);
  }

  cargarSeguros(idBien: number): void {
    this.cargandoSeguros = true;
    this.seguros = [];

    this.segurosApi.listarPorBien(idBien).subscribe({
      next: r => this.seguros = r ?? [],
      error: err => {
        if (err?.status === 204) {
          this.seguros = [];
          return;
        }

        console.error('Error cargando seguros', err);
        this.error = 'No fue posible cargar los seguros del vehículo.';
      },
      complete: () => this.cargandoSeguros = false
    });
  }

  nuevoSeguro(): void {
    if (!this.bienSeleccionadoSeguros?.idBien) return;

    this.seguroActual = nuevoSeguro(this.bienSeleccionadoSeguros.idBien);
    this.modoSeguro = 'NUEVO';
  }

  editarSeguro(s: BienVehiculoSeguro): void {
    this.seguroActual = { ...s };
    this.modoSeguro = 'EDITAR';
  }

  cancelarSeguro(): void {
    this.seguroActual = null;
    this.modoSeguro = 'NINGUNO';
    this.guardandoSeguro = false;
  }

  guardarSeguro(): void {
    if (!this.seguroActual) return;

    this.guardandoSeguro = true;
    this.error = '';
    this.mensaje = '';

    const dto: BienVehiculoSeguro = {
      ...this.seguroActual,
      valorAsegurado: Number(this.seguroActual.valorAsegurado || 0),
      estadoSeguro: (this.seguroActual.estadoSeguro || 'A').toUpperCase()
    };

    const peticion =
      this.modoSeguro === 'EDITAR' && dto.idBienVehiculoSeguro
        ? this.segurosApi.actualizar(dto.idBienVehiculoSeguro, dto)
        : this.segurosApi.crear(dto);

    peticion.subscribe({
      next: () => {
        this.mensaje = 'Seguro guardado correctamente.';

        if (dto.idBien) {
          this.cargarSeguros(dto.idBien);
        }

        this.cancelarSeguro();
        this.cargarBienes();
      },
      error: err => {
        console.error('Error guardando seguro', err);
        this.error = this.extraerMensajeError(err);
        this.guardandoSeguro = false;
      }
    });
  }

  eliminarSeguro(s: BienVehiculoSeguro): void {
    if (!s.idBienVehiculoSeguro) return;

    const ok = confirm('¿Eliminar este seguro?');
    if (!ok) return;

    this.segurosApi.eliminar(s.idBienVehiculoSeguro).subscribe({
      next: () => {
        this.mensaje = 'Seguro eliminado correctamente.';

        if (s.idBien) {
          this.cargarSeguros(s.idBien);
        }

        this.cargarBienes();
      },
      error: err => {
        console.error('Error eliminando seguro', err);
        this.error = this.extraerMensajeError(err);
      }
    });
  }

  cerrarSeguros(): void {
    this.bienSeleccionadoSeguros = null;
    this.seguros = [];
    this.seguroActual = null;
    this.modoSeguro = 'NINGUNO';
  }

  private extraerMensajeError(err: any): string {
    if (err?.error?.message) return err.error.message;
    if (typeof err?.error === 'string') return err.error;
    if (err?.error?.error) return err.error.error;
    return 'No fue posible completar la operación.';
  }

}
