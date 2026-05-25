import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface ExtractoAsociadoBusqueda {
  idDatosPersonal: number;
  documento: string;
  nombreCompleto: string;
  direccion: string;
  totalCuentas: number;
  saldoTotal: number;
}

export interface ExtractoAsociadoRequest {
  idDatosPersonal: number;
  fechaInicial: string;
  fechaFinal: string;
  codigoForma: string;
}

export interface ExtractoAsociadoResumen {
  razonSocial: string;
  siglaEmpresa: string;
  documentoEmpresa: string;
  digitoVerificacion: string;
  telefonoEmpresa: string;
  celularEmpresa: string;
  sitioWebEmpresa: string;
  logoUrl: string;

  idDatosPersonal: number;
  documento: string;
  nombreCompleto: string;
  direccion: string;

  totalCuentas: number;
  saldoInicial: number;
  totalCreditos: number;
  totalDebitos: number;
  saldoFinal: number;
}

export interface ExtractoAsociadoCuenta {
  idCuentaAhorro: number;
  codigoCuenta: string;
  codigoForma: string;
  nombreForma: string;
  nombreAgencia: string;
  saldoInicial: number;
  totalCreditos: number;
  totalDebitos: number;
  saldoFinal: number;
}

export interface ExtractoAsociadoMovimiento {
  idCuentaAhorro: number;
  codigoCuenta: string;
  codigoForma: string;
  nombreForma: string;
  nombreAgencia: string;

  fechaMovimiento: string;
  horaMovimiento: string;

  tipoMovimiento: string;
  descripcionMovimiento: string;

  tipoComprobante: string;
  numeroComprobante: string;

  debito: number;
  credito: number;
  saldoCuenta: number;
}

export interface ExtractoAsociadoResponse {
  resumen: ExtractoAsociadoResumen;
  cuentas: ExtractoAsociadoCuenta[];
  movimientos: ExtractoAsociadoMovimiento[];
}

@Injectable({
  providedIn: 'root'
})
export class ExtractoAsociadoApi {

  private readonly http = inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/extracto_asociado`;

  buscarAsociados(filtros: {
    documento: string;
    nombres: string;
    primerApellido: string;
    segundoApellido: string;
    codigoCuenta: string;
  }): Promise<ExtractoAsociadoBusqueda[]> {

    return firstValueFrom(
      this.http.get<ExtractoAsociadoBusqueda[]>(
        `${this.base}/buscar-asociados`,
        {
          params: {
            documento: filtros.documento || '',
            nombres: filtros.nombres || '',
            primerApellido: filtros.primerApellido || '',
            segundoApellido: filtros.segundoApellido || '',
            codigoCuenta: filtros.codigoCuenta || ''
          }
        }
      )
    );
  }

  consultar(
    request: ExtractoAsociadoRequest
  ): Promise<ExtractoAsociadoResponse> {

    return firstValueFrom(
      this.http.post<ExtractoAsociadoResponse>(
        this.base,
        request
      )
    );
  }

  listarFormasAhorro(): Promise<any[]> {

    return firstValueFrom(
      this.http.get<any[]>(
        `${environment.apiUrl}/depositos/formas-ahorro`
      )
    );
  }

}
