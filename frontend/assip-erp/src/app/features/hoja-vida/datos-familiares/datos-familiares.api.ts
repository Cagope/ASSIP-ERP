import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../../environments/environment';

/** Interfaz usada por tus templates (snake_case). */
export interface DatosFamiliar {
  id_datos_familiares?: number;
  id_datos_personal: number;
  codigo_parentesco?: string | null;
  nombre_datos_familiar: string;
  documento_datos_familiar?: string | null;
  fecha_nacimiento?: string | null;
  telefono_datos_familiar?: string | null;
  celular_datos_familiar?: string | null;
  direccion_datos_familiar: string;
  id_departamento?: number | null;
  id_ciudad?: number | null;
  ingresos_datos_familiar?: number | null;
  egresos_datos_familiar?: number | null;
  referencia_familiar?: boolean | null;
}

/** Page genérico (si lo usas alguna vez). */
export interface Page<T> {
  content: T[];
  totalElements?: number;
  totalPages?: number;
  size?: number;
  number?: number;
  first?: boolean;
  last?: boolean;
  numberOfElements?: number;
  empty?: boolean;
  sort?: any;
  pageable?: any;
}

/** Modelo camelCase (back). */
type DatosFamiliarCamel = {
  idDatosFamiliares?: number;
  idDatosPersonal: number;
  codigoParentesco?: string | null;
  nombreDatosFamiliar: string;
  documentoDatosFamiliar?: string | null;
  fechaNacimiento?: string | null;
  telefonoDatosFamiliar?: string | null;
  celularDatosFamiliar?: string | null;
  direccionDatosFamiliar: string;
  idDepartamento?: number | null;
  idCiudad?: number | null;
  ingresosDatosFamiliar?: number | string | null;
  egresosDatosFamiliar?: number | string | null;
  referenciaFamiliar?: boolean | null;
};

@Injectable({ providedIn: 'root' })
export class DatosFamiliaresApi {
  private http = inject(HttpClient);
  private base = `${environment.apiBase}/hoja-vida`;

  // ===== Helpers =====
  private toNumberOrNull(v: unknown): number | null {
    if (v === null || v === undefined || v === '') return null;
    const n = Number(v);
    return Number.isFinite(n) ? n : null;
  }

  // ===== Mapeos =====
  private toCamel(x: DatosFamiliar): DatosFamiliarCamel {
    return {
      idDatosFamiliares: x.id_datos_familiares,
      idDatosPersonal: x.id_datos_personal,
      codigoParentesco: x.codigo_parentesco ?? null,
      nombreDatosFamiliar: x.nombre_datos_familiar,
      documentoDatosFamiliar: x.documento_datos_familiar ?? null,
      fechaNacimiento: x.fecha_nacimiento ?? null,
      telefonoDatosFamiliar: x.telefono_datos_familiar ?? null,
      celularDatosFamiliar: x.celular_datos_familiar ?? null,
      direccionDatosFamiliar: x.direccion_datos_familiar,
      idDepartamento: x.id_departamento ?? null,
      idCiudad: x.id_ciudad ?? null,
      ingresosDatosFamiliar: this.toNumberOrNull(x.ingresos_datos_familiar),
      egresosDatosFamiliar: this.toNumberOrNull(x.egresos_datos_familiar),
      referenciaFamiliar: x.referencia_familiar ?? null,
    };
  }

  private toSnakeNonNull(y: DatosFamiliarCamel): DatosFamiliar {
    return {
      id_datos_familiares: y.idDatosFamiliares,
      id_datos_personal: y.idDatosPersonal,
      codigo_parentesco: y.codigoParentesco ?? null,
      nombre_datos_familiar: y.nombreDatosFamiliar,
      documento_datos_familiar: y.documentoDatosFamiliar ?? null,
      fecha_nacimiento: y.fechaNacimiento ?? null,
      telefono_datos_familiar: y.telefonoDatosFamiliar ?? null,
      celular_datos_familiar: y.celularDatosFamiliar ?? null,
      direccion_datos_familiar: y.direccionDatosFamiliar,
      id_departamento: y.idDepartamento ?? null,
      id_ciudad: y.idCiudad ?? null,
      // ✅ fuerza número o null al bajar del back
      ingresos_datos_familiar: this.toNumberOrNull(y.ingresosDatosFamiliar as any),
      egresos_datos_familiar: this.toNumberOrNull(y.egresosDatosFamiliar as any),
      referencia_familiar: y.referenciaFamiliar ?? null,
    };
  }

