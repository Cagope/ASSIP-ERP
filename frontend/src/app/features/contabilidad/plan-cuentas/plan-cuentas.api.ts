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

  /** 🔹 Listar todas las cuentas de una agencia */
  listar(idAgencia: number): Observable<PlanCuenta[]> {
    return this.http.get<PlanCuenta[]>(`${this.base}/agencia/${idAgencia}`);
  }

  /** 🔹 Obtener una cuenta por ID */
  obtener(id: number): Observable<PlanCuenta> {
    return this.http.get<PlanCuenta>(`${this.base}/${id}`);
  }

  /** 🔹 Crear una nueva cuenta */
  crear(data: PlanCuenta): Observable<PlanCuenta> {
    return this.http.post<PlanCuenta>(this.base, data);
  }

  /** 🔹 Actualizar cuenta existente */
  actualizar(id: number, data: PlanCuenta): Observable<PlanCuenta> {
    return this.http.put<PlanCuenta>(`${this.base}/${id}`, data);
  }

  /** 🔹 Eliminar una cuenta (requiere idAgencia para validar) */
  eliminar(id: number, idAgencia: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}?idAgencia=${idAgencia}`);
  }
}
