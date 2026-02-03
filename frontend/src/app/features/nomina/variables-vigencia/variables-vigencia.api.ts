import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTOs
   ========================================================= */

export interface VariablesVigenciaListDTO {
  idVariable: number;

  fechaInicial: string; // yyyy-MM-dd
  fechaFinal: string;   // yyyy-MM-dd

  smmlv: number;
  auxTransporte: number;

  activo: boolean;
}

export interface VariablesVigenciaFormDTO {
  idVariable?: number;

  fechaInicial: string;
  fechaFinal: string;

  smmlv: number;
  auxTransporte: number;

  porcSaludEmpleado: number;
  porcSaludEmpleador: number;

  porcPensionEmpleado: number;
  porcPensionEmpleador: number;

  porcCajaCompensacion: number;
  porcSena: number;
  porcIcbf: number;

  porProvisionPrima: number;
  porProvisionVacaciones: number;
  porProvisionCesantias: number;
  porProvisionInteresCesantias: number;

  topeIbcMinSmmlv: number;
  topeIbcMaxSmmlv: number;

  exoneradoSalud: boolean;
  exoneradoParafiscales: boolean;

  activo: boolean;
}

export interface VariablesVigenciaSaveDTO extends VariablesVigenciaFormDTO {}

/* =========================================================
   API
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class VariablesVigenciaApi {

  // ✔ Ruta alineada al backend
  private readonly base = `${environment.apiUrl}/nomina/variables-vigencia`;

  constructor(private http: HttpClient) {}

  // =========================================================
  // LISTAR
  // =========================================================
  listar(): Observable<VariablesVigenciaListDTO[]> {
    return this.http.get<VariablesVigenciaListDTO[]>(this.base);
  }

  // =========================================================
  // OBTENER (FORM)
  // =========================================================
  obtener(id: number): Observable<VariablesVigenciaFormDTO> {
    return this.http.get<VariablesVigenciaFormDTO>(`${this.base}/${id}`);
  }

  // =========================================================
  // CREAR
  // =========================================================
  crear(data: VariablesVigenciaSaveDTO): Observable<void> {
    return this.http.post<void>(this.base, data);
  }

  // =========================================================
  // ACTUALIZAR
  // =========================================================
  actualizar(id: number, data: VariablesVigenciaSaveDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}`, data);
  }

  // =========================================================
  // ELIMINAR
  // =========================================================
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
