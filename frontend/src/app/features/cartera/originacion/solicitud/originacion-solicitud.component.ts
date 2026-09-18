import {
  Component,
  OnInit,
  ViewChild
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormsModule
} from '@angular/forms';

import {
  forkJoin
} from 'rxjs';

import {
  HeaderActionsComponent
} from '../../../../shared/header-actions/header-actions.component';

import {
  TablaAmortizacionComponent
} from '../../../../shared/financiero/tabla-amortizacion/tabla-amortizacion.component';

import {
  TablaAmortizacionRequest
} from '../../../../shared/financiero/tabla-amortizacion/tabla-amortizacion.models';

import {
  SessionService
} from '../../../../core/auth/session.service';

import {
  OriginacionSolicitudApi
} from './originacion-solicitud.api';

import {
  OriginacionSolicitudStateService
} from './originacion-solicitud-state.service';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  OriginacionContextoApi
} from '../contexto/originacion-contexto.api';

import {
  OriginacionContexto
} from '../contexto/originacion-contexto.models';


import {
  NumericFormatDirective
} from '../../../../shared/utils/numeric-format.directive';

import {
  ClasificacionCredito,
  DestinoEconomico,
  FondoGarantia,
  FormaPago,
  GarantiaCredito,
  LineaCredito,
  ModalidadInteres,
  OriginacionAsociado,
  SolicitudCreditoCrearRequest,
  SolicitudCreditoDetalle,
  SolicitudCreditoGuardarRequest,
  SolicitudCreditoGuardarResponse,
  SolicitudCreditoResumen,
  SolicitudEnteAprobadorPreview,
  SubgarantiaCredito,
  TipoCuota
} from './originacion-solicitud.models';

interface FormularioCredito {

  idLineaCredito: number | null;

  codigoClasificacionCredito: string;

  codigoDestinoEconomico: string;

  codigoGarantiaCredito: string;

  codigoSubgarantia: string;

  idFondoGarantia: number | null;

  codigoFormaPago: string;

  amortizacionCapital: number | null;

  codigoTipoCuota: string;

  plazoSolicitado: number | null;

  mesesGraciaCapital: number;

  mesesGraciaInteres: number;

  valorSolicitado: number | null;

  idEmpresaLibranza: number | null;

  observacionAsesor: string;
}


