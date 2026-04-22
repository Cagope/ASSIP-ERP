import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface EventoLiquidacionListDTO {
  idEventoLiquidacion: number;

  idContrato: number;
  idEmpleado: number;
  idAgencia: number;

  nombreEmpleado: string;
  documentoEmpleado: string;

  fechaDocumento: string;
  fechaInicio: string;
  fechaFin: string;
  totalDias: number;

  tipoEvento: string;
  numeroSoporte: string;
  responsablePago: string;

  porcentajeResponsable: number;
  porcentajeEmpresa: number;
  diasEmpresa100: number;

  generaCxc: boolean;
  liquidaArl: boolean;
  esRemunerado: boolean;

  observacion: string;
  estado: string;

  fkSeguridadCreacion: number;
  fechaCreacion: string;
  fkSeguridadEdicion: number;
  fechaEdicion: string;
}

export interface EventoLiquidacionFormDTO {
  idEventoLiquidacion: number | null;

  idContrato: number | null;

  fechaDocumento: string | null;
  fechaInicio: string | null;
  fechaFin: string | null;
  totalDias: number | null;

  tipoEvento: string | null;
  numeroSoporte: string | null;
  responsablePago: string | null;

  porcentajeResponsable: number | null;
  porcentajeEmpresa: number | null;
  diasEmpresa100: number | null;

  generaCxc: boolean | null;
  liquidaArl: boolean | null;
  esRemunerado: boolean | null;

  observacion: string | null;
  estado: string | null;
}

export interface EventoLiquidacionSaveDTO {
  idEventoLiquidacion: number | null;

  idContrato: number | null;

  fechaDocumento: string | null;
  fechaInicio: string | null;
  fechaFin: string | null;
  totalDias: number | null;

  tipoEvento: string | null;
  numeroSoporte: string | null;
  responsablePago: string | null;

  porcentajeResponsable: number | null;
  porcentajeEmpresa: number | null;
  diasEmpresa100: number | null;

  generaCxc: boolean | null;
  liquidaArl: boolean | null;
  esRemunerado: boolean | null;

  observacion: string | null;
  estado: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class EventosLiquidacionApi {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/nomina/eventos-liquidacion`;

  listar(): Observable<EventoLiquidacionListDTO[]> {
    return this.http.get<EventoLiquidacionListDTO[]>(this.baseUrl);
  }

  obtener(id: number): Observable<EventoLiquidacionFormDTO> {
    return this.http.get<EventoLiquidacionFormDTO>(`${this.baseUrl}/${id}`);
  }

  guardar(dto: EventoLiquidacionSaveDTO): Observable<any> {
    return this.http.post<any>(this.baseUrl, dto);
  }

  eliminar(id: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/${id}`);
  }
}
