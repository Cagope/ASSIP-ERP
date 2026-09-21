import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { OriginacionSolicitudApi } from '../solicitud/originacion-solicitud.api';
import { FormaPago, ModalidadInteres, TipoCuota } from '../solicitud/originacion-solicitud.models';
import { SolicitudFormalizacionDetalle, SolicitudFormalizacionGuardarRequest } from './originacion-formalizacion.models';

@Injectable({ providedIn: 'root' })
export class OriginacionFormalizacionApi {
  private readonly http = inject(HttpClient);
  private readonly solicitudApi = inject(OriginacionSolicitudApi);
  private readonly baseUrl = `${environment.apiUrl}/cartera/originacion/formalizacion`;

  consultar(id: number): Observable<SolicitudFormalizacionDetalle> {
    return this.http.get<SolicitudFormalizacionDetalle>(`${this.baseUrl}/${id}`);
  }
  guardar(id: number, request: SolicitudFormalizacionGuardarRequest): Observable<SolicitudFormalizacionDetalle> {
    return this.http.put<SolicitudFormalizacionDetalle>(`${this.baseUrl}/${id}`, request);
  }
  finalizar(id: number): Observable<SolicitudFormalizacionDetalle> {
    return this.http.post<SolicitudFormalizacionDetalle>(`${this.baseUrl}/${id}/finalizar`, null);
  }
  listarFormasPago(): Observable<FormaPago[]> { return this.solicitudApi.listarFormasPago(); }
  listarModalidadesInteres(): Observable<ModalidadInteres[]> { return this.solicitudApi.listarModalidadesInteres(); }
  listarTiposCuota(): Observable<TipoCuota[]> { return this.solicitudApi.listarTiposCuota(); }
}
