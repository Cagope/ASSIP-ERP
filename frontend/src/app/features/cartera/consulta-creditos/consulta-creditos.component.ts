import {
  Component
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  HeaderActionsComponent
} from '../../../shared/header-actions/header-actions.component';

import {
  ConsultaCreditosApi
} from './consulta-creditos.api';

import {
  ConsultaCreditoDetalle,
  ConsultaCreditoIntegral,
  ConsultaCreditoResumen,
  crearConsultaCreditoIntegralVacia,
} from './consulta-creditos.models';

import {
  ConsultaCreditosAsociado,
  ConsultaCreditosBuscadorComponent
} from './buscador/consulta-creditos-buscador.component';

import {
  ConsultaCreditosExtractoComponent
} from './extracto/consulta-creditos-extracto.component';

import {
  ConsultaCreditosSegurosComponent
} from './seguros/consulta-creditos-seguros.component';

import {
  ConsultaCreditosInteresesComponent
} from './intereses/consulta-creditos-intereses.component';

import {
  ConsultaCreditosEvaluacionesComponent
} from './evaluaciones/consulta-creditos-evaluaciones.component';

import {
  ConsultaCreditosProrrogasComponent
} from './prorrogas/consulta-creditos-prorrogas.component';

import {
  ConsultaCreditosResultadosMensualesComponent
} from './resultados-mensuales/consulta-creditos-resultados-mensuales.component';


type ModuloRelacionado =
  | 'extracto'
  | 'seguros'
  | 'alivios'
  | 'intereses'
  | 'evaluaciones'
  | 'prorrogas'
  | 'resultadosMensuales';

@Component({
  selector: 'app-consulta-creditos',
  standalone: true,
  imports: [
    CommonModule,
    HeaderActionsComponent,
    ConsultaCreditosBuscadorComponent,
    ConsultaCreditosExtractoComponent,
    ConsultaCreditosSegurosComponent,
    ConsultaCreditosInteresesComponent,
    ConsultaCreditosEvaluacionesComponent,
    ConsultaCreditosProrrogasComponent,
    ConsultaCreditosResultadosMensualesComponent
  ],

  templateUrl:
    './consulta-creditos.component.html',
  styleUrls: [
    './consulta-creditos.component.scss'
  ]
})
export class ConsultaCreditosComponent {

  // =========================================================
  // ASOCIADO SELECCIONADO
  // =========================================================

  asociadoSeleccionado:
    ConsultaCreditosAsociado | null = null;

  idDatosPersonal:
    number | null = null;


  // =========================================================
  // CRÉDITOS
  // =========================================================

  creditos:
    ConsultaCreditoResumen[] = [];

  creditoResumenSeleccionado:
    ConsultaCreditoResumen | null = null;

  creditoSeleccionado:
    ConsultaCreditoIntegral =
      crearConsultaCreditoIntegralVacia();


  // =========================================================
  // ESTADO
  // =========================================================

  cargando = false;

  cargandoDetalle = false;

  error:
    string | null = null;

  // =========================================================
  // MÓDULO RELACIONADO ABIERTO
  // =========================================================

  moduloRelacionadoActivo:
    ModuloRelacionado | null = null;

  // =========================================================
  // PAGINACIÓN
  // =========================================================

  pagina = 1;

