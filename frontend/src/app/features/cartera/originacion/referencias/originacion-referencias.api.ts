import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import {
  ReferenciaCierreRequest, ReferenciaContactoRequest, ReferenciaEntrevistaRequest,
  ReferenciasFiltros, SolicitudReferenciaListado, SolicitudReferenciaParticipante,
  SolicitudReferenciaPersonal
} from './originacion-referencias.models';

@Injectable({ providedIn: 'root' })
export class OriginacionReferenciasApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/cartera/referencias-personales`;

  listar(filtros: ReferenciasFiltros): Observable<SolicitudReferenciaListado[]> {
    let params = new HttpParams();
    for (const [clave, valor] of Object.entries(filtros)) {
      if (valor !== null && valor !== undefined && String(valor).trim() !== '') {
        params = params.set(clave, String(valor).trim());
      }
    }
    return this.http.get<SolicitudReferenciaListado[]>(this.baseUrl, { params });
  }

  consultarSolicitud(id: number): Observable<SolicitudReferenciaListado> {
    return this.http.get<SolicitudReferenciaListado>(`${this.baseUrl}/${id}`);
  }

  listarParticipantes(id: number): Observable<SolicitudReferenciaParticipante[]> {
    return this.http.get<SolicitudReferenciaParticipante[]>(`${this.baseUrl}/${id}/participantes`);
  }

  listarReferencias(id: number): Observable<SolicitudReferenciaPersonal[]> {
    return this.http.get<SolicitudReferenciaPersonal[]>(`${this.baseUrl}/${id}/referencias`);
  }

  iniciar(id: number): Observable<number> {
    return this.http.post<number>(`${this.baseUrl}/${id}/iniciar`, {});
  }

  crear(idSolicitud: number, idDeudor: number, request: ReferenciaContactoRequest): Observable<SolicitudReferenciaPersonal> {
    return this.http.post<SolicitudReferenciaPersonal>(
      `${this.baseUrl}/${idSolicitud}/participantes/${idDeudor}/referencias`, request
    );
  }

  actualizar(idReferencia: number, request: ReferenciaContactoRequest): Observable<SolicitudReferenciaPersonal> {
    return this.http.put<SolicitudReferenciaPersonal>(`${this.baseUrl}/referencias/${idReferencia}`, request);
  }

  entrevista(idReferencia: number, request: ReferenciaEntrevistaRequest): Observable<SolicitudReferenciaPersonal> {
    return this.http.put<SolicitudReferenciaPersonal>(`${this.baseUrl}/referencias/${idReferencia}/entrevista`, request);
  }

  desactivar(idReferencia: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/referencias/${idReferencia}`);
  }

  cerrar(idSolicitud: number, request: ReferenciaCierreRequest): Observable<SolicitudReferenciaListado> {
    return this.http.post<SolicitudReferenciaListado>(`${this.baseUrl}/${idSolicitud}/cerrar`, request);
  }
}
