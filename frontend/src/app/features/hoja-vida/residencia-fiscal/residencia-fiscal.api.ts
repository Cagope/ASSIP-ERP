import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

/**
 * 🌎 Estructura de datos — Residencia Fiscal
 * ------------------------------------------------------------
 * Corresponde a la información administrada por el backend
 * para FATCA y CRS.
 */
export interface ResidenciaFiscal {

  // =========================================================
  // Identificación
  // =========================================================
  idResidenciaFiscal?: number | null;
  idDatosPersonal: number;

  // =========================================================
  // Declaraciones FATCA / CRS
  // =========================================================
  ciudadanoEstadosUnidos: boolean;
  residenteFiscalEstadosUnidos: boolean;
  residenteFiscalExterior: boolean;

  // =========================================================
  // Residencia fiscal exterior
  // =========================================================
  paisResidenciaFiscal?: string | null;
  ciudadResidenciaFiscal?: string | null;
  direccionResidenciaFiscal?: string | null;

  // =========================================================
  // Identificación fiscal
  // =========================================================
  tipoIdentificacionFiscal?: string | null;
  numeroIdentificacionFiscal?: string | null;

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
 * 🌐 API Service — Residencia Fiscal (FATCA / CRS)
 * ------------------------------------------------------------
 * Gestiona las operaciones CRUD de la información de
 * residencia fiscal del asociado.
 *
 * Endpoints:
 *  - GET    /hoja-vida/residencia-fiscal
 *  - GET    /hoja-vida/residencia-fiscal/{id}
 *  - GET    /hoja-vida/residencia-fiscal/persona/{idDatosPersonal}
 *  - POST   /hoja-vida/residencia-fiscal
 *  - PUT    /hoja-vida/residencia-fiscal/{id}
 *  - DELETE /hoja-vida/residencia-fiscal/{id}
 */
@Injectable({
  providedIn: 'root'
})
export class ResidenciaFiscalApi {

  private readonly base =
    `${environment.apiUrl}/hoja-vida/residencia-fiscal`;

  constructor(
    private http: HttpClient
  ) {}

  /**
   * 🔹 Listar todos los registros
   */
  listar(): Observable<ResidenciaFiscal[]> {
    return this.http.get<ResidenciaFiscal[]>(this.base);
  }

  /**
   * 🔹 Obtener un registro por ID
   */
  obtener(id: number): Observable<ResidenciaFiscal> {
    return this.http.get<ResidenciaFiscal>(
      `${this.base}/${id}`
    );
  }

  /**
   * 🔹 Obtener el registro de una persona
   */
  obtenerPorPersona(
    idDatosPersonal: number
  ): Observable<ResidenciaFiscal> {
    return this.http.get<ResidenciaFiscal>(
      `${this.base}/persona/${idDatosPersonal}`
    );
  }

  /**
   * 🔹 Crear un registro
   */
  crear(
    data: ResidenciaFiscal
  ): Observable<ResidenciaFiscal> {
    return this.http.post<ResidenciaFiscal>(
      this.base,
      data
    );
  }

  /**
   * 🔹 Actualizar un registro
   */
  actualizar(
    id: number,
    data: ResidenciaFiscal
  ): Observable<ResidenciaFiscal> {
    return this.http.put<ResidenciaFiscal>(
      `${this.base}/${id}`,
      data
    );
  }

  /**
   * 🔹 Eliminar un registro
   */
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.base}/${id}`
    );
  }
}
