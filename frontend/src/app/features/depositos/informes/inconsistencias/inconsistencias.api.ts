import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';

export interface InconsistenciasRequest {
  agencia: string;
  fechaCorte: string;
}

@Injectable({ providedIn: 'root' })
export class InconsistenciasApi {

  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/depositos/informes/inconsistencias`;

  consultar(filtros: InconsistenciasRequest) {
    return this.http.post<any[]>(this.base, filtros);
  }
}
