import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';

/**
 * 📘 Modelo: Parametro
 * ------------------------------------------------------------
 * Representa los parámetros configurables del sistema,
 * asociados a una agencia específica.
 */
export interface Parametro {
  idParametro?: number;
  idAgencia: number;
  codigoParametro: number;
  nombreParametro: string;
  valorParametro: number;
  tipoValor: boolean;
}

@Injectable({ providedIn: 'root' })
export class ParametrosApi {
  private readonly base = `${environment.apiUrl}/general/parametros`;

  constructor(private http: HttpClient) {}

  /** 🔹 Listar todos los parámetros */
  listar(): Observable<Parametro[]> {
    return this.http.get<Parametro[]>(this.base);
  }

  /** 🔹 Obtener un parámetro por ID */
  obtener(id: number): Observable<Parametro> {
    return this.http.get<Parametro>(`${this.base}/${id}`);
  }

  /** 🔹 Crear nuevo parámetro */
  crear(data: Parametro): Observable<Parametro> {
    return this.http.post<Parametro>(this.base, data);
  }

  /** 🔹 Actualizar parámetro existente */
  actualizar(id: number, data: Parametro): Observable<Parametro> {
    return this.http.put<Parametro>(`${this.base}/${id}`, data);
  }

  /** 🔹 Eliminar parámetro */
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  /** 🔹 Obtener parámetro por agencia y código (para cálculos automáticos) */
  obtenerPorAgenciaYCodigo(idAgencia: number, codigo: number): Observable<Parametro> {
    return this.http.get<Parametro>(`${this.base}/buscar/${idAgencia}/${codigo}`);
  }

}
