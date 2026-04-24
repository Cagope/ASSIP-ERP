import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';

export interface NovedadesConceptoInformeDTO {
  idNovedad: number;

  idPeriodo: number;
  anio: number;
  mes: number;
  numeroPeriodo: number;
  tipoPeriodo: string;

  documento: string;
  nombreEmpleado: string;

  codigoAgencia: string;
  nombreAgencia: string;

  codigoConcepto: string;
  nombreConcepto: string;
  tipoConcepto: string;

  fechaInicial: string | null;
  fechaFinal: string | null;

  cantidad: number;
  valor: number;

  estado: string;
  origen: string;
  observacion: string;
}

@Injectable({ providedIn: 'root' })
export class NovedadesConceptoInformeApi {

  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/nomina/informes/novedades-concepto`;

  consultar(filtros: {
    codigoConcepto?: string | null;
    idEmpleado?: number | null;
    fechaInicial?: string | null;
    fechaFinal?: string | null;
  }): Observable<NovedadesConceptoInformeDTO[]> {

    let params = new HttpParams();

    if (filtros.codigoConcepto) {
      params = params.set('codigoConcepto', filtros.codigoConcepto);
    }

    if (filtros.idEmpleado) {
      params = params.set('idEmpleado', filtros.idEmpleado);
    }

    if (filtros.fechaInicial) {
      params = params.set('fechaInicial', filtros.fechaInicial);
    }

    if (filtros.fechaFinal) {
      params = params.set('fechaFinal', filtros.fechaFinal);
    }

    return this.http.get<NovedadesConceptoInformeDTO[]>(this.baseUrl, { params });
  }
}
