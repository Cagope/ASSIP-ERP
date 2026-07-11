import { Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

import {
  BienMaquinariaSeguro
} from './bienes-maquinaria-seguros.dto';

@Injectable({
  providedIn: 'root'
})
export class BienesMaquinariaSegurosApi {

  private readonly baseUrl =
    `${environment.apiUrl}/hoja-vida/bienes-maquinaria-seguros`;

  constructor(
    private http: HttpClient
  ) {
  }

  listarPorBien(
    idBien: number
  ): Observable<BienMaquinariaSeguro[]> {

    return this.http.get<BienMaquinariaSeguro[]>(
      `${this.baseUrl}/bien/${idBien}`
    );

  }

  buscarPorId(
    idSeguro: number
  ): Observable<BienMaquinariaSeguro> {

    return this.http.get<BienMaquinariaSeguro>(
      `${this.baseUrl}/${idSeguro}`
    );

  }

  crear(
    dto: BienMaquinariaSeguro
  ): Observable<BienMaquinariaSeguro> {

    return this.http.post<BienMaquinariaSeguro>(
      this.baseUrl,
      dto
    );

  }

  actualizar(
    idSeguro: number,
    dto: BienMaquinariaSeguro
  ): Observable<BienMaquinariaSeguro> {

    return this.http.put<BienMaquinariaSeguro>(
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
