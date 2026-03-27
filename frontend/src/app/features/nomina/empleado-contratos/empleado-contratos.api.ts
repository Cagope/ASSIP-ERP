import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* ============================================================
   DTO LISTA (backend enriquecido / vista)
   ============================================================ */

export interface EmpleadoContratoListDTO {

  idContrato: number;

  // =========================
  // Empleado
  // =========================
  idEmpleado: number;
  idDatosPersonal?: number | null;
  documentoEmpleado?: string | null;
  nombreEmpleado?: string | null;

  // =========================
  // Fechas
  // =========================
  fechaInicio: string;
  fechaFin?: string | null;

  // =========================
  // Contrato
  // =========================
  idTipoContrato: number;
  tipoContratoNombre?: string | null;

  periodoPago: string;

  // =========================
  // Sección / Cargo
  // =========================
  idSeccion?: number | null;
  nombreSeccion?: string | null;

  idCargo?: number | null;
  nombreCargo?: string | null;

  // =========================
  // Valores
  // =========================
  salarioBase: number;
  salarioIntegral: boolean;

  // =========================
  // Afiliaciones
  // =========================
  idEps?: number | null;
  nombreEps?: string | null;

  idAfp?: number | null;
  nombreAfp?: string | null;

  idCesantias?: number | null;
  nombreCesantias?: string | null;

  idArl?: number | null;
  nombreArl?: string | null;

  idCajaCompensacion?: number | null;
  nombreCajaCompensacion?: string | null;

  // =========================
  // Cuenta nómina
  // =========================
  idCuentaAhorroNomina?: number | null;
  cuentaNominaDisplay?: string | null;

  idFormaAhorroNomina?: number | null;
  nombreFormaAhorroNomina?: string | null;

  // =========================
  // ARL / Renovación
  // =========================
  fechaEnvioNotaRenovacion?: string | null;

  claseRiesgoArl: number;
  porcentajeArl: number;

  // =========================
  // Estado
  // =========================
  activo: boolean;
}

/* ============================================================
   DTO FORMULARIO (crear / editar)
   ============================================================ */

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

  periodoPago: string;

  idEps?: number | null;
  idAfp?: number | null;
  idCesantias?: number | null;
  idArl?: number | null;
  idCajaCompensacion?: number | null;

  idCuentaAhorroNomina?: number | null;

  fechaEnvioNotaRenovacion?: string | null;

  claseRiesgoArl: number;
  porcentajeArl: number;

  activo: boolean;
}

export interface EmpleadoContratoSaveDTO extends EmpleadoContratoFormDTO {}

/* ============================================================
   API SERVICE
   ============================================================ */

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

  listarPorEmpleado(idEmpleado: number) {
    return this.http.get<EmpleadoContratoListDTO[]>(
      `${this.base}/por-empleado/${idEmpleado}`
    );
  }

}
