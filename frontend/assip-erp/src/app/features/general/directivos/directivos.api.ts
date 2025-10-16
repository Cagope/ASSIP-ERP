import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

export type DirectivoListItemDTO = {
  idDirectivo: number;
  idDatosPersonal: number;
  documento: string;
  nombrePersona: string;
  codigoTipoDirectivo: string;
  calidadDirectivo: '1' | '2';
  estadoDirectivo: '1' | '2' | '3';
  fechaResolucion: string | null; // ISO date
  fechaRetiro: string | null;     // ISO date
  periodosVigencia: number | null;
};

export type DirectivoDetailDTO = {
  idDirectivo: number;
  idDatosPersonal: number;
  codigoTipoDirectivo: string;
  calidadDirectivo: '1' | '2';
  estadoDirectivo: '1' | '2' | '3';
  actaAsamblea: string;
  fechaAsamblea: string;    // ISO date
  resolucionSes: string;
  fechaResolucion: string;  // ISO date
  fechaRetiro: string | null;
  periodosVigencia: number | null;
};

export type DirectivoCreateUpdate = {
  idDatosPersonal: number;
  codigoTipoDirectivo: string; // 2 chars
  calidadDirectivo: '1' | '2';
  estadoDirectivo?: '1' | '2' | '3'; // create puede omitir -> '1'
  actaAsamblea: string;
  fechaAsamblea: string;     // ISO
  resolucionSes: string;
  fechaResolucion: string;   // ISO
  fechaRetiro?: string | null;
  periodosVigencia?: number | null;
};

@Injectable({ providedIn: 'root' })
export class DirectivosApi {
  private http = inject(HttpClient);
  private base = `${environment.apiBase}/general/directivos`;

  list(q?: string) {
    let params = new HttpParams();
    if (q && q.trim().length) params = params.set('q', q.trim());
    return this.http.get<DirectivoListItemDTO[]>(this.base, { params });
  }

  get(id: number) {
    return this.http.get<DirectivoDetailDTO>(`${this.base}/${id}`);
  }

  create(payload: DirectivoCreateUpdate) {
    return this.http.post<number>(this.base, payload);
  }

  update(id: number, payload: DirectivoCreateUpdate) {
    return this.http.put<void>(`${this.base}/${id}`, payload);
  }

  remove(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
