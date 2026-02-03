import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';

/**
 * 📘 Modelo: PlanCuenta
 * ------------------------------------------------------------
 * Representa una cuenta del catálogo contable para una agencia.
 */
export interface PlanCuenta {
  id?: number;                    // ← nombre correcto según backend
  idAgencia: number;
  codigoCuenta: string;
  nombre: string;
  naturaleza: string;             // D / C
  nivel?: number;                 // lo calcula el backend
  operable: boolean;
  controlEntradaSalida: boolean;
  tipoEspecial?: number | null;   // nombre corregido
}

@Injectable({ providedIn: 'root' })
export class PlanCuentasApi {

  private readonly base = `${environment.apiUrl}/contabilidad/plan-cuentas`;

  constructor(private http: HttpClient) {}

  listar(idAgencia: number): Observable<PlanCuenta[]> {
    return this.http.get<PlanCuenta[]>(`${this.base}/agencia/${idAgencia}`);
  }

  obtener(id: number): Observable<PlanCuenta> {
    return this.http.get<PlanCuenta>(`${this.base}/${id}`);
  }

  crear(data: PlanCuenta): Observable<PlanCuenta> {
    return this.http.post<PlanCuenta>(this.base, data);
  }

  actualizar(id: number, data: PlanCuenta): Observable<PlanCuenta> {
    return this.http.put<PlanCuenta>(`${this.base}/${id}`, data);
  }

  eliminar(id: number, idAgencia: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}?idAgencia=${idAgencia}`);
  }

  // 🔥 NUEVO
  buscar(idAgencia: number, texto: string): Observable<PlanCuenta[]> {
    return this.http.get<PlanCuenta[]>(`${this.base}/buscar`, {
      params: {
        idAgencia,
        q: texto
      }
    });
  }
}
