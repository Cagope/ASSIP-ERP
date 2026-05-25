import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface CumpleaniosAsociadosRequest {
  fechaInicial: string;
  fechaFinal: string;
  idAgencia: number;
}

export interface CumpleaniosAsociadosResumen {
  totalAsociados: number;
  rangoFechas: string;
}

export interface CumpleaniosAsociadosItem {
  idAgencia: number;
  nombreAgencia: string;
  documento: string;
  nombreCompleto: string;
  fechaNacimiento: string;
  diaCumpleanios: number;
  edad: number;
  ciudad: string;
  celular: string;
  correo: string;
  saldoAportes: number;
}

export interface CumpleaniosAsociadosResponse {
  resumen: CumpleaniosAsociadosResumen;
  items: CumpleaniosAsociadosItem[];
}

@Injectable({
  providedIn: 'root'
})
export class CumpleaniosAsociadosApi {

  private readonly http = inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/cumpleanios-asociados`;

  consultar(
    filtros: CumpleaniosAsociadosRequest
  ): Promise<CumpleaniosAsociadosResponse> {

    return firstValueFrom(
      this.http.post<CumpleaniosAsociadosResponse>(
        this.base,
        filtros
      )
    );
  }

}
