import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/** Modelo snake_case (lo que usan tus templates) */
export interface ReferenciaPersonal {
  id_referencia_personal?: number;
  id_datos_personal: number;
  nombre_referencia_personal: string;
  direccion_referencia_personal: string;
  id_departamento?: number | null;
  id_ciudad?: number | null;
  telefono_referencia_personal?: string | null;
  celular_referencia_personal?: string | null;
}

/** Modelo camelCase (por si el back usa camel) */
type ReferenciaPersonalCamel = {
  idReferenciaPersonal?: number;
  idDatosPersonal?: number;
  nombreReferenciaPersonal: string;
  direccionReferenciaPersonal: string;
  idDepartamento?: number | null;
  idCiudad?: number | null;
  telefonoReferenciaPersonal?: string | null;
  celularReferenciaPersonal?: string | null;
};

@Injectable({ providedIn: 'root' })
export class ReferenciasPersonalesApi {
  private http = inject(HttpClient);
  private base = `${environment.apiBase}/hoja-vida`;

  // ===== Helpers =====
  /** Quita claves con null/undefined/'' */
  private pruneNulls<T extends object>(obj: T): Partial<T> {
    const out: any = {};
    Object.keys(obj ?? {}).forEach(k => {
      const v = (obj as any)[k];
      if (v !== null && v !== undefined && v !== '') out[k] = v;
    });
    return out;
  }

  /** Lee valor de cualquiera de las dos claves (snake/camel) */
  private pick<T = any>(o: any, snake: string, camel: string, fallback?: T): T | null {
    if (!o) return fallback ?? null;
    if (o.hasOwnProperty(snake)) return (o as any)[snake] as T;
    if (o.hasOwnProperty(camel)) return (o as any)[camel] as T;
    return fallback ?? null;
  }

  /** Convierte cualquier shape (snake o camel) a nuestro modelo snake_case */
  private toSnakeAny(obj: any, fkRuta?: number): ReferenciaPersonal | null {
    if (!obj) return null;

    // Si viene envuelto (muy común en controladores): { data: {...} } o { content: {...} }
    if (obj && typeof obj === 'object') {
      const inner = obj.data ?? obj.item ?? obj.row ?? null;
      const list = obj.content ?? obj.items ?? null;
      if (inner && typeof inner === 'object') return this.toSnakeAny(inner, fkRuta);
      if (Array.isArray(list) && list.length > 0) return this.toSnakeAny(list[0], fkRuta);
    }

    const idRef = this.pick<number>(obj, 'id_referencia_personal', 'idReferenciaPersonal') ?? undefined;
    // Si el back no devuelve el FK, usamos el de la ruta para no dejarlo en 0
    const idDatos = this.pick<number>(obj, 'id_datos_personal', 'idDatosPersonal', fkRuta ?? 0) ?? 0;

    const nombre = this.pick<string>(obj, 'nombre_referencia_personal', 'nombreReferenciaPersonal') ?? '';
    const dir = this.pick<string>(obj, 'direccion_referencia_personal', 'direccionReferenciaPersonal') ?? '';

    const idDepto = this.pick<number | null>(obj, 'id_departamento', 'idDepartamento', null);
    const idCiu = this.pick<number | null>(obj, 'id_ciudad', 'idCiudad', null);

    const tel = this.pick<string | null>(obj, 'telefono_referencia_personal', 'telefonoReferenciaPersonal', null);
    const cel = this.pick<string | null>(obj, 'celular_referencia_personal', 'celularReferenciaPersonal', null);

    // Si no hay nombre/dirección, es muy probable que la respuesta sea vacía
    // (pero dejamos pasar por si el back permite vacíos)
    return {
      id_referencia_personal: idRef,
      id_datos_personal: idDatos,
      nombre_referencia_personal: nombre,
      direccion_referencia_personal: dir,
      id_departamento: idDepto,
      id_ciudad: idCiu,
      telefono_referencia_personal: tel,
      celular_referencia_personal: cel,
    };
  }

  /** A camel para enviar a un back camelCase en rutas anidadas (sin idDatosPersonal) */
  private toCamel(x: ReferenciaPersonal): ReferenciaPersonalCamel {
    return {
      idReferenciaPersonal: x.id_referencia_personal,
      nombreReferenciaPersonal: x.nombre_referencia_personal,
      direccionReferenciaPersonal: x.direccion_referencia_personal,
      idDepartamento: x.id_departamento ?? null,
      idCiudad: x.id_ciudad ?? null,
      telefonoReferenciaPersonal: x.telefono_referencia_personal ?? null,
      celularReferenciaPersonal: x.celular_referencia_personal ?? null,
    };
  }

