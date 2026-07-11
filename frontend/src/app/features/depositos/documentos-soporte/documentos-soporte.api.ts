import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment }
  from '../../../../environments/environment';

export interface DocumentosSoporteBusqueda {

  idAgencia: number | null;

  documento: string;

  nombres: string;

  primerApellido: string;

  segundoApellido: string;

}

export interface DocumentosSoporteCuenta {
  idCuentaAhorro: number;

  codigoCuenta: string;

  documento: string;

  nombreCompleto: string;

  nombreForma: string;

  documentoForma: string;

  descripcionSoporte: string;

  cantidadSoporte: number;

  numeroInicialDocumento: string | null;

  numeroFinalDocumento: string | null;

  fechaEntregaDocumento: string | null;

  estadoDocumento: string | null;

  saldoActual: number;

  estadoCuenta: string;

  tieneDocumentoActivo: boolean;
}

export interface DocumentosSoporteActivo {
  idDocumentoSoporte: number;

  tipoDocumentoSoporte: string;

  numeroInicial: string;

  numeroFinal: string;

  fechaEntrega: string;

  estadoDocumento: string;

  fechaEstado: string;
}

export interface DocumentosSoporteHistorico {
  idDocumentoSoporte: number;

  tipoDocumentoSoporte: string;

  numeroInicial: string;

  numeroFinal: string;

  estadoDocumento: string;

  fechaEntrega: string;

  fechaEstado: string;

  usuarioCreacion: number;

  fechaCreacion: string;
}

export interface DocumentosSoporteGuardar {
  accion: string;

  idCuentaAhorro: number;

  numeroInicial: string;

  fechaEntrega: string;
}

export interface DocumentosSoporteResponse {
  success: boolean;

  message: string;

  idDocumentoSoporte: number;
}

@Injectable({
  providedIn: 'root'
})
export class DocumentosSoporteApi {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/depositos/documentos-soporte`;

  buscarCuentas(
    filtros: DocumentosSoporteBusqueda
  ) {

    return firstValueFrom(
      this.http.post<DocumentosSoporteCuenta[]>(
        `${this.baseUrl}/buscar-cuentas`,
        filtros
      )
    );

  }

  obtenerActivo(
    idCuentaAhorro: number
  ) {

    return firstValueFrom(
      this.http.get<DocumentosSoporteActivo | null>(
        `${this.baseUrl}/activo/${idCuentaAhorro}`
      )
    );

  }

  obtenerHistorico(
    idCuentaAhorro: number
  ) {

    return firstValueFrom(
      this.http.get<DocumentosSoporteHistorico[]>(
        `${this.baseUrl}/historico/${idCuentaAhorro}`
      )
    );

  }

  guardar(
    dto: DocumentosSoporteGuardar
  ) {

    return firstValueFrom(
      this.http.post<DocumentosSoporteResponse>(
        `${this.baseUrl}/guardar`,
        dto
      )
    );

  }

}
