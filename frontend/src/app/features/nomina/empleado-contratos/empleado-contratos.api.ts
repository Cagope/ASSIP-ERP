import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface EmpleadoContratoListDTO {
  idContrato: number;
  idEmpleado: number;

  fechaInicio: string; // yyyy-MM-dd
  fechaFin?: string | null;

  salarioBase: number;
  periodoPago: string;

  activo: boolean;
}

export interface EmpleadoContratoFormDTO {
  idContrato?: number;

  idEmpleado: number | null;
  idSeccion?: number | null;

  fechaInicio: string;
  fechaFin?: string | null;

  idTipoContrato: number | null;
  idCargo?: number | null;

  salarioBase: number;
  salarioIntegral: boolean;

  periodoPago: string; // MENSUAL | QUINCENAL

  idEps?: number | null;
  idAfp?: number | null;
  idCesantias?: number | null;
  idArl?: number | null;
  idCajaCompensacion?: number | null;

  cuentaNominaDisplay?: string | null;
  idCuentaAhorroNomina?: number | null;

  fechaEnvioNotaRenovacion?: string | null;

  claseRiesgoArl: number;
  porcentajeArl: number;

  activo: boolean;
}

export interface EmpleadoContratoSaveDTO extends EmpleadoContratoFormDTO {}

@Injectable({ providedIn: 'root' })
export class EmpleadoContratosApi {

  private readonly base = `${environment.apiUrl}/nomina/empleado-contratos`;

  constructor(private http: HttpClient) {}

  listar(): Observable<EmpleadoContratoListDTO[]> {
    return this.http.get<EmpleadoContratoListDTO[]>(this.base);
  }

  obtener(id: number): Observable<EmpleadoContratoFormDTO> {
    return this.http.get<EmpleadoContratoFormDTO>(`${this.base}/${id}`);
  }

  crear(data: EmpleadoContratoSaveDTO): Observable<number> {
    return this.http.post<number>(this.base, data);
  }

  actualizar(id: number, data: EmpleadoContratoSaveDTO): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
