import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTO — BÚSQUEDA EMPLEADOS NÓMINA
   ========================================================= */

export interface NominaEmpleadoBusquedaDTO {
  idEmpleado: number;
  documento: string;
  nombreCompleto: string;
}

/* =========================================================
   API — BÚSQUEDA NÓMINA
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class NominaBusquedaApi {

  private readonly base =
    `${environment.apiUrl}/nomina/busqueda`;

  constructor(private http: HttpClient) {}

  // =========================================================
  // BUSCAR EMPLEADOS (AUTOCOMPLETE)
  // =========================================================

  buscarEmpleados(
    q: string,
    limit = 20
  ): Observable<NominaEmpleadoBusquedaDTO[]> {

    const params = new HttpParams()
      .set('q', q)
      .set('limit', limit.toString());

    return this.http.get<NominaEmpleadoBusquedaDTO[]>(
      `${this.base}/empleados`,
      { params }
    );
  }

  // =========================================================
  // OBTENER EMPLEADO POR ID (para edición)
  // =========================================================

  buscarEmpleadoPorId(id: number): Observable<NominaEmpleadoBusquedaDTO> {

    return this.http.get<NominaEmpleadoBusquedaDTO>(
      `${this.base}/empleados/${id}`
    );
  }

}
