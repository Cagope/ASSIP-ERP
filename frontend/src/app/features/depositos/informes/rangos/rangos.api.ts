import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';

export interface RangoFiltro {
  desde: number;
  hasta: number;
}

export interface RangosRequest {

  tipo: string;

  idAgencia: number;

  codigoForma: string;

  fechaCorte: string;

  rangos: RangoFiltro[];
}

@Injectable({
  providedIn: 'root'
})
export class RangosApi {

  private readonly http =
    inject(HttpClient);

  private readonly base =
    `${environment.apiUrl}/depositos/informes/rangos`;

  consultar(
    body: RangosRequest
  ) {

    return this.http.post<any[]>(
      this.base,
      body
    );
  }

}
