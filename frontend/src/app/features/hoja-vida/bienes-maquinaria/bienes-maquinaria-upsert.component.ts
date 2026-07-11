import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';

import {
  BienMaquinaria,
  nuevoBienMaquinaria
} from './bienes-maquinaria.dto';

import { BienesMaquinariaApi } from './bienes-maquinaria.api';

import {
  BienMaquinariaSeguro,
  nuevoSeguro
} from './bienes-maquinaria-seguros.dto';

import {
  BienesMaquinariaSegurosApi
} from './bienes-maquinaria-seguros.api';

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
  selector: 'app-bienes-maquinaria-upsert',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    NumericFormatDirective
  ],
  templateUrl: './bienes-maquinaria-upsert.component.html',
  styleUrls: ['./bienes-maquinaria-upsert.component.scss']
})
export class BienesMaquinariaUpsertComponent implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly location = inject(Location);

  private readonly bienesApi = inject(BienesMaquinariaApi);
  private readonly segurosApi = inject(BienesMaquinariaSegurosApi);
  private readonly datosApi = inject(DatosPersonalesApi);
  private readonly catalogosApi = inject(CatalogosApi);

  idDatosPersonal = 0;

  asociado: DatosPersonales | null = null;
  bienes: BienMaquinaria[] = [];

  bienActual: BienMaquinaria | null = null;

  modoFormulario: 'NINGUNO' | 'NUEVO' | 'EDITAR' = 'NINGUNO';

  cargando = false;
  guardando = false;
  error = '';
  mensaje = '';

  tiposBienes: CatalogoIdCodigoNombre[] = [];
  tiposMaquinaria: CatalogoIdCodigoNombre[] = [];
  tiposGravamenes: CatalogoIdCodigoNombre[] = [];

  resumen = {
    cantidad: 0,
    valorComercial: 0,
    valorGravamen: 0,
    patrimonioNeto: 0
  };

  bienSeleccionadoSeguros: BienMaquinaria | null = null;
  seguros: BienMaquinariaSeguro[] = [];
  seguroActual: BienMaquinariaSeguro | null = null;

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
      tiposMaquinaria: this.catalogosApi.listarTiposMaquinariaHojaVida(),
      tiposGravamenes: this.catalogosApi.listarTiposGravamenesHojaVida()
    }).subscribe({
      next: r => {
        this.tiposBienes = r.tiposBienes ?? [];
        this.tiposMaquinaria = r.tiposMaquinaria ?? [];
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

        console.error('Error cargando bienes maquinaria', err);
        this.error = 'No fue posible cargar los bienes maquinaria.';
      },
      complete: () => this.cargando = false
    });
  }

  nuevo(): void {
    this.error = '';
    this.mensaje = '';

    const dto = nuevoBienMaquinaria(this.idDatosPersonal);

    const tipoMaquinariaBien = this.tiposBienes.find(t =>
      (t.codigo ?? '').toUpperCase() === 'MAQUINARIA'
      || (t.nombre ?? '').toUpperCase().includes('MAQUIN')
    );

    if (tipoMaquinariaBien?.id) {
      dto.idTipoBien = tipoMaquinariaBien.id;
    }

    this.bienActual = dto;
    this.modoFormulario = 'NUEVO';
  }

  editar(bien: BienMaquinaria): void {
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
        this.mensaje = 'Bien maquinaria guardado correctamente.';
        this.refrescar();
      },
      error: err => {
        console.error('Error guardando bien maquinaria', err);
        this.error = this.extraerMensajeError(err);
        this.guardando = false;
      }
    });
  }

  eliminar(bien: BienMaquinaria): void {
    if (!bien.idBien) return;

    const ok = confirm(
      `¿Eliminar la maquinaria "${bien.descripcionGeneral}"?`
    );

    if (!ok) return;

    this.error = '';
    this.mensaje = '';

    this.bienesApi.eliminar(bien.idBien).subscribe({
      next: () => {
        this.mensaje = 'Bien maquinaria eliminada correctamente.';
        this.cargarBienes();
      },
      error: err => {
        console.error('Error eliminando bien maquinaria', err);
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

  valorNeto(bien: BienMaquinaria): number {
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

  private normalizarParaGuardar(
    dto: BienMaquinaria
  ): BienMaquinaria {

    return {
      ...dto,

      idDatosPersonal: this.idDatosPersonal,

      porcentajePropiedad:
        Number(dto.porcentajePropiedad || 100),

      valorComercial:
        Number(dto.valorComercial || 0),

      valorGravamen:
        Number(dto.valorGravamen || 0),

      marca:
        dto.marca?.trim().toUpperCase() || null,

      modelo:
        dto.modelo?.trim().toUpperCase() || null,

      serial:
        dto.serial?.trim().toUpperCase() || null,

      referencia:
        dto.referencia?.trim().toUpperCase() || null,

      descripcionTecnica:
        dto.descripcionTecnica?.trim() || null,

      ubicacion:
        dto.ubicacion?.trim().toUpperCase() || null,

      estadoOperativo:
        dto.estadoOperativo?.trim().toUpperCase() || null,

      observaciones:
        dto.observaciones?.trim() || null
    };
  }

  // ============================================================
  // SEGUROS
  // ============================================================

  abrirSeguros(bien: BienMaquinaria): void {
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
      next: r => {
        this.seguros = r ?? [];
      },
      error: err => {
        if (err?.status === 204) {
          this.seguros = [];
          return;
        }

        console.error(
          'Error cargando seguros de maquinaria',
          err
        );

        this.error =
          'No fue posible cargar los seguros de la maquinaria.';
      },
      complete: () => {
        this.cargandoSeguros = false;
      }
    });
  }

  nuevoSeguro(): void {
    if (!this.bienSeleccionadoSeguros?.idBien) return;

    this.seguroActual =
      nuevoSeguro(this.bienSeleccionadoSeguros.idBien);

    this.modoSeguro = 'NUEVO';
  }

  editarSeguro(s: BienMaquinariaSeguro): void {
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

    const dto: BienMaquinariaSeguro = {
      ...this.seguroActual,

      aseguradora:
        this.seguroActual.aseguradora?.trim() || null,

      numeroPoliza:
        this.seguroActual.numeroPoliza?.trim() || null,

      valorAsegurado:
        Number(this.seguroActual.valorAsegurado || 0),

      estadoSeguro:
        (this.seguroActual.estadoSeguro || 'A')
          .trim()
          .toUpperCase(),

      observaciones:
        this.seguroActual.observaciones?.trim() || null
    };

    const peticion =
      this.modoSeguro === 'EDITAR'
      && dto.idBienMaquinariaSeguro

        ? this.segurosApi.actualizar(
            dto.idBienMaquinariaSeguro,
            dto
          )

        : this.segurosApi.crear(dto);

    peticion.subscribe({
      next: () => {
        this.mensaje =
          'Seguro de maquinaria guardado correctamente.';

        if (dto.idBien) {
          this.cargarSeguros(dto.idBien);
        }

        this.cancelarSeguro();
        this.cargarBienes();
      },
      error: err => {
        console.error(
          'Error guardando seguro de maquinaria',
          err
        );

        this.error = this.extraerMensajeError(err);
        this.guardandoSeguro = false;
      }
    });
  }

  eliminarSeguro(s: BienMaquinariaSeguro): void {
    if (!s.idBienMaquinariaSeguro) return;

    const ok = confirm(
      '¿Eliminar este seguro de maquinaria?'
    );

    if (!ok) return;

    this.error = '';
    this.mensaje = '';

    this.segurosApi
      .eliminar(s.idBienMaquinariaSeguro)
      .subscribe({
        next: () => {
          this.mensaje =
            'Seguro de maquinaria eliminado correctamente.';

          if (s.idBien) {
            this.cargarSeguros(s.idBien);
          }

          this.cargarBienes();
        },
        error: err => {
          console.error(
            'Error eliminando seguro de maquinaria',
            err
          );

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
    if (err?.error?.message) {
      return err.error.message;
    }

    if (typeof err?.error === 'string') {
      return err.error;
    }

    if (err?.error?.error) {
      return err.error.error;
    }

    return 'No fue posible completar la operación.';
  }
}
