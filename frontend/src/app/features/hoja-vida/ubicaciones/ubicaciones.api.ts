import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';
import { Ubicacion } from '../../../shared/models/ubicacion.model';

/**
 * 🌍 API Hoja de Vida — Ubicaciones
 * Maneja direcciones, contactos y referencias geográficas de las personas.
 */
@Injectable({ providedIn: 'root' })
export class UbicacionesApi {
  private readonly base = `${environment.apiUrl}/hoja-vida/ubicaciones`;

  constructor(private http: HttpClient) {}

  /** 🔹 Listar todas las ubicaciones */
  listar(): Observable<Ubicacion[]> {
    return this.http.get<Ubicacion[]>(this.base);
  }

  /** 🔹 Obtener una ubicación por ID */
  obtener(id: number): Observable<Ubicacion> {
    return this.http.get<Ubicacion>(`${this.base}/${id}`);
  }

  /** 🔹 Crear nueva ubicación */
  crear(data: Ubicacion): Observable<Ubicacion> {
    return this.http.post<Ubicacion>(this.base, data);
  }

  /** 🔹 Actualizar ubicación existente */
  actualizar(id: number, data: Ubicacion): Observable<Ubicacion> {
    return this.http.put<Ubicacion>(`${this.base}/${id}`, data);
  }

  /** 🔹 Eliminar ubicación */
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  // ===========================================================
  // 🔹 Nuevos métodos de filtrado
  // ===========================================================

  /** 🔸 Listar ubicaciones filtradas por zona */
  listarPorZona(idZona: number): Observable<Ubicacion[]> {
    return this.http.get<Ubicacion[]>(`${this.base}/zona/${idZona}`);
  }

  /** 🔸 Listar ubicaciones filtradas por subzona */
  listarPorSubZona(idSubZona: number): Observable<Ubicacion[]> {
    return this.http.get<Ubicacion[]>(`${this.base}/subzona/${idSubZona}`);
  }
}
