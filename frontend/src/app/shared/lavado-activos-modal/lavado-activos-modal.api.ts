import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface LavadoActivosRequest {
  modulo: string;
  proceso: string;
  idOrigen?: number | null;

  fechaTransaccion: string;
  fechaContable: string;

  tipoTransaccion: string;
  valorTransaccion: number;

  idAgencia: number;
  codigoAgencia?: string;
  nombreAgencia?: string;

  idDatosPersonal?: number;
  tipoDocumento?: string;
  documento: string;
  nombreCompleto: string;

  codigoProducto?: string;
  descripcionProducto?: string;

  numeroProducto?: string;
  numeroComprobante?: string;

  actividadEconomica?: string;

  tipoDocumentoRealiza?: string;
  documentoRealiza?: string;
  primerApellidoRealiza?: string;
  segundoApellidoRealiza?: string;
  primerNombreRealiza?: string;
  segundoNombreRealiza?: string;
  direccionRealiza?: string;
  telefonoRealiza?: string;
  departamentoRealiza?: string;
  ciudadRealiza?: string;

  nombreBeneficiario?: string;
  direccionBeneficiario?: string;
  telefonoBeneficiario?: string;
}

export interface LavadoActivosResponse {
  idFormatoLavadoActivos?: number;
  generado: boolean;
  requiereFormato: boolean;
  mensaje: string;
}

export interface LavadoActivosPersona {
  idDatosPersonal?: number;

  tipoDocumento?: string;
  documento?: string;

  idDepartamentoExpedicion?: number;
  idCiudadExpedicion?: number;

  primerApellido?: string;
  segundoApellido?: string;

  primerNombre?: string;
  segundoNombre?: string;

  nombreCompleto?: string;

  direccion?: string;
  telefono?: string;
}

export interface LavadoActivosFormato {
  idFormatoLavadoActivos: number;

  modulo?: string;
  proceso?: string;
  idOrigen?: number;

  fechaTransaccion: string;
  fechaContable: string;

  tipoTransaccion: string;
  valorTransaccion: number;

  idAgencia: number;
  codigoAgencia?: string;
  nombreAgencia?: string;

  idDatosPersonal?: number;
  tipoDocumento?: string;
  documento?: string;
  nombreCompleto?: string;

  codigoProducto?: string;
  descripcionProducto?: string;
  numeroProducto?: string;
  numeroComprobante?: string;

  actividadEconomica?: string;

  tipoDocumentoRealiza?: string;
  documentoRealiza?: string;
  primerApellidoRealiza?: string;
  segundoApellidoRealiza?: string;
  primerNombreRealiza?: string;
  segundoNombreRealiza?: string;
  direccionRealiza?: string;
  telefonoRealiza?: string;
  departamentoRealiza?: string;
  ciudadRealiza?: string;

  nombreBeneficiario?: string;
  direccionBeneficiario?: string;
  telefonoBeneficiario?: string;

  requiereFirma?: boolean;
  firmado?: boolean;
  vecesImpreso?: number;
}

@Injectable({
  providedIn: 'root'
})
export class LavadoActivosModalApi {

  private readonly baseUrl =
    `${environment.apiUrl}/sarlaft/lavado-activos`;

  constructor(
    private http: HttpClient
  ) {}

  generar(
    request: LavadoActivosRequest
  ): Observable<LavadoActivosResponse> {
    return this.http.post<LavadoActivosResponse>(
      `${this.baseUrl}/generar`,
      request
    );
  }

  buscarPersona(
    documento: string
  ): Observable<LavadoActivosPersona> {
    return this.http.get<LavadoActivosPersona>(
      `${this.baseUrl}/persona`,
      {
        params: {
          documento
        }
      }
    );
  }

  marcarImpreso(
    id: number
  ): Observable<void> {
    return this.http.put<void>(
      `${this.baseUrl}/${id}/impreso`,
      {}
    );
  }

  obtenerFormato(
    id: number
  ): Observable<LavadoActivosFormato> {
    return this.http.get<LavadoActivosFormato>(
      `${this.baseUrl}/${id}`
    );
  }

}
