import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';

export interface NovedadesEmpleadoInformeDTO {
  idNovedad: number;
  idPeriodo: number;
  anio: number;
  mes: number;
  numeroPeriodo: number;
  tipoPeriodo: string;
  estadoPeriodo: string;

  idEmpleado: number;
  idContrato: number;
  idDatosPersonal: number;
  documento: string;
  nombreEmpleado: string;

  idAgencia: number;
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
export class NovedadesEmpleadoInformeApi {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/nomina/informes/novedades-empleado`;

  consultar(filtros: {
    documento?: string | null;
    idEmpleado?: number | null;
    idPeriodo?: number | null;
    codigoConcepto?: string | null;
    fechaInicial?: string | null;
    fechaFinal?: string | null;
  }): Observable<NovedadesEmpleadoInformeDTO[]> {

    let params = new HttpParams();

    if (filtros.documento) {
      params = params.set('documento', filtros.documento);
    }

    if (filtros.idEmpleado) {
      params = params.set('idEmpleado', filtros.idEmpleado);
    }

    if (filtros.idPeriodo) {
      params = params.set('idPeriodo', filtros.idPeriodo);
    }

    if (filtros.codigoConcepto) {
      params = params.set('codigoConcepto', filtros.codigoConcepto);
    }

    if (filtros.fechaInicial) {
      params = params.set('fechaInicial', filtros.fechaInicial);
    }

    if (filtros.fechaFinal) {
      params = params.set('fechaFinal', filtros.fechaFinal);
    }

    return this.http.get<NovedadesEmpleadoInformeDTO[]>(this.baseUrl, { params });
  }
}
