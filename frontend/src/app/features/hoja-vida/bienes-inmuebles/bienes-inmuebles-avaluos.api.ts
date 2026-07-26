import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { BienInmuebleAvaluo } from './bienes-inmuebles-avaluos.dto';

@Injectable({
  providedIn: 'root'
})
export class BienesInmueblesAvaluosApi {

  private readonly baseUrl =
    `${environment.apiUrl}/hoja-vida/bienes-inmuebles-avaluos`;

  constructor(
    private readonly http: HttpClient
  ) {}

  listarPorBien(
    idBien: number
  ): Observable<BienInmuebleAvaluo[]> {

    return this.http.get<BienInmuebleAvaluo[]>(
      `${this.baseUrl}/bien/${idBien}`
    );
  }

  buscarPorId(
    idAvaluo: number
  ): Observable<BienInmuebleAvaluo> {

    return this.http.get<BienInmuebleAvaluo>(
      `${this.baseUrl}/${idAvaluo}`
    );
  }

  crear(
    dto: BienInmuebleAvaluo
  ): Observable<BienInmuebleAvaluo> {

    return this.http.post<BienInmuebleAvaluo>(
      this.baseUrl,
      dto
    );
  }

  actualizar(
    idAvaluo: number,
    dto: BienInmuebleAvaluo
  ): Observable<BienInmuebleAvaluo> {

    return this.http.put<BienInmuebleAvaluo>(
      `${this.baseUrl}/${idAvaluo}`,
      dto
    );
  }

  eliminar(
    idAvaluo: number
  ): Observable<void> {

    return this.http.delete<void>(
      `${this.baseUrl}/${idAvaluo}`
    );
  }
}
