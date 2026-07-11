import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';

import {
  BienInversion,
  nuevoBienInversion
} from './bienes-inversiones.dto';

import { BienesInversionesApi } from './bienes-inversiones.api';

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
  selector: 'app-bienes-inversiones-upsert',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    NumericFormatDirective
  ],
  templateUrl: './bienes-inversiones-upsert.component.html',
  styleUrls: ['./bienes-inversiones-upsert.component.scss']
})
export class BienesInversionesUpsertComponent implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly location = inject(Location);

  private readonly bienesApi = inject(BienesInversionesApi);
  private readonly datosApi = inject(DatosPersonalesApi);
  private readonly catalogosApi = inject(CatalogosApi);

  idDatosPersonal = 0;

  asociado: DatosPersonales | null = null;
  bienes: BienInversion[] = [];

  bienActual: BienInversion | null = null;

  modoFormulario: 'NINGUNO' | 'NUEVO' | 'EDITAR' = 'NINGUNO';

  cargando = false;
  guardando = false;
  error = '';
  mensaje = '';

  tiposBienes: CatalogoIdCodigoNombre[] = [];
  tiposInversiones: CatalogoIdCodigoNombre[] = [];
  tiposGravamenes: CatalogoIdCodigoNombre[] = [];

  resumen = {
    cantidad: 0,
    valorComercial: 0,
    valorGravamen: 0,
    patrimonioNeto: 0
  };

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
      tiposInversiones: this.catalogosApi.listarTiposInversionesHojaVida(),
      tiposGravamenes: this.catalogosApi.listarTiposGravamenesHojaVida()
    }).subscribe({
      next: r => {
        this.tiposBienes = r.tiposBienes ?? [];
        this.tiposInversiones = r.tiposInversiones ?? [];
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
      next: (r: DatosPersonales) => {
        this.asociado = r;
      },
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

        console.error('Error cargando bienes inversiones', err);
        this.error = 'No fue posible cargar los bienes inversiones.';
      },
      complete: () => {
        this.cargando = false;
      }
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

    const dto = nuevoBienInversion(this.idDatosPersonal);

    const tipoInversionBien = this.tiposBienes.find(t =>
      (t.codigo ?? '').toUpperCase() === 'INVERSION'
      || (t.codigo ?? '').toUpperCase() === 'INVERSIÓN'
      || (t.nombre ?? '').toUpperCase().includes('INVERS')
    );

    if (tipoInversionBien?.id) {
      dto.idTipoBien = tipoInversionBien.id;
    }

    this.bienActual = dto;
    this.modoFormulario = 'NUEVO';
  }

  editar(bien: BienInversion): void {
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
    if (!this.bienActual) {
      return;
    }

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
        this.mensaje = 'Bien inversión guardado correctamente.';
        this.refrescar();
      },
      error: err => {
        console.error('Error guardando bien inversión', err);
        this.error = this.extraerMensajeError(err);
        this.guardando = false;
      }
    });
  }

  eliminar(bien: BienInversion): void {
    if (!bien.idBien) {
      return;
    }

    const ok = confirm(
      `¿Eliminar la inversión "${bien.descripcionGeneral}"?`
    );

    if (!ok) {
      return;
    }

    this.error = '';
    this.mensaje = '';

    this.bienesApi.eliminar(bien.idBien).subscribe({
      next: () => {
        this.mensaje = 'Bien inversión eliminado correctamente.';
        this.cargarBienes();
      },
      error: err => {
        console.error('Error eliminando bien inversión', err);
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

  valorNeto(bien: BienInversion): number {
    return Number(bien.valorComercial || 0)
      - Number(bien.valorGravamen || 0);
  }

  // ============================================================
  // UTILIDADES
  // ============================================================

  volver(): void {
    this.location.back();
  }

  nombreAsociado(): string {
    if (!this.asociado) {
      return '';
    }

    return [
      this.asociado.nombres,
      this.asociado.primerApellido,
      this.asociado.segundoApellido
    ]
      .filter(Boolean)
      .join(' ');
  }

  private normalizarParaGuardar(
    dto: BienInversion
  ): BienInversion {

    return {
      ...dto,

      idDatosPersonal: this.idDatosPersonal,

      porcentajePropiedad:
        Number(dto.porcentajePropiedad || 100),

      valorComercial:
        Number(dto.valorComercial || 0),

      valorGravamen:
        Number(dto.valorGravamen || 0),

      valorNominal:
        Number(dto.valorNominal || 0),

      valorActual:
        Number(dto.valorActual || 0),

      tasaRendimiento:
        dto.tasaRendimiento == null
          ? null
          : Number(dto.tasaRendimiento),

      descripcionGeneral:
        dto.descripcionGeneral?.trim() || '',

      entidad:
        dto.entidad?.trim().toUpperCase() || null,

      numeroTitulo:
        dto.numeroTitulo?.trim().toUpperCase() || null,

      observaciones:
        dto.observaciones?.trim() || null
    };
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
