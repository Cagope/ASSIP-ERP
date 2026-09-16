import {
  CommonModule,
  Location
} from '@angular/common';

import {
  Component,
  Input,
  OnInit
} from '@angular/core';

import {
  ActivatedRoute
} from '@angular/router';

import {
  finalize,
  forkJoin
} from 'rxjs';

import {
  SessionService
} from '../../../../core/auth/session.service';

import {
  OriginacionSolicitudApi
} from '../solicitud/originacion-solicitud.api';

import {
  OriginacionAsociado
} from '../solicitud/originacion-solicitud.models';

import {
  OriginacionContextoApi
} from './originacion-contexto.api';

import {
  OriginacionCartera,
  OriginacionCodeuda,
  OriginacionContexto,
  OriginacionVectorDetalle,
  OriginacionVectorResumen
} from './originacion-contexto.models';


type SeccionContexto =
  | 'ECONOMICA'
  | 'DEPOSITOS'
  | 'CARTERA'
  | 'VECTOR'
  | 'CODEUDAS';


@Component({
  selector:
    'app-originacion-contexto',

  standalone: true,

  imports: [
    CommonModule
  ],

  templateUrl:
    './originacion-contexto.component.html',

  styleUrls: [
    './originacion-contexto.component.scss'
  ]
})
export class OriginacionContextoComponent
  implements OnInit {

  // =========================================================
  // IDENTIFICACIÓN
  // =========================================================

  @Input()
  idDatosPersonalInput:
    number | null = null;

  @Input()
  idAgenciaInput:
    number | null = null;

  @Input()
  mostrarVolver = true;

  idDatosPersonal = 0;

  idAgencia = 0;

  agenciaActiva: any | null = null;


  // =========================================================
  // ASOCIADO
  // =========================================================

  asociado:
    OriginacionAsociado | null = null;


  // =========================================================
  // CONTEXTO
  // =========================================================

  contexto:
    OriginacionContexto | null = null;


  // =========================================================
  // NAVEGACIÓN
  // =========================================================

  seccionActiva:
    SeccionContexto = 'ECONOMICA';


  // =========================================================
  // VECTOR
  // =========================================================

  vectorDetalle:
    OriginacionVectorDetalle[] = [];

  cargandoVector = false;

  vectorCargado = false;

  errorVector = '';


  // =========================================================
  // HISTÓRICO CARTERA
  // =========================================================

  mostrarHistoricoCartera = false;

  carteraHistorica:
    OriginacionCartera[] = [];

  cargandoHistoricoCartera = false;

  historicoCarteraCargado = false;

  errorHistoricoCartera = '';


  // =========================================================
  // HISTÓRICO CODEUDAS
  // =========================================================

  mostrarHistoricoCodeudas = false;

  codeudasHistoricas:
    OriginacionCodeuda[] = [];

  cargandoHistoricoCodeudas = false;

  historicoCodeudasCargado = false;

  errorHistoricoCodeudas = '';


  // =========================================================
  // ESTADO GENERAL
  // =========================================================

  cargando = false;

  error = '';


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly route:
      ActivatedRoute,

    private readonly location:
      Location,

    private readonly sessionService:
      SessionService,

    private readonly contextoApi:
      OriginacionContextoApi,

    private readonly solicitudApi:
      OriginacionSolicitudApi
  ) {
  }


  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    // =========================================================
    // MODO EMBEBIDO
    // =========================================================

    const idDatosPersonalInput =
      Number(
        this.idDatosPersonalInput
      );

    const idAgenciaInput =
      Number(
        this.idAgenciaInput
      );

    if (
      Number.isInteger(idDatosPersonalInput)
      && idDatosPersonalInput > 0
      && Number.isInteger(idAgenciaInput)
      && idAgenciaInput > 0
    ) {

      this.idDatosPersonal =
        idDatosPersonalInput;

      this.idAgencia =
        idAgenciaInput;

      this.consultar();

      return;
    }


    // =========================================================
    // MODO PÁGINA
    // =========================================================

    const parametro =
      this.route.snapshot.paramMap.get(
        'idDatosPersonal'
      );

    const id =
      Number(parametro);

    if (
      !Number.isInteger(id)
      || id <= 0
    ) {

      this.error =
        'No se recibió un asociado válido.';

      return;
    }

    this.idDatosPersonal = id;

    this.agenciaActiva =
      this.sessionService.getAgenciaActiva();

    const idAgencia =
      Number(
        this.agenciaActiva?.idAgencia
      );

    if (
      !Number.isInteger(idAgencia)
      || idAgencia <= 0
    ) {

      this.error =
        'No existe una agencia activa válida.';

      return;
    }

    this.idAgencia = idAgencia;

    this.consultar();
  }

  // =========================================================
  // CONSULTA GENERAL
  // =========================================================

  consultar(): void {

    if (
      this.cargando ||
      this.idDatosPersonal <= 0 ||
      this.idAgencia <= 0
    ) {
      return;
    }

    this.cargando = true;

    this.error = '';

    forkJoin({

      asociado:
        this.solicitudApi
          .buscarAsociadoPorId(
            this.idDatosPersonal,
            this.idAgencia
          ),

      contexto:
        this.contextoApi
          .consultar(
            this.idDatosPersonal,
            this.idAgencia
          )

    })
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({

        next: respuesta => {

          this.asociado =
            respuesta.asociado;

          this.contexto =
            respuesta.contexto;

          this.reiniciarConsultasComplementarias();
        },

        error: error => {

          console.error(
            'Error consultando contexto de originación:',
            error
          );

          this.asociado = null;

          this.contexto = null;

          this.error =
            error?.error?.mensaje ??
            error?.error?.message ??
            'No fue posible consultar el contexto del asociado.';
        }
      });
  }


  // =========================================================
  // NAVEGACIÓN
  // =========================================================

  seleccionarSeccion(
    seccion: SeccionContexto
  ): void {

    this.seccionActiva = seccion;

    if (
      seccion === 'VECTOR' &&
      !this.vectorCargado &&
      !this.cargandoVector
    ) {

      this.cargarVectorDetalle();
    }
  }


  esSeccionActiva(
    seccion: SeccionContexto
  ): boolean {

    return (
      this.seccionActiva === seccion
    );
  }


  volver(): void {

    this.location.back();
  }


  // =========================================================
  // VECTOR
  // =========================================================

  cargarVectorDetalle(): void {

    if (
      this.idDatosPersonal <= 0 ||
      this.cargandoVector
    ) {
      return;
    }

    this.cargandoVector = true;

    this.errorVector = '';

    this.contextoApi
      .listarVectorDetalle(
        this.idDatosPersonal
      )
      .pipe(
        finalize(() => {
          this.cargandoVector = false;
        })
      )
      .subscribe({

        next: respuesta => {

          this.vectorDetalle =
            respuesta ?? [];

          this.vectorCargado = true;
        },

        error: error => {

          console.error(
            'Error consultando vector de comportamiento:',
            error
          );

          this.vectorDetalle = [];

          this.errorVector =
            error?.error?.mensaje ??
            error?.error?.message ??
            'No fue posible consultar el vector de comportamiento.';
        }
      });
  }


  detalleVectorCredito(
    idCarteraCredito: number
  ): OriginacionVectorDetalle[] {

    return this.vectorDetalle
      .filter(
        item =>
          item.idCarteraCredito ===
          idCarteraCredito
      )
      .sort(
        (a, b) =>
          (a.posicionVector ?? 0) -
          (b.posicionVector ?? 0)
      );
  }


  // =========================================================
  // HISTÓRICO CARTERA
  // =========================================================

  alternarHistoricoCartera(): void {

    this.mostrarHistoricoCartera =
      !this.mostrarHistoricoCartera;

    if (
      this.mostrarHistoricoCartera &&
      !this.historicoCarteraCargado &&
      !this.cargandoHistoricoCartera
    ) {

      this.cargarHistoricoCartera();
    }
  }


  cargarHistoricoCartera(): void {

    this.cargandoHistoricoCartera = true;

    this.errorHistoricoCartera = '';

    this.contextoApi
      .listarCarteraHistorica(
        this.idDatosPersonal,
        this.idAgencia
      )
      .pipe(
        finalize(() => {

          this.cargandoHistoricoCartera =
            false;
        })
      )
      .subscribe({

        next: respuesta => {

          this.carteraHistorica =
            respuesta ?? [];

          this.historicoCarteraCargado =
            true;
        },

        error: error => {

          console.error(
            'Error consultando cartera histórica:',
            error
          );

          this.carteraHistorica = [];

          this.errorHistoricoCartera =
            error?.error?.mensaje ??
            error?.error?.message ??
            'No fue posible consultar la cartera histórica.';
        }
      });
  }


  // =========================================================
  // HISTÓRICO CODEUDAS
  // =========================================================

  alternarHistoricoCodeudas(): void {

    this.mostrarHistoricoCodeudas =
      !this.mostrarHistoricoCodeudas;

    if (
      this.mostrarHistoricoCodeudas &&
      !this.historicoCodeudasCargado &&
      !this.cargandoHistoricoCodeudas
    ) {

      this.cargarHistoricoCodeudas();
    }
  }


  cargarHistoricoCodeudas(): void {

    this.cargandoHistoricoCodeudas = true;

    this.errorHistoricoCodeudas = '';

    this.contextoApi
      .listarCodeudasHistoricas(
        this.idDatosPersonal,
        this.idAgencia
      )
      .pipe(
        finalize(() => {

          this.cargandoHistoricoCodeudas =
            false;
        })
      )
      .subscribe({

        next: respuesta => {

          this.codeudasHistoricas =
            respuesta ?? [];

          this.historicoCodeudasCargado =
            true;
        },

        error: error => {

          console.error(
            'Error consultando codeudas históricas:',
            error
          );

          this.codeudasHistoricas = [];

          this.errorHistoricoCodeudas =
            error?.error?.mensaje ??
            error?.error?.message ??
            'No fue posible consultar las codeudas históricas.';
        }
      });
  }


  // =========================================================
  // PRESENTACIÓN
  // =========================================================

  claseMora(
    diasMora: number | null
  ): string {

    const dias =
      diasMora ?? 0;

    if (dias <= 0) {
      return 'mora--al-dia';
    }

    if (dias <= 30) {
      return 'mora--1-30';
    }

    if (dias <= 60) {
      return 'mora--31-60';
    }

    if (dias <= 90) {
      return 'mora--61-90';
    }

    if (dias <= 120) {
      return 'mora--91-120';
    }

    return 'mora--mayor-120';
  }


  textoMora(
    diasMora: number | null
  ): string {

    if (
      diasMora === null ||
      diasMora === undefined
    ) {
      return '—';
    }

    if (diasMora <= 0) {
      return 'Al día';
    }

    return `${diasMora} días`;
  }


  vectorResumenOrdenado():
    OriginacionVectorResumen[] {

    return [
      ...(
        this.contexto?.vectorResumen ??
        []
      )
    ].sort(
      (a, b) =>
        (b.saldoActualMaestro ?? 0) -
        (a.saldoActualMaestro ?? 0)
    );
  }


  private reiniciarConsultasComplementarias(): void {

    this.vectorDetalle = [];

    this.vectorCargado = false;

    this.errorVector = '';

    this.mostrarHistoricoCartera =
      false;

    this.carteraHistorica = [];

    this.historicoCarteraCargado =
      false;

    this.errorHistoricoCartera = '';

    this.mostrarHistoricoCodeudas =
      false;

    this.codeudasHistoricas = [];

    this.historicoCodeudasCargado =
      false;

    this.errorHistoricoCodeudas = '';
  }

}
