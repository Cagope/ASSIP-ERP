import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface CesantiasDTO {
  idCesantias?: number;
  nombreCesantias: string;
  idDatosPersonal?: number | null;
  documento?: string;   // 👈 necesario para exportar
  activo: boolean;
}


@Injectable({ providedIn: 'root' })
export class CesantiasApi {

  private readonly base = `${environment.apiUrl}/nomina/cesantias`;

  constructor(private http: HttpClient) {}

  listar(): Observable<CesantiasDTO[]> {
    return this.http.get<CesantiasDTO[]>(this.base);
  }

  obtener(id: number): Observable<CesantiasDTO> {
    return this.http.get<CesantiasDTO>(`${this.base}/${id}`);
  }

  crear(data: CesantiasDTO): Observable<void> {
    return this.http.post<void>(this.base, data);
  }

  actualizar(id: number, data: CesantiasDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
