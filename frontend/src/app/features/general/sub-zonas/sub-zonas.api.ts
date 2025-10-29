import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment'; // ✅ ruta corregida
import { Observable } from 'rxjs';

export interface SubZona {
  idSubZona?: number;
  codigoSubZona: string;
  nombreSubZona: string;
  comentarioSubZona?: string;
  zona?: { idZona: number; nombreZona?: string }; // ✅ nombreZona opcional
}

@Injectable({ providedIn: 'root' })
export class SubZonasApi {
  private readonly base = `${environment.apiUrl}/general/sub-zonas`;

  constructor(private http: HttpClient) {}

  listar(): Observable<SubZona[]> {
    return this.http.get<SubZona[]>(this.base);
  }
  obtener(id: number): Observable<SubZona> {
    return this.http.get<SubZona>(`${this.base}/${id}`);
  }
  crear(data: SubZona): Observable<SubZona> {
    return this.http.post<SubZona>(this.base, data);
  }
  actualizar(id: number, data: SubZona): Observable<SubZona> {
    return this.http.put<SubZona>(`${this.base}/${id}`, data);
  }
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
