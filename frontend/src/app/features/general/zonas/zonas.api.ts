import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';

export interface Zona {
  idZona?: number;
  codigoZona: string;
  nombreZona: string;
  comentarioZona?: string;
}

@Injectable({ providedIn: 'root' })
export class ZonasApi {
  private base = `${environment.apiUrl}/general/zonas`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Zona[]> {
    return this.http.get<Zona[]>(this.base);
  }

  obtener(id: number): Observable<Zona> {
    return this.http.get<Zona>(`${this.base}/${id}`);
  }

  crear(data: Zona): Observable<Zona> {
    return this.http.post<Zona>(this.base, data);
  }

  actualizar(id: number, data: Zona): Observable<Zona> {
    return this.http.put<Zona>(`${this.base}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
