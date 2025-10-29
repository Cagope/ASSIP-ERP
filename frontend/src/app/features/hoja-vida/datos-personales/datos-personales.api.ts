import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';

/**
 * 🧍 Entidad DatosPersonales
 * Representa la información personal del afiliado o persona natural.
 */
export interface DatosPersonales {
  idDatosPersonal?: number;
  tipoDocumento: string;
  documento: string;
  tipoPersona: string;
  tieneRut: boolean;
  digitoVerificacion?: string;
  fechaDocumento?: string;
  idPaisDocumento?: number;
  idDepartamentoExpedicion?: number;
  idCiudadExpedicion?: number;
  nombres: string;
  primerApellido: string;
  segundoApellido?: string;
  fechaNacimiento?: string;
  idPaisNacimiento?: number;
  idDepartamentoNacimiento?: number;
  idCiudadNacimiento?: number;
  fechaApertura?: string;
  fechaActualizacion?: string;
  codigoGenero?: string;
  codigoEstadoCivil?: string;
  codigoEscolaridad?: string;
  cabezaFamilia?: string;
  estratoSocial?: number;
  codigoTipoVivienda?: string;
  numeroHijos?: number;
  codigoOcupacion?: string;
  codigoSectorEconomico?: string;
  codigoActividadSes?: string;
  codigoActividadDian?: string;
  comentario: string;
  foto?: string;
  firmaUno?: string;
  firmaDos?: string;
  fkSeguridadCreacion?: number;
  fechaCreacion?: string;
  fkSeguridadEdicion?: number;
  fechaEdicion?: string;
}

@Injectable({ providedIn: 'root' })
export class DatosPersonalesApi {
  private readonly base = `${environment.apiUrl}/hoja-vida/datos-personales`;

  constructor(private http: HttpClient) {}

  listar(): Observable<DatosPersonales[]> {
    return this.http.get<DatosPersonales[]>(this.base);
  }

  obtener(id: number): Observable<DatosPersonales> {
    return this.http.get<DatosPersonales>(`${this.base}/${id}`);
  }

  crear(data: DatosPersonales): Observable<DatosPersonales> {
    return this.http.post<DatosPersonales>(this.base, data);
  }

  actualizar(id: number, data: DatosPersonales): Observable<DatosPersonales> {
    return this.http.put<DatosPersonales>(`${this.base}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