  tamanoPagina = 10;

  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly api:
      ConsultaCreditosApi
  ) {}


  // =========================================================
  // SELECCIONAR ASOCIADO
  // =========================================================

  seleccionarAsociado(
    asociado:
      ConsultaCreditosAsociado
  ): void {

    if (
      !asociado
      || !asociado.idDatosPersonal
      || this.cargando
      || this.cargandoDetalle
    ) {
      return;
    }

    const idDatosPersonal =
      Number(
        asociado.idDatosPersonal
      );

    if (
      !Number.isInteger(
        idDatosPersonal
      )
      || idDatosPersonal <= 0
    ) {

      this.error =
        'El asociado seleccionado no tiene un identificador válido.';

      return;
    }

    this.asociadoSeleccionado =
      asociado;

    this.idDatosPersonal =
      idDatosPersonal;

    this.creditos = [];
    this.pagina = 1;

    this.creditoResumenSeleccionado =
      null;

    this.creditoSeleccionado =
      crearConsultaCreditoIntegralVacia();

    this.moduloRelacionadoActivo =
      null;

    this.error = null;

    this.cargarCreditosAsociado(
      idDatosPersonal
    );

  }


  // =========================================================
  // CARGAR CRÉDITOS DEL ASOCIADO
  // =========================================================

  private cargarCreditosAsociado(
    idDatosPersonal: number
  ): void {

    if (this.cargando) {
      return;
    }

    this.cargando = true;

    this.error = null;

    this.api
      .listarPorPersona(
        idDatosPersonal
      )
      .subscribe({

        next: (
          respuesta:
            ConsultaCreditoResumen[]
        ) => {

          this.creditos =
            Array.isArray(
              respuesta
            )
              ? respuesta
              : [];

          this.creditoResumenSeleccionado =
            null;

          this.creditoSeleccionado =
            crearConsultaCreditoIntegralVacia();

          this.cargando = false;

        },

        error: (
          error: unknown
        ) => {

          console.error(
            'Error consultando créditos del asociado:',
            error
          );

          this.creditos = [];

          this.creditoResumenSeleccionado =
            null;

          this.creditoSeleccionado =
            crearConsultaCreditoIntegralVacia();

          this.moduloRelacionadoActivo =
            null;

          this.moduloRelacionadoActivo =
            null;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar los créditos del asociado.'
            );

          this.cargando = false;

        }

      });

  }


  // =========================================================
  // SELECCIONAR CRÉDITO
  // =========================================================

  seleccionarCredito(
    credito:
      ConsultaCreditoResumen
  ): void {

    const idCarteraCredito =
      Number(
        credito?.idCarteraCredito
      );

    if (
      !Number.isInteger(
        idCarteraCredito
      )
      || idCarteraCredito <= 0
      || this.cargando
      || this.cargandoDetalle
    ) {
      return;
    }

    this.creditoResumenSeleccionado =
      credito;

    this.creditoSeleccionado =
      crearConsultaCreditoIntegralVacia();

    this.moduloRelacionadoActivo =
      null;

    this.error = null;

    this.cargandoDetalle = true;

    this.api
      .consultarIntegral(
        idCarteraCredito
      )
      .subscribe({

        next: (
          respuesta:
            ConsultaCreditoIntegral
        ) => {

          const estadoVacio =
            crearConsultaCreditoIntegralVacia();

          this.creditoSeleccionado = {
            ...estadoVacio,
            ...(respuesta ?? {}),
            extracto:
              respuesta?.extracto
              ?? [],
            seguros:
              respuesta?.seguros
              ?? [],
            alivios:
              respuesta?.alivios
              ?? [],
            interesesCausados:
              respuesta?.interesesCausados
              ?? [],
            evaluaciones:
              respuesta?.evaluaciones
              ?? [],
            prorrogas:
              respuesta?.prorrogas
              ?? [],
            resultadosMensuales:
              respuesta?.resultadosMensuales
              ?? [],
            ultimaEvaluacion:
              respuesta?.ultimaEvaluacion
              ?? null
          };

          this.cargandoDetalle = false;

        },

        error: (
          error: unknown
        ) => {

          console.error(
            'Error consultando el detalle integral del crédito:',
            error
          );

          this.creditoSeleccionado =
            crearConsultaCreditoIntegralVacia();

          this.moduloRelacionadoActivo =
            null;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar el detalle integral del crédito.'
            );

          this.cargandoDetalle = false;

        }

      });

  }


  // =========================================================
  // LIMPIAR BÚSQUEDA
  // =========================================================

  limpiarBusqueda(): void {

    if (
      this.cargando
      || this.cargandoDetalle
    ) {
      return;
    }

    this.asociadoSeleccionado =
      null;

    this.idDatosPersonal =
      null;

    this.creditos = [];
    this.pagina = 1;

    this.creditoResumenSeleccionado =
      null;

    this.creditoSeleccionado =
      crearConsultaCreditoIntegralVacia();

    this.moduloRelacionadoActivo =
      null;

    this.error = null;

  }


  // =========================================================
  // LIMPIAR CRÉDITO SELECCIONADO
  // =========================================================

  limpiarCreditoSeleccionado(): void {

    if (this.cargandoDetalle) {
      return;
    }

    this.creditoResumenSeleccionado =
      null;

    this.creditoSeleccionado =
      crearConsultaCreditoIntegralVacia();

    this.moduloRelacionadoActivo =
      null;

    this.error = null;

  }

  // =========================================================
  // INFORMACIÓN RELACIONADA
  // =========================================================

  abrirModuloRelacionado(
    modulo: ModuloRelacionado
  ): void {

    this.moduloRelacionadoActivo =
      this.moduloRelacionadoActivo === modulo
        ? null
        : modulo;

  }

  cerrarModuloRelacionado(): void {

    this.moduloRelacionadoActivo =
      null;

  }

  moduloRelacionadoAbierto(
    modulo: ModuloRelacionado
  ): boolean {

    return (
      this.moduloRelacionadoActivo
      === modulo
    );

  }

  // =========================================================
  // INDICADORES
  // =========================================================

  get tieneAsociadoSeleccionado(): boolean {

    return (
      this.asociadoSeleccionado !== null
      && this.idDatosPersonal !== null
    );

  }

  get tieneCreditos(): boolean {

    return this.creditos.length > 0;

  }

  get tieneDetalle(): boolean {

    return (
      this.creditoSeleccionado.credito
      !== null
    );

  }

  get cantidadCreditos(): number {

    return this.creditos.length;

  }

  get cantidadCreditosConSaldo(): number {

    return this.creditos.filter(
      credito =>
        credito.creditoSaldado !== true
        && Number(
          credito.saldoActual ?? 0
        ) > 0
    ).length;

  }

  get cantidadCreditosSaldados(): number {

    return this.creditos.filter(
      credito =>
        credito.creditoSaldado === true
        || Number(
          credito.saldoActual ?? 0
        ) <= 0
    ).length;

  }

  get cantidadCreditosEnMora(): number {

    return this.creditos.filter(
      credito =>
        credito.creditoEnMora === true
    ).length;

  }


  // =========================================================
  // SELECCIÓN VISUAL
  // =========================================================

  esCreditoSeleccionado(
    credito:
      ConsultaCreditoResumen
  ): boolean {

    return (
      this.creditoResumenSeleccionado
        ?.idCarteraCredito
      === credito.idCarteraCredito
    );

  }


  // =========================================================
  // TRACK BY
  // =========================================================

  trackByCredito(
    indice: number,
    credito:
      ConsultaCreditoResumen
  ): number {

    return (
      credito.idCarteraCredito
      ?? indice
    );

  }


  // =========================================================
  // UTILIDADES
  // =========================================================

  private obtenerMensajeError(
    error: unknown,
    mensajePredeterminado: string
  ): string {

    if (
      !error
      || typeof error !== 'object'
    ) {
      return mensajePredeterminado;
    }

    const respuesta =
      error as {
        message?: string;
        error?: {
          mensaje?: string;
          message?: string;
        };
      };

    return (
      respuesta.error?.mensaje
      ?? respuesta.error?.message
      ?? respuesta.message
      ?? mensajePredeterminado
    );

  }

  get creditosPaginados(): ConsultaCreditoResumen[] {

    const inicio =
      (this.pagina - 1)
      * this.tamanoPagina;

    return this.creditos.slice(
      inicio,
      inicio + this.tamanoPagina
    );

  }

  get totalPaginas(): number {

    return Math.max(
      1,
      Math.ceil(
        this.creditos.length
        / this.tamanoPagina
      )
    );

  }

  cambiarPagina(
    pagina: number
  ): void {

    if (
      pagina < 1
      || pagina > this.totalPaginas
    ) {
      return;
    }

    this.pagina = pagina;

  }

  cuotasPactadas(
    credito: ConsultaCreditoDetalle
  ): number {

    const plazo =
      Number(
        credito.plazo ?? 0
      );

    const amortizacionCapital =
      Number(
        credito.amortizacionCapital ?? 0
      );

    if (
      plazo <= 0
      || amortizacionCapital <= 0
    ) {
      return 0;
    }

    return Math.ceil(
      plazo / amortizacionCapital
    );

  }

}
