import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface EvaluacionSarlaftRequest {
  // 🔹 Datos base existentes
  idDatosPersonal: number;
  idAgencia: number;
  codigoModulo: string;
  accion: string;
  monto: number;
  fechaUltimaActualizacion: string;
  fechaNacimiento: string;
  tipoDocumento: string;
  codigoFormaAhorro: string;

  // 🔹 Nuevo — Datos económicos / patrimoniales (OPCIONALES)
  ingresosMensuales?: number;
  egresosMensuales?: number;
  totalActivos?: number;
  totalPasivos?: number;
}

export interface EvaluacionSarlaftResponse {
  alerta: boolean;
  severidad: string;
  descripcion: string;
  nombreRegla: string;
  bloqueaOperacion: boolean;
  accionRecomendada: string;
  idAlerta?: number;
}

@Injectable({ providedIn: 'root' })
export class SarlaftApi {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl + '/sarlaft';

  evaluar(body: EvaluacionSarlaftRequest) {
    return this.http.post<EvaluacionSarlaftResponse>(`${this.base}/evaluar`, body);
  }
}
