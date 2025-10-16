import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { map } from 'rxjs/operators';

// Tipos de respuesta (snake_case para alinear con backend)
export interface UbicacionResponse {
  id_ubicacion: number;
  id_datos_personal: number;
  direccion: string;
  barrio: string;
  telefono?: string | null;
  celular_uno?: string | null;
  celular_dos?: string | null;
  correo?: string | null;
  id_pais?: number | null;
  id_departamento?: number | null;
  id_ciudad?: number | null;
  id_sub_zona?: number | null;

  // Opcionales (si el back los envía o si futuro se enriquece)
  nombre_pais?: string | null;
  nombre_departamento?: string | null;
  nombre_ciudad?: string | null;
  nombre_zona?: string | null;
  nombre_sub_zona?: string | null;
}

export interface UbicacionRequest {
  direccion: string;
  barrio: string;
  telefono?: string | null;
  celular_uno?: string | null;
  celular_dos?: string | null;
  correo?: string | null;
  id_pais?: number | null;
  id_departamento?: number | null;
  id_ciudad?: number | null;
  id_sub_zona?: number | null;
}

@Injectable({ providedIn: 'root' })
export class UbicacionesApi {
  private http = inject(HttpClient);
  private base = environment.apiBase || '';

  /** GET (200 con body o 204 sin body) */
  getByPersona(idDatosPersonales: number): Observable<HttpResponse<UbicacionResponse>> {
    return this.http.get<UbicacionResponse>(
      `${this.base}/hoja-vida/datos-personales/${idDatosPersonales}/ubicacion`,
      { observe: 'response' },
    );
  }

  /** POST -> retorna id (number) */
  createForPersona(idDatosPersonales: number, payload: UbicacionRequest): Observable<number> {
    return this.http.post<number>(
      `${this.base}/hoja-vida/datos-personales/${idDatosPersonales}/ubicacion`,
      payload,
    );
  }

  /** PUT -> 204 */
  updateForPersona(idDatosPersonales: number, idUbicacion: number, payload: UbicacionRequest): Observable<void> {
    return this.http.put<void>(
      `${this.base}/hoja-vida/datos-personales/${idDatosPersonales}/ubicacion/${idUbicacion}`,
      payload,
    );
  }

  /** DELETE -> 204 */
  deleteForPersona(idDatosPersonales: number, idUbicacion: number): Observable<void> {
    return this.http.delete<void>(
      `${this.base}/hoja-vida/datos-personales/${idDatosPersonales}/ubicacion/${idUbicacion}`,
    );
  }

  // --- Catálogos: se consumen completo (y filtramos en front) ---

  /** Paises */
  getPaises(): Observable<Array<{ id_pais: number; nombre_pais: string }>> {
    return this.http.get<Array<{ id_pais: number; nombre_pais: string }>>(
      `${this.base}/catalogos/paises`
    );
  }

  /** Departamentos */
  getDepartamentos(): Observable<Array<{ id_departamento: number; id_pais?: number; nombre_departamento: string }>> {
    return this.http.get<Array<{ id_departamento: number; id_pais?: number; nombre_departamento: string }>>(
      `${this.base}/catalogos/departamentos`
    );
  }

  /** Ciudades */
  getCiudades(): Observable<Array<{ id_ciudad: number; id_departamento: number; nombre_ciudad: string }>> {
    return this.http.get<Array<{ id_ciudad: number; id_departamento: number; nombre_ciudad: string }>>(
      `${this.base}/catalogos/ciudades`
    );
  }

  /** Sub-zonas (desde módulo general) */
  getSubZonas(size: number = 1000): Observable<Array<{ idSubZona: number; idZona: number | null; nombreSubZona: string }>> {
    const params = new HttpParams().set('size', String(size));
    return this.http.get<any>(`${this.base}/general/sub-zonas`, { params }).pipe(
      map((data: any) => {
        const arr = Array.isArray(data) ? data : (data?.items ?? data?.content ?? []);
        return (arr ?? []).map((s: any) => ({
          idSubZona: (s?.idSubZona ?? s?.id_sub_zona ?? s?.id) ?? null,
          idZona: (s?.idZona ?? s?.id_zona) ?? null,
          nombreSubZona: (s?.nombreSubZona ?? s?.nombre_sub_zona ?? s?.nombre ?? '') as string,
        }));
      })
    );
  }
}
