import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';

import {
  BienInmueble,
  nuevoBienInmueble
} from './bienes-inmuebles.dto';

import { BienesInmueblesApi } from './bienes-inmuebles.api';

import {
  DatosPersonales,
  DatosPersonalesApi
} from '../datos-personales/datos-personales.api';

import {
  CatalogoIdCodigoNombre,
  CatalogosApi
} from '../../../shared/catalogos/catalogos.api';

import { forkJoin } from 'rxjs';
import { CodigoNombreDTO, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';
import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';

import {
  BienInmuebleAvaluo,
  nuevoAvaluo
} from './bienes-inmuebles-avaluos.dto';

import { BienesInmueblesAvaluosApi } from './bienes-inmuebles-avaluos.api';

import {
  BienInmuebleSeguro,
  nuevoSeguro
} from './bienes-inmuebles-seguros.dto';

import { BienesInmueblesSegurosApi } from './bienes-inmuebles-seguros.api';

@Component({
  selector: 'app-bienes-inmuebles-upsert',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    NumericFormatDirective
  ],
  templateUrl: './bienes-inmuebles-upsert.component.html',
  styleUrls: ['./bienes-inmuebles-upsert.component.scss']
})
export class BienesInmueblesUpsertComponent implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly location = inject(Location);

  private readonly bienesApi = inject(BienesInmueblesApi);
  private readonly datosApi = inject(DatosPersonalesApi);
  private readonly catalogosApi = inject(CatalogosApi);
  private readonly avaluosApi = inject(BienesInmueblesAvaluosApi);
  private readonly segurosApi = inject(BienesInmueblesSegurosApi);

  idDatosPersonal = 0;

  asociado: DatosPersonales | null = null;
  bienes: BienInmueble[] = [];

  bienActual: BienInmueble | null = null;

  modoFormulario: 'NINGUNO' | 'NUEVO' | 'EDITAR' = 'NINGUNO';

  cargando = false;
  guardando = false;
  error = '';
  mensaje = '';

  tiposBienes: CatalogoIdCodigoNombre[] = [];
  tiposInmuebles: CatalogoIdCodigoNombre[] = [];
  tiposGravamenes: CatalogoIdCodigoNombre[] = [];

  paises: CodigoNombreDTO[] = [];
  departamentos: Departamento[] = [];
  ciudades: Ciudad[] = [];

  ciudadesNotaria: Ciudad[] = [];

  resumen = {
    cantidad: 0,
    valorComercial: 0,
    valorGravamen: 0,
    patrimonioNeto: 0
  };

  bienSeleccionadoAvaluos: BienInmueble | null = null;
  avaluos: BienInmuebleAvaluo[] = [];
  avaluoActual: BienInmuebleAvaluo | null = null;

  modoAvaluo: 'NINGUNO' | 'NUEVO' | 'EDITAR' = 'NINGUNO';
  cargandoAvaluos = false;
  guardandoAvaluo = false;

  bienSeleccionadoSeguros: BienInmueble | null = null;
  seguros: BienInmuebleSeguro[] = [];
  seguroActual: BienInmuebleSeguro | null = null;

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

  // ============================================================
  // CARGA INICIAL
  // ============================================================

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
      tiposInmuebles: this.catalogosApi.listarTiposInmueblesHojaVida(),
      tiposGravamenes: this.catalogosApi.listarTiposGravamenesHojaVida(),
      paises: this.catalogosApi.listarPaises(),
      departamentos: this.catalogosApi.listarDepartamentos()
    }).subscribe({
      next: r => {
        this.tiposBienes = r.tiposBienes ?? [];
        this.tiposInmuebles = r.tiposInmuebles ?? [];
        this.tiposGravamenes = r.tiposGravamenes ?? [];
        this.paises = r.paises ?? [];
        this.departamentos = r.departamentos ?? [];
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
      error: (err: any) => {
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

        console.error('Error cargando bienes inmuebles', err);
        this.error = 'No fue posible cargar los bienes inmuebles.';
      },
      complete: () => this.cargando = false
    });
  }

  refrescar(): void {
    this.cargarBienes();
    this.cancelar();
  }

  // ============================================================
  // FORMULARIO
  // ============================================================

  nuevo(): void {
    this.error = '';
    this.mensaje = '';
    this.ciudades = [];
    this.ciudadesNotaria = [];

    const dto = nuevoBienInmueble(this.idDatosPersonal);

    const tipoInmueble = this.tiposBienes.find(t =>
      (t.codigo ?? '').toUpperCase() === 'INMUEBLE'
    );

    if (tipoInmueble?.id) {
      dto.idTipoBien = tipoInmueble.id;
    }

    this.bienActual = dto;
    this.modoFormulario = 'NUEVO';
  }

  editar(bien: BienInmueble): void {
    this.error = '';
    this.mensaje = '';

    this.bienActual = {
      ...bien,
      idDatosPersonal: this.idDatosPersonal
    };

    this.modoFormulario = 'EDITAR';
    this.cargarCiudadesEdicion(this.bienActual);
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

    const peticion = this.modoFormulario === 'EDITAR' && dto.idBien
      ? this.bienesApi.actualizar(dto.idBien, dto)
      : this.bienesApi.crear(dto);

    peticion.subscribe({
      next: () => {
        this.mensaje = 'Bien inmueble guardado correctamente.';
        this.refrescar();
      },
      error: err => {
        console.error('Error guardando bien inmueble', err);
        this.error = this.extraerMensajeError(err);
        this.guardando = false;
      }
    });
  }

  eliminar(bien: BienInmueble): void {
    if (!bien.idBien) return;

    const ok = confirm(
      `¿Eliminar el bien inmueble "${bien.descripcionGeneral}"?`
    );

    if (!ok) return;

    this.error = '';
    this.mensaje = '';

    this.bienesApi.eliminar(bien.idBien).subscribe({
      next: () => {
        this.mensaje = 'Bien inmueble eliminado correctamente.';
        this.cargarBienes();
      },
      error: err => {
        console.error('Error eliminando bien inmueble', err);
        this.error = this.extraerMensajeError(err);
      }
    });
  }

  // ============================================================
  // RESUMEN
  // ============================================================

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

  valorNeto(bien: BienInmueble): number {
    return Number(bien.valorComercial || 0) - Number(bien.valorGravamen || 0);
  }

  // ============================================================
  // UTILIDADES
  // ============================================================

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

  private normalizarParaGuardar(dto: BienInmueble): BienInmueble {
    return {
      ...dto,
      idDatosPersonal: this.idDatosPersonal,
      porcentajePropiedad: Number(dto.porcentajePropiedad || 100),
      valorComercial: Number(dto.valorComercial || 0),
      valorGravamen: Number(dto.valorGravamen || 0),
      areaTerreno: Number(dto.areaTerreno || 0),
      areaConstruida: Number(dto.areaConstruida || 0)
    };
  }

  private extraerMensajeError(err: any): string {
    if (err?.error?.message) return err.error.message;
    if (typeof err?.error === 'string') return err.error;
    if (err?.error?.error) return err.error.error;
    return 'No fue posible completar la operación.';
  }

  onDepartamentoChange(): void {
    const idDepartamento = Number(this.bienActual?.idDepartamento || 0);

    this.bienActual!.idCiudad = 0;
    this.ciudades = [];

    if (!idDepartamento) return;

    this.catalogosApi.listarCiudadesPorDepartamento(idDepartamento).subscribe({
      next: r => this.ciudades = r ?? [],
      error: () => this.ciudades = []
    });
  }

  onDepartamentoNotariaChange(): void {
    const idDepartamento = Number(this.bienActual?.idDepartamentoNotaria || 0);

    this.bienActual!.idCiudadNotaria = null;
    this.ciudadesNotaria = [];

    if (!idDepartamento) return;

    this.catalogosApi.listarCiudadesPorDepartamento(idDepartamento).subscribe({
      next: r => this.ciudadesNotaria = r ?? [],
      error: () => this.ciudadesNotaria = []
    });
  }

  private cargarCiudadesEdicion(bien: BienInmueble): void {
    this.ciudades = [];
    this.ciudadesNotaria = [];

    if (bien.idDepartamento) {
      this.catalogosApi.listarCiudadesPorDepartamento(bien.idDepartamento).subscribe({
        next: r => this.ciudades = r ?? [],
        error: () => this.ciudades = []
      });
    }

    if (bien.idDepartamentoNotaria) {
      this.catalogosApi.listarCiudadesPorDepartamento(bien.idDepartamentoNotaria).subscribe({
        next: r => this.ciudadesNotaria = r ?? [],
        error: () => this.ciudadesNotaria = []
      });
    }
  }

  abrirAvaluos(bien: BienInmueble): void {
    if (!bien.idBien) return;

    this.bienSeleccionadoAvaluos = bien;
    this.avaluoActual = null;
    this.modoAvaluo = 'NINGUNO';

    this.cargarAvaluos(bien.idBien);
  }

  cargarAvaluos(idBien: number): void {
    this.cargandoAvaluos = true;
    this.avaluos = [];

    this.avaluosApi.listarPorBien(idBien).subscribe({
      next: r => this.avaluos = r ?? [],
      error: err => {
        if (err?.status === 204) {
          this.avaluos = [];
          return;
        }

        console.error('Error cargando avalúos', err);
        this.error = 'No fue posible cargar los avalúos del inmueble.';
      },
      complete: () => this.cargandoAvaluos = false
    });
  }

  nuevoAvaluo(): void {
    if (!this.bienSeleccionadoAvaluos?.idBien) return;

    this.avaluoActual = nuevoAvaluo(this.bienSeleccionadoAvaluos.idBien);
    this.calcularVencimientoAvaluo();
    this.modoAvaluo = 'NUEVO';
  }

  editarAvaluo(a: BienInmuebleAvaluo): void {
    this.avaluoActual = { ...a };
    this.modoAvaluo = 'EDITAR';
  }

  cancelarAvaluo(): void {
    this.avaluoActual = null;
    this.modoAvaluo = 'NINGUNO';
    this.guardandoAvaluo = false;
  }

  guardarAvaluo(): void {
    if (!this.avaluoActual) return;

    this.guardandoAvaluo = true;
    this.error = '';
    this.mensaje = '';

    const dto: BienInmuebleAvaluo = {
      ...this.avaluoActual,
      valorAvaluoComercial: Number(this.avaluoActual.valorAvaluoComercial || 0),
      valorAvaluoCatastral: Number(this.avaluoActual.valorAvaluoCatastral || 0)
    };

    const peticion =
      this.modoAvaluo === 'EDITAR' && dto.idBienInmuebleAvaluo
        ? this.avaluosApi.actualizar(dto.idBienInmuebleAvaluo, dto)
        : this.avaluosApi.crear(dto);

    peticion.subscribe({
      next: () => {
        this.mensaje = 'Avalúo guardado correctamente.';

        if (dto.idBien) {
          this.cargarAvaluos(dto.idBien);
        }

        this.cancelarAvaluo();
        this.cargarBienes();
      },
      error: err => {
        console.error('Error guardando avalúo', err);
        this.error = this.extraerMensajeError(err);
        this.guardandoAvaluo = false;
      }
    });
  }

  eliminarAvaluo(a: BienInmuebleAvaluo): void {
    if (!a.idBienInmuebleAvaluo) return;

    const ok = confirm('¿Eliminar este avalúo?');
    if (!ok) return;

    this.avaluosApi.eliminar(a.idBienInmuebleAvaluo).subscribe({
      next: () => {
        this.mensaje = 'Avalúo eliminado correctamente.';

        if (a.idBien) {
          this.cargarAvaluos(a.idBien);
        }

        this.cargarBienes();
      },
      error: err => {
        console.error('Error eliminando avalúo', err);
        this.error = this.extraerMensajeError(err);
      }
    });
  }

  cerrarAvaluos(): void {
    this.bienSeleccionadoAvaluos = null;
    this.avaluos = [];
    this.avaluoActual = null;
    this.modoAvaluo = 'NINGUNO';
  }

  calcularVencimientoAvaluo(): void {
    if (!this.avaluoActual?.fechaAvaluo) {
      return;
    }

    const vigencia = Number(this.avaluoActual.vigenciaAnios || 3);
    const fecha = new Date(this.avaluoActual.fechaAvaluo + 'T00:00:00');

    fecha.setFullYear(fecha.getFullYear() + vigencia);

    this.avaluoActual.fechaVencimientoAvaluo =
      fecha.toISOString().substring(0, 10);
  }

  abrirSeguros(bien: BienInmueble): void {
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
        this.error = 'No fue posible cargar los seguros del inmueble.';
      },
      complete: () => this.cargandoSeguros = false
    });
  }

  nuevoSeguro(): void {
    if (!this.bienSeleccionadoSeguros?.idBien) return;

    this.seguroActual = nuevoSeguro(this.bienSeleccionadoSeguros.idBien);
    this.modoSeguro = 'NUEVO';
  }

  editarSeguro(s: BienInmuebleSeguro): void {
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

    const dto: BienInmuebleSeguro = {
      ...this.seguroActual,
      valorAsegurado: Number(this.seguroActual.valorAsegurado || 0),
      estadoSeguro: (this.seguroActual.estadoSeguro || 'A').toUpperCase()
    };

    const peticion =
      this.modoSeguro === 'EDITAR' && dto.idBienInmuebleSeguro
        ? this.segurosApi.actualizar(dto.idBienInmuebleSeguro, dto)
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

  eliminarSeguro(s: BienInmuebleSeguro): void {
    if (!s.idBienInmuebleSeguro) return;

    const ok = confirm('¿Eliminar este seguro?');
    if (!ok) return;

    this.segurosApi.eliminar(s.idBienInmuebleSeguro).subscribe({
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

}
