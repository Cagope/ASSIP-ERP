import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';

export interface RangoFiltro {
  desde: number;
  hasta: number;
}

export interface RangosRequest {
  tipo: string;
  agencia: string;
  fechaCorte: string;
  rangos: RangoFiltro[];
}

@Injectable({ providedIn: 'root' })
export class RangosApi {

  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/depositos/informes/rangos`;

  consultar(body: RangosRequest) {
    return this.http.post<any[]>(this.base, body);
  }
}
