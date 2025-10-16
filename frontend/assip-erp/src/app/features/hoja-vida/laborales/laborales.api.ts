// src/app/features/hoja-vida/laborales/laborales.api.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, catchError, map, of } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface LaboralDetail {
  idLaboral: number;
  idDatosPersonal: number;

  nombreEmpresa: string;
  direccion: string;
  idPais: number | null;
  idDepartamento: number | null;
  idCiudad: number | null;

  telefonoEmpresa: string | null;
  celularEmpresa: string | null;
  correoEmpresa: string | null;

  codigoTipoEmpresa: string | null;
  empleadoEntidad: boolean;
  codigoTipoContrato: string | null;
  codigoJornada: string | null;

  nombreContacto: string;
  celularContacto: string | null;
  fechaVinculacion: string | null; // ISO yyyy-MM-dd

  fechaCreacion?: string;
  fechaActualizacion?: string;
}

export type LaboralCreate = {
  nombreEmpresa: string;
  direccion: string;
  empleadoEntidad: boolean;
  nombreContacto: string;

  idPais: number | null;
  idDepartamento: number | null;
  idCiudad: number | null;

  telefonoEmpresa: string | null;
  celularEmpresa: string | null;
  correoEmpresa: string | null;

  codigoTipoEmpresa: string | null;
  codigoTipoContrato: string | null;
  codigoJornada: string | null;

  celularContacto: string | null;
  fechaVinculacion: string | null;
};

export type LaboralUpdate = LaboralCreate;

@Injectable({ providedIn: 'root' })
export class LaboralesApi {
  private http = inject(HttpClient);
  private base = (environment as any).apiBase?.replace(/\/+$/, '') || '';

  /**
   * Obtiene el registro laboral 1:1 de una persona.
   * - Devuelve `null` si no existe o si el back responde 404/500/… (modo silencioso para evitar errores de consola en listados).
   * - Tolera 204 (No Content).
   */
  getByPersona(idPersona: number): Observable<LaboralDetail | null> {
    const url = `${this.base}/hoja-vida/datos-personales/${idPersona}/laboral`;
    return this.http.get<LaboralDetail | null>(url, { observe: 'response' }).pipe(
      map((resp: HttpResponse<LaboralDetail | null>) => {
        if (resp.status === 204) return null;
        return resp.body ?? null;
      }),
      // 🔇 Silencioso: ante cualquier error → null (no propaga error a la consola del listado)
      catchError(() => of(null))
    );
  }

  create(idPersona: number, body: LaboralCreate): Observable<number> {
    const url = `${this.base}/hoja-vida/datos-personales/${idPersona}/laboral`;
    return this.http.post<number>(url, body);
  }

  update(idPersona: number, idLaboral: number, body: LaboralUpdate): Observable<void> {
    const url = `${this.base}/hoja-vida/datos-personales/${idPersona}/laboral/${idLaboral}`;
    return this.http.put<void>(url, body);
  }

  remove(idPersona: number, idLaboral: number): Observable<void> {
    const url = `${this.base}/hoja-vida/datos-personales/${idPersona}/laboral/${idLaboral}`;
    return this.http.delete<void>(url);
  }
}
