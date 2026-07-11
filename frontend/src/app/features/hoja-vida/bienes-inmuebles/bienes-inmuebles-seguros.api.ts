import { Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

import {
  BienInmuebleSeguro
} from './bienes-inmuebles-seguros.dto';

@Injectable({
  providedIn: 'root'
})
export class BienesInmueblesSegurosApi {

  private readonly baseUrl =
    `${environment.apiUrl}/hoja-vida/bienes-inmuebles-seguros`;

  constructor(
    private http: HttpClient
  ) {
  }

  listarPorBien(
    idBien: number
  ): Observable<BienInmuebleSeguro[]> {

    return this.http.get<BienInmuebleSeguro[]>(
      `${this.baseUrl}/bien/${idBien}`
    );

  }

  buscarPorId(
    idSeguro: number
  ): Observable<BienInmuebleSeguro> {

    return this.http.get<BienInmuebleSeguro>(
      `${this.baseUrl}/${idSeguro}`
    );

  }

  crear(
    dto: BienInmuebleSeguro
  ): Observable<BienInmuebleSeguro> {

    return this.http.post<BienInmuebleSeguro>(
      this.baseUrl,
      dto
    );

  }

  actualizar(
    idSeguro: number,
    dto: BienInmuebleSeguro
  ): Observable<BienInmuebleSeguro> {

    return this.http.put<BienInmuebleSeguro>(
      `${this.baseUrl}/${idSeguro}`,
      dto
    );

  }

  eliminar(
    idSeguro: number
  ): Observable<void> {

    return this.http.delete<void>(
      `${this.baseUrl}/${idSeguro}`
    );

  }

}
