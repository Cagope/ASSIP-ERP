import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../../environments/environment';

export type PermisoEspecialResponse = {
  id_permiso_especial: number;
  id_datos_personal: number;
  recibe_llamadas: boolean;
  recibe_msm: boolean;
  recibe_emails: boolean;
  recibe_cartas: boolean;
  recibe_redes_sociales: boolean;
};

export type PermisoEspecialRequest = {
  recibeLlamadas?: boolean | null;
  recibeMsm?: boolean | null;
  recibeEmails?: boolean | null;
  recibeCartas?: boolean | null;
  recibeRedesSociales?: boolean | null;
};

@Injectable({ providedIn: 'root' })
export class PermisosEspecialesApi {
  private http = inject(HttpClient);
  private base = environment.apiBase || '';

  getByPersona(idDatosPersonal: number) {
    return this.http.get<PermisoEspecialResponse>(`${this.base}/hoja-vida/datos-personales/${idDatosPersonal}/permiso-especial`, { observe: 'response' });
  }
  create(idDatosPersonal: number, body: PermisoEspecialRequest) {
    return this.http.post<number>(`${this.base}/hoja-vida/datos-personales/${idDatosPersonal}/permiso-especial`, body);
  }
  update(idDatosPersonal: number, idPermiso: number, body: PermisoEspecialRequest) {
    return this.http.put<void>(`${this.base}/hoja-vida/datos-personales/${idDatosPersonal}/permiso-especial/${idPermiso}`, body);
  }
  delete(idDatosPersonal: number, idPermiso: number) {
    return this.http.delete<void>(`${this.base}/hoja-vida/datos-personales/${idDatosPersonal}/permiso-especial/${idPermiso}`);
  }
}
