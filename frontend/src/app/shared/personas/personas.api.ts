import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PersonaBusquedaDTO {
  idDatosPersonal: number;
  documento: string;
  nombreCompleto: string;
  tipoPersona: string;
}

@Injectable({ providedIn: 'root' })
export class PersonasApi {

  private readonly base = `${environment.apiUrl}/shared/personas`;

  constructor(private http: HttpClient) {}

  // AUTOCOMPLETE
  buscar(q: string): Observable<PersonaBusquedaDTO[]> {
    return this.http.get<PersonaBusquedaDTO[]>(
      `${this.base}/buscar`,
      { params: { q } }
    );
  }

  // CARGA POR ID (EDICIÓN)
  obtenerPorId(id: number): Observable<PersonaBusquedaDTO> {
    return this.http.get<PersonaBusquedaDTO>(
      `${this.base}/${id}`
    );
  }
}
