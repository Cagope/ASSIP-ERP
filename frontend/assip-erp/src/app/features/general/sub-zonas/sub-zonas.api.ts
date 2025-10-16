import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

export interface SubZona {
  idSubZona: number;
  idZona: number | null;
  codigoSubZona: string | null;
  nombreSubZona: string | null;
  comentarioSubZona: string | null;
  nombreZona?: string | null;
}

export interface SubZonaCreate {
  idZona: number | null;
  codigoSubZona: string | null;
  nombreSubZona: string | null;
  comentarioSubZona: string | null;
}

export interface SubZonaUpdate extends SubZonaCreate {}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({ providedIn: 'root' })
export class SubZonasApi {
  private http = inject(HttpClient);
  private base = `${environment.apiBase}/general/sub-zonas`;

  list(params: { idZona?: number; q?: string; page?: number; size?: number; sort?: string } = {}) {
    let p = new HttpParams();
    if (params.idZona != null) p = p.set('idZona', String(params.idZona));
    if (params.q) p = p.set('q', params.q);
    p = p.set('page', String(params.page ?? 0));
    p = p.set('size', String(params.size ?? 20));
    p = p.set('sort', params.sort ?? 'nombreSubZona,asc');
    return this.http.get<Page<SubZona>>(this.base, { params: p });
  }

  get(id: number) {
    return this.http.get<SubZona>(`${this.base}/${id}`);
  }

  create(body: SubZonaCreate) {
    return this.http.post<number>(this.base, body);
  }

  update(id: number, body: SubZonaUpdate) {
    return this.http.put<void>(`${this.base}/${id}`, body);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
