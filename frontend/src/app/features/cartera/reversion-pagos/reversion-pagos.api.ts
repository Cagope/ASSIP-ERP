
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ConsultaCreditosApi } from '../consulta-creditos/consulta-creditos.api';
import { CarteraAsociadoBusqueda } from '../consulta-creditos/consulta-creditos.models';
import { PagoCuotasCreditoResumen } from '../pago-cuotas_v1/pago-cuotas_v1.models';
import {
  ReversionPagoOriginal,
  ReversionVistaPrevia,
  ReversionComprobanteContrario,
  ReversionEstadoRestitucion,
  ConfirmarReversionRequest,
  ReversionResultado
} from './reversion-pagos.models';

/**
 * Servicio HTTP de reversión de pagos de cuotas.
 * Reutiliza las consultas existentes y delega las
 * validaciones financieras al backend.
 */
@Injectable({
  providedIn: 'root'
})
export class ReversionPagosApi {
  private readonly baseUrl =
    `${environment.apiUrl}/cartera/reversion-pagos`;

  constructor(
    private readonly http: HttpClient,
    private readonly consultaCreditosApi: ConsultaCreditosApi
  ) {}

  /** Busca asociados por identificación, nombres y apellidos. */
  buscarAsociados(
    documento: string,
    nombres: string,
    primerApellido: string,
    segundoApellido: string
  ): Observable<CarteraAsociadoBusqueda[]> {
    return this.consultaCreditosApi.buscarAsociados(
      documento,
      nombres,
      primerApellido,
      segundoApellido
    );
  }

  /** Consulta los créditos del asociado seleccionado. */
  listarCreditosPorAsociado(
    idDatosPersonal: number
  ): Observable<PagoCuotasCreditoResumen[]> {
    return this.http.get<PagoCuotasCreditoResumen[]>(
      `${this.baseUrl}/asociados/${idDatosPersonal}/creditos`
    );
  }

  /** Consulta los pagos originales registrados para el crédito. */
  listarPagos(
    idCarteraCredito: number
  ): Observable<ReversionPagoOriginal[]> {
    return this.http.get<ReversionPagoOriginal[]>(
      `${this.baseUrl}/creditos/${idCarteraCredito}/pagos`
    );
  }

  /** Consulta la elegibilidad inicial del pago. */
  consultarVistaPrevia(
    idPago: number
  ): Observable<ReversionVistaPrevia> {
    return this.http.get<ReversionVistaPrevia>(
      `${this.baseUrl}/pagos/${idPago}/vista-previa`
    );
  }

  /** Consulta los movimientos del comprobante contrario. */
  consultarComprobanteContrario(
    idPago: number
  ): Observable<ReversionComprobanteContrario> {
    return this.http.get<ReversionComprobanteContrario>(
      `${this.baseUrl}/pagos/${idPago}/comprobante-contrario`
    );
  }

  /** Consulta el estado financiero que se restituirá. */
  consultarEstadoRestitucion(
    idPago: number
  ): Observable<ReversionEstadoRestitucion> {
    return this.http.get<ReversionEstadoRestitucion>(
      `${this.baseUrl}/pagos/${idPago}/estado-restitucion`
    );
  }

  /**
   * Confirma la reversión del pago.
   * El backend valida permiso, agencia, último pago,
   * saldos, contabilidad y atomicidad de la operación.
   */
  confirmarReversion(
    idPago: number,
    solicitud: ConfirmarReversionRequest
  ): Observable<ReversionResultado> {
    return this.http.post<ReversionResultado>(
      `${this.baseUrl}/pagos/${idPago}/confirmar`,
      solicitud
    );
  }
}
