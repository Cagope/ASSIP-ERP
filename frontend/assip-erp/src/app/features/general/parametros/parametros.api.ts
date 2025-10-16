import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

export interface Parametro {
  idParametro: number;
  idAgencia: number | null;
  nombreAgencia?: string | null;
  codigoParametro: number | null;
  nombreParametro: string | null;
  valorParametro: number | null;
  tipoValor: boolean | null; // true=valor, false=porcentaje
}

export interface ParametroCreate {
  idAgencia: number | null;
  codigoParametro: number | null;
  nombreParametro: string | null;
  valorParametro: number | null;
  tipoValor: boolean | null;
}

export interface ParametroUpdate extends ParametroCreate {}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({ providedIn: 'root' })
export class ParametrosApi {
  private http = inject(HttpClient);
  private base = `${environment.apiBase}/general/parametros`;

  list(params: { idAgencia?: number; q?: string; codigo?: number; page?: number; size?: number; sort?: string } = {}) {
    let p = new HttpParams();
    if (params.idAgencia != null) p = p.set('idAgencia', String(params.idAgencia));
    if (params.q) p = p.set('q', params.q);
    if (params.codigo != null) p = p.set('codigo', String(params.codigo));
    p = p.set('page', String(params.page ?? 0));
    p = p.set('size', String(params.size ?? 20));
    p = p.set('sort', params.sort ?? 'nombreParametro,asc');
    return this.http.get<Page<Parametro>>(this.base, { params: p });
  }

  get(id: number) {
    return this.http.get<Parametro>(`${this.base}/${id}`);
  }

  create(body: ParametroCreate) {
    return this.http.post<number>(this.base, body);
  }

  update(id: number, body: ParametroUpdate) {
    return this.http.put<void>(`${this.base}/${id}`, body);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
