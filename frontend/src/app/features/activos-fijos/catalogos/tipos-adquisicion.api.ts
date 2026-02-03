import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface TipoAdquisicionDTO {
  idTipoAdquisicion: number;
  nombreTipoAdquisicion: string;
}

@Injectable({ providedIn: 'root' })
export class TiposAdquisicionApi {

  private readonly base =
    `${environment.apiUrl}/activos-fijos/tipos-adquisicion`;

  constructor(private http: HttpClient) {}

  listar(): Observable<TipoAdquisicionDTO[]> {
    return this.http.get<TipoAdquisicionDTO[]>(this.base);
  }
}
