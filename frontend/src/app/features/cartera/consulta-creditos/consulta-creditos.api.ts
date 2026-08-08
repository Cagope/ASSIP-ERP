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
} from '../../../../environments/environment';

import {
  CarteraAsociadoBusqueda,
  ConsultaCreditoAlivio,
  ConsultaCreditoDetalle,
  ConsultaCreditoEvaluacion,
  ConsultaCreditoExtracto,
  ConsultaCreditoIntegral,
  ConsultaCreditoInteres,
  ConsultaCreditoResumen,
  ConsultaCreditoSeguro
} from './consulta-creditos.models';


@Injectable({
  providedIn: 'root'
})
export class ConsultaCreditosApi {

  // =========================================================
  // URLS
  // =========================================================

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/consulta-creditos`;

  private readonly asociadosUrl =
    `${environment.apiUrl}/cartera/asociados`;


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly http: HttpClient
  ) {}


  // =========================================================
  // BÚSQUEDA DE ASOCIADOS
  // =========================================================

  buscarAsociados(
    documento: string,
    nombres: string,
    primerApellido: string,
    segundoApellido: string
  ): Observable<CarteraAsociadoBusqueda[]> {

    let params =
      new HttpParams();

    const documentoNormalizado =
      this.normalizarTexto(
        documento
      );

    const nombresNormalizados =
      this.normalizarTexto(
        nombres
      );

    const primerApellidoNormalizado =
      this.normalizarTexto(
        primerApellido
      );

    const segundoApellidoNormalizado =
      this.normalizarTexto(
        segundoApellido
      );

    if (documentoNormalizado) {

      params =
        params.set(
          'documento',
          documentoNormalizado
        );

    }

    if (nombresNormalizados) {

      params =
        params.set(
          'nombres',
          nombresNormalizados
        );

    }

    if (primerApellidoNormalizado) {

      params =
        params.set(
          'primerApellido',
          primerApellidoNormalizado
        );

    }

    if (segundoApellidoNormalizado) {

      params =
        params.set(
          'segundoApellido',
          segundoApellidoNormalizado
        );

    }

    return this.http.get<
      CarteraAsociadoBusqueda[]
    >(
      `${this.asociadosUrl}/buscar`,
      {
        params
      }
    );

  }


  // =========================================================
  // CRÉDITOS DEL ASOCIADO
  // =========================================================

  listarPorPersona(
    idDatosPersonal: number
  ): Observable<ConsultaCreditoResumen[]> {

    return this.http.get<
      ConsultaCreditoResumen[]
    >(
      `${this.baseUrl}/persona/${idDatosPersonal}/creditos`
    );

  }


  // =========================================================
  // DETALLE DEL CRÉDITO
  // =========================================================

  buscarPorId(
    idCarteraCredito: number
  ): Observable<ConsultaCreditoDetalle> {

    return this.http.get<
      ConsultaCreditoDetalle
    >(
      `${this.baseUrl}/${idCarteraCredito}`
    );

  }


  // =========================================================
  // EXTRACTO
  // =========================================================

  listarExtracto(
    idCarteraCredito: number
  ): Observable<ConsultaCreditoExtracto[]> {

    return this.http.get<
      ConsultaCreditoExtracto[]
    >(
      `${this.baseUrl}/${idCarteraCredito}/extracto`
    );

  }


  // =========================================================
  // PDF DEL EXTRACTO
  // =========================================================

  generarExtractoPdf(
    idCarteraCredito: number
  ): Observable<Blob> {

    return this.http.get(
      `${this.baseUrl}/${idCarteraCredito}/extracto/pdf`,
      {
        responseType: 'blob'
      }
    );

  }


  // =========================================================
  // SEGUROS
  // =========================================================

  listarSeguros(
    idCarteraCredito: number
  ): Observable<ConsultaCreditoSeguro[]> {

    return this.http.get<
      ConsultaCreditoSeguro[]
    >(
      `${this.baseUrl}/${idCarteraCredito}/seguros`
    );

  }


  // =========================================================
  // ALIVIOS
  // =========================================================

  listarAlivios(
    idCarteraCredito: number
  ): Observable<ConsultaCreditoAlivio[]> {

    return this.http.get<
      ConsultaCreditoAlivio[]
    >(
      `${this.baseUrl}/${idCarteraCredito}/alivios`
    );

  }


  // =========================================================
  // INTERESES CAUSADOS
  // =========================================================

  listarInteresesCausados(
    idCarteraCredito: number
  ): Observable<ConsultaCreditoInteres[]> {

    return this.http.get<
      ConsultaCreditoInteres[]
    >(
      `${this.baseUrl}/${idCarteraCredito}/intereses-causados`
    );

  }


  // =========================================================
  // EVALUACIONES
  // =========================================================

  listarEvaluaciones(
    idCarteraCredito: number
  ): Observable<ConsultaCreditoEvaluacion[]> {

    return this.http.get<
      ConsultaCreditoEvaluacion[]
    >(
      `${this.baseUrl}/${idCarteraCredito}/evaluaciones`
    );

  }

  buscarUltimaEvaluacion(
    idCarteraCredito: number
  ): Observable<ConsultaCreditoEvaluacion | null> {

    return this.http.get<
      ConsultaCreditoEvaluacion | null
    >(
      `${this.baseUrl}/${idCarteraCredito}/ultima-evaluacion`
    );

  }


  // =========================================================
  // CONSULTA INTEGRAL
  // =========================================================

  consultarIntegral(
    idCarteraCredito: number
  ): Observable<ConsultaCreditoIntegral> {

    return this.http.get<
      ConsultaCreditoIntegral
    >(
      `${this.baseUrl}/${idCarteraCredito}/integral`
    );

  }


  // =========================================================
  // EXISTENCIA
  // =========================================================

  existeCredito(
    idCarteraCredito: number
  ): Observable<boolean> {

    return this.http.get<boolean>(
      `${this.baseUrl}/${idCarteraCredito}/existe`
    );

  }


  // =========================================================
  // UTILIDADES
  // =========================================================

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

}
