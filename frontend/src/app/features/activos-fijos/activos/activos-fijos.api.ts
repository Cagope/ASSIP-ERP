import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTOs
   ========================================================= */

export interface ActivoFijoListDTO {
  idActivoFijo: number;
  placaActivo: string;
  nombreActivo: string;
  fechaIngreso: string;
  mesesDepreciacion: number;
  valorAdquisicion: number;
  valorMensual: number;
  idAgencia: number;
  nombreAgencia: string;
  idEstadoActivo: number;
  nombreEstadoActivo: string;
}

export interface ActivoFijoFormDTO {
  idActivoFijo?: number;

  placaActivo: string;
  nombreActivo: string;

  fechaIngreso: string;
  fechaGarantia: string;
  fechaBaja?: string | null;

  idFormaDepreciacion: number;
  mesesDepreciacion: number;

  valorAdquisicion: number;
  valorMensual: number;

  idEstadoActivo: number;
  idTipoAdquisicion: number;
  idAgencia: number;
  idUbicacion: number;
  idBloque?: number | null;

  idDatosPersonalResponsable?: number | null;
  idDatosPersonalProveedor?: number | null;

  idCatalogoCuentaActivo?: number | null;
  idCatalogoCuentaDepreciacion?: number | null;
  idCatalogoCuentaGasto?: number | null;
  idCatalogoCuentaControl?: number | null;
  idCatalogoCuentaIva?: number | null;
}

export interface ActivoFijoSaveDTO extends ActivoFijoFormDTO {}

/* =========================================================
   API
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class ActivosFijosApi {

  // ✔ Ruta alineada al backend (sin repetir /api/v1)
  private readonly base = `${environment.apiUrl}/activos-fijos/activos`;

  constructor(private http: HttpClient) {}

  // =========================================================
  // LISTADO
  // =========================================================
  listar(): Observable<ActivoFijoListDTO[]> {
    return this.http.get<ActivoFijoListDTO[]>(this.base);
  }

  // =========================================================
  // FORMULARIO (detalle)
  // =========================================================
  obtener(id: number): Observable<ActivoFijoFormDTO> {
    return this.http.get<ActivoFijoFormDTO>(`${this.base}/${id}/form`);
  }

  // =========================================================
  // CREAR
  // =========================================================
  crear(data: ActivoFijoSaveDTO): Observable<void> {
    return this.http.post<void>(this.base, data);
  }

  // =========================================================
  // ACTUALIZAR
  // =========================================================
  actualizar(id: number, data: ActivoFijoSaveDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}`, data);
  }

  // =========================================================
  // ELIMINAR
  // =========================================================
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
