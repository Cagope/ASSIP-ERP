import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../../environments/environment';

export type FinancieroResponse = {
  id_financiero: number;
  id_datos_personal: number;

  valor_salario: number;
  valor_pension: number;
  ingresos_arriendo: number;
  ingresos_comisiones: number;
  otros_ingresos: number;
  comentario_otros_ingresos: string;

  egresos_familiares: number;
  egresos_arriendo: number;
  egresos_credito: number;
  otros_egresos: number;
  comentario_otros_egresos: string;

  total_activos: number;
  total_pasivos: number;

  origen_fondos: string;
  relacion_financiera: string;
  deuda_relacion_financiera: number;
};

export type FinancieroRequest = {
  valorSalario: number;
  valorPension: number;
  ingresosArriendo: number;
  ingresosComisiones: number;
  otrosIngresos: number;
  comentarioOtrosIngresos: string;

  egresosFamiliares: number;
  egresosArriendo: number;
  egresosCredito: number;
  otrosEgresos: number;
  comentarioOtrosEgresos: string;

  totalActivos: number;
  totalPasivos: number;

  origenFondos: string;
  relacionFinanciera: string;
  deudaRelacionFinanciera: number;
};

@Injectable({ providedIn: 'root' })
export class FinancierosApi {
  private http = inject(HttpClient);
  private base = environment.apiBase || '';

  getByPersona(idDatosPersonal: number) {
    return this.http.get<FinancieroResponse>(`${this.base}/hoja-vida/datos-personales/${idDatosPersonal}/financiero`, { observe: 'response' });
  }
  create(idDatosPersonal: number, body: FinancieroRequest) {
    return this.http.post<number>(`${this.base}/hoja-vida/datos-personales/${idDatosPersonal}/financiero`, body);
  }
  update(idDatosPersonal: number, idFinanciero: number, body: FinancieroRequest) {
    return this.http.put<void>(`${this.base}/hoja-vida/datos-personales/${idDatosPersonal}/financiero/${idFinanciero}`, body);
  }
  delete(idDatosPersonal: number, idFinanciero: number) {
    return this.http.delete<void>(`${this.base}/hoja-vida/datos-personales/${idDatosPersonal}/financiero/${idFinanciero}`);
  }
}
