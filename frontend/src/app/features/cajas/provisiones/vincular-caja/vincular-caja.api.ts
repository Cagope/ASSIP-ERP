import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface CajaProvisionVincularRequest {
  idCaja: number;
  fechaContable: string;
  efectivoInicio: number;
  chequesInicio: number;
}

export interface CajaEstadoDTO {
  idCaja: number;
  codigoCaja: string;
  descripcionCaja: string;

  idProvision: number | null;
  estado: string | null;
  fkUsuarioApertura: number | null;
  fechaApertura: string | null;

  disponible: boolean;
}

export interface CajaProvisionActivaDTO {
  idCaja: number;
  codigoCaja: string;
  descripcionCaja: string;

  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;

  idProvision: number;
  fechaContable: string;
  estado: string;
}

@Injectable({
  providedIn: 'root'
})
export class VincularCajaApi {

  private readonly baseUrl =
    `${environment.apiUrl}/cajas/provisiones`;

  constructor(
    private http: HttpClient
  ) {
  }

  vincular(request: CajaProvisionVincularRequest): Observable<number> {
    return this.http.post<number>(
      `${this.baseUrl}/vincular`,
      request
    );
  }

  listarEstadoCajas(
    idAgencia: number,
    fechaContable: string
  ): Observable<CajaEstadoDTO[]> {
    return this.http.get<CajaEstadoDTO[]>(
      `${this.baseUrl}/estado-cajas`,
      {
        params: {
          idAgencia,
          fechaContable
        }
      }
    );
  }

  obtenerProvisionActivaUsuario(): Observable<CajaProvisionActivaDTO> {
    return this.http.get<CajaProvisionActivaDTO>(
      `${this.baseUrl}/activa-usuario`
    );
  }
}
