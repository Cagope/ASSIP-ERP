import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PersonaBusquedaDTO } from './personas-busqueda.dto';

@Injectable({ providedIn: 'root' })
export class PersonasBusquedaApi {

  private readonly base =
    `${environment.apiUrl}/shared/personas`;

  constructor(private http: HttpClient) {}

  buscar(q: string): Observable<PersonaBusquedaDTO[]> {
    return this.http.get<PersonaBusquedaDTO[]>(
      `${this.base}/buscar`,
      { params: { q } }
    );
  }
}
