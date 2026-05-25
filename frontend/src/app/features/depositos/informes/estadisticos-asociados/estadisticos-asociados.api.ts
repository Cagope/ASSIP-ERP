import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface EstadisticosAsociadosRequest {
  fechaCorte: string;
  idAgencia: number;
  codigoForma: string;
}

export interface EstadisticosAsociadosResumen {
  totalAsociados: number;
  totalAportes: number;
}

export interface EstadisticosAsociadosItem {
  grupo: string;
  categoria: string;
  cantidad: number;
  saldoTotal: number;
  porcentaje: number;
}

export interface EstadisticosAsociadosDetalle {
  grupo: string;
  categoria: string;
  documento: string;
  nombreCompleto: string;
  nombreAgencia: string;
  codigoForma: string;
  nombreForma: string;
  ciudad: string;
  celular: string;
  correo: string;
  saldoAportes: number;
  genero: string;
  estadoCivil: string;
  cabezaFamilia: string;
  escolaridad: string;
  tipoVivienda: string;
  ocupacion: string;
  sectorEconomico: string;
}

export interface EstadisticosAsociadosResponse {
  resumen: EstadisticosAsociadosResumen;
  items: EstadisticosAsociadosItem[];
  detalle: EstadisticosAsociadosDetalle[];
}

@Injectable({
  providedIn: 'root'
})
export class EstadisticosAsociadosApi {

  private readonly http = inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/estadisticos-asociados`;

  consultar(
    filtros: EstadisticosAsociadosRequest
  ): Promise<EstadisticosAsociadosResponse> {

    return firstValueFrom(
      this.http.post<EstadisticosAsociadosResponse>(
        this.base,
        filtros
      )
    );
  }

  listarFormasAhorro() {
    return this.http
      .get<any[]>(
        `${environment.apiUrl}/depositos/formas-ahorro`
      )
      .toPromise();
  }

}
