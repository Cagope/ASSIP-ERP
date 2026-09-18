import {
  Injectable
} from '@angular/core';

import {
  HttpClient,
  HttpParams
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  environment
} from '../../../../../environments/environment';

import {
  CentralRiesgoCatalogo,
  ClasificacionCredito,
  DestinoEconomico,
  FondoGarantia,
  FormaPago,
  GarantiaCredito,
  LineaCredito,
  ModalidadInteres,
  OriginacionAsociado,
  SolicitudCrearRetomarRequest,
  SolicitudCrearRetomarResponse,
  SolicitudCreditoCrearRequest,
  SolicitudCreditoDetalle,
  SolicitudCreditoGuardarRequest,
  SolicitudCreditoGuardarResponse,
  SolicitudCreditoResumen,
  SolicitudFinalizarRequest,
  SolicitudFinalizarResponse,
  SolicitudValidacionAprobacion,
  SolicitudEnviarAprobacionResponse,
  SolicitudEnteAprobadorPreviewRequest,
  SolicitudEnteAprobadorPreview,
  SubgarantiaCredito,
  TipoCuota
} from './originacion-solicitud.models';

@Injectable({
  providedIn: 'root'
})
export class OriginacionSolicitudApi {

  // =========================================================
  // URLS
  // =========================================================

  private readonly solicitudesUrl =
    `${environment.apiUrl}/cartera/originacion/solicitudes`;

  private readonly asociadosUrl =
    `${environment.apiUrl}/cartera/originacion/asociados`;