  // ========== RUTAS ANIDADAS 1:1 (SINGULAR) ==========
  /** GET única referencia (puede no existir). */
  getByPersona(idDatosPersonal: number): Observable<ReferenciaPersonal | null> {
    const url = `${this.base}/datos-personales/${idDatosPersonal}/referencia-personal`;
    return this.http.get<any>(url, { observe: 'body' }).pipe(
      map((resp: any) => {
        // Toleramos 204/empty body/{} y snake o camel en el payload
        const sn = this.toSnakeAny(resp, idDatosPersonal);
        // Si no trae nada significativo, devolvemos null para que el template muestre "No hay…"
        if (!sn) return null;
        // Si no hay PK, igualmente consideramos como no existente
        if (!sn.id_referencia_personal && !sn.nombre_referencia_personal && !sn.direccion_referencia_personal) {
          return null;
        }
        return sn;
      })
    );
  }

  /** POST crear (1:1). */
  createForPersona(idDatosPersonal: number, payloadSnake: ReferenciaPersonal): Observable<number | ReferenciaPersonal> {
    const url = `${this.base}/datos-personales/${idDatosPersonal}/referencia-personal`;
    const body = this.pruneNulls(this.toCamel(payloadSnake)); // sin idDatosPersonal
    return this.http.post<any>(url, body).pipe(
      map((resp: any) => {
        if (typeof resp === 'number') return resp;
        return this.toSnakeAny(resp, idDatosPersonal) ?? ({ ...payloadSnake, id_datos_personal: idDatosPersonal } as ReferenciaPersonal);
      })
    );
  }

  /** PUT actualizar (1:1 por id). */
  updateForPersona(
    idDatosPersonal: number,
    idReferencia: number,
    payloadSnake: ReferenciaPersonal
  ): Observable<ReferenciaPersonal> {
    const url = `${this.base}/datos-personales/${idDatosPersonal}/referencia-personal/${idReferencia}`;
    const body = this.pruneNulls(this.toCamel(payloadSnake)); // sin idDatosPersonal
    return this.http.put<any>(url, body).pipe(
      map((resp: any) => this.toSnakeAny(resp, idDatosPersonal) ?? { ...payloadSnake, id_referencia_personal: idReferencia })
    ) as Observable<ReferenciaPersonal>;
  }

  /** DELETE (1:1 por id). */
  deleteForPersona(idDatosPersonal: number, idReferencia: number): Observable<void> {
    const url = `${this.base}/datos-personales/${idDatosPersonal}/referencia-personal/${idReferencia}`;
    return this.http.delete<void>(url);
  }

  // ===== (Opcional) FLAT: si tu back los expone, quedan aquí para compatibilidad =====
  get(id: number): Observable<ReferenciaPersonal> {
    const url = `${this.base}/referencias-personales/${id}`;
    return this.http.get<any>(url).pipe(map((obj) => this.toSnakeAny(obj) as ReferenciaPersonal));
  }

  create(payloadSnake: ReferenciaPersonal): Observable<number | ReferenciaPersonal> {
    const url = `${this.base}/referencias-personales`;
    const body = this.pruneNulls({ ...this.toCamel(payloadSnake), idDatosPersonal: payloadSnake.id_datos_personal });
    return this.http.post<any>(url, body).pipe(
      map((resp) => (typeof resp === 'number' ? resp : this.toSnakeAny(resp) ?? payloadSnake))
    );
  }

  update(id: number, payloadSnake: ReferenciaPersonal): Observable<ReferenciaPersonal> {
    const url = `${this.base}/referencias-personales/${id}`;
    const body = this.pruneNulls({ ...this.toCamel(payloadSnake), idDatosPersonal: payloadSnake.id_datos_personal });
    return this.http.put<any>(url, body).pipe(
      map((resp) => this.toSnakeAny(resp) ?? { ...payloadSnake, id_referencia_personal: id })
    ) as Observable<ReferenciaPersonal>;
  }

  delete(id: number): Observable<void> {
    const url = `${this.base}/referencias-personales/${id}`;
    return this.http.delete<void>(url);
  }
}
