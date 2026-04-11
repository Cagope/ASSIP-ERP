import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTOs INDIVIDUAL
   ========================================================= */

export interface NovedadNominaFormDTO {
  idEmpleado: number | null;
  idContrato?: number | null;

  codigoConcepto: string | null;

  fechaInicial: string;
  fechaFinal: string;

  cantidad: number;
  valor: number;

  observacion?: string | null;
}

export interface NovedadNominaListDTO {
  idNovedad: number;

  idEmpleado: number;
  idContrato?: number;

  codigoConcepto: string;

  fechaInicial: string;
  fechaFinal: string;

  cantidad: number;
  valor: number;

  estado: string;
  observacion?: string;

  documentoEmpleado?: string;
  nombreEmpleado?: string;
}

/* =========================================================
   DTO MASIVO
   ========================================================= */

export interface NovedadMasivaRequestDTO {
  codigoConcepto: string;
  cantidad: number;
  valor: number;
  observacion?: string | null;
}

export interface NovedadMasivaResultDTO {
  totalContratos: number;
  insertados: number;
  omitidos: number;
}

/* =========================================================
   DTO CALCULO
   ========================================================= */

export interface NovedadCalculoRequestDTO {
  idContrato: number;
  codigoConcepto: string;
  cantidad: number;
}

export interface NovedadCalculoResponseDTO {
  valorCalculado: number;
}

/* =========================================================
   API
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class NovedadesNominaApi {

  private readonly base = `${environment.apiUrl}/nomina/novedades`;

  constructor(private http: HttpClient) {}

  // =========================================================
  // LISTAR (PERÍODO OPERATIVO IMPLÍCITO)
  // =========================================================

  listar(empleado?: number)
    : Observable<NovedadNominaListDTO[]> {

    const url = empleado != null
      ? `${this.base}?empleado=${empleado}`
      : this.base;

    return this.http.get<NovedadNominaListDTO[]>(url);
  }

  obtenerPeriodoActivo(): Observable<any> {
    return this.http.get<any>(`${this.base}/periodo-activo`);
  }

  // =========================================================
  // OBTENER
  // =========================================================

  obtener(id: number): Observable<NovedadNominaFormDTO> {
    return this.http.get<NovedadNominaFormDTO>(`${this.base}/${id}`);
  }

  // =========================================================
  // CREAR INDIVIDUAL
  // =========================================================

  crear(data: NovedadNominaFormDTO): Observable<number> {
    return this.http.post<number>(this.base, data);
  }

  // =========================================================
  // ACTUALIZAR
  // =========================================================

  actualizar(id: number, data: NovedadNominaFormDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}`, data);
  }

  // =========================================================
  // ELIMINAR
  // =========================================================

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  // =========================================================
  // MASIVO
  // =========================================================

  generarMasivo(
    data: NovedadMasivaRequestDTO
  ): Observable<NovedadMasivaResultDTO> {

    return this.http.post<NovedadMasivaResultDTO>(
      `${this.base}/masivo`,
      data
    );
  }

  // =========================================================
  // 🔍 PREVIEW MASIVO
  // =========================================================

  previewMasivo(
    data: NovedadMasivaRequestDTO
  ): Observable<any[]> {

    return this.http.post<any[]>(
      `${this.base}/masivo/preview`,
      data
    );
  }

  // =========================================================
  // CALCULAR (PREVIEW INDIVIDUAL)
  // =========================================================

  calcular(
    data: NovedadCalculoRequestDTO
  ): Observable<NovedadCalculoResponseDTO> {

    return this.http.post<NovedadCalculoResponseDTO>(
      `${this.base}/calcular`,
      data
    );
  }

}
