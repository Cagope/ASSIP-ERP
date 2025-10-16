import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable, map } from 'rxjs';

export interface Page<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: any;
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalElements: number;
  totalPages: number;
  first: boolean;
  size: number;
  number: number;
  sort: any;
  numberOfElements: number;
  empty: boolean;
}

export interface DatosPersonalesListItem {
  idDatosPersonal: number;
  tipoDocumento: string;
  documento: string;
  tieneRut: boolean;
  digitoVerificacion: string | null;
  nombres: string;
  primerApellido: string;
  fechaNacimiento: string; // ISO
}

export interface DatosPersonalesDetail {
  idDatosPersonal: number;
  tipoDocumento: string;
  documento: string;
  tieneRut: boolean;
  digitoVerificacion: string | null;
  nombres: string;
  primerApellido: string;
  segundoApellido: string | null;
  fechaNacimiento: string;
  fechaDocumento: string;
  fechaApertura: string;
  idPaisDocumento: number;
  idDepartamentoExpedicion: number;
  idCiudadExpedicion: number;
  idPaisNacimiento: number;
  idDepartamentoNacimiento: number;
  idCiudadNacimiento: number;
  comentario: string;
  codigoGenero: string;
  codigoEstadoCivil: string;
  codigoEscolaridad: string;
  codigoTipoVivienda: string;
  estratoSocial: number;
  numeroHijos: number;
  codigoOcupacion: string;
  codigoSectorEconomico: string;
  codigoActividadSes: string;
  codigoActividadDian: string;
  codigoRetencion: string;
}

export interface DatosPersonaCreate {
  tipoDocumento: string;
  documento: string;
  tieneRut: boolean;
  digitoVerificacion?: string | null;
  tipoPersona: string; // "1" | "2"
  nombres: string;
  primerApellido: string;
  segundoApellido?: string | null;
  fechaNacimiento: string;
  fechaDocumento: string;
  idPaisDocumento: number;
  idPaisNacimiento: number;
  idCiudadExpedicion: number;
  idCiudadNacimiento: number;
  comentario: string;
  codigoGenero: string;
  codigoEstadoCivil: string;
  codigoEscolaridad: string;
  codigoTipoVivienda: string;
  estratoSocial: number;
  numeroHijos: number;
  codigoOcupacion: string;
  codigoSectorEconomico: string;
  codigoActividadSes: string;
  codigoActividadDian: string;
  codigoRetencion: string;
  cabezaFamilia: string; // "0" | "1"
}

export type DatosPersonaUpdate = DatosPersonaCreate;

@Injectable({ providedIn: 'root' })
export class DatosPersonalesApi {
  private http = inject(HttpClient);
  private base = `${environment.apiBase}/hoja-vida/datos-personales`;

  list(opts?: { q?: string; page?: number; size?: number; sort?: string }): Observable<Page<DatosPersonalesListItem>> {
    let params = new HttpParams();
    if (opts?.q) params = params.set('q', opts.q);
    if (opts?.page != null) params = params.set('page', String(opts.page));
    if (opts?.size != null) params = params.set('size', String(opts.size));
    if (opts?.sort) params = params.set('sort', opts.sort);
    return this.http.get<Page<DatosPersonalesListItem>>(this.base, { params });
  }

  get(id: number): Observable<DatosPersonalesDetail> {
    return this.http.get<DatosPersonalesDetail>(`${this.base}/${id}`);
  }

  create(body: DatosPersonaCreate): Observable<number> {
    return this.http.post<number>(this.base, body);
  }

  update(id: number, body: DatosPersonaUpdate): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  /** Helper: consulta con q=documento y verifica coincidencia exacta tipo+documento */
  existsByTipoDocumentoYNumero(tipoDocumento: string, documento: string): Observable<boolean> {
    return this.list({ q: documento, size: 5 }).pipe(
      map(page => (page?.content ?? []).some(it => it.tipoDocumento === tipoDocumento && it.documento === documento))
    );
  }
}
