import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { map } from 'rxjs/operators';

export type PrivilegiadoListItemDTO = {
  idPrivilegiado: number;
  idDirectivo: number;
  idDatosPersonal: number;

  documentoPersona: string;
  nombrePersona: string;

  codigoParentesco: string;
  parentescoNombre?: string | null;

  // Para impresión / lista enriquecida
  documentoDirectivo?: string | null;
  nombreDirectivo?: string | null;
};

export type PrivilegiadoDetailDTO = {
  idPrivilegiado: number;
  idDirectivo: number;
  idDatosPersonal: number;

  documentoPersona: string;
  nombrePersona: string;

  codigoParentesco: string;
  parentescoNombre?: string | null;

  documentoDirectivo?: string | null;
  nombreDirectivo?: string | null;
};

export type PrivilegiadoCreateUpdate = {
  idDirectivo: number;
  idDatosPersonal: number;
  codigoParentesco: string;
};

@Injectable({ providedIn: 'root' })
export class PrivilegiadosApi {
  private http = inject(HttpClient);
  private base = `${environment.apiBase}/general/privilegiados`;

  /**
   * El backend exige ?idDirectivo=...
   * Además, mapeamos:
   *  - documentoPersona  <= documento
   *  - parentescoNombre  <= nombreParentesco
   */
  list(idDirectivo: number, q?: string) {
    let params = new HttpParams().set('idDirectivo', String(idDirectivo));
    if (q && q.trim().length) params = params.set('q', q.trim());

    return this.http.get<any[]>(this.base, { params }).pipe(
      map(rows => (Array.isArray(rows) ? rows : (rows as any)?.content ?? [])),
      map(rows =>
        rows.map((r: any) => ({
          idPrivilegiado: r.idPrivilegiado,
          idDirectivo: r.idDirectivo,
          idDatosPersonal: r.idDatosPersonal,

          documentoPersona: r.documentoPersona ?? r.documento ?? '',
          nombrePersona: r.nombrePersona ?? '',

          codigoParentesco: r.codigoParentesco ?? '',
          parentescoNombre: r.parentescoNombre ?? r.nombreParentesco ?? null,

          // Si el back (en otra query) devuelve estos campos, los preservamos
          documentoDirectivo: r.documentoDirectivo ?? null,
          nombreDirectivo: r.nombreDirectivo ?? null,
        }) as PrivilegiadoListItemDTO)
      )
    );
  }

  get(id: number) {
    return this.http.get<any>(`${this.base}/${id}`).pipe(
      map(r => ({
        idPrivilegiado: r.idPrivilegiado,
        idDirectivo: r.idDirectivo,
        idDatosPersonal: r.idDatosPersonal,

        documentoPersona: r.documentoPersona ?? r.documento ?? '',
        nombrePersona: r.nombrePersona ?? '',

        codigoParentesco: r.codigoParentesco ?? '',
        parentescoNombre: r.parentescoNombre ?? r.nombreParentesco ?? null,

        documentoDirectivo: r.documentoDirectivo ?? null,
        nombreDirectivo: r.nombreDirectivo ?? null,
      }) as PrivilegiadoDetailDTO)
    );
  }

  create(payload: PrivilegiadoCreateUpdate) {
    return this.http.post<number>(this.base, payload);
  }

  update(id: number, payload: PrivilegiadoCreateUpdate) {
    return this.http.put<void>(`${this.base}/${id}`, payload);
  }

  remove(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
