import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { BienInversion } from './bienes-inversiones.dto';

@Injectable({
  providedIn: 'root'
})
export class BienesInversionesApi {

  private readonly baseUrl =
    `${environment.apiUrl}/hoja-vida/bienes-inversiones`;

  constructor(
    private http: HttpClient
  ) {}

  listarPorPersona(
    idDatosPersonal: number
  ): Observable<BienInversion[]> {

    return this.http.get<BienInversion[]>(
      `${this.baseUrl}/persona/${idDatosPersonal}`
    );

  }

  buscarPorIdBien(
    idBien: number
  ): Observable<BienInversion> {

    return this.http.get<BienInversion>(
      `${this.baseUrl}/${idBien}`
    );

  }

  crear(
    dto: BienInversion
  ): Observable<BienInversion> {

    return this.http.post<BienInversion>(
      this.baseUrl,
      dto
    );

  }

  actualizar(
    idBien: number,
    dto: BienInversion
  ): Observable<BienInversion> {

    return this.http.put<BienInversion>(
      `${this.baseUrl}/${idBien}`,
      dto
    );

  }

  eliminar(
    idBien: number
  ): Observable<void> {

    return this.http.delete<void>(
      `${this.baseUrl}/${idBien}`
    );

  }

}
