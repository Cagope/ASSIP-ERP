import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

/**
 * 🛡️ Estructura de datos — Condiciones de Protección
 * ------------------------------------------------------------
 * Representa las condiciones especiales, poblacionales y
 * constitucionales asociadas a una persona.
 */
export interface CondicionProteccion {

  // =========================================================
  // Identificación
  // =========================================================
  idCondicionProteccion?: number | null;
  idDatosPersonal: number;

  // =========================================================
  // Condiciones de protección
  // =========================================================
  administraRecursosPublicos: boolean;
  grupoProteccionEspecialConstitucional: boolean;
  personaMayor60Anos: boolean;
  discapacidadFisica: boolean;
  victimaConflictoArmado: boolean;
  pobrezaExtrema: boolean;
  poblacionIndigena: boolean;
  poblacionAfrodescendiente: boolean;
  poblacionLgbtiqMas: boolean;
  perteneceGrupoProteccionConstitucional: boolean;

  // =========================================================
  // Información adicional
  // =========================================================
  observaciones?: string | null;

  // =========================================================
  // Auditoría
  // =========================================================
  fkSeguridadCreacion?: number | null;
  fechaCreacion?: string | null;
  fkSeguridadEdicion?: number | null;
  fechaEdicion?: string | null;
}

/**
 * Crea la estructura inicial para un nuevo registro.
 */
export function nuevaCondicionProteccion(
  idDatosPersonal: number = 0
): CondicionProteccion {
  return {
    idCondicionProteccion: null,
    idDatosPersonal,

    administraRecursosPublicos: false,
    grupoProteccionEspecialConstitucional: false,
    personaMayor60Anos: false,
    discapacidadFisica: false,
    victimaConflictoArmado: false,
    pobrezaExtrema: false,
    poblacionIndigena: false,
    poblacionAfrodescendiente: false,
    poblacionLgbtiqMas: false,
    perteneceGrupoProteccionConstitucional: false,

    observaciones: null,

    fkSeguridadCreacion: null,
    fechaCreacion: null,
    fkSeguridadEdicion: null,
    fechaEdicion: null
  };
}

/**
 * 🌐 API Service — Condiciones de Protección
 * ------------------------------------------------------------
 * Gestiona las operaciones CRUD de las condiciones de
 * protección asociadas a una persona.
 *
 * Endpoints:
 *  - GET    /hoja-vida/condiciones-proteccion
 *  - GET    /hoja-vida/condiciones-proteccion/{id}
 *  - GET    /hoja-vida/condiciones-proteccion/persona/{idDatosPersonal}
 *  - POST   /hoja-vida/condiciones-proteccion
 *  - PUT    /hoja-vida/condiciones-proteccion/{id}
 *  - DELETE /hoja-vida/condiciones-proteccion/{id}
 */
@Injectable({
  providedIn: 'root'
})
export class CondicionesProteccionApi {

  private readonly base =
    `${environment.apiUrl}/hoja-vida/condiciones-proteccion`;

  constructor(
    private http: HttpClient
  ) {}

  /**
   * 🔹 Listar todos los registros.
   */
  listar(): Observable<CondicionProteccion[]> {
    return this.http.get<CondicionProteccion[]>(
      this.base
    );
  }

  /**
   * 🔹 Obtener un registro por ID.
   */
  obtener(
    idCondicionProteccion: number
  ): Observable<CondicionProteccion> {
    return this.http.get<CondicionProteccion>(
      `${this.base}/${idCondicionProteccion}`
    );
  }

  /**
   * 🔹 Obtener las condiciones de protección de una persona.
   */
  obtenerPorPersona(
    idDatosPersonal: number
  ): Observable<CondicionProteccion> {
    return this.http.get<CondicionProteccion>(
      `${this.base}/persona/${idDatosPersonal}`
    );
  }

  /**
   * 🔹 Crear un registro.
   */
  crear(
    data: CondicionProteccion
  ): Observable<CondicionProteccion> {
    return this.http.post<CondicionProteccion>(
      this.base,
      data
    );
  }

  /**
   * 🔹 Actualizar un registro existente.
   */
  actualizar(
    idCondicionProteccion: number,
    data: CondicionProteccion
  ): Observable<CondicionProteccion> {
    return this.http.put<CondicionProteccion>(
      `${this.base}/${idCondicionProteccion}`,
      data
    );
  }

  /**
   * 🔹 Eliminar un registro.
   */
  eliminar(
    idCondicionProteccion: number
  ): Observable<void> {
    return this.http.delete<void>(
      `${this.base}/${idCondicionProteccion}`
    );
  }
}
