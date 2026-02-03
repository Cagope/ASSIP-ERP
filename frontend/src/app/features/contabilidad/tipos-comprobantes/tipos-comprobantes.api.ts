import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface TipoComprobante {
  tipoComprobante: string;
  idAgencia: number;
  nombreTipoComprobante: string;
  cscComprobante: number;
  comprobanteActivo: boolean;
  fkSeguridadEdicion?: number;
  fkSeguridadCreacion?: number;
}

@Injectable({ providedIn: 'root' })
export class TiposComprobantesApi {

  private readonly base =
    `${environment.apiUrl}/contabilidad/tipos-comprobantes`;

  constructor(private http: HttpClient) {}

  // =====================================================
  // CRUD ADMIN
  // → Lista TODO (todas las agencias, activos + inactivos)
  // =====================================================
  listarTodos(): Observable<TipoComprobante[]> {
    return this.http.get<TipoComprobante[]>(this.base);
  }

  // =====================================================
  // PROCESOS (INGRESO ACTIVOS, ETC)
  // → Lista SOLO por agencia
  // =====================================================
  listarPorAgencia(idAgencia: number): Observable<TipoComprobante[]> {

    const params = new HttpParams()
      .set('idAgencia', idAgencia.toString());

    return this.http.get<TipoComprobante[]>(
      `${this.base}/por-agencia`,
      { params }
    );
  }

  // =====================================================
  // OBTENER (EDITAR)
  // =====================================================
  obtener(
    tipo: string,
    idAgencia: number
  ): Observable<TipoComprobante> {

    return this.http.get<TipoComprobante>(
      `${this.base}/${tipo}/${idAgencia}`
    );
  }

  // =====================================================
  // CREAR
  // =====================================================
  crear(payload: TipoComprobante): Observable<void> {
    return this.http.post<void>(this.base, payload);
  }

  // =====================================================
  // ACTUALIZAR
  // =====================================================
  actualizar(payload: TipoComprobante): Observable<void> {
    return this.http.put<void>(this.base, payload);
  }

  // =====================================================
  // CAMBIAR ESTADO (ACTIVO / INACTIVO)
  // =====================================================
  cambiarEstado(
    tipo: string,
    idAgencia: number,
    activo: boolean
  ): Observable<void> {

    return this.http.patch<void>(
      `${this.base}/${tipo}/${idAgencia}/estado`,
      { activo }
    );
  }
}
