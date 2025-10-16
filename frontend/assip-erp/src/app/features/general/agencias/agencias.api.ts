import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

export type AgenciaListItemDTO = {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
  siglaAgencia: string;
};

export type AgenciaDetailDTO = {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
  siglaAgencia: string;
  direccionAgencia: string;
  idDepartamento: number | null;
  idCiudad: number | null;
  correoAgencia: string | null;
  celularAgencia: string | null;
  telefonoAgencia: string | null;
};

export type AgenciaCreateUpdate = {
  codigoAgencia: string;
  nombreAgencia: string;
  siglaAgencia: string;
  direccionAgencia: string;
  idDepartamento?: number | null;
  idCiudad?: number | null;
  correoAgencia?: string | null;
  celularAgencia?: string | null;
  telefonoAgencia?: string | null;
};

@Injectable({ providedIn: 'root' })
export class AgenciasApi {
  private http = inject(HttpClient);
  private base = `${environment.apiBase}/general/agencias`; // ✅ sin /api/v1 extra

  list(q?: string) {
    let params = new HttpParams();
    if (q && q.trim().length) params = params.set('q', q.trim());
    return this.http.get<AgenciaListItemDTO[]>(this.base, { params });
  }

  get(id: number) { return this.http.get<AgenciaDetailDTO>(`${this.base}/${id}`); }
  create(payload: AgenciaCreateUpdate) { return this.http.post<number>(this.base, payload); }
  update(id: number, payload: AgenciaCreateUpdate) { return this.http.put<void>(`${this.base}/${id}`, payload); }
  remove(id: number) { return this.http.delete<void>(`${this.base}/${id}`); }
}