  private mapArrayToSnake(arr: DatosFamiliarCamel[]): DatosFamiliar[] {
    return arr.map((it) => this.toSnakeNonNull(it));
  }

  // ====== ENDPOINTS ANIDADOS ======

  /** Lista familiares por persona (acepta array o Page en el back). */
  listByPersona(idDatosPersonal: number): Observable<DatosFamiliar[]> {
    const url = `${this.base}/datos-personales/${idDatosPersonal}/familiares`;
    return this.http.get<any>(url).pipe(
      map((resp: any) => {
        if (Array.isArray(resp)) return this.mapArrayToSnake(resp as DatosFamiliarCamel[]);
        if (resp && Array.isArray(resp.content)) return this.mapArrayToSnake(resp.content as DatosFamiliarCamel[]);
        return [];
      })
    );
  }

  /** Crear familiar para una persona (POST anidado). */
  create(payloadSnake: DatosFamiliar): Observable<number | DatosFamiliar> {
    const idPersona = payloadSnake.id_datos_personal;
    if (!idPersona && idPersona !== 0) {
      throw new Error('id_datos_personal es requerido en el payload para crear.');
    }
    const url = `${this.base}/datos-personales/${idPersona}/familiares`;
    const body = this.toCamel(payloadSnake);
    return this.http.post<number | DatosFamiliarCamel | null>(url, body).pipe(
      map(resp => {
        if (typeof resp === 'number') return resp;
        if (resp) return this.toSnakeNonNull(resp as DatosFamiliarCamel);
        return { ...payloadSnake };
      })
    );
  }

  /** Obtener familiar por persona + id (GET anidado). */
  getForPersona(idDatosPersonal: number, idFamiliar: number): Observable<DatosFamiliar> {
    const url = `${this.base}/datos-personales/${idDatosPersonal}/familiares/${idFamiliar}`;
    return this.http.get<DatosFamiliarCamel>(url).pipe(map(obj => this.toSnakeNonNull(obj)));
  }

  /** Actualizar familiar (PUT anidado). Tolera 204/empty body. */
  updateForPersona(
    idDatosPersonal: number,
    idFamiliar: number,
    payloadSnake: DatosFamiliar
  ): Observable<DatosFamiliar> {
    const url = `${this.base}/datos-personales/${idDatosPersonal}/familiares/${idFamiliar}`;
    const body = this.toCamel(payloadSnake);
    return this.http.put<DatosFamiliarCamel | null>(url, body).pipe(
      map(resp => {
        if (resp) return this.toSnakeNonNull(resp);
        return { ...payloadSnake, id_datos_familiares: payloadSnake.id_datos_familiares ?? idFamiliar };
      })
    );
  }

  /** Eliminar familiar (DELETE anidado). */
  deleteForPersona(idDatosPersonal: number, idFamiliar: number): Observable<void> {
    const url = `${this.base}/datos-personales/${idDatosPersonal}/familiares/${idFamiliar}`;
    return this.http.delete<void>(url);
  }

  // ====== Endpoints directos por ID (por compatibilidad) ======

  get(id: number): Observable<DatosFamiliar> {
    const url = `${this.base}/datos-familiares/${id}`;
    return this.http.get<DatosFamiliarCamel>(url).pipe(map(obj => this.toSnakeNonNull(obj)));
  }

  update(id: number, payloadSnake: DatosFamiliar): Observable<DatosFamiliar> {
    const url = `${this.base}/datos-familiares/${id}`;
    const body = this.toCamel(payloadSnake);
    return this.http.put<DatosFamiliarCamel | null>(url, body).pipe(
      map(resp => (resp ? this.toSnakeNonNull(resp) : { ...payloadSnake, id_datos_familiares: id }))
    );
  }

  delete(id: number): Observable<void> {
    const url = `${this.base}/datos-familiares/${id}`;
    return this.http.delete<void>(url);
  }
}
