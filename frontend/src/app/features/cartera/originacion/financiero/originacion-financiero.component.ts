import {
  CommonModule
} from '@angular/common';

import {
  Component,
  OnInit
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  Router
} from '@angular/router';

import {
  forkJoin
} from 'rxjs';

import {
  NumericFormatDirective
} from '../../../../shared/utils/numeric-format.directive';

import {
  CatalogosApi,
  CodigoNombreDTO
} from '../../../../shared/catalogos/catalogos.api';

import {
  OriginacionSolicitudStateService
} from '../solicitud/originacion-solicitud-state.service';

import {
  OriginacionFinancieroApi
} from './originacion-financiero.api';

import {
  SolicitudFinanciero,
  SolicitudFinancieroDetalle,
  SolicitudFinancieroGuardarRequest
} from './originacion-financiero.models';


// =========================================================
// FORMULARIO FINANCIERO
// =========================================================

interface FormularioFinanciero {

  idSolicitudDeudor: number | null;

  /*
   * Tipo de persona proveniente del backend.
   *
   * 1 = Persona natural
   * 2 = Persona jurídica
   *
   * Este valor se utiliza únicamente para controlar
   * la presentación del formulario.
   *
   * NO se envía al guardar.
   */
  tipoPersona: string;


  // ---------------------------------------------------------
  // Actividad económica
  // ---------------------------------------------------------

  codigoOcupacion: string;
  codigoSectorEconomico: string;
  codigoActividadSes: string;
  codigoActividadDian: string;


  // ---------------------------------------------------------
  // Persona natural - ingresos
  // ---------------------------------------------------------

  valorSalario: number | null;
  valorPension: number | null;
  ingresoIndependiente: number | null;
  ingresosArriendo: number | null;
  ingresosComisiones: number | null;
  otrosIngresos: number | null;

  comentarioOtrosIngresos: string;


  // ---------------------------------------------------------
  // Persona natural - egresos
  // ---------------------------------------------------------

  egresosFamiliares: number | null;
  egresosArriendo: number | null;
  egresosCredito: number | null;
  otrosEgresos: number | null;

  comentarioOtrosEgresos: string;


  // ---------------------------------------------------------
  // Declaración de renta
  // ---------------------------------------------------------

  declaraRenta: boolean;
  anioDeclaracion: number | null;
  fechaPresentacionDeclaracion: string;


  // ---------------------------------------------------------
  // Persona jurídica
  // ---------------------------------------------------------

  ingresosOperacionales: number | null;
  ingresosNoOperacionales: number | null;

  costos: number | null;
  gastosOperacionales: number | null;
  gastosFinancieros: number | null;
  otrosGastos: number | null;


  // ---------------------------------------------------------
  // Balance
  // ---------------------------------------------------------

  activoCorriente: number | null;
  pasivoCorriente: number | null;

  utilidadOperacional: number | null;
  utilidadNeta: number | null;

  totalActivos: number | null;
  totalPasivos: number | null;


  // ---------------------------------------------------------
  // Información complementaria
  // ---------------------------------------------------------

  origenFondos: string;
  relacionFinanciera: string;
  deudaRelacionFinanciera: number | null;
}


