import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CuentaAhorroSelectDTO {

  idCuentaAhorro: number;
  idDatosPersonal: number;

  codigoForma: number;
  nombreForma: string;

  numeroCuenta: string;

  estadoCodigo: string;
  estadoNombre: string;

  cuentaDisplay: string;
}

@Injectable({ providedIn: 'root' })
export class CuentasAhorroApi {

  private readonly base =
    `${environment.apiUrl}/shared/cuentas_ahorro`;

  constructor(private http: HttpClient) {}

  listarPorPersona(
    idDatosPersonal: number
  ): Observable<CuentaAhorroSelectDTO[]> {

    return this.http.get<CuentaAhorroSelectDTO[]>(
      `${this.base}/${idDatosPersonal}`
    );
  }
}
