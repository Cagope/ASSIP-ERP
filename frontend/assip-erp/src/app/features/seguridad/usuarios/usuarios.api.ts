import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

export interface Usuario {
  id: number;
  username: string;
  superuserSeguridad: boolean;
  activo: boolean;
  ultimoLoginEn: string | null;
}

export interface ListMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  sort: string;
}

export interface ListResponse<T> {
  data: T[];
  meta: ListMeta;
}

@Injectable({ providedIn: 'root' })
export class UsuariosApi {
  private http = inject(HttpClient);
  private base = `${environment.apiBase}/seguridad/usuarios`;

  listar(opts?: { q?: string; activo?: boolean; superuser?: boolean; page?: number; size?: number; sort?: string }) {
    let params = new HttpParams();
    if (opts?.q) params = params.set('q', opts.q);
    if (opts?.activo !== undefined) params = params.set('activo', String(opts.activo));
    if (opts?.superuser !== undefined) params = params.set('superuser', String(opts.superuser));
    params = params
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20))
      .set('sort', opts?.sort ?? 'username,asc');

    return this.http.get<ListResponse<Usuario>>(this.base, { params });
  }
}
