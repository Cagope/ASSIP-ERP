import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface EstadoActivoDTO {
  idEstadoActivo: number;
  nombreEstadoActivo: string;
}

@Injectable({ providedIn: 'root' })
export class EstadosActivoApi {

  private readonly base =
    `${environment.apiUrl}/activos-fijos/estados-activo`;

  constructor(private http: HttpClient) {}

  listar(): Observable<EstadoActivoDTO[]> {
    return this.http.get<EstadoActivoDTO[]>(this.base);
  }
}