@Component({
  selector: 'app-originacion-solicitud',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    TablaAmortizacionComponent,
    NumericFormatDirective
  ],

  templateUrl:
    './originacion-solicitud.component.html',

  styleUrls: [
    './originacion-solicitud.component.scss'
  ]
})
export class OriginacionSolicitudComponent
  implements OnInit {

  @ViewChild('tablaAmortizacion')
  tablaAmortizacion?: TablaAmortizacionComponent;

  // =========================================================
  // AGENCIA
  // =========================================================

  agenciaActiva: any | null = null;


  // =========================================================
  // BÚSQUEDA
  // =========================================================

  filtrosAsociado = {
    documento: '',
    nombres: '',
    primerApellido: '',
    segundoApellido: ''
  };

  buscandoAsociados = false;

  busquedaRealizada = false;

  asociados:
    OriginacionAsociado[] = [];

  asociadoSeleccionado:
    OriginacionAsociado | null = null;


  // =========================================================
  // SOLICITUD
  // =========================================================

  solicitudExistente:
    SolicitudCreditoResumen | null = null;

  solicitud:
    SolicitudCreditoDetalle | null = null;

  cargandoSolicitud = false;

  guardandoCredito = false;


  // =========================================================
  // CATÁLOGOS
  // =========================================================

  cargandoCatalogos = false;

  lineasCredito:
    LineaCredito[] = [];

  clasificaciones:
    ClasificacionCredito[] = [];

  destinos:
    DestinoEconomico[] = [];

  garantias:
    GarantiaCredito[] = [];

  subgarantias:
    SubgarantiaCredito[] = [];

  fondosGarantias:
    FondoGarantia[] = [];

  formasPago:
    FormaPago[] = [];

  modalidades:
    ModalidadInteres[] = [];

  tiposCuota:
    TipoCuota[] = [];


  // =========================================================
  // FORMULARIO
  // =========================================================

  modalidadSeleccionada = '';

  formulario:
    FormularioCredito =
      this.crearFormularioVacio();


  // =========================================================
  // RESULTADO DE GUARDADO
  // =========================================================

  resultadoGuardado:
    SolicitudCreditoGuardarResponse | null = null;

  // =========================================================
  // PREVISUALIZACIÓN ENTE APROBADOR
  // =========================================================

  enteAprobadorPreview:
    SolicitudEnteAprobadorPreview | null = null;

    consultandoEnteAprobador = false;

  // =========================================================
  // CONTEXTO RESUMIDO DEL ASOCIADO
  // =========================================================

  contextoAsociado:
    OriginacionContexto | null = null;

  cargandoContextoAsociado = false;

  reciprocidadSimulada = 50;

  // =========================================================
  // FINALIZAR SOLICITUD
  // =========================================================

  mostrandoFinalizacion = false;

  finalizandoSolicitud = false;

  idResultadoFinalizacion: number | null = null;

  observacionFinal = '';

  // =========================================================
  // MENSAJES
  // =========================================================

  error = '';
  mensaje = '';

  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly api:
      OriginacionSolicitudApi,

    private readonly session:
      SessionService,

    private readonly router: Router,
    private readonly route: ActivatedRoute,

    private readonly stateService:
      OriginacionSolicitudStateService,

    private readonly contextoApi:
      OriginacionContextoApi

  ) {}


  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {

    this.agenciaActiva =
      this.session.getAgenciaActiva();

    const idSolicitudCredito =
      Number(
        this.route.snapshot.queryParamMap.get(
          'idSolicitudCredito'
        )
      );

    const idDatosPersonal =
      Number(
        this.route.snapshot.queryParamMap.get(
          'idDatosPersonal'
        )
      );

    const idAgencia =
      Number(
        this.route.snapshot.queryParamMap.get(
          'idAgencia'
        )
      );

    const vieneDesdeBandeja =
      idSolicitudCredito > 0
      && idDatosPersonal > 0
      && idAgencia > 0;

    if (vieneDesdeBandeja) {

      this.stateService.limpiar();

      this.cargarAsociadoDesdeBandeja(
        idSolicitudCredito,
        idDatosPersonal,
        idAgencia
      );

    } else {

      this.restaurarEstadoNavegacion();
    }

    this.cargarCatalogos();

  }

  private cargarAsociadoDesdeBandeja(
    idSolicitudCredito: number,
    idDatosPersonal: number,
    idAgencia: number
  ): void {

    this.cargandoSolicitud = true;
    this.error = '';
    this.mensaje = '';

    this.api
      .buscarAsociadoPorId(
        idDatosPersonal,
        idAgencia
      )
      .subscribe({

        next: asociado => {

          this.asociadoSeleccionado =
            asociado;

          this.api
            .crearRetomar({
              idSolicitudCredito,
              idAgencia,
              idDatosPersonal
            })
            .subscribe({

              next: respuesta => {

                this.mensaje =
                  `Solicitud ${respuesta.numeroSolicitud} retomada correctamente.`;

                this.cargarDetalleSolicitud(
                  respuesta.idSolicitudCredito
                );

              },

              error: error => {

                this.cargandoSolicitud =
                  false;

                this.error =
                  this.obtenerMensajeError(
                    error,
                    'No fue posible retomar la solicitud.'
                  );

              }

            });

        },

        error: error => {

          this.cargandoSolicitud =
            false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible cargar el asociado de la solicitud.'
            );

        }

      });

  }

  // =========================================================
  // CATÁLOGOS
  // =========================================================

  private cargarCatalogos(): void {

    this.cargandoCatalogos = true;

    this.error = '';

    forkJoin({

      lineas:
        this.api.listarLineasCredito(),

      clasificaciones:
        this.api.listarClasificacionesCredito(),

      destinos:
        this.api.listarDestinosEconomicos(),

      garantias:
        this.api.listarGarantias(),

      subgarantias:
        this.api.listarSubgarantias(),

      fondosGarantias:
        this.api.listarFondosGarantias(),

      formasPago:
        this.api.listarFormasPago(),

      modalidades:
        this.api.listarModalidadesInteres(),

      tiposCuota:
        this.api.listarTiposCuota()

    }).subscribe({

      next: respuesta => {

        this.lineasCredito =
          respuesta.lineas ?? [];

        this.clasificaciones =
          respuesta.clasificaciones ?? [];

        this.destinos =
          respuesta.destinos ?? [];

        this.garantias =
          respuesta.garantias ?? [];

        this.subgarantias =
          respuesta.subgarantias ?? [];

        this.fondosGarantias =
          respuesta.fondosGarantias ?? [];

        this.formasPago =
          respuesta.formasPago ?? [];

        this.modalidades =
          respuesta.modalidades ?? [];

        this.tiposCuota =
          respuesta.tiposCuota ?? [];

        this.cargandoCatalogos = false;

      },

      error: error => {

        this.cargandoCatalogos = false;

        this.error =
          this.obtenerMensajeError(
            error,
            'No fue posible cargar los catálogos de originación.'
          );

      }

    });

  }

  // =========================================================
  // BUSCAR ASOCIADOS
  // =========================================================

  buscarAsociados(): void {

    if (
      this.buscandoAsociados
      || this.cargandoSolicitud
    ) {
      return;
    }

    // =======================================================
    // VALIDAR AGENCIA ACTIVA
    // =======================================================

    const idAgencia =
      Number(
        this.agenciaActiva?.idAgencia
      );

    if (
      !Number.isInteger(idAgencia)
      || idAgencia <= 0
    ) {

      this.error =
        'No se pudo identificar la agencia activa.';

      return;
    }

    // =======================================================
    // NORMALIZAR FILTROS
    // =======================================================

    const documento =
      this.normalizarTexto(
        this.filtrosAsociado.documento
      );

    const nombres =
      this.normalizarTexto(
        this.filtrosAsociado.nombres
      );

    const primerApellido =
      this.normalizarTexto(
        this.filtrosAsociado.primerApellido
      );

    const segundoApellido =
      this.normalizarTexto(
        this.filtrosAsociado.segundoApellido
      );

    // =======================================================
    // VALIDAR CRITERIOS
    // =======================================================

    if (
      !documento
      && !nombres
      && !primerApellido
      && !segundoApellido
    ) {

      this.error =
        'Ingrese al menos un criterio de búsqueda.';

      return;
    }

    // =======================================================
    // CONSERVAR FILTROS NORMALIZADOS
    // =======================================================

    this.filtrosAsociado = {
      documento,
      nombres,
      primerApellido,
      segundoApellido
    };

    this.error = '';
    this.mensaje = '';

    this.asociados = [];

    this.busquedaRealizada = true;

    this.buscandoAsociados = true;

    // =======================================================
    // CONSULTAR
    // =======================================================

    this.api
      .buscarAsociados(
        idAgencia,
        documento,
        nombres,
        primerApellido,
        segundoApellido
      )
      .subscribe({

        next: respuesta => {

          this.asociados =
            Array.isArray(respuesta)
              ? respuesta
              : [];

          this.buscandoAsociados =
            false;
        },

        error: error => {

          this.asociados = [];

          this.buscandoAsociados =
            false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible consultar los asociados.'
            );
        }

      });
  }

  // =========================================================
  // SELECCIONAR ASOCIADO
  // =========================================================

  seleccionarAsociado(
    asociado:
      OriginacionAsociado
  ): void {

    if (
      !asociado
      || !asociado.idDatosPersonal
    ) {
      return;
    }

    this.asociadoSeleccionado =
      asociado;

    this.solicitudExistente =
      null;

    this.solicitud =
      null;

    this.resultadoGuardado =
      null;

    this.enteAprobadorPreview =
      null;

    this.formulario =
      this.crearFormularioVacio();

    this.modalidadSeleccionada =
      '';

    this.error = '';
    this.mensaje = '';

    this.cargarContextoResumido();

    this.buscarSolicitudActiva();

  }

  // =========================================================
  // VER CONTEXTO DEL ASOCIADO
  // =========================================================

  verContextoAsociado(): void {

    const idDatosPersonal =
      Number(
        this.asociadoSeleccionado
          ?.idDatosPersonal
      );

    if (
      !Number.isInteger(idDatosPersonal)
      || idDatosPersonal <= 0
    ) {
      return;
    }

    this.guardarEstadoNavegacion();

    this.router.navigate([
      '/cartera/originacion/contexto',
      idDatosPersonal
    ]);

  }

  // =========================================================
  // DEUDORES
  // =========================================================

  irADeudores(): void {

    if (!this.solicitud) {

      this.error =
        'Debe existir una solicitud antes de consultar los deudores.';

      return;
    }

    this.guardarEstadoNavegacion();

    this.router.navigate([
      '/cartera/originacion/deudores'
    ]);
  }


  // =========================================================
  // BUSCAR SOLICITUD ACTIVA DEL ASOCIADO
  // =========================================================

  private buscarSolicitudActiva(): void {

    if (
      !this.asociadoSeleccionado
      || !this.agenciaActiva
    ) {
      return;
    }

    const idDatosPersonal =
      Number(
        this.asociadoSeleccionado
          .idDatosPersonal
      );

    const idAgencia =
      Number(
        this.agenciaActiva.idAgencia
      );

    this.cargandoSolicitud = true;

    this.api
      .listarSolicitudes()
      .subscribe({

        next: solicitudes => {

          const candidatas =
            (solicitudes ?? [])
              .filter(
                solicitud =>
                  solicitud.idDatosPersonal
                    === idDatosPersonal
                  && solicitud.idAgencia
                    === idAgencia
                  && solicitud.activo !== false
                  && solicitud.resultadoFinal !== true
              )
              .sort(
                (
                  a,
                  b
                ) =>
                  this.fechaEnMilisegundos(
                    b.fechaUltimaGestion
                  )
                  -
                  this.fechaEnMilisegundos(
                    a.fechaUltimaGestion
                  )
              );

          this.solicitudExistente =
            candidatas.length > 0
              ? candidatas[0]
              : null;

          this.cargandoSolicitud =
            false;

        },

        error: error => {

          this.cargandoSolicitud =
            false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible verificar las solicitudes activas del asociado.'
            );

        }

      });

  }


  // =========================================================
  // RETOMAR SOLICITUD EXISTENTE
  // =========================================================

  crearRetomarSolicitud(): void {

    if (
      !this.asociadoSeleccionado
      || !this.agenciaActiva
      || !this.solicitudExistente
      || this.cargandoSolicitud
    ) {
      return;
    }

    const idAgencia =
      Number(
        this.agenciaActiva.idAgencia
      );

    if (
      !Number.isInteger(idAgencia)
      || idAgencia <= 0
    ) {

      this.error =
        'No se pudo identificar la agencia activa.';

      return;
    }

    this.error = '';
    this.mensaje = '';

    this.cargandoSolicitud = true;

    this.api
      .crearRetomar({

        idSolicitudCredito:
          this.solicitudExistente
            .idSolicitudCredito,

        idAgencia,

        idDatosPersonal:
          this.asociadoSeleccionado
            .idDatosPersonal

      })
      .subscribe({

        next: respuesta => {

          this.mensaje =
            `Solicitud ${respuesta.numeroSolicitud} retomada correctamente.`;

          this.cargarDetalleSolicitud(
            respuesta.idSolicitudCredito
          );

        },

        error: error => {

          this.cargandoSolicitud =
            false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible retomar la solicitud.'
            );
        }

      });
  }


  // =========================================================
  // DETALLE SOLICITUD
  // =========================================================

  private cargarDetalleSolicitud(
    idSolicitudCredito: number
  ): void {

    this.api
      .buscarSolicitudPorId(
        idSolicitudCredito
      )
      .subscribe({

        next: detalle => {

          this.solicitud =
            detalle;

          this.solicitudExistente =
            null;

          this.cargarFormularioDesdeDetalle(
            detalle
          );

          this.cargandoSolicitud =
            false;

        },

        error: error => {

          this.solicitud =
            null;

          this.cargandoSolicitud =
            false;

          this.error =
            this.obtenerMensajeError(
              error,
              'La solicitud fue procesada, pero no fue posible consultar su detalle.'
            );

        }

      });

  }


  // =========================================================
  // FORMULARIO DESDE DETALLE
  // =========================================================

  private cargarFormularioDesdeDetalle(
    detalle:
      SolicitudCreditoDetalle
  ): void {

    this.formulario = {

      idLineaCredito:
        detalle.idLineaCredito,

      codigoClasificacionCredito:
        detalle.codigoClasificacionCredito
        ?? '',

      codigoDestinoEconomico:
        detalle.codigoDestinoEconomico
        ?? '',

      codigoGarantiaCredito:
        detalle.codigoGarantiaCredito
        ?? '',

      codigoSubgarantia:
        detalle.codigoSubgarantia
        ?? '',

      idFondoGarantia:
        detalle.idFondoGarantia,

      codigoFormaPago:
        detalle.codigoFormaPago
        ?? '',

      amortizacionCapital:
        detalle.amortizacionCapital,

      codigoTipoCuota:
        detalle.codigoTipoCuota
        ?? '',

      plazoSolicitado:
        detalle.plazoSolicitado,

      mesesGraciaCapital:
        detalle.mesesGraciaCapital
        ?? 0,

      mesesGraciaInteres:
        detalle.mesesGraciaInteres
        ?? 0,

      valorSolicitado:
        detalle.valorSolicitado,

      idEmpresaLibranza:
        detalle.idEmpresaLibranza,

      observacionAsesor:
        detalle.observacionAsesor
        ?? ''

    };

    if (
      detalle.periodoCodigoInteres
      && detalle.tipoModalidadInteres
    ) {

      this.modalidadSeleccionada =
        this.crearClaveModalidad(
          detalle.periodoCodigoInteres,
          detalle.tipoModalidadInteres
        );

    } else {

      this.modalidadSeleccionada =
        '';

    }

  }


  // =========================================================
  // CREAR SOLICITUD / GUARDAR CRÉDITO
  // =========================================================

  guardarCredito(): void {

    if (
      !this.asociadoSeleccionado
      || this.guardandoCredito
    ) {
      return;
    }

    if (
      !this.solicitud
      && this.solicitudExistente
    ) {

      this.error =
        'El asociado tiene una solicitud activa. Retómela antes de continuar.';

      return;
    }

    const validacion =
      this.validarFormulario();

    if (validacion) {

      this.error =
        validacion;

      return;
    }

    const modalidad =
      this.obtenerModalidadSeleccionada();

    if (!modalidad) {

      this.error =
        'Seleccione la modalidad de interés.';

      return;
    }

    const datosCredito = {

      idLineaCredito:
        Number(
          this.formulario.idLineaCredito
        ),

      codigoClasificacionCredito:
        this.formulario.codigoClasificacionCredito,

      codigoDestinoEconomico:
        this.formulario.codigoDestinoEconomico,

      codigoGarantiaCredito:
        this.formulario.codigoGarantiaCredito,

      codigoSubgarantia:
        this.formulario.codigoSubgarantia
        || null,

      idFondoGarantia:
        Number(
          this.formulario.idFondoGarantia
        ),

      codigoFormaPago:
        this.formulario.codigoFormaPago,

      periodoCodigoInteres:
        modalidad.periodoCodigo,

      tipoModalidadInteres:
        modalidad.tipoModalidad,

      amortizacionCapital:
        Number(
          this.formulario.amortizacionCapital
        ),

      codigoTipoCuota:
        this.formulario.codigoTipoCuota,

      plazoSolicitado:
        Number(
          this.formulario.plazoSolicitado
        ),

      mesesGraciaCapital: 0,

      mesesGraciaInteres: 0,

      valorSolicitado:
        Number(
          this.formulario.valorSolicitado
        ),

      idEmpresaLibranza:
        this.formulario.idEmpresaLibranza,

      observacionAsesor:
        this.normalizarTexto(
          this.formulario.observacionAsesor
        )
        || null
    };

    this.error = '';
    this.mensaje = '';

    this.resultadoGuardado =
      null;

    this.guardandoCredito =
      true;


    // =======================================================
    // SOLICITUD YA EXISTENTE -> SOLO ACTUALIZAR
    // =======================================================

    if (this.solicitud) {

      const request:
        SolicitudCreditoGuardarRequest = {

          idSolicitudCredito:
            this.solicitud.idSolicitudCredito,

          ...datosCredito
        };

      this.api
        .guardarCredito(
          request
        )
        .subscribe({

          next: respuesta => {

            this.resultadoGuardado =
              respuesta;

            this.mensaje =
              'Los datos del crédito se guardaron correctamente.';

            this.guardandoCredito =
              false;

            this.cargarDetalleSolicitud(
              respuesta.idSolicitudCredito
            );
          },

          error: error => {

            this.guardandoCredito =
              false;

            this.error =
              this.obtenerMensajeError(
                error,
                'No fue posible guardar los datos del crédito.'
              );
          }

        });

      return;
    }


    // =======================================================
    // NUEVA SOLICITUD -> CREAR + GUARDAR CONDICIONES
    // =======================================================

    const idAgencia =
      Number(
        this.agenciaActiva?.idAgencia
      );

    if (
      !Number.isInteger(idAgencia)
      || idAgencia <= 0
    ) {

      this.guardandoCredito =
        false;

      this.error =
        'No se pudo identificar la agencia activa.';

      return;
    }

    const request:
      SolicitudCreditoCrearRequest = {

        idAgencia,

        idDatosPersonal:
          this.asociadoSeleccionado
            .idDatosPersonal,

        ...datosCredito
      };

    this.api
      .crearSolicitud(
        request
      )
      .subscribe({

        next: respuesta => {

          this.resultadoGuardado =
            respuesta;

          this.mensaje =
            `Solicitud ${respuesta.numeroSolicitud} creada correctamente.`;

          this.guardandoCredito =
            false;

          this.cargarDetalleSolicitud(
            respuesta.idSolicitudCredito
          );
        },

        error: error => {

          this.guardandoCredito =
            false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible crear la solicitud.'
            );
        }

      });
  }

  // =========================================================
  // PREVISUALIZAR ENTE APROBADOR
  // =========================================================

  previsualizarEnteAprobador(): void {

    const idAgencia =
      Number(
        this.agenciaActiva?.idAgencia
      );

    const idDatosPersonal =
      Number(
        this.asociadoSeleccionado?.idDatosPersonal
      );

    const codigoGarantiaCredito =
      this.normalizarTexto(
        this.formulario.codigoGarantiaCredito
      );

    const plazoSolicitado =
      Number(
        this.formulario.plazoSolicitado
      );

    const valorSolicitado =
      Number(
        this.formulario.valorSolicitado
      );

    if (
      !Number.isInteger(idAgencia)
      || idAgencia <= 0
      || !Number.isInteger(idDatosPersonal)
      || idDatosPersonal <= 0
      || !codigoGarantiaCredito
      || !Number.isInteger(plazoSolicitado)
      || plazoSolicitado <= 0
      || !Number.isFinite(valorSolicitado)
      || valorSolicitado <= 0
    ) {

      this.enteAprobadorPreview = null;

      return;
    }

    this.consultandoEnteAprobador = true;

    this.api
      .previsualizarEnteAprobador({

        idAgencia,

        idDatosPersonal,

        codigoGarantiaCredito,

        plazoSolicitado,

        valorSolicitado

      })
      .subscribe({

        next: respuesta => {

          this.enteAprobadorPreview =
            respuesta;

          this.consultandoEnteAprobador =
            false;
        },

        error: () => {

          this.enteAprobadorPreview =
            null;

          this.consultandoEnteAprobador =
            false;
        }

      });
  }

  // =========================================================
  // VALIDAR FORMULARIO
  // =========================================================

  private validarFormulario():
    string | null {

    if (
      !this.formulario
        .idLineaCredito
    ) {
      return 'Seleccione la línea de crédito.';
    }

    if (
      !this.formulario
        .codigoClasificacionCredito
    ) {
      return 'Seleccione la clasificación del crédito.';
    }

    if (
      !this.formulario
        .codigoDestinoEconomico
    ) {
      return 'Seleccione el destino económico.';
    }

    if (
      !this.formulario
        .codigoGarantiaCredito
    ) {
      return 'Seleccione la garantía.';
    }

    if (
      !Number.isInteger(
        Number(
          this.formulario.idFondoGarantia
        )
      )
      || Number(
        this.formulario.idFondoGarantia
      ) <= 0
    ) {
      return 'Seleccione el fondo de garantías.';
    }

    if (
      !this.formulario
        .codigoFormaPago
    ) {
      return 'Seleccione la forma de pago.';
    }

    if (!this.modalidadSeleccionada) {
      return 'Seleccione la modalidad de interés.';
    }

    if (
      !Number.isInteger(
        Number(
          this.formulario
            .amortizacionCapital
        )
      )
      || Number(
        this.formulario
          .amortizacionCapital
      ) <= 0
    ) {
      return 'La amortización de capital debe ser mayor que cero.';
    }

    if (
      !this.formulario
        .codigoTipoCuota
    ) {
      return 'Seleccione el tipo de cuota.';
    }

    if (
      !Number.isInteger(
        Number(
          this.formulario
            .plazoSolicitado
        )
      )
      || Number(
        this.formulario
          .plazoSolicitado
      ) <= 0
    ) {
      return 'El plazo solicitado debe ser mayor que cero.';
    }

    if (
      Number(
        this.formulario
          .mesesGraciaCapital
      ) < 0
    ) {
      return 'Los meses de gracia de capital no pueden ser negativos.';
    }

    if (
      Number(
        this.formulario
          .mesesGraciaInteres
      ) < 0
    ) {
      return 'Los meses de gracia de interés no pueden ser negativos.';
    }

    if (
      !Number.isFinite(
        Number(
          this.formulario
            .valorSolicitado
        )
      )
      || Number(
        this.formulario
          .valorSolicitado
      ) <= 0
    ) {
      return 'El valor solicitado debe ser mayor que cero.';
    }

    return null;

  }


  // =========================================================
  // NUEVA OPERACIÓN
  // =========================================================

  nuevaOperacion(): void {

    if (
      this.cargandoSolicitud
      || this.guardandoCredito
    ) {
      return;
    }

    this.stateService.limpiar();

    this.filtrosAsociado = {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: ''
    };

    this.asociados = [];

    this.asociadoSeleccionado =
      null;

    this.solicitudExistente =
      null;

    this.solicitud =
      null;

    this.resultadoGuardado =
      null;

    this.enteAprobadorPreview =
      null;

    this.contextoAsociado = null;

    this.reciprocidadSimulada = 50;

    this.formulario =
      this.crearFormularioVacio();

    this.modalidadSeleccionada =
      '';

    this.busquedaRealizada =
      false;

    this.error = '';

    this.mensaje = '';

  }

  // =========================================================
  // BUSCAR OTRO ASOCIADO
  // =========================================================

  buscarOtroAsociado(): void {

    if (
      this.cargandoSolicitud
      || this.guardandoCredito
    ) {
      return;
    }

    this.nuevaOperacion();

    setTimeout(
      () =>
        window.scrollTo({
          top: 0,
          behavior: 'smooth'
        }),
      0
    );
  }

  // =========================================================
  // FINALIZAR SOLICITUD
  // =========================================================

  abrirFinalizacion(): void {

    if (
      !this.solicitud
      || this.solicitud.resultadoFinal === true
      || this.solicitud.activo === false
      || this.finalizandoSolicitud
    ) {
      return;
    }

    this.idResultadoFinalizacion = null;

    this.observacionFinal = '';

    this.error = '';

    this.mensaje = '';

    this.mostrandoFinalizacion = true;
  }

  cancelarFinalizacion(): void {

    if (this.finalizandoSolicitud) {
      return;
    }

    this.mostrandoFinalizacion = false;

    this.idResultadoFinalizacion = null;

    this.observacionFinal = '';

    this.error = '';

    this.mensaje = '';
  }

  finalizarSolicitud(): void {

    if (
      !this.solicitud
      || this.finalizandoSolicitud
    ) {
      return;
    }

    const idResultado =
      Number(this.idResultadoFinalizacion);

    if (
      ![3, 4, 6].includes(idResultado)
    ) {

      this.error =
        'Seleccione el resultado de la finalización.';

      return;
    }

    const observacion =
      this.normalizarTexto(
        this.observacionFinal
      );

    if (!observacion) {

      this.error =
        'Ingrese la observación final de la solicitud.';

      return;
    }

    if (observacion.length > 1000) {

      this.error =
        'La observación final no puede superar los 1000 caracteres.';

      return;
    }

    this.error = '';

    this.mensaje = '';

    this.finalizandoSolicitud = true;

    this.api
      .finalizarSolicitud({

        idSolicitudCredito:
          this.solicitud.idSolicitudCredito,

        idSolicitudResultado:
          idResultado,

        observacionFinal:
          observacion

      })
      .subscribe({

        next: respuesta => {

          this.finalizandoSolicitud = false;

          this.mostrandoFinalizacion = false;

          this.idResultadoFinalizacion = null;

          this.observacionFinal = '';

          this.stateService.limpiar();

          this.mensaje =
            `Solicitud ${respuesta.numeroSolicitud} finalizada como ${respuesta.nombreResultado}.`;

          this.router.navigate([
            '/cartera/originacion'
          ]);
        },

        error: error => {

          this.finalizandoSolicitud = false;

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible finalizar la solicitud.'
            );
        }

      });
  }

  // =========================================================
  // ESTADO DE NAVEGACIÓN
  // =========================================================

  private guardarEstadoNavegacion(): void {

    const idAgencia =
      Number(
        this.agenciaActiva?.idAgencia
      );

    if (
      !Number.isInteger(idAgencia)
      || idAgencia <= 0
    ) {
      return;
    }

    this.stateService.guardar({
      idAgencia,
      filtrosAsociado: {
        ...this.filtrosAsociado
      },
      busquedaRealizada:
        this.busquedaRealizada,
      asociados:
        [...this.asociados],
      asociadoSeleccionado:
        this.asociadoSeleccionado,
      solicitudExistente:
        this.solicitudExistente,
      solicitud:
        this.solicitud,
      formulario: {
        ...this.formulario
      },
      modalidadSeleccionada:
        this.modalidadSeleccionada,
      resultadoGuardado:
        this.resultadoGuardado,
      scrollY:
        window.scrollY
    });
  }


  private restaurarEstadoNavegacion(): void {

    const estado =
      this.stateService.consumir();

    if (!estado) {
      return;
    }

    const idAgenciaActiva =
      Number(
        this.agenciaActiva?.idAgencia
      );

    if (
      !Number.isInteger(idAgenciaActiva)
      || idAgenciaActiva <= 0
      || estado.idAgencia !== idAgenciaActiva
    ) {
      return;
    }

    this.filtrosAsociado = {
      ...estado.filtrosAsociado
    };

    this.busquedaRealizada =
      estado.busquedaRealizada;

    this.asociados =
      [...estado.asociados];

    this.asociadoSeleccionado =
      estado.asociadoSeleccionado;

    if (this.asociadoSeleccionado) {
      this.cargarContextoResumido();
    }

    this.solicitudExistente =
      estado.solicitudExistente;

    this.solicitud =
      estado.solicitud;

    this.formulario = {
      ...estado.formulario
    };

    this.modalidadSeleccionada =
      estado.modalidadSeleccionada;

    this.resultadoGuardado =
      estado.resultadoGuardado;

    if (
      Number.isFinite(estado.scrollY)
      && estado.scrollY > 0
    ) {

      setTimeout(
        () =>
          window.scrollTo({
            top: estado.scrollY,
            behavior: 'auto'
          }),
        0
      );
    }
  }

  // =========================================================
  // GETTERS
  // =========================================================

  get garantiaSeleccionada(): GarantiaCredito | null {

    const codigo =
      this.formulario.codigoGarantiaCredito;

    if (!codigo) {
      return null;
    }

    return (
      this.garantias.find(
        garantia =>
          garantia.codigoGarantiaCredito === codigo
      )
      ?? null
    );
  }


  get tipoGarantiaSeleccionada(): string {

    const tipo =
      this.garantiaSeleccionada
        ?.tipoGarantia
        ?.trim()
        .toUpperCase();

    if (tipo === 'R') {
      return 'REAL';
    }

    if (tipo === 'P') {
      return 'PERSONAL';
    }

    return '';
  }

  get tieneAsociado(): boolean {

    return (
      this.asociadoSeleccionado
      !== null
    );

  }

  get tieneSolicitud(): boolean {

    return (
      this.solicitud !== null
    );

  }

  get solicitudEditable(): boolean {

    if (this.solicitud) {

      return (
        this.solicitud.resultadoFinal !== true
        && this.solicitud.activo !== false
      );
    }

    return (
      this.asociadoSeleccionado !== null
      && this.solicitudExistente === null
    );
  }

  get nombreAgenciaActiva(): string {

    return (
      this.agenciaActiva
        ?.nombreAgencia
      ?? ''
    );

  }

  get cupoReciprocidadSimulado(): number {

    const aportes =
      Number(
        this.contextoAsociado
          ?.reciprocidad
          ?.saldoAportes
        ?? 0
      );

    const factor =
      Number(
        this.reciprocidadSimulada
        ?? 0
      );

    return aportes * factor;
  }


  get diferenciaReciprocidadSimulada(): number {

    const saldoCartera =
      Number(
        this.contextoAsociado
          ?.reciprocidad
          ?.saldoCarteraActual
        ?? 0
      );

    return (
      this.cupoReciprocidadSimulado
      - saldoCartera
    );
  }


  get disponibleReciprocidadSimulado(): number {

    return Math.max(
      this.diferenciaReciprocidadSimulada,
      0
    );
  }


  get excesoReciprocidadSimulado(): number {

    return Math.max(
      -this.diferenciaReciprocidadSimulada,
      0
    );
  }

  // =========================================================
  // MODALIDAD
  // =========================================================

  claveModalidad(
    modalidad:
      ModalidadInteres
  ): string {

    return this.crearClaveModalidad(
      modalidad.periodoCodigo,
      modalidad.tipoModalidad
    );

  }


  private crearClaveModalidad(
    periodoCodigo: string,
    tipoModalidad: string
  ): string {

    return (
      `${periodoCodigo}|${tipoModalidad}`
    );

  }


  private obtenerModalidadSeleccionada():
    ModalidadInteres | null {

    return (
      this.modalidades.find(
        modalidad =>
          this.claveModalidad(
            modalidad
          )
          === this.modalidadSeleccionada
      )
      ?? null
    );

  }


  // =========================================================
  // TABLA DE AMORTIZACIÓN
  // =========================================================

  get puedeGenerarTablaAmortizacion(): boolean {

    // =======================================================
    // SOLICITUD YA GUARDADA
    // =======================================================

    if (this.solicitud) {

      const modalidad =
        this.obtenerModalidadSolicitud(
          this.solicitud
        );

      return (
        Number(this.solicitud.valorSolicitado) > 0
        && Number(this.solicitud.tasaColocacionAplicada) > 0
        && Number(this.solicitud.plazoSolicitado) > 0
        && Number(this.solicitud.amortizacionCapital) > 0
        && !!this.solicitud.codigoTipoCuota
        && !!this.solicitud.tipoModalidadInteres
        && modalidad !== null
        && Number(modalidad.periodoMeses) > 0
      );
    }


    // =======================================================
    // SIMULACIÓN ANTES DE GRABAR
    // =======================================================

    const modalidad =
      this.obtenerModalidadSeleccionada();

    return (
      Number(this.formulario.idLineaCredito) > 0
      && !!this.formulario.codigoGarantiaCredito
      && Number(this.formulario.amortizacionCapital) > 0
      && Number(this.formulario.plazoSolicitado) > 0
      && Number(this.formulario.valorSolicitado) > 0
      && !!this.formulario.codigoTipoCuota
      && modalidad !== null
      && Number(modalidad.periodoMeses) > 0
      && !!modalidad.tipoModalidad
    );
  }


  abrirTablaAmortizacion(): void {

    // =======================================================
    // SOLICITUD YA GUARDADA
    // =======================================================

    if (this.solicitud) {

      const modalidad =
        this.obtenerModalidadSolicitud(
          this.solicitud
        );

      if (!modalidad) {

        this.error =
          'No fue posible determinar la periodicidad de intereses de la solicitud.';

        return;
      }

      this.abrirTablaAmortizacionConDatos(
        Number(this.solicitud.valorSolicitado),
        Number(this.solicitud.tasaColocacionAplicada),
        Number(this.solicitud.plazoSolicitado),
        Number(this.solicitud.amortizacionCapital),
        Number(modalidad.periodoMeses),
        this.solicitud.tipoModalidadInteres ?? '',
        this.solicitud.codigoTipoCuota ?? ''
      );

      return;
    }


    // =======================================================
    // SIMULACIÓN ANTES DE GRABAR
    // =======================================================

    const modalidad =
      this.obtenerModalidadSeleccionada();

    if (!modalidad) {

      this.error =
        'Seleccione la modalidad de interés para generar la tabla de amortización.';

      return;
    }

    const idLineaCredito =
      Number(
        this.formulario.idLineaCredito
      );

    const codigoGarantiaCredito =
      this.normalizarTexto(
        this.formulario.codigoGarantiaCredito
      );

    const amortizacionCapital =
      Number(
        this.formulario.amortizacionCapital
      );

    const plazoSolicitado =
      Number(
        this.formulario.plazoSolicitado
      );

    const valorSolicitado =
      Number(
        this.formulario.valorSolicitado
      );

    if (
      !Number.isInteger(idLineaCredito)
      || idLineaCredito <= 0
      || !codigoGarantiaCredito
      || !Number.isInteger(amortizacionCapital)
      || amortizacionCapital <= 0
      || !Number.isInteger(plazoSolicitado)
      || plazoSolicitado <= 0
      || !Number.isFinite(valorSolicitado)
      || valorSolicitado <= 0
      || !this.formulario.codigoTipoCuota
      || Number(modalidad.periodoMeses) <= 0
      || !modalidad.tipoModalidad
    ) {

      this.error =
        'Complete los datos financieros requeridos para generar la tabla de amortización.';

      return;
    }

    this.error = '';

    this.api
      .consultarTasaSimulacion(
        idLineaCredito,
        codigoGarantiaCredito,
        amortizacionCapital,
        plazoSolicitado
      )
      .subscribe({

        next: tasaColocacion => {

          this.abrirTablaAmortizacionConDatos(
            valorSolicitado,
            Number(tasaColocacion),
            plazoSolicitado,
            amortizacionCapital,
            Number(modalidad.periodoMeses),
            modalidad.tipoModalidad,
            this.formulario.codigoTipoCuota
          );
        },

        error: error => {

          this.error =
            this.obtenerMensajeError(
              error,
              'No fue posible determinar la tasa de colocación para la simulación.'
            );
        }

      });
  }


  private abrirTablaAmortizacionConDatos(
    valorCredito: number,
    tasaColocacion: number,
    plazoMeses: number,
    amortizacionCapitalMeses: number,
    periodoInteresMeses: number,
    tipoModalidadInteres: string,
    codigoTipoCuota: string
  ): void {

    if (
      !Number.isFinite(valorCredito)
      || valorCredito <= 0
      || !Number.isFinite(tasaColocacion)
      || tasaColocacion <= 0
      || !Number.isInteger(plazoMeses)
      || plazoMeses <= 0
      || !Number.isInteger(amortizacionCapitalMeses)
      || amortizacionCapitalMeses <= 0
      || !Number.isInteger(periodoInteresMeses)
      || periodoInteresMeses <= 0
      || !tipoModalidadInteres
      || !codigoTipoCuota
    ) {

      this.error =
        'No están completos los datos financieros requeridos para generar la tabla de amortización.';

      return;
    }

    const fechaDesembolso =
      this.fechaHoyLocal();

    const fechaPrimeraCuotaCapital =
      this.sumarMesesFecha(
        fechaDesembolso,
        amortizacionCapitalMeses
      );

    const fechaPrimeraCuotaInteres =
      this.sumarMesesFecha(
        fechaDesembolso,
        periodoInteresMeses
      );

    const request:
      TablaAmortizacionRequest = {

        valorCredito,
        tasaColocacion,
        plazoMeses,
        amortizacionCapitalMeses,
        periodoInteresMeses,

        tipoModalidadInteres,
        codigoTipoCuota,

        fechaDesembolso,
        fechaPrimeraCuotaCapital,
        fechaPrimeraCuotaInteres,

        // En Solicitud todavía no existe el seguro definitivo.
        porcentajeSeguroCredito: 0,
        porcentajeSeguroEntidad: 0
      };

    this.error = '';

    this.tablaAmortizacion
      ?.abrir(request);
  }

  private obtenerModalidadSolicitud(
    solicitud: SolicitudCreditoDetalle
  ): ModalidadInteres | null {

    if (
      !solicitud.periodoCodigoInteres
      || !solicitud.tipoModalidadInteres
    ) {
      return null;
    }

    return (
      this.modalidades.find(
        modalidad =>
          modalidad.periodoCodigo
            === solicitud.periodoCodigoInteres
          && modalidad.tipoModalidad
            === solicitud.tipoModalidadInteres
      )
      ?? null
    );

  }


  private fechaHoyLocal(): string {

    const hoy =
      new Date();

    return this.formatearFechaLocal(
      hoy.getFullYear(),
      hoy.getMonth() + 1,
      hoy.getDate()
    );

  }


  private sumarMesesFecha(
    fecha: string,
    meses: number
  ): string {

    const partes =
      fecha.split('-')
        .map(Number);

    const anio = partes[0];
    const mes = partes[1];
    const dia = partes[2];

    if (
      !Number.isInteger(anio)
      || !Number.isInteger(mes)
      || !Number.isInteger(dia)
      || !Number.isInteger(meses)
    ) {
      return fecha;
    }

    const indiceMes =
      (mes - 1) + meses;

    const anioDestino =
      anio + Math.floor(indiceMes / 12);

    const mesDestino =
      ((indiceMes % 12) + 12) % 12;

    const ultimoDiaMes =
      new Date(
        anioDestino,
        mesDestino + 1,
        0
      ).getDate();

    const diaDestino =
      Math.min(
        dia,
        ultimoDiaMes
      );

    return this.formatearFechaLocal(
      anioDestino,
      mesDestino + 1,
      diaDestino
    );

  }


  private formatearFechaLocal(
    anio: number,
    mes: number,
    dia: number
  ): string {

    return (
      `${anio.toString().padStart(4, '0')}`
      + `-${mes.toString().padStart(2, '0')}`
      + `-${dia.toString().padStart(2, '0')}`
    );

  }


  // =========================================================
  // TRACK BY
  // =========================================================

  trackByAsociado(
    indice: number,
    asociado:
      OriginacionAsociado
  ): number {

    return (
      asociado.idDatosPersonal
      ?? indice
    );

  }


  // =========================================================
  // UTILIDADES
  // =========================================================

  private crearFormularioVacio():
    FormularioCredito {

    return {

      idLineaCredito: null,

      codigoClasificacionCredito: '',

      codigoDestinoEconomico: '',

      codigoGarantiaCredito: '',

      codigoSubgarantia: '',

      idFondoGarantia: null,

      codigoFormaPago: 'C',

      amortizacionCapital: null,

      codigoTipoCuota: '',

      plazoSolicitado: null,

      mesesGraciaCapital: 0,

      mesesGraciaInteres: 0,

      valorSolicitado: null,

      idEmpresaLibranza: null,

      observacionAsesor: ''

    };

  }


  private normalizarTexto(
    valor:
      | string
      | null
      | undefined
  ): string {

    return String(
      valor ?? ''
    )
      .trim()
      .replace(
        /\s+/g,
        ' '
      );

  }


  private fechaEnMilisegundos(
    fecha:
      string | null
  ): number {

    if (!fecha) {
      return 0;
    }

    const valor =
      new Date(fecha)
        .getTime();

    return Number.isFinite(valor)
      ? valor
      : 0;

  }


  private obtenerMensajeError(
    error: unknown,
    predeterminado: string
  ): string {

    if (
      !error
      || typeof error !== 'object'
    ) {
      return predeterminado;
    }

    const respuesta =
      error as {

        message?: string;

        error?: {

          mensaje?: string;

          message?: string;

          error?: string;

        };

      };

    return (
      respuesta.error?.mensaje
      ?? respuesta.error?.message
      ?? respuesta.error?.error
      ?? respuesta.message
      ?? predeterminado
    );

  }

  limpiarBusquedaAsociado(): void {

    if (
      this.buscandoAsociados
      || this.cargandoSolicitud
    ) {
      return;
    }

    this.filtrosAsociado = {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: ''
    };

    this.asociados = [];

    this.busquedaRealizada = false;

    this.error = '';
  }

  // =========================================================
  // CONTEXTO RESUMIDO DEL ASOCIADO
  // =========================================================

  private cargarContextoResumido(): void {

    if (
      !this.asociadoSeleccionado
      || !this.agenciaActiva
    ) {
      return;
    }

    const idDatosPersonal =
      Number(
        this.asociadoSeleccionado.idDatosPersonal
      );

    const idAgencia =
      Number(
        this.agenciaActiva.idAgencia
      );

    if (
      !Number.isInteger(idDatosPersonal)
      || idDatosPersonal <= 0
      || !Number.isInteger(idAgencia)
      || idAgencia <= 0
    ) {
      return;
    }

    this.cargandoContextoAsociado = true;

    this.contextoApi
      .consultar(
        idDatosPersonal,
        idAgencia
      )
      .subscribe({

        next: contexto => {

          this.contextoAsociado =
            contexto;

          this.reciprocidadSimulada =
            contexto.reciprocidad
              ?.reciprocidadInicial
            ?? 50;

          this.cargandoContextoAsociado =
            false;
        },

        error: () => {

          this.contextoAsociado =
            null;

          this.reciprocidadSimulada =
            50;

          this.cargandoContextoAsociado =
            false;
        }

      });
  }

  regresarAlListado(): void {

    this.stateService.limpiar();

    this.router.navigate([
      '/cartera/originacion'
    ]);
  }

}