  private readonly catalogosUrl =
    `${environment.apiUrl}/cartera/originacion/catalogos`;


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly http: HttpClient
  ) {}


  // =========================================================
  // ASOCIADOS
  // =========================================================

  buscarAsociados(
    idAgencia: number,
    documento: string,
    nombres: string,
    primerApellido: string,
    segundoApellido: string
  ): Observable<OriginacionAsociado[]> {

    let params = new HttpParams()
      .set('idAgencia', idAgencia.toString());

    const documentoNormalizado = documento?.trim() ?? '';
    const nombresNormalizados = nombres?.trim() ?? '';
    const primerApellidoNormalizado = primerApellido?.trim() ?? '';
    const segundoApellidoNormalizado = segundoApellido?.trim() ?? '';

    if (documentoNormalizado) {
      params = params.set(
        'documento',
        documentoNormalizado
      );
    }

    if (nombresNormalizados) {
      params = params.set(
        'nombres',
        nombresNormalizados
      );
    }

    if (primerApellidoNormalizado) {
      params = params.set(
        'primerApellido',
        primerApellidoNormalizado
      );
    }

    if (segundoApellidoNormalizado) {
      params = params.set(
        'segundoApellido',
        segundoApellidoNormalizado
      );
    }

    return this.http.get<OriginacionAsociado[]>(
      `${this.asociadosUrl}/buscar`,
      { params }
    );
  }


  buscarAsociadoPorId(
    idDatosPersonal: number,
    idAgencia: number
  ): Observable<OriginacionAsociado> {

    const params = new HttpParams()
      .set(
        'idAgencia',
        idAgencia.toString()
      );

    return this.http.get<OriginacionAsociado>(
      `${this.asociadosUrl}/${idDatosPersonal}`,
      { params }
    );
  }


  // =========================================================
  // SOLICITUD
  // CREAR + GUARDAR DATOS DEL CRÉDITO
  // =========================================================

  crearSolicitud(
    request:
      SolicitudCreditoCrearRequest
  ): Observable<SolicitudCreditoGuardarResponse> {

    return this.http.post<
      SolicitudCreditoGuardarResponse
    >(
      `${this.solicitudesUrl}/crear`,
      request
    );
  }


  // =========================================================
  // SOLICITUD
  // CREAR / RETOMAR
  // =========================================================

  crearRetomar(
    request:
      SolicitudCrearRetomarRequest
  ): Observable<SolicitudCrearRetomarResponse> {

    return this.http.post<
      SolicitudCrearRetomarResponse
    >(
      `${this.solicitudesUrl}/crear-retomar`,
      request
    );
  }


  // =========================================================
  // SOLICITUD
  // GUARDAR DATOS DEL CRÉDITO
  // =========================================================

  guardarCredito(
    request:
      SolicitudCreditoGuardarRequest
  ): Observable<SolicitudCreditoGuardarResponse> {

    return this.http.post<
      SolicitudCreditoGuardarResponse
    >(
      `${this.solicitudesUrl}/guardar-credito`,
      request
    );
  }

  // =========================================================
  // SOLICITUD
  // PREVISUALIZAR ENTE APROBADOR
  // =========================================================

  previsualizarEnteAprobador(
    request: SolicitudEnteAprobadorPreviewRequest
  ): Observable<SolicitudEnteAprobadorPreview> {

    return this.http.post<SolicitudEnteAprobadorPreview>(
      `${this.solicitudesUrl}/preview-ente-aprobador`,
      request
    );
  }

  // =========================================================
  // SOLICITUD
  // CONSULTAR TASA PARA SIMULACIÓN
  // =========================================================

  consultarTasaSimulacion(
    idLineaCredito: number,
    codigoGarantiaCredito: string,
    amortizacionCapital: number,
    plazoSolicitado: number
  ): Observable<number> {

    const params = new HttpParams()
      .set(
        'idLineaCredito',
        idLineaCredito.toString()
      )
      .set(
        'codigoGarantiaCredito',
        codigoGarantiaCredito
      )
      .set(
        'amortizacionCapital',
        amortizacionCapital.toString()
      )
      .set(
        'plazoSolicitado',
        plazoSolicitado.toString()
      );

    return this.http.get<number>(
      `${this.solicitudesUrl}/tasa-simulacion`,
      { params }
    );
  }

  // =========================================================
  // SOLICITUD
  // LISTAR
  // =========================================================

  listarSolicitudes():
    Observable<SolicitudCreditoResumen[]> {

    return this.http.get<
      SolicitudCreditoResumen[]
    >(
      this.solicitudesUrl
    );
  }


  // =========================================================
  // SOLICITUD
  // DETALLE
  // =========================================================

  buscarSolicitudPorId(
    idSolicitudCredito: number
  ): Observable<SolicitudCreditoDetalle> {

    return this.http.get<
      SolicitudCreditoDetalle
    >(
      `${this.solicitudesUrl}/${idSolicitudCredito}`
    );
  }

  // =========================================================
  // SOLICITUD
  // FINALIZAR
  // =========================================================

  finalizarSolicitud(
    request: SolicitudFinalizarRequest
  ): Observable<SolicitudFinalizarResponse> {

    return this.http.put<SolicitudFinalizarResponse>(
      `${this.solicitudesUrl}/finalizar`,
      request
    );

  }

  // =========================================================
  // SOLICITUD
  // VALIDAR PARA APROBACIÓN
  // =========================================================

  validarParaAprobacion(
    idSolicitudCredito: number
  ): Observable<SolicitudValidacionAprobacion> {

    return this.http.get<SolicitudValidacionAprobacion>(
      `${this.solicitudesUrl}/${idSolicitudCredito}/validar-aprobacion`
    );
  }


  // =========================================================
  // SOLICITUD
  // ENVIAR A APROBACIÓN
  // =========================================================

  enviarAprobacion(
    idSolicitudCredito: number
  ): Observable<SolicitudEnviarAprobacionResponse> {

    return this.http.put<SolicitudEnviarAprobacionResponse>(
      `${this.solicitudesUrl}/${idSolicitudCredito}/enviar-aprobacion`,
      null
    );
  }


  // =========================================================
  // CATÁLOGOS
  // =========================================================

  listarLineasCredito():
    Observable<LineaCredito[]> {

    return this.http.get<
      LineaCredito[]
    >(
      `${this.catalogosUrl}/lineas-credito`
    );
  }


  listarClasificacionesCredito():
    Observable<ClasificacionCredito[]> {

    return this.http.get<
      ClasificacionCredito[]
    >(
      `${this.catalogosUrl}/clasificaciones-credito`
    );
  }


  listarDestinosEconomicos():
    Observable<DestinoEconomico[]> {

    return this.http.get<
      DestinoEconomico[]
    >(
      `${this.catalogosUrl}/destinos-economicos`
    );
  }

  listarGarantias():
    Observable<GarantiaCredito[]> {

    return this.http.get<
      GarantiaCredito[]
    >(
      `${this.catalogosUrl}/garantias`
    );
  }


  listarSubgarantias():
    Observable<SubgarantiaCredito[]> {

    return this.http.get<
      SubgarantiaCredito[]
    >(
      `${this.catalogosUrl}/subgarantias`
    );
  }

  listarFondosGarantias():
    Observable<FondoGarantia[]> {

    return this.http.get<
      FondoGarantia[]
    >(
      `${environment.apiUrl}/cartera/catalogos/fondos-garantias`
    );
  }

  listarFormasPago():
    Observable<FormaPago[]> {

    return this.http.get<
      FormaPago[]
    >(
      `${this.catalogosUrl}/formas-pago`
    );
  }


  listarModalidadesInteres():
    Observable<ModalidadInteres[]> {

    return this.http.get<
      ModalidadInteres[]
    >(
      `${this.catalogosUrl}/modalidades-interes`
    );
  }


  listarTiposCuota():
    Observable<TipoCuota[]> {

    return this.http.get<
      TipoCuota[]
    >(
      `${this.catalogosUrl}/tipos-cuota`
    );
  }


  listarCentralesRiesgo():
    Observable<CentralRiesgoCatalogo[]> {

    return this.http.get<
      CentralRiesgoCatalogo[]
    >(
      `${this.catalogosUrl}/centrales-riesgo`
    );
  }
}
