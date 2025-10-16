// src/app/shared/general/empresas.api.ts
import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment'; // ← CORREGIDO
import { Observable, map } from 'rxjs';

export interface EmpresaDTO {
  tipoDocumento: string;
  documentoEmpresa: string;
  digitoVerificacion?: string | null;

  razonSocial: string;
  siglaEmpresa?: string | null;

  idPaisDocumento?: number | null;
  idDepartamento?: number | null;
  idCiudad?: number | null;

  correoCorporativo?: string | null;
  telefono?: string | null;
  celular?: string | null;
  sitioWeb?: string | null;

  logoUrl?: string | null;
}

@Injectable({ providedIn: 'root' })
export class EmpresasApi {
  private http = inject(HttpClient);
  private base = `${environment.apiBase}/general/empresas`;

  getEmpresaPrincipal(): Observable<EmpresaDTO | null> {
    return this.http.get<EmpresaDTO>(`${this.base}/principal`, { observe: 'response' })
      .pipe(map(res => res.status === 204 ? null : (res.body as EmpresaDTO)));
  }
}