@Component({
  selector: 'app-originacion-financiero',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
    NumericFormatDirective
  ],

  templateUrl:
    './originacion-financiero.component.html',

  styleUrl:
    './originacion-financiero.component.scss'
})
export class OriginacionFinancieroComponent
  implements OnInit {

  // =========================================================
  // SOLICITUD
  // =========================================================

  idSolicitudCredito: number | null = null;

  financieros: SolicitudFinanciero[] = [];

  deudorSeleccionado:
    SolicitudFinanciero | null = null;

  detalleActual:
    SolicitudFinancieroDetalle | null = null;


  // =========================================================
  // CATÁLOGOS
  // =========================================================

  ocupaciones: CodigoNombreDTO[] = [];
  sectoresEconomicos: CodigoNombreDTO[] = [];
  actividadesSes: CodigoNombreDTO[] = [];
  actividadesDian: CodigoNombreDTO[] = [];

  actividadesSesFiltradas: CodigoNombreDTO[] = [];
  actividadesDianFiltradas: CodigoNombreDTO[] = [];

  filtroSes = '';
  filtroDian = '';

  cargandoCatalogos = false;


  // =========================================================
  // FORMULARIO
  // =========================================================

  formulario:
    FormularioFinanciero =
      this.crearFormularioVacio();


  // =========================================================
  // ESTADOS
  // =========================================================

  cargando = false;
  cargandoDetalle = false;
  guardando = false;

  error = '';
  mensaje = '';


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly api:
      OriginacionFinancieroApi,

    private readonly catalogos:
      CatalogosApi,

    private readonly state:
      OriginacionSolicitudStateService,

    private readonly router:
      Router
  ) {}


  // =========================================================
  // INICIO
  // =========================================================

  ngOnInit(): void {

    this.idSolicitudCredito =
      this.state.getIdSolicitudCredito();

    if (!this.idSolicitudCredito) {

      this.error =
        'No se encontró una solicitud de crédito activa.';

      return;
    }

    this.cargarInicial();
  }


  // =========================================================
  // CARGA INICIAL
  // =========================================================

  private cargarInicial(): void {

    this.cargando = true;
    this.cargandoCatalogos = true;
    this.error = '';
    this.mensaje = '';

    forkJoin({

      financieros:
        this.api.listarPorSolicitud(
          this.idSolicitudCredito!
        ),

      ocupaciones:
        this.catalogos.listarOcupaciones(),

      sectoresEconomicos:
        this.catalogos.listarSectoresEconomicos(),

      actividadesSes:
        this.catalogos.listarActividadesSes(),

      actividadesDian:
        this.catalogos.listarActividadesDian()

    }).subscribe({

      next: resultado => {

        this.financieros =
          resultado.financieros ?? [];

        this.ocupaciones =
          resultado.ocupaciones ?? [];

        this.sectoresEconomicos =
          resultado.sectoresEconomicos ?? [];

        this.actividadesSes =
          resultado.actividadesSes ?? [];

        this.actividadesDian =
          resultado.actividadesDian ?? [];

        this.actividadesSesFiltradas = [
          ...this.actividadesSes
        ];

        this.actividadesDianFiltradas = [
          ...this.actividadesDian
        ];

        this.cargando = false;
        this.cargandoCatalogos = false;
      },

      error: err => {

        this.cargando = false;
        this.cargandoCatalogos = false;

        this.error =
          this.obtenerMensajeError(
            err,
            'No fue posible cargar la información financiera.'
          );
      }
    });
  }


  // =========================================================
  // LISTAR DEUDORES / FINANCIEROS
  // =========================================================

  cargarFinancieros(): void {

    if (!this.idSolicitudCredito) {
      return;
    }

    this.cargando = true;
    this.error = '';
    this.mensaje = '';

    this.api
      .listarPorSolicitud(
        this.idSolicitudCredito
      )
      .subscribe({

        next: financieros => {

          this.financieros =
            financieros ?? [];

          this.cargando = false;
        },

        error: err => {

          this.cargando = false;

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible consultar la información financiera de la solicitud.'
            );
        }
      });
  }


  // =========================================================
  // SELECCIONAR DEUDOR
  // =========================================================

  seleccionarDeudor(
    financiero: SolicitudFinanciero
  ): void {

    if (
      !financiero
      || !financiero.idSolicitudDeudor
    ) {
      return;
    }

    this.deudorSeleccionado =
      financiero;

    this.detalleActual = null;

    this.formulario =
      this.crearFormularioVacio();

    this.filtroSes = '';
    this.filtroDian = '';

    this.actividadesSesFiltradas = [
      ...this.actividadesSes
    ];

    this.actividadesDianFiltradas = [
      ...this.actividadesDian
    ];

    this.error = '';
    this.mensaje = '';

    this.cargarInformacionDeudor(
      financiero
    );
  }


  // =========================================================
  // CARGAR INFORMACIÓN DEL DEUDOR
  // =========================================================

  private cargarInformacionDeudor(
    financiero: SolicitudFinanciero
  ): void {

    this.cargandoDetalle = true;
    this.error = '';

    /*
     * Se consulta SIEMPRE el detalle.
     *
     * El backend:
     *
     * - devuelve la información guardada de la solicitud
     *   cuando ya existe;
     *
     * - precarga desde Hoja de Vida cuando todavía no
     *   existe un registro financiero de la solicitud.
     */
    this.api
      .buscarPorDeudor(
        financiero.idSolicitudDeudor
      )
      .subscribe({

        next: detalle => {

          this.detalleActual =
            detalle;

          this.cargarFormularioDesdeDetalle(
            detalle
          );

          this.cargandoDetalle = false;
        },

        error: err => {

          this.cargandoDetalle = false;

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible cargar la información financiera del deudor.'
            );
        }
      });
  }


  // =========================================================
  // FORMULARIO DESDE DETALLE
  // =========================================================

  private cargarFormularioDesdeDetalle(
    detalle: SolicitudFinancieroDetalle
  ): void {

    this.formulario = {

      idSolicitudDeudor:
        detalle.idSolicitudDeudor,

      tipoPersona:
        detalle.tipoPersona ?? '',


      // -------------------------------------------------------
      // Actividad económica
      // -------------------------------------------------------

      codigoOcupacion:
        detalle.codigoOcupacion ?? '',

      codigoSectorEconomico:
        detalle.codigoSectorEconomico ?? '',

      codigoActividadSes:
        detalle.codigoActividadSes ?? '',

      codigoActividadDian:
        detalle.codigoActividadDian ?? '',


      // -------------------------------------------------------
      // Persona natural
      // -------------------------------------------------------

      valorSalario:
        detalle.valorSalario,

      valorPension:
        detalle.valorPension,

      ingresoIndependiente:
        detalle.ingresoIndependiente,

      ingresosArriendo:
        detalle.ingresosArriendo,

      ingresosComisiones:
        detalle.ingresosComisiones,

      otrosIngresos:
        detalle.otrosIngresos,

      comentarioOtrosIngresos:
        detalle.comentarioOtrosIngresos ?? '',

      egresosFamiliares:
        detalle.egresosFamiliares,

      egresosArriendo:
        detalle.egresosArriendo,

      egresosCredito:
        detalle.egresosCredito,

      otrosEgresos:
        detalle.otrosEgresos,

      comentarioOtrosEgresos:
        detalle.comentarioOtrosEgresos ?? '',


      // -------------------------------------------------------
      // Declaración
      // -------------------------------------------------------

      declaraRenta:
        detalle.declaraRenta ?? false,

      anioDeclaracion:
        detalle.anioDeclaracion,

      fechaPresentacionDeclaracion:
        detalle.fechaPresentacionDeclaracion ?? '',


      // -------------------------------------------------------
      // Persona jurídica
      // -------------------------------------------------------

      ingresosOperacionales:
        detalle.ingresosOperacionales,

      ingresosNoOperacionales:
        detalle.ingresosNoOperacionales,

      costos:
        detalle.costos,

      gastosOperacionales:
        detalle.gastosOperacionales,

      gastosFinancieros:
        detalle.gastosFinancieros,

      otrosGastos:
        detalle.otrosGastos,

      activoCorriente:
        detalle.activoCorriente,

      pasivoCorriente:
        detalle.pasivoCorriente,

      utilidadOperacional:
        detalle.utilidadOperacional,

      utilidadNeta:
        detalle.utilidadNeta,


      // -------------------------------------------------------
      // Balance
      // -------------------------------------------------------

      totalActivos:
        detalle.totalActivos,

      totalPasivos:
        detalle.totalPasivos,


      // -------------------------------------------------------
      // Información complementaria
      // -------------------------------------------------------

      origenFondos:
        detalle.origenFondos ?? '',

      relacionFinanciera:
        detalle.relacionFinanciera ?? '',

      deudaRelacionFinanciera:
        detalle.deudaRelacionFinanciera
    };

    this.actualizarTextosCatalogos();
  }


  // =========================================================
  // TEXTOS CATÁLOGOS
  // =========================================================

  private actualizarTextosCatalogos(): void {

    const ses =
      this.actividadesSes.find(
        item =>
          String(item.codigo) ===
          this.formulario.codigoActividadSes
      );

    this.filtroSes =
      ses
        ? `${ses.codigo} - ${ses.nombre}`
        : this.formulario.codigoActividadSes;

    const dian =
      this.actividadesDian.find(
        item =>
          String(item.codigo) ===
          this.formulario.codigoActividadDian
      );

    this.filtroDian =
      dian
        ? `${dian.codigo} - ${dian.nombre}`
        : this.formulario.codigoActividadDian;
  }


  // =========================================================
  // FILTRAR ACTIVIDAD SES
  // =========================================================

  filtrarActividadesSes(): void {

    const q =
      this.filtroSes
        .trim()
        .toLowerCase();

    if (!q) {

      this.actividadesSesFiltradas = [
        ...this.actividadesSes
      ];

      return;
    }

    this.actividadesSesFiltradas =
      this.actividadesSes.filter(
        actividad => {

          const codigo =
            String(
              actividad.codigo ?? ''
            ).toLowerCase();

          const nombre =
            String(
              actividad.nombre ?? ''
            ).toLowerCase();

          return codigo.includes(q)
            || nombre.includes(q);
        }
      );
  }


  // =========================================================
  // SELECCIONAR ACTIVIDAD SES
  // =========================================================

  seleccionarActividadSes(
    actividad: CodigoNombreDTO
  ): void {

    this.formulario.codigoActividadSes =
      String(
        actividad.codigo ?? ''
      );

    this.filtroSes =
      `${actividad.codigo} - ${actividad.nombre}`;

    this.actividadesSesFiltradas = [];
  }


  // =========================================================
  // FILTRAR ACTIVIDAD DIAN
  // =========================================================

  filtrarActividadesDian(): void {

    const q =
      this.filtroDian
        .trim()
        .toLowerCase();

    if (!q) {

      this.actividadesDianFiltradas = [
        ...this.actividadesDian
      ];

      return;
    }

    this.actividadesDianFiltradas =
      this.actividadesDian.filter(
        actividad => {

          const codigo =
            String(
              actividad.codigo ?? ''
            ).toLowerCase();

          const nombre =
            String(
              actividad.nombre ?? ''
            ).toLowerCase();

          return codigo.includes(q)
            || nombre.includes(q);
        }
      );
  }


  // =========================================================
  // SELECCIONAR ACTIVIDAD DIAN
  // =========================================================

  seleccionarActividadDian(
    actividad: CodigoNombreDTO
  ): void {

    this.formulario.codigoActividadDian =
      String(
        actividad.codigo ?? ''
      );

    this.filtroDian =
      `${actividad.codigo} - ${actividad.nombre}`;

    this.actividadesDianFiltradas = [];
  }


  // =========================================================
  // GUARDAR
  // =========================================================

  guardar(): void {

    this.error = '';
    this.mensaje = '';

    const errorValidacion =
      this.validarFormulario();

    if (errorValidacion) {

      this.error =
        errorValidacion;

      return;
    }

    const request =
      this.construirRequest();

    if (!request) {

      this.error =
        'No fue posible construir la información financiera.';

      return;
    }

    this.guardando = true;

    this.api
      .guardar(request)
      .subscribe({

        next: detalle => {

          this.guardando = false;

          this.detalleActual =
            detalle;

          this.cargarFormularioDesdeDetalle(
            detalle
          );

          this.mensaje =
            'La información financiera fue guardada correctamente.';

          this.actualizarResumenDespuesDeGuardar(
            detalle
          );
        },

        error: err => {

          this.guardando = false;

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible guardar la información financiera.'
            );
        }
      });
  }


  // =========================================================
  // CONSTRUIR REQUEST
  // =========================================================

  private construirRequest():
    SolicitudFinancieroGuardarRequest | null {

    const idSolicitudDeudor =
      this.formulario.idSolicitudDeudor;

    if (!idSolicitudDeudor) {
      return null;
    }

    /*
     * tipoPersona NO se envía.
     *
     * El backend obtiene el tipo directamente desde
     * hoja_vida.datos_personales.
     */
    return {

      idSolicitudDeudor,


      // -------------------------------------------------------
      // Actividad económica
      // -------------------------------------------------------

      codigoOcupacion:
        this.normalizarTexto(
          this.formulario.codigoOcupacion
        ),

      codigoSectorEconomico:
        this.normalizarTexto(
          this.formulario.codigoSectorEconomico
        ),

      codigoActividadSes:
        this.normalizarTexto(
          this.formulario.codigoActividadSes
        ),

      codigoActividadDian:
        this.normalizarTexto(
          this.formulario.codigoActividadDian
        ),


      // -------------------------------------------------------
      // Persona natural
      // -------------------------------------------------------

      valorSalario:
        this.formulario.valorSalario,

      valorPension:
        this.formulario.valorPension,

      ingresoIndependiente:
        this.formulario.ingresoIndependiente,

      ingresosArriendo:
        this.formulario.ingresosArriendo,

      ingresosComisiones:
        this.formulario.ingresosComisiones,

      otrosIngresos:
        this.formulario.otrosIngresos,

      comentarioOtrosIngresos:
        this.normalizarTexto(
          this.formulario
            .comentarioOtrosIngresos
        ),

      egresosFamiliares:
        this.formulario.egresosFamiliares,

      egresosArriendo:
        this.formulario.egresosArriendo,

      egresosCredito:
        this.formulario.egresosCredito,

      otrosEgresos:
        this.formulario.otrosEgresos,

      comentarioOtrosEgresos:
        this.normalizarTexto(
          this.formulario
            .comentarioOtrosEgresos
        ),


      // -------------------------------------------------------
      // Declaración de renta
      // -------------------------------------------------------

      declaraRenta:
        this.formulario.declaraRenta,

      anioDeclaracion:
        this.formulario.declaraRenta
          ? this.formulario.anioDeclaracion
          : null,

      fechaPresentacionDeclaracion:
        this.formulario.declaraRenta
          ? (
              this.formulario
                .fechaPresentacionDeclaracion
              || null
            )
          : null,


      // -------------------------------------------------------
      // Persona jurídica
      // -------------------------------------------------------

      ingresosOperacionales:
        this.formulario.ingresosOperacionales,

      ingresosNoOperacionales:
        this.formulario
          .ingresosNoOperacionales,

      costos:
        this.formulario.costos,

      gastosOperacionales:
        this.formulario.gastosOperacionales,

      gastosFinancieros:
        this.formulario.gastosFinancieros,

      otrosGastos:
        this.formulario.otrosGastos,

      activoCorriente:
        this.formulario.activoCorriente,

      pasivoCorriente:
        this.formulario.pasivoCorriente,

      utilidadOperacional:
        this.formulario.utilidadOperacional,

      utilidadNeta:
        this.formulario.utilidadNeta,


      // -------------------------------------------------------
      // Balance
      // -------------------------------------------------------

      totalActivos:
        this.formulario.totalActivos,

      totalPasivos:
        this.formulario.totalPasivos,


      // -------------------------------------------------------
      // Información complementaria
      // -------------------------------------------------------

      origenFondos:
        this.normalizarTexto(
          this.formulario.origenFondos
        ),

      relacionFinanciera:
        this.normalizarTexto(
          this.formulario.relacionFinanciera
        ),

      deudaRelacionFinanciera:
        this.formulario
          .deudaRelacionFinanciera
    };
  }


  // =========================================================
  // VALIDACIONES FRONT
  // =========================================================

  private validarFormulario():
    string | null {

    if (
      !this.formulario.idSolicitudDeudor
    ) {

      return 'Debe seleccionar un deudor.';
    }


    // ---------------------------------------------------------
    // Tipo de persona
    // ---------------------------------------------------------

    if (
      !this.esPersonaNatural
      && !this.esPersonaJuridica
    ) {

      return 'No fue posible determinar el tipo de persona registrado en Hoja de Vida.';
    }


    // ---------------------------------------------------------
    // Actividad económica
    // ---------------------------------------------------------

    if (
      !this.formulario
        .codigoOcupacion
        .trim()
    ) {

      return 'Debe indicar la ocupación.';
    }

    if (
      !this.formulario
        .codigoSectorEconomico
        .trim()
    ) {

      return 'Debe indicar el sector económico.';
    }

    if (
      !this.formulario
        .codigoActividadSes
        .trim()
    ) {

      return 'Debe indicar la actividad económica SES.';
    }

    if (
      !this.formulario
        .codigoActividadDian
        .trim()
    ) {

      return 'Debe indicar la actividad económica DIAN.';
    }


    // ---------------------------------------------------------
    // Otros ingresos
    // ---------------------------------------------------------

    if (
      this.esPositivo(
        this.formulario.otrosIngresos
      )
      && !this.formulario
        .comentarioOtrosIngresos
        .trim()
    ) {

      return 'Debe indicar un comentario para otros ingresos.';
    }


    // ---------------------------------------------------------
    // Otros egresos
    // ---------------------------------------------------------

    if (
      this.esPositivo(
        this.formulario.otrosEgresos
      )
      && !this.formulario
        .comentarioOtrosEgresos
        .trim()
    ) {

      return 'Debe indicar un comentario para otros egresos.';
    }


    // ---------------------------------------------------------
    // Relación financiera
    // ---------------------------------------------------------

    if (
      this.esPositivo(
        this.formulario
          .deudaRelacionFinanciera
      )
      && !this.formulario
        .relacionFinanciera
        .trim()
    ) {

      return 'Debe indicar la relación financiera asociada a la deuda.';
    }


    // ---------------------------------------------------------
    // Balance
    // ---------------------------------------------------------

    if (
      this.valor(
        this.formulario.activoCorriente
      )
      >
      this.valor(
        this.formulario.totalActivos
      )
    ) {

      return 'El activo corriente no puede ser mayor que el total de activos.';
    }

    if (
      this.valor(
        this.formulario.pasivoCorriente
      )
      >
      this.valor(
        this.formulario.totalPasivos
      )
    ) {

      return 'El pasivo corriente no puede ser mayor que el total de pasivos.';
    }


    // ---------------------------------------------------------
    // Declaración de renta
    // ---------------------------------------------------------

    if (
      this.formulario.declaraRenta
      && !this.formulario.anioDeclaracion
    ) {

      return 'Debe indicar el año de la declaración de renta.';
    }

    if (
      this.formulario.declaraRenta
      && !this.formulario
        .fechaPresentacionDeclaracion
    ) {

      return 'Debe indicar la fecha de presentación de la declaración de renta.';
    }

    if (
      this.formulario.declaraRenta
      && this.formulario.anioDeclaracion
      && this.formulario.anioDeclaracion >
         new Date().getFullYear()
    ) {

      return 'El año de la declaración de renta no puede ser futuro.';
    }

    if (
      this.formulario.declaraRenta
      && this.formulario
        .fechaPresentacionDeclaracion
    ) {

      const fecha =
        new Date(
          `${this.formulario.fechaPresentacionDeclaracion}T00:00:00`
        );

      const hoy =
        new Date();

      hoy.setHours(
        0,
        0,
        0,
        0
      );

      if (
        fecha.getTime() >
        hoy.getTime()
      ) {

        return 'La fecha de presentación de la declaración de renta no puede ser futura.';
      }
    }

    return null;
  }


  // =========================================================
  // TOTALES PERSONA NATURAL
  // =========================================================

  get totalIngresosNatural(): number {

    return (
      this.valor(
        this.formulario.valorSalario
      )
      + this.valor(
        this.formulario.valorPension
      )
      + this.valor(
        this.formulario.ingresoIndependiente
      )
      + this.valor(
        this.formulario.ingresosArriendo
      )
      + this.valor(
        this.formulario.ingresosComisiones
      )
      + this.valor(
        this.formulario.otrosIngresos
      )
    );
  }


  get totalEgresosNatural(): number {

    return (
      this.valor(
        this.formulario.egresosFamiliares
      )
      + this.valor(
        this.formulario.egresosArriendo
      )
      + this.valor(
        this.formulario.egresosCredito
      )
      + this.valor(
        this.formulario.otrosEgresos
      )
    );
  }


  // =========================================================
  // TOTALES PERSONA JURÍDICA
  // =========================================================

  get totalIngresosJuridica(): number {

    return (
      this.valor(
        this.formulario
          .ingresosOperacionales
      )
      + this.valor(
        this.formulario
          .ingresosNoOperacionales
      )
    );
  }


  get totalEgresosJuridica(): number {

    return (
      this.valor(
        this.formulario.costos
      )
      + this.valor(
        this.formulario
          .gastosOperacionales
      )
      + this.valor(
        this.formulario
          .gastosFinancieros
      )
      + this.valor(
        this.formulario.otrosGastos
      )
    );
  }


  // =========================================================
  // BALANCE
  // =========================================================

  get patrimonio(): number {

    return (
      this.valor(
        this.formulario.totalActivos
      )
      - this.valor(
        this.formulario.totalPasivos
      )
    );
  }


  // =========================================================
  // TIPO PERSONA
  // =========================================================

  get esPersonaNatural(): boolean {

    return this.formulario
      .tipoPersona
      ?.trim() === '1';
  }


  get esPersonaJuridica(): boolean {

    return this.formulario
      .tipoPersona
      ?.trim() === '2';
  }


  // =========================================================
  // SINCRONIZACIÓN HOJA DE VIDA
  // =========================================================

  get sincronizadoHojaVida(): boolean {

    return this.detalleActual
      ?.sincronizadoHojaVida !== false;
  }


  get tieneRegistroFinanciero(): boolean {

    return !!this.detalleActual
      ?.idSolicitudDeudorFinanciero;
  }


  // =========================================================
  // ESTADO REGISTRO FINANCIERO
  // =========================================================

  tieneFotografia(
    financiero: SolicitudFinanciero
  ): boolean {

    return !!financiero
      .idSolicitudDeudorFinanciero;
  }


  // =========================================================
  // ACTUALIZAR RESUMEN LOCAL
  // =========================================================

  private actualizarResumenDespuesDeGuardar(
    detalle: SolicitudFinancieroDetalle
  ): void {

    const indice =
      this.financieros.findIndex(
        item =>
          item.idSolicitudDeudor ===
          detalle.idSolicitudDeudor
      );

    if (indice < 0) {
      return;
    }

    const actual =
      this.financieros[indice];

    const actualizado:
      SolicitudFinanciero = {

      ...actual,

      idSolicitudDeudorFinanciero:
        detalle.idSolicitudDeudorFinanciero,

      tipoPersona:
        detalle.tipoPersona,

      fechaFotografia:
        detalle.fechaFotografia,

      ingresosTotalesNatural:
        detalle.ingresosTotalesNatural,

      egresosTotalesNatural:
        detalle.egresosTotalesNatural,

      ingresosTotalesJuridica:
        detalle.ingresosTotalesJuridica,

      egresosTotalesJuridica:
        detalle.egresosTotalesJuridica,

      totalActivos:
        detalle.totalActivos,

      totalPasivos:
        detalle.totalPasivos,

      patrimonioTotal:
        detalle.patrimonioTotal,

      activo:
        detalle.activo
    };

    this.financieros[indice] =
      actualizado;

    this.financieros = [
      ...this.financieros
    ];

    this.deudorSeleccionado =
      actualizado;
  }


  // =========================================================
  // DESCARTAR / CERRAR EDICIÓN
  // =========================================================

  cerrarEdicion(): void {

    this.deudorSeleccionado = null;
    this.detalleActual = null;

    this.formulario =
      this.crearFormularioVacio();

    this.filtroSes = '';
    this.filtroDian = '';

    this.actividadesSesFiltradas = [
      ...this.actividadesSes
    ];

    this.actividadesDianFiltradas = [
      ...this.actividadesDian
    ];

    this.error = '';
    this.mensaje = '';
  }


  // =========================================================
  // ESTADO DEL PASO FINANCIERO
  // =========================================================

  get puedeContinuarCentralRiesgo(): boolean {

    return (
      this.financieros.length > 0
      && this.financieros.every(
        financiero =>
          !!financiero.idSolicitudDeudorFinanciero
      )
    );
  }


  get cantidadFinancierosPendientes(): number {

    return this.financieros.filter(
      financiero =>
        !financiero.idSolicitudDeudorFinanciero
    ).length;
  }


  // =========================================================
  // CONTINUAR A CENTRAL DE RIESGO
  // =========================================================

  continuarCentralRiesgo(): void {

    this.error = '';
    this.mensaje = '';

    if (!this.idSolicitudCredito) {

      this.error =
        'No se encontró una solicitud de crédito activa.';

      return;
    }

    if (this.financieros.length === 0) {

      this.error =
        'La solicitud no tiene deudores disponibles para continuar.';

      return;
    }

    if (!this.puedeContinuarCentralRiesgo) {

      this.error =
        this.cantidadFinancierosPendientes === 1
          ? 'Debe completar la información financiera del deudor pendiente antes de continuar a Central de riesgo.'
          : `Debe completar la información financiera de ${this.cantidadFinancierosPendientes} deudores antes de continuar a Central de riesgo.`;

      return;
    }

    this.router.navigate([
      '/cartera/originacion/central-riesgo'
    ]);
  }


  // =========================================================
  // VOLVER A BIENES
  // =========================================================

 volverBienes(): void {
   this.router.navigate([
     '/cartera/originacion/bienes'
   ]);
 }

  // =========================================================
  // FORMULARIO VACÍO
  // =========================================================

  private crearFormularioVacio():
    FormularioFinanciero {

    return {

      idSolicitudDeudor: null,
      tipoPersona: '',

      codigoOcupacion: '',
      codigoSectorEconomico: '',
      codigoActividadSes: '',
      codigoActividadDian: '',

      valorSalario: null,
      valorPension: null,
      ingresoIndependiente: null,
      ingresosArriendo: null,
      ingresosComisiones: null,
      otrosIngresos: null,

      comentarioOtrosIngresos: '',

      egresosFamiliares: null,
      egresosArriendo: null,
      egresosCredito: null,
      otrosEgresos: null,

      comentarioOtrosEgresos: '',

      declaraRenta: false,
      anioDeclaracion: null,
      fechaPresentacionDeclaracion: '',

      ingresosOperacionales: null,
      ingresosNoOperacionales: null,

      costos: null,
      gastosOperacionales: null,
      gastosFinancieros: null,
      otrosGastos: null,

      activoCorriente: null,
      pasivoCorriente: null,

      utilidadOperacional: null,
      utilidadNeta: null,

      totalActivos: null,
      totalPasivos: null,

      origenFondos: '',
      relacionFinanciera: '',
      deudaRelacionFinanciera: null
    };
  }


  // =========================================================
  // SOPORTE
  // =========================================================

  private valor(
    valor: number | null | undefined
  ): number {

    return Number(
      valor ?? 0
    );
  }


  private esPositivo(
    valor: number | null | undefined
  ): boolean {

    return this.valor(valor) > 0;
  }


  private normalizarTexto(
    valor: string | null | undefined
  ): string | null {

    const texto =
      valor?.trim() ?? '';

    return texto.length > 0
      ? texto
      : null;
  }


  private obtenerMensajeError(
    error: any,
    mensajeDefault: string
  ): string {

    if (
      typeof error?.error === 'string'
      && error.error.trim()
    ) {

      return error.error;
    }

    if (
      error?.error?.message
      && typeof error.error.message ===
        'string'
    ) {

      return error.error.message;
    }

    if (
      error?.message
      && typeof error.message === 'string'
    ) {

      return error.message;
    }

    return mensajeDefault;
  }
}
