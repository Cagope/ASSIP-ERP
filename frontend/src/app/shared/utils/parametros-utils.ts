import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable, catchError, map, of } from 'rxjs';

/**
 * 🧮 Utilidad global — Parámetros del sistema
 * ------------------------------------------------------------
 * Permite obtener el valor de un parámetro configurado
 * en la tabla general.parametros, filtrado por agencia y código.
 *
 * Uso:
 *   this.parametrosUtils.obtenerValor(1, 103).subscribe(valor => { ... });
 */
@Injectable({ providedIn: 'root' })
export class ParametrosUtils {
  private readonly base = `${environment.apiUrl}/general/parametros`;

  constructor(private http: HttpClient) {}

  /**
   * 🔹 Retorna el valor del parámetro solicitado.
   * Si no existe, devuelve 0.
   */
  obtenerValor(idAgencia: number, codigo: number): Observable<number> {
    const url = `${this.base}/buscar/${idAgencia}/${codigo}`;
    return this.http.get<any>(url).pipe(
      map((res) => {
        if (!res || res.valorParametro == null) return 0;
        const val = Number(res.valorParametro);
        return isNaN(val) ? 0 : val;
      }),
      catchError(() => of(0))
    );
  }
}
