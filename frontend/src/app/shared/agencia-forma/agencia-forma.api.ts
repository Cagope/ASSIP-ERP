import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { catchError, map, of } from 'rxjs';

/**
 * DTO de Agencia (backend /general/agencias)
 */
export interface AgenciaDTO {
  idAgencia: number;
  nombreAgencia: string;
}

/**
 * DTO de Forma filtrada por Agencia
 */
export interface FormaAhorroDTO {
  idFormaAhorro: number;
  codigoForma: string;
  nombreForma: string;
}

@Injectable({ providedIn: 'root' })
export class AgenciaFormaApi {
  private readonly http = inject(HttpClient);

  // BASE real de agencias
  private readonly baseGeneral = `${environment.apiUrl}/general`;

  // =====================================================
  // 🏦 AGENCIAS
  // =====================================================
  listarAgencias() {
    return this.http.get<any[]>(`${this.baseGeneral}/agencias`).pipe(
      map(items =>
        items.map(item => ({
          idAgencia: item.idAgencia ?? item.id_agencia,
          nombreAgencia: item.nombreAgencia ?? item.nombre_agencia
        }))
      ),
      catchError(() => of([] as AgenciaDTO[]))
    );
  }

  // =====================================================
  // 🔢 FORMAS POR AGENCIA (CORREGIDO)
  // =====================================================
  listarFormasPorAgencia(idAgencia: number) {
    return this.http
      .get<any[]>(`${environment.apiUrl}/depositos/formasagencias/${idAgencia}`)
      .pipe(
        map(items =>
          items.map(item => ({
            idFormaAhorro: item.idFormaAhorro ?? item.id_forma_ahorro,
            codigoForma: item.codigoForma ?? item.codigo_forma,
            nombreForma: item.nombreForma ?? item.nombre_forma
          }))
        ),
        catchError(() => of([] as FormaAhorroDTO[]))
      );
  }
}
