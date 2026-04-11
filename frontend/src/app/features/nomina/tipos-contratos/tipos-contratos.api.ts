import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface TipoContratoDTO {
  idTipoContrato: number;
  codigo: string;
  nombre: string;
  activo: boolean;
  codigoSuperintendencia?: string | null;
}

@Injectable({ providedIn: 'root' })
export class TiposContratosApi {

  private readonly base = `${environment.apiUrl}/nomina/tipos_contratos`;

  constructor(private http: HttpClient) {}

  listar(): Observable<TipoContratoDTO[]> {
    return this.http.get<TipoContratoDTO[]>(this.base);
  }
}
