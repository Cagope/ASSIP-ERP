import { Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

import {
  BienVehiculoSeguro
} from './bienes-vehiculos-seguros.dto';

@Injectable({
  providedIn: 'root'
})
export class BienesVehiculosSegurosApi {

  private readonly baseUrl =
    `${environment.apiUrl}/hoja-vida/bienes-vehiculos-seguros`;

  constructor(
    private http: HttpClient
  ) {
  }

  listarPorBien(
    idBien: number
  ): Observable<BienVehiculoSeguro[]> {

    return this.http.get<BienVehiculoSeguro[]>(
      `${this.baseUrl}/bien/${idBien}`
    );

  }

  buscarPorId(
    idSeguro: number
  ): Observable<BienVehiculoSeguro> {

    return this.http.get<BienVehiculoSeguro>(
      `${this.baseUrl}/${idSeguro}`
    );

  }

  crear(
    dto: BienVehiculoSeguro
  ): Observable<BienVehiculoSeguro> {

    return this.http.post<BienVehiculoSeguro>(
      this.baseUrl,
      dto
    );

  }

  actualizar(
    idSeguro: number,
    dto: BienVehiculoSeguro
  ): Observable<BienVehiculoSeguro> {

    return this.http.put<BienVehiculoSeguro>(
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
