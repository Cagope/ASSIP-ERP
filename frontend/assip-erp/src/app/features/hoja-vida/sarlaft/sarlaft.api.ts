import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../../environments/environment';

export type SarlaftResponse = {
  id_sarlaft: number;
  id_datos_personal: number;

  exoneracion_uiaf: boolean | null;
  fecha_exoneracion: string | null; // ISO (yyyy-mm-dd)

  asociado_peps: boolean | null;
  tipo_peps: string | null;
  observaciones_peps: string | null;
  fecha_inicial_peps: string | null;
  fecha_final_peps: string | null;

  familia_peps: boolean | null;
  tipo_familia_peps: string | null;
  cedula_familia_peps: string | null;
  codigo_parentesco: string | null;
  nombre_familia_peps: string | null;

  moneda_extranjera: boolean | null;
  observacion_moneda_extranjera: string | null;

  cuenta_extranjero: boolean | null;
  tipo_moneda_extranjera: string | null;
  numero_cuenta_extranjero: string | null;
  nombre_banco_extranjero: string | null;
  ciudad_cuenta_extranjero: string | null;
  pais_cuenta_extranjero: string | null;
};

export type SarlaftRequest = {
  exoneracionUiaf?: boolean | null;
  fechaExoneracion?: string | null;

  asociadoPeps?: boolean | null;
  tipoPeps?: string | null;
  observacionesPeps?: string | null;
  fechaInicialPeps?: string | null;
  fechaFinalPeps?: string | null;

  familiaPeps?: boolean | null;
  tipoFamiliaPeps?: string | null;
  cedulaFamiliaPeps?: string | null;
  codigoParentesco?: string | null;
  nombreFamiliaPeps?: string | null;

  monedaExtranjera?: boolean | null;
  observacionMonedaExtranjera?: string | null;

  cuentaExtranjero?: boolean | null;
  tipoMonedaExtranjera?: string | null;
  numeroCuentaExtranjero?: string | null;
  nombreBancoExtranjero?: string | null;
  ciudadCuentaExtranjero?: string | null;
  paisCuentaExtranjero?: string | null;
};

@Injectable({ providedIn: 'root' })
export class SarlaftApi {
  private http = inject(HttpClient);
  private base = environment.apiBase || '';

  getByPersona(idDatosPersonal: number) {
    return this.http.get<SarlaftResponse>(`${this.base}/hoja-vida/datos-personales/${idDatosPersonal}/sarlaft`, { observe: 'response' });
  }
  create(idDatosPersonal: number, body: SarlaftRequest) {
    return this.http.post<number>(`${this.base}/hoja-vida/datos-personales/${idDatosPersonal}/sarlaft`, body);
  }
  update(idDatosPersonal: number, idSarlaft: number, body: SarlaftRequest) {
    return this.http.put<void>(`${this.base}/hoja-vida/datos-personales/${idDatosPersonal}/sarlaft/${idSarlaft}`, body);
  }
  delete(idDatosPersonal: number, idSarlaft: number) {
    return this.http.delete<void>(`${this.base}/hoja-vida/datos-personales/${idDatosPersonal}/sarlaft/${idSarlaft}`);
  }
}
