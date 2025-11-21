// src/app/features/ses/aportes/aportes.api.ts
import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
// Ajusta la ruta si en tu proyecto usas alias para environments
import { environment } from '../../../../environments/environment';

export interface Aportes {
  tipoIdentificacion: string;
  numeroIdentificacion: string;
  primerApellido: string;
  segundoApellido: string;
  nombres: string;
  nombreCompleto: string;

  fechaIngreso: string;

  saldoAportes: number;
  valorAporteMensual: number;
  valorRevalorizacion: number;
  aportesOrdinarios: number;
  aportesExtraordinarios: number;
  promedioDiaAnual: number;
  fechaUltimoPago: string | null;

  codigoCuenta: string;
}

@Injectable({
  providedIn: 'root'
})
export class AportesApi {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/super/aportes`;

  consultar(fechaCorte: string): Observable<Aportes[]> {
    const params = new HttpParams().set('fechaCorte', fechaCorte);
    return this.http.get<Aportes[]>(this.baseUrl, { params });
  }
}
