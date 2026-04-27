import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTOs
   ========================================================= */

export interface ConceptoCuentaContableListDTO {
  idMapeo: number;

  codigoConcepto: string;

  idAgencia: number;
  nombreAgencia: string;

  idCuentaDebito: number;
  cuentaDebito: string;

  idCuentaCredito: number;
  cuentaCredito: string;

  activo: boolean;
}

export interface ConceptoCuentaContableFormDTO {
  idMapeo?: number;

  codigoConcepto: string;
  idAgencia: number;

  idCuentaDebito: number | null;
  idCuentaCredito: number | null;

  activo: boolean;
}

export interface ConceptoCuentaContableSaveDTO
  extends ConceptoCuentaContableFormDTO {}

/* =========================================================
   API
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class ConceptoCuentasContablesApi {

  private readonly base =
    `${environment.apiUrl}/nomina/concepto-cuentas-contables`;

  constructor(private http: HttpClient) {}

  // =========================================================
  // LISTAR (con filtro opcional por agencia)
  // =========================================================
  listar(idAgencia?: number): Observable<ConceptoCuentaContableListDTO[]> {

    let params = new HttpParams();

    if (idAgencia !== undefined && idAgencia !== null) {
      params = params.set('idAgencia', idAgencia);
    }

    return this.http.get<ConceptoCuentaContableListDTO[]>(
      this.base,
      { params }
    );
  }

  // =========================================================
  // OBTENER
  // =========================================================
  obtener(idMapeo: number): Observable<ConceptoCuentaContableFormDTO> {
    return this.http.get<ConceptoCuentaContableFormDTO>(
      `${this.base}/${idMapeo}`
    );
  }

  // =========================================================
  // CREAR
  // =========================================================
  crear(data: ConceptoCuentaContableSaveDTO): Observable<void> {
    return this.http.post<void>(this.base, data);
  }

  // =========================================================
  // ACTUALIZAR
  // =========================================================
  actualizar(
    idMapeo: number,
    data: ConceptoCuentaContableSaveDTO
  ): Observable<void> {

    return this.http.put<void>(
      `${this.base}/${idMapeo}`,
      data
    );
  }

  // =========================================================
  // ELIMINAR (SOFT)
  // =========================================================
  eliminar(idMapeo: number): Observable<void> {
    return this.http.delete<void>(
      `${this.base}/${idMapeo}`
    );
  }
}
