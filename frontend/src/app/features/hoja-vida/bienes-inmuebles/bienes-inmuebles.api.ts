import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { BienInmueble } from './bienes-inmuebles.dto';

@Injectable({
  providedIn: 'root'
})
export class BienesInmueblesApi {

  private readonly baseUrl =
    `${environment.apiUrl}/hoja-vida/bienes-inmuebles`;

  constructor(private http: HttpClient) {}

  listarPorPersona(idDatosPersonal: number): Observable<BienInmueble[]> {
    return this.http.get<BienInmueble[]>(
      `${this.baseUrl}/persona/${idDatosPersonal}`
    );
  }

  buscarPorIdBien(idBien: number): Observable<BienInmueble> {
    return this.http.get<BienInmueble>(
      `${this.baseUrl}/${idBien}`
    );
  }

  crear(dto: BienInmueble): Observable<BienInmueble> {
    return this.http.post<BienInmueble>(
      this.baseUrl,
      dto
    );
  }

  actualizar(idBien: number, dto: BienInmueble): Observable<BienInmueble> {
    return this.http.put<BienInmueble>(
      `${this.baseUrl}/${idBien}`,
      dto
    );
  }

  eliminar(idBien: number): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/${idBien}`
    );
  }
}
