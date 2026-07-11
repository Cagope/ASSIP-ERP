import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { BienMaquinaria } from './bienes-maquinaria.dto';

@Injectable({
  providedIn: 'root'
})
export class BienesMaquinariaApi {

  private readonly baseUrl =
    `${environment.apiUrl}/hoja-vida/bienes-maquinaria`;

  constructor(private http: HttpClient) {}

  listarPorPersona(idDatosPersonal: number): Observable<BienMaquinaria[]> {
    return this.http.get<BienMaquinaria[]>(
      `${this.baseUrl}/persona/${idDatosPersonal}`
    );
  }

  buscarPorIdBien(idBien: number): Observable<BienMaquinaria> {
    return this.http.get<BienMaquinaria>(
      `${this.baseUrl}/${idBien}`
    );
  }

  crear(dto: BienMaquinaria): Observable<BienMaquinaria> {
    return this.http.post<BienMaquinaria>(
      this.baseUrl,
      dto
    );
  }

  actualizar(idBien: number, dto: BienMaquinaria): Observable<BienMaquinaria> {
    return this.http.put<BienMaquinaria>(
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
