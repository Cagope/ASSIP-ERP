import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface ExtractoCuentaBusqueda {
  idCuentaAhorro: number;
  codigoCuenta: string;
  documento: string;
  nombreCompleto: string;
  codigoForma: string;
  nombreForma: string;
  nombreAgencia: string;
  saldoActual: number;
}

export interface ExtractoCuentaRequest {
  idCuentaAhorro: number;
  fechaInicial: string;
  fechaFinal: string;
}

export interface ExtractoCuentaResumen {

  // EMPRESA
  razonSocial: string;
  siglaEmpresa: string;

  documentoEmpresa: string;
  digitoVerificacion: string;

  telefonoEmpresa: string;
  celularEmpresa: string;

  sitioWebEmpresa: string;

  logoUrl: string;

  // CUENTA
  idCuentaAhorro: number;

  codigoCuenta: string;

  // ASOCIADO
  documento: string;

  nombreCompleto: string;

  direccion: string;

  // DEPÓSITO
  codigoForma: string;
  nombreForma: string;

  // AGENCIA
  nombreAgencia: string;

  // RESUMEN
  saldoInicial: number;

  totalCreditos: number;
  totalDebitos: number;

  saldoFinal: number;

}

export interface ExtractoCuentaMovimiento {
  fechaMovimiento: string;
  horaMovimiento: string;
  tipoMovimiento: string;
  descripcionMovimiento: string;
  tipoComprobante: string;
  numeroComprobante: string;
  debito: number;
  credito: number;
  saldo: number;
}

export interface ExtractoCuentaResponse {
  resumen: ExtractoCuentaResumen;
  movimientos: ExtractoCuentaMovimiento[];
}

@Injectable({
  providedIn: 'root'
})
export class ExtractoCuentaApi {

  private readonly http = inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/extracto_cuenta`;

  buscarCuentas(filtros: {
    documento: string;
    nombres: string;
    primerApellido: string;
    segundoApellido: string;
    codigoCuenta: string;
  }): Promise<ExtractoCuentaBusqueda[]> {

    return firstValueFrom(
      this.http.get<ExtractoCuentaBusqueda[]>(
        `${this.base}/buscar-cuentas`,
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
    request: ExtractoCuentaRequest
  ): Promise<ExtractoCuentaResponse> {

    return firstValueFrom(
      this.http.post<ExtractoCuentaResponse>(
        this.base,
        request
      )
    );
  }

  generarPdf(
    request: ExtractoCuentaRequest
  ): Promise<Blob> {

    return firstValueFrom(
      this.http.post(
        `${this.base}/pdf`,
        request,
        {
          responseType: 'blob'
        }
      )
    );
  }

}
