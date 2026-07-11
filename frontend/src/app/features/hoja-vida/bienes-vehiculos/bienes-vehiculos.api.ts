import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { BienVehiculo } from './bienes-vehiculos.dto';

@Injectable({
  providedIn: 'root'
})
export class BienesVehiculosApi {

  private readonly baseUrl =
    `${environment.apiUrl}/hoja-vida/bienes-vehiculos`;

  constructor(private http: HttpClient) {}

  listarPorPersona(idDatosPersonal: number): Observable<BienVehiculo[]> {
    return this.http.get<BienVehiculo[]>(
      `${this.baseUrl}/persona/${idDatosPersonal}`
    );
  }

  buscarPorIdBien(idBien: number): Observable<BienVehiculo> {
    return this.http.get<BienVehiculo>(
      `${this.baseUrl}/${idBien}`
    );
  }

  crear(dto: BienVehiculo): Observable<BienVehiculo> {
    return this.http.post<BienVehiculo>(
      this.baseUrl,
      dto
    );
  }

  actualizar(idBien: number, dto: BienVehiculo): Observable<BienVehiculo> {
    return this.http.put<BienVehiculo>(
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
