import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { ConsultaCreditosApi } from '../../consulta-creditos/consulta-creditos.api';
import {
  CarteraAsociadoBusqueda,
  ConsultaCreditoResumen,
  ConsultaCreditoDetalle
} from '../../consulta-creditos/consulta-creditos.models';
import {
  AdministracionSegurosConfiguracion,
  AdministracionSegurosSeguro,
  AdministracionSegurosEdicionRequest
} from './administracion-seguros.models';

/** API HTTP de administración de seguros; el backend valida agencia, permisos y saldos. */
@Injectable({ providedIn: 'root' })
export class AdministracionSegurosApi {
  private readonly baseUrl = `${environment.apiUrl}/cartera/administracion-seguros`;

  constructor(
    private readonly http: HttpClient,
    private readonly consultaCreditosApi: ConsultaCreditosApi
  ) {}

  /** Busca asociados por identificación y nombres, sin pedir IDs internos al funcionario. */
  buscarAsociados(
    documento: string,
    nombres: string,
    primerApellido: string,
    segundoApellido: string
  ): Observable<CarteraAsociadoBusqueda[]> {
    return this.consultaCreditosApi.buscarAsociados(
      documento, nombres, primerApellido, segundoApellido
    );
  }

  /** Lista los créditos del asociado seleccionado mediante la consulta ya existente. */
  listarCreditosPorAsociado(idDatosPersonal: number): Observable<ConsultaCreditoResumen[]> {
    return this.consultaCreditosApi.listarPorPersona(idDatosPersonal);
  }

  /** Consulta el detalle del crédito para obtener su agencia real. */
  consultarDetalleCredito(idCredito: number): Observable<ConsultaCreditoDetalle> {
    return this.consultaCreditosApi.buscarPorId(idCredito);
  }

  /** Consulta seguros activos e inactivos del crédito y la agencia seleccionados. */
  consultar(idCredito: number, idAgencia: number): Observable<AdministracionSegurosSeguro[]> {
    return this.http.get<AdministracionSegurosSeguro[]>(
      `${this.baseUrl}/creditos/${idCredito}`,
      { params: new HttpParams().set('idAgencia', String(idAgencia)) }
    );
  }

  /** Obtiene el porcentaje base del seguro parametrizado por agencia (código 641). */
  consultarPorcentajeBaseSeguro(idAgencia: number): Observable<number> {
    return this.http.get<number>(
      `${this.baseUrl}/parametros/porcentaje-base`,
      { params: new HttpParams().set('idAgencia', String(idAgencia)) }
    );
  }

  /** Crea la configuración inicial si el crédito aún no tiene seguro. */
  crear(
    idCredito: number,
    idAgencia: number,
    configuracion: AdministracionSegurosConfiguracion
  ): Observable<number> {
    return this.http.post<number>(
      `${this.baseUrl}/creditos/${idCredito}`,
      configuracion,
      { params: new HttpParams().set('idAgencia', String(idAgencia)) }
    );
  }

  /** Edita la configuración existente; el backend registra la auditoría y valida el saldo. */
  editar(
    idCredito: number,
    idCreditoSeguro: number,
    idAgencia: number,
    solicitud: AdministracionSegurosEdicionRequest
  ): Observable<void> {
    return this.http.put<void>(
      `${this.baseUrl}/creditos/${idCredito}/seguros/${idCreditoSeguro}`,
      solicitud,
      { params: new HttpParams().set('idAgencia', String(idAgencia)) }
    );
  }
}
