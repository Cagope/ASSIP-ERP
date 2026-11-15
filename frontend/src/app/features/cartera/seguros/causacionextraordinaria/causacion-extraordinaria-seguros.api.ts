import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { ConsultaCreditosApi } from '../../consulta-creditos/consulta-creditos.api';
import {
  CarteraAsociadoBusqueda,
  ConsultaCreditoResumen,
  ConsultaCreditoDetalle
} from '../../consulta-creditos/consulta-creditos.models';
import {
  CausacionExtraordinariaSegurosSugerencia,
  CausacionExtraordinariaSegurosSolicitud,
  CausacionExtraordinariaSegurosVistaPrevia,
  CausacionExtraordinariaSegurosResultado
} from './causacion-extraordinaria-seguros.models';

/** API HTTP de causación extraordinaria de seguros; reutiliza la búsqueda de créditos existente. */
@Injectable({ providedIn: 'root' })
export class CausacionExtraordinariaSegurosApi {
  private readonly baseUrl = `${environment.apiUrl}/cartera/seguros/causacion-extraordinaria`;

  constructor(
    private readonly http: HttpClient,
    private readonly consultaCreditosApi: ConsultaCreditosApi
  ) {}

  /** Busca asociados por identificación y nombres sin solicitar IDs internos al funcionario. */
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

  /** Lista los créditos del asociado seleccionado. */
  listarCreditosPorAsociado(idDatosPersonal: number): Observable<ConsultaCreditoResumen[]> {
    return this.consultaCreditosApi.listarPorPersona(idDatosPersonal);
  }

  /** Consulta el detalle del crédito y su agencia mediante el API existente. */
  consultarDetalleCredito(idCredito: number): Observable<ConsultaCreditoDetalle> {
    return this.consultaCreditosApi.buscarPorId(idCredito);
  }

  /** Obtiene la configuración vigente y los valores sugeridos para la causación. */
  consultarSugerencia(idCredito: number): Observable<CausacionExtraordinariaSegurosSugerencia> {
    return this.http.get<CausacionExtraordinariaSegurosSugerencia>(
      `${this.baseUrl}/creditos/${idCredito}/sugerencia`
    );
  }

  /** Valida la solicitud y obtiene la nota contable propuesta sin registrar movimientos. */
  previsualizar(
    solicitud: CausacionExtraordinariaSegurosSolicitud
  ): Observable<CausacionExtraordinariaSegurosVistaPrevia> {
    return this.http.post<CausacionExtraordinariaSegurosVistaPrevia>(
      `${this.baseUrl}/vista-previa`,
      solicitud
    );
  }

  /** Confirma la causación y la generación de la NC en el backend. */
  confirmar(
    solicitud: CausacionExtraordinariaSegurosSolicitud
  ): Observable<CausacionExtraordinariaSegurosResultado> {
    return this.http.post<CausacionExtraordinariaSegurosResultado>(
      `${this.baseUrl}/confirmar`,
      solicitud
    );
  }
}
