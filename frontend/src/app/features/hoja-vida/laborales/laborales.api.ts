import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Laboral } from '../../../shared/models/laboral.model';

@Injectable({ providedIn: 'root' })
export class LaboralesApi {
  private readonly base = `${environment.apiUrl}/hoja-vida/laborales`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Laboral[]> {
    return this.http.get<Laboral[]>(this.base);
  }

  obtener(id: number): Observable<Laboral> {
    return this.http.get<Laboral>(`${this.base}/${id}`);
  }

  crear(data: Laboral): Observable<Laboral> {
    return this.http.post<Laboral>(this.base, data);
  }

  actualizar(id: number, data: Laboral): Observable<Laboral> {
    return this.http.put<Laboral>(`${this.base}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  obtenerPorPersona(idDatosPersonal: number): Observable<Laboral> {
    return this.http.get<Laboral>(`${this.base}/persona/${idDatosPersonal}`);
  }
}
