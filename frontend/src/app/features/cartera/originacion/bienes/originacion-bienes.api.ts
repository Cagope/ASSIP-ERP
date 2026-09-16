import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import {
  SolicitudBien,
  SolicitudBienCreditoRespaldado,
  SolicitudBienSeleccionRequest,
  SolicitudBienValidacion
} from './originacion-bienes.models';

@Injectable({ providedIn: 'root' })
export class OriginacionBienesApi {
  private readonly baseUrl =
    `${environment.apiUrl}/cartera/originacion/bienes`;

  constructor(private readonly http: HttpClient) {}

  listarPorSolicitud(idSolicitudCredito: number): Observable<SolicitudBien[]> {
    return this.http.get<SolicitudBien[]>(
      `${this.baseUrl}/solicitud/${idSolicitudCredito}`
    );
  }

  listarPorDeudor(idSolicitudDeudor: number): Observable<SolicitudBien[]> {
    return this.http.get<SolicitudBien[]>(
      `${this.baseUrl}/deudor/${idSolicitudDeudor}`
    );
  }

  listarCreditosRespaldados(idBien: number): Observable<SolicitudBienCreditoRespaldado[]> {
    return this.http.get<SolicitudBienCreditoRespaldado[]>(
      `${this.baseUrl}/${idBien}/creditos-respaldados`
    );
  }

  seleccionar(request: SolicitudBienSeleccionRequest): Observable<SolicitudBien> {
    return this.http.put<SolicitudBien>(
      `${this.baseUrl}/seleccion`,
      request
    );
  }

  validarParaContinuar(idSolicitudCredito: number): Observable<SolicitudBienValidacion> {
    return this.http.get<SolicitudBienValidacion>(
      `${this.baseUrl}/solicitud/${idSolicitudCredito}/validar`
    );
  }
}
