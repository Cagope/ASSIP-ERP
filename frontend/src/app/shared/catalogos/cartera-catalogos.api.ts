import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CarteraCatalogo } from './cartera-catalogos.models';

@Injectable({
  providedIn: 'root'
})
export class CarteraCatalogosApi {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/cartera/catalogos`;

  // =========================================================
  // LÍNEAS DE CRÉDITO
  // =========================================================

  listarLineasCredito(): Observable<CarteraCatalogo[]> {
    return this.http.get<CarteraCatalogo[]>(
      `${this.baseUrl}/lineas-credito`
    );
  }

  // =========================================================
  // EDADES DE RIESGO
  // =========================================================

  listarEdadesRiesgo(): Observable<CarteraCatalogo[]> {
    return this.http.get<CarteraCatalogo[]>(
      `${this.baseUrl}/edades-riesgo`
    );
  }

  // =========================================================
  // CLASIFICACIONES DE CRÉDITO
  // =========================================================

  listarClasificacionesCredito(): Observable<CarteraCatalogo[]> {
    return this.http.get<CarteraCatalogo[]>(
      `${this.baseUrl}/clasificaciones-credito`
    );
  }

  // =========================================================
  // GARANTÍAS DE CRÉDITO
  // =========================================================

  listarGarantiasCredito(): Observable<CarteraCatalogo[]> {
    return this.http.get<CarteraCatalogo[]>(
      `${this.baseUrl}/garantias-credito`
    );
  }

  // =========================================================
  // ESTADOS DE CARTERA
  // =========================================================

  listarEstadosCartera(): Observable<CarteraCatalogo[]> {
    return this.http.get<CarteraCatalogo[]>(
      `${this.baseUrl}/estados-cartera`
    );
  }

  // =========================================================
  // ESTADOS JURÍDICOS
  // =========================================================

  listarEstadosJuridicos(): Observable<CarteraCatalogo[]> {
    return this.http.get<CarteraCatalogo[]>(
      `${this.baseUrl}/estados-juridicos`
    );
  }

  // =========================================================
  // FORMAS DE PAGO
  // =========================================================

  listarFormasPago(): Observable<CarteraCatalogo[]> {
    return this.http.get<CarteraCatalogo[]>(
      `${this.baseUrl}/formas-pago`
    );
  }

}
