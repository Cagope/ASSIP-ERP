import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PermisoEspecial } from '../../../shared/models/permisos-especiales.model';

/**
 * 🌐 API Service — Permisos Especiales
 * ------------------------------------------------------------
 * Gestiona las operaciones CRUD para los permisos de contacto
 * (llamadas, SMS, emails, cartas, redes sociales) asociados a
 * una persona en el módulo Hoja de Vida.
 *
 * Endpoints:
 *  - GET    /hoja-vida/permisos-especiales
 *  - GET    /hoja-vida/permisos-especiales/{id}
 *  - GET    /hoja-vida/permisos-especiales/persona/{idDatosPersonal}
 *  - POST   /hoja-vida/permisos-especiales
 *  - PUT    /hoja-vida/permisos-especiales/{id}
 *  - DELETE /hoja-vida/permisos-especiales/{id}
 */
@Injectable({ providedIn: 'root' })
export class PermisosEspecialesApi {
  private readonly base = `${environment.apiUrl}/hoja-vida/permisos-especiales`;

  constructor(private http: HttpClient) {}

  /** 🔹 Listar todos los registros de permisos especiales */
  listar(): Observable<PermisoEspecial[]> {
    return this.http.get<PermisoEspecial[]>(this.base);
  }

  /** 🔹 Obtener un permiso especial por ID */
  obtener(id: number): Observable<PermisoEspecial> {
    return this.http.get<PermisoEspecial>(`${this.base}/${id}`);
  }

  /** 🔹 Crear un nuevo registro de permisos especiales */
  crear(data: PermisoEspecial): Observable<PermisoEspecial> {
    return this.http.post<PermisoEspecial>(this.base, data);
  }

  /** 🔹 Actualizar un registro existente */
  actualizar(id: number, data: PermisoEspecial): Observable<PermisoEspecial> {
    return this.http.put<PermisoEspecial>(`${this.base}/${id}`, data);
  }

  /** 🔹 Eliminar un registro de permisos especiales */
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  /** 🔹 Obtener permisos especiales por persona (idDatosPersonal) */
  obtenerPorPersona(idDatosPersonal: number): Observable<PermisoEspecial> {
    return this.http.get<PermisoEspecial>(`${this.base}/persona/${idDatosPersonal}`);
  }
}

/** ✅ Exporta el tipo para uso global */
export type { PermisoEspecial };
