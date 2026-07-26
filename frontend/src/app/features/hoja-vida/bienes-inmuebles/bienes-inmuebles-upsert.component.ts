import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

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
  CatalogosApi,
  CodigoNombreDTO,
  Departamento,
  Ciudad
} from '../../../shared/catalogos/catalogos.api';

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
  tiposZonasInmuebles: CatalogoIdCodigoNombre[] = [];
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
      tiposBienes:
        this.catalogosApi.listarTiposBienesHojaVida(),

      tiposInmuebles:
        this.catalogosApi.listarTiposInmueblesHojaVida(),

      tiposZonasInmuebles:
        this.catalogosApi.listarTiposZonasInmueblesHojaVida(),

      tiposGravamenes:
        this.catalogosApi.listarTiposGravamenesHojaVida(),

      paises:
        this.catalogosApi.listarPaises(),

      departamentos:
        this.catalogosApi.listarDepartamentos()
    }).subscribe({

      next: r => {

        this.tiposBienes =
          r.tiposBienes ?? [];

        this.tiposInmuebles =
          r.tiposInmuebles ?? [];

        this.tiposZonasInmuebles =
          r.tiposZonasInmuebles ?? [];

        this.tiposGravamenes =
          r.tiposGravamenes ?? [];

        this.paises =
          r.paises ?? [];

        this.departamentos =
          r.departamentos ?? [];
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

    this.cargando = true;

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

      complete: () => {
        this.cargando = false;
      }
    });
  }

  refrescar(): void {
    this.cancelar();
    this.cargarBienes();
  }

  // ============================================================
  // FORMULARIO DEL INMUEBLE
  // ============================================================

  nuevo(): void {

    this.error = '';
    this.mensaje = '';

    this.ciudades = [];
    this.ciudadesNotaria = [];

    const dto = nuevoBienInmueble(
      this.idDatosPersonal
    );

    const tipoInmueble = this.tiposBienes.find(
      tipo =>
        (tipo.codigo ?? '').trim().toUpperCase() === 'INMUEBLE'
    );

    if (tipoInmueble?.id) {
      dto.idTipoBien = tipoInmueble.id;
    }

    if (!dto.estadoBien) {
      dto.estadoBien = 'A';
    }

    if (!dto.fechaEstado) {
      dto.fechaEstado =
        dto.fechaAdquisicion || this.obtenerFechaActual();
    }

    this.bienActual = dto;
    this.modoFormulario = 'NUEVO';
  }

  editar(bien: BienInmueble): void {

    this.error = '';
    this.mensaje = '';

    this.bienActual = {
      ...bien,

      idDatosPersonal:
        this.idDatosPersonal,

      estadoBien:
        bien.estadoBien || 'A',

      fechaEstado:
        bien.fechaEstado ||
        bien.fechaAdquisicion ||
        this.obtenerFechaActual(),

      idTipoZonaInmueble:
        Number(bien.idTipoZonaInmueble || 0)
    };

    this.modoFormulario = 'EDITAR';

    this.cargarCiudadesEdicion(
      this.bienActual
    );
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

    const dto =
      this.normalizarParaGuardar(
        this.bienActual
      );

    if (!this.validarBien(dto)) {
      return;
    }

    this.guardando = true;

    const peticion =
      this.modoFormulario === 'EDITAR' &&
      dto.idBien
        ? this.bienesApi.actualizar(
            dto.idBien,
            dto
          )
        : this.bienesApi.crear(dto);

    peticion.subscribe({

      next: () => {
        this.mensaje =
          'Bien inmueble guardado correctamente.';

        this.refrescar();
      },

      error: err => {
        console.error(
          'Error guardando bien inmueble',
          err
        );

        this.error =
          this.extraerMensajeError(err);

        this.guardando = false;
      }
    });
  }

  eliminar(bien: BienInmueble): void {

    if (!bien.idBien) {
      return;
    }

    const confirmar = confirm(
      `¿Eliminar el bien inmueble "${bien.descripcionGeneral}"?`
    );

    if (!confirmar) {
      return;
    }

    this.error = '';
    this.mensaje = '';

    this.bienesApi.eliminar(bien.idBien).subscribe({

      next: () => {

        this.mensaje =
          'Bien inmueble eliminado correctamente.';

        this.cargarBienes();
      },

      error: err => {

        console.error(
          'Error eliminando bien inmueble',
          err
        );

        this.error =
          this.extraerMensajeError(err);
      }
    });
  }

  private validarBien(
    dto: BienInmueble
  ): boolean {

    if (!dto.idTipoInmueble) {
      this.error =
        'Debe seleccionar el tipo de inmueble.';
      return false;
    }

    if (!dto.fechaAdquisicion) {
      this.error =
        'Debe registrar la fecha de adquisición.';
      return false;
    }

    if (!dto.estadoBien) {
      this.error =
        'Debe seleccionar el estado del bien.';
      return false;
    }

    if (!dto.fechaEstado) {
      this.error =
        'Debe registrar la fecha del estado del bien.';
      return false;
    }

    if (!dto.descripcionGeneral?.trim()) {
      this.error =
        'Debe registrar la descripción general.';
      return false;
    }

    if (!dto.idTipoZonaInmueble) {
      this.error =
        'Debe seleccionar el tipo de zona del inmueble.';
      return false;
    }

    if (!dto.idPais) {
      this.error =
        'Debe seleccionar el país.';
      return false;
    }

    if (!dto.idDepartamento) {
      this.error =
        'Debe seleccionar el departamento.';
      return false;
    }

    if (!dto.idCiudad) {
      this.error =
        'Debe seleccionar la ciudad.';
      return false;
    }

    if (!dto.direccion?.trim()) {
      this.error =
        'Debe registrar la dirección del inmueble.';
      return false;
    }

    if (!dto.idTipoGravamen) {
      this.error =
        'Debe seleccionar el tipo de gravamen.';
      return false;
    }

    if (
      Number(dto.porcentajePropiedad || 0) <= 0 ||
      Number(dto.porcentajePropiedad || 0) > 100
    ) {
      this.error =
        'El porcentaje de propiedad debe ser mayor que cero y máximo 100.';
      return false;
    }

    return true;
  }

  private normalizarParaGuardar(
    dto: BienInmueble
  ): BienInmueble {

    return {
      ...dto,

      idDatosPersonal:
        this.idDatosPersonal,

      idTipoBien:
        Number(dto.idTipoBien || 0),

      idTipoInmueble:
        Number(dto.idTipoInmueble || 0),

      idTipoZonaInmueble:
        Number(dto.idTipoZonaInmueble || 0),

      idPais:
        Number(dto.idPais || 0),

      idDepartamento:
        Number(dto.idDepartamento || 0),

      idCiudad:
        Number(dto.idCiudad || 0),

      idPaisNotaria:
        dto.idPaisNotaria
          ? Number(dto.idPaisNotaria)
          : null,

      idDepartamentoNotaria:
        dto.idDepartamentoNotaria
          ? Number(dto.idDepartamentoNotaria)
          : null,

      idCiudadNotaria:
        dto.idCiudadNotaria
          ? Number(dto.idCiudadNotaria)
          : null,

      idTipoGravamen:
        Number(dto.idTipoGravamen || 0),

      porcentajePropiedad:
        Number(dto.porcentajePropiedad || 0),

      valorComercial:
        Number(dto.valorComercial || 0),

      valorGravamen:
        Number(dto.valorGravamen || 0),

      areaTerreno:
        Number(dto.areaTerreno || 0),

      areaConstruida:
        Number(dto.areaConstruida || 0),

      estadoBien:
        (dto.estadoBien || 'A')
          .trim()
          .toUpperCase(),

      fechaAdquisicion:
        dto.fechaAdquisicion || '',

      fechaEstado:
        dto.fechaEstado || '',

      descripcionGeneral:
        dto.descripcionGeneral?.trim() || '',

      direccion:
        dto.direccion?.trim() || '',

      barrioVereda:
        dto.barrioVereda?.trim() || null,

      numeroMatriculaInmobiliaria:
        dto.numeroMatriculaInmobiliaria?.trim() || null,

      cedulaCatastral:
        dto.cedulaCatastral?.trim() || null,

      numeroEscritura:
        dto.numeroEscritura?.trim() || null,

      fechaEscritura:
        dto.fechaEscritura || null,

      notaria:
        dto.notaria?.trim() || null,

      fechaRegistroEscritura:
        dto.fechaRegistroEscritura || null,

      oficinaRegistro:
        dto.oficinaRegistro?.trim() || null,

      observacionesBien:
        dto.observacionesBien?.trim() || null,

      observaciones:
        dto.observaciones?.trim() || null
    };
  }

  // ============================================================
  // RESUMEN
  // ============================================================

  calcularResumen(): void {

    this.resumen.cantidad =
      this.bienes.length;

    this.resumen.valorComercial =
      this.bienes.reduce(
        (total, bien) =>
          total +
          Number(bien.valorComercial || 0),
        0
      );

    this.resumen.valorGravamen =
      this.bienes.reduce(
        (total, bien) =>
          total +
          Number(bien.valorGravamen || 0),
        0
      );

    this.resumen.patrimonioNeto =
      this.resumen.valorComercial -
      this.resumen.valorGravamen;
  }

  valorNeto(
    bien: BienInmueble
  ): number {

    return (
      Number(bien.valorComercial || 0) -
      Number(bien.valorGravamen || 0)
    );
  }

  // ============================================================
  // UBICACIONES
  // ============================================================

  onDepartamentoChange(): void {

    if (!this.bienActual) {
      return;
    }

    const idDepartamento =
      Number(
        this.bienActual.idDepartamento || 0
      );

    this.bienActual.idCiudad = 0;
    this.ciudades = [];

    if (!idDepartamento) {
      return;
    }

    this.catalogosApi
      .listarCiudadesPorDepartamento(
        idDepartamento
      )
      .subscribe({

        next: r => {
          this.ciudades = r ?? [];
        },

        error: () => {
          this.ciudades = [];
        }
      });
  }

  onDepartamentoNotariaChange(): void {

    if (!this.bienActual) {
      return;
    }

    const idDepartamento =
      Number(
        this.bienActual.idDepartamentoNotaria || 0
      );

    this.bienActual.idCiudadNotaria = null;
    this.ciudadesNotaria = [];

    if (!idDepartamento) {
      return;
    }

    this.catalogosApi
      .listarCiudadesPorDepartamento(
        idDepartamento
      )
      .subscribe({

        next: r => {
          this.ciudadesNotaria = r ?? [];
        },

        error: () => {
          this.ciudadesNotaria = [];
        }
      });
  }

  private cargarCiudadesEdicion(
    bien: BienInmueble
  ): void {

    this.ciudades = [];
    this.ciudadesNotaria = [];

    if (bien.idDepartamento) {

      this.catalogosApi
        .listarCiudadesPorDepartamento(
          bien.idDepartamento
        )
        .subscribe({

          next: r => {
            this.ciudades = r ?? [];
          },

          error: () => {
            this.ciudades = [];
          }
        });
    }

    if (bien.idDepartamentoNotaria) {

      this.catalogosApi
        .listarCiudadesPorDepartamento(
          bien.idDepartamentoNotaria
        )
        .subscribe({

          next: r => {
            this.ciudadesNotaria = r ?? [];
          },

          error: () => {
            this.ciudadesNotaria = [];
          }
        });
    }
  }

  // ============================================================
  // AVALÚOS
  // ============================================================

  abrirAvaluos(
    bien: BienInmueble
  ): void {

    if (!bien.idBien) {
      return;
    }

    this.cerrarSeguros();

    this.error = '';
    this.mensaje = '';

    this.bienSeleccionadoAvaluos = bien;
    this.avaluoActual = null;
    this.modoAvaluo = 'NINGUNO';

    this.cargarAvaluos(
      bien.idBien
    );
  }

  cargarAvaluos(
    idBien: number
  ): void {

    this.cargandoAvaluos = true;
    this.avaluos = [];

    this.avaluosApi
      .listarPorBien(idBien)
      .subscribe({

        next: r => {
          this.avaluos = r ?? [];
        },

        error: err => {

          if (err?.status === 204) {
            this.avaluos = [];
            return;
          }

          console.error(
            'Error cargando avalúos',
            err
          );

          this.error =
            'No fue posible cargar los avalúos del inmueble.';
        },

        complete: () => {
          this.cargandoAvaluos = false;
        }
      });
  }

  nuevoAvaluo(): void {

    if (!this.bienSeleccionadoAvaluos?.idBien) {
      return;
    }

    this.error = '';
    this.mensaje = '';

    this.avaluoActual = nuevoAvaluo(
      this.bienSeleccionadoAvaluos.idBien
    );

    this.calcularVencimientoAvaluo();

    this.modoAvaluo = 'NUEVO';
  }

  editarAvaluo(
    avaluo: BienInmuebleAvaluo
  ): void {

    this.error = '';
    this.mensaje = '';

    this.avaluoActual = {
      ...avaluo,

      vigenciaAnios:
        Number(avaluo.vigenciaAnios || 3),

      valorTerreno:
        Number(avaluo.valorTerreno || 0),

      valorConstruccion:
        Number(avaluo.valorConstruccion || 0),

      valorCultivos:
        Number(avaluo.valorCultivos || 0),

      valorOtros:
        Number(avaluo.valorOtros || 0),

      valorAvaluoComercial:
        Number(avaluo.valorAvaluoComercial || 0),

      valorAvaluoCatastral:
        Number(avaluo.valorAvaluoCatastral || 0)
    };

    this.modoAvaluo = 'EDITAR';
  }

  cancelarAvaluo(): void {

    this.avaluoActual = null;
    this.modoAvaluo = 'NINGUNO';
    this.guardandoAvaluo = false;
  }

  guardarAvaluo(): void {

    if (!this.avaluoActual) {
      return;
    }

    this.error = '';
    this.mensaje = '';

    const dto: BienInmuebleAvaluo = {
      ...this.avaluoActual,

      vigenciaAnios:
        Number(
          this.avaluoActual.vigenciaAnios || 0
        ),

      valorTerreno:
        Number(
          this.avaluoActual.valorTerreno || 0
        ),

      valorConstruccion:
        Number(
          this.avaluoActual.valorConstruccion || 0
        ),

      valorCultivos:
        Number(
          this.avaluoActual.valorCultivos || 0
        ),

      valorOtros:
        Number(
          this.avaluoActual.valorOtros || 0
        ),

      valorAvaluoComercial:
        Number(
          this.avaluoActual.valorAvaluoComercial || 0
        ),

      valorAvaluoCatastral:
        Number(
          this.avaluoActual.valorAvaluoCatastral || 0
        ),

      entidadAvaluadora:
        this.avaluoActual.entidadAvaluadora?.trim() || null,

      numeroInforme:
        this.avaluoActual.numeroInforme?.trim() || null,

      observaciones:
        this.avaluoActual.observaciones?.trim() || null
    };

    if (!this.validarAvaluo(dto)) {
      return;
    }

    this.guardandoAvaluo = true;

    const peticion =
      this.modoAvaluo === 'EDITAR' &&
      dto.idBienInmuebleAvaluo
        ? this.avaluosApi.actualizar(
            dto.idBienInmuebleAvaluo,
            dto
          )
        : this.avaluosApi.crear(dto);

    peticion.subscribe({

      next: () => {

        this.mensaje =
          'Avalúo guardado correctamente.';

        this.cancelarAvaluo();

        this.cargarAvaluos(
          dto.idBien
        );

        this.cargarBienes();
      },

      error: err => {

        console.error(
          'Error guardando avalúo',
          err
        );

        this.error =
          this.extraerMensajeError(err);

        this.guardandoAvaluo = false;
      }
    });
  }

  private validarAvaluo(
    dto: BienInmuebleAvaluo
  ): boolean {

    if (!dto.fechaAvaluo) {
      this.error =
        'Debe registrar la fecha del avalúo.';
      return false;
    }

    if (
      Number(dto.vigenciaAnios || 0) <= 0 ||
      Number(dto.vigenciaAnios || 0) > 30
    ) {
      this.error =
        'La vigencia del avalúo debe estar entre 1 y 30 años.';
      return false;
    }

    if (
      Number(dto.valorAvaluoComercial) < 0
    ) {
      this.error =
        'El valor comercial del avalúo no puede ser negativo.';
      return false;
    }

    if (
      Number(dto.valorAvaluoCatastral) < 0
    ) {
      this.error =
        'El valor catastral del avalúo no puede ser negativo.';
      return false;
    }

    if (
      Number(dto.valorTerreno) < 0 ||
      Number(dto.valorConstruccion) < 0 ||
      Number(dto.valorCultivos) < 0 ||
      Number(dto.valorOtros) < 0
    ) {
      this.error =
        'Los componentes del avalúo no pueden tener valores negativos.';
      return false;
    }

    return true;
  }

  eliminarAvaluo(
    avaluo: BienInmuebleAvaluo
  ): void {

    if (!avaluo.idBienInmuebleAvaluo) {
      return;
    }

    const confirmar = confirm(
      '¿Eliminar este avalúo?'
    );

    if (!confirmar) {
      return;
    }

    this.avaluosApi
      .eliminar(
        avaluo.idBienInmuebleAvaluo
      )
      .subscribe({

        next: () => {

          this.mensaje =
            'Avalúo eliminado correctamente.';

          this.cargarAvaluos(
            avaluo.idBien
          );

          this.cargarBienes();
        },

        error: err => {

          console.error(
            'Error eliminando avalúo',
            err
          );

          this.error =
            this.extraerMensajeError(err);
        }
      });
  }

  cerrarAvaluos(): void {

    this.bienSeleccionadoAvaluos = null;
    this.avaluos = [];
    this.avaluoActual = null;
    this.modoAvaluo = 'NINGUNO';
    this.cargandoAvaluos = false;
    this.guardandoAvaluo = false;
  }

  calcularVencimientoAvaluo(): void {

    if (!this.avaluoActual?.fechaAvaluo) {
      return;
    }

    const vigencia =
      Number(
        this.avaluoActual.vigenciaAnios || 0
      );

    if (vigencia <= 0) {
      this.avaluoActual.fechaVencimientoAvaluo = null;
      return;
    }

    const fecha = new Date(
      `${this.avaluoActual.fechaAvaluo}T00:00:00`
    );

    if (Number.isNaN(fecha.getTime())) {
      this.avaluoActual.fechaVencimientoAvaluo = null;
      return;
    }

    fecha.setFullYear(
      fecha.getFullYear() + vigencia
    );

    this.avaluoActual.fechaVencimientoAvaluo =
      this.formatearFechaLocal(fecha);
  }

  calcularTotalComponentesAvaluo(
    avaluo: BienInmuebleAvaluo
  ): number {

    return (
      Number(avaluo.valorTerreno || 0) +
      Number(avaluo.valorConstruccion || 0) +
      Number(avaluo.valorCultivos || 0) +
      Number(avaluo.valorOtros || 0)
    );
  }

  // ============================================================
  // SEGUROS
  // ============================================================

  abrirSeguros(
    bien: BienInmueble
  ): void {

    if (!bien.idBien) {
      return;
    }

    this.cerrarAvaluos();

    this.error = '';
    this.mensaje = '';

    this.bienSeleccionadoSeguros = bien;
    this.seguroActual = null;
    this.modoSeguro = 'NINGUNO';

    this.cargarSeguros(
      bien.idBien
    );
  }

  cargarSeguros(
    idBien: number
  ): void {

    this.cargandoSeguros = true;
    this.seguros = [];

    this.segurosApi
      .listarPorBien(idBien)
      .subscribe({

        next: r => {
          this.seguros = r ?? [];
        },

        error: err => {

          if (err?.status === 204) {
            this.seguros = [];
            return;
          }

          console.error(
            'Error cargando seguros',
            err
          );

          this.error =
            'No fue posible cargar los seguros del inmueble.';
        },

        complete: () => {
          this.cargandoSeguros = false;
        }
      });
  }

  nuevoSeguro(): void {

    if (!this.bienSeleccionadoSeguros?.idBien) {
      return;
    }

    this.error = '';
    this.mensaje = '';

    this.seguroActual = nuevoSeguro(
      this.bienSeleccionadoSeguros.idBien
    );

    this.modoSeguro = 'NUEVO';
  }

  editarSeguro(
    seguro: BienInmuebleSeguro
  ): void {

    this.error = '';
    this.mensaje = '';

    this.seguroActual = {
      ...seguro,

      valorAsegurado:
        Number(seguro.valorAsegurado || 0),

      estadoSeguro:
        (seguro.estadoSeguro || 'A')
          .trim()
          .toUpperCase()
    };

    this.modoSeguro = 'EDITAR';
  }

  cancelarSeguro(): void {

    this.seguroActual = null;
    this.modoSeguro = 'NINGUNO';
    this.guardandoSeguro = false;
  }

  guardarSeguro(): void {

    if (!this.seguroActual) {
      return;
    }

    this.error = '';
    this.mensaje = '';

    const dto: BienInmuebleSeguro = {
      ...this.seguroActual,

      valorAsegurado:
        Number(
          this.seguroActual.valorAsegurado || 0
        ),

      estadoSeguro:
        (this.seguroActual.estadoSeguro || 'A')
          .trim()
          .toUpperCase(),

      aseguradora:
        this.seguroActual.aseguradora?.trim() || null,

      numeroPoliza:
        this.seguroActual.numeroPoliza?.trim() || null,

      fechaInicioSeguro:
        this.seguroActual.fechaInicioSeguro || null,

      fechaVencimientoSeguro:
        this.seguroActual.fechaVencimientoSeguro || null,

      observaciones:
        this.seguroActual.observaciones?.trim() || null
    };

    if (!this.validarSeguro(dto)) {
      return;
    }

    this.guardandoSeguro = true;

    const peticion =
      this.modoSeguro === 'EDITAR' &&
      dto.idBienInmuebleSeguro
        ? this.segurosApi.actualizar(
            dto.idBienInmuebleSeguro,
            dto
          )
        : this.segurosApi.crear(dto);

    peticion.subscribe({

      next: () => {

        this.mensaje =
          'Seguro guardado correctamente.';

        this.cancelarSeguro();

        this.cargarSeguros(
          dto.idBien
        );

        this.cargarBienes();
      },

      error: err => {

        console.error(
          'Error guardando seguro',
          err
        );

        this.error =
          this.extraerMensajeError(err);

        this.guardandoSeguro = false;
      }
    });
  }

  private validarSeguro(
    dto: BienInmuebleSeguro
  ): boolean {

    if (Number(dto.valorAsegurado) < 0) {
      this.error =
        'El valor asegurado no puede ser negativo.';
      return false;
    }

    if (!dto.estadoSeguro) {
      this.error =
        'Debe seleccionar el estado del seguro.';
      return false;
    }

    if (
      dto.fechaInicioSeguro &&
      dto.fechaVencimientoSeguro &&
      dto.fechaVencimientoSeguro <
        dto.fechaInicioSeguro
    ) {
      this.error =
        'La fecha de vencimiento no puede ser anterior a la fecha de inicio.';
      return false;
    }

    return true;
  }

  eliminarSeguro(
    seguro: BienInmuebleSeguro
  ): void {

    if (!seguro.idBienInmuebleSeguro) {
      return;
    }

    const confirmar = confirm(
      '¿Eliminar este seguro?'
    );

    if (!confirmar) {
      return;
    }

    this.segurosApi
      .eliminar(
        seguro.idBienInmuebleSeguro
      )
      .subscribe({

        next: () => {

          this.mensaje =
            'Seguro eliminado correctamente.';

          this.cargarSeguros(
            seguro.idBien
          );

          this.cargarBienes();
        },

        error: err => {

          console.error(
            'Error eliminando seguro',
            err
          );

          this.error =
            this.extraerMensajeError(err);
        }
      });
  }

  cerrarSeguros(): void {

    this.bienSeleccionadoSeguros = null;
    this.seguros = [];
    this.seguroActual = null;
    this.modoSeguro = 'NINGUNO';
    this.cargandoSeguros = false;
    this.guardandoSeguro = false;
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

  private obtenerFechaActual(): string {
    return this.formatearFechaLocal(
      new Date()
    );
  }

  private formatearFechaLocal(
    fecha: Date
  ): string {

    const anio =
      fecha.getFullYear();

    const mes =
      String(
        fecha.getMonth() + 1
      ).padStart(2, '0');

    const dia =
      String(
        fecha.getDate()
      ).padStart(2, '0');

    return `${anio}-${mes}-${dia}`;
  }

  private extraerMensajeError(
    err: unknown
  ): string {

    const errorHttp = err as {
      error?: {
        message?: string;
        error?: string;
      } | string;
    };

    if (
      typeof errorHttp.error === 'object' &&
      errorHttp.error?.message
    ) {
      return errorHttp.error.message;
    }

    if (
      typeof errorHttp.error === 'string'
    ) {
      return errorHttp.error;
    }

    if (
      typeof errorHttp.error === 'object' &&
      errorHttp.error?.error
    ) {
      return errorHttp.error.error;
    }

    return 'No fue posible completar la operación.';
  }

}
